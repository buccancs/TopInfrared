package com.topdon.hik.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.lifecycleScope
import com.topdon.hik.R
import com.topdon.hik.databinding.ActivityIrCorrectHikFourBinding
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseBindingActivity
import com.topdon.libhik.util.HikHelper
import com.topdon.module.thermal.ir.event.CorrectionFinishEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class IRCorrectHikFourActivity : BaseBindingActivity<ActivityIrCorrectHikFourBinding>() {
    companion object {
        
        private const val COUNT_DOWN_SECOND = 60
    }

    override fun initContentLayoutId(): Int = R.layout.activity_ir_correct_hik_four

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.titleView.setLeftClickListener { showExitTipsDialog() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitTipsDialog()
            }
        })

        HikHelper.bind(this)
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

        binding.timeDownView.onTimeListener = {
            if (it == COUNT_DOWN_SECOND - 10) {
                startCorrect()
            }
        }
        binding.timeDownView.onFinishListener = {
            try {
                if (!isFinishing && !isDestroyed) {
                    TipDialog.Builder(this)
                        .setMessage(R.string.correction_complete)
                        .setPositiveListener(R.string.app_confirm) {
                            EventBus.getDefault().post(CorrectionFinishEvent())
                            finish()
                        }
                        .create().show()
                }
            } catch (_: Exception) {

            }
        }
        lifecycleScope.launch {
            delay(2000)
            binding.timeDownView.downSecond(COUNT_DOWN_SECOND, false)
        }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        binding.timeDownView.cancel()
        EventBus.getDefault().post(CorrectionFinishEvent())
        finish()
    }

    override fun disConnected() {
        binding.timeDownView.cancel()
        EventBus.getDefault().post(CorrectionFinishEvent())
        finish()
    }

    private fun startCorrect() {
        lifecycleScope.launch {
            HikHelper.setTemperatureMode(1)
            HikHelper.setAutoShutter(false)
            HikHelper.startCorrect()
            HikHelper.setAutoShutter(SaveSettingUtil.isAutoShutter)
        }
    }

    private fun showExitTipsDialog() {
        TipDialog.Builder(this)
            .setTitleMessage(getString(R.string.app_tip))
            .setMessage(R.string.tips_cancel_correction)
            .setPositiveListener(R.string.app_yes) {
                EventBus.getDefault().post(CorrectionFinishEvent())
                finish()
            }.setCancelListener(R.string.app_no){
            }
            .create().show()
    }
}