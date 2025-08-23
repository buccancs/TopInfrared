package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import com.blankj.utilcode.util.GsonUtils
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReportItemBean(
    val testReportId: String?,
    val testInfo: String?,
    val testTime: String?,
    val uploadTime: String?,
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