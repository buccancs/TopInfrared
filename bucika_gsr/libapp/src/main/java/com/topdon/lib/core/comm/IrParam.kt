package com.topdon.lib.core.comm

/**
 * des:
 * author: CaiSongL
 * date: 2024/4/30 10:16
 **/
enum class IrParam {

    ParamLevel,//对比度
    ParamAlarm,//预警
    ParamSharpness,//锐度
    ParamTempFont,//温度值字体设置
    ParamRotate,//旋转
    ParamColor,//伪彩
    ParamMirror,//镜像
    ParamCompass,//指南针
    ParamPColor,//伪彩样式
    ParamTemperature,//温度模式、高低增益
}

data class TempFont(val textSize : Int,val textColor : Int)
