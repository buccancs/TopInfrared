package com.topdon.libhik.bean

import com.topdon.libhik.util.ByteArrayUtil.toFloat
import com.topdon.libhik.util.ByteArrayUtil.toInt

data class FrameHead(
    val tempWidth: Int,
    val tempHeight: Int,
    val yuvWidth: Int,
    val yuvHeight: Int,
    val tempType: Int,
    val minTemp: Float,
    val maxTemp: Float,
    val aveTemp: Float,
    val maxX: Int,
    val maxY: Int,
    val minX: Int,
    val minY: Int,
    val isNormalTest: Boolean,
    val pointCount: Int,
    val rectCount: Int,
    val lineCount: Int,
    val totalCount: Int,
    val tempRuleList: ArrayList<TempRule>,
) {

    constructor(byteArray: ByteArray) : this(
        tempWidth = byteArray.toInt(64),
        tempHeight = byteArray.toInt(68),
        yuvWidth = byteArray.toInt(88),
        yuvHeight = byteArray.toInt(92),
        tempType = byteArray.toInt(120),
        minTemp = byteArray.toFloat(144),
        maxTemp = byteArray.toFloat(148),
        aveTemp = byteArray.toFloat(152),
        maxX = byteArray.toInt(156),
        maxY = byteArray.toInt(160),
        minX = byteArray.toInt(164),
        minY = byteArray.toInt(168),
        isNormalTest = byteArray.toInt(180) == 1,
        pointCount = byteArray[204].toInt() and 0xff,
        rectCount = byteArray[205].toInt() and 0xff,
        lineCount = byteArray[206].toInt() and 0xff,
        totalCount = byteArray[207].toInt() and 0xff,
        tempRuleList = byteArray.toRuleList(216),
    )

    companion object {
        
        private fun ByteArray.toRuleList(index: Int): ArrayList<TempRule> = try {
            val resultList: ArrayList<TempRule> = ArrayList(21)
            for (i in 0 until 21) {
                if (this[index + i * 208] == 1.toByte()) {
                    resultList.add(TempRule(this, index + i * 208))
                }
            }
            resultList
        } catch (_: IndexOutOfBoundsException) {
            ArrayList(0)
        }
    }

    override fun toString(): String = "尺寸:${tempWidth}x$tempHeight, ${yuvWidth}x$yuvHeight，" +
            "全屏最低温:($minX,$minY) ${minTemp}°C，全屏最高温:($maxX,$maxY) ${maxTemp}°C " +
            "平均温:${aveTemp}°C，${if (isNormalTest) "普通" else "专家"}测温，" +
            "$pointCount + $rectCount + $lineCount = $totalCount"
}
