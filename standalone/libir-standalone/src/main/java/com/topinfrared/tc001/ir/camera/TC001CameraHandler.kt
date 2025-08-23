package com.topinfrared.tc001.ir.camera

import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TC001CameraHandler(
    private val context: Context,
    private val frameCallback: (Bitmap?) -> Unit
) {
    
    companion object {
        private const val TAG = "TC001CameraHandler"
        private const val PREVIEW_WIDTH = 256
        private const val PREVIEW_HEIGHT = 192
    }
    
    private var isConnected = false
    private var isCapturing = false
    private var mockFrameThread: Thread? = null
    
    fun initialize(): Boolean {
        Log.d(TAG, "Initializing TC001 camera handler (mock)")
        return true
    }
    
    suspend fun connectToDevice(device: UsbDevice?): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.i(TAG, "Connecting to TC001 device (mock)${device?.let { " - Device: ${it.deviceName}" } ?: ""}")
            isConnected = true
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect to TC001 device", e)
            false
        }
    }
    
    suspend fun startPreview(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isConnected) {
                Log.w(TAG, "Camera not connected, cannot start preview")
                return@withContext false
            }
            
            if (isCapturing) {
                Log.w(TAG, "Preview already running")
                return@withContext true
            }
            
            isCapturing = true
            startMockFrameUpdates()
            
            Log.i(TAG, "TC001 thermal preview started (mock)")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start preview", e)
            false
        }
    }
    
    suspend fun stopPreview(): Unit = withContext(Dispatchers.IO) {
        try {
            if (isCapturing) {
                isCapturing = false
                mockFrameThread?.interrupt()
                mockFrameThread = null
                Log.i(TAG, "TC001 thermal preview stopped")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop preview", e)
        }
    }
    
    private fun startMockFrameUpdates() {
        mockFrameThread = Thread {
            while (isCapturing && !Thread.currentThread().isInterrupted) {
                try {
                    val bitmap = createMockThermalBitmap()
                    frameCallback(bitmap)
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "Error in mock frame updates", e)
                    break
                }
            }
        }
        mockFrameThread?.start()
    }
    
    private fun createMockThermalBitmap(): Bitmap {
        val colors = IntArray(PREVIEW_WIDTH * PREVIEW_HEIGHT)
        val time = System.currentTimeMillis() / 1000.0
        val centerX = PREVIEW_WIDTH / 2
        val centerY = PREVIEW_HEIGHT / 2
        
        for (y in 0 until PREVIEW_HEIGHT) {
            for (x in 0 until PREVIEW_WIDTH) {
                val index = y * PREVIEW_WIDTH + x
                
                val dx = (x - centerX).toDouble() / PREVIEW_WIDTH
                val dy = (y - centerY).toDouble() / PREVIEW_HEIGHT
                
                val hotSpot1 = kotlin.math.exp(-((dx - 0.3 * kotlin.math.sin(time * 0.5)) * (dx - 0.3 * kotlin.math.sin(time * 0.5)) + 
                                                (dy - 0.3 * kotlin.math.cos(time * 0.5)) * (dy - 0.3 * kotlin.math.cos(time * 0.5))) * 10)
                val hotSpot2 = kotlin.math.exp(-((dx + 0.2 * kotlin.math.sin(time * 0.7)) * (dx + 0.2 * kotlin.math.sin(time * 0.7)) + 
                                                (dy + 0.2 * kotlin.math.cos(time * 0.7)) * (dy + 0.2 * kotlin.math.cos(time * 0.7))) * 15)
                
                var temp = 0.3 + 0.4 * hotSpot1 + 0.3 * hotSpot2 + 0.1 * kotlin.math.sin(time + x * 0.1 + y * 0.1)
                temp = temp.coerceIn(0.0, 1.0)
                
                val r: Int
                val g: Int 
                val b: Int
                
                when {
                    temp < 0.25 -> {
                        val factor = temp * 4
                        r = 0
                        g = 0
                        b = (128 + factor * 127).toInt()
                    }
                    temp < 0.5 -> {
                        val factor = (temp - 0.25) * 4
                        r = 0
                        g = (factor * 255).toInt()
                        b = 255
                    }
                    temp < 0.75 -> {
                        val factor = (temp - 0.5) * 4
                        r = (factor * 255).toInt()
                        g = 255
                        b = ((1 - factor) * 255).toInt()
                    }
                    else -> {
                        val factor = (temp - 0.75) * 4
                        r = 255
                        g = ((1 - factor) * 255).toInt()
                        b = 0
                    }
                }
                
                colors[index] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        
        return Bitmap.createBitmap(colors, PREVIEW_WIDTH, PREVIEW_HEIGHT, Bitmap.Config.ARGB_8888)
    }
    
    fun isConnected(): Boolean = isConnected
    
    fun isCapturing(): Boolean = isCapturing
    
    fun disconnect() {
        try {
            isCapturing = false
            mockFrameThread?.interrupt()
            mockFrameThread = null
            isConnected = false
            
            Log.i(TAG, "TC001 camera disconnected")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect", e)
        }
    }
    
    fun cleanup() {
        disconnect()
        Log.d(TAG, "TC001 camera handler cleaned up")
    }
}