package com.topdon.lib.core.common

import android.content.Context
import android.preference.PreferenceManager
import android.util.Base64
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.google.gson.Gson
import com.topdon.lib.core.bean.CarDetectBean
import com.topdon.lib.core.bean.CarDetectChildBean
import com.topdon.lib.core.bean.ContinuousBean
import com.topdon.lib.core.bean.WatermarkBean
import com.topdon.lib.core.dialog.CarDetectDialog

object SharedManager {
    
    var hasClickWinter: Boolean
        get() = SPUtils.getInstance().getBoolean("hasClickWinter", false)
        set(value) = SPUtils.getInstance().put("hasClickWinter", value)

    var isNeedShowTrendTips: Boolean
        get() = SPUtils.getInstance().getBoolean("isNeedShowTrendTips", true)
        set(value) = SPUtils.getInstance().put("isNeedShowTrendTips", value)

    var houseSpaceUnit: Int
        get() = SPUtils.getInstance().getInt("houseSpaceUnit", 0)
        set(value) {
            SPUtils.getInstance().put("houseSpaceUnit", value)
        }

    var costUnit: Int
        get() = SPUtils.getInstance().getInt("costUnit", 0)
        set(value) {
            SPUtils.getInstance().put("costUnit", value)
        }

    var hasTcLine: Boolean
        get() = SPUtils.getInstance().getBoolean("hasConnectTcLine", false)
        set(value) {
            SPUtils.getInstance().put("hasConnectTcLine", value)
        }

    var hasTS004: Boolean
        get() = SPUtils.getInstance().getBoolean("hasConnectTS004", false)
        set(value) {
            SPUtils.getInstance().put("hasConnectTS004", value)
        }

    var hasTC007: Boolean
        get() = SPUtils.getInstance().getBoolean("hasConnectTC007", false)
        set(value) {
            SPUtils.getInstance().put("hasConnectTC007", value)
        }

    var irConfigJsonTC007: String
        get() = SPUtils.getInstance().getString("irConfigJsonTC007")
        set(value) {
            SPUtils.getInstance().put("irConfigJsonTC007", value)
        }

    var homeGuideStep: Int
        get() {
            val value = SPUtils.getInstance().getInt("homeGuideStep", 2)
            return if (value == 1) 2 else value
        }
        set(value) {
            SPUtils.getInstance().put("homeGuideStep", value)
        }

    var configGuideStep: Int
        get() = SPUtils.getInstance().getInt("configGuideStep", 1)
        set(value) = SPUtils.getInstance().put("configGuideStep", value)

    var isHideEmissivityTips: Boolean
        get() = SPUtils.getInstance().getBoolean("isHideEmissivityTips", false)
        set(value) {
            SPUtils.getInstance().put("isHideEmissivityTips", value)
        }

    var is07HideEmissivityTips: Boolean
        get() = SPUtils.getInstance().getBoolean("is07HideEmissivityTips", false)
        set(value) {
            SPUtils.getInstance().put("is07HideEmissivityTips", value)
        }

    var is04TISR: Boolean
        get() = SPUtils.getInstance().getBoolean("is04TISR", false)
        set(value) {
            SPUtils.getInstance().put("is04TISR", value)
        }

    var is04AutoSync: Boolean
        get() = SPUtils.getInstance().getBoolean("is04AutoSync", false)
        set(value) {
            SPUtils.getInstance().put("is04AutoSync", value)
        }

    fun getManualAngle(sId: String): Int {
        return SPUtils.getInstance().getInt("manualAngle_${sId}", 1000)
    }

    fun setManualAngle(sId: String, value: Int) {
        SPUtils.getInstance().put("manualAngle_${sId}", value)
    }

    fun getManualData(sId: String): ByteArray {
        val strValue = SPUtils.getInstance().getString("manualData_${sId}")
        return if (strValue.isNullOrEmpty()) {
            byteArrayOf(
                0,
                0,
                -128,
                63,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                -128,
                63,
                0,
                0,
                0,
                0
            )
        } else {
            Base64.decode(strValue.toByteArray(), Base64.DEFAULT)
        }
    }

    fun setManualData(sId: String, value: ByteArray) {
        if (value.size == 24) {
            SPUtils.getInstance()
                .put("manualData_${sId}", String(Base64.encode(value, Base64.DEFAULT)))
        }
    }

    var isConnectAutoOpen: Boolean
        get() = SPUtils.getInstance().getBoolean("isConnectAutoOpen", false)
        set(value) {
            SPUtils.getInstance().put("isConnectAutoOpen", value)
        }

    var isConnect07AutoOpen: Boolean
        get() = SPUtils.getInstance().getBoolean("isConnect07AutoOpen", false)
        set(value) {
            SPUtils.getInstance().put("isConnect07AutoOpen", value)
        }

    var isTipOTG: Boolean
        get() = SPUtils.getInstance().getBoolean("isTipOTG", true)
        set(value) {
            SPUtils.getInstance().put("isTipOTG", value)
        }

    var isTipShutter: Boolean
        get() = SPUtils.getInstance().getBoolean("isTipShutter", true)
        set(value) {
            SPUtils.getInstance().put("isTipShutter", value)
        }

    var isTipHighTemp: Boolean
        get() = SPUtils.getInstance().getBoolean("isTipHighTemp", true)
        set(value) {
            SPUtils.getInstance().put("isTipHighTemp", value)
        }

    var isTipPinP: Boolean
        get() = SPUtils.getInstance().getBoolean("isTipPinP", true)
        set(value) {
            SPUtils.getInstance().put("isTipPinP", value)
        }

    var isTipCoordinate: Boolean
        get() = SPUtils.getInstance().getBoolean("isTipCoordinate", true)
        set(value) {
            SPUtils.getInstance().put("isTipCoordinate", value)
        }

    var isTipAIRecognition: Boolean
        get() = SPUtils.getInstance().getBoolean("isTipAIRecognition", true)
        set(value) {
            SPUtils.getInstance().put("isTipAIRecognition", value)
        }

    var isTipObservePhoto: Boolean
        get() = SPUtils.getInstance().getBoolean("isTipObservePhoto", true)
        set(value) {
            SPUtils.getInstance().put("isTipObservePhoto", value)
        }

    var continuousBean: ContinuousBean
        get() {
            val json = SPUtils.getInstance().getString("continuousBean", "")
            return if (json.isNullOrEmpty()) ContinuousBean() else Gson().fromJson(
                json,
                ContinuousBean::class.java
            )
        }
        set(value) {
            SPUtils.getInstance().put("continuousBean", Gson().toJson(value))
        }

    var wifiWatermarkBean: WatermarkBean
        get() {
            val json = SPUtils.getInstance().getString("wifiWatermarkBean", "")
            return if (json.isNullOrEmpty()) WatermarkBean() else Gson().fromJson(
                json,
                WatermarkBean::class.java
            )
        }
        set(value) {
            SPUtils.getInstance().put("watermarkBean", Gson().toJson(value))
        }

    var watermarkBean: WatermarkBean
        get() {
            val json = SPUtils.getInstance().getString("watermarkBean", "")
            return if (json.isNullOrEmpty()) WatermarkBean() else Gson().fromJson(
                json,
                WatermarkBean::class.java
            )
        }
        set(value) {
            SPUtils.getInstance().put("watermarkBean", Gson().toJson(value))
        }

    var isTipChangeDevice: Boolean
        get() = SPUtils.getInstance().getBoolean("isTipChangeDevice", true)
        set(value) {
            SPUtils.getInstance().put("isTipChangeDevice", value)
        }

    var isChangeDevice: Boolean
        get() = SPUtils.getInstance().getBoolean("isChangeDevice", false)
        set(value) {
            SPUtils.getInstance().put("isChangeDevice", value)
        }

    private const val TOKEN: String = "token"
    private const val USER_ID: String = "user_id"
    private const val USERNAME: String = "username"
    private const val NICKNAME: String = "nickname"
    private const val HEAD_ICON: String = "head_icon"

    private const val BASE_HOST: String = "base_host"
    private const val LANGUAGE = "language"

    private const val HAS_SHOW_CLAUSE = "hasShowClause"
    private const val TEMPERATURE_UNIT = "temperature"
    private const val VERSION_CHECK_DATE = "version_check_date"

    private const val DEVICE_SN = "deviceSn"
    private const val DEVICE_VERSION = "deviceVersion"

    private const val IR_CONFIG = "ir_config"
    private const val SP_CUSTOM_PSEUDO = "sp_custom_pseudo"
    private const val SP_TARGET_POP = "sp_target_pop"

    private const val SP_SETTING_IS_PUSH = "sp_setting_is_push"
    private const val SP_SETTING_IS_RECOMMEND = "sp_setting_is_recommend"

    private const val SP_HOT_MODE = "sp_hot_mode"
    private const val SP_CHANGE_DEVICE = "sp_change_device"
    private const val SP_TC007_CUSTOM_PSEUDO = "sp_tc007_custom_pseudo"

    private const val SP_CAR_DETECT = "sp_car_detect"

    fun setToken(token: String) {
        SPUtils.getInstance().put(TOKEN, token)
    }

    fun getToken(): String {
        return SPUtils.getInstance().getString(TOKEN, "")
    }

    fun setUserId(token: String) {
        SPUtils.getInstance().put(USER_ID, token)
    }

    fun getUserId(): String {
        return SPUtils.getInstance().getString(USER_ID, "0")
    }

    fun setUsername(username: String) {
        SPUtils.getInstance().put(USERNAME, username)
    }

    fun getUsername(): String {
        return SPUtils.getInstance().getString(USERNAME, "")
    }

    fun setNickname(nickname: String) {
        SPUtils.getInstance().put(NICKNAME, nickname)
    }

    fun getNickname(): String {
        return SPUtils.getInstance().getString(NICKNAME, "")
    }

    fun setHeadIcon(headIcon: String) {
        SPUtils.getInstance().put(HEAD_ICON, headIcon)
    }

    fun getHeadIcon(): String {
        return SPUtils.getInstance().getString(HEAD_ICON, "")
    }

    fun setBaseHost(value: String) {
        return SPUtils.getInstance().put(BASE_HOST, value)
    }

    fun getBaseHost(): String {
        return SPUtils.getInstance().getString(BASE_HOST, "")
    }

    fun setLanguage(context: Context, language: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putString(LANGUAGE, language).apply()
    }

    fun getLanguage(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(LANGUAGE, "")!!
    }

    fun setHasShowClause(hasShowClause: Boolean) {
        return SPUtils.getInstance().put(HAS_SHOW_CLAUSE, hasShowClause)
    }

    fun getHasShowClause(): Boolean {
        return SPUtils.getInstance().getBoolean(HAS_SHOW_CLAUSE, false)
    }

    fun setIRConfig(value: String) {
        return SPUtils.getInstance().put(IR_CONFIG, value)
    }

    fun getIRConfig(): String {
        return SPUtils.getInstance().getString(IR_CONFIG, "")
    }

    fun setTemperature(value: Int) {
        return SPUtils.getInstance().put(TEMPERATURE_UNIT, value)
    }

    fun getTemperature(): Int {
        return SPUtils.getInstance().getInt(TEMPERATURE_UNIT, 1)
    }

    fun setVersionCheckDate(value: Long) {
        return SPUtils.getInstance().put(VERSION_CHECK_DATE, value)
    }

    fun getVersionCheckDate(): Long {
        return SPUtils.getInstance().getLong(VERSION_CHECK_DATE, 0)
    }

    fun setDeviceSn(value: String) {
        return SPUtils.getInstance().put(DEVICE_SN, value)
    }

    fun getDeviceSn(): String {
        return SPUtils.getInstance().getString(DEVICE_SN, "")
    }

    fun setDeviceVersion(value: String) {
        return SPUtils.getInstance().put(DEVICE_VERSION, value)
    }

    fun getDeviceVersion(): String {
        return SPUtils.getInstance().getString(DEVICE_VERSION, "")
    }

    fun saveCustomPseudo(json: String) {
        SPUtils.getInstance().put(SP_CUSTOM_PSEUDO, json)
    }

    fun getCustomPseudo(): String {
        return SPUtils.getInstance().getString(SP_CUSTOM_PSEUDO, "")
    }

    fun saveTC007CustomPseudo(json: String) {
        SPUtils.getInstance().put(SP_TC007_CUSTOM_PSEUDO, json)
    }

    fun getTC0007CustomPseudo(): String {
        return SPUtils.getInstance().getString(SP_TC007_CUSTOM_PSEUDO, "")
    }

    fun getTargetPop(): Boolean {
        return SPUtils.getInstance().getBoolean(SP_TARGET_POP, false)
    }

    fun saveTargetPop(targetPop: Boolean) {
        SPUtils.getInstance().put(SP_TARGET_POP, targetPop)
    }

    private const val IR_DUAL_DISP = "ir_dual_disp"
    private const val IR_DUAL_DISP_V = "ir_dual_disp_v"

    fun saveSettingIsPush(isPush: Boolean) {
        SPUtils.getInstance().put(SP_SETTING_IS_PUSH, isPush)
    }

    fun getSettingIsPush(): Boolean {
        return SPUtils.getInstance().getBoolean(SP_SETTING_IS_PUSH, true)
    }

    fun saveSettingIsRecommend(isRecommend: Boolean) {
        SPUtils.getInstance().put(SP_SETTING_IS_RECOMMEND, isRecommend)
    }

    fun getSettingIsRecommend(): Boolean {
        return SPUtils.getInstance().getBoolean(SP_SETTING_IS_RECOMMEND, true)
    }

    fun getMainPermissionsState(): Boolean {
        return SPUtils.getInstance().getBoolean("main_permissions_state", false)
    }

    fun setMainPermissionsState(value: Boolean) {
        return SPUtils.getInstance().put("main_permissions_state", value)
    }

    fun getImagePermissionsState(): Boolean {
        return SPUtils.getInstance().getBoolean("storage_permissions_state", false)
    }

    fun setImagePermissionsState(value: Boolean) {
        return SPUtils.getInstance().put("storage_permissions_state", value)
    }

    fun getHotMode(): Int {
        return SPUtils.getInstance().getInt(SP_HOT_MODE, 1)
    }

    fun saveHotMode(hotMode: Int) {
        SPUtils.getInstance().put(SP_HOT_MODE, hotMode)
    }

    fun getChangeDevice(): Int {
        return SPUtils.getInstance().getInt(SP_CHANGE_DEVICE, 0)
    }

    fun saveChangeDevice(device: Int) {
        SPUtils.getInstance().put(SP_CHANGE_DEVICE, device)
    }

    fun getCarDetectInfo(): CarDetectChildBean {
        var detectInfo = SPUtils.getInstance().getString(SP_CAR_DETECT, "")
        if (detectInfo.isEmpty()) {
            return CarDetectDialog.getDetectList()[0].detectChildBeans[0]
        }
        var detectChildBean = GsonUtils.fromJson(detectInfo, CarDetectChildBean::class.java)
        var type = detectChildBean.type
        var pos = detectChildBean.pos
        return CarDetectDialog.getDetectList()[type].detectChildBeans[pos]
    }

    fun saveCarDetectInfo(bean: CarDetectChildBean) {
        SPUtils.getInstance().put(SP_CAR_DETECT, GsonUtils.toJson(bean))
    }

}