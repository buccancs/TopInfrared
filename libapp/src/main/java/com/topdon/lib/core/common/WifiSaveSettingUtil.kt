package com.topdon.lib.core.common

import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.gson.Gson
import com.topdon.lib.core.bean.AlarmBean
import com.topdon.lib.core.bean.CameraItemBean
import com.topdon.lib.core.bean.ObserveBean
import com.topdon.lib.core.common.SaveSettingUtil.FusionTypeIROnly
import com.topdon.lib.core.common.SaveSettingUtil.FusionTypeLPYFusion
import com.topdon.lib.core.config.DeviceConfig
import com.topdon.lib.core.utils.CommUtils

object WifiSaveSettingUtil {
    
    private const val SP_NAME = "WifiSaveSettingUtil"

    const val TYPE_PLUG = 0
    const val TYPE_WIFI = 1

    fun reset() {
        isMeasureTempMode = true
        isVideoMode = false
        isAutoShutter = true
        isRecordAudio = false
        isOpenMirror = false
        delayCaptureSecond = 0
        contrastValue = 128
        pseudoColorMode = 3
        rotateAngle = DeviceConfig.S_ROTATE_ANGLE

        isOpenPseudoBar = true
        isOpenTwoLight = false
        twoLightAlpha = 50
        ddeConfig = 2
        tempTextColor = 0xffffffff.toInt()
        temperatureMode = CameraItemBean.TYPE_TMP_C
        alarmBean = AlarmBean()

        isOpenCompass = false
        isOpenHighPoint = false
        isOpenLowPoint = false
        aiTraceType = ObserveBean.TYPE_NONE
        isOpenTarget = false
        targetMeasureMode = ObserveBean.TYPE_MEASURE_PERSON
        targetType = ObserveBean.TYPE_TARGET_HORIZONTAL
        targetColorType = ObserveBean.TYPE_TARGET_COLOR_GREEN

        reportAuthorName = CommUtils.getAppName()
        reportWatermarkText = CommUtils.getAppName()
        reportHumidity = 500
        fusionType = FusionTypeLPYFusion
        registrationX = 0
        registrationY = 0

    }
    var registrationX : Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("registrationX", 0) else 0
        set(value) {
            SPUtils.getInstance(SP_NAME).put("registrationX", value)
        }
    var registrationY : Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("registrationY", 0) else 0
        set(value) {
            SPUtils.getInstance(SP_NAME).put("registrationY", value)
        }
    var fusionType : Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("fusionType", FusionTypeIROnly) else FusionTypeIROnly
        set(value) {
            SPUtils.getInstance(SP_NAME).put("fusionType", value)
        }

    var isSaveSetting: Boolean
        get() = SPUtils.getInstance(SP_NAME).getBoolean("isSaveSetting", true)
        set(value) {
            SPUtils.getInstance(SP_NAME).put("isSaveSetting", value)
        }

    var isMeasureTempMode: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getBoolean("isMeasureTempMode", true) else true
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isMeasureTempMode", value)
            }
        }

    var isVideoMode: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isVideoMode", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isVideoMode", value)
            }
        }

    var isAutoShutter: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isAutoShutter", true) else true
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isAutoShutter", value)
            }
        }

    var isRecordAudio: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isRecordAudio", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isRecordAudio", value)
            }
        }

    var isOpenMirror: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenMirror", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenMirror", value)
            }
        }

    var delayCaptureSecond: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("delayCaptureSecond", 0) else 0
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("delayCaptureSecond", value)
            }
        }

    var contrastValue: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("contrastValue", 128) else 128
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("contrastValue", value)
            }
        }

    var pseudoColorMode: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("pseudoColorMode", 3) else 3
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("pseudoColorMode", value)
            }
        }

    var rotateAngle: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("rotateAngle", DeviceConfig.S_ROTATE_ANGLE) else DeviceConfig.S_ROTATE_ANGLE
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("rotateAngle", value)
            }
        }

    var isOpenPseudoBar: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenPseudoBar", true) else true
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenPseudoBar", value)
            }
        }

    var isOpenTwoLight: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenTwoLight", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenTwoLight", value)
            }
        }

    var twoLightAlpha: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("twoLightAlpha", 50) else 50
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("twoLightAlpha", value)
            }
        }

    var ddeConfig: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("ddeConfig", 2) else 2
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("ddeConfig", value)
            }
        }

    var tempTextColor: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("tempTextColor", 0xffffffff.toInt()) else 0xffffffff.toInt()
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("tempTextColor", value)
            }
        }
    
    var tempTextSize: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("tempTextSize", 14) else 14
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("tempTextSize", value)
            }
        }

    var temperatureMode: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("temperatureMode", CameraItemBean.TYPE_TMP_C) else CameraItemBean.TYPE_TMP_C
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("temperatureMode", value)
            }
        }

    var alarmBean: AlarmBean
        get() = if (isSaveSetting) {
            val json = SPUtils.getInstance(SP_NAME).getString("alarmBean", "")
            if (json.isNullOrEmpty()) AlarmBean() else Gson().fromJson(json, AlarmBean::class.java)
        } else {
            AlarmBean()
        }
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("alarmBean", Gson().toJson(value))
            }
        }

    var isOpenCompass: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenCompass", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenCompass", value)
            }
        }

    var isOpenHighPoint: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenHighPoint", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenHighPoint", value)
            }
        }

    var isOpenLowPoint: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenLowPoint", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenLowPoint", value)
            }
        }

    var aiTraceType: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("aiTraceType", ObserveBean.TYPE_NONE) else ObserveBean.TYPE_NONE
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("aiTraceType", value)
            }
        }

    var isOpenTarget: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenTarget", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenTarget", value)
            }
        }

    var targetMeasureMode: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt(
            "targetMeasureMode",
            ObserveBean.TYPE_MEASURE_PERSON
        ) else ObserveBean.TYPE_MEASURE_PERSON
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("targetMeasureMode", value)
            }
        }

    var targetType: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt(
            "targetType",
            ObserveBean.TYPE_TARGET_HORIZONTAL
        ) else ObserveBean.TYPE_TARGET_HORIZONTAL
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("targetType", value)
            }
        }

    var targetColorType: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt(
            "targetColorType",
            ObserveBean.TYPE_TARGET_COLOR_GREEN
        ) else ObserveBean.TYPE_TARGET_COLOR_GREEN
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("targetColorType", value)
            }
        }

    var reportAuthorName: String
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getString("reportAuthorName", CommUtils.getAppName()) else CommUtils.getAppName()
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("reportAuthorName", value)
            }
        }

    var reportWatermarkText: String
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getString("reportWatermarkText", CommUtils.getAppName()) else CommUtils.getAppName()
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("reportWatermarkText", value)
            }
        }

    var reportHumidity: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("reportHumidity", 500) else 500
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("reportHumidity", value)
            }
        }
}