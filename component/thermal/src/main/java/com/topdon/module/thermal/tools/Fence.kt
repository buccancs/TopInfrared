package com.topdon.module.thermal.tools

import android.util.Log

class Fence(var w: Int = 256, var h: Int = 192, val srcRect: IntArray, rotateType: Int = 0) {

    var scale = 0f

    init {
        when (rotateType) {
            1, 3 -> {
                w = 192
                h = 256
            }
            else -> {
                w = 256
                h = 192
            }
        }
        scale = w / srcRect[0].toFloat()
        Log.w("123", "scale: $scale")
    }

    fun getSinglePoint(start: IntArray): ArrayList<IntArray> {
        val startPoint: IntArray = start
        val startX: Int = (startPoint[0] * scale).toInt()
        val startY: Int = (startPoint[1] * scale).toInt()
        val lineList = arrayListOf<IntArray>()
        lineList.add(intArrayOf(startX, startY))
        showArray(lineList)
        showArrayIndex(lineList)
        return lineList
    }

    fun getPointIndex(start: IntArray): ArrayList<Int> {
        val lineList = getSinglePoint(start)
        return pointToIndex(lineList)
    }

    fun getLinePoint(start: IntArray, end: IntArray): ArrayList<IntArray> {
        val startPoint: IntArray
        val endPoint: IntArray
        if (start[0] > end[0]) {
            startPoint = end
            endPoint = start
        } else {
            startPoint = start
            endPoint = end
        }
        val k: Float =
            (start[1].toFloat() - end[1].toFloat()) / (start[0].toFloat() - end[0].toFloat())
        Log.w("123", "k: $k")

        val startX: Int = (startPoint[0] * scale).toInt()
        val startY: Int = (startPoint[1] * scale).toInt()
        val endX: Int = (endPoint[0] * scale).toInt()
        val endY: Int = (endPoint[1] * scale).toInt()
        val lineList = arrayListOf<IntArray>()
        var y: Int
        for (i in startX..endX) {
            y = (startY - k * (startX - i)).toInt()
            lineList.add(intArrayOf(i, y))
        }
        showArray(lineList)
        showArrayIndex(lineList)
        return lineList
    }

    fun getLineIndex(start: IntArray, end: IntArray): ArrayList<Int> {
        val lineList = getLinePoint(start, end)
        return pointToIndex(lineList)
    }

    fun getAreaPoint(start: IntArray, end: IntArray): ArrayList<IntArray> {
        val startX: Int = (start[0] * scale).toInt()
        val startY: Int = (start[1] * scale).toInt()
        val endX: Int = (end[0] * scale).toInt()
        val endY: Int = (end[1] * scale).toInt()
        val lineList = arrayListOf<IntArray>()
        for (y in startY..endY) {
            for (x in startX..endX) {
                lineList.add(intArrayOf(x, y))
            }
        }
        return lineList
    }

    fun getAreaIndex(start: IntArray, end: IntArray): ArrayList<Int> {
        val lineList = getAreaPoint(start, end)
        return pointToIndex(lineList)
    }

    fun pointToIndex(lineList: ArrayList<IntArray>): ArrayList<Int> {
        val indexList = arrayListOf<Int>()
        lineList.forEach {
            indexList.add(FenceTool.pointToIndex(it, w))
        }
        return indexList
    }

    private fun showArray(list: ArrayList<IntArray>) {
        val stringBuilder = StringBuilder()
        list.forEach {
            stringBuilder.append(it.contentToString()).append(", ")
        }
        Log.w("123", "list size:${list.size}")
        Log.w("123", "list point:$stringBuilder")
    }

    private fun showArrayIndex(list: ArrayList<IntArray>) {
        val stringBuilder = StringBuilder()
        list.forEach {
            stringBuilder.append(FenceTool.pointToIndex(it, w)).append(", ")
        }
        Log.w("123", "list size:${list.size}")
        Log.w("123", "list index:$stringBuilder")
    }
}