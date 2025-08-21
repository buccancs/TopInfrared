package com.topdon.lib.core.bean.event

/**
 * @param isForcedUpgrade 是否为强制升级
 * @param description 版本升级描述
 * @param downPageUrl 下载 Url
 */
data class VersionUpData(val versionNo: String, val isForcedUpgrade: Boolean, val description: String, val downPageUrl : String, val sizeStr: String)
