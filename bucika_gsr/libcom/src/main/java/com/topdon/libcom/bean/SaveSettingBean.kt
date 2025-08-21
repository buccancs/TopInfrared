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

/**
 * 保存设置开关开启时倒还好，读写都可以用 SharedPreferences 保存；
 *
 * 但若保存设置开关关闭时，那一堆的配置每个都需要一个变量来保存当前的更改，
 * 结果就是 Activity 里一大堆的变量。
 *
 * 这个类的想法就是把保存设置开关相关的变量都扔进里面，从这里读写。
 *
 * Created by LCG on 2024/12/24.
 */
class SaveSettingBean(private val isWifi: Boolean = false) {

    /**
     * 获取 SPUtil 单例.
     */
    private fun getSPUtils(): SPUtils = SPUtils.getInstance(if (isWifi) "WifiSaveSettingUtil" else "SaveSettingUtil")




    /**
     * 是否开启保存设置开关，默认关闭.
     */
    var isSaveSetting: Boolean = getSPUtils().getBoolean("isSaveSetting", true)
        set(value) {
            field = value
            getSPUtils().put("isSaveSetting", value)
        }



    /**
     * 热成像是否处于测温模式，默认测温模式 true-测温 false-观测
     */
    var isMeasureTempMode: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isMeasureTempMode", true) else true
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isMeasureTempMode", value)
            }
        }
    /**
     * 是否开启超分
     */
    var isOpenAmplify : Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenAmplify", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenAmplify", value)
            }
        }


    /**
     * 热成像是否选择录像模式，默认拍照 true-录像 false-拍照
     */
    var isVideoMode: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isVideoMode", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isVideoMode", value)
            }
        }
    /**
     * 热成像是否打开自动快门，默认打开 true-打开 false-关闭
     */
    var isAutoShutter: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isAutoShutter", true) else true
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isAutoShutter", value)
            }
        }
    /**
     * 热成像录像是否同时使用麦克风录制音频，默认关闭 true-开启 false-关闭
     */
    var isRecordAudio: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isRecordAudio", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isRecordAudio", value)
            }
        }
    /**
     * 延迟拍照或延时录制的延时秒数，单位秒，默认0秒即不延迟.
     */
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
    /**
     * 热成像-测温模式-是否开启双光，默认关闭 true-开启 false-关闭
     */
    var isOpenTwoLight: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenTwoLight", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenTwoLight", value)
            }
        }
    /**
     * 热成像-测温模式-双光开启时融合度，取值`[0,100]`，0表示完全不透明，100表示完全透明，默认 50%
     */
    var twoLightAlpha: Int = if (isSaveSetting) getSPUtils().getInt("twoLightAlpha", 50) else 50
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("twoLightAlpha", value)
            }
        }


    /**
     * 热成像伪彩模式，取值为伪彩枚举值，默认铁红
     */
    var pseudoColorMode: Int = if (isSaveSetting) getSPUtils().getInt("pseudoColorMode", 3) else 3
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("pseudoColorMode", value)
            }
        }


    /**
     * 热成像-测温模式-是否开启伪彩条，默认开启 true-开启 false-关闭
     */
    var isOpenPseudoBar: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenPseudoBar", true) else true
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenPseudoBar", value)
            }
        }
    /**
     * 热成像对比度，取值范围`[0,255]`，默认 128
     */
    var contrastValue: Int = if (isSaveSetting) getSPUtils().getInt("contrastValue", 128) else 128
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("contrastValue", value)
            }
        }
    /**
     * 热成像-测温模式-锐度(细节增强等级)，取值范围`[0,4]`，默认为 2
     */
    var ddeConfig: Int = if (isSaveSetting) getSPUtils().getInt("ddeConfig", 2) else 2
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("ddeConfig", value)
            }
        }
    /**
     *热成像-测温模式-温度报警相关设置项.
     */
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
    /**
     * 热成像画面逆时针旋转角度，取值 0、90、180、270，默认 [DeviceConfig.S_ROTATE_ANGLE]
     */
    var rotateAngle: Int = if (isSaveSetting) getSPUtils().getInt("rotateAngle", DeviceConfig.S_ROTATE_ANGLE) else DeviceConfig.S_ROTATE_ANGLE
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("rotateAngle", value)
            }
        }
    /**
     * 热成像画面是否为竖屏尺寸(192x256)
     */
    fun isRotatePortrait(): Boolean = rotateAngle == 90 || rotateAngle == 270

    /**
     * 热成像是否开启镜像，默认关闭即不镜像 true-镜像 false-不镜像
     */
    var isOpenMirror: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenMirror", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenMirror", value)
            }
        }
    /**
     * 热成像-观测模式-是否开启指南针，默认关闭 true-开启 false-关闭
     */
    var isOpenCompass: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenCompass", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenCompass", value)
            }
        }
    /**
     * 热成像-测温模式-温度字体颜色值，默认白色.
     */
    var tempTextColor: Int = if (isSaveSetting) getSPUtils().getInt("tempTextColor", 0xffffffff.toInt()) else 0xffffffff.toInt()
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("tempTextColor", value)
            }
        }
    /**
     * 热成像-测温模式-温度字体大小，单位 px，默认14sp.
     */
    var tempTextSize: Int = if (isSaveSetting) getSPUtils().getInt("tempTextSize", SizeUtils.sp2px(14f)) else SizeUtils.sp2px(14f)
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("tempTextSize", value)
            }
        }
    /**
     * 判断当前温度字体颜色及大小是否为默认设置
     */
    fun isTempTextDefault(): Boolean = tempTextColor == 0xffffffff.toInt() && tempTextSize == SizeUtils.sp2px(14f)


    /**
     * 热成像-测温模式-温度档位，默认常温，取值
     *
     * 常温 ([CameraItemBean.TYPE_TMP_C] = 1）
     *
     * 高温 ([CameraItemBean.TYPE_TMP_H] = 0)
     *
     * 自动 ([CameraItemBean.TYPE_TMP_ZD] = -1)
     */
    var temperatureMode: Int = if (isSaveSetting) getSPUtils().getInt("temperatureMode", CameraItemBean.TYPE_TMP_C) else CameraItemBean.TYPE_TMP_C
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("temperatureMode", value)
            }
        }



    /**
     * 热成像-观测模式-是否开启高温点，默认关闭 true-开启 false-关闭
     */
    var isOpenHighPoint: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenHighPoint", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenHighPoint", value)
            }
        }
    /**
     * 热成像-观测模式-是否开启低温点，默认关闭 true-开启 false-关闭
     */
    var isOpenLowPoint: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenLowPoint", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenLowPoint", value)
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
    var aiTraceType: Int = if (isSaveSetting) getSPUtils().getInt("aiTraceType", ObserveBean.TYPE_NONE) else ObserveBean.TYPE_NONE
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("aiTraceType", value)
            }
        }

    /**
     * 热成像-观测模式-标靶-是否开启标靶，默认关闭 true-开启 false-关闭
     */
    var isOpenTarget: Boolean = if (isSaveSetting) getSPUtils().getBoolean("isOpenTarget", false) else false
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("isOpenTarget", value)
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
    var targetMeasureMode: Int = if (isSaveSetting) getSPUtils().getInt("targetMeasureMode", ObserveBean.TYPE_MEASURE_PERSON) else ObserveBean.TYPE_MEASURE_PERSON
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("targetMeasureMode", value)
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
    var targetType: Int = if (isSaveSetting) getSPUtils().getInt("targetType", ObserveBean.TYPE_TARGET_HORIZONTAL) else ObserveBean.TYPE_TARGET_HORIZONTAL
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("targetType", value)
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
    var targetColorType: Int = if (isSaveSetting) getSPUtils().getInt("targetColorType", ObserveBean.TYPE_TARGET_COLOR_GREEN) else ObserveBean.TYPE_TARGET_COLOR_GREEN
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("targetColorType", value)
            }
        }


    /**
     * 报告-作者名称，默认值 App 名称.
     */
    var reportAuthorName: String = if (isSaveSetting) getSPUtils().getString("reportAuthorName", CommUtils.getAppName()) else CommUtils.getAppName()
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("reportAuthorName", value)
            }
        }

    /**
     * 报告-水印内容，默认值 App 名称.
     */
    var reportWatermarkText: String = if (isSaveSetting) getSPUtils().getString("reportWatermarkText", CommUtils.getAppName()) else CommUtils.getAppName()
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("reportWatermarkText", value)
            }
        }

    /**
     * 报告-环境湿度千分比，默认值500，取值`[0, 1000]`
     */
    var reportHumidity: Int = if (isSaveSetting) getSPUtils().getInt("reportHumidity", 500) else 500
        set(value) {
            field = value
            if (isSaveSetting) {
                getSPUtils().put("reportHumidity", value)
            }
        }
}