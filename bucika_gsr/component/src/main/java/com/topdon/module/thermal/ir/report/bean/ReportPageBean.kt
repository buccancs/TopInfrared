package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 从服务器接口返回的，一页报告信息.
 */
@Parcelize
data class ReportPageBean(
    val total: Int = 0,
    var current: Int = 0,
    var pages: Int = 0,
    var size: Int = 0,
    var isHitCount: Boolean = false,
    var isOptimizeCountSql: Boolean = false,
    var isSearchCount: Boolean = false,
    var records: MutableList<ReportItemBean>? = null,
) : Parcelable