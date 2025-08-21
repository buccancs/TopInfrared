package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import com.blankj.utilcode.util.GsonUtils
import kotlinx.android.parcel.Parcelize

/**
 * 从服务器接口返回的，一页报告信息中的一条报告信息.
 */
@Parcelize
data class ReportItemBean(
    val testReportId: String?,
    val testInfo: String?, //上传的 JSON
    val testTime: String?,
    val uploadTime: String?, //上传时间
    val sn: String?,
    val url: String?,
    val status: Int?
) : Parcelable {

    var reportBean: ReportBean? = null
        get() {
            if (field == null) {
                field = GsonUtils.fromJson(testInfo, ReportBean::class.java)
            }
            return field
        }

    var isFirst: Boolean = false
    var isTitle: Boolean = false
}