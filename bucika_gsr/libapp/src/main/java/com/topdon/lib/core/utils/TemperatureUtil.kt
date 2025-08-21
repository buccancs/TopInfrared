package com.topdon.lib.core.utils

import com.topdon.lib.core.common.SharedManager

/**
 * @author: lvqiang
 * @date: 2024/8/26 9:59
 */
object TemperatureUtil {
    fun getTempStr(min: Int, max: Int): String = if (SharedManager.getTemperature() == 1) {
        "${min}°C~${max}°C"
    } else {
        "${(min * 1.8 + 32).toInt()}°F~${(max * 1.8 + 32).toInt()}°F"
    }
}