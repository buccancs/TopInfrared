package com.topinfrared.tc001.standalone.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class TC001UsbManager(
    private val activity: AppCompatActivity,
    private val connectionCallback: (Boolean) -> Unit
) {
    
    private val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
    private var connectedDevice: UsbDevice? = null
    
    companion object {
        private const val TAG = "TC001UsbManager"
        private const val ACTION_USB_PERMISSION = "com.topinfrared.tc001.USB_PERMISSION"
        
        // TC001 USB device identifiers - these would need to be updated with actual TC001 VID/PID
        private const val TC001_VENDOR_ID = 0x1234  // Example - replace with actual TC001 VID
        private const val TC001_PRODUCT_ID = 0x5678 // Example - replace with actual TC001 PID
    }
    
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
                
                ACTION_USB_PERMISSION -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    
                    if (granted && device != null) {
                        handlePermissionGranted(device)
                    } else {
                        Log.w(TAG, "USB permission denied for device: $device")
                        connectionCallback(false)
                    }
                }
            }
        }
    }
    
    init {
        registerUsbReceiver()
    }
    
    private fun registerUsbReceiver() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(ACTION_USB_PERMISSION)
        }
        activity.registerReceiver(usbReceiver, filter)
    }
    
    suspend fun connectToTC001(): Boolean {
        val tc001Device = findTC001Device()
        
        return if (tc001Device != null) {
            requestPermissionAndConnect(tc001Device)
        } else {
            Log.w(TAG, "TC001 device not found")
            false
        }
    }
    
    private fun findTC001Device(): UsbDevice? {
        val deviceList = usbManager.deviceList
        
        for (device in deviceList.values) {
            Log.d(TAG, "Found USB device: VID=${device.vendorId}, PID=${device.productId}")
            
            // Check if this is a TC001 device
            if (device.vendorId == TC001_VENDOR_ID && device.productId == TC001_PRODUCT_ID) {
                Log.i(TAG, "TC001 device found: ${device.deviceName}")
                return device
            }
            
            // For development/testing - you might want to accept any USB camera device
            // Remove this in production and use proper TC001 VID/PID
            if (device.deviceClass == 14) { // USB Video Class
                Log.i(TAG, "UVC device found (potential TC001): ${device.deviceName}")
                return device
            }
        }
        
        return null
    }
    
    private fun requestPermissionAndConnect(device: UsbDevice): Boolean {
        return if (usbManager.hasPermission(device)) {
            handlePermissionGranted(device)
            true
        } else {
            val permissionIntent = PendingIntent.getBroadcast(
                activity,
                0,
                Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            usbManager.requestPermission(device, permissionIntent)
            false // Permission request is async, result comes via broadcast
        }
    }
    
    private fun handleDeviceAttached(device: UsbDevice) {
        Log.d(TAG, "USB device attached: ${device.deviceName}")
        if (isTC001Device(device)) {
            requestPermissionAndConnect(device)
        }
    }
    
    private fun handleDeviceDetached(device: UsbDevice) {
        Log.d(TAG, "USB device detached: ${device.deviceName}")
        if (device == connectedDevice) {
            connectedDevice = null
            connectionCallback(false)
        }
    }
    
    private fun handlePermissionGranted(device: UsbDevice) {
        Log.i(TAG, "USB permission granted for device: ${device.deviceName}")
        connectedDevice = device
        connectionCallback(true)
    }
    
    private fun isTC001Device(device: UsbDevice): Boolean {
        return device.vendorId == TC001_VENDOR_ID && device.productId == TC001_PRODUCT_ID ||
               device.deviceClass == 14 // UVC class for development
    }
    
    fun checkTC001Connection() {
        val device = findTC001Device()
        val isConnected = device != null && usbManager.hasPermission(device)
        
        if (isConnected && device != null) {
            connectedDevice = device
        }
        
        connectionCallback(isConnected)
    }
    
    fun getConnectedDevice(): UsbDevice? = connectedDevice
    
    fun cleanup() {
        try {
            activity.unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering USB receiver", e)
        }
        connectedDevice = null
    }
}