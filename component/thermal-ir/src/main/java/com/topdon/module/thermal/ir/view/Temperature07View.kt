package com.topdon.module.thermal.ir.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent

class Temperature07View : TemperatureBaseView {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onDraw(canvas: Canvas) {
        if (!isTouching) {
            return
        }
        when (mode) {
            Mode.POINT -> operatePoint?.let { drawPoint(canvas, it) }
            Mode.LINE -> operateLine?.let { drawLine(canvas, it) }
            Mode.RECT -> operateRect?.let { drawRect(canvas, it) }
            Mode.TREND -> {
            }
            else -> {

            }
        }
    }

    private var isTouching = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> isTouching = true
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isTouching = false
        }
        return super.onTouchEvent(event)
    }
}