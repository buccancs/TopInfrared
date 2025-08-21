package com.topdon.module.thermal.ir.video.media

import android.graphics.Bitmap
import android.media.MediaCodecInfo.CodecCapabilities.*

/**
 * COLOR_FormatYUV420Planar             正常
 *
 * COLOR_FormatYUV420SemiPlanar         个例有花屏
 *
 * COLOR_FormatYUV420PackedSemiPlanar   个例有花屏
 *
 * COLOR_FormatYUV420PackedPlanar       个例有花屏
 *
 */
object EncodeYuvTools {

    fun getNV12(
        inputWidth: Int,
        inputHeight: Int,
        scaled: Bitmap?,
        colorFormat: Int = COLOR_FormatYUV420SemiPlanar
    ): ByteArray {
        val argb = IntArray(inputWidth * inputHeight)
        scaled!!.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight)
        val yuv = ByteArray(inputWidth * inputHeight * 3 / 2)
        when (colorFormat) {
            COLOR_FormatYUV420SemiPlanar -> encodeYUV420SP(
                yuv,
                argb,
                inputWidth,
                inputHeight
            )
            COLOR_FormatYUV420Planar -> encodeYUV420P(
                yuv,
                argb,
                inputWidth,
                inputHeight
            )
            COLOR_FormatYUV420PackedSemiPlanar -> encodeYUV420PSP(
                yuv,
                argb,
                inputWidth,
                inputHeight
            )
            COLOR_FormatYUV420PackedPlanar -> encodeYUV420PP(
                yuv,
                argb,
                inputWidth,
                inputHeight
            )
            else -> encodeYUV420SP(
                yuv,
                argb,
                inputWidth,
                inputHeight
            )
        }
        return yuv
    }

    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                // val a = argb[index] and -0x1000000 shr 24
                val r = argb[index] and 0xff0000 shr 16
                val g = argb[index] and 0xff00 shr 8
                val b = argb[index] and 0xff shr 0
                val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                val u = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                val v = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                yuv420sp[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                    yuv420sp[uvIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                }
                index++
            }
        }
    }

    private fun encodeYUV420P(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uIndex = frameSize
        var vIndex = frameSize + width * height / 4
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                // val a = argb[index] and -0x1000000 shr 24
                val r = argb[index] and 0xff0000 shr 16
                val g = argb[index] and 0xff00 shr 8
                val b = argb[index] and 0xff shr 0
                val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                val u = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                val v = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                yuv420sp[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[vIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                    yuv420sp[uIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                }
                index++
            }
        }
    }

    private fun encodeYUV420PSP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        var yIndex = 0
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                // val a = argb[index] and -0x1000000 shr 24
                val r = argb[index] and 0xff0000 shr 16
                val g = argb[index] and 0xff00 shr 8
                val b = argb[index] and 0xff shr 0
                val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                val u = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                val v = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                yuv420sp[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[yIndex + 1] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                    yuv420sp[yIndex + 3] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                }
                if (index % 2 == 0) {
                    yIndex++
                }
                index++
            }
        }
    }

    private fun encodeYUV420PP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        var yIndex = 0
        var vIndex = yuv420sp.size / 2
        var index = 0
        for (j in 0 until height) {
            for (i in 0 until width) {
                // val a = argb[index] and -0x1000000 shr 24
                val r = argb[index] and 0xff0000 shr 16
                val g = argb[index] and 0xff00 shr 8
                val b = argb[index] and 0xff shr 0
                val y = (66 * r + 129 * g + 25 * b + 128 shr 8) + 16
                val u = (112 * r - 94 * g - 18 * b + 128 shr 8) + 128
                val v = (-38 * r - 74 * g + 112 * b + 128 shr 8) + 128
                if (j % 2 == 0 && index % 2 == 0) { // 0
                    yuv420sp[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                    yuv420sp[yIndex + 1] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                    yuv420sp[vIndex + 1] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                    yIndex++
                } else if (j % 2 == 0 && index % 2 == 1) { //1
                    yuv420sp[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                } else if (j % 2 == 1 && index % 2 == 0) { //2
                    yuv420sp[vIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                    vIndex++
                } else if (j % 2 == 1 && index % 2 == 1) { //3
                    yuv420sp[vIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()
                }
                index++
            }
        }
    }
}