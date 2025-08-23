package com.topdon.thermal07.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.ThermalEntity
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.lib.core.socket.WebSocketProxy
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.tools.TimeTool
import com.topdon.module.thermal.ir.event.MonitorSaveEvent
import com.topdon.tc004.activity.video.PlayFragment
import com.topdon.thermal07.R
import com.topdon.thermal07.bean.SelectInfoBean
import kotlinx.android.synthetic.main.activity_ir_07_monitor_capture2.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.easydarwin.video.Client
import org.greenrobot.eventbus.EventBus

class IR07MonitorCapture2Activity : BaseActivity() {

    companion object {
        private const val RTSP_URL = "rtsp://192.168.40.1/stream0"
    }

    private var selectInfo = SelectInfoBean()

    override fun initContentView(): Int = R.layout.activity_ir_07_monitor_capture2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val playFragment = PlayFragment.newInstance(RTSP_URL, Client.TRANSTYPE_TCP, 1, null,true)
            supportFragmentManager.beginTransaction().add(R.id.fl_rtsp, playFragment).commit()
        }
    }

    override fun initView() {
        title_view.setRightClickListener {
            EventBus.getDefault().post(MonitorSaveEvent())
            finish()
        }

        selectInfo = intent.getParcelableExtra("select")!!

        monitor_current_vol.text = getString(if (selectInfo.type == 1) R.string.chart_temperature else R.string.chart_temperature_high)
        monitor_real_vol.visibility = if (selectInfo.type == 1) View.GONE else View.VISIBLE
        monitor_real_img.visibility = if (selectInfo.type == 1) View.GONE else View.VISIBLE

        addCallback(selectInfo)
    }

    override fun initData() {
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
        CoroutineScope(Dispatchers.IO).launch {
            TC007Repository.clearAllTemp()
        }
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        if (!isTS004) {
            EventBus.getDefault().post(MonitorSaveEvent())
            finish()
        }
    }

    private fun addCallback(selectInfo: SelectInfoBean) {
        var lastSaveTime: Long = 0
        val thermalId = TimeTool.showDateSecond()
        val startTime = System.currentTimeMillis()

        WebSocketProxy.getInstance().setOnFrameListener(this) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSaveTime > 1000) {
                lastSaveTime = currentTime

                val maxValue = when (selectInfo.type) {
                    1 -> it.p1Value / 10f
                    2 -> it.l1MaxValue / 10f
                    else -> it.r1MaxValue / 10f
                }
                val minValue = when (selectInfo.type) {
                    1 -> it.p1Value / 10f
                    2 -> it.l1MinValue / 10f
                    else -> it.r1MinValue / 10f
                }
                val entity = ThermalEntity()
                entity.userId = SharedManager.getUserId()
                entity.thermalId = thermalId
                entity.thermal = NumberTools.to02f(maxValue)
                entity.thermalMax = NumberTools.to02f(maxValue)
                entity.thermalMin = NumberTools.to02f(minValue)
                entity.startTime = startTime
                entity.createTime = currentTime
                entity.type = when (selectInfo.type) {
                    1 -> "point"
                    2 -> "line"
                    else -> "fence"
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getInstance().thermalDao().insert(entity)
                }

                mp_chart_view.addPointToChart(bean = entity, selectType = selectInfo.type)
                tv_time.text = TimeTool.showVideoLongTime(currentTime - startTime)
            }
        }
    }
}