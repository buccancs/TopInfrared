package com.topdon.lib.core.utils

import android.content.Context
import android.content.res.Configuration

object ScreenUtil {
    @JvmStatic
    fun getScreenWidth(context: Context): Int = context.resources.displayMetrics.widthPixels

    @JvmStatic
    fun getScreenHeight(context: Context): Int = context.resources.displayMetrics.heightPixels

    @JvmStatic
    fun isPortrait(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    @JvmStatic
    fun isLandscape(context: Context): Boolean {
        return context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}