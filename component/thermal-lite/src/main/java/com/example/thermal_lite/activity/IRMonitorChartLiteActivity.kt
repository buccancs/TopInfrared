package com.example.thermal_lite.activity

import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.example.thermal_lite.IrConst
import com.example.thermal_lite.R
import com.example.thermal_lite.camera.DeviceIrcmdControlManager
import com.example.thermal_lite.fragment.IRMonitorLiteFragment
import com.example.thermal_lite.util.CommonUtil
import com.infisense.usbir.view.ITsTempListener
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.bean.event.device.DeviceCameraEvent
import com.topdon.lib.core.bean.tools.ThermalBean
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.ThermalEntity
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.tools.TimeTool
import com.topdon.lms.sdk.LMS.mContext
import com.topdon.module.thermal.ir.bean.DataBean
import com.topdon.module.thermal.ir.bean.SelectPositionBean
import com.topdon.module.thermal.ir.event.MonitorSaveEvent
import com.topdon.module.thermal.ir.repository.ConfigRepository
import kotlinx.android.synthetic.main.activity_ir_monitor_chart_lite.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.math.BigDecimal
import java.math.RoundingMode

@Route(path = RouterConfig.IR_MONITOR_CHART_LITE)
class IRMonitorChartLiteActivity : BaseActivity(),ITsTempListener {

    private var selectBean: SelectPositionBean = SelectPositionBean()

    private val bean = ThermalBean()
    var irMonitorLiteFragment : IRMonitorLiteFragment ?= null
    protected var tau_data_H: ByteArray? = null
    protected var tau_data_L: ByteArray? = null

    override fun initContentView() = R.layout.activity_ir_monitor_chart_lite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectBean = intent.getParcelableExtra("select")!!
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                if (BaseApplication.instance.tau_data_H == null){
                    BaseApplication.instance.tau_data_H = CommonUtil.getAssetData(mContext, IrConst.TAU_HIGH_GAIN_ASSET_PATH)
                }
                if (BaseApplication.instance.tau_data_L == null){
                    BaseApplication.instance.tau_data_L = CommonUtil.getAssetData(mContext, IrConst.TAU_LOW_GAIN_ASSET_PATH)
                }
            }
            delay(1000)
            irMonitorLiteFragment = IRMonitorLiteFragment()
            val args = Bundle()
            args.putParcelable("select", selectBean)
            irMonitorLiteFragment?.arguments = args
            supportFragmentManager.beginTransaction().add(R.id.thermal_lay, irMonitorLiteFragment!!).commit()
            delay(1000)
            recordThermal()
        }
    }

    override fun initView() {
        title_view.setRightClickListener {
            recordJob?.cancel()
            lifecycleScope.launch {
                delay(500)
                finish()
            }
        }

        monitor_current_vol.text = getString(if (selectBean.type == 1) R.string.chart_temperature else R.string.chart_temperature_high)
        monitor_real_vol.visibility = if (selectBean.type == 1) View.GONE else View.VISIBLE
        monitor_real_img.visibility = if (selectBean.type == 1) View.GONE else View.VISIBLE

    }

    override fun finish() {
        super.finish()
        EventBus.getDefault().post(MonitorSaveEvent())
    }

    private var showTask: Job? = null

    override fun initData() {
        if (showTask != null && showTask!!.isActive) {
            showTask!!.cancel()
            showTask = null
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
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mp_chart_view.highlightValue(null)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        super.onDestroy()
        recordJob?.cancel()
    }

    override fun disConnected() {
        super.disConnected()
        finish()
    }

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun cameraEvent(event: DeviceCameraEvent) {
        when (event.action) {
            100 -> {
                showCameraLoading()
            }
            101 -> {
                dismissCameraLoading()
            }
        }
    }

    var config : DataBean ?= null
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
            if (tau_data_H == null || tau_data_L == null) return temp
            tempNew = LibIRTempAC020.temperatureCorrection(
                params_array[0],
                tau_data_H,
                tau_data_L,
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
}