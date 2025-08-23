package com.topdon.module.thermal.ir.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.databinding.ViewTrendBinding
import kotlin.math.min

class TrendView : FrameLayout {

    fun expand() {
        binding.clOpen.isVisible = true
        binding.llClose.isVisible = false
    }

    fun close() {
        binding.clOpen.isVisible = false
        binding.llClose.isVisible = true
    }

    fun refreshChart(tempList: List<Float>) {
        if (isVisible && binding.clOpen.isVisible) {
            binding.viewChartTrend.refresh(tempList)
        }
    }

    fun setToEmpty() {
        binding.viewChartTrend.setToEmpty()
    }

    private lateinit var binding: ViewTrendBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        if (isInEditMode) {
            LayoutInflater.from(context).inflate(R.layout.view_trend, this, true)
        } else {
            binding = ViewTrendBinding.inflate(LayoutInflater.from(context), this, true)

            binding.ivClose.setOnClickListener {
                binding.clOpen.isVisible = false
                binding.llClose.isVisible = true
            }
            binding.ivOpen.setOnClickListener {
                binding.clOpen.isVisible = true
                binding.llClose.isVisible = false
            }
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            binding.viewChartTrend.setToEmpty()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val wantHeight: Int = SizeUtils.dp2px(34f) + (widthSize * 158 / 264f).toInt()
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(wantHeight, heightSize)
            else -> wantHeight
        }

        val newWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
        val newHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(newWidthSpec, newHeightSpec)
    }
}