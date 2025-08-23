package com.infisense.usbir.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt
import com.blankj.utilcode.util.SizeUtils
import kotlin.math.max
import kotlin.math.min

class TempDrawHelper {
    companion object {
        
        private val POINT_SIZE: Int = SizeUtils.dp2px(16f)
        
        private val CIRCLE_RADIUS: Int = SizeUtils.dp2px(3f)
        
        private val TEMP_TEXT_OFFSET = SizeUtils.dp2px(6f)

        fun Float.correctPoint(max: Int): Int = this.toInt()
            .coerceAtLeast(POINT_SIZE / 2)
            .coerceAtMost(max - POINT_SIZE / 2)

        fun Float.correct(max: Int): Int = this.toInt()
            .coerceAtLeast(CIRCLE_RADIUS)
            .coerceAtMost(max - CIRCLE_RADIUS)

        fun getRect(width: Int, height: Int): Rect = Rect(CIRCLE_RADIUS, CIRCLE_RADIUS, width - CIRCLE_RADIUS, height - CIRCLE_RADIUS)
    }

    var textSize: Int
        get() = textPaint.textSize.toInt()
        set(value) {
            textPaint.textSize = value.toFloat()
        }

    var textColor: Int
        @ColorInt get() = textPaint.color
        set(@ColorInt value) {
            textPaint.color = value
        }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        linePaint.strokeWidth = SizeUtils.dp2px(1f).toFloat()
        linePaint.color = Color.WHITE

        bluePaint.color = Color.BLUE

        redPaint.color = Color.RED

        textPaint.textSize = SizeUtils.sp2px(14f).toFloat()
        textPaint.color = Color.WHITE

    }

    fun drawPoint(canvas: Canvas, x: Int, y: Int) {
        val left: Float = x - POINT_SIZE / 2f
        val top: Float = y - POINT_SIZE / 2f
        val right: Float = x + POINT_SIZE / 2f
        val bottom: Float = y + POINT_SIZE / 2f
        canvas.drawLine(left, y.toFloat(), right, y.toFloat(), linePaint)
        canvas.drawLine(x.toFloat(), top, x.toFloat(), bottom, linePaint)
    }

    fun drawLine(canvas: Canvas, startX: Int, startY: Int, stopX: Int, stopY: Int) {
        canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), linePaint)
    }

    fun drawRect(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        val leftF: Float = left.toFloat()
        val topF: Float = top.toFloat()
        val rightF: Float = right.toFloat()
        val bottomF: Float = bottom.toFloat()
        val points = floatArrayOf(leftF, topF, rightF, topF, rightF, topF, rightF, bottomF, rightF, bottomF, leftF, bottomF, leftF, bottomF, leftF, topF)
        canvas.drawLines(points, linePaint)
    }

    fun drawCircle(canvas: Canvas, x: Int, y: Int, isMax: Boolean) {
        canvas.drawCircle(x.toFloat(), y.toFloat(), CIRCLE_RADIUS.toFloat(), if (isMax) redPaint else bluePaint)
    }

    fun drawTempText(canvas: Canvas, text: String, width: Int, x: Int, y: Int) {
        var textX: Float = (x + TEMP_TEXT_OFFSET).toFloat()
        var textY: Float = (y - TEMP_TEXT_OFFSET).toFloat()

        val textWidth: Float = textPaint.measureText(text)
        if (x > width - textWidth - TEMP_TEXT_OFFSET) {
            textX = x - TEMP_TEXT_OFFSET - textWidth
        }

        val textFontTop: Float = -textPaint.getFontMetrics().top
        if (y < textFontTop + TEMP_TEXT_OFFSET / 2) {
            textY = y + TEMP_TEXT_OFFSET / 2 + textFontTop
        }

        canvas.drawText(text, textX, textY, textPaint)
    }

    fun drawTrendText(canvas: Canvas, width: Int, height: Int, startX: Int, startY: Int, stopX: Int, stopY: Int) {
        val fontMetrics: Paint.FontMetrics = textPaint.getFontMetrics()
        val textWidth: Float = textPaint.measureText("A")
        val textHeight: Float = -fontMetrics.top

        val minX: Int = min(startX, stopX)
        val maxX: Int = max(startX, stopX)
        val leftX: Float = (minX - textWidth).coerceAtLeast(0f)
        val rightX: Float = maxX.toFloat().coerceAtMost(width - textWidth)

        val minY: Int = min(startY, stopY)
        val maxY: Int = max(startY, stopY)
        val topY: Float = (minY - (-fontMetrics.top + fontMetrics.ascent)).coerceAtLeast(textHeight)
        val bottomY: Float = (maxY + textHeight).coerceAtMost(height.toFloat())

        val k: Float = (startY - stopY).toFloat() / (startX - stopX)
        canvas.drawText("A", leftX, if (k >= 0) topY else bottomY, textPaint)
        canvas.drawText("B", rightX, if (k >= 0) bottomY else topY, textPaint)
    }

    fun drawPointName(canvas: Canvas, name: String, width: Int, height: Int, x: Int, y: Int) {
        val textWidth: Float = textPaint.measureText(name)
        val textHeight: Float = -textPaint.getFontMetrics().top

        var textX = x - textWidth / 2
        var textY = y + POINT_SIZE / 2 + textHeight

        if (textX < 0) {
            textX = 0f
        }
        if (textX + textWidth > width) {
            textX = width - textWidth
        }
        if (textY > height) {
            textY = y - POINT_SIZE / 2 - textPaint.fontMetrics.bottom
        }
        canvas.drawText(name, textX, textY, textPaint)
    }

    fun drawPointRectName(canvas: Canvas, name: String, width: Int, height: Int, left: Int, top: Int, right: Int, bottom: Int) {
        val fontMetrics: Paint.FontMetrics = textPaint.getFontMetrics()
        val textWidth: Float = textPaint.measureText(name)
        val textHeight: Float = -fontMetrics.top
        val centerX: Int = left + (right - left) / 2
        val centerY: Int = top + (bottom - top) / 2
        val offset: Float = (-fontMetrics.ascent + fontMetrics.descent) / 2 - fontMetrics.descent

        var textX: Float = centerX - textWidth / 2
        var textY: Float = centerY + offset

        if (textX < 0) {
            textX = 0f
        }
        if (textX + textWidth > width) {
            textX = width - textWidth
        }
        if (textY < textHeight) {
            textY = textHeight
        }
        if (textY > height) {
            textY = height.toFloat()
        }
        canvas.drawText(name, textX, textY, textPaint)
    }

}