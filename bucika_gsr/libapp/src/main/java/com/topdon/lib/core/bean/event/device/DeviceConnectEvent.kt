package com.topdon.lib.core.bean.event.device

import android.hardware.usb.UsbDevice

/**
 * 目标 USB 设备（即符合 productId 及 vendorId）连接状态事件.
 * @param isConnect true-已连接 false-已断开
 * @param device 若已连接，连接的设备
 */
data class DeviceConnectEvent(val isConnect: Boolean, val device: UsbDevice?)
