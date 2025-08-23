package com.topdon.module.thermal.ir.bean

import android.graphics.Point
import android.graphics.Rect
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectPositionBean(
    val type: Int = 0,
    val startPosition: Point = Point(),
    val endPosition: Point = Point(),
) : Parcelable {

    constructor(rect: Rect): this(3, Point(rect.left, rect.top), Point(rect.right, rect.bottom))

    fun getRect(): Rect = Rect(startPosition.x, startPosition.y, endPosition.x, endPosition.y)
}
