package com.infisense.usbir.tools

import androidx.annotation.ColorInt
import com.elvishew.xlog.XLog
import com.infisense.usbir.tools.bean.SelectIndexBean
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.utils.ByteUtils.bytesToInt
import com.topdon.lib.core.utils.ByteUtils.descBytes
import java.util.concurrent.LinkedBlockingQueue

object ImageTools {

    fun readFrame(imageBytes: ByteArray, tempBytes: ByteArray, max: Float = 40f, min: Float = 20f) {
        if (max < min) {
            return
        }
        val selectBean = getTempIndex(tempBytes, max, min)
//        Log.w("123", "max size: ${selectBean.maxIndex.size}, min size: ${selectBean.minIndex.size}")
        bitmapFromRgbaGrey(bytes = imageBytes, bean = selectBean)//灰度
    }

    fun readFrame(
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
        val selectBean = getTempIndex(tempBytes, max, min)
        bitmapFromRgba(
            bytes = imageBytes,
            bean = selectBean,
            maxColor = maxColor,
            minColor = minColor
        )//换颜色
    }

    // 选取区域转颜色
    private fun bitmapFromRgba(
        bytes: ByteArray,
        bean: SelectIndexBean,
        @ColorInt maxColor: Int,
        @ColorInt minColor: Int
    ) {
        val len = bytes.size / 4
        val selectMaxIndex = bean.maxIndex
        val selectMinIndex = bean.minIndex
        selectMaxIndex.sort()
        val maxQueue = LinkedBlockingQueue<Int>()
        val minQueue = LinkedBlockingQueue<Int>()
        selectMaxIndex.forEach {
            maxQueue.offer(it)
        }
        selectMinIndex.forEach {
            minQueue.offer(it)
        }
        val maxA = ((maxColor shr 24) and 0xff).toByte()
        val maxR = ((maxColor shr 16) and 0xff).toByte()
        val maxG = ((maxColor shr 8) and 0xff).toByte()
        val maxB = ((maxColor shr 0) and 0xff).toByte()
        val minA = ((minColor shr 24) and 0xff).toByte()
        val minR = ((minColor shr 16) and 0xff).toByte()
        val minG = ((minColor shr 8) and 0xff).toByte()
        val minB = ((minColor shr 0) and 0xff).toByte()
        for (i in 0 until len) {
            if (maxQueue.peek() == i) {
                bytes[i * 4] = maxR //r
                bytes[i * 4 + 1] = maxG //g
                bytes[i * 4 + 2] = maxB//b
                bytes[i * 4 + 3] = maxA //a
                maxQueue.poll()
            }
            if (minQueue.peek() == i) {
                bytes[i * 4] = minR
                bytes[i * 4 + 1] = minG
                bytes[i * 4 + 2] = minB
                bytes[i * 4 + 3] = minA
                minQueue.poll()
            }
        }
    }

    // 选取区域转灰度
    private fun bitmapFromRgbaGrey(bytes: ByteArray, bean: SelectIndexBean) {
        val len = bytes.size / 4
        val selectIndex = bean.maxIndex.plus(bean.minIndex)
        selectIndex.sort()
        val queue = LinkedBlockingQueue<Int>()
        selectIndex.forEach {
            queue.offer(it)
        }
        var r: Int
        var g: Int
        var b: Int
        var grey: Int
        for (i in 0 until len) {
            if (queue.peek() == i) {
                r = bytes[i * 4].toInt() and 0xff
                g = bytes[i * 4 + 1].toInt() and 0xff
                b = bytes[i * 4 + 2].toInt() and 0xff
                //灰度
                grey = (r * 0.3f).toInt() + (g * 0.59f).toInt() + (b * 0.11f).toInt()
                bytes[i * 4] = grey.toByte()
                bytes[i * 4 + 1] = grey.toByte()
                bytes[i * 4 + 2] = grey.toByte()
                queue.poll()
            }
        }
    }


    /**
     * 温度选取点
     *
     * @param bytes 温度数据
     */
    private fun getTempIndex(bytes: ByteArray, max: Float, min: Float): SelectIndexBean {
        var data: ByteArray
        val maxList = arrayListOf<Int>()
        val minList = arrayListOf<Int>()
        for (i in 0 until (bytes.size / 2)) {
            data = bytes.copyOfRange(i * 2, i * 2 + 2)
            val value = readTempValue(data)
            if (value > max && (NumberTools.scale(max, 0) != -273f)) {
                maxList.add(i)
            }
            if (value < min && (NumberTools.scale(min, 0) != -273f)) {
                minList.add(i)
            }
        }
        val maxIndex: IntArray = maxList.toIntArray()
        val minIndex: IntArray = minList.toIntArray()
        return SelectIndexBean(maxIndex, minIndex)
    }

    private fun readTempValue(bytes: ByteArray): Float {
        val data: ByteArray = bytes.descBytes()
        val scale = 16
        val tempInt = data.bytesToInt() / 4
        return (tempInt.toDouble() / scale.toDouble() - 273.15).toFloat()
    }

//    // RGBA 转 bitmap
//    fun bitmapFromRgba(bytes: ByteArray, width: Int, height: Int): Bitmap {
//        val len = bytes.size / 4
//        val pixels = IntArray(len)
//        for (i in pixels.indices) {
//            if (i > len / 4 * 3 && i < len) {
//                //指定区域颜色
//                val r = 255
//                val g = 215
//                val b = 0
//                val a = 255
//                val pixel = (a shl 24) or (r shl 16) or (g shl 8) or b
//                pixels[i] = pixel
//            } else if (i > 0 && i < len / 2) {
//                val r: Int = (bytes[i * 4] and 0xff.toByte()).toUByte().toInt()
//                val g: Int = (bytes[i * 4 + 1] and 0xff.toByte()).toUByte().toInt()
//                val b: Int = (bytes[i * 4 + 2] and 0xff.toByte()).toUByte().toInt()
//                val a: Int = (bytes[i * 4 + 3] and 0xff.toByte()).toUByte().toInt()
//
//                //灰度
//                val grey = (r * 0.3f).toInt() + (g * 0.59f).toInt() + (b * 0.11f).toInt()
//                val pixel = (a shl 24) or (grey shl 16) or (grey shl 8) or grey
//                pixels[i] = pixel
//            } else {
//                val r: Int = (bytes[i * 4] and 0xff.toByte()).toUByte().toInt()
//                val g: Int = (bytes[i * 4 + 1] and 0xff.toByte()).toUByte().toInt()
//                val b: Int = (bytes[i * 4 + 2] and 0xff.toByte()).toUByte().toInt()
//                val a: Int = (bytes[i * 4 + 3] and 0xff.toByte()).toUByte().toInt()
//                val pixel = (a shl 24) or (r shl 16) or (g shl 8) or b
//                pixels[i] = pixel
//            }
//        }
//        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
//        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
//        return bitmap
//    }

    /**
     * @param imageBytes    图像数据
     * @param tempBytes     温度数据
     * @param max           温度上限阈值
     * @param min           温度下限阈值
     */
    fun dualReadFrame(
        imageBytes: ByteArray,
        tempBytes: ByteArray,
        max: Float = 40f,
        min: Float = 20f,
        @ColorInt maxColor: Int = 0,
        @ColorInt minColor: Int = 0
    ) {
        if (max < min) {
            return
        }
        dualReplaceColor(imageBytes, tempBytes, max, min, maxColor, minColor)
    }

    /**
     * 替换颜色
     */
    @JvmStatic
    private fun dualReplaceColor(
        imageBytes: ByteArray,
        tempBytes: ByteArray,
        max: Float = 40f,
        min: Float = 20f,
        @ColorInt maxColor: Int,
        @ColorInt minColor: Int
    ) {
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
                        //max color
                        r = imageBytes[i * 4].toInt() and 0xff
                        g = imageBytes[i * 4 + 1].toInt() and 0xff
                        b = imageBytes[i * 4 + 2].toInt() and 0xff
                        //灰度
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
                        //max color
                        imageBytes[i * 4] = maxR //r
                        imageBytes[i * 4 + 1] = maxG //g
                        imageBytes[i * 4 + 2] = maxB //b
                        imageBytes[i * 4 + 3] = maxA //a
                    }
                    if (value < min) {
                        //min color
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

