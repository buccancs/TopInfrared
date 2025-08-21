package com.topdon.lib.core.common

import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.SizeUtils
import com.google.gson.Gson
import com.topdon.lib.core.bean.AlarmBean
import com.topdon.lib.core.bean.CameraItemBean
import com.topdon.lib.core.bean.ObserveBean
import com.topdon.lib.core.config.DeviceConfig
import com.topdon.lib.core.utils.CommUtils

/**
 * 保存设置开关牵扯面太广，分布太乱，统一封装使用.
 *
 * 当前类封装受“保存设置开关”影响的配置项，
 *
 * [SharedManager] 保存不受“保存设置开关”影响的配置项.
 */
object SaveSettingUtil {
    /**
     * 保存设置开关使用的 SharedPreferences 名称.
     */
    private const val SP_NAME = "SaveSettingUtil"

    /**
     * 双光1
     */
    const val FusionTypeLPYFusion = 4
    /**
     * 双光2
     */
    const val FusionTypeMeanFusion = 2
    /**
     * 单红外
     */
    const val FusionTypeIROnly = 1 //单独红外
    /**
     * 单可见光
     */
    const val FusionTypeVLOnly = 0 //单独可见光
    /**
     * 画中画
     */
    const val FusionTypeTC007Fusion = 7 //tc007的画中画

    const val FusionTypeHSLFusion = 3
    const val FusionTypeScreenFusion = 5
    const val FusionTypeIROnlyNoFusion = 6


    /**
     * 保存设置开关关闭时，要将所有影响的配置项重置为默认项.
     */
    fun reset() {
        //热成像测温观测模式共有
        isMeasureTempMode = true
        isVideoMode = false
        isAutoShutter = true
        isRecordAudio = false
        isOpenMirror = false
        delayCaptureSecond = 0
        contrastValue = 128
        pseudoColorMode = 3
        rotateAngle = DeviceConfig.S_ROTATE_ANGLE

        //测温模式独有
        isOpenPseudoBar = true
        isOpenTwoLight = false
        twoLightAlpha = 50
        ddeConfig = 2
        tempTextColor = 0xffffffff.toInt()
        temperatureMode = CameraItemBean.TYPE_TMP_C
        alarmBean = AlarmBean()

        //观测模式独有
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
        isOpenAmplify = false
    }



    /**
     * 是否开启保存设置开关，默认关闭.
     */
    var isSaveSetting: Boolean
        get() = SPUtils.getInstance(SP_NAME).getBoolean("isSaveSetting", true)
        set(value) {
            SPUtils.getInstance(SP_NAME).put("isSaveSetting", value)
        }



    /**
     * 热成像是否处于测温模式，默认测温模式 true-测温 false-观测
     */
    var isMeasureTempMode: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getBoolean("isMeasureTempMode", true) else true
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isMeasureTempMode", value)
            }
        }
    /**
     * 是否开启超分
     */
    var isOpenAmplify : Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getBoolean("isOpenAmplify", false) else false
        set(value) {
            SPUtils.getInstance(SP_NAME).put("isOpenAmplify", value)
        }


    /**
     * 热成像是否选择录像模式，默认拍照 true-录像 false-拍照
     */
    var isVideoMode: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isVideoMode", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isVideoMode", value)
            }
        }
    /**
     * 热成像是否打开自动快门，默认打开 true-打开 false-关闭
     */
    var isAutoShutter: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isAutoShutter", true) else true
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isAutoShutter", value)
            }
        }
    /**
     * 热成像录像是否同时使用麦克风录制音频，默认关闭 true-开启 false-关闭
     */
    var isRecordAudio: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isRecordAudio", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isRecordAudio", value)
            }
        }
    /**
     * 延迟拍照或延时录制的延时秒数，单位秒，默认0秒即不延迟.
     */
    var delayCaptureSecond: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("delayCaptureSecond", 0) else 0
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("delayCaptureSecond", value)
            }
        }


    var fusionType : Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("fusionType", FusionTypeLPYFusion) else FusionTypeLPYFusion
        set(value) {
            SPUtils.getInstance(SP_NAME).put("fusionType", value)
        }
    /**
     * 热成像-测温模式-是否开启双光，默认关闭 true-开启 false-关闭
     */
    var isOpenTwoLight: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getBoolean("isOpenTwoLight", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenTwoLight", value)
            }
        }
    /**
     * 热成像-测温模式-双光开启时融合度，取值`[0,100]`，0表示完全不透明，100表示完全透明，默认 50%
     */
    var twoLightAlpha: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("twoLightAlpha", 50) else 50
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("twoLightAlpha", value)
            }
        }


    /**
     * 热成像伪彩模式，取值为伪彩枚举值，默认铁红
     */
    var pseudoColorMode: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("pseudoColorMode", 3) else 3
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("pseudoColorMode", value)
            }
        }


    /**
     * 热成像-测温模式-是否开启伪彩条，默认开启 true-开启 false-关闭
     */
    var isOpenPseudoBar: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenPseudoBar", true) else true
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenPseudoBar", value)
            }
        }
    /**
     * 热成像对比度，取值范围`[0,255]`，默认 128
     */
    var contrastValue: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("contrastValue", 128) else 128
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("contrastValue", value)
            }
        }
    /**
     * 热成像-测温模式-锐度(细节增强等级)，取值范围`[0,4]`，默认为 2
     */
    var ddeConfig: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME).getInt("ddeConfig", 2) else 2
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("ddeConfig", value)
            }
        }
    /**
     *热成像-测温模式-温度报警相关设置项.
     */
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
    /**
     * 热成像画面逆时针旋转角度，取值 0、90、180、270，默认 [DeviceConfig.S_ROTATE_ANGLE]
     */
    var rotateAngle: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("rotateAngle", DeviceConfig.S_ROTATE_ANGLE) else DeviceConfig.S_ROTATE_ANGLE
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("rotateAngle", value)
            }
        }
    /**
     * 热成像是否开启镜像，默认关闭即不镜像 true-镜像 false-不镜像
     */
    var isOpenMirror: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenMirror", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenMirror", value)
            }
        }
    /**
     * 热成像-观测模式-是否开启指南针，默认关闭 true-开启 false-关闭
     */
    var isOpenCompass: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenCompass", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenCompass", value)
            }
        }
    /**
     * 热成像-测温模式-温度字体颜色值，默认白色.
     */
    var tempTextColor: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("tempTextColor", 0xffffffff.toInt()) else 0xffffffff.toInt()
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("tempTextColor", value)
            }
        }
    /**
     * 热成像-测温模式-温度字体颜色值，默认14sp.
     */
    var tempTextSize: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("tempTextSize", SizeUtils.sp2px(14f)) else SizeUtils.sp2px(14f)
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("tempTextSize", value)
            }
        }


    /**
     * 热成像-测温模式-温度档位，默认常温，取值
     *
     * 常温 ([CameraItemBean.TYPE_TMP_C] = 1）
     *
     * 高温 ([CameraItemBean.TYPE_TMP_H] = 0)
     *
     * 自动 ([CameraItemBean.TYPE_TMP_ZD] = -1)
     */
    var temperatureMode: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("temperatureMode", CameraItemBean.TYPE_TMP_C) else CameraItemBean.TYPE_TMP_C
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("temperatureMode", value)
            }
        }



    /**
     * 热成像-观测模式-是否开启高温点，默认关闭 true-开启 false-关闭
     */
    var isOpenHighPoint: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenHighPoint", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenHighPoint", value)
            }
        }
    /**
     * 热成像-观测模式-是否开启低温点，默认关闭 true-开启 false-关闭
     */
    var isOpenLowPoint: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenLowPoint", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenLowPoint", value)
            }
        }

    /**
     * 热成像-观测模式-选中AI追踪类型，默认未选中，取值
     *
     * 未选中 ([ObserveBean.TYPE_NONE] = -1)
     *
     * 动态识别 ([ObserveBean.TYPE_DYN_R] = 0)
     *
     * 高温源 ([ObserveBean.TYPE_TMP_H_S] = 1)
     *
     * 低温源 ([ObserveBean.TYPE_TMP_L_S] = 2)
     */
    var aiTraceType: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("aiTraceType", ObserveBean.TYPE_NONE) else ObserveBean.TYPE_NONE
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("aiTraceType", value)
            }
        }

    /**
     * 热成像-观测模式-标靶-是否开启标靶，默认关闭 true-开启 false-关闭
     */
    var isOpenTarget: Boolean
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getBoolean("isOpenTarget", false) else false
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("isOpenTarget", value)
            }
        }

    /**
     * 热成像-观测模式-标靶-标靶测量模式，默认人，取值
     *
     * 人 ([ObserveBean.TYPE_MEASURE_PERSON] = 10)
     *
     * 羊 ([ObserveBean.TYPE_MEASURE_SHEEP] = 11)
     *
     * 狗 ([ObserveBean.TYPE_MEASURE_DOG] = 12)
     *
     * 鸟 ([ObserveBean.TYPE_MEASURE_BIRD] = 13)
     */
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

    /**
     * 热成像-观测模式-标靶-标靶类型，默认横向，取值
     *
     * 横向 ([ObserveBean.TYPE_TARGET_HORIZONTAL] = 15)
     *
     * 竖向 ([ObserveBean.TYPE_TARGET_VERTICAL] = 16)
     *
     * 圆形 ([ObserveBean.TYPE_TARGET_CIRCLE] = 17)
     */
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

    /**
     * 热成像-观测模式-标靶-标靶颜色，默认绿色，取值
     *
     * 绿色 ([ObserveBean.TYPE_TARGET_COLOR_GREEN] = 20)
     *
     * 红色 ([ObserveBean.TYPE_TARGET_COLOR_RED] = 21)
     *
     * 蓝色 ([ObserveBean.TYPE_TARGET_COLOR_BLUE] = 22)
     *
     * 黑色 ([ObserveBean.TYPE_TARGET_COLOR_BLACK] = 23)
     *
     * 白色 ([ObserveBean.TYPE_TARGET_COLOR_WHITE] = 24)
     */
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


    /**
     * 报告-作者名称，默认值 App 名称.
     */
    var reportAuthorName: String
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getString("reportAuthorName", CommUtils.getAppName()) else CommUtils.getAppName()
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("reportAuthorName", value)
            }
        }

    /**
     * 报告-水印内容，默认值 App 名称.
     */
    var reportWatermarkText: String
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getString("reportWatermarkText", CommUtils.getAppName()) else CommUtils.getAppName()
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("reportWatermarkText", value)
            }
        }

    /**
     * 报告-环境湿度千分比，默认值500，取值`[0, 1000]`
     */
    var reportHumidity: Int
        get() = if (isSaveSetting) SPUtils.getInstance(SP_NAME)
            .getInt("reportHumidity", 500) else 500
        set(value) {
            if (isSaveSetting) {
                SPUtils.getInstance(SP_NAME).put("reportHumidity", value)
            }
        }
}