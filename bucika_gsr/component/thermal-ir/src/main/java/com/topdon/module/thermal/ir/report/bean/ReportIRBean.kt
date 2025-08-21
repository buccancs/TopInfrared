package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 一张图片对应的数据信息.
 */
@Parcelize
data class ReportIRBean(
    var picture_id: String, //上传服务器后接口返回的图片Id
    var picture_url: String,//上传服务器后接口返回的图片URL
    val full_graph_data: ReportTempBean?,   //全图数据
    val point_data: List<ReportTempBean>,  //点数据
    val line_data: List<ReportTempBean>,   //线数据
    val surface_data: List<ReportTempBean>,//面数据
) : Parcelable
