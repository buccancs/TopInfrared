package com.topdon.lib.core.ktbase

import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.WindowManager
import com.hjq.permissions.Permission
import com.topdon.lib.core.utils.NetWorkUtils

/**
 * des:
 * author: CaiSongL
 * date: 2024/4/28 17:08
 **/
abstract class BaseWifiActivity : BaseActivity() {


    protected val permissionList by lazy {
        if (this.applicationInfo.targetSdkVersion >= 34){
            listOf(
                Permission.WRITE_EXTERNAL_STORAGE,
            )
        } else if (this.applicationInfo.targetSdkVersion == 33) {
            mutableListOf(
                Permission.READ_MEDIA_VIDEO, Permission.READ_MEDIA_IMAGES,
                Permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            mutableListOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= 29) {
            //Android10 及以上
            NetWorkUtils.switchNetwork(true)
        }
        super.onCreate(savedInstanceState)
        PreferenceManager.getDefaultSharedPreferences(this@BaseWifiActivity)
            .edit()
            .putBoolean("use-sw-codec", true)
            .apply()
        PreferenceManager.getDefaultSharedPreferences(this@BaseWifiActivity)
            .getBoolean("auto_audio", false);
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 29) {//Android10 及以上
            NetWorkUtils.switchNetwork(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
    }

}