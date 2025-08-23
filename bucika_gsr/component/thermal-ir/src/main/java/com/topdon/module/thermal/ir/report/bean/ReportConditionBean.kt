package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReportConditionBean(
    val ambient_humidity: String?,
    val is_ambient_humidity: Int,
    val ambient_temperature: String?,
    val is_ambient_temperature: Int,
    val emissivity: String?,
    val is_emissivity: Int,
    val test_distance: String?,
    val is_test_distance: Int,
) : Parcelable