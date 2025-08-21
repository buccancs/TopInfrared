package com.topdon.lib.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.ui.R
import kotlin.math.roundToInt


/**
 * 支持竖向的 SeekBar。
 * 暂不支持 thumbOffset.
 */
class Comm3DSeekBar: AppCompatSeekBar {
    private lateinit var mPaint: TextPaint

    /**
     * 0-横向 1-竖向
     */
    private val orientation: Int

    private var mMaxWidth = 48
    private var mMaxHeight = 48
    private var mMinWidth = 24
    private var mMinHeight = 24
    var level = 0;

    // 进度文字位置信息
    private val mProgressTextRect: Rect = Rect()

    // 滑块按钮宽度
    private val mThumbWidth: Int = SizeUtils.dp2px(50f)

    // 进度指示器宽度
    private val mIndicatorWidth: Int = SizeUtils.dp2px(50f)
    private var onSeekBarChangeListener: OnSeekBarChangeListener? = null

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CommSeekBar, defStyleAttr, 0)
        orientation = typedArray.getInt(R.styleable.CommSeekBar_android_orientation, 0)
        mMaxWidth = typedArray.getDimensionPixelSize(R.styleable.CommSeekBar_android_maxWidth, mMaxWidth)
        mMaxHeight = typedArray.getDimensionPixelSize(R.styleable.CommSeekBar_android_maxHeight, mMaxHeight)
        mMinWidth = typedArray.getDimensionPixelSize(R.styleable.CommSeekBar_android_minWidth, mMinWidth)
        mMinHeight = typedArray.getDimensionPixelSize(R.styleable.CommSeekBar_android_minHeight, mMinHeight)
        mPaint = TextPaint()
        mPaint.setAntiAlias(true)
        mPaint.setColor(Color.parseColor("#00574B"))
        mPaint.setTextSize(SizeUtils.sp2px(16f).toFloat())
        typedArray.recycle()
    }

    override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
        if (orientation == 0) {
            super.setOnSeekBarChangeListener(l)
        } else {
            onSeekBarChangeListener = l
        }
    }

    override fun setProgress(progress: Int) {
        super.setProgress(progress)
        if (orientation != 0) {
            onSeekBarChangeListener?.onProgressChanged(this, progress, false)
        }
    }

    override fun setProgress(progress: Int, animate: Boolean) {
        super.setProgress(progress, animate)
        if (orientation != 0) {
            onSeekBarChangeListener?.onProgressChanged(this, progress, false)
        }
    }

    override fun setMax(max: Int) {
        super.setMax(max)
        if (orientation != 0) {
            onSeekBarChangeListener?.onProgressChanged(this, progress, false)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (orientation == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        } else {
            val d = progressDrawable

            val thumbWidth = thumb?.intrinsicWidth ?: 0
            var dw = 0
            var dh = 0
            if (d != null) {
                dw = mMinWidth.coerceAtLeast(mMaxWidth.coerceAtMost(d.intrinsicWidth))
                dw = thumbWidth.coerceAtLeast(dw)
                dh = mMinHeight.coerceAtLeast(mMaxHeight.coerceAtMost(d.intrinsicHeight))
            }
            dw += paddingLeft + paddingRight
            dh += paddingTop + paddingBottom

            setMeasuredDimension(
                resolveSizeAndState(dw, widthMeasureSpec, 0),
                resolveSizeAndState(dh, heightMeasureSpec, 0)
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (orientation != 0) {
            calculateDrawable(w, h)
        }
    }



    private fun calculateDrawable(w: Int, h: Int) {
        val paddingWidth: Int = w - paddingLeft - paddingRight
        val paddingHeight: Int = h - paddingTop - paddingBottom

        val trackWidth = mMaxWidth.coerceAtMost(paddingWidth)
        val thumbWidth = thumb?.intrinsicWidth ?: 0
        val thumbHeight = thumb?.intrinsicHeight ?: 0

        val trackOffset: Int
        val thumbTopOffset: Int
        if (thumbWidth > trackWidth) {
            val offsetHeight = (paddingWidth - thumbWidth) / 2
            trackOffset = offsetHeight + (thumbWidth - trackWidth) / 2
            thumbTopOffset = offsetHeight
        } else {
            val offsetHeight = (paddingWidth - trackWidth) / 2
            trackOffset = offsetHeight
            thumbTopOffset = offsetHeight + (trackWidth - thumbWidth) / 2
        }
        if (progressDrawable != null) {
            progressDrawable.setBounds(0, trackOffset, paddingHeight, trackOffset + trackWidth)
        }

        if (thumb != null) {
            val available: Int = paddingHeight - thumbHeight + thumbOffset * 2
            val left = progress / max.toFloat() * available + 0.5f
            val reviseLeft = left.coerceAtLeast(thumbHeight / 2 + 0.5f)
                .coerceAtMost(paddingHeight - thumbHeight / 2 - 0.5f).toInt()
            thumb.setBounds(reviseLeft, thumbTopOffset, reviseLeft + thumbHeight, thumbTopOffset + thumbWidth)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (orientation == 0) {
            super.onDraw(canvas)
//            val progressText = "$progress%"
//            mPaint.getTextBounds(progressText, 0, progressText.length, mProgressTextRect)
//            // 进度百分比
//            val progressRatio = progress.toFloat() / max
//            // thumb偏移量
//            val thumbOffset: Float =
//                (mThumbWidth - mProgressTextRect.width()) / 2 - mThumbWidth * progressRatio
//            val thumbX = width * progressRatio + thumbOffset
//            val thumbY: Float = height / 2f + mProgressTextRect.height() / 2f
//            canvas!!.drawText(progressText, thumbX, thumbY, mPaint)
        } else {
            canvas?.let {
                it.rotate(90f)
                it.translate(-paddingStart.toFloat(), -width.toFloat() + paddingEnd)
                super.onDraw(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (orientation == 0) {
            return super.onTouchEvent(event)
        }
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                trackTouchEvent(event)
                onSeekBarChangeListener?.onStartTrackingTouch(this)
            }
            MotionEvent.ACTION_MOVE -> {
                trackTouchEvent(event)
            }
            MotionEvent.ACTION_UP -> {
                isPressed = false
                trackTouchEvent(event)
                invalidate()
                onSeekBarChangeListener?.onStopTrackingTouch(this)
            }
            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                invalidate()
                stopTrackTouchLevel()
                onSeekBarChangeListener?.onStopTrackingTouch(this)
            }
        }
        return true
    }

    /**
     * 通过级别分层进行粘性处理
     */
    fun stopTrackTouchLevel(){
        if (level > 0){
            val newLevel = (progress.toFloat() / 100 * 4).roundToInt()
            setProgress((newLevel.toFloat() / level * 100).toInt())
         }
    }


    private fun trackTouchEvent(event: MotionEvent) {
        val y = event.y.roundToInt()
        progress = if (y < paddingTop) {
            0
        } else if (y > height - paddingBottom) {
            max
        } else {
            val availableHeight: Int = height - paddingTop - paddingBottom
            val scale: Float = (y - paddingTop) / availableHeight.toFloat()
            (scale * max).roundToInt()
        }
        stopTrackTouchLevel()
        if (thumb != null) {
            calculateDrawable(width, height)
            invalidate()
        }
        onSeekBarChangeListener?.onProgressChanged(this, progress, true)
    }
}