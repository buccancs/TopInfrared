package com.topdon.lib.core.http.ts004

object TS004URL {
    const val RTSP_URL = "rtsp://192.168.40.1/ch0/stream0"
    private const val BASE_URL = "http://192.168.40.1:8080"
    const val SET_PSEUDO_COLOR = "$BASE_URL/api/v1/system/setPseudoColor"//设置伪彩样式
    const val GET_PSEUDO_COLOR = "$BASE_URL/api/v1/system/getPseudoColor"//获取伪彩样式
    const val SET_PANEL_PARAM = "$BASE_URL/api/v1/system/setPanelParam"//设置屏幕亮度
    const val GET_PANEL_PARAM = "$BASE_URL/api/v1/system/setPanelParam"//获取屏幕亮度
    const val SET_PIP = "$BASE_URL/api/v1/system/setPip"//设置画中画
    const val GET_PIP = "$BASE_URL/api/v1/system/getPip"//获取画中画状态
    const val SET_ZOOM = "$BASE_URL/api/v1/system/setZoom"//设置放大倍数
    const val GET_ZOOM = "$BASE_URL/api/v1/system/getZoom"//获取放大倍数
    const val SET_SNAPSHOT = "$BASE_URL/api/v1/system/snapshot"//拍照
    const val GET_VRECORD = "$BASE_URL/api/v1/system/vrecord"//录像
    const val GET_RECORD_STATUS = "$BASE_URL/api/v1/system/getRecordStatus"//获取录像状态
    const val GET_VERSION = "$BASE_URL/api/v1/system/getVersion"//获取版本信息
    const val GET_DEVICE_DETAILS = "$BASE_URL/api/v1/system/getDeviceInfo"//获取设备信息
    const val GET_FREE_SPACE = "$BASE_URL/api/v1/system/getFreeSpace"//获取存储分区信息
    const val GET_FORMAT_STORAGE = "$BASE_URL/api/v1/system/formatStorage"//格式化存储分区
    const val GET_RESET_ALL= "$BASE_URL/api/v1/system/resetAll"//恢复出厂设置
    const val GET_DELETE_FILE= "$BASE_URL/api/v1/system/deleteFile"//删除照片视频文件
    const val GET_UPGRADE_STATUS= "$BASE_URL/api/v1/system/getUpgradeStatus"//获取固件升级状态
    const val SET_TEMPERATURE_STATE= "$BASE_URL/api/v1/system/setTemperatureState"//设置测温开关
    const val GET_FILE_LIST= "$BASE_URL/api/v1/system/getFileList"//获取文件列表
    const val SET_DATE_TIME= "$BASE_URL/api/v1/system/setDateTime"//设置时钟
    const val GET_SEND_UPGRADE_FILE_START= "$BASE_URL/api/v1/system/sendUpgradeFileStart"//固件升级数据传输开始
    const val GET_SEND_UPGRADE_FILE_DATA= "$BASE_URL/api/v1/system/sendUpgradeFileData"//固件升级数据传输
    const val GET_SEND_UPGRADE_FILE_END= "$BASE_URL/api/v1/system/sendUpgradeFileEnd"//固件升级数据传输结束
    const val GET_REMOTE_UPGRADE= "$BASE_URL/api/v1/system/remoteUpgrade"//固件升级
    const val SET_WIFI_AP_ON_OFF= "$BASE_URL/api/v1/system/setWifiAPOnOff"//设置wifi on/off
    const val GET_WIFI_AP_CONFIG= "$BASE_URL/api/v1/system/getWifiAPConfig"//获取wifi配置信息
    const val GET_FILE_COUNT= "$BASE_URL/api/v1/system/getFileCount"//获取文件数量
    const val SET_POWER_ACTION= "$BASE_URL/api/v1/system/setPowerAction"//设置电源状态
    const val SET_DO_NUC= "$BASE_URL/api/v1/system/doNuc"//nuc
    const val SET_FREEZE= "$BASE_URL/api/v1/system/setFreeze"//图像冻结
    const val GET_BATTERY_INFO= "$BASE_URL/api/v1/system/getBatteryInfo"//获取电池信息
    const val SET_TISP= "$BASE_URL/api/v1/system/setTISR"//设置超分
    const val GET_TISR= "$BASE_URL/api/v1/system/getTISR"//获取超分状态
    const val GET_DATE_TIME= "$BASE_URL/api/v1/system/getDateTime"//获取时钟
}