package com.topdon.module.thermal.ir.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.blankj.utilcode.util.SizeUtils

/**
 * 常用材料发射率 页面所用，一行常用材料发射率.
 *
 * Created by LCG on 2024/10/14.
 */
class EmissivityView : View {
    companion object {
        /**
         * 默认描边尺寸，单位 dp.
         */
        private const val DEFAULT_STROKE_WIDTH: Float = 0.5f
    }

    /**
     * 是否顶部对齐
     */
    var isAlignTop = false
    /**
     * 是否需要绘制顶部横线
     */
    var drawTopLine = false

    /**
     * 要显示的文字列表.
     */
    private val textList: ArrayList<CharSequence> = ArrayList(3)
    /**
     * 执行绘制的 Layout 列表.
     */
    private val layoutList: ArrayList<StaticLayout> = ArrayList(3)


    private val strokeWidth = SizeUtils.dp2px(DEFAULT_STROKE_WIDTH).coerceAtLeast(1).toFloat()
    private val linePaint = Paint()
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        linePaint.color = 0xff5b5961.toInt()
        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = strokeWidth
    }

    fun refreshText(newList: List<String>) {
        textList.clear()
        textList.addAll(newList)

        textPaint.color = if (textList.size == 1) 0xffffffff.toInt() else 0xccffffff.toInt()
        textPaint.textSize = SizeUtils.sp2px(if (textList.size == 1) 12f else 11f).toFloat()

        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize: Int = MeasureSpec.getSize(widthMeasureSpec) - paddingStart - paddingEnd
        val firstWidth: Int = (widthSize * 135 / 335f).toInt()// 3 列的比例为 135:100:100
        val elseWidth: Int = (widthSize - firstWidth) / 2
        val contentWidth: Int = firstWidth + elseWidth * 2

        //初始化 layoutList
        layoutList.clear()
        for (i in textList.indices) {
            val textWidth: Int = if (textList.size == 1) {
                contentWidth - SizeUtils.dp2px(24f)//左右各 12dp padding
            } else {
                (if (i == 0) firstWidth else elseWidth) - SizeUtils.dp2px(24f)//左右各 12dp padding
            }
            layoutList.add(
                StaticLayout.Builder.obtain(textList[i], 0, textList[i].length, textPaint, textWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .build()
            )
        }

        //计算最大高度
        var maxHeight = 0
        for (layout in layoutList) {
            maxHeight = maxHeight.coerceAtLeast(layout.height)
        }
        if (maxHeight == 0) {//没有设置要显示的字符时，给个占位的高度好了
            maxHeight = textPaint.fontMetricsInt.bottom - textPaint.fontMetricsInt.top
        }
        maxHeight += SizeUtils.dp2px(12f)//上下各 6dp padding

        //宽度为 UNSPECIFIED 的情况目前不存在，不考虑
        setMeasuredDimension(contentWidth + paddingStart + paddingEnd, maxHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(paddingStart.toFloat(), 0f)

        val contentWidth = (width - paddingStart - paddingEnd).toFloat()
        if (drawTopLine) {
            canvas.drawLine(0f, strokeWidth / 2, contentWidth, strokeWidth / 2, linePaint)
        }
        canvas.drawLine(0f, height.toFloat() - strokeWidth / 2, contentWidth, height.toFloat() - strokeWidth / 2, linePaint)
        canvas.drawLine(strokeWidth / 2, 0f, strokeWidth / 2, height.toFloat(), linePaint)

        val padding = SizeUtils.dp2px(12f).toFloat()
        for (layout in layoutList) {
            canvas.save()
            canvas.translate(padding, if (isAlignTop) SizeUtils.dp2px(6f).toFloat() else (height - layout.height) / 2f)
            layout.draw(canvas)
            canvas.restore()

            val itemWidth = padding + layout.width.toFloat() + padding
            canvas.drawLine(itemWidth - strokeWidth / 2, 0f, itemWidth - strokeWidth / 2, height.toFloat(), linePaint)
            canvas.translate(itemWidth, 0f)
        }
    }
}