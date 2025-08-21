package com.topdon.lib.core.bean.event.device

import android.hardware.usb.UsbDevice

/**
 * 目标 USB 设备（即符合 productId 及 vendorId）已连接但需要进行权限申请事件.
 * @param device 已连接但没有授权的设备
 */
data class DevicePermissionEvent(val device: UsbDevice)
