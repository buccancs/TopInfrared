package com.topinfrared.tc001.ir.camera

import android.content.Context
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Simplified TC001 camera handler for standalone version
 * This is a mock implementation for demonstration purposes
 * In a real implementation, this would interface with actual TC001 hardware
 */
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
        // Mock initialization for standalone demo
        Log.d(TAG, "Initializing TC001 camera handler (mock)")
        return true
    }
    
    suspend fun connectToDevice(device: UsbDevice?): Boolean = withContext(Dispatchers.IO) {
        try {
            // Mock connection for standalone demo
            Log.i(TAG, "Connecting to TC001 device (mock)")
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
                    Thread.sleep(100) // ~10 FPS for demo
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
        
        for (y in 0 until PREVIEW_HEIGHT) {
            for (x in 0 until PREVIEW_WIDTH) {
                val index = y * PREVIEW_WIDTH + x
                val temp = (x + y + System.currentTimeMillis() / 100) % 256
                val normalized = temp / 255f
                
                // Thermal color mapping
                val r = (normalized * 255).toInt()
                val g = ((1 - normalized) * normalized * 4 * 255).toInt()
                val b = ((1 - normalized) * 255).toInt()
                
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