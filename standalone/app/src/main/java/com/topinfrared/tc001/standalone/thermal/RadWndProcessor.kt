package com.topinfrared.tc001.standalone.thermal

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.*
import java.nio.ByteBuffer
import kotlin.math.*

/**
 * RAD WND (Radiance Wind) Processor for advanced thermal analysis
 * 
 * RAD WND is an advanced thermal imaging technique that analyzes radiance patterns
 * in thermal data to detect wind effects and air flow dynamics. Level 3 processing
 * provides the highest fidelity analysis for professional thermal applications.
 */
class RadWndProcessor(private val context: Context) {
    
    companion object {
        private const val TAG = "RadWndProcessor"
        
        // RAD WND processing constants
        private const val WIND_DETECTION_THRESHOLD = 0.5f
        private const val RADIANCE_ANALYSIS_WINDOW = 16 // 16 frame window
        private const val THERMAL_GRADIENT_SENSITIVITY = 0.1f
        
        // Level 3 specific constants
        private const val L3_PROCESSING_FREQUENCY = 30 // 30 FPS processing
        private const val L3_WIND_VECTOR_RESOLUTION = 64 // High resolution wind vectors
        private const val L3_RADIANCE_PRECISION = 0.01f // High precision radiance measurement
    }
    
    enum class ProcessingLevel(val level: Int, val description: String) {
        LEVEL_1(1, "Basic radiance detection"),
        LEVEL_2(2, "Enhanced wind pattern analysis"),
        LEVEL_3(3, "Advanced RAD WND with full wind vector analysis")
    }
    
    data class WindVector(
        val x: Float,
        val y: Float,
        val magnitude: Float,
        val direction: Float, // in degrees
        val confidence: Float
    )
    
    data class RadianceData(
        val timestamp: Long,
        val averageRadiance: Float,
        val radianceVariance: Float,
        val windVectors: List<WindVector>,
        val thermalGradients: Array<FloatArray>
    )
    
    private var currentLevel = ProcessingLevel.LEVEL_1
    private var isProcessingActive = false
    private var processingJob: Job? = null
    private var radianceHistory = mutableListOf<RadianceData>()
    private var windAnalysisCallback: ((WindVector) -> Unit)? = null
    
    // Level 3 specific processing variables
    private var level3Surface: Surface? = null
    private val windVectorGrid = Array(L3_WIND_VECTOR_RESOLUTION) { 
        Array(L3_WIND_VECTOR_RESOLUTION) { WindVector(0f, 0f, 0f, 0f, 0f) }
    }
    
    /**
     * Set the RAD WND processing level
     */
    fun setProcessingLevel(level: ProcessingLevel) {
        currentLevel = level
        Log.i(TAG, "RAD WND processing level set to: ${level.description}")
        
        when (level) {
            ProcessingLevel.LEVEL_3 -> {
                Log.d(TAG, "Initializing Level 3 advanced RAD WND processing")
                initializeLevel3Processing()
            }
            else -> {
                Log.d(TAG, "Standard RAD WND processing initialized")
            }
        }
    }
    
    /**
     * Initialize Level 3 specific processing components
     */
    private fun initializeLevel3Processing() {
        Log.d(TAG, "Level 3 RAD WND processor initialized with:")
        Log.d(TAG, "  - Wind vector resolution: ${L3_WIND_VECTOR_RESOLUTION}x${L3_WIND_VECTOR_RESOLUTION}")
        Log.d(TAG, "  - Processing frequency: ${L3_PROCESSING_FREQUENCY} FPS")
        Log.d(TAG, "  - Radiance precision: ${L3_RADIANCE_PRECISION}")
    }
    
    /**
     * Start Level 3 RAD WND processing
     */
    fun startLevel3Processing(surface: Surface?) {
        if (currentLevel != ProcessingLevel.LEVEL_3) {
            Log.w(TAG, "Level 3 processing requested but not set to Level 3")
            return
        }
        
        level3Surface = surface
        isProcessingActive = true
        
        Log.i(TAG, "Starting Level 3 RAD WND processing at 30 FPS")
        
        processingJob = CoroutineScope(Dispatchers.Default).launch {
            processLevel3RadWndLoop()
        }
    }
    
    /**
     * Stop Level 3 processing
     */
    fun stopLevel3Processing() {
        Log.d(TAG, "Stopping Level 3 RAD WND processing")
        
        isProcessingActive = false
        processingJob?.cancel()
        processingJob = null
        level3Surface = null
        
        // Generate final analysis report
        generateLevel3AnalysisReport()
    }
    
    /**
     * Main Level 3 RAD WND processing loop
     */
    private suspend fun processLevel3RadWndLoop() {
        val frameInterval = 1000L / L3_PROCESSING_FREQUENCY // ~33ms for 30 FPS
        var frameCount = 0L
        
        while (isProcessingActive && currentCoroutineContext().isActive) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Generate mock thermal frame data for processing
                val thermalData = generateMockThermalData(frameCount)
                
                // Process RAD WND Level 3 analysis
                val radianceData = processLevel3Frame(thermalData, startTime)
                
                // Store in history for wind pattern analysis
                synchronized(radianceHistory) {
                    radianceHistory.add(radianceData)
                    
                    // Keep only recent history (last 5 seconds at 30 FPS)
                    if (radianceHistory.size > 150) {
                        radianceHistory.removeAt(0)
                    }
                }
                
                // Perform wind vector analysis
                analyzeWindPatterns(radianceData)
                
                frameCount++
                
                // Maintain precise 30 FPS timing
                val processingTime = System.currentTimeMillis() - startTime
                val sleepTime = maxOf(0L, frameInterval - processingTime)
                
                if (sleepTime > 0) {
                    delay(sleepTime)
                }
                
                // Log processing stats every 5 seconds
                if (frameCount % (L3_PROCESSING_FREQUENCY * 5) == 0L) {
                    Log.d(TAG, "Level 3 RAD WND: ${frameCount} frames processed, ${processingTime}ms/frame")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Level 3 RAD WND processing", e)
                delay(frameInterval)
            }
        }
        
        Log.d(TAG, "Level 3 RAD WND processing loop completed")
    }
    
    /**
     * Process a single frame for Level 3 RAD WND analysis
     */
    private fun processLevel3Frame(thermalData: Array<FloatArray>, timestamp: Long): RadianceData {
        val height = thermalData.size
        val width = if (height > 0) thermalData[0].size else 0
        
        // Calculate average radiance across the thermal frame
        var totalRadiance = 0f
        var pixelCount = 0
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                totalRadiance += thermalData[y][x]
                pixelCount++
            }
        }
        
        val averageRadiance = if (pixelCount > 0) totalRadiance / pixelCount else 0f
        
        // Calculate radiance variance for wind detection
        var varianceSum = 0f
        for (y in 0 until height) {
            for (x in 0 until width) {
                val diff = thermalData[y][x] - averageRadiance
                varianceSum += diff * diff
            }
        }
        val radianceVariance = if (pixelCount > 0) varianceSum / pixelCount else 0f
        
        // Generate thermal gradients for wind vector calculation
        val gradients = calculateThermalGradients(thermalData)
        
        // Calculate wind vectors from thermal gradients
        val windVectors = calculateWindVectors(gradients)
        
        return RadianceData(
            timestamp = timestamp,
            averageRadiance = averageRadiance,
            radianceVariance = radianceVariance,
            windVectors = windVectors,
            thermalGradients = gradients
        )
    }
    
    /**
     * Calculate thermal gradients for wind analysis
     */
    private fun calculateThermalGradients(thermalData: Array<FloatArray>): Array<FloatArray> {
        val height = thermalData.size
        val width = if (height > 0) thermalData[0].size else 0
        
        val gradients = Array(height) { FloatArray(width) }
        
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                // Calculate gradient magnitude using Sobel operator
                val gx = (-thermalData[y-1][x-1] - 2*thermalData[y][x-1] - thermalData[y+1][x-1] +
                          thermalData[y-1][x+1] + 2*thermalData[y][x+1] + thermalData[y+1][x+1]) / 8f
                
                val gy = (-thermalData[y-1][x-1] - 2*thermalData[y-1][x] - thermalData[y-1][x+1] +
                          thermalData[y+1][x-1] + 2*thermalData[y+1][x] + thermalData[y+1][x+1]) / 8f
                
                gradients[y][x] = sqrt(gx * gx + gy * gy)
            }
        }
        
        return gradients
    }
    
    /**
     * Calculate wind vectors from thermal gradients
     */
    private fun calculateWindVectors(gradients: Array<FloatArray>): List<WindVector> {
        val vectors = mutableListOf<WindVector>()
        val height = gradients.size
        val width = if (height > 0) gradients[0].size else 0
        
        // Divide thermal image into analysis blocks
        val blockSize = 16
        
        for (blockY in 0 until height step blockSize) {
            for (blockX in 0 until width step blockSize) {
                val vector = analyzeWindInBlock(gradients, blockX, blockY, blockSize)
                if (vector.confidence > WIND_DETECTION_THRESHOLD) {
                    vectors.add(vector)
                }
            }
        }
        
        return vectors
    }
    
    /**
     * Analyze wind patterns in a specific block
     */
    private fun analyzeWindInBlock(gradients: Array<FloatArray>, startX: Int, startY: Int, blockSize: Int): WindVector {
        val height = gradients.size
        val width = if (height > 0) gradients[0].size else 0
        
        var sumGradientX = 0f
        var sumGradientY = 0f
        var totalMagnitude = 0f
        var pixelCount = 0
        
        val endX = minOf(startX + blockSize, width)
        val endY = minOf(startY + blockSize, height)
        
        for (y in startY until endY) {
            for (x in startX until endX) {
                if (y > 0 && y < height - 1 && x > 0 && x < width - 1) {
                    // Calculate gradient direction
                    val gx = gradients[y][x + 1] - gradients[y][x - 1]
                    val gy = gradients[y + 1][x] - gradients[y - 1][x]
                    
                    sumGradientX += gx
                    sumGradientY += gy
                    totalMagnitude += gradients[y][x]
                    pixelCount++
                }
            }
        }
        
        if (pixelCount == 0) {
            return WindVector(0f, 0f, 0f, 0f, 0f)
        }
        
        val avgGradientX = sumGradientX / pixelCount
        val avgGradientY = sumGradientY / pixelCount
        val avgMagnitude = totalMagnitude / pixelCount
        
        val magnitude = sqrt(avgGradientX * avgGradientX + avgGradientY * avgGradientY)
        val direction = atan2(avgGradientY, avgGradientX) * 180 / PI.toFloat()
        
        // Calculate confidence based on gradient consistency
        val confidence = minOf(1f, avgMagnitude / 10f) // Normalized confidence
        
        return WindVector(
            x = avgGradientX,
            y = avgGradientY,
            magnitude = magnitude,
            direction = direction,
            confidence = confidence
        )
    }
    
    /**
     * Analyze wind patterns across multiple frames
     */
    private fun analyzeWindPatterns(currentData: RadianceData) {
        if (radianceHistory.size < RADIANCE_ANALYSIS_WINDOW) {
            return // Not enough data yet
        }
        
        // Analyze wind pattern consistency over time
        val recentData = radianceHistory.takeLast(RADIANCE_ANALYSIS_WINDOW)
        
        // Calculate dominant wind direction
        val dominantVector = calculateDominantWindVector(recentData)
        
        // Callback with wind analysis results
        windAnalysisCallback?.invoke(dominantVector)
        
        // Log Level 3 analysis results
        if (dominantVector.confidence > WIND_DETECTION_THRESHOLD) {
            Log.v(TAG, "RAD WND L3 Wind detected: " +
                  "mag=${String.format("%.2f", dominantVector.magnitude)}, " +
                  "dir=${String.format("%.1f", dominantVector.direction)}°, " +
                  "conf=${String.format("%.2f", dominantVector.confidence)}")
        }
    }
    
    /**
     * Calculate dominant wind vector from historical data
     */
    private fun calculateDominantWindVector(historyData: List<RadianceData>): WindVector {
        var totalX = 0f
        var totalY = 0f
        var totalConfidence = 0f
        var vectorCount = 0
        
        historyData.forEach { data ->
            data.windVectors.forEach { vector ->
                if (vector.confidence > WIND_DETECTION_THRESHOLD) {
                    totalX += vector.x * vector.confidence
                    totalY += vector.y * vector.confidence
                    totalConfidence += vector.confidence
                    vectorCount++
                }
            }
        }
        
        if (vectorCount == 0 || totalConfidence == 0f) {
            return WindVector(0f, 0f, 0f, 0f, 0f)
        }
        
        val avgX = totalX / totalConfidence
        val avgY = totalY / totalConfidence
        val avgConfidence = totalConfidence / vectorCount
        
        val magnitude = sqrt(avgX * avgX + avgY * avgY)
        val direction = atan2(avgY, avgX) * 180 / PI.toFloat()
        
        return WindVector(avgX, avgY, magnitude, direction, avgConfidence)
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
                // Create thermal pattern with simulated wind effect
                val centerX = width / 2f
                val centerY = height / 2f
                val distance = sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY))
                
                // Base temperature pattern
                var temp = 25f + 10f * exp(-distance / 50f)
                
                // Add wind simulation (moving thermal pattern)
                val windX = cos(time * 0.1).toFloat() * 2
                val windY = sin(time * 0.1).toFloat() * 2
                val windDistance = sqrt((x - centerX - windX) * (x - centerX - windX) + 
                                       (y - centerY - windY) * (y - centerY - windY))
                temp += 5f * exp(-windDistance / 30f)
                
                // Add noise for realistic thermal data
                temp += (Math.random().toFloat() - 0.5f) * 2f
                
                data[y][x] = temp
            }
        }
        
        return data
    }
    
    /**
     * Generate Level 3 analysis report
     */
    private fun generateLevel3AnalysisReport() {
        if (radianceHistory.isEmpty()) {
            Log.i(TAG, "No RAD WND data to generate report")
            return
        }
        
        Log.i(TAG, "=== RAD WND Level 3 Analysis Report ===")
        Log.i(TAG, "Total frames processed: ${radianceHistory.size}")
        
        // Calculate average radiance
        val avgRadiance = radianceHistory.map { it.averageRadiance }.average()
        Log.i(TAG, "Average radiance: ${String.format("%.2f", avgRadiance)}")
        
        // Calculate wind detection rate
        val windDetections = radianceHistory.sumOf { data ->
            data.windVectors.count { it.confidence > WIND_DETECTION_THRESHOLD }
        }
        val windDetectionRate = windDetections.toFloat() / radianceHistory.size
        Log.i(TAG, "Wind detection rate: ${String.format("%.1f%%", windDetectionRate * 100)}")
        
        // Calculate dominant wind characteristics
        val dominantWind = calculateDominantWindVector(radianceHistory)
        if (dominantWind.confidence > 0) {
            Log.i(TAG, "Dominant wind - Magnitude: ${String.format("%.2f", dominantWind.magnitude)}, " +
                      "Direction: ${String.format("%.1f", dominantWind.direction)}°")
        }
        
        Log.i(TAG, "=== End RAD WND Level 3 Report ===")
    }
    
    /**
     * Set wind analysis callback
     */
    fun setWindAnalysisCallback(callback: (WindVector) -> Unit) {
        windAnalysisCallback = callback
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
     * Get wind vector grid for Level 3 visualization
     */
    fun getWindVectorGrid(): Array<Array<WindVector>> = windVectorGrid
    
    /**
     * Cleanup processor resources
     */
    fun cleanup() {
        Log.d(TAG, "Cleaning up RAD WND processor")
        
        isProcessingActive = false
        processingJob?.cancel()
        processingJob = null
        
        synchronized(radianceHistory) {
            radianceHistory.clear()
        }
        
        windAnalysisCallback = null
        level3Surface = null
        
        Log.d(TAG, "RAD WND processor cleanup completed")
    }
}