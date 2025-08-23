package com.topdon.lib.core.bean.event

data class VersionUpData(val versionNo: String, val isForcedUpgrade: Boolean, val description: String, val downPageUrl : String, val sizeStr: String)
