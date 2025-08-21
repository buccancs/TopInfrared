package com.topdon.lib.core.tools

import android.content.Context
import android.os.Build
import com.blankj.utilcode.util.AppUtils
import com.elvishew.xlog.XLog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.R
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lms.sdk.weiget.TToast

object PermissionTool {

    /**
     * 请求 RECORD_AUDIO 权限.
     */
    fun requestRecordAudio(context: Context, callback: () -> Unit) = request(context, Type.RECORD_AUDIO, callback)

    /**
     * 请求 CAMERA 权限.
     */
    fun requestCamera(context: Context, callback: () -> Unit) = request(context, Type.CAMERA, callback)

    /**
     * 请求 ACCESS_FINE_LOCATION 权限.
     */
    fun requestLocation(context: Context, callback: () -> Unit) = request(context, Type.LOCATION, callback)

    /**
     * 请求 图片读取 权限.
     */
    fun requestImageRead(context: Context, callback: () -> Unit) = request(context, Type.IMAGE, callback)

    /**
     * Android 10 及以下：请求外部存储文件读、写权限
     *
     * Android 11、Android 12、Android 12L：请求外部存储读权限
     *
     * Android 13 及以上：请求媒体-视频、媒体-图片权限
     */
    fun requestFile(context: Context, callback: () -> Unit) = request(context, Type.FILE, callback)



    private enum class Type { RECORD_AUDIO, CAMERA, LOCATION, IMAGE, FILE }

    private fun request(context: Context, type: Type, callback: () -> Unit) {
        val permissions: List<String> = when (type) {
            Type.RECORD_AUDIO -> listOf(Permission.RECORD_AUDIO)
            Type.CAMERA -> listOf(Permission.CAMERA)
            Type.LOCATION -> listOf(Permission.ACCESS_COARSE_LOCATION, Permission.ACCESS_FINE_LOCATION)
            Type.IMAGE -> listOf(if (context.applicationInfo.targetSdkVersion < 33) Permission.READ_EXTERNAL_STORAGE else Permission.READ_MEDIA_IMAGES)
            Type.FILE -> if (context.applicationInfo.targetSdkVersion < 30) {//Android 10及以下
                listOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
            } else if (context.applicationInfo.targetSdkVersion < 33) {//Android 13以下
                listOf(Permission.READ_EXTERNAL_STORAGE)
            } else {//Android 13及以上
                listOf(Permission.READ_MEDIA_VIDEO, Permission.READ_MEDIA_IMAGES)
            }
        }

        XXPermissions.with(context)
            .permission(permissions)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        callback.invoke()
                    } else {
                        TToast.shortToast(context, R.string.scan_ble_tip_authorize)
                    }
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    if (never) {
                        val tipsResId: Int = when (type) {
                            Type.RECORD_AUDIO -> R.string.app_microphone_content
                            Type.CAMERA -> R.string.app_camera_content
                            Type.LOCATION -> R.string.app_location_content
                            Type.IMAGE -> R.string.app_album_content
                            Type.FILE -> R.string.app_storage_content
                        }
                        if (BaseApplication.instance.isDomestic()) {//国内版
                            TToast.shortToast(context, tipsResId)
                        } else {
                            TipDialog.Builder(context)
                                .setTitleMessage(context.getString(R.string.app_tip))
                                .setMessage(tipsResId)
                                .setPositiveListener(R.string.app_open) {
                                    AppUtils.launchAppDetailsSettings()
                                }
                                .setCancelListener(R.string.app_cancel) {
                                }
                                .setCanceled(true)
                                .create().show()
                        }
                    } else {
                        TToast.shortToast(context, R.string.scan_ble_tip_authorize)
                    }
                }
            })
    }



    /**
     * 判断是否具有 ACCESS_FINE_LOCATION、BLUETOOTH_SCAN、BLUETOOTH_CONNECT 权限。
     * 低于 Android12 视为具有。
     */
    fun hasBtPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT < 31) {//低于 Android12
            XXPermissions.isGranted(context, Permission.ACCESS_FINE_LOCATION)
        } else {
            XXPermissions.isGranted(context, Permission.ACCESS_FINE_LOCATION, Permission.BLUETOOTH_SCAN, Permission.BLUETOOTH_CONNECT)
        }
    }

    /**
     * 仅当 Android12 及以上版本时，请求 BLUETOOTH_SCAN、BLUETOOTH_CONNECT 权限
     * @param isBtFirst true-永久拒绝时优先提示蓝牙 false-永久拒绝时优先提示定位
     */
    fun requestBluetooth(context: Context, isBtFirst: Boolean, callback: Callback) {
        val permissionList: List<String> = if (Build.VERSION.SDK_INT < 31) {//低于 Android12
            arrayListOf(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)
        } else {
            arrayListOf(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION, Permission.BLUETOOTH_SCAN, Permission.BLUETOOTH_CONNECT)
        }

        XXPermissions.with(context)
            .permission(permissionList)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    XLog.i("onGranted($allGranted)")
                    callback.onResult(allGranted)
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    XLog.i("onDenied($never)")
                    if (never) {
                        var isBtNever = false
                        var isLocationNever = false
                        for (permission in permissions) {
                            if (permission == Permission.BLUETOOTH_SCAN || permission == Permission.BLUETOOTH_CONNECT) {
                                isBtNever = true
                            }
                            if (permission == Permission.ACCESS_FINE_LOCATION || permission == Permission.ACCESS_COARSE_LOCATION) {
                                isLocationNever = true
                            }
                        }
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        TipDialog.Builder(context)
                            .setTitleMessage(context.getString(R.string.app_tip))
                            .setMessage(if (!isLocationNever || (isBtNever && isBtFirst)) R.string.app_bluetooth_content else R.string.app_location_content)
                            .setPositiveListener(R.string.app_open) {
                                XXPermissions.startPermissionActivity(context, permissions)
                                callback.onNever(true)
                            }
                            .setCancelListener(R.string.app_cancel) {
                                callback.onNever(false)
                            }
                            .setCanceled(true)
                            .create().show()
                    } else {
                        callback.onResult(false)
                    }
                }
            })
    }

    interface Callback {
        /**
         * 未被永久拒绝时，全部授予 或 有部分未授予 回调.
         */
        fun onResult(allGranted: Boolean)

        /**
         * 永久拒绝时，跳转弹框 去打开 或 取消 回调.
         */
        fun onNever(isJump: Boolean)
    }
}