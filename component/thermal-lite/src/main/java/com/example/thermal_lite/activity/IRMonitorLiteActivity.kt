package com.example.thermal_lite.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.elvishew.xlog.XLog
import com.energy.ac020library.bean.IrcmdError
import com.energy.irutilslibrary.LibIRTempAC020
import com.energy.irutilslibrary.bean.GainStatus
import com.energy.iruvc.sdkisp.LibIRTemp
import com.energy.iruvc.utils.Line
import com.example.thermal_lite.R
import com.example.thermal_lite.camera.DeviceIrcmdControlManager
import com.example.thermal_lite.fragment.IRMonitorLiteFragment
import com.google.gson.Gson
import com.infisense.usbir.view.ITsTempListener
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.bean.tools.ThermalBean
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.ThermalEntity
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.tools.TimeTool
import com.topdon.lib.ui.dialog.MonitorSelectDialog
import com.topdon.lib.ui.listener.SingleClickListener
import com.topdon.module.thermal.ir.bean.DataBean
import com.topdon.module.thermal.ir.bean.SelectPositionBean
import com.topdon.module.thermal.ir.event.MonitorSaveEvent
import com.topdon.module.thermal.ir.event.ThermalActionEvent
import com.topdon.module.thermal.ir.repository.ConfigRepository
import kotlinx.android.synthetic.main.activity_ir_monitor_lite.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal
import java.math.RoundingMode

@Route(path = RouterConfig.IR_THERMAL_MONITOR_LITE)
open class IRMonitorLiteActivity : BaseActivity(), View.OnClickListener , ITsTempListener {

    private var selectIndex: SelectPositionBean? = null
    val irMonitorLiteFragment = IRMonitorLiteFragment()
    private val bean = ThermalBean()
    private var selectBean: SelectPositionBean = SelectPositionBean()

    override fun initContentView() = R.layout.activity_ir_monitor_lite

    override fun initView() {
        motion_btn.setOnClickListener(object : SingleClickListener() {
            override fun onSingleClick() {
                MonitorSelectDialog.Builder(this@IRMonitorLiteActivity)
                    .setPositiveListener {
                        updateUI()
                        when (it) {
                            1 -> EventBus.getDefault().post(ThermalActionEvent(action = 2001))
                            2 -> EventBus.getDefault().post(ThermalActionEvent(action = 2002))
                            else -> EventBus.getDefault().post(ThermalActionEvent(action = 2003))
                        }
                    }
                    .create().show()
            }
        })
        motion_start_btn.setOnClickListener(this)
    }

    private fun startChart(){
        if (selectIndex == null){
            return
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        selectBean = selectIndex!!
        if (showTask != null && showTask!!.isActive) {
            showTask!!.cancel()
            showTask = null
        }
        title_view.setRightText(R.string.monitor_finish)
        title_view.setRightClickListener {
            recordJob?.cancel()
            lifecycleScope.launch {
                delay(500)
                finish()
            }
        }
        showTask = lifecycleScope.launch {
            var isFirstRead = true
            var errorReadCount = 0
            while (true) {
                delay(1000)
                if (irMonitorLiteFragment!=null){
                    val result: LibIRTemp.TemperatureSampleResult = when (selectBean.type) {
                        1 -> irMonitorLiteFragment!!.getTemperatureView().getPointTemp(selectBean.startPosition)
                        2 -> irMonitorLiteFragment!!.getTemperatureView().getLineTemp(Line(selectBean.startPosition, selectBean.endPosition))
                        else -> irMonitorLiteFragment!!.getTemperatureView().getRectTemp(selectBean.getRect())
                    } ?: continue
                    if (isFirstRead) {
                        if (result.maxTemperature > 200f || result.minTemperature < -200f) {
                            errorReadCount++
                            XLog.w("第 $errorReadCount 次读取到异常数据，max = ${result.maxTemperature} min = ${result.minTemperature}")
                            if (errorReadCount > 10) {
                                XLog.i("连续10次获取到异常数据，认为温度区域稳定")
                                isFirstRead = false
                            }
                            continue
                        } else {
                            isFirstRead = false
                            lifecycleScope.launch(Dispatchers.Main) {
                                ll_time.isVisible = true
                            }
                        }
                    }
                    if (result.maxTemperature >= -270f) {
                        val maxBigDecimal = BigDecimal.valueOf(tempCorrectByTs(result.maxTemperature).toDouble())
                        val minBigDecimal = BigDecimal.valueOf(tempCorrectByTs(result.minTemperature).toDouble())
                        bean.centerTemp = maxBigDecimal.setScale(1, RoundingMode.HALF_UP).toFloat()
                        bean.maxTemp = maxBigDecimal.setScale(1, RoundingMode.HALF_UP).toFloat()
                        bean.minTemp = minBigDecimal.setScale(1, RoundingMode.HALF_UP).toFloat()
                        bean.createTime = System.currentTimeMillis()
                        canUpdate = true
                    }
                }
            }
        }

        monitor_current_vol.text = getString(if (selectIndex!!.type == 1) R.string.chart_temperature else R.string.chart_temperature_high)
        monitor_real_vol.visibility = if (selectIndex!!.type == 1) View.GONE else View.VISIBLE
        monitor_real_img.visibility = if (selectIndex!!.type == 1) View.GONE else View.VISIBLE
        recordThermal()
    }
    private var showTask: Job? = null

    private var isRecord = false
    private var timeMillis = 1000L
    private var canUpdate = false

    private var recordJob: Job? = null
    
    private fun recordThermal() {
        recordJob = lifecycleScope.launch(Dispatchers.IO) {
            isRecord = true
            val thermalId = TimeTool.showDateSecond()
            val startTime = System.currentTimeMillis()
            val typeStr = when (selectBean.type) {
                1 -> "point"
                2 -> "line"
                else -> "fence"
            }
            var time = 0L
            while (isRecord) {
                if (canUpdate) {
                    val entity = ThermalEntity()
                    entity.userId = SharedManager.getUserId()
                    entity.thermalId = thermalId
                    entity.thermal = NumberTools.to02f(bean.centerTemp)
                    entity.thermalMax = NumberTools.to02f(bean.maxTemp)
                    entity.thermalMin = NumberTools.to02f(bean.minTemp)
                    entity.type = typeStr
                    entity.startTime = startTime
                    entity.createTime = System.currentTimeMillis()
                    AppDatabase.getInstance().thermalDao().insert(entity)
                    time++
                    launch(Dispatchers.Main) {
                        mp_chart_view.addPointToChart(bean = entity, selectType = selectBean.type)
                    }
                    delay(timeMillis)
                } else {
                    delay(100)
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    tv_time.text = TimeTool.showVideoLongTime(System.currentTimeMillis() - startTime)
                }
            }
            XLog.w("停止记录, 数据量:$time")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().add(R.id.thermal_fragment, irMonitorLiteFragment).commit()
    }

    override fun initData() {

    }

    override fun onClick(v: View?) {
        when (v) {
            motion_start_btn -> {
                if (selectIndex == null) {
                    MonitorSelectDialog.Builder(this)
                        .setPositiveListener {
                            updateUI()
                            when (it) {
                                1 -> EventBus.getDefault().post(ThermalActionEvent(action = 2001))
                                2 -> EventBus.getDefault().post(ThermalActionEvent(action = 2002))
                                else -> EventBus.getDefault().post(ThermalActionEvent(action = 2003))
                            }
                        }
                        .create().show()
                    return
                }
                lifecycleScope.launch {
                    if (irMonitorLiteFragment.frameReady) {
                        lifecycleScope.launch {
                            if (selectIndex == null){
                                return@launch
                            }
                            irMonitorLiteFragment?.stopTask()
                            thermal_fragment.getViewTreeObserver().addOnGlobalLayoutListener(object :
                                ViewTreeObserver.OnGlobalLayoutListener {
                                override fun onGlobalLayout() {
                                    thermal_fragment.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    irMonitorLiteFragment?.restTempView()
                                    irMonitorLiteFragment?.addTempLine(selectIndex!!)
                                }
                            })
                            motion_action_lay.isVisible = false
                            chart_lay.isVisible = true
                            showCameraLoading()
                            delay(500)
                            dismissCameraLoading()
                            startChart()
                        }
                    }
                }
            }
        }
    }

    fun select(selectIndex: SelectPositionBean?) {
        this.selectIndex = selectIndex
        XLog.i("绘制的点线面：${Gson().toJson(selectIndex)}")
    }

    private fun updateUI() {
        motion_start_btn.visibility = View.VISIBLE
        motion_btn.visibility = View.GONE
    }

    override fun disConnected() {
        super.disConnected()
        finish()
    }

    var config : DataBean?= null
    val basicGainGetValue = IntArray(1)
    var basicGainGetTime = 0L

    override fun tempCorrectByTs(temp: Float?): Float {
        var tempNew = temp
        try {
            if (config == null){
                config = ConfigRepository.readConfig(false)
            }
            val defModel = DataBean()
            if (config!!.radiation == defModel.radiation &&
                defModel.environment == config!!.environment &&
                defModel.distance == config!!.distance){
                return temp!!
            }

            if (System.currentTimeMillis() - basicGainGetTime > 5000L){
                try {
                    val basicGainGet: IrcmdError? = DeviceIrcmdControlManager.getInstance().getIrcmdEngine()
                        ?.basicGainGet(basicGainGetValue)
                }catch (e : Exception){
                    XLog.e("增益获取失败")
                }
                basicGainGetTime = System.currentTimeMillis()
            }
            val params_array = floatArrayOf(
                temp!!, config!!.radiation, config!!.environment,
                config!!.environment, config!!.distance, 0.8f
            )
            if (BaseApplication.instance.tau_data_H == null || BaseApplication.instance.tau_data_L == null) return temp
            tempNew = LibIRTempAC020.temperatureCorrection(
                params_array[0],
                BaseApplication.instance.tau_data_H,
                BaseApplication.instance.tau_data_L,
                params_array[1],
                params_array[2],
                params_array[3],
                params_array[4],
                params_array[5],
                if (basicGainGetValue[0] == 0) GainStatus.LOW_GAIN else GainStatus.HIGH_GAIN
            )
            Log.i(
                TAG,
                "temp correct,${basicGainGetValue[0]} oldTemp = " + params_array[0] + "newtemp = " + tempNew +
                        " ems = " + params_array[1] + " ta = " + params_array[2] + " " +
                        "distance = " + params_array[4] + " hum = " + params_array[5]
            )
        }catch (e : Exception){
            XLog.e("$TAG--温度修正异常：${e.message}")
        }finally {
            return tempNew ?: 0f
        }
    }

    override fun finish() {
        super.finish()
        if(isRecord){
            EventBus.getDefault().post(MonitorSaveEvent())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        showTask?.cancel()
        recordJob?.cancel()
    }
}