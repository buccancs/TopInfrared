package com.topdon.lib.core.bean.json

data class CheckVersionJson(
    val downloadPackageUrl: String,
    val downloadPageUrl: String,
    val forcedUpgradeFlag: String?,
    val googleVerCode: Int,
    val softConfigOtherTypeVOList: List<SoftConfigOtherTypeVO>,
    val versionCode: Int,
    val versionNo: String?,
    val notUnZipSize: Double,
)

data class SoftConfigOtherTypeVO(
    val descType: Int,
    val descTypeName: String,
    val fileUrl: Any,
    val textDescription: String
)