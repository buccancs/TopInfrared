package com.infisense.usbir.inf

import com.energy.iruvc.dual.DualUVCCamera
import com.energy.iruvc.utils.DualCameraParams

interface ILiteListener {

    fun getDeltaNucAndVTemp() : Float

    fun compensateTemp(temp : Float) : Float

}