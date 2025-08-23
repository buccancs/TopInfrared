package com.energy.commoncomponent

import android.os.Environment
import com.blankj.utilcode.util.Utils
import com.energy.commoncomponent.bean.DeviceType

object Const {
    val DEVICE_TYPE: DeviceType = DeviceType.DEVICE_TYPE_TC2C

    val DATA_FILE_SAVE_PATH: String = Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath

    const val ZETA_ROOM_LIBRARY_CLASS = "com.energy.zetazoomlibrary.ZetaZoomHelper"
}