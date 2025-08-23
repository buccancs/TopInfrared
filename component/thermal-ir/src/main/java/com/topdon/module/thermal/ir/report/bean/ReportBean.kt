package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReportBean(
    val software_info: SoftwareInfo,
    val report_info: ReportInfoBean,
    val detection_condition: ReportConditionBean,
    val infrared_data: List<ReportIRBean>
) : Parcelable
