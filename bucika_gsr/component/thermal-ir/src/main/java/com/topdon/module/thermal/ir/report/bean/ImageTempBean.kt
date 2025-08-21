package com.topdon.module.thermal.ir.report.bean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 一张图片的点线面全图温度信息.
 */
@Parcelize
data class ImageTempBean(
    val full: TempBean?,//全图
    val pointList: ArrayList<TempBean>,//点
    val lineList: ArrayList<TempBean>, //线
    val rectList: ArrayList<TempBean>, //面
) : Parcelable {

    @Parcelize
    data class TempBean(
        val max: String,//不带符号最高温，单位跟随用户配置
        val min: String? = null,//不带符号最低温，单位跟随用户配置
        val average: String? = null,//不带符号平均温，单位跟随用户配置
    ) : Parcelable
}
