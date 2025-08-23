package com.topdon.libcom.bean

import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.gson.Gson
import com.topdon.lib.core.bean.AlarmBean
import com.topdon.lib.core.bean.CameraItemBean
import com.topdon.lib.core.bean.ObserveBean
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.config.DeviceConfig
import com.topdon.lib.core.utils.CommUtils

class SaveSettingBean(private val isWifi: Boolean = false) {

    private fun getSPUtils(): SPUtils = SPUtils.getInstance(if (isWifi) "WifiSaveSettingUtil" else "SaveSettingUtil")

    var isSaveSetting: Boolean = getSPUtils().getBoolean("isSaveSetting", true)
        set(value) {
            field = value
            getSPUtils().put("isSaveSetting", value)
        }

    var isMeasureTempMode: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isMeasureTempMode", true) else true
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isMeasureTempMode", value)
            }
        }
    
    var isOpenAmplify : Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenAmplify", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenAmplify", value)
            }
        }

    var isVideoMode: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isVideoMode", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isVideoMode", value)
            }
        }
    
    var isAutoShutter: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isAutoShutter", true) else true
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isAutoShutter", value)
            }
        }
    
    var isRecordAudio: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isRecordAudio", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isRecordAudio", value)
            }
        }
    
    var delayCaptureSecond: Int = if (isSaveSetting) getSPUtils().getInt("delayCaptureSecond", 0) else 0
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("delayCaptureSecond", value)
            }
        }

    var fusionType : Int = if (isSaveSetting) getSPUtils().getInt("fusionType", SaveSettingUtil.FusionTypeLPYFusion) else SaveSettingUtil.FusionTypeLPYFusion
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("fusionType", value)
            }
        }
    
    var isOpenTwoLight: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenTwoLight", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenTwoLight", value)
            }
        }
    
    var twoLightAlpha: Int = if (isSaveSetting) getSPUtils().getInt("twoLightAlpha", 50) else 50
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("twoLightAlpha", value)
            }
        }

    var pseudoColorMode: Int = if (isSaveSetting) getSPUtils().getInt("pseudoColorMode", 3) else 3
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("pseudoColorMode", value)
            }
        }

    var isOpenPseudoBar: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenPseudoBar", true) else true
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenPseudoBar", value)
            }
        }
    
    var contrastValue: Int = if (isSaveSetting) getSPUtils().getInt("contrastValue", 128) else 128
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("contrastValue", value)
            }
        }
    
    var ddeConfig: Int = if (isSaveSetting) getSPUtils().getInt("ddeConfig", 2) else 2
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("ddeConfig", value)
            }
        }
    
    var alarmBean: AlarmBean = if (isSaveSetting) {
        val json = getSPUtils().getString("alarmBean", "")
        if (json.isNullOrEmpty()) AlarmBean() else Gson().fromJson(json, AlarmBean::class.java)
    } else {
        AlarmBean()
    }
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("alarmBean", Gson().toJson(value))
            }
        }
    
    var rotateAngle: Int = if (isSaveSetting) getSPUtils().getInt("rotateAngle", DeviceConfig.S_ROTATE_ANGLE) else DeviceConfig.S_ROTATE_ANGLE
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("rotateAngle", value)
            }
        }
    
    fun isRotatePortrait(): Boolean = rotateAngle == 90 || rotateAngle == 270

    var isOpenMirror: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenMirror", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenMirror", value)
            }
        }
    
    var isOpenCompass: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenCompass", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenCompass", value)
            }
        }
    
    var tempTextColor: Int = if (isSaveSetting) getSPUtils().getInt("tempTextColor", 0xffffffff.toInt()) else 0xffffffff.toInt()
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("tempTextColor", value)
            }
        }
    
    var tempTextSize: Int = if (isSaveSetting) getSPUtils().getInt("tempTextSize", SizeUtils.sp2px(14f)) else SizeUtils.sp2px(14f)
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("tempTextSize", value)
            }
        }
    
    fun isTempTextDefault(): Boolean = tempTextColor == 0xffffffff.toInt() && tempTextSize == SizeUtils.sp2px(14f)

    var temperatureMode: Int = if (isSaveSetting) getSPUtils().getInt("temperatureMode", CameraItemBean.TYPE_TMP_C) else CameraItemBean.TYPE_TMP_C
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("temperatureMode", value)
            }
        }

    var isOpenHighPoint: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenHighPoint", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenHighPoint", value)
            }
        }
    
    var isOpenLowPoint: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenLowPoint", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenLowPoint", value)
            }
        }

    var aiTraceType: Int = if (isSaveSetting) getSPUtils().getInt("aiTraceType", ObserveBean.TYPE_NONE) else ObserveBean.TYPE_NONE
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("aiTraceType", value)
            }
        }

    var isOpenTarget: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenTarget", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenTarget", value)
            }
        }

    var targetMeasureMode: Int = if (isSaveSetting) getSPUtils().getInt("targetMeasureMode", ObserveBean.TYPE_MEASURE_PERSON) else ObserveBean.TYPE_MEASURE_PERSON
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("targetMeasureMode", value)
            }
        }

    var targetType: Int = if (isSaveSetting) getSPUtils().getInt("targetType", ObserveBean.TYPE_TARGET_HORIZONTAL) else ObserveBean.TYPE_TARGET_HORIZONTAL
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("targetType", value)
            }
        }

    var targetColorType: Int = if (isSaveSetting) getSPUtils().getInt("targetColorType", ObserveBean.TYPE_TARGET_COLOR_GREEN) else ObserveBean.TYPE_TARGET_COLOR_GREEN
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("targetColorType", value)
            }
        }

    var reportAuthorName: String = if (isSaveSetting) getSPUtils().getString("reportAuthorName", CommUtils.getAppName()) else CommUtils.getAppName()
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("reportAuthorName", value)
            }
        }

    var reportWatermarkText: String = if (isSaveSetting) getSPUtils().getString("reportWatermarkText", CommUtils.getAppName()) else CommUtils.getAppName()
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("reportWatermarkText", value)
            }
        }

    var reportHumidity: Int = if (isSaveSetting) getSPUtils().getInt("reportHumidity", 500) else 500
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("reportHumidity", value)
            }
        }
}