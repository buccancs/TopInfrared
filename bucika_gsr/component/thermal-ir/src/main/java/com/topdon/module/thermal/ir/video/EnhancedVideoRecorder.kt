package com.topdon.module.thermal.ir.video

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.hardware.camera2.*
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.media.MediaRecorder.VideoEncoder
import android.media.MediaRecorder.AudioEncoder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import com.elvishew.xlog.XLog
import com.topdon.lib.core.config.FileConfig
import com.topdon.module.thermal.ir.overlay.TemperatureOverlayManager
import com.topdon.module.thermal.ir.dng.DNGCaptureManager
import java.io.File
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Enhanced Video Recorder for bucika_gsr
 * Supports Samsung specific 4K30FPS recording and STAGE 3/LEVEL 3 RAD DNG recording
 */
@SuppressLint("MissingPermission")
class EnhancedVideoRecorder(
    private val context: Context,
    private val thermalView: TextureView?,
    private val visualView: TextureView?
) {

    // Recording modes
    enum class RecordingMode {
        SAMSUNG_4K_30FPS,
        RAD_DNG_LEVEL3_30FPS,  // Updated to DNG format
        PARALLEL_DUAL_STREAM
    }

    // Recording state
    private var isRecording = false
    private var recordingMode: RecordingMode = RecordingMode.SAMSUNG_4K_30FPS

    // Camera2 API components
    private var cameraManager: CameraManager? = null
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null
    private val cameraOpenCloseLock = Semaphore(1)

    // MediaRecorder components
    private var mediaRecorder: MediaRecorder? = null
    private var thermalRecorder: MediaRecorder? = null
    private var visualRecorder: MediaRecorder? = null
    
    // DNG Capture Manager for RAD DNG Level 3
    private var dngCaptureManager: DNGCaptureManager? = null

    // Recording parameters
    private val samsung4KSize = Size(3840, 2160) // 4K UHD
    private val radDngSize = Size(1920, 1080)    // Full HD for RAD DNG
    private val targetFps = 30

    // File paths
    private var currentVideoFile: File? = null
    private var currentThermalFile: File? = null

    // Temperature overlay manager
    private val overlayManager = TemperatureOverlayManager()

    companion object {
        private const val TAG = "EnhancedVideoRecorder"
        
        // Samsung specific camera characteristics
        private const val SAMSUNG_CAMERA_ID = "0"
        
        // STAGE 3/LEVEL 3 RAD DNG specifications
        private const val RAD_DNG_BITRATE = 8000000 // 8 Mbps for high quality
        private const val SAMSUNG_4K_BITRATE = 20000000 // 20 Mbps for 4K
    }

    init {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        dngCaptureManager = DNGCaptureManager(context)
    }

    /**
     * Start enhanced recording with specified mode
     */
    fun startRecording(mode: RecordingMode): Boolean {
        if (isRecording) {
            XLog.w(TAG, "Recording already in progress")
            return false
        }

        recordingMode = mode
        
        return try {
            setupBackgroundThread()
            
            when (mode) {
                RecordingMode.SAMSUNG_4K_30FPS -> startSamsung4KRecording()
                RecordingMode.RAD_DNG_LEVEL3_30FPS -> startRadDngRecording()  // Updated method
                RecordingMode.PARALLEL_DUAL_STREAM -> startParallelRecording()
            }
        } catch (e: Exception) {
            XLog.e(TAG, "Failed to start recording: ${e.message}", e)
            false
        }
    }

    /**
     * Start Samsung specific 4K 30FPS recording
     */
    private fun startSamsung4KRecording(): Boolean {
        XLog.i(TAG, "Starting Samsung 4K 30FPS recording")
        
        val videoFile = createVideoFile("samsung_4k_${Date().time}.mp4")
        currentVideoFile = videoFile
        
        mediaRecorder = MediaRecorder().apply {
            // Samsung optimized settings for 4K recording
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(videoFile.absolutePath)
            
            // 4K video settings optimized for Samsung devices
            setVideoEncoder(VideoEncoder.H264)
            setVideoSize(samsung4KSize.width, samsung4KSize.height)
            setVideoFrameRate(targetFps)
            setVideoEncodingBitRate(SAMSUNG_4K_BITRATE)
            
            // Audio settings
            setAudioEncoder(AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            
            try {
                prepare()
                start()
                isRecording = true
                XLog.i(TAG, "Samsung 4K recording started successfully")
                return true
            } catch (e: Exception) {
                XLog.e(TAG, "Failed to start Samsung 4K recording: ${e.message}", e)
                releaseMediaRecorder()
                return false
            }
        }
        
        return false
    }

    private fun startRadDngRecording(): Boolean {
        XLog.i(TAG, "Starting STAGE 3/LEVEL 3 RAD DNG recording at 30FPS")
        
        return try {
            val success = dngCaptureManager?.startDNGCapture() ?: false
            if (success) {
                isRecording = true
                XLog.i(TAG, "RAD DNG Level 3 capture started successfully")
            } else {
                XLog.e(TAG, "Failed to start RAD DNG capture")
            }
            success
        } catch (e: Exception) {
            XLog.e(TAG, "Failed to start RAD DNG recording: ${e.message}", e)
            false
        }
    }

    /**
     * Start parallel dual stream recording (thermal + visual)
     */
    private fun startParallelRecording(): Boolean {
        XLog.i(TAG, "Starting parallel dual stream recording")
        
        val thermalFile = createVideoFile("thermal_${Date().time}.mp4")
        val visualFile = createVideoFile("visual_${Date().time}.mp4")
        
        currentVideoFile = visualFile
        currentThermalFile = thermalFile
        
        // Setup thermal stream recorder
        thermalRecorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(thermalFile.absolutePath)
            
            setVideoEncoder(VideoEncoder.H264)
            setVideoSize(radDngSize.width, radDngSize.height)
            setVideoFrameRate(targetFps)
            setVideoEncodingBitRate(RAD_DNG_BITRATE)
        }
        
        // Setup visual stream recorder
        visualRecorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(visualFile.absolutePath)
            
            setVideoEncoder(VideoEncoder.H264)
            setVideoSize(samsung4KSize.width, samsung4KSize.height)
            setVideoFrameRate(targetFps)
            setVideoEncodingBitRate(SAMSUNG_4K_BITRATE)
            
            setAudioEncoder(AudioEncoder.AAC)
            setAudioSamplingRate(48000)
            setAudioEncodingBitRate(192000)
        }
        
        return try {
            thermalRecorder?.prepare()
            visualRecorder?.prepare()
            
            thermalRecorder?.start()
            visualRecorder?.start()
            
            isRecording = true
            XLog.i(TAG, "Parallel dual stream recording started successfully")
            true
        } catch (e: Exception) {
            XLog.e(TAG, "Failed to start parallel recording: ${e.message}", e)
            releaseAllRecorders()
            false
        }
    }

    /**
     * Stop recording
     */
    fun stopRecording(): Boolean {
        if (!isRecording) {
            XLog.w(TAG, "No recording in progress")
            return false
        }
        
        return try {
            when (recordingMode) {
                RecordingMode.PARALLEL_DUAL_STREAM -> stopParallelRecording()
                RecordingMode.RAD_DNG_LEVEL3_30FPS -> stopDNGRecording()  // Updated method
                else -> stopSingleRecording()
            }
            
            isRecording = false
            stopBackgroundThread()
            
            XLog.i(TAG, "Recording stopped successfully")
            true
        } catch (e: Exception) {
            XLog.e(TAG, "Failed to stop recording: ${e.message}", e)
            false
        }
    }

    private fun stopDNGRecording() {
        val success = dngCaptureManager?.stopDNGCapture() ?: false
        if (success) {
            XLog.i(TAG, "DNG capture stopped successfully")
        } else {
            XLog.w(TAG, "Failed to stop DNG capture cleanly")
        }
    }

    private fun stopSingleRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
    }

    private fun stopParallelRecording() {
        thermalRecorder?.apply {
            stop()
            release()
        }
        visualRecorder?.apply {
            stop()
            release()
        }
        thermalRecorder = null
        visualRecorder = null
    }

    /**
     * Add temperature measurement overlay to frame
     */
    fun addTemperatureOverlay(
        frame: Bitmap, 
        temperature: Float, 
        x: Float, 
        y: Float,
        gsrValue: Double? = null,
        skinTemperature: Double? = null
    ): Bitmap {
        val mode = when (recordingMode) {
            RecordingMode.SAMSUNG_4K_30FPS -> "Samsung 4K"
            RecordingMode.RAD_DNG_LEVEL3_30FPS -> "RAD DNG Level 3"
            RecordingMode.PARALLEL_DUAL_STREAM -> "Parallel Dual"
        }
        
        return overlayManager.addOverlayToFrame(
            frame, temperature, x, y, gsrValue, skinTemperature,
            System.currentTimeMillis(), mode
        )
    }

    /**
     * Add dual stream overlays for parallel recording
     */
    fun addDualStreamOverlays(
        thermalFrame: Bitmap,
        visualFrame: Bitmap,
        temperature: Float,
        x: Float,
        y: Float,
        gsrValue: Double? = null,
        skinTemperature: Double? = null
    ): Pair<Bitmap, Bitmap> {
        return overlayManager.addDualStreamOverlay(
            thermalFrame, visualFrame, temperature, x, y, gsrValue, skinTemperature
        )
    }

    /**
     * Get current recording status
     */
    fun isRecording(): Boolean = isRecording

    /**
     * Get current recording mode
     */
    fun getRecordingMode(): RecordingMode = recordingMode

    /**
     * Get recorded files (includes both video files and DNG files)
     */
    fun getRecordedFiles(): List<File> {
        val files = mutableListOf<File>()
        currentVideoFile?.let { files.add(it) }
        currentThermalFile?.let { files.add(it) }
        
        // Add DNG files if in DNG recording mode
        if (recordingMode == RecordingMode.RAD_DNG_LEVEL3_30FPS) {
            val dngFiles = dngCaptureManager?.getCapturedFiles() ?: emptyList()
            files.addAll(dngFiles)
        }
        
        return files
    }
    
    /**
     * Get DNG capture statistics (frames captured, FPS, etc.)
     */
    fun getDNGCaptureStats(): Map<String, Any> {
        return dngCaptureManager?.getCaptureStats() ?: emptyMap()
    }

    private fun createVideoFile(filename: String): File {
        val videoDir = File(FileConfig.lineGalleryDir, "enhanced_recordings")
        if (!videoDir.exists()) {
            videoDir.mkdirs()
        }
        return File(videoDir, filename)
    }

    private fun setupBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground")
        backgroundThread?.start()
        backgroundHandler = Handler(backgroundThread?.looper!!)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            XLog.e(TAG, "Error stopping background thread", e)
        }
    }

    private fun releaseMediaRecorder() {
        mediaRecorder?.release()
        mediaRecorder = null
    }

    private fun releaseAllRecorders() {
        releaseMediaRecorder()
        thermalRecorder?.release()
        thermalRecorder = null
        visualRecorder?.release()
        visualRecorder = null
    }

    /**
     * Cleanup resources
     */
    fun cleanup() {
        stopRecording()
        releaseAllRecorders()
        dngCaptureManager?.cleanup()
        stopBackgroundThread()
    }
}