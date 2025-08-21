package com.topdon.tc001

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.module.thermal.ir.activity.IRMainActivity
import com.topdon.tc001.app.App

/**
 * des:usb识别的之后进行界面中转，所有的设备中转逻辑可参考次界面
 * author: CaiSongL
 * date: 2024/3/25 16:50
 **/
class BlankDevActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SharedManager.getHasShowClause()) {
            if (!App.instance.activityNameList.contains(IRMainActivity::class.simpleName)){
                ARouter.getInstance().build(RouterConfig.MAIN).navigation(this)
                if (!SharedManager.isConnectAutoOpen){
                    ARouter.getInstance().build(RouterConfig.IR_MAIN).navigation(this)
                }
            }
            finish()
        } else {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }

    fun isActivityExists(context: Context, activityClassName: String): Boolean {
        val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            ?: return false
        val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)
        for (task in tasks) {
            if (task.topActivity != null && task.topActivity!!.className == activityClassName) {
                return true
            }
            if (task.baseActivity != null && task.baseActivity!!.className == activityClassName) {
                return true
            }
        }
        return false
    }
}