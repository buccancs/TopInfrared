package com.topdon.thermal07.util

import com.topdon.lib.core.bean.CameraItemBean

object WifiAttributeChangeU {

    fun getTemperatureModeToWifi(temperatureMode : Int) : Int{
        return when(temperatureMode){
            CameraItemBean.TYPE_TMP_ZD -> {
                3
            }
            CameraItemBean.TYPE_TMP_C -> {
                0
            }else ->{
                1
            }
        }
    }

    fun getTemperatureModeByWifi(wifiTemperatureMode : Int) : Int{
        return when(wifiTemperatureMode){
            3 -> {
                CameraItemBean.TYPE_TMP_ZD
            }
            0 -> {
                CameraItemBean.TYPE_TMP_C
            }else ->{
                CameraItemBean.TYPE_TMP_H
            }
        }
    }

}