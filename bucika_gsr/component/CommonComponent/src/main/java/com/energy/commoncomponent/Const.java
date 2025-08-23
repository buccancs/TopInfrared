package com.energy.commoncomponent;

import android.os.Environment;

import com.blankj.utilcode.util.Utils;
import com.energy.commoncomponent.bean.DeviceType;

public class Const {
    public static final DeviceType DEVICE_TYPE = DeviceType.DEVICE_TYPE_TC2C;

    public static final String DATA_FILE_SAVE_PATH = Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();

    public static final String ZETA_ROOM_LIBRARY_CLASS = "com.energy.zetazoomlibrary.ZetaZoomHelper";
}
