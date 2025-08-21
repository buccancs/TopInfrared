package com.topdon.lib.core.repository

import android.graphics.Point
import android.graphics.Rect
import java.lang.NumberFormatException

// 这个文件用来放 TC007 接口返回 JSON 的封装

/**
 * TC007 所有接口请求返回的格式内容.
 * @param Detail 当出错时，详细错误信息
 * @param Data 实际返回的数据，视不同的接口而定
 */
data class TC007Response<T>(
    val Code: Int,
    val Message: String?,
    val Translate: String?,
    val Detail: String?,
    val Data: T?,
) {
    /**
     * 判断请求是否成功.
     */
    fun isSuccess(): Boolean = Code == 200
}


/**
 * TC007 接口请求返回：产品信息
 * @param ProductName 产品名称
 * @param ProductPN PN
 * @param ProductSN SN
 * @param Code 激活码
 */
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

/**
 * TC007 接口请求返回：电池电量信息
 * @param Status Charging-充电中 Discharging-未充电
 * @param Remaining 剩余电量百分比
 */
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


/**
 * TC007 接口请求返回：固件升级状态
 * @param Status 当前升级状态 1-开始升级 2-升级中 3-升级失败 4-升级成功
 * @param Percent 当前升级进度百分比
 * @param Code 升级错误码
 */
data class TC07UpgradeStatus(
    val Status: Int,
    val Percent: Int,
    val Code: Int,
)

/**
 * TC007 接口返回：测温属性参数
 * @param Fps 测温帧率[0,采集帧率]，默认12，最高支持12
 * @param Level 测温档位 0-高增益 1-低增益 3-自动切换
 * @param OsdMode 测温信息叠加方式 0-视频编码前叠加 1-码流信息叠加(编码后预览时叠加) 2-无叠加
 * @param TempUnit 温度单位 0-摄氏度 1-开尔文 2-华氏度
 * @param DistanceUnit 距离单位 0-米 1-英尺
 */
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
//    constructor(isEnable: Boolean): this(FrameParam(isEnable), FrameParam(isEnable), FrameParam(isEnable))
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




/**
 * @param DCFile 可见光数据
 * @param IRFile 红外数据
 */
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

/**
 * 汇总TC007的所有的属性值
 */
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
    var brightness: Int = 50,//亮度, 0-100, 默认50
    var contrast: Int = 50,//对比度, 0-100, 默认50
    var saturation: Int = 50,//饱和度, 0-100, 默认50
    var sharpness: Int = 50,//锐度, 0-100, 默认50
    var flipMode: Int = 0,//翻转, 0:正常, 1:水平翻转 2:垂直翻转 3:180度翻转
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
    val mode : Int,//0：关，1：阈值上，2：阈值下，3：区间内
    val highThreshold : Int,
    val lowThreshold : Int,
    var greaterThreshold : Int = 0,
    var lessThreshold : Int = 0

)



