package com.topdon.hik.activity

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.hik.R
import com.topdon.hik.databinding.ActivityIrCorrectHikThreeBinding
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseBindingActivity
import com.topdon.libhik.util.HikHelper
import com.topdon.module.thermal.ir.bean.DataBean
import com.topdon.module.thermal.ir.repository.ConfigRepository
import kotlinx.coroutines.launch

@Route(path = RouterConfig.IR_HIK_CORRECT_THREE)
class IRCorrectHikThreeActivity : BaseBindingActivity<ActivityIrCorrectHikThreeBinding>() {

    private var hasClickNext = false

    override fun initContentLayoutId(): Int = R.layout.activity_ir_correct_hik_three

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HikHelper.init(this)
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

        binding.tvCorrection.setOnClickListener {
            hasClickNext = true
            HikHelper.stopStream()
            HikHelper.release()
            startActivity(Intent(this, IRCorrectHikFourActivity::class.java))
            finish()
        }
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
}