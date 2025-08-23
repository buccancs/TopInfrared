package com.topdon.lib.core

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import androidx.annotation.RequiresApi
import com.blankj.utilcode.util.LanguageUtils
import com.elvishew.xlog.XLog
import com.topdon.lib.core.broadcast.DeviceBroadcastReceiver
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.tools.AppLanguageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

abstract class BaseApplication : Application() {

    companion object {
        lateinit var instance: BaseApplication
        val usbObserver by lazy { DeviceBroadcastReceiver() }
    }
    var tau_data_H: ByteArray? = null
    var tau_data_L: ByteArray? = null

    var activitys = arrayListOf<Activity>()
    var hasOtgShow = false//otg提示只出现一次

    /**
     * 获取软件编码.
     */
    abstract fun getSoftWareCode(): String

    /**
     * 是否国内渠道。
     *
     * 国内渠道一些逻辑不同，如国内渠道可以应用内升级，权限申请前有提示弹窗等。
     * 根据 2024/8/27 邮件结论，“热视界和电小搭其实没有形成销售，可以不用维护。”
     * @return true-国内渠道 false-非国内渠道
     */
    abstract fun isDomestic(): Boolean


    override fun onCreate() {
        super.onCreate()
        instance = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            webviewSetPath(this)
        }
        // ARouter removed - deprecated and not needed for offline mode
        onLanguageChange()

    }

    //清除无用数据
    fun clearDb() {
        GlobalScope.launch(Dispatchers.Default) {
            try {
                AppDatabase.getInstance().thermalDao().deleteZero(SharedManager.getUserId())
            } catch (e: Exception) {
                XLog.e("delete db error: ${e.message}")
            }
        }
    }

    open fun onLanguageChange() {
        val selectLan = SharedManager.getLanguage(baseContext)
        if (TextUtils.isEmpty(selectLan)) {
            if (isDomestic()) {
                //国内版默认中文
                val autoSelect = AppLanguageUtils.getChineseSystemLanguage()
                val locale = AppLanguageUtils.getLocaleByLanguage(autoSelect)
                LanguageUtils.applyLanguage(locale)
                SharedManager.setLanguage(baseContext, autoSelect)
            } else {
                //初始语言设置
                //默认初始语言，跟随系统语言设置，没有则默认英文
                val autoSelect = AppLanguageUtils.getSystemLanguage()
                val locale = AppLanguageUtils.getLocaleByLanguage(autoSelect)
                LanguageUtils.applyLanguage(locale)
                SharedManager.setLanguage(baseContext, autoSelect)
            }
        } else {
            val locale = AppLanguageUtils.getLocaleByLanguage(SharedManager.getLanguage(this))
            LanguageUtils.applyLanguage(locale)
        }
        WebView(this).destroy()
    }

    open fun getAppLanguage(context: Context): String? {
        return SharedManager.getLanguage(context)
    }

    /**
     * 退出所有
     */
    fun exitAll() {
        hasOtgShow = false
        activitys.forEach {
            it.finish()
        }
    }

}
