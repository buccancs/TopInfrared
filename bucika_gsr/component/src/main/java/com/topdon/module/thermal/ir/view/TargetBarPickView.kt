package com.topdon.module.thermal.ir.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.module.thermal.ir.R

/**
 * 3D 编辑使用的，长地像 SeekBar 的那个条条.
 */
class TargetBarPickView : View {

    companion object {
        /**
         * 默认条条背景颜色.
         */
        @ColorInt
        private const val DEFAULT_BG_COLOR = 0x7F000000.toInt()
        /**
         * 默认进度条颜色.
         */
        @ColorInt
        private const val DEFAULT_PROGRESS_COLOR = 0xffffffff.toInt()

        /**
         * Thumb 圆角尺寸，单位 dp.
         */
        private const val THUMB_CORNERS = 4f
        /**
         * Thumb 描边尺寸，单位 dp.
         */
        private const val THUMB_STROKE_WIDTH = 1.5f
    }

    var onStartTrackingTouch: ((progress: Int, max: Int) -> Unit)? = null

    var onProgressChanged: ((progress: Int, max: Int) -> Unit)? = null

    var onStopTrackingTouch: ((progress: Int, max: Int) -> Unit)? = null

    /**
     * 根据进度格式化指示 View 文字.
     */
    var valueFormatListener: ((progress: Int) -> String) = {
        it.toString()
    }

    /**
     * 条条进度最大值.
     */
    var max: Int = 100
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    var min: Int = 0
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }



    /**
     * 条条当前进度.
     */
    private var progress: Int
        set(value) {
            if (field != value) {
                field = value.coerceAtLeast(min).coerceAtMost(max)
                invalidate()
            }
        }

    fun setProgressAndRefresh(progress: Int) {
        this.progress = progress
        onProgressChanged?.invoke(this.progress, max)
    }

    /**
     * 条条尺寸，单位 px（横向时是高度，竖向时是宽度）
     */
    private val barSize: Int
    /**
     * 顺时针旋转角度，仅支持 0、90、180、270.
     */
    private val rotate: Int
    /**
     * 标签文字.
     */
    private val labelText: String



    private val path = Path()
    private val paint = TextPaint()
    private val thumbRect = RectF()
    private val barRect = RectF()



    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BarPickView, 0, 0)
        max = typedArray.getInt(R.styleable.BarPickView_android_max, 100)
        min = typedArray.getInt(R.styleable.BarPickView_barMin, 0)
        progress = typedArray.getInt(R.styleable.BarPickView_android_progress, min).coerceAtMost(max).coerceAtLeast(min)
        barSize = typedArray.getInt(R.styleable.BarPickView_barSize, SizeUtils.dp2px(4f))
        rotate = typedArray.getInt(R.styleable.BarPickView_barOrientation, 0)
        labelText = typedArray.getString(R.styleable.BarPickView_barLabel) ?: ""
        val textSize = typedArray.getDimensionPixelSize(R.styleable.BarPickView_android_textSize, SizeUtils.sp2px(13f))
        typedArray.recycle()

        paint.isAntiAlias = true
        paint.textSize = textSize.toFloat()
        paint.strokeWidth = SizeUtils.dp2px(THUMB_STROKE_WIDTH).toFloat()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        parent.requestDisallowInterceptTouchEvent(true)
        val x: Float = event.x - barRect.left
        val y: Float = event.y - barRect.top
        val barWidth: Float = barRect.width()
        val barHeight: Float = barRect.height()
        progress = when (rotate) {
            0 -> (x / barWidth * (max - min) + min).toInt()
            180 -> ((barWidth - x) / barWidth * (max - min) + min).toInt()
            90 -> (y / barHeight * (max - min) + min).toInt()
            else -> ((barHeight - y) / barHeight * (max - min) + min).toInt()
        }.coerceAtLeast(min).coerceAtMost(max)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> onStartTrackingTouch?.invoke(progress, max)
            MotionEvent.ACTION_MOVE -> onProgressChanged?.invoke(progress, max)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                onStopTrackingTouch?.invoke(progress, max)
            }
        }
        return true
    }

    /**
     * 计算 Thumb 宽度，单位 px.
     */
    private fun computeThumbWidth(): Int {
        val minTextWidth = paint.measureText(valueFormatListener.invoke(min)).toInt()
        val maxTextWidth = paint.measureText(valueFormatListener.invoke(max)).toInt()
        return minTextWidth.coerceAtLeast(maxTextWidth) + SizeUtils.dp2px(12f)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val thumbWidth = computeThumbWidth()
        val thumbHeight = paint.fontMetricsInt.bottom - paint.fontMetricsInt.top + SizeUtils.dp2px(4f)
        
        val width: Int = if (rotate == 0 || rotate == 180) {
            if (widthMode == MeasureSpec.UNSPECIFIED) ScreenUtil.getScreenWidth(context) else widthSize
        } else {
            val wantWidth: Int = thumbWidth + paddingStart + paddingEnd
            when (widthMode) {
                MeasureSpec.EXACTLY -> widthSize
                MeasureSpec.AT_MOST -> wantWidth.coerceAtMost(widthSize)
                MeasureSpec.UNSPECIFIED -> wantWidth
                else -> wantWidth
            }
        }

        val height: Int = if (rotate == 0 || rotate == 180) {
            val wantHeight: Int = thumbHeight + paddingTop + paddingBottom
            when (heightMode) {
                MeasureSpec.EXACTLY -> heightSize
                MeasureSpec.AT_MOST -> wantHeight.coerceAtMost(heightSize)
                MeasureSpec.UNSPECIFIED -> wantHeight
                else -> wantHeight
            }
        } else {
            if (heightMode == MeasureSpec.UNSPECIFIED) ScreenUtil.getScreenHeight(context) else heightSize
        }

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }

        computeBarRect()
        computeThumbRect()

        clipToBarRect(canvas)
        drawBgBar(canvas)
        drawProgress(canvas)
        canvas.restore()

        drawThumb(canvas)
//        drawText(canvas)
    }

    private fun computeBarRect() {
        val textHeight = paint.fontMetricsInt.bottom - paint.fontMetricsInt.top
        val textMargin = SizeUtils.dp2px(4f)
        val thumbWidth = computeThumbWidth()
        val thumbHeight = textHeight + SizeUtils.dp2px(4f)
        if (rotate == 0 || rotate == 180) {
            val labelTextSpace = if (labelText.isEmpty()) 0 else (paint.measureText(labelText).toInt() + SizeUtils.dp2px(6f))
            val leftText = valueFormatListener.invoke(if (rotate == 0) min else max)
            val rightText = valueFormatListener.invoke(if (rotate == 0) max else min)
            val leftTextWidth = paint.measureText(leftText).toInt()
            val rightTextWidth = paint.measureText(rightText).toInt()
            val left = paddingStart.toFloat() + leftTextWidth + textMargin + if (rotate == 0) labelTextSpace else 0
            val top = (paddingTop + thumbHeight / 2 - barSize / 2).toFloat()
            val right = (measuredWidth - paddingEnd - rightTextWidth - textMargin - if (rotate == 0) 0 else labelTextSpace).toFloat()
            val bottom = top + barSize
            barRect.set(left, top, right, bottom)
        } else {
            val labelTextSpace = if (labelText.isEmpty()) 0 else (textHeight + SizeUtils.dp2px(6f))
            val left = (paddingStart + thumbWidth / 2 - barSize / 2).toFloat()
            val top = paddingTop.toFloat() + textHeight + textMargin + if (rotate == 90) labelTextSpace else 0
            val right = left + barSize
            val bottom = (measuredHeight - paddingBottom - textHeight - textMargin - if (rotate == 90) 0 else labelTextSpace).toFloat()
            barRect.set(left, top, right, bottom)
        }
    }

    private fun computeThumbRect() {
        val thumbWidth = computeThumbWidth()
        val thumbHeight = paint.fontMetricsInt.bottom - paint.fontMetricsInt.top + SizeUtils.dp2px(4f)
        if (rotate == 0 || rotate == 180) {
            val progressWidth = (barRect.width() * (progress - min) / (max - min)).toInt()
            val left = (if (rotate == 0) (barRect.left + progressWidth - thumbWidth / 2) else (barRect.right - progressWidth - thumbWidth / 2))
                .toInt()
                .coerceAtLeast(barRect.left.toInt())
                .coerceAtMost(barRect.right.toInt() - thumbWidth)
            val right = left + thumbWidth
            val top = paddingTop
            val bottom = measuredHeight - paddingBottom
            thumbRect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        } else {
            val progressHeight = (barRect.height() * (progress - min) / (max - min).toFloat()).toInt()
            val left = paddingStart
            val right = measuredWidth - paddingEnd
            val top = (if (rotate == 90) (barRect.top + progressHeight - thumbHeight / 2) else (barRect.bottom - progressHeight - thumbHeight / 2))
                .toInt()
                .coerceAtLeast(barRect.top.toInt())
                .coerceAtMost(barRect.bottom.toInt() - thumbHeight)
            val bottom = top + thumbHeight
            thumbRect.set(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
        }
    }

    private fun clipToBarRect(canvas: Canvas) {
        canvas.save()
        val radius = (barSize / 2).toFloat()
        if (rotate == 0 || rotate == 180) {
            path.rewind()
            path.moveTo(barRect.left + radius, barRect.top)
            path.lineTo(barRect.right - radius, barRect.top)
            path.quadTo(barRect.right, barRect.top, barRect.right, barRect.top + barSize / 2)
            path.quadTo(barRect.right, barRect.bottom, barRect.right - radius, barRect.bottom)
            path.lineTo(barRect.left + radius, barRect.bottom)
            path.quadTo(barRect.left, barRect.bottom, barRect.left, barRect.bottom - barSize / 2)
            path.quadTo(barRect.left, barRect.top, barRect.left + radius, barRect.top)
            canvas.clipPath(path)
        } else {
            path.rewind()
            path.moveTo(barRect.left, barRect.bottom - radius)
            path.lineTo(barRect.left, barRect.top + radius)
            path.quadTo(barRect.left, barRect.top, barRect.left + barSize / 2, barRect.top)
            path.quadTo(barRect.right, barRect.top, barRect.right, barRect.top + radius)
            path.lineTo(barRect.right, barRect.bottom - radius)
            path.quadTo(barRect.right, barRect.bottom, barRect.right - barSize / 2, barRect.bottom)
            path.quadTo(barRect.left, barRect.bottom, barRect.left, barRect.bottom - radius)
            canvas.clipPath(path)
        }
    }

    private fun drawBgBar(canvas: Canvas) {
        paint.color = DEFAULT_BG_COLOR

        val left = barRect.left
        val top = barRect.top
        val right = barRect.right
        val bottom = barRect.bottom
        if (rotate == 0 || rotate == 180) {
            val thumbWidth = computeThumbWidth()
            val bgWidth = (barRect.width() * (max - progress) / (max - min).toFloat()).toInt()
            if (bgWidth == 0) {
                return
            }
            if (rotate == 0) {
                canvas.drawRect((right - bgWidth + thumbWidth / 2).coerceAtLeast(left + thumbWidth), top, right, bottom, paint)
            } else {
                canvas.drawRect(left, top, (left + bgWidth - thumbWidth / 2).coerceAtMost(right - thumbWidth), bottom, paint)
            }
        } else {
            val thumbHeight = paint.fontMetricsInt.bottom - paint.fontMetricsInt.top + SizeUtils.dp2px(4f)
            val bgHeight = (barRect.height() * (max - progress) / (max - min).toFloat()).toInt()
            if (bgHeight == 0) {
                return
            }
            if (rotate == 90) {
                canvas.drawRect(left, (bottom - bgHeight + thumbHeight / 2).coerceAtLeast(top + thumbHeight), right, bottom, paint)
            } else {
                canvas.drawRect(left, top, right, (top + bgHeight - thumbHeight / 2).coerceAtMost(bottom - thumbHeight), paint)
            }
        }
    }

    private fun drawProgress(canvas: Canvas) {
        paint.color = DEFAULT_PROGRESS_COLOR

        val left = barRect.left
        val top = barRect.top
        val right = barRect.right
        val bottom = barRect.bottom
        if (rotate == 0 || rotate == 180) {
            val thumbWidth = computeThumbWidth()
            val progressWidth = (barRect.width() * (progress - min) / (max - min).toFloat()).toInt()
            if (progressWidth == 0) {
                return
            }
            if (rotate == 0) {
                canvas.drawRect(left, top, (left + progressWidth - thumbWidth / 2).coerceAtMost(right - thumbWidth), bottom, paint)
            } else {
                canvas.drawRect((right - progressWidth + thumbWidth / 2).coerceAtLeast(left + thumbWidth), top, right, bottom, paint)
            }
        } else {
            val thumbHeight = paint.fontMetricsInt.bottom - paint.fontMetricsInt.top + SizeUtils.dp2px(4f)
            val progressHeight = (barRect.height() * (progress - min)  / (max - min).toFloat()).toInt()
            if (progressHeight == 0) {
                return
            }
            if (rotate == 90) {
                canvas.drawRect(left, top, right, (top + progressHeight - thumbHeight / 2).coerceAtMost(bottom - thumbHeight), paint)
            } else {
                canvas.drawRect(left, (bottom - progressHeight + thumbHeight / 2).coerceAtLeast(top + thumbHeight), right, bottom, paint)
            }
        }
    }

    private fun drawThumb(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        val radius = SizeUtils.dp2px(THUMB_CORNERS).toFloat()
        canvas.drawRoundRect(thumbRect, radius, radius, paint)
    }
}