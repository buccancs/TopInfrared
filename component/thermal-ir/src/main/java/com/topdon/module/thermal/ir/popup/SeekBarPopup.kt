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

    var isRealTimeTrigger = false

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

    fun show(anchor: View, isDropDown: Boolean) {
        if (isDropDown) {
            showAsDropDown(anchor)
        } else {
            showAsDropDown(anchor, 0, -height, Gravity.NO_GRAVITY)
        }
    }
}