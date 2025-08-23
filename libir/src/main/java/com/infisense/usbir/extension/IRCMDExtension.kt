package com.infisense.usbir.extension

import com.energy.iruvc.ircmd.IRCMD
import com.energy.iruvc.utils.CommonParams
import com.energy.iruvc.utils.CommonParams.PropAutoShutterParameter
import com.energy.iruvc.utils.CommonParams.PropAutoShutterParameterValue.StatusSwith
import com.energy.iruvc.utils.CommonParams.PropImageParams
import com.energy.iruvc.utils.CommonParams.PropImageParamsValue
import com.energy.iruvc.utils.CommonParams.PropImageParamsValue.DDEType
import com.energy.iruvc.utils.CommonParams.PropImageParamsValue.MirrorFlipType

fun IRCMD.setAutoShutter(isAutoShutter: Boolean) {
    setPropAutoShutterParameter(
        PropAutoShutterParameter.SHUTTER_PROP_SWITCH,
        if (isAutoShutter) StatusSwith.ON else StatusSwith.OFF
    )
}

fun IRCMD.setMirror(isMirror: Boolean) {
    setPropImageParams(
        PropImageParams.IMAGE_PROP_SEL_MIRROR_FLIP,
        if (isMirror) MirrorFlipType.ONLY_FLIP else MirrorFlipType.NO_MIRROR_FLIP
    )
}

fun IRCMD.setContrast(value: Int) {
    setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_CONTRAST, PropImageParamsValue.NumberType(value.toString()))
}

fun IRCMD.setPropDdeLevel(level: Int) {
    when (level) {
        0 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_0)
        1 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_1)
        2 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_2)
        3 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_3)
        4 -> setPropImageParams(PropImageParams.IMAGE_PROP_LEVEL_DDE, DDEType.DDE_4)
    }
}