package com.topdon.lib.menu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import androidx.appcompat.widget.AppCompatTextView

class MarqueeTextView : AppCompatTextView {

    companion object {
        private const val BASE_TIME = 2000
    }

    private val rect = Rect()

    private var scroller: Scroller? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        maxLines = 1
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        initScroller()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            initScroller()
        }
    }

    private fun initScroller() {
        if (width == 0) {
            return
        }
        paint.getTextBounds(text.toString(), 0, text.length, rect)
        if (rect.width() <= width) {
            scroller = null
            return
        }
        scroller = Scroller(context, LinearInterpolator())
        scroller?.startScroll(0, 0, rect.width(), 0, (rect.width() * BASE_TIME / width.toFloat()).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        if (scroller == null) {
            super.onDraw(canvas)
            return
        }
        scroller?.let {
            if (it.computeScrollOffset()) {
                val fontMetrics = paint.fontMetrics
                val y = height / 2 + (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
                paint.color = currentTextColor
                canvas?.drawText(text.toString(), -it.currX.toFloat(), y, paint)
            } else {
                scroller?.startScroll(0, 0, rect.width(), 0, (rect.width() * BASE_TIME / width.toFloat()).toInt())
            }
            postInvalidateOnAnimation()
        }
    }
}