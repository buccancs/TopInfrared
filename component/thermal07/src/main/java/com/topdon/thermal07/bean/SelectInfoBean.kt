package com.topdon.thermal07.bean

import android.graphics.Point
import android.graphics.Rect
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectInfoBean(
    val type: Int = 0,
    val startPoint: Point = Point(0, 0),
    val endPoint: Point = Point(0, 0)
) : Parcelable {

    constructor(point: Point): this(1, point, point)

    constructor(startPoint: Point, endPoint: Point): this(2, startPoint, endPoint)

    constructor(rect: Rect): this(3, Point(rect.left, rect.top), Point(rect.right, rect.bottom))

    fun getRect(): Rect = Rect(startPoint.x, startPoint.y, endPoint.x, endPoint.y)

}
