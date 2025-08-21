package com.infisense.usbir.utils

import android.graphics.Point
import kotlin.math.abs

object TempUtil {
    fun getLineTemps(point1: Point, point2: Point, tempArray: ByteArray, width: Int): List<Float> {
        if (point1 == point2) {//搞毛啊，两个相同的点
            return ArrayList(0)
        }

        val pointList: ArrayList<Point> = ArrayList(abs(point1.x - point2.x).coerceAtLeast(abs(point1.y - point2.y)))
        if (point1.x == point2.x) {//垂直于 X 轴的直线
            val startY = point1.y.coerceAtMost(point2.y)
            val endY = point1.y.coerceAtLeast(point2.y)
            for (i in startY .. endY) {
                pointList.add(Point(point1.x, i))
            }
        } else {
            val k = (point1.y - point2.y).toFloat() / (point1.x - point2.x).toFloat()
            val b = point1.y - k * point1.x
            if (abs(k) <= 1) {//x轴正整数点较多
                val startX = point1.x.coerceAtMost(point2.x)
                val endX = point1.x.coerceAtLeast(point2.x)
                for (i in startX .. endX) {
                    pointList.add(Point(i, (k * i + b).toInt()))
                }
            } else {//y轴正整数点较多
                if (k >= 0) {//左上到右下
                    val startY = point1.y.coerceAtMost(point2.y)
                    val endY = point1.y.coerceAtLeast(point2.y)
                    for (y in startY .. endY) {
                        pointList.add(Point(((y - b) / k).toInt(), y))
                    }
                } else {//左下到右上
                    val startY = point1.y.coerceAtLeast(point2.y)
                    val endY = point1.y.coerceAtMost(point2.y)
                    for (y in startY downTo endY) {
                        pointList.add(Point(((y - b) / k).toInt(), y))
                    }
                }
            }
        }

        val tempList: ArrayList<Float> = ArrayList(pointList.size)
        pointList.forEach {
            val index = (it.y * width + it.x) * 2
            val tempInt = (tempArray[index + 1].toInt() shl 8 and 0xff00) or (tempArray[index].toInt() and 0xff)
            val tempValue = tempInt / 64f - 273.15f
            tempList.add(tempValue)
        }

        return tempList
    }
}