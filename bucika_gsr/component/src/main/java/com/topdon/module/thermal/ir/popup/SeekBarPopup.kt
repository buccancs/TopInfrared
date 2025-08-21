package com.topdon.module.thermal.ir.popup

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.core.view.isVisible
import com.topdon.module.thermal.ir.databinding.PopSeekBarBinding

/**
 * 有一根 SeekBar 用于拾取值的 PopupWindow.
 *
 * 用于 融合度(带标题)、对比度(无标题)、锐度(无标题) 设置
 *
 * Created by LCG on 2024/12/3.
 *
 * @param hasTitle 是否有标题文字
 */
@SuppressLint("SetTextI18n")
class SeekBarPopup(context: Context, hasTitle: Boolean = false) : PopupWindow() {

    var progress: Int
        get() = binding.seekBar.progress
        set(value) {
            binding.seekBar.progress = value
        }

    var max: Int
        get() = binding.seekBar.max
        set(value) {
            binding.seekBar.max = value
        }

    /**
     * 是否在滑动过程中实时触发回调.
     *
     * true-实时触发  false-滑动停止(stop)时才触发
     */
    var isRealTimeTrigger = false

    /**
     * 进度值拾取事件监听.
     */
    var onValuePickListener: ((progress: Int) -> Unit)? = null


    private val binding: PopSeekBarBinding = PopSeekBarBinding.inflate(LayoutInflater.from(context))

    init {
        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.widthPixels, View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.heightPixels, View.MeasureSpec.AT_MOST)
        binding.tvTitle.isVisible = hasTitle
        binding.root.measure(widthMeasureSpec, heightMeasureSpec)
        binding.tvValue.text = "${progress}%"
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvValue.text = "${progress}%"
                if (isRealTimeTrigger) {
                    onValuePickListener?.invoke(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onValuePickListener?.invoke(seekBar.progress)
            }
        })

        contentView = binding.root
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        isOutsideTouchable = false
    }

    /**
     * @param isDropDown true-放置于anchor下方 false-底边缘与anchor对齐
     */
    fun show(anchor: View, isDropDown: Boolean) {
        if (isDropDown) {
            showAsDropDown(anchor)
        } else {
            showAsDropDown(anchor, 0, -height, Gravity.NO_GRAVITY)
        }
    }
}