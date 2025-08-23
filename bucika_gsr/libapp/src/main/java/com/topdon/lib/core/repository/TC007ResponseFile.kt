package com.topdon.lib.core.repository

import android.graphics.Point
import android.graphics.Rect
import java.lang.NumberFormatException

data class TC007Response<T>(
    val Code: Int,
    val Message: String?,
    val Translate: String?,
    val Detail: String?,
    val Data: T?,
) {
    
    fun isSuccess(): Boolean = Code == 200
}

data class ProductBean(
    val ProductName: String,
    val ProductPN: String,
    val ProductSN: String,
    val Code: String,
    val SoftwareVersion: Version07Bean?
) {
    fun getVersionStr(): String = "${SoftwareVersion?.Major ?: "-"}.${SoftwareVersion?.Minor ?: "-"}${SoftwareVersion?.Build ?: "-"}"
}

data class Version07Bean(
    val Major: String?,
    val Minor: String?,
    val Build: String?,
)

data class BatteryInfo(
    val Status: String?,
    val Remaining: String?,
) {
    fun isCharging(): Boolean = Status == "Charging"

    fun getBattery(): Int? = try {
        Remaining?.toInt()
    } catch (_: NumberFormatException) {
        null
    }
}

data class TC07UpgradeStatus(
    val Status: Int,
    val Percent: Int,
    val Code: Int,
)

data class EnvAttr(
    val Fps: Int,
    val Level: Int,
    val OsdMode: Int,
    val TempUnit: Int,
    val DistanceUnit: Int,
)

data class FrameParam(
    var Enable: Boolean,
    val TempRule : TempRule
)

data class TempRule(
    val AlarmRule : Int,
    val ThresholdTemp : Int,
    val Debounce : Int,
    val ToleranceTemp : Int,
    val TempRise : TempRise
)

data class TempRise(
    var Enable : Boolean,
    var TRTemp : Int,
    var TRTime : Int,
    var TRNum : Int
)

data class TempFrameParam(
    val FrameHigh: FrameParam,
    val FrameLow: FrameParam,
    val FrameCenter: FrameParam,

) {
}

internal data class PointParam(val X: Int, val Y: Int) {
    constructor(point: Point?): this(point?.x ?: 0, point?.y ?: 0)
}

internal data class TargetParam(val Enable: Boolean)

internal data class TempPointParam(
    val Enable: Boolean,
    val ID: Int,
    val Name: String,
    val Point: PointParam,
    val Target: TargetParam,
) {
    constructor(id: Int, point: Point?): this(
        Enable = point != null,
        ID = id,
        Name = "P$id",
        Point = PointParam(point?.x ?: 0, point?.y ?: 0),
        Target = TargetParam(true),
    )
}

internal data class TempLineParam(
    val Enable: Boolean,
    val ID: Int,
    val Name: String,
    val Line: LineParam,
    val Target: TargetParam,
) {
    constructor(id: Int, start: Point?, end: Point?): this(
        Enable = start != null && end != null,
        ID = id,
        Name = "L$id",
        Line = LineParam(PointParam(start), PointParam(end)),
        Target = TargetParam(true),
    )

    data class LineParam(val Point0: PointParam, val Point1: PointParam)
}

internal data class TempRectParam(
    val Enable: Boolean,
    val ID: Int,
    val Name: String,
    val Rectangle: RectParam,
    val Target: TargetParam,
) {
    constructor(id: Int, rect: Rect?): this(
        Enable = rect != null,
        ID = id,
        Name = "L$id",
        Rectangle = RectParam(rect),
        Target = TargetParam(true),
    )

    data class RectParam(val Point0: PointParam, val Point1: PointParam, val Point2: PointParam, val Point3: PointParam) {
        constructor(rect: Rect?): this(
            Point0 = PointParam(rect?.left ?: 0, rect?.top ?: 0),
            Point1 = PointParam(rect?.right ?: 0, rect?.top ?: 0),
            Point2 = PointParam(rect?.left ?: 0, rect?.bottom ?: 0),
            Point3 = PointParam(rect?.right ?: 0, rect?.bottom ?: 0),
        )
    }
}

data class PhotoBean(
    val DCFile: String?,
    val IRFile: String?
)

data class AttributeBean(
    var Fps: Int?,
    var Level: Int?,
    var TempUnit: Int?,
    var DistanceUnit: Int?
)

data class WifiAttributeBean(
    var Ratio : Int ?= null,
    var X : Int ?= null,
    var Y : Int ?= null
)

data class PalleteBean(
    val palleteMode : Int,
    var stander : Stander ?= null,
    var custom : Custom ?= null
)

data class Stander(
    var palleteNo : Int = 0,
    val threshold: List<Int>,
)

data class Custom(
    var customMode : Int,
    var highThreshold : Int,
    var lowThreshold : Int,
    var highColor : CustomColor,
    var middleColor : CustomColor,
    var lowColor : CustomColor,
)
data class CustomColor(
    var red : Int,
    var green : Int,
    var blue : Int
)

data class Param(
    var brightness: Int = 50,
    var contrast: Int = 50,
    var saturation: Int = 50,
    var sharpness: Int = 50,
    var flipMode: Int = 0,
)

data class Isotherm(
    val color: Long,
    val size: Int
)

data class IsothermColor(
    val red: Int,
    val green: Int,
    val blue: Int,
)

data class IsothermC(
    val mode : Int,
    val highThreshold : Int,
    val lowThreshold : Int,
    var greaterThreshold : Int = 0,
    var lessThreshold : Int = 0

)

