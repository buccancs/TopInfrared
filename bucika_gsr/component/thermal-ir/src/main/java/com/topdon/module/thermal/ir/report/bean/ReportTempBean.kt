package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReportTempBean(
    val max_temperature: String?, //带单位符号的最高温，单位跟随用户配置
    val is_max_temperature: Int,  //是否显示最高温
    val min_temperature: String?, //带单位符号的最低温，单位跟随用户配置
    val is_min_temperature: Int,  //是否显示最低温

    val comment: String?,//注释
    val is_comment: Int, //是否显示注释

    val mean_temperature: String? = null,//带单位符号的平均温，单位跟随用户配置
    val is_mean_temperature: Int = 0,    //是否显示平均温

    val temperature: String? = null,//带单位符号的点温度，单位跟随用户配置
    val is_temperature: Int = 0,    //是否显示点温度
): Parcelable {

    constructor(temperature: String?, is_temperature: Int, comment: String?, is_comment: Int): this(
        null,
        0,
        null,
        0,
        comment,
        is_comment,
        null,
        0,
        temperature,
        is_temperature
    )

    fun isMaxOpen() = is_max_temperature == 1
    fun isMinOpen() = is_min_temperature == 1
    fun isAverageOpen() = is_mean_temperature == 1
    fun isExplainOpen() = is_comment == 1

    fun isTempOpen() = is_temperature == 1
}
