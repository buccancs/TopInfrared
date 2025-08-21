package com.infisense.usbir.extension

import com.energy.iruvc.ircmd.IRCMD
import com.energy.iruvc.utils.CommonParams
import com.energy.iruvc.utils.CommonParams.PropAutoShutterParameter
import com.energy.iruvc.utils.CommonParams.PropAutoShutterParameterValue.StatusSwith
import com.energy.iruvc.utils.CommonParams.PropImageParams
import com.energy.iruvc.utils.CommonParams.PropImageParamsValue
import com.energy.iruvc.utils.CommonParams.PropImageParamsValue.DDEType
import com.energy.iruvc.utils.CommonParams.PropImageParamsValue.MirrorFlipType

/**
 * 设置自动快门开启或关闭
 * @param isAutoShutter true-开启自动快门 false-关闭自动快门
 */
fun IRCMD.setAutoShutter(isAutoShutter: Boolean) {
    setPropAutoShutterParameter(
        PropAutoShutterParameter.SHUTTER_PROP_SWITCH,
        if (isAutoShutter) StatusSwith.ON else StatusSwith.OFF
    )
}

/**
 * 设置镜像开启或关闭
 * @param isMirror true-开启 false-关闭
 */
fun IRCMD.setMirror(isMirror: Boolean) {
    setPropImageParams(
        PropImageParams.IMAGE_PROP_SEL_MIRROR_FLIP,
        if (isMirror) MirrorFlipType.ONLY_FLIP else MirrorFlipType.NO_MIRROR_FLIP
    )
}

/**
 * 设置对比度
 * @param value 取值范围 `[0, 255]`
 */
fun IRCMD.setContrast(value: Int) {
    setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_CONTRAST, PropImageParamsValue.NumberType(value.toString()))
}

/**
 * 设置锐度（细节）
 * @param level 取值范围 `[0,4]`
 */
fun IRCMD.setPropDdeLevel(level: Int) {
    when (level) {
        0 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_0)
        1 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_1)
        2 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_2)
        3 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_3)
        4 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_4)
    }
}