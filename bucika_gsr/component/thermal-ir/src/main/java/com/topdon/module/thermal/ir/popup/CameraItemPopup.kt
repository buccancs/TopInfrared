package com.topdon.module.thermal.ir.popup

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ToastUtils
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.TipShutterDialog
import com.topdon.libcom.bean.SaveSettingBean
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.databinding.PopCameraItemBinding

/**
 * 热成像 拍照/录像 菜单.
 *
 * Created by LCG on 2025/1/3.
 */
@SuppressLint("SetTextI18n")
class CameraItemPopup(val context: Context, private val saveSetBean: SaveSettingBean) : PopupWindow(), View.OnClickListener {

    /**
     * 手动快门是否处于选中状态
     */
    var isShutterSelect: Boolean
        get() = binding.ivShutter.isSelected
        set(value) {
            binding.ivShutter.isSelected = value
        }
    /**
     * 录音开关是否处于选中状态
     */
    var isAudioSelect: Boolean
        get() = binding.ivAudio.isSelected
        set(value) {
            binding.ivAudio.isSelected = value
        }



    /**
     * 延时秒数点击事件监听，返回值为是否响应该次点击事件
     */
    var onDelayClickListener: (() -> Boolean)? = null
    /**
     * 自动快门开启关闭事件监听.
     */
    var onAutoCLickListener: ((isOpen: Boolean) -> Unit)? = null
    /**
     * 手动快门点击事件监听.
     */
    var onShutterClickListener: (() -> Unit)? = null
    /**
     * 录音开启关闭事件监听.
     */
    var onAudioCLickListener: (() -> Unit)? = null


    private val binding: PopCameraItemBinding = PopCameraItemBinding.inflate(LayoutInflater.from(context))

    init {
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.widthPixels, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.heightPixels, View.MeasureSpec.AT_MOST)
        binding.root.measure(widthMeasureSpec, heightMeasureSpec)

        contentView = binding.root
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        isOutsideTouchable = false

        binding.ivDelay.setImageLevel(saveSetBean.delayCaptureSecond)
        binding.ivAuto.isSelected = saveSetBean.isAutoShutter
        binding.ivAudio.isSelected = saveSetBean.isRecordAudio && XXPermissions.isGranted(context, Permission.RECORD_AUDIO)

        binding.clDelay.setOnClickListener(this)
        binding.clAuto.setOnClickListener(this)
        binding.clShutter.setOnClickListener(this)
        binding.clAudio.setOnClickListener(this)
        binding.clSetting.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.clDelay -> if (onDelayClickListener?.invoke() == true) {
                when (saveSetBean.delayCaptureSecond) {
                    0 -> {
                        saveSetBean.delayCaptureSecond = 3
                        ToastUtils.showShort(R.string.seconds_dalay_3)
                    }
                    3 -> {
                        saveSetBean.delayCaptureSecond = 6
                        ToastUtils.showShort(R.string.seconds_dalay_6)
                    }
                    6 -> {
                        saveSetBean.delayCaptureSecond = 0
                        ToastUtils.showShort(R.string.off_photography)
                    }
                }
                binding.ivDelay.setImageLevel(saveSetBean.delayCaptureSecond)
            }
            binding.clAuto -> {//自动快门
                saveSetBean.isAutoShutter = !saveSetBean.isAutoShutter
                binding.ivAuto.isSelected = saveSetBean.isAutoShutter
                if (SharedManager.isTipShutter && !saveSetBean.isAutoShutter) {
                    TipShutterDialog.Builder(context)
                        .setMessage(R.string.shutter_tips)
                        .setCancelListener { isCheck ->
                            SharedManager.isTipShutter = !isCheck
                        }
                        .create().show()
                }
                onAutoCLickListener?.invoke(saveSetBean.isAutoShutter)
            }
            binding.clShutter -> if (!binding.ivShutter.isSelected) {
                onShutterClickListener?.invoke()
            }
            binding.clAudio -> onAudioCLickListener?.invoke()
            binding.clSetting -> ARouter.getInstance().build(RouterConfig.IR_CAMERA_SETTING).navigation(context)
        }
    }

    fun showAsUp(anchor: View) {
        val locationArray = IntArray(2)
        anchor.getLocationInWindow(locationArray)
        showAtLocation(anchor, Gravity.NO_GRAVITY, 0, locationArray[1] - height)
    }
}