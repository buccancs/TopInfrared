package com.topdon.lib.core.bean

import android.os.Parcelable
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.repository.FileBean
import com.topdon.lib.core.tools.TimeTool
import com.topdon.lib.core.tools.VideoTools
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.util.TimeZone

@Parcelize
open class GalleryBean(
    val id: Int, //仅TS004远端时，id
    val path: String,
    val thumb: String,
    val name: String,
    val duration: Long,//仅当为视频时，持续毫秒数
    val timeMillis: Long,
    var hasDownload: Boolean,
) : Parcelable {
    constructor(file: File): this(
        id = 0,
        path = file.absolutePath,
        thumb = file.absolutePath,
        name = file.name,
        duration = VideoTools.getLocalVideoDuration(file.absolutePath),
        timeMillis = TimeTool.updateDateTime(file),
        hasDownload = true,
    )

    constructor(isVideo: Boolean, fileBean: FileBean): this(
        id = fileBean.id,
        path = "http://192.168.40.1:8080/DCIM/${fileBean.name}",
        thumb = if (isVideo) "http://192.168.40.1:8080/DCIM/${fileBean.thumb}" else "http://192.168.40.1:8080/DCIM/${fileBean.name}",
        name = fileBean.name,
        duration = fileBean.duration * 1000L,
        timeMillis = fileBean.time * 1000 - TimeZone.getDefault().getOffset(fileBean.time * 1000),
        hasDownload = File(FileConfig.ts004GalleryDir, fileBean.name).exists(),
    )
}

class GalleryTitle(timeMillis: Long) : GalleryBean(
    id = 0,
    path = "",
    thumb = "",
    name = "",
    duration = 0L,
    timeMillis = timeMillis,
    hasDownload = true,
)

