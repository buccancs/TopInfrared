package com.topdon.module.thermal.tools

object FenceTool {

    fun pointToIndex(point: IntArray, w: Int): Int {
        val x = point[0]
        val y = point[1]
        return y * w + x
    }

    fun indexToPoint(index: Int, w: Int): IntArray {
        val y = index / w
        val x = index % w
        return intArrayOf(x, y)
    }
}