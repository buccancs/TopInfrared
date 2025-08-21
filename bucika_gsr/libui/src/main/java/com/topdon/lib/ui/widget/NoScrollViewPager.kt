package com.topdon.lib.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NoScrollViewPager : ViewPager {

    private var isCanScroll = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return isCanScroll && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return isCanScroll && super.onTouchEvent(ev)
    }

    override fun setCurrentItem(item: Int) {
        //去除切换时动画
        super.setCurrentItem(item, false)
    }


}