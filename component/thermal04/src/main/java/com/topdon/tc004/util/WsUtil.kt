package com.topdon.tc004.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.topdon.lib.core.repository.WsResponse
import com.topdon.tc004.bean.MenuBean

object WsUtil {
    fun getWebSocketPseudo(mode : Int) : Int{
        var wsMode :Int = MenuBean.TYPE_MIX
        when (mode) {
            MenuBean.TYPE_WHITE_HOT -> {
                wsMode = MenuBean.TYPE_BLACK_HOT
            }
            MenuBean.TYPE_BLACK_HOT -> {
                wsMode = MenuBean.TYPE_RED_HOT
            }
            MenuBean.TYPE_RED_HOT -> {
                wsMode = MenuBean.TYPE_MIX
            }
            MenuBean.TYPE_MIX -> {
                wsMode = MenuBean.TYPE_BIRD
            }
            MenuBean.TYPE_BIRD -> {
                wsMode = MenuBean.TYPE_WHITE_HOT
            }
        }
        return wsMode
    }

    fun getBrightness(brightness : Int) : Int{
        var wsLight :Int = MenuBean.TYPE_LIGHT_MIDDLE
        when (brightness) {
            in 81..100 -> {
                wsLight = MenuBean.TYPE_LIGHT_MIDDLE
            }
            in 61..80 -> {
                wsLight = MenuBean.TYPE_LIGHT_LOW
            }
            in 0..60 -> {
                wsLight = MenuBean.TYPE_LIGHT_HIGH
            }
        }
        return wsLight
    }

    inline fun <reified T> getWsResponse(msgJson: String): T? {
        return try {
            val typeToken = TypeToken.getParameterized(WsResponse::class.java, T::class.java).type
            Gson().fromJson<WsResponse<T>>(msgJson, typeToken).data
        } catch (e: java.lang.Exception) {
            null
        }
    }
}