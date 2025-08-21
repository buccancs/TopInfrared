package com.topdon.lib.ui.utils
import android.os.Handler
import android.os.Looper

object MainThreadHandler {
    private val handler = Handler(Looper.getMainLooper())
    fun runOnUiThread(r: Runnable?) {
        handler.post(r!!)
    }

    fun postDelayed(r: Runnable?, millis: Long) {
        handler.postDelayed(r!!, millis)
    }

    fun remove(r: Runnable?) {
        handler.removeCallbacks(r!!)
    }
}