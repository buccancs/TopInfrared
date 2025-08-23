package com.topdon.lib.core.tools

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.blankj.utilcode.util.Utils
import com.elvishew.xlog.XLog
import com.topdon.lib.core.bean.event.device.DeviceConnectEvent
import com.topdon.lib.core.bean.event.device.DevicePermissionEvent
import com.topdon.lib.core.broadcast.DeviceBroadcastReceiver
import com.topdon.lib.core.config.DeviceConfig.isHik256
import com.topdon.lib.core.config.DeviceConfig.isTcLiteDevice
import com.topdon.lib.core.config.DeviceConfig.isTcTsDevice
import com.topdon.lib.core.utils.ByteUtils.toBytes
import com.topdon.lib.core.utils.ByteUtils.toHexString
import org.greenrobot.eventbus.EventBus

object DeviceTools {

    fun isConnect(isSendConnectEvent: Boolean = false, isAutoRequest: Boolean = true): Boolean {
        val usbManager = Utils.getApp().getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList
        for (usbDevice in deviceList.values) {
            if (usbDevice.isTcTsDevice()) {
                return if (usbManager.hasPermission(usbDevice)) {
                    XLog.i("设备已连接且有权限")
                    if (isSendConnectEvent) {
                        EventBus.getDefault().post(DeviceConnectEvent(true, usbDevice))
                    }
                    true
                } else {
                    XLog.w("设备已连接但无权限")
                    if (isAutoRequest) {
                        EventBus.getDefault().post(DevicePermissionEvent(usbDevice))
                    }
                    false
                }
            }
        }
        return false
    }

    fun findUsbDevice(): UsbDevice? {
        val usbManager = Utils.getApp().getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> =  usbManager.deviceList
        for (usbDevice in deviceList.values) {
            if (usbDevice.isTcTsDevice()) {
                val productID = usbDevice.productId.toBytes(2).toHexString()
                val vendorID = usbDevice.vendorId.toBytes(2).toHexString()
                XLog.i("找到一个usb设备 productId:${productID}, vendorId:${vendorID}, deviceName:${usbDevice.deviceName}")
                return usbDevice
            }
        }
        XLog.i("检索到${deviceList.size}个设备, 没有符合定制usb设备")
        return null
    }

    fun isTC001PlusConnect(): Boolean {
        val usbManager = Utils.getApp().getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> =  usbManager.deviceList
        var usbCameraNumber = 0
        var isTcTsDev = false
        for (usbDevice in deviceList.values) {
            if ("USB Camera" == usbDevice.productName){
                usbCameraNumber ++
            }
            if (!isTcTsDev){
                isTcTsDev = usbDevice.isTcTsDevice() && usbManager.hasPermission(usbDevice)
            }
        }
        return isTcTsDev && usbCameraNumber > 1
    }

    fun isTC001LiteConnect() : Boolean{
        val usbManager = Utils.getApp().getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList: HashMap<String, UsbDevice> =  usbManager.deviceList
        for (usbDevice in deviceList.values) {
            if (usbDevice.isTcLiteDevice()) {
                return true
            }
        }
        return false
    }

    fun isHikConnect(): Boolean {
        val usbManager: UsbManager = Utils.getApp().getSystemService(Context.USB_SERVICE) as UsbManager
        for (usbDevice in usbManager.deviceList.values) {
            if (usbDevice.isHik256()) {
                return true
            }
        }
        return false
    }

    fun requestUsb(activity: Activity, requestCode: Int, device: UsbDevice) {
        val usbManager = Utils.getApp().getSystemService(Context.USB_SERVICE) as UsbManager
        val intent = Intent(DeviceBroadcastReceiver.ACTION_USB_PERMISSION)
        val flag = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getBroadcast(activity, requestCode, intent, flag)
        usbManager.requestPermission(device, pendingIntent)
        XLog.i("申请usb权限")
    }

}