package com.infisense.usbir.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import androidx.annotation.ColorInt
import com.blankj.utilcode.util.SizeUtils
import kotlin.math.max
import kotlin.math.min

/**
 * TC007、2D编辑、插件的 点线面温度图层有 View、SurfaceView 两种实现，
 * 先用这个工具类抽取相同 draw 逻辑，后续考虑优化。
 *
 * Created by LCG on 2024/12/6.
 */
class TempDrawHelper {
    companion object {
        /**
         * 点是一个十字架，该值为十字架的长度，单位 px.
         */
        private val POINT_SIZE: Int = SizeUtils.dp2px(16f)
        /**
         * 点、线、面、全图 最高温及最低温中心实心圆的半径，单位 px.
         */
        private val CIRCLE_RADIUS: Int = SizeUtils.dp2px(3f)
        /**
         * 温度值文字，与实心圆圆心的偏移量，防止文字与实心圆重叠，X轴为该值，Y轴为该值/2，单位 px.
         */
        private val TEMP_TEXT_OFFSET = SizeUtils.dp2px(6f)



        /**
         * 修正指定十字架的 View 坐标值，确保十字架不会超出 View 界外.
         */
        fun Float.correctPoint(max: Int): Int = this.toInt()
            .coerceAtLeast(POINT_SIZE / 2)
            .coerceAtMost(max - POINT_SIZE / 2)

        /**
         * 修正线、面、最高温点、最低温点的 View 坐标值，确保实心圆不会超出 View 界外。
         */
        fun Float.correct(max: Int): Int = this.toInt()
            .coerceAtLeast(CIRCLE_RADIUS)
            .coerceAtMost(max - CIRCLE_RADIUS)

        /**
         * 获取可保证实心圆不会超出 View 界外的 Rect.
         */
        fun getRect(width: Int, height: Int): Rect = Rect(CIRCLE_RADIUS, CIRCLE_RADIUS, width - CIRCLE_RADIUS, height - CIRCLE_RADIUS)
    }


    /**
     * 温度值文字、趋势图 AB 两个字母、点线面名称 文字大小，单位 px.
     */
    var textSize: Int
        get() = textPaint.textSize.toInt()
        set(value) {
            textPaint.textSize = value.toFloat()
        }

    /**
     * 温度值文字、趋势图 AB 两个字母、点线面名称 文字颜色值.
     */
    var textColor: Int
        @ColorInt get() = textPaint.color
        set(@ColorInt value) {
            textPaint.color = value
        }





    /**
     * 绘制 点、线、面、趋势图直线 Paint，白色.
     * 描边宽度 1dp.
     */
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /**
     * 点、线、面、全图 低温点实心圆 Paint，蓝色.
     */
    private val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /**
     * 点、线、面、全图 高温点实心圆 Paint，红色.
     */
    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /**
     * 高温温度文字、低温温度文字、趋势图 AB 两个字母、点线面名称 Paint，
     * 颜色默认白色，大小默认 14sp，可由文字颜色、大小设置更改.
     */
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)


    init {
        linePaint.strokeWidth = SizeUtils.dp2px(1f).toFloat()
        linePaint.color = Color.WHITE

        bluePaint.color = Color.BLUE

        redPaint.color = Color.RED

        textPaint.textSize = SizeUtils.sp2px(14f).toFloat()
        textPaint.color = Color.WHITE

    }


    /* ******************************************** Draw ******************************************** */
    /**
     * 在 (x,y) 画一个十字.
     *
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     */
    fun drawPoint(canvas: Canvas, x: Int, y: Int) {
        val left: Float = x - POINT_SIZE / 2f
        val top: Float = y - POINT_SIZE / 2f
        val right: Float = x + POINT_SIZE / 2f
        val bottom: Float = y + POINT_SIZE / 2f
        canvas.drawLine(left, y.toFloat(), right, y.toFloat(), linePaint) //画横线
        canvas.drawLine(x.toFloat(), top, x.toFloat(), bottom, linePaint) //画竖线
    }

    /**
     * 连接 (startX, startY)、(stopX, stopY) 两点绘制一条线段.
     *
     * 注意，不对 坐标参数 进行处理，传进来是哪就在哪绘制。
     */
    fun drawLine(canvas: Canvas, startX: Int, startY: Int, stopX: Int, stopY: Int) {
        canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), linePaint)
    }

    /**
     * 按指定范围绘制一个矩形.
     *
     * 注意，不对 坐标参数 进行处理，传进来是哪就在哪绘制。
     */
    fun drawRect(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        val leftF: Float = left.toFloat()
        val topF: Float = top.toFloat()
        val rightF: Float = right.toFloat()
        val bottomF: Float = bottom.toFloat()
        val points = floatArrayOf(leftF, topF, rightF, topF, rightF, topF, rightF, bottomF, rightF, bottomF, leftF, bottomF, leftF, bottomF, leftF, topF)
        canvas.drawLines(points, linePaint)
    }



    /**
     * 在 (x,y) 画一个实心圆。
     *
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     * @param isMax true-最高温红色 false-最低温蓝色
     */
    fun drawCircle(canvas: Canvas, x: Int, y: Int, isMax: Boolean) {
        canvas.drawCircle(x.toFloat(), y.toFloat(), CIRCLE_RADIUS.toFloat(), if (isMax) redPaint else bluePaint)
    }

    /**
     * 指定的 (x,y) 坐标为实心圆圆心，以该实心圆为基准绘制指定文字。
     * 若空间允许则放置在实心圆圆心右上方，否则根据实际情况放置在下方、左方或左下方.
     *
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     * @param x 实心圆圆心的 View 尺寸坐标
     */
    fun drawTempText(canvas: Canvas, text: String, width: Int, x: Int, y: Int) {
        var textX: Float = (x + TEMP_TEXT_OFFSET).toFloat()
        var textY: Float = (y - TEMP_TEXT_OFFSET).toFloat()

        val textWidth: Float = textPaint.measureText(text)
        if (x > width - textWidth - TEMP_TEXT_OFFSET) {//超出右边界，那就挪到左边
            textX = x - TEMP_TEXT_OFFSET - textWidth
        }

        val textFontTop: Float = -textPaint.getFontMetrics().top
        if (y < textFontTop + TEMP_TEXT_OFFSET / 2) {//超出上边界，那就挪到下面
            textY = y + TEMP_TEXT_OFFSET / 2 + textFontTop
        }

        canvas.drawText(text, textX, textY, textPaint)
    }

    /**
     * 指定的 (startX, startY)、(stopX, stopY) 坐标为线段，
     * 以该线段为基准绘制趋势图的 "A"、"B" 文字。
     *
     * 注意，不对 坐标参数 进行处理，传进来是哪就在哪绘制。
     */
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

    /**
     * 指定的 (x,y) 坐标为实心圆圆心，以该实心圆为基准绘制指定点名称文字。
     * 若空间允许则放置在实心圆圆心正下方，否则放正上方.
     *
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     * @param x 实心圆圆心的 View 尺寸坐标
     */
    fun drawPointName(canvas: Canvas, name: String, width: Int, height: Int, x: Int, y: Int) {
        val textWidth: Float = textPaint.measureText(name)
        val textHeight: Float = -textPaint.getFontMetrics().top

        var textX = x - textWidth / 2
        var textY = y + POINT_SIZE / 2 + textHeight

        if (textX < 0) {//x超出左边界
            textX = 0f
        }
        if (textX + textWidth > width) {//x超出右边界
            textX = width - textWidth
        }
        if (textY > height) {//若名字放点下面要超出范围时，放点上面
            textY = y - POINT_SIZE / 2 - textPaint.fontMetrics.bottom
        }
        canvas.drawText(name, textX, textY, textPaint)
    }

    /**
     * 指定的 线段或矩形 坐标为范围，
     * 以该范围为基准绘制指定线名称文字，放置于范围中心。
     *
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     */
    fun drawPointRectName(canvas: Canvas, name: String, width: Int, height: Int, left: Int, top: Int, right: Int, bottom: Int) {
        val fontMetrics: Paint.FontMetrics = textPaint.getFontMetrics()
        val textWidth: Float = textPaint.measureText(name)
        val textHeight: Float = -fontMetrics.top
        val centerX: Int = left + (right - left) / 2
        val centerY: Int = top + (bottom - top) / 2
        val offset: Float = (-fontMetrics.ascent + fontMetrics.descent) / 2 - fontMetrics.descent

        var textX: Float = centerX - textWidth / 2
        var textY: Float = centerY + offset

        if (textX < 0) {//x超出左边界
            textX = 0f
        }
        if (textX + textWidth > width) {//x超出右边界
            textX = width - textWidth
        }
        if (textY < textHeight) {//y超出上边界
            textY = textHeight
        }
        if (textY > height) {//y超出下边界
            textY = height.toFloat()
        }
        canvas.drawText(name, textX, textY, textPaint)
    }


    /* ******************************************** Touch ******************************************** */
}