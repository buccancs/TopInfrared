package com.topdon.lib.core.tools

import com.topdon.lib.core.common.SharedManager
import java.math.BigDecimal
import java.util.*

object UnitTools {

    /**
     * 温度显示
     *
     * @param float 温度
     */
    @JvmStatic
    fun showC(float: Float): String {
        val str = if (SharedManager.getTemperature() == 1) {
            //温度
            "${String.format(Locale.ENGLISH, "%.1f", float)}°C"
        } else {
            //华氏度
            "${String.format(Locale.ENGLISH, "%.1f", (float * 1.8000 + 32.00))}°F"
        }
        return str
    }
    /**
     * 温度显示
     *
     * @param float 温度
     */
    @JvmStatic
    fun showC(float: Float,isC: Boolean): String {
        val str = if (isC) {
            //温度
            "${String.format(Locale.ENGLISH, "%.1f", float)}°C"
        } else {
            //华氏度
            "${String.format(Locale.ENGLISH, "%.1f", (float * 1.8000 + 32.00))}°F"
        }
        return str
    }


    /**
     * 温度区间
     */
    @JvmStatic
    fun showIntervalC(min: Int, max: Int): String {
        val str = if (SharedManager.getTemperature() == 1) {
            //温度
            "${min}~${max}°C"
        } else {
            //华氏度
            val maxT: Int = (max * 1.8000 + 32.00).toInt()
            val minT: Int = (min * 1.8000 + 32.00).toInt()
            "${minT}~${maxT}°F"
        }
        return str
    }

    /**
     * 配置温度区间
     */
    @JvmStatic
    fun showConfigC(min: Int, max: Int): String {
        val str = if (SharedManager.getTemperature() == 1) {
            //温度
            "(${min}~${max}°C)"
        } else {
            //华氏度
            val maxT: Int = (max * 1.8000 + 32.00).toInt()
            val minT: Int = (min * 1.8000 + 32.00).toInt()
            "(${minT}~${maxT}°F)"
        }
        return str
    }

    /**
     * 温度显示单位
     *
     * @param float 温度
     */
    @JvmStatic
    fun showUnit(): String {
        val str = if (SharedManager.getTemperature() == 1) {
            //温度
            "°C"
        } else {
            //华氏度
            "°F"
        }
        return str
    }

    /**
     * 温度显示单位
     *
     * @param float 温度
     */
    @JvmStatic
    fun showUnitValue(value: Float): Float {
        val str = if (SharedManager.getTemperature() == 1) {
            //温度
            value
        } else {
            //华氏度
            toF(value)
        }
        return str.toFloat()
    }
    /**
     * 温度显示单位
     *
     * @param float 温度
     */
    @JvmStatic
    fun showUnitValue(value: Float,showC: Boolean): Float {
        if (value == Float.MAX_VALUE || value == Float.MIN_VALUE){
            return value
        }
        val str = if (showC) {
            //温度
            value
        } else {
            //华氏度
            toF(value)
        }
        return str.toFloat()
    }

    /**
     * 统一转成摄氏度
     *
     * @param float 温度
     */
    @JvmStatic
    fun showToCValue(value: Float,isShowC: Boolean): Float {
        val str = if (isShowC) {
            //温度
            value
        } else {
            //华氏度
            toC(value)
        }
        return str.toFloat()
    }
    /**
     * 统一转成摄氏度
     *
     * @param float 温度
     */
    @JvmStatic
    fun showToCValue(value: Float): Float {
        val str = if (SharedManager.getTemperature() == 1) {
            //温度
            value
        } else {
            //华氏度
            toC(value)
        }
        return str.toFloat()
    }

    /**
     * 转华氏度
     */
    fun toF(value: Float): Float {
        return value * 1.8000f + 32.00f
    }

    /**
     * 转摄氏度
     * 使用浮点型,防止华氏度转摄氏度精度丢失
     */
    fun toC(value: Float): Float {
        return (value - 32.0f) / 1.8000f
    }

    /**
     * 输入摄氏度，返回保留1位小数不带单位字符的 String.
     *
     * @param float 温度值，单位摄氏度
     */
    @JvmStatic
    fun showNoUnit(float: Float): String {
        val str = if (SharedManager.getTemperature() == 1) {//摄氏度
            String.format(Locale.ENGLISH, "%.1f", float)
        } else {
            String.format(Locale.ENGLISH, "%.1f", (float * 1.8000 + 32.00))
        }
        return if (str.endsWith(".0")) str.substring(0, str.length - 2) else str
    }

    /**
     * 输入摄氏度，返回保留1位小数带单位字符的 String.
     *
     * @param float 温度值，单位摄氏度
     */
    @JvmStatic
    fun showWithUnit(float: Float): String {
        val str = if (SharedManager.getTemperature() == 1) {//摄氏度
            String.format(Locale.ENGLISH, "%.1f", float)
        } else {
            String.format(Locale.ENGLISH, "%.1f", (float * 1.8000 + 32.00))
        }
        return (if (str.endsWith(".0")) str.substring(0, str.length - 2) else str) + showUnit()
    }
}