package com.energy.commoncomponent

import android.os.Environment
import com.blankj.utilcode.util.Utils
import com.energy.commoncomponent.bean.DeviceType

/**
 * Created by fengjibo on 2023/5/8.
 */
object Const {
    //todo 暂时通过此全局变量，区分不同的模组:指令调用，业务处理
    val DEVICE_TYPE: DeviceType = DeviceType.DEVICE_TYPE_TC2C

    val DATA_FILE_SAVE_PATH: String = Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath

    const val ZETA_ROOM_LIBRARY_CLASS = "com.energy.zetazoomlibrary.ZetaZoomHelper"
}