package com.topdon.module.thermal.ir.bean

import android.graphics.Point
import android.graphics.Rect
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 温度监控 第1步 第2步 之间传递的要监控的信息.
 * @param type 1-点 2-线 3-面
 */
@Parcelize
data class SelectPositionBean(
    val type: Int = 0, //1-点 2-线 3-面
    val startPosition: Point = Point(),
    val endPosition: Point = Point(),
) : Parcelable {


    constructor(rect: Rect): this(3, Point(rect.left, rect.top), Point(rect.right, rect.bottom))

    fun getRect(): Rect = Rect(startPosition.x, startPosition.y, endPosition.x, endPosition.y)
}
