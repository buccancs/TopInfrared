package com.topdon.house.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class SignView : View {
    companion object {
        
        private const val PAINT_WIDTH = 10f
    }

    var hasSign = false
    
    var onSignChangeListener: ((hasSign: Boolean) -> Unit)? = null

    var bitmap: Bitmap? = null

    private var canvas: Canvas? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        paint.color = 0xffffffff.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = PAINT_WIDTH
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.isDither = true
    }

    fun clear() {
        hasSign = false
        onSignChangeListener?.invoke(false)
        path.reset()
        canvas?.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val oldBitmap = bitmap
        if (oldBitmap == null || oldBitmap.width != width || oldBitmap.height != height) {
            val newBitmap = if (oldBitmap == null) {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createScaledBitmap(oldBitmap, width, height, true)
            }
            canvas = Canvas(newBitmap)
            bitmap = newBitmap
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, paint)
        }
    }

    private val path = Path()
    private var beforeX = 0f
    private var beforeY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        val currentX = event.x
        val currentY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(currentX, currentY)
                beforeX = currentX
                beforeY = currentY
            }

            MotionEvent.ACTION_MOVE -> {
                if (abs(currentX - beforeX) > 3 || abs(currentY - beforeY) > 3) {
                    path.quadTo(beforeX, beforeY, currentX, currentY)
                    hasSign = true
                    onSignChangeListener?.invoke(true)
                    beforeX = currentX
                    beforeY = currentY
                    canvas?.drawPath(path, paint)
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                path.reset()
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        canvas = null
        bitmap = null
    }
}