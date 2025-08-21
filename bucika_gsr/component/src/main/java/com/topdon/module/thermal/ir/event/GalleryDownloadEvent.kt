package com.topdon.module.thermal.ir.event

/**
 * 有一张 TS004 热成像图片或视频从远端下载完毕事件.
 * @param filename 下载成功的文件名，如 xxx.jpg
 */
data class GalleryDownloadEvent(val filename: String)