package com.topdon.module.thermal.ir.report.bean

import android.os.Build
import android.os.Parcelable
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.tools.AppLanguageUtils
import com.topdon.module.thermal.ir.BuildConfig
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SoftwareInfo(
    val app_language: String,   // APP语言
    val sdk_version: String,    // SDK版本
) : Parcelable {

    val software_code = BaseApplication.instance.getSoftWareCode() //软件编码
    val system_language = AppLanguageUtils.getSystemLanguage()// 系统语言
    val app_version = BuildConfig.VERSION_NAME//软件版本
    val hardware_version = ""//硬件版本
    val app_sn = ""
    val mobile_phone_model = Build.BRAND//手机型号
    val system_version = Build.VERSION.RELEASE//系统版本
}