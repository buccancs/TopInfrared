package com.topdon.lib.ui.fence

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.ui.R

class FencePointView : View {

    var listener: CallBack? = null
    private val iconSize = SizeUtils.dp2px(32f)

    constructor (context: Context) : super(context)

    constructor (context: Context, attrs: AttributeSet) : super(context, attrs)


    constructor (context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {

    }

    private val mPaint by lazy {
        Paint().apply {
            color = Color.BLUE
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = strokeWidth
            alpha = 255
        }
    }

    var destW = 0
    var destH = 0

    private val drawable: BitmapDrawable by lazy {
        resources.getDrawable(
            R.mipmap.ic_fence_point,
            null
        ) as BitmapDrawable
    }


    @SuppressLint("UseCompatLoadingForDrawables", "DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = drawable.bitmap
        val bw = bitmap.width
        val bh = bitmap.height
        destW = iconSize
        destH = destW * bh / bw
        val src = Rect(0, 0, bw, bh)

        var left = startPoint[0] - destW / 2
        var top = startPoint[1] - destH / 2
        var right = startPoint[0] + destW / 2
        var bottom = startPoint[1] + destH / 2
        if (left < 0) {
            left = 0
            right = destW
        }
        if (right > width) {
            right = width
            left = width - destW
        }
        if (top < 0) {
            top = 0
            bottom = destH
        }
        if (bottom > height) {
            bottom = height
            top = height - destH
        }

        val dst = Rect(
            left,
            top,
            right,
            bottom
        )
        canvas.drawBitmap(bitmap, src, dst, mPaint)
    }

    var mX = 0f
    var mY = 0f
    var old = Rect(0, 0, 0, 0)
    var startPoint = intArrayOf(0, 0)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mX = event.x
        mY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startPoint[0] = mX.toInt()
                startPoint[1] = mY.toInt()
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
//                Log.i("123", "onTouchEvent: ACTION_UP")
                startPoint[0] = mX.toInt()
                startPoint[1] = mY.toInt()
                result()
            }
            MotionEvent.ACTION_MOVE -> {
                startPoint[0] = mX.toInt()
                startPoint[1] = mY.toInt()
                invalidate()
            }
        }
        return true
    }

    private fun result() {
        val point1 = intArrayOf(startPoint[0], startPoint[1])
        if (startPoint[0] - destW / 2 < 0) {
            //left
            point1[0] = destW / 2
        }
        if (startPoint[0] + destW / 2 > width) {
            //right
            point1[0] = width - destW / 2
        }
        if (startPoint[1] - destW / 2 < 0) {
            //top
            point1[1] = destH / 2
        }
        if (startPoint[1] + destW / 2 > height) {
            //bottom
            point1[1] = height - destH / 2
        }
        Log.w("123", "坐标 point:${point1.contentToString()}")
        if (listener != null) {
            listener!!.callback(point1, intArrayOf(width, height))
        }
    }

    fun clear() {
        startPoint = intArrayOf(0, 0)
        result()
        invalidate()
    }

    interface CallBack {
        /**
         * startPoint: 左上角
         * endPoint: 右下角
         */
        fun callback(startPoint: IntArray, srcRect: IntArray)
    }
}