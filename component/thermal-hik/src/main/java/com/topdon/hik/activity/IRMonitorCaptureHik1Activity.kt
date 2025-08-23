package com.topdon.hik.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.hik.R
import com.topdon.hik.databinding.ActivityIrMonitorCaptureHik1Binding
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseBindingActivity
import com.topdon.lib.ui.dialog.MonitorSelectDialog
import com.topdon.libhik.util.HikHelper
import com.topdon.module.thermal.ir.bean.DataBean
import com.topdon.module.thermal.ir.bean.SelectPositionBean
import com.topdon.module.thermal.ir.repository.ConfigRepository
import com.topdon.module.thermal.ir.view.TemperatureBaseView.Mode
import kotlinx.coroutines.launch

@Route(path = RouterConfig.IR_HIK_MONITOR_CAPTURE1)
class IRMonitorCaptureHik1Activity : BaseBindingActivity<ActivityIrMonitorCaptureHik1Binding>(), View.OnClickListener {

    private var hasClickNext = false

    private var selectIndex: SelectPositionBean? = null

    override fun initContentLayoutId(): Int = R.layout.activity_ir_monitor_capture_hik1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HikHelper.init(this)
        HikHelper.setTempListener {
            binding.temperatureView.refreshTemp(it)
        }
        HikHelper.setFrameListener { yuvArray, tempArray ->
            binding.hikSurfaceView.refresh(yuvArray, tempArray)
        }
        HikHelper.onTimeoutListener = {
            TipDialog.Builder(this)
                .setMessage("机芯出了毛病，5秒了没个回调过来")
                .setPositiveListener(R.string.app_got_it) {
                    finish()
                }
                .create().show()
        }
        HikHelper.onReadyListener = {
            lifecycleScope.launch {

                HikHelper.initConfig()
                HikHelper.setAutoShutter(true)
                HikHelper.setContrast(50)
                HikHelper.setEnhanceLevel(50)

                val config: DataBean = ConfigRepository.readConfig(false)
                HikHelper.setEmissivity((config.radiation * 100).toInt())
                HikHelper.setDistance((config.distance * 100).toInt().coerceAtLeast(30))
            }
        }

        binding.temperatureView.mode = Mode.CLEAR
        binding.temperatureView.tempTextSize = SaveSettingUtil.tempTextSize
        binding.temperatureView.onPointListener = {
            if (it.isNotEmpty()) {
                selectIndex = SelectPositionBean(1, it.first())
            }
        }
        binding.temperatureView.onLineListener = {
            if (it.size > 1) {
                selectIndex = SelectPositionBean(2, it[0], it[1])
            }
        }
        binding.temperatureView.onRectListener = {
            if (it.isNotEmpty()) {
                selectIndex = SelectPositionBean(it.first())
            }
        }

        binding.btnSelect.setOnClickListener(this)
        binding.btnStart.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            HikHelper.startStream()
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        if (!hasClickNext) {
            HikHelper.stopStream()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!hasClickNext) {
            HikHelper.release()
        }
    }

    override fun disConnected() {
        finish()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSelect -> {
                showSelectDialog()
            }
            binding.btnStart -> {
                if (selectIndex == null) {
                    showSelectDialog()
                } else {
                    hasClickNext = true
                    HikHelper.stopStream()
                    HikHelper.release()
                    val intent = Intent(this, IRMonitorCaptureHik2Activity::class.java)
                    intent.putExtra("select", selectIndex)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun showSelectDialog() {
        MonitorSelectDialog.Builder(this)
            .setPositiveListener {
                binding.btnStart.isVisible = true
                binding.btnSelect.isVisible = false
                when (it) {
                    1 -> binding.temperatureView.mode = Mode.POINT
                    2 -> binding.temperatureView.mode = Mode.LINE
                    3 -> binding.temperatureView.mode = Mode.RECT
                }
            }
            .create().show()
    }
}