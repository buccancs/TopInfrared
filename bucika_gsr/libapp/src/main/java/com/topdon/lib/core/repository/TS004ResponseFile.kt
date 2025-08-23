package com.topdon.lib.core.repository

data class TS004Response<T>(
    val command: Int,
    val data: T?,
    val detail: String?,
    val status: Int,
    val transmit_cast: Int,
) {
    
    fun isSuccess(): Boolean = status == 0
}

data class PseudoColorBean(
    val enable: Boolean?,
    val mode: Int?,
)

data class RangeBean(
    val state: Int?,
)

data class PipBean(
    val enable: Boolean?,
)

data class BrightnessBean(
    val brightness: Int
)

data class ZoomBean(
    val factor: Int?,
)

data class TISRBean(
    val enable: Int?,
)

data class VersionBean(
    val firmware: String?,
)

data class DeviceInfo(
    val code: String,
    val model: String,
    val sn: String,
    val uuid: String,
)

data class FileCountBean(
    val fileCount: Int,
)

data class FilePageBean(
    val current: Int,
    val total: Int,
    val filelist: List<FileBean>,
)

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

data class UpgradeStatus(
    val status: Int,
    val percent: Int,
)

data class FreeSpaceBean(
    val total: Long,
    val free: Long,
    val system: Long,
    val image_size: Long,
    val video_size: Long,
) {
    fun hasUseSize(): Long = system + image_size + video_size
}

data class RecordStatusBean(
    val errCode: Int,
    val path: String,
    val pts: Int,
    val status: Boolean,
)