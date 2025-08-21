package com.topdon.lib.core.tools

import android.content.Context
import android.util.DisplayMetrics
import com.blankj.utilcode.util.Utils
import com.topdon.lib.core.utils.ScreenUtil
import kotlin.math.pow
import kotlin.math.sqrt


object ScreenTool {

    /**
     * 折叠屏
     */
    fun isLandPhone(): Boolean {
        val displayMetrics: DisplayMetrics = Utils.getApp().resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()
        return (width / height) < 0.75f
    }

    fun isIPad(context: Context) : Boolean{
        val width = ScreenUtil.getScreenWidth(context)
        val height = ScreenUtil.getScreenHeight(context)
        val densityDpi = context.resources.displayMetrics.densityDpi
        val diagonalPixels = sqrt(width.toDouble().pow(2) + height.toDouble().pow(2))
        val screenInches = diagonalPixels / densityDpi
        return screenInches >= 7f
    }
}