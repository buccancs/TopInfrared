package com.topinfrared.tc001.standalone.usb

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.util.Log

class UsbReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "UsbReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                Log.d(TAG, "USB device attached")
                // Handle USB device attachment
                // This will be handled by TC001UsbManager
            }
            
            UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                Log.d(TAG, "USB device detached")
                // Handle USB device detachment
                // This will be handled by TC001UsbManager
            }
            
            "com.topinfrared.tc001.USB_PERMISSION" -> {
                Log.d(TAG, "USB permission response received")
                // Handle USB permission response
                // This will be handled by TC001UsbManager
            }
        }
    }
}