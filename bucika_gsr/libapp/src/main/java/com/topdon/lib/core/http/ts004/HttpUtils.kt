package com.topdon.lib.core.http.ts004
import com.topdon.lms.sdk.xutils.common.Callback
import com.topdon.lms.sdk.xutils.http.RequestParams
import com.topdon.lms.sdk.xutils.x
object HttpUtils {
    /**
     * 设置伪彩样式
     * @param mode              伪彩样式
     * @param iResponseCallback 回调函数
     * @ void
     */
    fun setPseudoColor(mode: Int, iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.addBodyParameter("enable", false)
        params.addBodyParameter("mode", mode)
        params.uri = TS004URL.SET_PSEUDO_COLOR
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 获取伪彩样式
     */
    fun getPseudoColor(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.GET_PSEUDO_COLOR
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 设置屏幕亮度
     * @param mode              屏幕亮度值:范围0-100
     * @param iResponseCallback 回调函数
     * @ void
     */
    fun setBrightness(brightness: Int, iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.addBodyParameter("brightness", brightness)
        params.uri = TS004URL.SET_PANEL_PARAM
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 获取屏幕亮度
     */
    fun getBrightness(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.GET_PANEL_PARAM
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 设置画中画
     * @param iResponseCallback 回调函数
     * @ void
     */
    fun setPip(enable: Boolean,iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.addBodyParameter("enable", enable)
        params.uri = TS004URL.SET_PIP
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 获取画中画
     */
    fun getPip(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.GET_PIP
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 设置放大倍数
     * @param factor            放大倍数:1,2,4,8
     * @param iResponseCallback 回调函数
     * @ void
     */
    fun setZoom(factor: Int, iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.addBodyParameter("enable", true)
        params.addBodyParameter("factor", factor)
        params.uri = TS004URL.SET_ZOOM
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 获取放大倍数
     */
    fun getZoom(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.GET_ZOOM
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 设置拍照
     * @param iResponseCallback 回调函数
     * @void
     */
    fun setCamera(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.SET_SNAPSHOT
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 设置录像
     * @param enable 录制开关
     * @param iResponseCallback 回调函数
     * @void
     */
    fun setVideo(enable: Boolean,iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.addBodyParameter("enable", enable)
        params.uri = TS004URL.GET_VRECORD
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 获取录像状态
     * @param iResponseCallback 回调函数
     * @void
     */
    fun getVideoStatus(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.GET_RECORD_STATUS
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 获取版本信息
     */
    fun getVersion(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.GET_VERSION
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 获取设备信息
     */
    fun getDeviceDetails(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.GET_DEVICE_DETAILS
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 获取存储分区信息
     */
    fun getFreeSpace(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.GET_FREE_SPACE
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }

    /**
     * 恢复出厂设置
     */
    fun getResetAll(iResponseCallback: Callback.CommonCallback<String>?) {
        val params = RequestParams()
        params.uri = TS004URL.GET_RESET_ALL
        params.isAsJsonContent = true
        x.http().post(params,iResponseCallback!!)
    }
}