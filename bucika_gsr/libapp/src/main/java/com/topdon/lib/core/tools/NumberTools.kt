package com.topdon.lib.core.tools

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

object NumberTools {

    /**
     * 精确小数点后一位
     */
    fun to01(float: Float): String {
        return String.format(Locale.ENGLISH, "%.1f", float)
    }

    /**
     * 精确小数点后两位
     */
    fun to01f(float: Float): Float {
        return to01(float).toFloat()
    }

    /**
     * 精确小数点后两位
     */
    fun to02(float: Float): String {
        return String.format(Locale.ENGLISH, "%.2f", float)
    }

    /**
     * 精确小数点后两位
     */
    fun to02f(float: Float): Float {
        return to02(float).toFloat()
    }

    /**
     * 四舍五入
     * @param newScale 保留多少位小数
     */
    fun scale(value: Float, newScale: Int): Float {
        return BigDecimal(value.toDouble()).setScale(newScale, RoundingMode.HALF_UP).toFloat()
    }
}