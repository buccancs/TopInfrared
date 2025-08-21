package com.topinfrared.tc001.standalone.thermal

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import android.view.Surface
import android.media.CamcorderProfile
import android.media.MediaMetadataRetriever
import com.topinfrared.tc001.ir.camera.TC001CameraHandler
import com.topinfrared.tc001.common.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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
    private var recordingSurface: Surface? = null
    private var recordingStartTime: Long = 0
    
    // Enhanced recording capabilities
    private val enhancedRecordingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var enhancedRecordingManager: EnhancedRecordingManager? = null
    
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
            
            // Initialize enhanced recording manager
            enhancedRecordingManager = EnhancedRecordingManager(context, enhancedRecordingScope)
            
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
        
        // Configure TC001 temperature measurement mode
        configureTC001MeasurementMode(mode)
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
            
            Log.d(TAG, "Starting enhanced thermal recording with MediaRecorder")
            
            // Create video file with proper naming
            val filename = FileUtils.generateVideoFilename()
            val videosDir = FileUtils.getVideosDirectory(context)
            currentVideoFile = File(videosDir, filename)
            
            // Initialize MediaRecorder with proper configuration
            mediaRecorder = MediaRecorder().apply {
                // Set video source - for thermal camera, we'd use Camera or Surface
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                
                // Set output format
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                
                // Set video encoder and configuration
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoEncodingBitRate(8000000) // 8 Mbps for good quality
                setVideoFrameRate(30) // 30 FPS for smooth playback
                setVideoSize(640, 480) // TC001 resolution
                
                // Set output file
                setOutputFile(currentVideoFile!!.absolutePath)
                
                // Set maximum duration (10 minutes) and file size (500MB)
                setMaxDuration(600000) // 10 minutes in milliseconds
                setMaxFileSize(500 * 1024 * 1024) // 500MB
                
                setOnInfoListener { _, what, _ ->
                    when (what) {
                        MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> {
                            Log.w(TAG, "Maximum recording duration reached")
                            // Signal that recording should be stopped
                            isRecording = false
                        }
                        MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> {
                            Log.w(TAG, "Maximum file size reached")
                            // Signal that recording should be stopped
                            isRecording = false
                        }
                    }
                }
                
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaRecorder error: what=$what, extra=$extra")
                    // Handle recording error
                    isRecording = false
                }
                
                // Prepare the recorder
                prepare()
                
                // Get the surface for recording
                recordingSurface = surface
            }
            
            // Start recording
            mediaRecorder?.start()
            recordingStartTime = System.currentTimeMillis()
            isRecording = true
            
            Log.i(TAG, "Enhanced thermal recording started: $filename")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start enhanced recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            recordingSurface = null
            currentVideoFile?.delete()
            currentVideoFile = null
            false
        }
    }
    
    suspend fun stopRecording(): String? = withContext(Dispatchers.IO) {
        try {
            if (!isRecording) return@withContext null
            
            val recordingDuration = System.currentTimeMillis() - recordingStartTime
            Log.d(TAG, "Stopping recording after ${recordingDuration}ms")
            
            isRecording = false
            
            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                    Log.d(TAG, "MediaRecorder stopped and released successfully")
                } catch (e: Exception) {
                    Log.w(TAG, "Error stopping MediaRecorder", e)
                }
            }
            mediaRecorder = null
            recordingSurface = null
            
            val filename = currentVideoFile?.name
            val videoFile = currentVideoFile
            currentVideoFile = null
            
            // Verify the recorded file
            if (videoFile != null && videoFile.exists() && videoFile.length() > 0) {
                // Add metadata to the video file
                try {
                    addVideoMetadata(videoFile, recordingDuration)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to add metadata to video", e)
                }
                
                Log.i(TAG, "Enhanced thermal recording completed: $filename (${FileUtils.formatFileSize(videoFile.length())})")
            } else {
                Log.w(TAG, "Recording file is empty or missing")
            }
            
            filename
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop enhanced recording", e)
            mediaRecorder?.release()
            mediaRecorder = null
            recordingSurface = null
            currentVideoFile = null
            null
        }
    }
    
    fun isRecording(): Boolean = isRecording
    
    fun cleanup() {
        isCapturing = false
        
        // Stop recording if active
        if (isRecording) {
            isRecording = false
            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                } catch (e: Exception) {
                    Log.w(TAG, "Error stopping MediaRecorder during cleanup", e)
                }
            }
        }
        
        mediaRecorder?.release()
        mediaRecorder = null
        recordingSurface = null
        
        // Cleanup enhanced recording manager
        enhancedRecordingManager?.cleanup()
        enhancedRecordingManager = null
        
        cameraHandler?.cleanup()
        cameraHandler = null
        currentThermalBitmap?.recycle()
        currentThermalBitmap = null
        currentVideoFile = null
        recordingStartTime = 0
        Log.d(TAG, "TC001 thermal manager cleaned up")
    }
    
    /**
     * Get the recording surface for thermal data input
     */
    fun getRecordingSurface(): Surface? = recordingSurface
    
    /**
     * Get current recording duration in seconds
     */
    fun getRecordingDuration(): Long {
        return if (isRecording) {
            (System.currentTimeMillis() - recordingStartTime) / 1000
        } else 0
    }
    
    /**
     * Add metadata to recorded video file
     */
    private fun addVideoMetadata(videoFile: File, durationMs: Long) {
        try {
            // Use MediaMetadataRetriever to add thermal-specific metadata
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoFile.absolutePath)
            
            // Log video properties
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            
            Log.d(TAG, "Video metadata - Width: $width, Height: $height, Duration: $duration ms")
            
            retriever.release()
            
            // Create a companion metadata file for thermal-specific info
            val metadataFile = File(videoFile.parent, "${videoFile.nameWithoutExtension}.json")
            val metadata = """
                {
                    "device": "TC001",
                    "recording_duration_ms": $durationMs,
                    "temperature_mode": "$currentTempMode",
                    "timestamp": ${System.currentTimeMillis()},
                    "thermal_format": "mock_data",
                    "version": "1.0"
                }
            """.trimIndent()
            
            metadataFile.writeText(metadata)
            Log.d(TAG, "Thermal metadata written to: ${metadataFile.name}")
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to process video metadata", e)
        }
    }
    
    private fun configureTC001MeasurementMode(mode: TempMode) {
        try {
            // Configure TC001 device for specific measurement mode
            when (mode) {
                TempMode.POINT -> {
                    Log.d(TAG, "Configuring TC001 for point temperature measurement")
                    // Point mode: single temperature reading at cursor location
                    // In a real implementation, this would send USB commands to TC001
                }
                TempMode.LINE -> {
                    Log.d(TAG, "Configuring TC001 for line temperature measurement")
                    // Line mode: temperature profile along a line
                    // Enhanced thermal processing for line analysis
                }
                TempMode.AREA -> {
                    Log.d(TAG, "Configuring TC001 for area temperature measurement")
                    // Area mode: statistical analysis of temperature regions
                    // Configure for min/max/avg calculations
                }
            }
            
            // Apply mode-specific settings to mock thermal processing
            adjustMockThermalGeneration(mode)
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to configure TC001 measurement mode: $mode", e)
        }
    }
    
    private fun adjustMockThermalGeneration(mode: TempMode) {
        // Adjust mock thermal data generation based on measurement mode
        // This simulates how real TC001 hardware would behave differently
        when (mode) {
            TempMode.POINT -> {
                // Generate focused temperature data for point measurements
                Log.v(TAG, "Mock thermal: Enhanced point resolution")
            }
            TempMode.LINE -> {
                // Generate gradient thermal data for line profiles  
                Log.v(TAG, "Mock thermal: Enhanced line gradient data")
            }
            TempMode.AREA -> {
                // Generate varied thermal data for statistical analysis
                Log.v(TAG, "Mock thermal: Enhanced area temperature variation")
            }
        }
    }
    
    /**
     * Start Samsung-specific 4K recording at 30 FPS
     */
    suspend fun startSamsung4KRecording(): Boolean {
        return enhancedRecordingManager?.startSamsung4KRecording() ?: false
    }
    
    /**
     * Start DNG RAW Level 3 recording at 30 FPS  
     */
    suspend fun startDngRawLevel3Recording(): Boolean {
        return enhancedRecordingManager?.startDngRawLevel3Recording() ?: false
    }
    
    /**
     * Start parallel Samsung 4K + DNG RAW Level 3 recording
     */
    suspend fun startParallelRecording(): Pair<Boolean, Boolean> {
        return enhancedRecordingManager?.startParallelRecording() ?: Pair(false, false)
    }
    
    /**
     * Stop specific enhanced recording type
     */
    suspend fun stopEnhancedRecording(type: EnhancedRecordingManager.Companion.RecordingType): String? {
        return enhancedRecordingManager?.stopRecording(type)
    }
    
    /**
     * Stop all enhanced recordings
     */
    suspend fun stopAllEnhancedRecordings(): List<String> {
        return enhancedRecordingManager?.stopAllRecordings() ?: emptyList()
    }
    
    /**
     * Check if specific enhanced recording is active
     */
    fun isEnhancedRecordingActive(type: EnhancedRecordingManager.Companion.RecordingType): Boolean {
        return enhancedRecordingManager?.isRecording(type) ?: false
    }
    
    /**
     * Get enhanced recording duration for specific type
     */
    fun getEnhancedRecordingDuration(type: EnhancedRecordingManager.Companion.RecordingType): Long {
        return enhancedRecordingManager?.getRecordingDuration(type) ?: 0
    }
    
    /**
     * Check if any enhanced recording is active
     */
    fun isAnyEnhancedRecordingActive(): Boolean {
        return enhancedRecordingManager?.isAnyRecordingActive() ?: false
    }
    
    /**
     * Get all active enhanced recording types
     */
    fun getActiveEnhancedRecordingTypes(): Set<EnhancedRecordingManager.Companion.RecordingType> {
        return enhancedRecordingManager?.getActiveRecordingTypes() ?: emptySet()
    }
}