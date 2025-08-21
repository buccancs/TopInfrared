package com.topdon.tc001.utils

import android.content.Context
import android.text.TextUtils
import com.topdon.tc001.BuildConfig

object VersionUtils {

    fun getCodeStr(context: Context):String{
        val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
        var codeStr = "$versionName"
        codeStr = if (BuildConfig.DEBUG) "${codeStr}_debug" else codeStr
        return codeStr
    }

}