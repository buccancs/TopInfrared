package com.topdon.lib.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.lib.ui.R

class TipsSeekBar: ViewGroup, SeekBar.OnSeekBarChangeListener {
    private val tipsPercent: Float
    private val seekPercent: Float

    private val seekBar: SeekBar
    private val tvTips: TextView
    private val tvMin: TextView
    private val tvMax: TextView

    var progress: Int
        get() {
            return seekBar.progress
        }
        set(value) {
            seekBar.progress = value
            if (valueFormatListener != null) {
                tvTips.text = valueFormatListener?.invoke(value)
            }
        }

    /**
     * 指示 View 当前显示的文字.
     */
    var valueText: String
        get() {
            return tvTips.text.toString()
        }
        set(value) {
            tvTips.text = value
        }
    /**
     * seekBar 的 onProgressChange 事件监听.
     */
    var onProgressChangeListener: ((progress: Int, fromUser: Boolean) -> Unit)? = null
    /**
     * seekBar 的 onStopTrackingTouch 事件监听.
     */
    var onStopTrackingTouch: ((progress: Int) -> Unit)? = null
    /**
     * 根据进度格式化指示 View 文字.
     */
    var valueFormatListener: ((progress: Int) -> CharSequence?)? = null
        set(value) {
            tvTips.text = value?.invoke(seekBar.progress)
            field = value
        }

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        // seekBar 的 maxHeight 在 29 以下只能通过 xml 设置实在太蛋疼了，这里只好给当前 View 设置 maxHeight,在 attr 中传递给 seekBar
        val thumb = ContextCompat.getDrawable(context, R.drawable.ic_tips_seek_bar_thumb)
        val thumbWidth = thumb?.intrinsicWidth ?: 0
        seekBar = SeekBar(context, attrs)
        seekBar.splitTrack = false
        seekBar.thumb = thumb
        seekBar.progressDrawable = ContextCompat.getDrawable(context, R.drawable.ui_progress_ir_camera_setting)
        seekBar.setPadding(thumbWidth / 2, 0, thumbWidth / 2, 0)
        seekBar.setOnSeekBarChangeListener(this)
        addView(seekBar, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        tvTips = TextView(context)
        tvTips.text = seekBar.progress.toString()
        tvTips.textSize = 12f
        tvTips.gravity = Gravity.CENTER
        tvTips.paint.isFakeBoldText = true
        tvTips.setTextColor(0xff16131e.toInt())
        tvTips.setBackgroundResource(R.drawable.ic_tips_seek_bar_tips_bg)
        addView(tvTips)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TipsSeekBar, defStyleAttr, 0)
        val minText = typedArray.getText(R.styleable.TipsSeekBar_minText)
        val maxText = typedArray.getText(R.styleable.TipsSeekBar_maxText)
        tipsPercent = typedArray.getFraction(R.styleable.TipsSeekBar_tipsPercent, 1, 1, 0f)
        seekPercent = typedArray.getFraction(R.styleable.TipsSeekBar_seekPercent, 1, 1, 0f)
        typedArray.recycle()

        tvMin = TextView(context)
        tvMin.text = minText
        tvMin.textSize = 14f
        tvMin.setTextColor(0xffffffff.toInt())
        addView(tvMin)

        tvMax = TextView(context)
        tvMax.text = maxText
        tvMax.textSize = 14f
        tvMax.setTextColor(0xffffffff.toInt())
        addView(tvMax)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width = if (widthMode == MeasureSpec.UNSPECIFIED) ScreenUtil.getScreenWidth(context) else widthSize

        for (i in 0 until  childCount) {
            when (val child = getChildAt(i)) {
                seekBar -> {
                    val childWidthSpec = MeasureSpec.makeMeasureSpec((width * seekPercent).toInt(), MeasureSpec.EXACTLY)
                    val childHeightSpc = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST)
                    child.measure(childWidthSpec, if (heightMode == MeasureSpec.EXACTLY) childHeightSpc else heightMeasureSpec)
                }
                tvTips -> {
                    val tipsWidth = (width * tipsPercent).toInt()
                    val tipsHeight = (tipsWidth * 44 / 56f).toInt()
                    val childWidthSpec = MeasureSpec.makeMeasureSpec(tipsWidth, MeasureSpec.EXACTLY)
                    val childHeightSpc = MeasureSpec.makeMeasureSpec(tipsHeight, MeasureSpec.EXACTLY)
                    child.measure(childWidthSpec, childHeightSpc)
                }
                else -> {
                    measureChild(child, widthMeasureSpec, heightMeasureSpec)
                }
            }
        }

        val height = tvTips.measuredHeight + SizeUtils.dp2px(5f) + (seekBar.thumb?.intrinsicHeight ?: seekBar.measuredHeight)
        setMeasuredDimension(width, if (heightMode == MeasureSpec.EXACTLY) heightSize else height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until  childCount) {
            val child = getChildAt(i)
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            when (child) {
                seekBar -> {
                    val top = paddingTop + tvTips.measuredHeight + SizeUtils.dp2px(5f)
                    val left = (measuredWidth - childWidth) / 2
                    child.layout(left, top, left + childWidth, top + childHeight)
                }
                tvTips -> {
                    val seekBarSeeWidth = seekBar.measuredWidth - seekBar.paddingLeft - seekBar.paddingRight
                    val baseLeft = (measuredWidth - seekBarSeeWidth) / 2
                    val progressLeft = (seekBarSeeWidth * seekBar.progress / seekBar.max.toFloat()).toInt()
                    val left = baseLeft + progressLeft - childWidth / 2
                    child.layout(left, paddingTop, left + childWidth, paddingTop + childHeight)
                }
                tvMin -> {
                    val baseTop = paddingTop + tvTips.measuredHeight + SizeUtils.dp2px(5f)
                    val top = baseTop + (seekBar.measuredHeight - childHeight) / 2
                    child.layout(paddingStart, top, paddingStart + childWidth, top + childHeight)
                }
                tvMax -> {
                    val baseTop = paddingTop + tvTips.measuredHeight + SizeUtils.dp2px(5f)
                    val top = baseTop + (seekBar.measuredHeight - childHeight) / 2
                    val left = measuredWidth - paddingEnd - childWidth
                    child.layout(left, top, left + childWidth, top + childHeight)
                }
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        tvTips.text = if (valueFormatListener == null) progress.toString() else valueFormatListener?.invoke(progress)
        requestLayout()
        onProgressChangeListener?.invoke(progress, fromUser)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        onStopTrackingTouch?.invoke(this.seekBar.progress)
    }


}