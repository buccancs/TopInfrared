package com.topdon.module.thermal.ir.report.view

import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.blankj.utilcode.util.SizeUtils

class WatermarkView: View {

    /**
     * 水印文字.
     */
    var watermarkText: String? = null
        set(value) {
            field = value
            invalidate()
        }

    private val marginTop = SizeUtils.dp2px(220f).toFloat()

    private val textPaint: TextPaint = TextPaint()


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        textPaint.isFakeBoldText = true
        textPaint.isAntiAlias = true
        textPaint.color = 0x082b79d8
        textPaint.textSize = SizeUtils.sp2px(80f).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        watermarkText?.let {
            var hasAddCount = 0
            var hasUseHeight = 0f
            while (hasUseHeight < height + marginTop) {
                canvas?.save()
                canvas?.rotate(15f)
                val translateX = (width - textPaint.measureText(it)).coerceAtLeast(0f) / 2f + if (hasAddCount % 2 == 0) 100f else 0f
                canvas?.translate(translateX, 0f)
                canvas?.drawText(it, 0f, 0f, textPaint)
                canvas?.restore()
                canvas?.translate(0f, marginTop)
                hasUseHeight += marginTop
                hasAddCount++
            }
        }
    }
}