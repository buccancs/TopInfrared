package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReportIRBean(
    var picture_id: String,
    var picture_url: String,
    val full_graph_data: ReportTempBean?,
    val point_data: List<ReportTempBean>,
    val line_data: List<ReportTempBean>,
    val surface_data: List<ReportTempBean>,
) : Parcelable
