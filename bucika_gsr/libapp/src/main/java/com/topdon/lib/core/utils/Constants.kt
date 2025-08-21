package com.topdon.lib.core.utils

object Constants {
    const val PRODUCT_TYPE_NAME = "product_type"
    const val PRODUCT_TS001_NAME = "TS001"
    const val PRODUCT_TS004_NAME = "TS004"
    const val SETTING_TYPE = "setting_type"
    const val SETTING_BOOK = 0
    const val SETTING_FAQ = 1
    const val SETTING_CONNECTION_TYPE = "connection_type"
    const val SETTING_CONNECTION = 0
    const val SETTING_DISCONNECTION = 1
    const val IR_TEMPERATURE_MODE = 1
    const val IR_OBSERVE_MODE = 2
    const val IR_EDIT_MODE = 4 //二次编辑模式
    const val IR_TCPLUS_MODE = 5 // 双光设备
    const val IR_TC007_MODE = 6 // TC007设备
    const val IR_TEMPERATURE_LITE = 7 // lite设备

    /**
     * 当为生成报告时，是否为生成报告第1张图.
     * true-第1张图编辑 false-再次添加图片编辑
     */
    const val IS_REPORT_FIRST = "IS_REPORT_FIRST"

}