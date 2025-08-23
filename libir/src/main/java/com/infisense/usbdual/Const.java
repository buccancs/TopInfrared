package com.infisense.usbdual;

import android.os.Environment;

import com.energy.iruvc.utils.CommonParams;
import com.energy.iruvc.utils.DeviceType;
import com.energy.iruvc.utils.DualCameraParams;

import java.io.File;

public class Const {

    public static final int TYPE_IR = 0;
    public static final int TYPE_IR_DUAL = 1;

    public static int RESTART_USB = 1000;
    public static int HANDLE_CONNECT = 10001;
    public static int HANDLE_REGISTER = 10002;
    public static int SHOW_LOADING = 1003;
    public static int HIDE_LOADING = 1004;
    public static int SHOW_RESTART_MESSAGE = 1005;
    public static int HIDE_LOADING_FINISH = 1006;

    public static boolean isReadFlashData = false;
    public static boolean isDeviceConnected = false;

    public static final String TAU_HIGH_GAIN_ASSET_PATH = "tau/V262_mini256带防尘片_H.bin";
    public static final String TAU_HIGH_LOW_ASSET_PATH = "tau/V262_mini256带防尘片_L.bin";

    public static DeviceType USE_DEVICE_TYPE = DeviceType.WN_256;
    public static final int PID = 0x5840;
    public static final int SENSOR_WIDTH = 256;
    public static final int SENSOR_HEIGHT = 384;
    public static int CAMERA_WIDTH = 640;
    public static int CAMERA_HEIGHT = 480;
    public static final int CAMERA_LOW_FPS = 15;
    public static final int CAMERA_HIGH_FPS = 30;

    public static int IR_WIDTH = 192;
    public static int IR_HEIGHT = 256;
    public static int VL_WIDTH = 480;
    public static int VL_HEIGHT = 640;
    public static final DualCameraParams.TypeLoadParameters IR_ROTATE = DualCameraParams.TypeLoadParameters.ROTATE_0;
    public static final CommonParams.PropImageParamsValue.MirrorFlipType IR_MIRROR_FLIP_TYPE = CommonParams.PropImageParamsValue.MirrorFlipType.NO_MIRROR_FLIP;

    public static int DUAL_WIDTH = 480;
    public static int DUAL_HEIGHT = 640;

    public static final String INFISENSE_DIR = "infiray";
    public static final String INFISENSE_SAVE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            + File.separator + INFISENSE_DIR;

    public static final String SP_KEY_ALIGN_INIT_DATA = "alignInitData";
    public static final String SP_KEY_ALIGN_ANGLE = "alignAngle";

    public static final int SP_KEY_PATTERN_WIDTH = 10;
    public static final int SP_KEY_PATTERN_HEIGHT = 7;
    public static final double SP_KEY_PATTERN_SPACE = 0.024;
    public static final int SP_KEY_CALIB_CNT = 13;
    public static final boolean SP_KEY_VL_DISTORTED = true;
    public static final double SP_KEY_CAM_DIST = 0.013;
    public static final boolean SP_KEY_IS_HORIZONTAL = true;
    public static final double SP_KEY_RATIO_THRESH = 0.9;
    public static final double SP_KEY_ROT_THRESH = 1.5;
}
