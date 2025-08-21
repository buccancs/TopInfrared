package com.topdon.lib.ui.fence

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.SizeUtils

class FenceView : View {

    var listener: CallBack? = null

    private val mPaint by lazy { Paint() }
    private val rect: Rect = Rect(0, 0, 0, 0) //手动绘制矩形
    private val strokeWidth by lazy { SizeUtils.dp2px(2f).toFloat() } //线宽度


    constructor (context: Context) : super(context)


    constructor (context: Context, attrs: AttributeSet) : super(context, attrs)


    constructor (context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        mPaint.color = Color.WHITE
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = strokeWidth
        mPaint.alpha = 255
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
//        //设置无锯齿
//        canvas.drawARGB(50, 255, 227, 0)
//        mPaint.color = Color.GREEN
//        // 绘制绿色实心矩形
//        canvas.drawRect(100f, 200f, 400f, 200f + 400, mPaint)
//        mPaint.color = Color.RED
        canvas.drawRect(rect, mPaint)
    }

    var mX = 0f
    var mY = 0f
    var old = Rect(0, 0, 0, 0)
    var startPoint = intArrayOf(0, 0)
    var endPoint = intArrayOf(0, 0)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mX = event.x
        mY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                rect.right += strokeWidth.toInt()
                rect.bottom += strokeWidth.toInt()
                invalidate(rect)
                rect.left = mX.toInt()
                rect.top = mY.toInt()
                rect.right = rect.left
                rect.bottom = rect.top
                startPoint[0] = mX.toInt()
                startPoint[1] = mY.toInt()
            }
            MotionEvent.ACTION_UP -> {
                var x = mX.toInt()
                var y = mY.toInt()
                if (x > right) {
                    x = right - 1
                }
                if (y > bottom) {
                    y = bottom - 1
                }

                if (x < left) {
                    x = left + 1
                }
                if (y < top) {
                    y = top + 1
                }
                endPoint[0] = x
                endPoint[1] = y

                old = Rect(
                    rect.left,
                    rect.top,
                    (rect.right + strokeWidth).toInt(),
                    (rect.bottom + strokeWidth).toInt()
                )
                rect.right = x
                rect.bottom = y
                old.union(x, y)
                invalidate(old)
                result()
            }
            MotionEvent.ACTION_MOVE -> {
                old = Rect(
                    rect.left,
                    rect.top,
                    (rect.right + strokeWidth).toInt(),
                    (rect.bottom + strokeWidth).toInt()
                )
                rect.right = mX.toInt()
                rect.bottom = mY.toInt()
                old.union(mX.toInt(), mY.toInt())
                invalidate(old)
            }
        }
        return true
    }

    private fun result() {
        val point1 = intArrayOf(startPoint[0], startPoint[1])
        val point2 = intArrayOf(endPoint[0], endPoint[1])
        //调整位置
        for (i in 0..1) {
            if (startPoint[i] > endPoint[i]) {
                point1[i] = endPoint[i]
                point2[i] = startPoint[i]
            }
        }
        Log.i(
            "123",
            "原始坐标 start:${startPoint.contentToString()}, end:${endPoint.contentToString()}"
        )
        Log.w("123", "修正坐标 start:${point1.contentToString()}, end:${point2.contentToString()}")
        if (listener != null) {
            listener!!.callback(point1, point2, intArrayOf(width, height))
        }
    }

    fun clear() {
        startPoint = intArrayOf(0, 0)
        endPoint = intArrayOf(0, 0)
        rect.left = 0
        rect.right = 0
        rect.top = 0
        rect.bottom = 0
        result()
        invalidate()
    }

    interface CallBack {
        /**
         * startPoint: 左上角
         * endPoint: 右下角
         */
        fun callback(startPoint: IntArray, endPoint: IntArray, srcRect: IntArray)
    }
}