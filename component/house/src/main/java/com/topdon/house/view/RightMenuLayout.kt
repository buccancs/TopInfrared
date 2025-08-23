package com.topdon.house.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.Scroller
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import kotlin.math.abs

class RightMenuLayout : ConstraintLayout {

    companion object {
        
        private const val DEFAULT_AUTO_PERCENT = 0.5f

        @JvmStatic
        private var currentOpenView: RightMenuLayout? = null
    }

    private val scroller: Scroller
    
    private val scaledTouchSlop: Int
    
    private var autoPercent: Float = DEFAULT_AUTO_PERCENT

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        scroller = Scroller(context)
        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    private var menuWidth = 0
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var totalWidth = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView.isVisible) {
                totalWidth += childView.measuredWidth
            }
        }
        menuWidth = (totalWidth - measuredWidth).coerceAtLeast(0)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var left = 0
        for (i in 0 until childCount) {
            val childView = getChildAt(i)
            if (childView.isVisible) {
                childView.layout(left, 0, left + childView.measuredWidth, childView.measuredHeight)
                left += childView.measuredWidth
            }
        }
    }

    private var downX = 0
    private var downY = 0
    private var downScrollX = 0
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ev == null || menuWidth <= 0) {
            return super.onInterceptTouchEvent(ev)
        }
        if (super.onInterceptTouchEvent(ev)) {
            return true
        }

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x.toInt()
                downY = ev.y.toInt()
                downScrollX = scrollX
                if (currentOpenView !== this) {
                    currentOpenView?.close()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val distanceX = abs(ev.x.toInt() - downX)
                val distanceY = abs(ev.y.toInt() - downY)
                if (distanceX > scaledTouchSlop && distanceX > distanceY) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    return true
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {

            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || menuWidth <= 0) {
            return true
        }
        val currentX = event.x.toInt()
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                scrollX = (downScrollX + downX - currentX).coerceAtMost(menuWidth).coerceAtLeast(0)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (abs(currentX - downX) < scaledTouchSlop) {
                    if (scrollX != 0 && scrollX != menuWidth) {
                        scroller.startScroll(scrollX, 0, if (scrollX > 0) menuWidth - scrollX else -scrollX, 0)
                        invalidate()
                    }
                } else {
                    if (currentX > downX) {
                        if (currentOpenView === this) {
                            currentOpenView = null
                        }
                        scroller.startScroll(scrollX, 0, -scrollX, 0)
                    } else {
                        if ((downX - currentX) < (menuWidth * autoPercent).toInt()) {
                            if (currentOpenView === this) {
                                currentOpenView = null
                            }
                            scroller.startScroll(scrollX, 0, -scrollX, 0)
                        } else {
                            currentOpenView = this
                            scroller.startScroll(scrollX, 0, menuWidth - scrollX, 0)
                        }
                    }
                    invalidate()
                }
            }
        }
        return true
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            if (scroller.currX == scrollX) {
                postInvalidateOnAnimation()
                return
            }
            scrollX = scroller.currX
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (currentOpenView === this) {
            currentOpenView = null
            scroller.abortAnimation()
            scrollX = 0
        }
    }

    fun close() {
        if (currentOpenView === this) {
            currentOpenView = null
        }
        scroller.startScroll(scrollX, 0, -scrollX, 0)
        invalidate()
    }

    fun expand() {
        if (currentOpenView !== this) {
            currentOpenView?.close()
        }

        currentOpenView = this
        scroller.startScroll(scrollX, 0, menuWidth - scrollX, 0)
        invalidate()
    }
}