package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import com.topdon.lib.core.utils.CommUtils
import kotlinx.android.parcel.Parcelize

/**
 * 报告信息.
 *
 * 报告由 3 部分组成：报告信息、检测条件、红外数据.
 */
@Parcelize
data class ReportInfoBean(
    val report_name: String?,    //报告名称
    val report_author: String?, //作者名称
    val is_report_author: Int,  //是否显示作者名称，0、不显示 1、显示
    val report_date: String?,   //报告日期
    val is_report_date: Int,    //是否显示报告日期，0、不显示 1、显示
    val report_place: String?,  //报告地点
    val is_report_place: Int,   //是否显示报告地点，0、不显示 1、显示
    val report_watermark: String?,//报告水印
    val is_report_watermark: Int, //是否显示报告水印，0、不显示 1、显示
) : Parcelable {

    val is_report_name: Int = 1//是否显示报告名称，0、不显示 1、显示
    val report_type: Int = 1     //报告类型，1、点线面报告
    val report_version: String = "V1.00"//报告版本，当前为 V1.00
    val report_number: String = "${CommUtils.getAppName()}${System.currentTimeMillis()}"//报告编号，APP名称 + 时间戳秒级
}