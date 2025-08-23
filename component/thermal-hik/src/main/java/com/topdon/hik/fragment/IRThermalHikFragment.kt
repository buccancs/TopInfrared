package com.topdon.hik.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import com.topdon.hik.R
import com.topdon.hik.databinding.FragmentIrThermalHikBinding
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseBindingFragment
import com.topdon.libcom.bean.SaveSettingBean
import com.topdon.libhik.util.HikHelper
import com.topdon.module.thermal.ir.bean.DataBean
import com.topdon.module.thermal.ir.repository.ConfigRepository
import kotlinx.coroutines.launch

class IRThermalHikFragment : BaseBindingFragment<FragmentIrThermalHikBinding>() {
    override fun initContentLayoutId(): Int = R.layout.fragment_ir_thermal_hik

    override fun initView(savedInstanceState: Bundle?) {
        HikHelper.bind(this)
        HikHelper.setFrameListener { yuvArray, tempArray ->
            binding.hikSurfaceView.refresh(yuvArray, tempArray)
        }
        HikHelper.onTimeoutListener = {
            TipDialog.Builder(requireContext())
                .setMessage("机芯出了毛病，5秒了没个回调过来")
                .setPositiveListener(R.string.app_got_it) {
                    activity?.finish()
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

        val saveSetBean = SaveSettingBean()
        binding.hikSurfaceView.setPseudoCode(saveSetBean.pseudoColorMode)
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun getBitmap(): Bitmap = binding.hikSurfaceView.getScaleBitmap()
}