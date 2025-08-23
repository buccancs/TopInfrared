package com.infisense.usbir.tools

import android.util.Log
import androidx.annotation.ColorInt
import com.elvishew.xlog.XLog
import com.topdon.lib.core.utils.ByteUtils.bytesToInt
import com.topdon.lib.core.utils.ByteUtils.descBytes

object BitmapTools {

    private fun readTempValue(bytes: ByteArray): Float {
        val data: ByteArray = bytes.descBytes()
        val scale = 16
        val tempInt = data.bytesToInt() / 4
        return (tempInt.toDouble() / scale.toDouble() - 273.15).toFloat()
    }

    fun replaceBitmapColor(
        imageBytes: ByteArray,
        tempBytes: ByteArray,
        max: Float = 40f,
        min: Float = 20f,
        @ColorInt maxColor: Int,
        @ColorInt minColor: Int
    ) {
        if (max < min) {
            return
        }
        try {
            if (maxColor == 0 && minColor == 0) {
                var data: ByteArray
                val len = imageBytes.size / 4
                var value: Float
                var r: Int
                var g: Int
                var b: Int
                var grey: Int
                for (i in 0 until len) {
                    data = tempBytes.copyOfRange(i * 2, i * 2 + 2)
                    value = readTempValue(data)
                    if (value > max || value < min) {
                        r = imageBytes[i * 4].toInt() and 0xff
                        g = imageBytes[i * 4 + 1].toInt() and 0xff
                        b = imageBytes[i * 4 + 2].toInt() and 0xff
                        grey = (r * 0.3f).toInt() + (g * 0.59f).toInt() + (b * 0.11f).toInt()
                        imageBytes[i * 4] = grey.toByte()
                        imageBytes[i * 4 + 1] = grey.toByte()
                        imageBytes[i * 4 + 2] = grey.toByte()
                    }
                }
            } else {
                var data: ByteArray
                val len = imageBytes.size / 4
                val maxA = ((maxColor shr 24) and 0xff).toByte()
                val maxR = ((maxColor shr 16) and 0xff).toByte()
                val maxG = ((maxColor shr 8) and 0xff).toByte()
                val maxB = ((maxColor shr 0) and 0xff).toByte()
                val minA = ((minColor shr 24) and 0xff).toByte()
                val minR = ((minColor shr 16) and 0xff).toByte()
                val minG = ((minColor shr 8) and 0xff).toByte()
                val minB = ((minColor shr 0) and 0xff).toByte()
                var value: Float
                for (i in 0 until len) {
                    data = tempBytes.copyOfRange(i * 2, i * 2 + 2)
                    value = readTempValue(data)
                    if (value > max) {
                        imageBytes[i * 4] = maxR
                        imageBytes[i * 4 + 1] = maxG
                        imageBytes[i * 4 + 2] = maxB
                        imageBytes[i * 4 + 3] = maxA
                    }
                    if (value < min) {
                        imageBytes[i * 4] = minR
                        imageBytes[i * 4 + 1] = minG
                        imageBytes[i * 4 + 2] = minB
                        imageBytes[i * 4 + 3] = minA
                    }
                }
            }
        } catch (e: Exception) {
            XLog.w("颜色替换失败: ${e.message}")
        }
    }
}