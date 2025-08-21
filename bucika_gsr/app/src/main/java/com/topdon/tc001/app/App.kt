package com.topdon.tc001.app

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.elvishew.xlog.XLog
import com.example.thermal_lite.IrConst
import com.example.thermal_lite.util.CommonUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.HttpConfig
import com.topdon.lib.ui.recycler.LoadingFooter
import com.topdon.lms.sdk.Config
import com.topdon.lms.sdk.LMS.mContext
import com.topdon.lms.sdk.UrlConstant
import com.topdon.lms.sdk.utils.SPUtils
import com.topdon.tc001.BuildConfig
import com.topdon.tc001.InitUtil.initJPush
import com.topdon.tc001.InitUtil.initLms
import com.topdon.tc001.InitUtil.initLog
import com.topdon.tc001.InitUtil.initReceiver
import com.topdon.tc001.InitUtil.initUM
import com.zoho.livechat.android.listeners.InitListener
import com.zoho.salesiqembed.ZohoSalesIQ
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class App : BaseApplication() {

    init {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            MaterialHeader(
                context
            )
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            LoadingFooter(context)
        }
    }

    companion object{
        lateinit var instance: App
        /**
         * 延时初始化
         */
        fun delayInit() {
            initReceiver()
            initLog()
            initLms()
            initUM()
            initJPush()
        }
    }




    override fun getSoftWareCode(): String = BuildConfig.SOFT_CODE

    override fun isDomestic(): Boolean = BuildConfig.ENV_TYPE == 1

    val activityNameList : MutableList<String> = mutableListOf()

    override fun onCreate() {
        super.onCreate()
        instance = this
        //隐私政策弹框用app内的，默认设置lms里的隐私政策设置为true
        SPUtils.getInstance(this).put(Config.KEY_PRIVACY_AGREEMENT, true)

        if (SharedManager.getHasShowClause() || !isDomestic()) {
            delayInit()
        }

        RxJavaPlugins.setErrorHandler {
            if (SharedManager.getHasShowClause()) {
                XLog.w("未知异常： ${it.message}")
            }
        }
        if (!isDomestic()) {
            if (!BuildConfig.DEBUG) {
                // 正式版本,服务地址强制转换正式地址,并且不可更换
                // 测试版本,可自由切换
                UrlConstant.setBaseUrl("${HttpConfig.HOST}/", false)
            } else {
                if (SharedManager.getHasShowClause()) {
                    XLog.w("lms host: ${UrlConstant.BASE_URL}")
                }
            }
            SharedManager.setBaseHost(UrlConstant.BASE_URL) //更新app服务地址
        }
        if(BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false);
        }
        CoroutineScope(Dispatchers.IO).launch {
            tau_data_H = CommonUtil.getAssetData(mContext, IrConst.TAU_HIGH_GAIN_ASSET_PATH)
            tau_data_L = CommonUtil.getAssetData(mContext, IrConst.TAU_LOW_GAIN_ASSET_PATH)
        }
//        CrashReport.initCrashReport(applicationContext, "cd1f9e26ee", false)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (!activityNameList.contains(activity.javaClass.getSimpleName())){
                    activityNameList.add(activity.javaClass.getSimpleName())
                }
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }
            override fun onActivityDestroyed(activity: Activity) {
                activityNameList.remove(activity.javaClass.getSimpleName())
            }

        })
        initZoho()
    }

    /**
     * 初始化客服ZOHO
     */
    private fun initZoho() {
        ZohoSalesIQ.init(
            this,
            "IjGWlJ%2FAnwvKPO0yHSMeLDRbq9%2Bcumf0TA6lWzHNybOq7Ew5UI7135B1F4y60Vwh",
            "CvYpd1tLP6hT1aJmYxGdvW8UtM0LUMt6bBvazW%2FbsCBFODZM54UgnVzDVtVbh%2F3hcFU7q4JlCZCw7vElzm8MeN5MdZjWoFSAKHNNgYfT33vNaBPm8ASTII05T57%2F3WxK",
            null,
            object : InitListener {
                override fun onInitSuccess() {
//                    ZohoSalesIQ.Launcher.show(ZohoSalesIQ.Launcher.VisibilityMode.ALWAYS)
                    XLog.e("bcf", "ZohoSalesIQ成功")
                }

                override fun onInitError(errorCode: Int, errorMessage: String?) {
                    //your code
                    XLog.e("bcf", "ZohoSalesIQ失敗")
                }
            })
    }
}

