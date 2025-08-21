package com.topinfrared.tc001.standalone.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class TC001UsbManager(
    private val activity: AppCompatActivity,
    private val connectionCallback: (Boolean, UsbDevice?) -> Unit,
    private val statusCallback: (String) -> Unit = { }
) {
    
    private val usbManager = activity.getSystemService(Context.USB_SERVICE) as UsbManager
    private var connectedDevice: UsbDevice? = null
    private var isMonitoring = false
    private val connectionHandler = Handler(Looper.getMainLooper())
    private var monitoringJob: Job? = null
    private var reconnectionAttempts = 0
    private var lastDisconnectTime = 0L
    
    companion object {
        private const val TAG = "TC001UsbManager"
        private const val ACTION_USB_PERMISSION = "com.topinfrared.tc001.USB_PERMISSION"
        
        // TC001 USB device identifiers - these would need to be updated with actual TC001 VID/PID
        private const val TC001_VENDOR_ID = 0x1234  // Example - replace with actual TC001 VID
        private const val TC001_PRODUCT_ID = 0x5678 // Example - replace with actual TC001 PID
        
        // Enhanced connection management constants
        private const val MAX_RECONNECTION_ATTEMPTS = 5
        private const val RECONNECTION_DELAY_MS = 2000L
        private const val CONNECTION_TIMEOUT_MS = 10000L
        private const val HEALTH_CHECK_INTERVAL_MS = 5000L
        private const val MIN_DISCONNECT_TIME_MS = 1000L // Debounce disconnections
    }
    
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    device?.let { 
                        Log.i(TAG, "USB device attached: ${it.deviceName}")
                        statusCallback("TC001 device detected")
                        handleDeviceAttached(it) 
                    }
                }
                
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    device?.let { 
                        Log.i(TAG, "USB device detached: ${it.deviceName}")
                        statusCallback("TC001 device disconnected")
                        handleDeviceDetached(it) 
                    }
                }
                
                ACTION_USB_PERMISSION -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    
                    if (granted && device != null) {
                        Log.i(TAG, "USB permission granted for: ${device.deviceName}")
                        statusCallback("Permission granted, connecting...")
                        handlePermissionGranted(device)
                    } else {
                        Log.w(TAG, "USB permission denied for device: $device")
                        statusCallback("Permission denied for TC001 device")
                        connectionCallback(false, null)
                    }
                }
            }
        }
    }
    
    init {
        registerUsbReceiver()
        startConnectionMonitoring()
    }
    
    private fun registerUsbReceiver() {
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(ACTION_USB_PERMISSION)
        }
        activity.registerReceiver(usbReceiver, filter)
        Log.d(TAG, "USB broadcast receiver registered")
    }
    
    private fun startConnectionMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringJob = CoroutineScope(Dispatchers.Default).launch {
            while (isMonitoring) {
                try {
                    performHealthCheck()
                    delay(HEALTH_CHECK_INTERVAL_MS)
                } catch (e: Exception) {
                    Log.e(TAG, "Error during connection monitoring", e)
                }
            }
        }
        Log.d(TAG, "Connection monitoring started")
    }
    
    private suspend fun performHealthCheck() {
        withContext(Dispatchers.Main) {
            val device = connectedDevice
            if (device != null) {
                // Check if device is still physically connected
                val isStillConnected = usbManager.deviceList.values.any { 
                    it.deviceId == device.deviceId && it.deviceName == device.deviceName 
                }
                
                if (!isStillConnected) {
                    Log.w(TAG, "Health check: Device no longer available")
                    statusCallback("TC001 connection lost")
                    handleConnectionLost(device)
                } else {
                    // Device is still there - could perform additional checks here
                    // like verifying communication
                    Log.v(TAG, "Health check: TC001 connection healthy")
                }
            } else {
                // No device connected - check if TC001 became available
                val tc001Device = findTC001Device()
                if (tc001Device != null) {
                    Log.d(TAG, "Health check: TC001 device now available")
                    statusCallback("TC001 device found")
                    attemptConnection(tc001Device)
                }
            }
        }
    }
    
    suspend fun connectToTC001(): Boolean {
        statusCallback("Searching for TC001 device...")
        val tc001Device = findTC001Device()
        
        return if (tc001Device != null) {
            statusCallback("TC001 found, requesting permissions...")
            attemptConnection(tc001Device)
        } else {
            Log.w(TAG, "TC001 device not found")
            statusCallback("TC001 device not found")
            false
        }
    }
    
    private suspend fun attemptConnection(device: UsbDevice): Boolean {
        return try {
            withTimeout(CONNECTION_TIMEOUT_MS) {
                withContext(Dispatchers.Main) {
                    requestPermissionAndConnect(device)
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Connection attempt timed out")
            statusCallback("Connection timeout")
            false
        }
    }
    
    private fun findTC001Device(): UsbDevice? {
        val deviceList = usbManager.deviceList
        
        for (device in deviceList.values) {
            Log.v(TAG, "Scanning device: VID=${String.format("0x%04X", device.vendorId)}, PID=${String.format("0x%04X", device.productId)}")
            
            // Check if this is a TC001 device
            if (device.vendorId == TC001_VENDOR_ID && device.productId == TC001_PRODUCT_ID) {
                Log.i(TAG, "TC001 device found: ${device.deviceName}")
                return device
            }
            
            // For development/testing - accept UVC devices
            if (device.deviceClass == 14 || // USB Video Class
                (device.interfaceCount > 0 && device.getInterface(0).interfaceClass == 14)) {
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
        Log.d(TAG, "Device attached: ${device.deviceName}")
        if (isTC001Device(device)) {
            statusCallback("TC001 attached, connecting...")
            CoroutineScope(Dispatchers.Main).launch {
                attemptConnection(device)
            }
        }
    }
    
    private fun handleDeviceDetached(device: UsbDevice) {
        Log.d(TAG, "Device detached: ${device.deviceName}")
        if (device.deviceId == connectedDevice?.deviceId) {
            lastDisconnectTime = System.currentTimeMillis()
            handleConnectionLost(device)
        }
    }
    
    private fun handleConnectionLost(device: UsbDevice) {
        Log.w(TAG, "Connection lost to device: ${device.deviceName}")
        connectedDevice = null
        connectionCallback(false, null)
        
        // Schedule reconnection attempt
        scheduleReconnection()
    }
    
    private fun scheduleReconnection() {
        if (reconnectionAttempts >= MAX_RECONNECTION_ATTEMPTS) {
            Log.w(TAG, "Max reconnection attempts reached")
            statusCallback("Unable to reconnect to TC001")
            return
        }
        
        // Don't reconnect too quickly after disconnect (debouncing)
        val timeSinceDisconnect = System.currentTimeMillis() - lastDisconnectTime
        val delay = kotlin.math.max(RECONNECTION_DELAY_MS, MIN_DISCONNECT_TIME_MS - timeSinceDisconnect)
        
        connectionHandler.postDelayed({
            CoroutineScope(Dispatchers.Main).launch {
                Log.i(TAG, "Attempting reconnection (${++reconnectionAttempts}/$MAX_RECONNECTION_ATTEMPTS)")
                statusCallback("Reconnecting to TC001... ($reconnectionAttempts/$MAX_RECONNECTION_ATTEMPTS)")
                
                val success = connectToTC001()
                if (!success) {
                    scheduleReconnection() // Try again if failed
                }
            }
        }, delay)
    }
    
    private fun handlePermissionGranted(device: UsbDevice) {
        Log.i(TAG, "USB permission granted for device: ${device.deviceName}")
        connectedDevice = device
        reconnectionAttempts = 0 // Reset attempt counter on successful connection
        statusCallback("TC001 connected successfully")
        connectionCallback(true, device)
    }
    
    private fun isTC001Device(device: UsbDevice): Boolean {
        return (device.vendorId == TC001_VENDOR_ID && device.productId == TC001_PRODUCT_ID) ||
               device.deviceClass == 14 || // UVC class for development
               (device.interfaceCount > 0 && device.getInterface(0).interfaceClass == 14)
    }
    
    fun checkTC001Connection(): Boolean {
        val device = findTC001Device()
        val isConnected = device != null && usbManager.hasPermission(device)
        
        if (isConnected && device != null) {
            connectedDevice = device
            statusCallback("TC001 connected")
        } else {
            connectedDevice = null
            statusCallback("TC001 not connected")
        }
        
        connectionCallback(isConnected, device)
        return isConnected
    }
    
    fun getConnectedDevice(): UsbDevice? = connectedDevice
    
    fun getConnectionStatus(): String {
        return when {
            connectedDevice != null -> "Connected: ${connectedDevice!!.deviceName}"
            reconnectionAttempts > 0 -> "Reconnecting... ($reconnectionAttempts/$MAX_RECONNECTION_ATTEMPTS)"
            else -> "Not connected"
        }
    }
    
    fun forceReconnection() {
        Log.i(TAG, "Forcing reconnection attempt")
        reconnectionAttempts = 0
        connectedDevice = null
        CoroutineScope(Dispatchers.Main).launch {
            connectToTC001()
        }
    }
    
    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        connectionHandler.removeCallbacksAndMessages(null)
    }
    
    fun cleanup() {
        stopMonitoring()
        
        try {
            activity.unregisterReceiver(usbReceiver)
            Log.d(TAG, "USB receiver unregistered")
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering USB receiver", e)
        }
        
        connectedDevice = null
        reconnectionAttempts = 0
        Log.d(TAG, "TC001 USB manager cleaned up")
    }
}