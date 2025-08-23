package com.topdon.lib.core.config

import android.hardware.usb.UsbDevice

object DeviceConfig {

    const val TS004_NAME_START = "TS004_"
    const val TS004_PASSWORD = "TS004001"

    const val TC007_NAME_START = "TC007_"
    const val TC007_PASSWORD = "12345678"

    const val IR_VENDOR_ID = 0x0BDA
    const val IR_PRODUCT_ID = 0x5840

    const val TOPDON_VENDOR_ID = 0x0BDA
    const val TOPDON_PRODUCT_ID = 0x5830

    const val TCLITE_VENDOR_ID = 13428
    const val TCLITE_PRODUCT_ID = 17185

    const val HIK_VENDOR_ID = 11231
    const val HIK_PRODUCT_ID = 258

    fun UsbDevice.isTcTsDevice(): Boolean {
        return (productId == TOPDON_PRODUCT_ID && vendorId == TOPDON_VENDOR_ID) ||
                (productId == IR_PRODUCT_ID && vendorId == IR_VENDOR_ID) ||
                (productId == TCLITE_PRODUCT_ID && vendorId == TCLITE_VENDOR_ID) ||
                (productId == HIK_PRODUCT_ID && vendorId == HIK_VENDOR_ID)
    }

    fun UsbDevice.isTcLiteDevice() : Boolean{
        return (productId == TCLITE_PRODUCT_ID && vendorId == TCLITE_VENDOR_ID)
    }

    fun UsbDevice.isHik256(): Boolean = productId == HIK_PRODUCT_ID && vendorId == HIK_VENDOR_ID

    const val SKU = "TDTC001A11"
    const val SN = "TC001A11000001"

    const val ROTATE_ANGLE = 0
    const val IS_PORTRAIT = false

    const val S_ROTATE_ANGLE = 270
    const val S_IS_PORTRAIT = true

}