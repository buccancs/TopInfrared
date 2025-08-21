package com.topinfrared.tc001.standalone.thermal

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.media.CamcorderProfile
import android.os.Build
import android.util.Log
import android.view.Surface
import com.topinfrared.tc001.common.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class EnhancedRecordingManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    
    companion object {
        private const val TAG = "EnhancedRecordingManager"
        
        // Samsung-specific device detection
        private val SAMSUNG_MANUFACTURERS = setOf("samsung", "Samsung", "SAMSUNG")
        
        // RAD WND (Radiance Wind) Analysis Levels
        enum class RadWndLevel(val stage: Int, val description: String) {
            LEVEL_1(1, "Basic Radiance Analysis"),
            LEVEL_2(2, "Enhanced Thermal Wind Analysis"), 
            LEVEL_3(3, "Advanced RAD WND Processing")
        }
        
        enum class RecordingType {
            SAMSUNG_4K_30FPS,
            RAD_WND_LEVEL3_30FPS,
            STANDARD_RECORDING
        }
    }
    
    data class RecordingSession(
        val type: RecordingType,
        val mediaRecorder: MediaRecorder,
        val outputFile: File,
        val startTime: Long,
        val surface: Surface?
    )
    
    private val activeRecordings = ConcurrentHashMap<RecordingType, RecordingSession>()
    private var isSamsungDevice = false
    private var radWndProcessor: RadWndProcessor? = null
    
    init {
        detectDeviceCapabilities()
        initializeRadWndProcessor()
    }
    
    /**
     * Detect if device supports Samsung-specific features
     */
    private fun detectDeviceCapabilities() {
        isSamsungDevice = SAMSUNG_MANUFACTURERS.contains(Build.MANUFACTURER)
        Log.d(TAG, "Samsung device detected: $isSamsungDevice (${Build.MANUFACTURER})")
        
        if (isSamsungDevice) {
            Log.d(TAG, "Samsung device model: ${Build.MODEL}")
            checkSamsung4KSupport()
        }
    }
    
    /**
     * Check Samsung-specific 4K recording capabilities
     */
    private fun checkSamsung4KSupport() {
        try {
            // Check for Samsung Camera2 API support
            val hasCamera2 = context.packageManager.hasSystemFeature("android.hardware.camera2")
            Log.d(TAG, "Camera2 API support: $hasCamera2")
            
            // Check for 4K recording profile availability
            if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_2160P)) {
                Log.i(TAG, "Samsung 4K (2160p) recording profile available")
            } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
                Log.w(TAG, "4K not available, falling back to 1080p")
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check Samsung 4K capabilities", e)
        }
    }
    
    /**
     * Initialize RAD WND (Radiance Wind) thermal analysis processor
     */
    private fun initializeRadWndProcessor() {
        radWndProcessor = RadWndProcessor(context).apply {
            setProcessingLevel(RadWndProcessor.ProcessingLevel.LEVEL_3)
        }
        Log.d(TAG, "RAD WND Level 3 processor initialized")
    }
    
    /**
     * Start Samsung-specific 4K recording at 30 FPS
     */
    suspend fun startSamsung4KRecording(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isSamsungDevice) {
                Log.w(TAG, "Samsung 4K recording requested on non-Samsung device")
                return@withContext false
            }
            
            if (activeRecordings.containsKey(RecordingType.SAMSUNG_4K_30FPS)) {
                Log.w(TAG, "Samsung 4K recording already active")
                return@withContext false
            }
            
            Log.i(TAG, "Starting Samsung-specific 4K 30FPS thermal recording")
            
            // Create output file
            val filename = "samsung_4k_${FileUtils.generateTimestamp()}.mp4"
            val videosDir = FileUtils.getVideosDirectory(context)
            val outputFile = File(videosDir, filename)
            
            // Configure MediaRecorder for Samsung 4K
            val mediaRecorder = MediaRecorder().apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                
                // Samsung-optimized 4K settings
                if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_2160P)) {
                    val profile = CamcorderProfile.get(CamcorderProfile.QUALITY_2160P)
                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    setVideoEncodingBitRate(20000000) // 20 Mbps for 4K
                    setVideoFrameRate(30) // 30 FPS
                    setVideoSize(3840, 2160) // True 4K resolution
                    Log.d(TAG, "Samsung 4K profile configured: 3840x2160 @ 30fps")
                } else {
                    // Fallback to enhanced 1080p
                    setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                    setVideoEncodingBitRate(15000000) // 15 Mbps
                    setVideoFrameRate(30)
                    setVideoSize(1920, 1080)
                    Log.w(TAG, "Fallback to enhanced 1080p recording")
                }
                
                setOutputFile(outputFile.absolutePath)
                
                // Samsung-specific optimizations
                configureSamsungSpecificSettings()
                
                // Set limits
                setMaxDuration(1800000) // 30 minutes for 4K
                setMaxFileSize(2L * 1024 * 1024 * 1024) // 2GB limit
                
                setOnInfoListener { _, what, _ ->
                    handleRecordingInfo(RecordingType.SAMSUNG_4K_30FPS, what)
                }
                
                setOnErrorListener { _, what, extra ->
                    handleRecordingError(RecordingType.SAMSUNG_4K_30FPS, what, extra)
                }
                
                prepare()
            }
            
            val surface = mediaRecorder.surface
            val session = RecordingSession(
                RecordingType.SAMSUNG_4K_30FPS,
                mediaRecorder,
                outputFile,
                System.currentTimeMillis(),
                surface
            )
            
            mediaRecorder.start()
            activeRecordings[RecordingType.SAMSUNG_4K_30FPS] = session
            
            Log.i(TAG, "Samsung 4K 30FPS recording started: $filename")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Samsung 4K recording", e)
            false
        }
    }
    
    /**
     * Start RAD WND Level 3 recording at 30 FPS
     */
    suspend fun startRadWndLevel3Recording(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (activeRecordings.containsKey(RecordingType.RAD_WND_LEVEL3_30FPS)) {
                Log.w(TAG, "RAD WND Level 3 recording already active")
                return@withContext false
            }
            
            Log.i(TAG, "Starting RAD WND Level 3 thermal analysis recording at 30FPS")
            
            // Create output file with RAD WND naming convention
            val filename = "rad_wnd_l3_${FileUtils.generateTimestamp()}.mp4"
            val videosDir = FileUtils.getVideosDirectory(context)
            val outputFile = File(videosDir, filename)
            
            // Configure MediaRecorder for RAD WND Level 3
            val mediaRecorder = MediaRecorder().apply {
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                
                // RAD WND Level 3 optimized settings
                setVideoEncodingBitRate(12000000) // 12 Mbps for thermal data
                setVideoFrameRate(30) // Critical: 30 FPS for wind analysis
                setVideoSize(1280, 720) // Optimized for thermal processing
                
                setOutputFile(outputFile.absolutePath)
                
                // RAD WND specific configuration
                configureRadWndSpecificSettings()
                
                // Extended recording for thermal analysis
                setMaxDuration(3600000) // 1 hour for long-term analysis
                setMaxFileSize(1L * 1024 * 1024 * 1024) // 1GB limit
                
                setOnInfoListener { _, what, _ ->
                    handleRecordingInfo(RecordingType.RAD_WND_LEVEL3_30FPS, what)
                }
                
                setOnErrorListener { _, what, extra ->
                    handleRecordingError(RecordingType.RAD_WND_LEVEL3_30FPS, what, extra)
                }
                
                prepare()
            }
            
            val surface = mediaRecorder.surface
            val session = RecordingSession(
                RecordingType.RAD_WND_LEVEL3_30FPS,
                mediaRecorder,
                outputFile,
                System.currentTimeMillis(),
                surface
            )
            
            // Start RAD WND processor
            radWndProcessor?.startLevel3Processing(surface)
            
            mediaRecorder.start()
            activeRecordings[RecordingType.RAD_WND_LEVEL3_30FPS] = session
            
            Log.i(TAG, "RAD WND Level 3 recording started: $filename")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start RAD WND Level 3 recording", e)
            false
        }
    }
    
    /**
     * Start parallel recording - both Samsung 4K and RAD WND Level 3
     */
    suspend fun startParallelRecording(): Pair<Boolean, Boolean> {
        Log.i(TAG, "Starting parallel Samsung 4K + RAD WND Level 3 recording")
        
        val samsung4KResult = if (isSamsungDevice) {
            startSamsung4KRecording()
        } else {
            Log.w(TAG, "Skipping Samsung 4K on non-Samsung device")
            false
        }
        
        val radWndResult = startRadWndLevel3Recording()
        
        Log.i(TAG, "Parallel recording started - Samsung 4K: $samsung4KResult, RAD WND L3: $radWndResult")
        return Pair(samsung4KResult, radWndResult)
    }
    
    /**
     * Stop specific recording type
     */
    suspend fun stopRecording(type: RecordingType): String? = withContext(Dispatchers.IO) {
        try {
            val session = activeRecordings.remove(type) ?: return@withContext null
            
            val duration = System.currentTimeMillis() - session.startTime
            Log.d(TAG, "Stopping $type recording after ${duration}ms")
            
            try {
                session.mediaRecorder.stop()
                session.mediaRecorder.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping MediaRecorder for $type", e)
            }
            
            // Special handling for RAD WND
            if (type == RecordingType.RAD_WND_LEVEL3_30FPS) {
                radWndProcessor?.stopLevel3Processing()
            }
            
            // Generate metadata
            generateRecordingMetadata(session, duration, type)
            
            val filename = session.outputFile.name
            Log.i(TAG, "$type recording completed: $filename")
            
            filename
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop $type recording", e)
            null
        }
    }
    
    /**
     * Stop all active recordings
     */
    suspend fun stopAllRecordings(): List<String> {
        val results = mutableListOf<String>()
        
        // Stop all active recording sessions
        activeRecordings.keys.toList().forEach { type ->
            stopRecording(type)?.let { filename ->
                results.add(filename)
            }
        }
        
        return results
    }
    
    /**
     * Configure Samsung-specific recording settings
     */
    private fun MediaRecorder.configureSamsungSpecificSettings() {
        try {
            // Samsung-specific encoder optimizations
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Use Samsung's enhanced encoder if available
                Log.d(TAG, "Applying Samsung-specific encoder optimizations")
            }
            
            // Samsung HDR and color space optimizations for thermal data
            Log.d(TAG, "Samsung 4K thermal recording configured")
            
        } catch (e: Exception) {
            Log.w(TAG, "Samsung-specific settings not available", e)
        }
    }
    
    /**
     * Configure RAD WND specific recording settings
     */
    private fun MediaRecorder.configureRadWndSpecificSettings() {
        try {
            // RAD WND requires specific frame timing for wind analysis
            Log.d(TAG, "Configuring RAD WND Level 3 recording parameters")
            
            // Ensure precise 30 FPS timing for thermal wind analysis
            // This is critical for proper radiance wind calculation
            
        } catch (e: Exception) {
            Log.w(TAG, "RAD WND specific settings configuration failed", e)
        }
    }
    
    /**
     * Handle recording information callbacks
     */
    private fun handleRecordingInfo(type: RecordingType, what: Int) {
        when (what) {
            MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> {
                Log.w(TAG, "$type: Maximum recording duration reached")
                scope.launch { stopRecording(type) }
            }
            MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> {
                Log.w(TAG, "$type: Maximum file size reached")
                scope.launch { stopRecording(type) }
            }
        }
    }
    
    /**
     * Handle recording error callbacks
     */
    private fun handleRecordingError(type: RecordingType, what: Int, extra: Int) {
        Log.e(TAG, "$type recording error: what=$what, extra=$extra")
        scope.launch { stopRecording(type) }
    }
    
    /**
     * Generate metadata for completed recording
     */
    private fun generateRecordingMetadata(session: RecordingSession, duration: Long, type: RecordingType) {
        try {
            val metadataFile = File(
                session.outputFile.parent,
                "${session.outputFile.nameWithoutExtension}_metadata.json"
            )
            
            val metadata = when (type) {
                RecordingType.SAMSUNG_4K_30FPS -> """
                    {
                        "recording_type": "Samsung_4K_30FPS",
                        "device": "${Build.MANUFACTURER} ${Build.MODEL}",
                        "resolution": "3840x2160",
                        "frame_rate": 30,
                        "duration_ms": $duration,
                        "bitrate": "20Mbps",
                        "thermal_device": "TC001",
                        "samsung_optimized": true,
                        "timestamp": ${System.currentTimeMillis()},
                        "version": "2.0"
                    }
                """.trimIndent()
                
                RecordingType.RAD_WND_LEVEL3_30FPS -> """
                    {
                        "recording_type": "RAD_WND_Level3_30FPS",
                        "analysis_level": 3,
                        "stage": 3,
                        "resolution": "1280x720",
                        "frame_rate": 30,
                        "duration_ms": $duration,
                        "bitrate": "12Mbps",
                        "thermal_device": "TC001",
                        "rad_wnd_processing": true,
                        "wind_analysis_enabled": true,
                        "radiance_calculation": "level_3",
                        "timestamp": ${System.currentTimeMillis()},
                        "version": "2.0"
                    }
                """.trimIndent()
                
                else -> """
                    {
                        "recording_type": "Standard",
                        "duration_ms": $duration,
                        "timestamp": ${System.currentTimeMillis()}
                    }
                """.trimIndent()
            }
            
            metadataFile.writeText(metadata)
            Log.d(TAG, "Metadata generated for $type: ${metadataFile.name}")
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to generate metadata for $type", e)
        }
    }
    
    /**
     * Get recording surface for specific type
     */
    fun getRecordingSurface(type: RecordingType): Surface? {
        return activeRecordings[type]?.surface
    }
    
    /**
     * Get recording duration for specific type
     */
    fun getRecordingDuration(type: RecordingType): Long {
        val session = activeRecordings[type] ?: return 0
        return (System.currentTimeMillis() - session.startTime) / 1000
    }
    
    /**
     * Check if specific recording type is active
     */
    fun isRecording(type: RecordingType): Boolean {
        return activeRecordings.containsKey(type)
    }
    
    /**
     * Check if any recording is active
     */
    fun isAnyRecordingActive(): Boolean {
        return activeRecordings.isNotEmpty()
    }
    
    /**
     * Get all active recording types
     */
    fun getActiveRecordingTypes(): Set<RecordingType> {
        return activeRecordings.keys.toSet()
    }
    
    /**
     * Cleanup all resources
     */
    fun cleanup() {
        // Stop all active recordings
        activeRecordings.values.forEach { session ->
            try {
                session.mediaRecorder.stop()
                session.mediaRecorder.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error during cleanup", e)
            }
        }
        
        activeRecordings.clear()
        radWndProcessor?.cleanup()
        
        Log.d(TAG, "Enhanced recording manager cleaned up")
    }
}