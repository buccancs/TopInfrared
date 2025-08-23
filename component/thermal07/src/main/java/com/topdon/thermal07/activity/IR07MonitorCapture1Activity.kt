package com.topdon.thermal07.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.lib.ui.dialog.MonitorSelectDialog
import com.topdon.module.thermal.ir.repository.ConfigRepository
import com.topdon.module.thermal.ir.view.TemperatureBaseView.Mode
import com.topdon.tc004.activity.video.PlayFragment
import com.topdon.thermal07.R
import com.topdon.thermal07.bean.SelectInfoBean
import kotlinx.android.synthetic.main.activity_ir_07_monitor_capture1.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.easydarwin.video.Client

@Route(path = RouterConfig.IR_MONITOR_CAPTURE_07)
class IR07MonitorCapture1Activity : BaseActivity(), View.OnClickListener {

    companion object {
        private const val RTSP_URL = "rtsp://192.168.40.1/stream0"
    }

    private var selectInfo: SelectInfoBean? = null

    override fun initContentView(): Int = R.layout.activity_ir_07_monitor_capture1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val playFragment = PlayFragment.newInstance(RTSP_URL, Client.TRANSTYPE_TCP, 1, null,true)
            supportFragmentManager.beginTransaction().add(R.id.fl_rtsp, playFragment).commit()
        }
    }

    override fun initView() {
        geometry_view.mode = Mode.CLEAR
        geometry_view.setImageSize(8191, 8191)
        geometry_view.onPointListener = {
            if (it.isNotEmpty()) {
                selectInfo = SelectInfoBean(it.first())
            }
            lifecycleScope.launch {
                TC007Repository.setTempPointList(it)
            }
        }
        geometry_view.onLineListener = {
            if (it.size > 1) {
                selectInfo = SelectInfoBean(it[0], it[1])
            }
            lifecycleScope.launch {
                TC007Repository.setTempLineList(it)
            }
        }
        geometry_view.onRectListener = {
            if (it.isNotEmpty()) {
                selectInfo = SelectInfoBean(it.first())
            }
            lifecycleScope.launch {
                TC007Repository.setTempRectList(it)
            }
        }

        motion_btn.setOnClickListener(this)
        motion_start_btn.setOnClickListener(this)

        initConfig()
    }

    private fun initConfig() {
        lifecycleScope.launch(Dispatchers.IO) {
            val config = ConfigRepository.readConfig(true)
            TC007Repository.setIRConfig(config.environment, config.distance, config.radiation)
            TC007Repository.setEnvAttr(SharedManager.getTemperature() == 1,0)
            TC007Repository.clearAllTemp()
            TC007Repository.setTempFrame(false)
        }
    }

    override fun initData() {
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        if (!isTS004) {
            finish()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            motion_btn -> {
                showMonitorSelectDialog()
            }
            motion_start_btn -> {
                if (selectInfo == null) {
                    showMonitorSelectDialog()
                    return
                }
                val intent = Intent(this, IR07MonitorCapture2Activity::class.java)
                intent.putExtra("select", selectInfo)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showMonitorSelectDialog() {
        MonitorSelectDialog.Builder(this)
            .setPositiveListener {
                motion_start_btn.isVisible = true
                motion_btn.isVisible = false
                when (it) {
                    1 -> geometry_view.mode = Mode.POINT
                    2 -> geometry_view.mode = Mode.LINE
                    3 -> geometry_view.mode = Mode.RECT
                }
            }
            .create().show()
    }
}