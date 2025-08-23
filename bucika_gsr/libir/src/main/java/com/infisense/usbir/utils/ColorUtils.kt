package com.infisense.usbir.utils

import java.util.*

object ColorUtils {

    fun getRed(color: Int): Int {
        return color shr 16 and 0xFF
    }

    fun getGreen(color: Int): Int {
        return color shr 8 and 0xFF
    }

    fun getBlue(color: Int): Int {
        return color and 0xFF
    }

    fun to01(float: Float): String {
        return String.format(Locale.ENGLISH, "%.1f", float)
    }

}