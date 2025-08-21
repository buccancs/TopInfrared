package com.infisense.usbir.inf

import com.energy.iruvc.dual.DualUVCCamera
import com.energy.iruvc.utils.DualCameraParams


/**
 * 统一管理双光的特殊接口，区别于单光
 * @author: CaiSongL
 * @date: 2024/1/10 11:40
 */
@Deprecated("未使用，好像没什么用")
interface IDualListener {


    fun setDualUVCCamera(dualUVCCamera : DualUVCCamera)

    fun setCurrentFusionType(currentFusionType : DualCameraParams.FusionType)

    fun setUseIRISP(useIRISP : Boolean)



}