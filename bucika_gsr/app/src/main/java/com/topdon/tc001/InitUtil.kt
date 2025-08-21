package com.topdon.tc001

import android.content.Context
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Build
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.Utils
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.flattener.PatternFlattener
import com.elvishew.xlog.printer.AndroidPrinter
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.backup.FileSizeBackupStrategy2
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.elvishew.xlog.printer.file.naming.ChangelessFileNameGenerator
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.broadcast.DeviceBroadcastReceiver
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.HttpConfig
import com.topdon.lms.sdk.LMS
import com.topdon.lms.sdk.utils.ConstantUtil
import com.topdon.lms.sdk.utils.LanguageUtil
import java.util.Date

/**
 * 国内权限申请后才能初始化
 */
object InitUtil {
    fun initLog() {
        val fileName = "logs_${TimeUtils.date2String(Date(), "yyyy-MM-dd")}.log"
        val fileDir = BaseApplication.instance.getExternalFilesDir("log")!!.absolutePath
        val tag = "TopInfrared_LOG"
        val pattern = "{d}, {L}, {t}, {m}"
        val backupStrategy = FileSizeBackupStrategy2(5 * 1024 * 1024L, 10) //一份文件的大小
        val cleanStrategy = FileLastModifiedCleanStrategy(30 * 24 * 60 * 60) //设置自动清除时间

        val config = LogConfiguration.Builder()
            .logLevel(LogLevel.ALL)
            .tag(tag)
            .build()
        val androidPrinter = AndroidPrinter(true)
        val filePrinter = FilePrinter.Builder(fileDir) //指定保存日志文件的路径
            .fileNameGenerator(ChangelessFileNameGenerator(fileName)) //指定日志文件名生成器
            .backupStrategy(backupStrategy) //指定日志文件备份策略
            .cleanStrategy(cleanStrategy) //指定日志文件清除策略
            .flattener(PatternFlattener(pattern)) //自定义日志格式
            .build()
        if (BuildConfig.DEBUG) {
            XLog.init(config, androidPrinter, filePrinter)
        } else {
            // release不使用logcat
            XLog.init(config, filePrinter)
        }
    }

    fun initLms(){
        //隐私政策地址
        val privacyPolicyUrl = "https://plat.topdon.com/topdon-plat/out-user/baseinfo/template/getHtmlContentById?" +
                "softCode=${BaseApplication.instance.getSoftWareCode()}&" +
                "language=${LanguageUtil.getLanguageId(Utils.getApp())}&type=22"
        //用户协议地址
        val servicesAgreementUrl = "https://plat.topdon.com/topdon-plat/out-user/baseinfo/template/getHtmlContentById?" +
                "softCode=${BaseApplication.instance.getSoftWareCode()}&" +
                "language=${LanguageUtil.getLanguageId(Utils.getApp())}&type=21"

        LMS.getInstance().init(BaseApplication.instance)
            .apply {
                productType = "TC001"
                setLoginType(ConstantUtil.LOGIN_TS001_TYPE)
                softwareCode = BaseApplication.instance.getSoftWareCode()
                setEnabledLog(BuildConfig.DEBUG)
                setPrivacyPolicy(privacyPolicyUrl)
                setServicesAgreement(servicesAgreementUrl)
                if (!BaseApplication.instance.isDomestic()) {
                    initXutils()
                } else {
                    //有数据之后需要进行替换
                    setWxAppId("wx588cb319449b72dd")
                    setBuglyAppId("0b375add84")
                    //国内版需要友盟
//                    setUMengAppKey("65780ed9a7208a5af184643c", channel, "")
                }
                setAppKey(BuildConfig.APP_KEY)
                setAppSecret(BuildConfig.APP_SECRET)
                setAuthSecret(HttpConfig.AUTH_SECRET)
            }
    }

    fun initUM() {
//        if (BaseApplication.instance.isDomestic()){
            //只有国内版才需要接入友盟
//            UMConfigure.setLogEnabled(BuildConfig.DEBUG)
//            //友盟预初始化
//            UMConfigure.preInit(BaseApplication.instance, "659384b895b14f599d0d9247", "Um-eng")
//            //判断是否同意隐私协议，uminit为1时为已经同意，直接初始化umsdk
//            UMConfigure.init(
//                BaseApplication.instance,
//                "659384b895b14f599d0d9247",
//                "Um-eng",
//                UMConfigure.DEVICE_TYPE_PHONE,
//                ""
//            )
//            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
//        }
    }

    fun initJPush() {
        var registrationID = ""
//        if (BaseApplication.instance.isDomestic()){
//            //只有国内版才需要接入友盟
//            JPushInterface.setDebugMode(BuildConfig.DEBUG)
//            JPushInterface.init(BaseApplication.instance)
//            registrationID = JPushInterface.getRegistrationID(BaseApplication.instance)
//        }
        if (SharedManager.getHasShowClause()) {
            XLog.w("registrationID= $registrationID")
        }
    }

    fun initReceiver() {
        try {
            BaseApplication.instance.unregisterReceiver(BaseApplication.usbObserver)
        } catch (e: Exception) { }
        //必须动态注册,否则部分机型无法收到usb状态
        val filter = IntentFilter()
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED)
        filter.addAction(DeviceBroadcastReceiver.ACTION_USB_PERMISSION) // 申请USB权限
        if (Build.VERSION.SDK_INT < 33) {
            BaseApplication.instance.registerReceiver(BaseApplication.usbObserver, filter)
        } else {
            BaseApplication.instance.registerReceiver(BaseApplication.usbObserver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
    }

}