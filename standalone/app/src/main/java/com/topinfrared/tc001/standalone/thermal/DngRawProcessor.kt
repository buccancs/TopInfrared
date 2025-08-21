package com.topinfrared.tc001.standalone.thermal

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

/**
 * DNG RAW Processor for high-fidelity thermal data recording
 * 
 * DNG (Digital Negative) is Adobe's open standard for RAW image files.
 * This processor captures and processes RAW thermal data in DNG format
 * for maximum data preservation and scientific analysis capabilities.
 */
class DngRawProcessor(private val context: Context) {
    
    companion object {
        private const val TAG = "DngRawProcessor"
        
        // DNG RAW processing constants
        private const val DNG_VERSION = "1.6"
        private const val THERMAL_PRECISION = 0.01f // High precision thermal measurement
        private const val RAW_PROCESSING_FREQUENCY = 30 // 30 FPS RAW capture
        
        // DNG specific constants
        private const val DNG_BITS_PER_SAMPLE = 16 // 16-bit RAW thermal data
        private const val DNG_COMPRESSION_NONE = 1 // Uncompressed RAW
        private const val THERMAL_RANGE_MIN = -20f // Minimum thermal range (°C)
        private const val THERMAL_RANGE_MAX = 150f // Maximum thermal range (°C)
    }
    
    enum class ProcessingLevel(val level: Int, val description: String) {
        LEVEL_1(1, "Basic DNG RAW capture"),
        LEVEL_2(2, "Enhanced thermal DNG processing"),
        LEVEL_3(3, "Advanced DNG RAW with full thermal fidelity")
    }
    
    data class ThermalRawData(
        val timestamp: Long,
        val width: Int,
        val height: Int,
        val thermalData: Array<FloatArray>,
        val rawData: ByteArray,
        val metadata: ThermalMetadata
    )
    
    data class ThermalMetadata(
        val deviceType: String,
        val captureTime: Long,
        val thermalRange: Pair<Float, Float>,
        val calibrationData: String,
        val processingLevel: Int
    )
    
    private var currentLevel = ProcessingLevel.LEVEL_1
    private var isProcessingActive = false
    private var processingJob: Job? = null
    private var rawDataHistory = mutableListOf<ThermalRawData>()
    private var rawCaptureCallback: ((File) -> Unit)? = null
    
    // Level 3 specific processing variables
    private var level3Surface: Surface? = null
    private val thermalCalibrationMatrix = Array(240) { FloatArray(320) }
    
    /**
     * Set the DNG RAW processing level
     */
    fun setProcessingLevel(level: ProcessingLevel) {
        currentLevel = level
        Log.i(TAG, "DNG RAW processing level set to: ${level.description}")
        
        when (level) {
            ProcessingLevel.LEVEL_3 -> {
                Log.d(TAG, "Initializing Level 3 advanced DNG RAW processing")
                initializeLevel3Processing()
            }
            else -> {
                Log.d(TAG, "Standard DNG RAW processing initialized")
            }
        }
    }
    
    /**
     * Initialize Level 3 specific processing components
     */
    private fun initializeLevel3Processing() {
        Log.d(TAG, "Level 3 DNG RAW processor initialized with:")
        Log.d(TAG, "  - Thermal resolution: 320x240")
        Log.d(TAG, "  - Processing frequency: ${RAW_PROCESSING_FREQUENCY} FPS")
        Log.d(TAG, "  - Thermal precision: ${THERMAL_PRECISION}°C")
        Log.d(TAG, "  - DNG version: ${DNG_VERSION}")
        
        // Initialize thermal calibration matrix
        initializeThermalCalibration()
    }
    
    /**
     * Initialize thermal calibration matrix for accurate temperature measurement
     */
    private fun initializeThermalCalibration() {
        Log.d(TAG, "Initializing thermal calibration matrix")
        
        // Initialize calibration matrix with default values
        for (y in 0 until 240) {
            for (x in 0 until 320) {
                // Default calibration factor (can be loaded from device-specific calibration file)
                thermalCalibrationMatrix[y][x] = 1.0f
            }
        }
    }
    
    /**
     * Start Level 3 DNG RAW processing
     */
    fun startLevel3Processing(surface: Surface?) {
        if (currentLevel != ProcessingLevel.LEVEL_3) {
            Log.w(TAG, "Level 3 processing requested but not set to Level 3")
            return
        }
        
        level3Surface = surface
        isProcessingActive = true
        
        Log.i(TAG, "Starting Level 3 DNG RAW processing at 30 FPS")
        
        processingJob = CoroutineScope(Dispatchers.Default).launch {
            processLevel3DngRawLoop()
        }
    }
    
    /**
     * Stop Level 3 processing
     */
    fun stopLevel3Processing() {
        Log.d(TAG, "Stopping Level 3 DNG RAW processing")
        
        isProcessingActive = false
        processingJob?.cancel()
        processingJob = null
        level3Surface = null
        
        // Generate final DNG processing report
        generateLevel3ProcessingReport()
    }
    
    /**
     * Main Level 3 DNG RAW processing loop
     */
    private suspend fun processLevel3DngRawLoop() {
        val frameInterval = 1000L / RAW_PROCESSING_FREQUENCY // ~33ms for 30 FPS
        var frameCount = 0L
        
        while (isProcessingActive && currentCoroutineContext().isActive) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Generate mock thermal frame data for processing
                val thermalData = generateMockThermalData(frameCount)
                
                // Process DNG RAW Level 3 capture
                val rawData = processLevel3Frame(thermalData, startTime)
                
                // Store in history for analysis
                synchronized(rawDataHistory) {
                    rawDataHistory.add(rawData)
                    
                    // Keep only recent history (last 5 seconds at 30 FPS)
                    if (rawDataHistory.size > 150) {
                        rawDataHistory.removeAt(0)
                    }
                }
                
                // Save DNG file every 30 frames (1 second intervals)
                if (frameCount % 30 == 0L) {
                    saveDngRawFile(rawData)
                }
                
                frameCount++
                
                // Maintain precise 30 FPS timing
                val processingTime = System.currentTimeMillis() - startTime
                val sleepTime = maxOf(0L, frameInterval - processingTime)
                
                if (sleepTime > 0) {
                    delay(sleepTime)
                }
                
                // Log processing stats every 5 seconds
                if (frameCount % (RAW_PROCESSING_FREQUENCY * 5) == 0L) {
                    Log.d(TAG, "Level 3 DNG RAW: ${frameCount} frames processed, ${processingTime}ms/frame")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Level 3 DNG RAW processing", e)
                delay(frameInterval)
            }
        }
        
        Log.d(TAG, "Level 3 DNG RAW processing loop completed")
    }
    
    /**
     * Process a single frame for Level 3 DNG RAW capture
     */
    private fun processLevel3Frame(thermalData: Array<FloatArray>, timestamp: Long): ThermalRawData {
        val height = thermalData.size
        val width = if (height > 0) thermalData[0].size else 0
        
        // Apply thermal calibration
        val calibratedData = applyCalibratedThermalProcessing(thermalData)
        
        // Convert thermal data to 16-bit RAW format
        val rawData = convertToRawData(calibratedData)
        
        // Generate thermal metadata
        val metadata = ThermalMetadata(
            deviceType = "TC001",
            captureTime = timestamp,
            thermalRange = Pair(THERMAL_RANGE_MIN, THERMAL_RANGE_MAX),
            calibrationData = "TC001_CALIBRATION_V1.0",
            processingLevel = 3
        )
        
        return ThermalRawData(
            timestamp = timestamp,
            width = width,
            height = height,
            thermalData = calibratedData,
            rawData = rawData,
            metadata = metadata
        )
    }
    
    /**
     * Apply calibrated thermal processing for accurate temperature measurement
     */
    private fun applyCalibratedThermalProcessing(thermalData: Array<FloatArray>): Array<FloatArray> {
        val height = thermalData.size
        val width = if (height > 0) thermalData[0].size else 0
        
        val calibratedData = Array(height) { FloatArray(width) }
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                // Apply pixel-specific calibration
                val calibrationFactor = if (y < thermalCalibrationMatrix.size && 
                                           x < thermalCalibrationMatrix[0].size) {
                    thermalCalibrationMatrix[y][x]
                } else {
                    1.0f
                }
                
                // Apply calibration and ensure thermal range limits
                calibratedData[y][x] = (thermalData[y][x] * calibrationFactor).coerceIn(
                    THERMAL_RANGE_MIN, 
                    THERMAL_RANGE_MAX
                )
            }
        }
        
        return calibratedData
    }
    
    /**
     * Convert thermal data to 16-bit RAW format
     */
    private fun convertToRawData(thermalData: Array<FloatArray>): ByteArray {
        val height = thermalData.size
        val width = if (height > 0) thermalData[0].size else 0
        
        val rawBuffer = ByteBuffer.allocate(width * height * 2) // 16-bit = 2 bytes per pixel
        rawBuffer.order(ByteOrder.LITTLE_ENDIAN)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                // Convert temperature to 16-bit unsigned integer
                // Map thermal range to 0-65535
                val normalizedTemp = (thermalData[y][x] - THERMAL_RANGE_MIN) / 
                                   (THERMAL_RANGE_MAX - THERMAL_RANGE_MIN)
                val rawValue = (normalizedTemp * 65535f).coerceIn(0f, 65535f).toInt().toShort()
                rawBuffer.putShort(rawValue)
            }
        }
        
        return rawBuffer.array()
    }
    
    /**
     * Save thermal data as DNG RAW file
     */
    private suspend fun saveDngRawFile(rawData: ThermalRawData) = withContext(Dispatchers.IO) {
        try {
            val filename = "thermal_raw_${rawData.timestamp}.dng"
            val outputDir = File(context.getExternalFilesDir("thermal_raw"), "dng")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            val dngFile = File(outputDir, filename)
            
            // Generate DNG file content
            val dngContent = generateDngFile(rawData)
            
            // Write DNG file
            FileOutputStream(dngFile).use { fos ->
                fos.write(dngContent)
            }
            
            Log.d(TAG, "DNG RAW file saved: $filename (${dngContent.size} bytes)")
            
            // Notify callback
            rawCaptureCallback?.invoke(dngFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save DNG RAW file", e)
        }
    }
    
    /**
     * Generate DNG file content with proper TIFF header and thermal data
     */
    private fun generateDngFile(rawData: ThermalRawData): ByteArray {
        val output = ByteArrayOutputStream()
        
        try {
            // Write TIFF header for DNG
            writeTiffHeader(output)
            
            // Write DNG-specific IFD (Image File Directory)
            writeDngIfd(output, rawData)
            
            // Write RAW thermal data
            output.write(rawData.rawData)
            
            // Write DNG metadata
            writeDngMetadata(output, rawData.metadata)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating DNG file", e)
        }
        
        return output.toByteArray()
    }
    
    /**
     * Write TIFF header for DNG format
     */
    private fun writeTiffHeader(output: ByteArrayOutputStream) {
        // TIFF magic number (little-endian)
        output.write(byteArrayOf(0x49, 0x49)) // "II"
        // TIFF version
        output.write(byteArrayOf(0x2A, 0x00))
        // Offset to first IFD
        output.write(byteArrayOf(0x08, 0x00, 0x00, 0x00))
    }
    
    /**
     * Write DNG Image File Directory
     */
    private fun writeDngIfd(output: ByteArrayOutputStream, rawData: ThermalRawData) {
        // Simplified DNG IFD - in production would include full DNG specification
        val ifdEntries = 12 // Number of IFD entries
        
        // Write number of directory entries
        output.write(byteArrayOf(ifdEntries.toByte(), 0x00))
        
        // Example IFD entries (simplified)
        // In production, this would include all required DNG tags
        writeTiffTag(output, 0x0100, 4, 1, rawData.width) // ImageWidth
        writeTiffTag(output, 0x0101, 4, 1, rawData.height) // ImageLength
        writeTiffTag(output, 0x0102, 3, 1, DNG_BITS_PER_SAMPLE) // BitsPerSample
        writeTiffTag(output, 0x0103, 3, 1, DNG_COMPRESSION_NONE) // Compression
        
        // Additional thermal-specific tags would be added here
        
        // Next IFD offset (0 = no more IFDs)
        output.write(byteArrayOf(0x00, 0x00, 0x00, 0x00))
    }
    
    /**
     * Write TIFF tag entry
     */
    private fun writeTiffTag(output: ByteArrayOutputStream, tag: Int, type: Int, count: Int, value: Int) {
        // Tag
        output.write(byteArrayOf((tag and 0xFF).toByte(), (tag shr 8 and 0xFF).toByte()))
        // Type  
        output.write(byteArrayOf((type and 0xFF).toByte(), (type shr 8 and 0xFF).toByte()))
        // Count
        output.write(byteArrayOf((count and 0xFF).toByte(), (count shr 8 and 0xFF).toByte(),
                                (count shr 16 and 0xFF).toByte(), (count shr 24 and 0xFF).toByte()))
        // Value/Offset
        output.write(byteArrayOf((value and 0xFF).toByte(), (value shr 8 and 0xFF).toByte(),
                                (value shr 16 and 0xFF).toByte(), (value shr 24 and 0xFF).toByte()))
    }
    
    /**
     * Write DNG metadata
     */
    private fun writeDngMetadata(output: ByteArrayOutputStream, metadata: ThermalMetadata) {
        // Simplified metadata - in production would include full DNG metadata
        val metadataString = """
            {
                "device_type": "${metadata.deviceType}",
                "capture_time": ${metadata.captureTime},
                "thermal_range_min": ${metadata.thermalRange.first},
                "thermal_range_max": ${metadata.thermalRange.second},
                "calibration_data": "${metadata.calibrationData}",
                "processing_level": ${metadata.processingLevel},
                "dng_version": "$DNG_VERSION"
            }
        """.trimIndent()
        
        output.write(metadataString.toByteArray())
    }
    
    /**
     * Generate mock thermal data for processing demonstration
     */
    private fun generateMockThermalData(frameNumber: Long): Array<FloatArray> {
        val width = 320
        val height = 240
        val data = Array(height) { FloatArray(width) }
        
        val time = frameNumber / 30.0 // Convert to seconds at 30 FPS
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                // Create thermal pattern for RAW capture
                val centerX = width / 2f
                val centerY = height / 2f
                val distance = sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY))
                
                // Base temperature pattern
                var temp = 25f + 15f * exp(-distance / 60f)
                
                // Add temporal variation for RAW capture demonstration
                temp += 5f * sin(time * 0.2 + x * 0.01 + y * 0.01).toFloat()
                
                // Add realistic thermal noise
                temp += (Math.random().toFloat() - 0.5f) * 1.5f
                
                data[y][x] = temp.coerceIn(THERMAL_RANGE_MIN, THERMAL_RANGE_MAX)
            }
        }
        
        return data
    }
    
    /**
     * Generate Level 3 processing report
     */
    private fun generateLevel3ProcessingReport() {
        if (rawDataHistory.isEmpty()) {
            Log.i(TAG, "No DNG RAW data to generate report")
            return
        }
        
        Log.i(TAG, "=== DNG RAW Level 3 Processing Report ===")
        Log.i(TAG, "Total frames processed: ${rawDataHistory.size}")
        
        // Calculate thermal statistics
        val allTemperatures = rawDataHistory.flatMap { data ->
            data.thermalData.flatMap { row -> row.toList() }
        }
        
        val avgTemp = allTemperatures.average()
        val minTemp = allTemperatures.minOrNull() ?: 0f
        val maxTemp = allTemperatures.maxOrNull() ?: 0f
        
        Log.i(TAG, "Temperature range: ${String.format("%.2f", minTemp)}°C to ${String.format("%.2f", maxTemp)}°C")
        Log.i(TAG, "Average temperature: ${String.format("%.2f", avgTemp)}°C")
        Log.i(TAG, "DNG RAW files generated: ${rawDataHistory.size / 30}") // 1 per second
        
        Log.i(TAG, "=== End DNG RAW Level 3 Report ===")
    }
    
    /**
     * Set RAW capture callback
     */
    fun setRawCaptureCallback(callback: (File) -> Unit) {
        rawCaptureCallback = callback
    }
    
    /**
     * Get current processing level
     */
    fun getCurrentLevel(): ProcessingLevel = currentLevel
    
    /**
     * Check if processing is active
     */
    fun isProcessing(): Boolean = isProcessingActive
    
    /**
     * Get thermal calibration matrix for visualization
     */
    fun getThermalCalibrationMatrix(): Array<FloatArray> = thermalCalibrationMatrix
    
    /**
     * Cleanup processor resources
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up DNG RAW processor")
        
        isProcessingActive = false
        processingJob?.cancel()
        processingJob = null
        
        synchronized(rawDataHistory) {
            rawDataHistory.clear()
        }
        
        rawCaptureCallback = null
        level3Surface = null
        
        Log.d(TAG, "DNG RAW processor cleanup completed")
    }
}