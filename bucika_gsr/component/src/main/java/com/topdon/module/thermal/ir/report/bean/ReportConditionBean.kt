package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 检测条件.
 *
 * 报告由 3 部分组成：报告信息、检测条件、红外数据.
 */
@Parcelize
data class ReportConditionBean(
    val ambient_humidity: String?,   //环境湿度
    val is_ambient_humidity: Int,    //是否显示环境湿度，0、不显示 1、显示
    val ambient_temperature: String?,//带单位符号的环境温度，单位跟随用户配置
    val is_ambient_temperature: Int, //是否显示环境温度，0、不显示 1、显示
    val emissivity: String?,         //发射率
    val is_emissivity: Int,          //是否显示发射率，0、不显示 1、显示
    val test_distance: String?,      //测试距离
    val is_test_distance: Int,       //是否显示测试距离，0、不显示 1、显示
) : Parcelable