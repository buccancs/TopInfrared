package com.topdon.lib.core.bean

/**
 * 连续拍照配置.
 * @param isOpen 是否开启
 * @param continuaTime 连续拍照时间间隔，单位毫秒
 * @param count 连续拍照数量
 */
data class ContinuousBean(var isOpen: Boolean = false, var continuaTime: Long = 1000, var count: Int = 3)



class ObserveBean {
    companion object {
        //动态追踪
        const val TYPE_NONE = -1 //不开启动态识别
        const val TYPE_DYN_R = 0 //动态识别
        const val TYPE_TMP_H_S = 1 //高温源
        const val TYPE_TMP_L_S = 2 //低温源

        const val TYPE_MEASURE_PERSON = 10 //人
        const val TYPE_MEASURE_SHEEP = 11 //羊
        const val TYPE_MEASURE_DOG = 12 //狗
        const val TYPE_MEASURE_BIRD = 13 //鸟

        const val TYPE_TARGET_HORIZONTAL = 15 //横向
        const val TYPE_TARGET_VERTICAL = 16 //竖向
        const val TYPE_TARGET_CIRCLE = 17 //圆形

        const val TYPE_TARGET_COLOR_GREEN = 20 //绿色
        const val TYPE_TARGET_COLOR_RED = 21 //红色
        const val TYPE_TARGET_COLOR_BLUE = 22 //蓝色
        const val TYPE_TARGET_COLOR_BLACK = 23 //黑色
        const val TYPE_TARGET_COLOR_WHITE = 24 //白色
    }
}

data class CameraItemBean(
    var name: String = "延迟",
    var type: Int = 0,
    var time: Int = DELAY_TIME_0,
    var isSel: Boolean = false,
) {

    fun changeDelayType() {
        if (type == TYPE_DELAY) {
            when (time) {
                DELAY_TIME_0 -> {
                    time = DELAY_TIME_3
                }

                DELAY_TIME_3 -> {
                    time = DELAY_TIME_6
                }

                DELAY_TIME_6 -> {
                    time = DELAY_TIME_0
                }
            }
        }
    }


    companion object {
        const val TYPE_DELAY = 0
        const val TYPE_ZDKM = 1
        const val TYPE_SDKM = 2
        const val TYPE_AUDIO = 3
        const val TYPE_SETTING = 4

        const val DELAY_TIME_0 = 0//延迟3秒
        const val DELAY_TIME_3 = 3//延迟3秒
        const val DELAY_TIME_6 = 6//延迟6秒

        //温度模式
        const val TYPE_TMP_ZD = -1 //自动识别模式
        const val TYPE_TMP_C = 1 // 常温模式
        const val TYPE_TMP_H = 0 //高温模式

    }

}