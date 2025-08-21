package com.topdon.tc001.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.elvishew.xlog.XLog

/**
 * USB Hot-Plug Manager for bucika_gsr
 * Enhanced USB device detection and connection management
 */
class USBHotPlugManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "USBHotPlugManager"
        
        @Volatile
        private var INSTANCE: USBHotPlugManager? = null
        
        fun getInstance(context: Context): USBHotPlugManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: USBHotPlugManager(context.applicationContext).also { INSTANCE = it }
            }
        }
        
        // Known device identifiers for TC001 and related devices
        private val SUPPORTED_DEVICES = mapOf(
            0x3537 to listOf(0x0001, 0x0002), // TC001 thermal camera
            0x0403 to listOf(0x6001), // FTDI devices
            0x067B to listOf(0x2303)  // Additional supported devices
        )
    }

    interface USBDeviceListener {
        fun onDeviceAttached(device: UsbDevice)
        fun onDeviceDetached(device: UsbDevice)
        fun onSupportedDeviceDetected(device: UsbDevice, deviceType: DeviceType)
        fun onDevicePermissionGranted(device: UsbDevice)
        fun onDevicePermissionDenied(device: UsbDevice)
    }

    enum class DeviceType {
        TC001_THERMAL,
        FTDI_SERIAL,
        UNKNOWN_SUPPORTED,
        UNSUPPORTED
    }

    private var listener: USBDeviceListener? = null
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var isMonitoring = false

    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    device?.let { handleDeviceAttached(it) }
                }
                
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    device?.let { handleDeviceDetached(it) }
                }
                
                "com.topdon.tc001.USB_PERMISSION" -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    device?.let {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            handlePermissionGranted(it)
                        } else {
                            handlePermissionDenied(it)
                        }
                    }
                }
            }
        }
    }

    fun setUSBDeviceListener(listener: USBDeviceListener?) {
        this.listener = listener
    }

    fun startMonitoring() {
        if (isMonitoring) return
        
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction("com.topdon.tc001.USB_PERMISSION")
        }
        
        context.registerReceiver(usbReceiver, filter)
        isMonitoring = true
        
        XLog.i(TAG, "USB hot-plug monitoring started")
        
        // Check for already connected devices
        checkConnectedDevices()
    }

    fun stopMonitoring() {
        if (!isMonitoring) return
        
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            XLog.w(TAG, "Error unregistering USB receiver: ${e.message}")
        }
        
        isMonitoring = false
        XLog.i(TAG, "USB hot-plug monitoring stopped")
    }

    private fun handleDeviceAttached(device: UsbDevice) {
        XLog.i(TAG, "USB device attached: ${device.deviceName} (${device.vendorId}:${device.productId})")
        
        val deviceType = identifyDevice(device)
        
        listener?.onDeviceAttached(device)
        
        if (deviceType != DeviceType.UNSUPPORTED) {
            listener?.onSupportedDeviceDetected(device, deviceType)
            
            // Auto-request permission for supported devices
            requestPermission(device)
        }
    }

    private fun handleDeviceDetached(device: UsbDevice) {
        XLog.i(TAG, "USB device detached: ${device.deviceName}")
        listener?.onDeviceDetached(device)
    }

    private fun handlePermissionGranted(device: UsbDevice) {
        XLog.i(TAG, "USB permission granted for: ${device.deviceName}")
        listener?.onDevicePermissionGranted(device)
    }

    private fun handlePermissionDenied(device: UsbDevice) {
        XLog.w(TAG, "USB permission denied for: ${device.deviceName}")
        listener?.onDevicePermissionDenied(device)
    }

    private fun identifyDevice(device: UsbDevice): DeviceType {
        val vendorId = device.vendorId
        val productId = device.productId
        
        return when (vendorId) {
            0x3537 -> {
                if (SUPPORTED_DEVICES[vendorId]?.contains(productId) == true) {
                    DeviceType.TC001_THERMAL
                } else {
                    DeviceType.UNKNOWN_SUPPORTED
                }
            }
            0x0403 -> {
                if (SUPPORTED_DEVICES[vendorId]?.contains(productId) == true) {
                    DeviceType.FTDI_SERIAL
                } else {
                    DeviceType.UNKNOWN_SUPPORTED
                }
            }
            else -> {
                if (SUPPORTED_DEVICES.keys.contains(vendorId)) {
                    DeviceType.UNKNOWN_SUPPORTED
                } else {
                    DeviceType.UNSUPPORTED
                }
            }
        }
    }

    private fun requestPermission(device: UsbDevice) {
        if (usbManager.hasPermission(device)) {
            XLog.i(TAG, "Permission already granted for: ${device.deviceName}")
            listener?.onDevicePermissionGranted(device)
            return
        }
        
        val permissionIntent = android.app.PendingIntent.getBroadcast(
            context, 0, 
            Intent("com.topdon.tc001.USB_PERMISSION"),
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or 
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.app.PendingIntent.FLAG_IMMUTABLE
            } else {
                0
            }
        )
        
        usbManager.requestPermission(device, permissionIntent)
        XLog.i(TAG, "Requesting permission for: ${device.deviceName}")
    }

    private fun checkConnectedDevices() {
        val deviceList = usbManager.deviceList
        XLog.i(TAG, "Checking ${deviceList.size} connected USB devices")
        
        deviceList.values.forEach { device ->
            val deviceType = identifyDevice(device)
            if (deviceType != DeviceType.UNSUPPORTED) {
                listener?.onSupportedDeviceDetected(device, deviceType)
                
                if (usbManager.hasPermission(device)) {
                    listener?.onDevicePermissionGranted(device)
                } else {
                    requestPermission(device)
                }
            }
        }
    }

    fun getConnectedDevices(): List<UsbDevice> {
        return usbManager.deviceList.values.toList()
    }

    fun getSupportedConnectedDevices(): List<Pair<UsbDevice, DeviceType>> {
        return getConnectedDevices().mapNotNull { device ->
            val deviceType = identifyDevice(device)
            if (deviceType != DeviceType.UNSUPPORTED) {
                Pair(device, deviceType)
            } else {
                null
            }
        }
    }

    fun hasPermissionForDevice(device: UsbDevice): Boolean {
        return usbManager.hasPermission(device)
    }

    fun requestPermissionManually(device: UsbDevice) {
        requestPermission(device)
    }

    fun getDeviceInfo(device: UsbDevice): String {
        return """
            Device Name: ${device.deviceName}
            Vendor ID: 0x${device.vendorId.toString(16).uppercase()}
            Product ID: 0x${device.productId.toString(16).uppercase()}
            Device Class: ${device.deviceClass}
            Device Subclass: ${device.deviceSubclass}
            Device Protocol: ${device.deviceProtocol}
            Interface Count: ${device.interfaceCount}
            Has Permission: ${usbManager.hasPermission(device)}
            Device Type: ${identifyDevice(device)}
        """.trimIndent()
    }
}