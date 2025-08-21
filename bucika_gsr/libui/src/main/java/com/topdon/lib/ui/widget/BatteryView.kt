package com.topdon.lib.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * TC007 电池电量图标.
 *
 * Created by LCG on 2024/5/22.
 */
class BatteryView : AppCompatImageView {
    /**
     * 当前电量
     */
    var battery = -1
        set(value) {
            field = value
            invalidate()
        }
    /**
     * 当前是否充电中
     */
    var isCharging = false
        set(value) {
            field = value
            invalidate()
        }


    private val paint = Paint()
    private val path = Path()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        paint.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        when (heightMode) {
            MeasureSpec.EXACTLY -> {
                val wantWidth = (heightSize * 58 / 30f).toInt()
                when (widthMode) {
                    MeasureSpec.EXACTLY -> setMeasuredDimension(widthSize, heightSize)
                    MeasureSpec.AT_MOST -> setMeasuredDimension(wantWidth.coerceAtMost(widthSize), heightSize)
                    else -> setMeasuredDimension(wantWidth, heightSize)
                }
            }
            MeasureSpec.AT_MOST -> {
                when (widthMode) {
                    MeasureSpec.EXACTLY -> setMeasuredDimension(widthSize, (widthSize * 30 / 58f).toInt().coerceAtMost(heightSize))
                    MeasureSpec.AT_MOST -> {
                        if (widthSize < 58) {
                            if (heightSize < 30) {//宽✘ 高✘
                                if ((widthSize * 30 / 58f).toInt() <= heightSize) {
                                    setMeasuredDimension(widthSize, (widthSize * 30 / 58f).toInt())
                                } else {
                                    setMeasuredDimension((heightSize * 58 / 30f).toInt(), heightSize)
                                }
                            } else {//宽✘ 高✔
                                setMeasuredDimension(widthSize, (widthSize * 30 / 58f).toInt())
                            }
                        } else {
                            if (heightSize < 30) {//宽✔ 高✘
                                setMeasuredDimension((heightSize * 58 / 30f).toInt(), heightSize)
                            } else {//宽✔ 高✔
                                setMeasuredDimension(58, 30)
                            }
                        }
                    }
                    else -> setMeasuredDimension((widthSize * 30.coerceAtMost(heightSize) / 58f).toInt(), 30.coerceAtMost(heightSize))
                }
            }
            else -> {
                when (widthMode) {
                    MeasureSpec.EXACTLY -> setMeasuredDimension(widthSize, (widthSize * 30 / 58f).toInt())
                    MeasureSpec.AT_MOST -> setMeasuredDimension(58.coerceAtMost(widthSize), (58.coerceAtMost(widthSize) * 30 / 58f).toInt())
                    else -> setMeasuredDimension(58, 30)
                }
            }
        }

        drawWidth = if ((measuredWidth * 30 / 58f).toInt() <= measuredHeight) measuredWidth else (measuredHeight * 58 / 30f).toInt()
        drawHeight = if ((measuredWidth * 30 / 58f).toInt() <= measuredHeight) (measuredWidth * 30 / 58f).toInt() else measuredHeight

        paint.strokeWidth = drawWidth * 2 / 58f

        val levelWidth = drawWidth * 42 / 58f
        val levelHeight = drawHeight * 20 / 30f
        val radius = drawWidth * 4 / 58f
        val left = drawWidth * 5 / 58f
        val top = drawWidth * 5 / 58f
        val right = left + levelWidth
        val bottom = top + levelHeight
        path.rewind()
        path.moveTo(left + radius, top)
        path.lineTo(right - radius, top)
        path.quadTo(right, top, right, top + radius)
        path.lineTo(right, bottom - radius)
        path.quadTo(right, bottom, right - radius, bottom)
        path.lineTo(left + radius, bottom)
        path.quadTo(left, bottom, left, bottom - radius)
        path.lineTo(left, top + radius)
        path.quadTo(left, top, left + radius, top)
    }

    private var drawWidth: Int = 0
    private var drawHeight: Int = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //绘制电池外壳
        val lineSize = drawWidth * 2 / 58f
        val roundSize = drawWidth * 6 / 58f
        val batteryWidth = drawWidth * 50 / 58f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.BUTT
        paint.color = 0xff83808c.toInt()
        canvas.drawRoundRect(lineSize / 2, lineSize / 2, lineSize / 2 + batteryWidth, drawHeight.toFloat() - lineSize / 2, roundSize, roundSize, paint)

        //绘制电池正极
        val anodeWidth = drawWidth * 3 / 58f
        val anodeHeight = drawHeight * 8 / 30f - lineSize
        val anodeX = drawWidth - anodeWidth / 2
        val anodeStartY = (drawHeight - anodeHeight) / 2
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = anodeWidth
        canvas.drawLine(anodeX, anodeStartY, anodeX, anodeStartY + anodeHeight, paint)

        //绘制电量
        if (battery <= 0) {
            return
        }
        val progressWidth = drawWidth * 42 / 58f * battery / 100
        paint.strokeCap = Paint.Cap.BUTT
        paint.color = (if (isCharging) 0xff6dc80e else if (battery <= 10) 0xffeb433e else 0xffffffff).toInt()
        canvas.clipPath(path)
        canvas.drawRect(lineSize + anodeWidth, lineSize + anodeWidth, lineSize + anodeWidth + progressWidth, drawHeight - lineSize - anodeWidth, paint)
    }
}