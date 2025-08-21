package com.topinfrared.tc001.standalone.thermal

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import com.topinfrared.tc001.ir.camera.TC001CameraHandler
import com.topinfrared.tc001.common.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class TC001ThermalManager(
    private val context: Context,
    private val frameCallback: (Bitmap?) -> Unit
) {
    
    companion object {
        private const val TAG = "TC001ThermalManager"
        private const val THERMAL_FOLDER = "TC001_Thermal"
    }
    
    enum class TempMode {
        POINT, LINE, AREA
    }
    
    private var isCapturing = false
    private var isRecording = false
    private var currentTempMode = TempMode.POINT
    private var cameraHandler: TC001CameraHandler? = null
    private var currentThermalBitmap: Bitmap? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentVideoFile: File? = null
    
    suspend fun startThermalCapture(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting thermal capture for TC001")
            
            // Initialize camera handler
            cameraHandler = TC001CameraHandler(context) { bitmap ->
                currentThermalBitmap = bitmap
                frameCallback(bitmap)
            }
            
            val initialized = cameraHandler?.initialize() ?: false
            if (!initialized) {
                Log.e(TAG, "Failed to initialize camera handler")
                return@withContext false
            }
            
            // Connect to TC001 device (mock connection for demo)
            val connected = cameraHandler?.connectToDevice(null) ?: false
            if (!connected) {
                Log.e(TAG, "Failed to connect to TC001 device")
                return@withContext false
            }
            
            // Start thermal preview
            val started = cameraHandler?.startPreview() ?: false
            if (!started) {
                Log.e(TAG, "Failed to start thermal preview")
                return@withContext false
            }
            
            isCapturing = true
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start thermal capture", e)
            false
        }
    }
    
    fun pauseCapture() {
        Log.d(TAG, "Pausing thermal capture")
        // Pause camera preview but keep connection
    }
    
    fun resumeCapture() {
        Log.d(TAG, "Resuming thermal capture")
        // Resume camera preview
    }
    
    fun setTemperatureMeasureMode(mode: TempMode) {
        currentTempMode = mode
        Log.d(TAG, "Temperature measurement mode set to: $mode")
        // TODO: Configure TC001 temperature measurement mode
    }
    
    suspend fun captureImage(): String? = withContext(Dispatchers.IO) {
        try {
            val currentBitmap = currentThermalBitmap ?: return@withContext null
            
            val filename = FileUtils.generateImageFilename()
            val imagesDir = FileUtils.getImagesDirectory(context)
            val file = File(imagesDir, filename)
            
            FileOutputStream(file).use { out ->
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            Log.i(TAG, "Thermal image captured: $filename")
            filename
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture thermal image", e)
            null
        }
    }
    
    suspend fun startRecording(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isRecording) return@withContext false
            
            Log.d(TAG, "Starting thermal recording")
            
            // Create video file
            val filename = FileUtils.generateVideoFilename()
            val videosDir = FileUtils.getVideosDirectory(context)
            currentVideoFile = File(videosDir, filename)
            
            // Setup MediaRecorder for mock recording
            // In real implementation, this would record from TC001 thermal stream
            mediaRecorder = MediaRecorder().apply {
                // For mock implementation, we'll just create an empty video file
                // Real TC001 integration would configure video source, format, etc.
                Log.d(TAG, "MediaRecorder configured for thermal video recording")
            }
            
            isRecording = true
            Log.i(TAG, "Thermal recording started: $filename")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            false
        }
    }
    
    suspend fun stopRecording(): String? = withContext(Dispatchers.IO) {
        try {
            if (!isRecording) return@withContext null
            
            isRecording = false
            
            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error stopping MediaRecorder", e)
                }
            }
            mediaRecorder = null
            
            val filename = currentVideoFile?.name
            currentVideoFile = null
            
            Log.i(TAG, "Thermal recording stopped: $filename")
            
            filename
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            currentVideoFile = null
            null
        }
    }
    
    fun isRecording(): Boolean = isRecording
    
    fun cleanup() {
        isCapturing = false
        isRecording = false
        
        mediaRecorder?.apply {
            try {
                if (isRecording) stop()
                release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing MediaRecorder", e)
            }
        }
        mediaRecorder = null
        
        cameraHandler?.cleanup()
        cameraHandler = null
        currentThermalBitmap?.recycle()
        currentThermalBitmap = null
        currentVideoFile = null
        Log.d(TAG, "TC001 thermal manager cleaned up")
    }
}