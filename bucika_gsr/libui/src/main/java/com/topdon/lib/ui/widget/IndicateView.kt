package com.topdon.lib.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.lib.ui.R

/**
 * ViewPager 指示 View.
 *
 * Created by chenggeng.lin on 2023/11/13.
 */
class IndicateView : View {

    var itemCount: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    var currentIndex: Int = 0
        set(value) {
            field = value
            invalidate()
        }


    private val itemWidth: Int = (ScreenUtil.getScreenWidth(context) * 20 / 375f).toInt()

    private val itemHeight: Int = (itemWidth * 2 / 20f).toInt()

    private val itemMargin: Int = (itemWidth * 4 / 20f).toInt()

    private val roundRadius: Float = SizeUtils.dp2px(2f).toFloat()


    private val defaultPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectPaint = Paint(Paint.ANTI_ALIAS_FLAG)


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        defaultPaint.color = 0xffffffff.toInt()
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndicateView)
        val color = typedArray.getColor(R.styleable.IndicateView_selectColor, 0xffffba42.toInt() )
        typedArray.recycle()
        selectPaint.color = color
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(itemWidth * itemCount + itemMargin * (itemCount - 1), itemHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until itemCount) {
            val left = i * (itemWidth + itemMargin).toFloat()
            val right = left + itemWidth
            val top = 0f
            val bottom = itemHeight.toFloat()
            canvas.drawRoundRect(left, top, right, bottom, roundRadius, roundRadius, if (i == currentIndex) selectPaint else defaultPaint)
        }
    }
}