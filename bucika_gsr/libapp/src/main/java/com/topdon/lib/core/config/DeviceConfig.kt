package com.topdon.lib.core.config

import android.hardware.usb.UsbDevice

object DeviceConfig {

    //TC001 - main supported device
    //vid:3034, pid:22592
    const val IR_VENDOR_ID = 0x0BDA
    const val IR_PRODUCT_ID = 0x5840

    //topdon
    const val TOPDON_VENDOR_ID = 0x0BDA
    const val TOPDON_PRODUCT_ID = 0x5830

    /**
     * 判断该 UsbDevice 是否为TC001设备.
     */
    fun UsbDevice.isTcTsDevice(): Boolean {
        return (productId == TOPDON_PRODUCT_ID && vendorId == TOPDON_VENDOR_ID) ||
                (productId == IR_PRODUCT_ID && vendorId == IR_VENDOR_ID)
    }





    const val SKU = "TDTC001A11"
    const val SN = "TC001A11000001"

//    //test
//    const val SKU = "TDBT006A11"
//    const val SN = "BT006AAG100001"

    // 横屏 TC003校对默认角度0 默认竖屏false 初始化设置initDataIR()
    const val ROTATE_ANGLE = 0
    const val IS_PORTRAIT = false

    // 竖屏
    const val S_ROTATE_ANGLE = 270
    const val S_IS_PORTRAIT = true

}