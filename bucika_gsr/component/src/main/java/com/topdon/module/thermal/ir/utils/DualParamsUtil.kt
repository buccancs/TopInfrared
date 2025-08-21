package com.topdon.module.thermal.ir.utils

import com.energy.iruvc.utils.DualCameraParams
import com.topdon.lib.core.common.SaveSettingUtil

/**
author: CaiSongL
date: 2024/2/1 17:59
des:艾睿枚举参数转换工具
 **/
object DualParamsUtil {


    fun wifiFusionTypeToParams(fusionType: Int) : Int{
        return when(fusionType){
            SaveSettingUtil.FusionTypeVLOnly -> 3
            SaveSettingUtil.FusionTypeIROnlyNoFusion -> 1
            SaveSettingUtil.FusionTypeMeanFusion-> 4
            SaveSettingUtil.FusionTypeIROnly -> 0
            else -> 3
        }
    }


    fun fusionTypeToParams(fusionType: Int) : DualCameraParams.FusionType {
        return when(fusionType){
            SaveSettingUtil.FusionTypeVLOnly -> DualCameraParams.FusionType.VLOnly
            SaveSettingUtil.FusionTypeIROnlyNoFusion -> DualCameraParams.FusionType.IROnlyNoFusion
            SaveSettingUtil.FusionTypeScreenFusion -> DualCameraParams.FusionType.ScreenFusion
            SaveSettingUtil.FusionTypeHSLFusion -> DualCameraParams.FusionType.HSLFusion
            SaveSettingUtil.FusionTypeMeanFusion-> DualCameraParams.FusionType.MeanFusion
            SaveSettingUtil.FusionTypeLPYFusion -> DualCameraParams.FusionType.LPYFusion
            SaveSettingUtil.FusionTypeIROnly -> DualCameraParams.FusionType.IROnly
            else -> DualCameraParams.FusionType.LPYFusion
        }
    }
    fun paramsToFusionType(fusionTypeP: DualCameraParams.FusionType) : Int{
        return when(fusionTypeP){
            DualCameraParams.FusionType.VLOnly  -> SaveSettingUtil.FusionTypeVLOnly
            DualCameraParams.FusionType.IROnlyNoFusion -> SaveSettingUtil.FusionTypeIROnlyNoFusion
            DualCameraParams.FusionType.ScreenFusion -> SaveSettingUtil.FusionTypeScreenFusion
            DualCameraParams.FusionType.HSLFusion -> SaveSettingUtil.FusionTypeHSLFusion
            DualCameraParams.FusionType.MeanFusion -> SaveSettingUtil.FusionTypeMeanFusion
            DualCameraParams.FusionType.LPYFusion -> SaveSettingUtil.FusionTypeLPYFusion
            DualCameraParams.FusionType.IROnly -> SaveSettingUtil.FusionTypeIROnly
            else -> SaveSettingUtil.FusionTypeLPYFusion
        }
    }


}