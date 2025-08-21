package com.topdon.module.thermal.ir.utils

import android.graphics.Point
import android.util.Log
import com.github.mikephil.charting.charts.LineChart
import com.topdon.lib.core.tools.UnitTools
import kotlin.math.abs
import kotlin.math.roundToInt

object ChartTools {

    fun getLineTemps(point1: Point, point2: Point, tempArray: ByteArray, rotate: Int): List<Float> {
        val tempList: ArrayList<Float> = ArrayList()
        if (point1 == point2) {//搞毛啊，两个相同的点
            return tempList
        }

        val pointList: ArrayList<Point> = ArrayList()
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

        val width = if (rotate == 90 || rotate == 270) 192 else 256

        pointList.forEach {
            val index = (it.y * width + it.x) * 2
            val tempInt = (tempArray[index + 1].toInt() shl 8 and 0xff00) or (tempArray[index].toInt() and 0xff)
            val tempValue = tempInt / 64f - 273.15f
            tempList.add(tempValue)
        }

        return tempList
    }

    //X数值缩放
    fun scale(type: Int): Long {
        return when (type) {
            1 -> 1 * 1000 //s
            2 -> 60 * 1000  //min
            3 -> 60 * 60 * 1000 //hour
            4 -> 24 * 60 * 60 * 1000 //day
            else -> 1 //10s
        }
    }

    //获取显示最小区间
    fun getMinimum(type: Int): Float {
        val min = when (type) {
            1 -> 10f //10s
            2 -> 10f //10min
            3 -> 10f //10hour
            4 -> 10f //10day
            else -> 1 * 10f //10s
        }
        return min
    }

    //获取显示最大区间，以最小区间的50倍
    fun getMaximum(type: Int): Float {
        return getMinimum(type) * 50f
    }

    /**
     * 设置Y轴范围
     */
    fun setY(chart: LineChart) {
        var maxVol = 0f
        var minVol = 0f
        when (chart.data.dataSetCount) {
            1 -> {
                val dataSet = chart.data.getDataSetByIndex(0) ?: return
                maxVol = dataSet.yMax
                minVol = dataSet.yMin
            }
            2 -> {
                val dataSet1 = chart.data.getDataSetByIndex(0)
                val dataSet2 = chart.data.getDataSetByIndex(1)
                maxVol = if (dataSet1.yMax > dataSet2.yMax) dataSet1.yMax else dataSet2.yMax
                minVol = if (dataSet1.yMin < dataSet2.yMin) dataSet1.yMin else dataSet2.yMin
            }
            3 -> {
                val dataSet1 = chart.data.getDataSetByIndex(0)
                val dataSet2 = chart.data.getDataSetByIndex(1)
                val dataSet3 = chart.data.getDataSetByIndex(2)
                maxVol = if (dataSet1.yMax > dataSet2.yMax) dataSet1.yMax else dataSet2.yMax
                minVol = if (dataSet1.yMin < dataSet2.yMin) dataSet1.yMin else dataSet2.yMin

                maxVol = if (dataSet3.yMax > maxVol) dataSet3.yMax else maxVol
                minVol = if (dataSet3.yMin < minVol) dataSet3.yMin else minVol
            }
            else -> {
                return
            }
        }
        if (maxVol == minVol) {
            chart.axisLeft.axisMaximum = 50f
            chart.axisLeft.axisMinimum = 0f
        } else {
            if (maxVol - minVol < 0.5f) {
                chart.axisLeft.axisMaximum = (maxVol + minVol) / 2f + 0.3f
                chart.axisLeft.axisMinimum = (maxVol + minVol) / 2f - 0.3f
            } else {
                chart.axisLeft.axisMaximum = maxVol + (maxVol - minVol) * 0.15f
                chart.axisLeft.axisMinimum = minVol - (maxVol - minVol) * 0.15f
            }
        }
        Log.w("chart", "yAxis max:${chart.axisLeft.axisMaximum}, min:${chart.axisLeft.axisMinimum}")
    }

    /**
     * 设置X轴刻度
     */
    fun setX(chart: LineChart, type: Int) {
        //true保证有刻度数量不变,滑动要false
        val xLen = chart.xChartMax - chart.xChartMin
//        Log.w("chart", "xLen: $xLen")
//        chart.xAxis.setLabelCount(getLabCount(xLen.toInt()), getLabCount(xLen.toInt()) < 3)
//        chart.xAxis.setLabelCount(5, false) // 3点 ok
//        chart.xAxis.setLabelCount(5, true) //
        chart.xAxis.setLabelCount(getLabCount(xLen.toInt()), xLen <= 3)
    }

    /**
     * x轴显示多少个刻度
     */
    private fun getLabCount(count: Int): Int {
        return when {
            count <= 2 -> 1
            count in 3..4 -> 2
            count in 5..7 -> 3
//            count in 8..9 -> 4
//            count >= 9 -> 5
            count >= 8 -> 4
            else -> count
        }
    }

    fun getChartX(x: Long, startTime: Long, type: Int): Long {
        return (x - startTime) / scale(type)
    }
}