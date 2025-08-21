package com.topdon.lib.ui.listener

import android.view.View

/**
 * 防止重复点击事件
 * @author: CaiSongL
 * @date: 2020/5/14 16:01
 */
public abstract class SingleClickListener : View.OnClickListener {
    private var mLastClickTime: Long = 0
    private var timeInterval = 500L

    constructor() {}
    constructor(interval: Long) {
        timeInterval = interval
    }

    override fun onClick(v: View) {
        val nowTime = System.currentTimeMillis()
        if (nowTime - mLastClickTime > timeInterval) {
            onSingleClick()
            mLastClickTime = nowTime
        }
    }

    protected abstract fun onSingleClick()
}