package com.topdon.lib.core.repository

// 这个文件用来放 TS004 接口返回 JSON 的封装

/**
 * TS004 所有接口请求返回的格式内容.
 * @param command 不知道什么东西，艾睿的文档也没说
 * @param data 实际返回的数据，视不同的接口而定
 * @param detail 请求结果描述，如 "ok"、"error: request process error"
 * @param status 状态码 0-成功 其他详见文档
 * @param transmit_cast 该请求耗时毫秒数
 */
data class TS004Response<T>(
    val command: Int,
    val data: T?,
    val detail: String?,
    val status: Int,
    val transmit_cast: Int,
) {
    /**
     * 判断请求是否成功.
     */
    fun isSuccess(): Boolean = status == 0
}

/**
 * TS004 接口请求返回：获取伪彩样式
 * @param enable
 * @param mode 当前伪彩样式
 */
data class PseudoColorBean(
    val enable: Boolean?,
    val mode: Int?,
)

/**
 * TS004 接口请求返回：获取测距
 * @param state 0-关闭，1-开启
 */
data class RangeBean(
    val state: Int?,
)

/**
 * TS004 接口请求返回：获取画中画
 * @param enable true 打开，false 关闭
 */
data class PipBean(
    val enable: Boolean?,
)

/**
 * TS004 接口请求返回：获取屏幕亮度
 * brightness: Int
 */
data class BrightnessBean(
    val brightness: Int
)
/**
 * TS004 接口请求返回：获取放大倍数
 * @param factor 缩放比例
 */
data class ZoomBean(
    val factor: Int?,
)

/**
 * TS004 接口请求返回：获取超分状态
 * @param enable 0-关闭 1-开启
 */
data class TISRBean(
    val enable: Int?,
)

/**
 * TS004 接口请求返回：版本信息
 * @param firmware 固件版本，如1.0
 */
data class VersionBean(
    val firmware: String?,
)

/**
 * TS004 接口请求返回：设备信息
 * @param code 激活码（又叫注册码）
 * @param model 应该是设备类型名称，如 TS004
 * @param sn sn
 * @param uuid 不知道啥
 */
data class DeviceInfo(
    val code: String,
    val model: String,
    val sn: String,
    val uuid: String,
)

/**
 * TS004 接口请求返回：文件数量
 * @param fileCount 文件数量
 */
data class FileCountBean(
    val fileCount: Int,
)

/**
 * 一页文件信息
 * @param current 当前分页数
 * @param total 总页数
 * @param filelist 当前分页文件信息列表
 */
data class FilePageBean(
    val current: Int,
    val total: Int,
    val filelist: List<FileBean>,
)

/**
 * TS004 接口请求返回：文件信息
 * @param type 0-照片 1-录像
 * @param duration 录像时长，单位秒
 * @param size 文件大小，单位 byte
 * @param name 文件名称，如 1970_01_02075103.mp4
 * @param thumb 录像缩略图
 * @param time 拍摄时 Unix 时间戳，单位秒
 * @param timezone 通过时区设置接口设置的时区
 */
data class FileBean(
    val id: Int,
    val type: Int,
    val duration: Int,
    val size: Long,
    val name: String,
    val thumb: String,
    val time: Long,
    val timezone: Int,
)

/**
 * TS004 接口请求返回：固件升级状态
 * @param status 当前升级状态 1-start 2-running 3-failed 4-success
 * @param percent 当前升级进度百分比
 */
data class UpgradeStatus(
    val status: Int,
    val percent: Int,
)

/**
 * TS004 接口请求返回：文件信息
 * @param total 总存储大小，单位 byte
 * @param free 剩余可用存储大小，单位 byte
 * @param system 系统占用大小，单位 byte
 * @param image_size 照片存储大小，单位 byte
 * @param video_size 视频存储大小，单位 byte
 */
data class FreeSpaceBean(
    val total: Long,
    val free: Long,
    val system: Long,
    val image_size: Long,
    val video_size: Long,
) {
    fun hasUseSize(): Long = system + image_size + video_size
}

/**
 * TS004 接口请求返回：获取录像状态
 * @param errCode 录制的错误代码， 0:无错误，1: 初始化错误，2: 电池电量低
 * @param path 当前录制的视频文件名
 * @param pts 当前录制的时间
 * @param status 当前录制的开关
 */
data class RecordStatusBean(
    val errCode: Int,
    val path: String,
    val pts: Int,
    val status: Boolean,
)