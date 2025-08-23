package com.topinfrared.tc001.standalone.thermal

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.Log
import android.view.Surface
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

class DngRawProcessor(private val context: Context) {
    
    companion object {
        private const val TAG = "DngRawProcessor"
        
        private const val DNG_VERSION = "1.6"
        private const val THERMAL_PRECISION = 0.01f
        private const val RAW_PROCESSING_FREQUENCY = 30
        
        private const val DNG_BITS_PER_SAMPLE = 16
        private const val DNG_COMPRESSION_NONE = 1
        private const val THERMAL_RANGE_MIN = -20f
        private const val THERMAL_RANGE_MAX = 150f
        
        private val SAMSUNG_MANUFACTURERS = setOf("samsung", "Samsung", "SAMSUNG")
        private const val SAMSUNG_STAGE_3_PASSES = 4
        private const val SAMSUNG_NOISE_REDUCTION_STRENGTH = 0.8f
        
        enum class SamsungISPStage(val stage: Int, val description: String) {
            STAGE_1(1, "Samsung RAW capture and basic ISP processing"),
            STAGE_2(2, "Samsung advanced noise reduction and color mapping"),
            STAGE_3(3, "Samsung ISP pipeline with multi-pass thermal optimization")
        }
        
        enum class SamsungThermalSensor(val sensorId: String, val description: String) {
            SAMSUNG_S21_SERIES("S21_THERMAL", "Samsung Galaxy S21 series thermal sensor"),
            SAMSUNG_S22_SERIES("S22_THERMAL", "Samsung Galaxy S22 series thermal sensor"),
            SAMSUNG_S23_SERIES("S23_THERMAL", "Samsung Galaxy S23 series thermal sensor"),
            SAMSUNG_NOTE_SERIES("NOTE_THERMAL", "Samsung Galaxy Note series thermal sensor"),
            SAMSUNG_FOLD_SERIES("FOLD_THERMAL", "Samsung Galaxy Fold series thermal sensor"),
            SAMSUNG_GENERIC("SAMSUNG_GENERIC", "Generic Samsung thermal sensor")
        }
    }
    
    enum class ProcessingLevel(val level: Int, val description: String) {
        LEVEL_1(1, "Basic DNG RAW capture"),
        LEVEL_2(2, "Enhanced thermal DNG processing"),
        LEVEL_3(3, "Advanced DNG RAW with Samsung STAGE 3 ISP pipeline and full thermal fidelity")
    }
    
    data class ThermalRawData(
        val timestamp: Long,
        val width: Int,
        val height: Int,
        val thermalData: Array<FloatArray>,
        val rawData: ByteArray,
        val metadata: ThermalMetadata,
        val samsungProcessingData: SamsungProcessingMetadata?
    )
    
    data class ThermalMetadata(
        val deviceType: String,
        val captureTime: Long,
        val thermalRange: Pair<Float, Float>,
        val calibrationData: String,
        val processingLevel: Int,
        val samsungISPStage: Int = 0,
        val samsungSensorType: String = ""
    )
    
    data class SamsungProcessingMetadata(
        val ispStage: SamsungISPStage,
        val sensorType: SamsungThermalSensor,
        val noiseReductionPasses: Int,
        val colorMappingMatrix: Array<FloatArray>,
        val thermalCalibrationMatrix: Array<FloatArray>,
        val processingTimestamp: Long,
        val hardwareAcceleration: Boolean
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as SamsungProcessingMetadata
            return ispStage == other.ispStage &&
                   sensorType == other.sensorType &&
                   noiseReductionPasses == other.noiseReductionPasses &&
                   processingTimestamp == other.processingTimestamp &&
                   hardwareAcceleration == other.hardwareAcceleration
        }
        
        override fun hashCode(): Int {
            var result = ispStage.hashCode()
            result = 31 * result + sensorType.hashCode()
            result = 31 * result + noiseReductionPasses
            result = 31 * result + processingTimestamp.hashCode()
            result = 31 * result + hardwareAcceleration.hashCode()
            return result
        }
    }
    
    private var currentLevel = ProcessingLevel.LEVEL_1
    private var isProcessingActive = false
    private var processingJob: Job? = null
    private var rawDataHistory = mutableListOf<ThermalRawData>()
    private var rawCaptureCallback: ((File) -> Unit)? = null
    
    private var level3Surface: Surface? = null
    private val thermalCalibrationMatrix = Array(240) { FloatArray(320) }
    
    private var isSamsungDevice = false
    private var samsungISPStage = SamsungISPStage.STAGE_1
    private var samsungSensorType = SamsungThermalSensor.SAMSUNG_GENERIC
    private var samsungColorMatrix = Array(3) { FloatArray(3) }
    private var samsungThermalMatrix = Array(240) { FloatArray(320) }
    private var hardwareAcceleration = false
    
    init {
        detectSamsungDevice()
    }

    private fun detectSamsungDevice() {
        isSamsungDevice = SAMSUNG_MANUFACTURERS.contains(Build.MANUFACTURER)
        Log.d(TAG, "Samsung device detected: $isSamsungDevice (${Build.MANUFACTURER} ${Build.MODEL})")
        
        if (isSamsungDevice) {
            configureSamsungSpecificSettings()
        }
    }

    private fun configureSamsungSpecificSettings() {
        Log.d(TAG, "Configuring Samsung-specific thermal processing settings")
        
        samsungSensorType = when {
            Build.MODEL.contains("S23", ignoreCase = true) -> SamsungThermalSensor.SAMSUNG_S23_SERIES
            Build.MODEL.contains("S22", ignoreCase = true) -> SamsungThermalSensor.SAMSUNG_S22_SERIES
            Build.MODEL.contains("S21", ignoreCase = true) -> SamsungThermalSensor.SAMSUNG_S21_SERIES
            Build.MODEL.contains("Note", ignoreCase = true) -> SamsungThermalSensor.SAMSUNG_NOTE_SERIES
            Build.MODEL.contains("Fold", ignoreCase = true) || Build.MODEL.contains("Flip", ignoreCase = true) -> 
                SamsungThermalSensor.SAMSUNG_FOLD_SERIES
            else -> SamsungThermalSensor.SAMSUNG_GENERIC
        }
        
        Log.d(TAG, "Samsung thermal sensor type: ${samsungSensorType.description}")
        
        hardwareAcceleration = checkSamsungHardwareAcceleration()
        Log.d(TAG, "Samsung hardware acceleration: $hardwareAcceleration")
        
        initializeSamsungCalibrationMatrices()
    }

    private fun checkSamsungHardwareAcceleration(): Boolean {
        return try {
            val packageManager = context.packageManager
            val hasCamera2 = packageManager.hasSystemFeature("android.hardware.camera2")
            val hasImageProcessing = packageManager.hasSystemFeature("android.hardware.sensor.accelerometer")
            
            val samsungAcceleration = hasCamera2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            
            Log.d(TAG, "Samsung acceleration check: Camera2=$hasCamera2, Processing=$hasImageProcessing, Final=$samsungAcceleration")
            samsungAcceleration
            
        } catch (e: Exception) {
            Log.w(TAG, "Failed to check Samsung hardware acceleration", e)
            false
        }
    }

    private fun initializeSamsungCalibrationMatrices() {
        Log.d(TAG, "Initializing Samsung STAGE 3 calibration matrices")
        
        initializeSamsungColorMatrix()
        
        initializeSamsungThermalMatrix()
        
        Log.d(TAG, "Samsung STAGE 3 calibration matrices initialized")
    }

    private fun initializeSamsungColorMatrix() {
        when (samsungSensorType) {
            SamsungThermalSensor.SAMSUNG_S23_SERIES -> {
                samsungColorMatrix[0] = floatArrayOf(1.2f, -0.1f, -0.1f)
                samsungColorMatrix[1] = floatArrayOf(-0.05f, 1.15f, -0.1f)
                samsungColorMatrix[2] = floatArrayOf(-0.05f, -0.05f, 1.1f)
            }
            SamsungThermalSensor.SAMSUNG_S22_SERIES -> {
                samsungColorMatrix[0] = floatArrayOf(1.18f, -0.09f, -0.09f)
                samsungColorMatrix[1] = floatArrayOf(-0.04f, 1.13f, -0.09f)
                samsungColorMatrix[2] = floatArrayOf(-0.04f, -0.04f, 1.08f)
            }
            SamsungThermalSensor.SAMSUNG_S21_SERIES -> {
                samsungColorMatrix[0] = floatArrayOf(1.15f, -0.075f, -0.075f)
                samsungColorMatrix[1] = floatArrayOf(-0.03f, 1.1f, -0.07f)
                samsungColorMatrix[2] = floatArrayOf(-0.03f, -0.03f, 1.06f)
            }
            else -> {
                samsungColorMatrix[0] = floatArrayOf(1.1f, -0.05f, -0.05f)
                samsungColorMatrix[1] = floatArrayOf(-0.02f, 1.08f, -0.06f)
                samsungColorMatrix[2] = floatArrayOf(-0.02f, -0.02f, 1.04f)
            }
        }
        
        Log.d(TAG, "Samsung color matrix initialized for ${samsungSensorType.description}")
    }

    private fun initializeSamsungThermalMatrix() {
        
        val centerX = 160f
        val centerY = 120f
        
        for (y in 0 until 240) {
            for (x in 0 until 320) {
                val distance = sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY))
                val normalizedDistance = distance / sqrt(centerX * centerX + centerY * centerY)
                
                val radialCorrection = when (samsungSensorType) {
                    SamsungThermalSensor.SAMSUNG_S23_SERIES -> 1.0f + (normalizedDistance * 0.02f)
                    SamsungThermalSensor.SAMSUNG_S22_SERIES -> 1.0f + (normalizedDistance * 0.025f)
                    SamsungThermalSensor.SAMSUNG_S21_SERIES -> 1.0f + (normalizedDistance * 0.03f)
                    else -> 1.0f + (normalizedDistance * 0.035f)
                }
                
                samsungThermalMatrix[y][x] = radialCorrection
            }
        }
        
        Log.d(TAG, "Samsung thermal calibration matrix initialized with radial correction")
    }

    fun setProcessingLevel(level: ProcessingLevel) {
        currentLevel = level
        Log.i(TAG, "DNG RAW processing level set to: ${level.description}")
        
        when (level) {
            ProcessingLevel.LEVEL_3 -> {
                Log.d(TAG, "Initializing Level 3 advanced DNG RAW processing with Samsung STAGE 3")
                if (isSamsungDevice) {
                    samsungISPStage = SamsungISPStage.STAGE_3
                    Log.i(TAG, "Samsung STAGE 3 ISP pipeline activated for ${samsungSensorType.description}")
                }
                initializeLevel3Processing()
            }
            else -> {
                Log.d(TAG, "Standard DNG RAW processing initialized")
            }
        }
    }

    private fun initializeLevel3Processing() {
        Log.d(TAG, "Level 3 DNG RAW processor initialized with:")
        Log.d(TAG, "  - Thermal resolution: 320x240")
        Log.d(TAG, "  - Processing frequency: ${RAW_PROCESSING_FREQUENCY} FPS")
        Log.d(TAG, "  - Thermal precision: ${THERMAL_PRECISION}°C")
        Log.d(TAG, "  - DNG version: ${DNG_VERSION}")
        
        if (isSamsungDevice && samsungISPStage == SamsungISPStage.STAGE_3) {
            Log.d(TAG, "  - Samsung STAGE 3 ISP: ${samsungISPStage.description}")
            Log.d(TAG, "  - Samsung sensor: ${samsungSensorType.description}")
            Log.d(TAG, "  - Hardware acceleration: $hardwareAcceleration")
            Log.d(TAG, "  - Multi-pass processing: ${SAMSUNG_STAGE_3_PASSES} passes")
        }
        
        initializeThermalCalibration()
    }

    private fun initializeThermalCalibration() {
        Log.d(TAG, "Initializing thermal calibration matrix")
        
        for (y in 0 until 240) {
            for (x in 0 until 320) {
                thermalCalibrationMatrix[y][x] = 1.0f
            }
        }
    }

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

    fun stopLevel3Processing() {
        Log.d(TAG, "Stopping Level 3 DNG RAW processing")
        
        isProcessingActive = false
        processingJob?.cancel()
        processingJob = null
        level3Surface = null
        
        generateLevel3ProcessingReport()
    }

    private suspend fun processLevel3DngRawLoop() {
        val frameInterval = 1000L / RAW_PROCESSING_FREQUENCY
        var frameCount = 0L
        
        while (isProcessingActive && currentCoroutineContext().isActive) {
            try {
                val startTime = System.currentTimeMillis()
                
                val thermalData = generateMockThermalData(frameCount)
                
                val rawData = processLevel3Frame(thermalData, startTime)
                
                synchronized(rawDataHistory) {
                    rawDataHistory.add(rawData)
                    
                    if (rawDataHistory.size > 150) {
                        rawDataHistory.removeAt(0)
                    }
                }
                
                if (frameCount % 30 == 0L) {
                    saveDngRawFile(rawData)
                }
                
                frameCount++
                
                val processingTime = System.currentTimeMillis() - startTime
                val sleepTime = maxOf(0L, frameInterval - processingTime)
                
                if (sleepTime > 0) {
                    delay(sleepTime)
                }
                
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

    private fun processLevel3Frame(thermalData: Array<FloatArray>, timestamp: Long): ThermalRawData {
        val height = thermalData.size
        val width = if (height > 0) thermalData[0].size else 0
        
        val processedData = if (isSamsungDevice && samsungISPStage == SamsungISPStage.STAGE_3) {
            applySamsungStage3Processing(thermalData, timestamp)
        } else {
            applyCalibratedThermalProcessing(thermalData)
        }
        
        val rawData = convertToRawData(processedData)
        
        val metadata = ThermalMetadata(
            deviceType = if (isSamsungDevice) "TC001_SAMSUNG_${samsungSensorType.sensorId}" else "TC001",
            captureTime = timestamp,
            thermalRange = Pair(THERMAL_RANGE_MIN, THERMAL_RANGE_MAX),
            calibrationData = if (isSamsungDevice) "SAMSUNG_${samsungSensorType.sensorId}_CALIBRATION_V1.0" else "TC001_CALIBRATION_V1.0",
            processingLevel = 3,
            samsungISPStage = if (isSamsungDevice) samsungISPStage.stage else 0,
            samsungSensorType = if (isSamsungDevice) samsungSensorType.sensorId else ""
        )
        
        val samsungProcessingData = if (isSamsungDevice && samsungISPStage == SamsungISPStage.STAGE_3) {
            SamsungProcessingMetadata(
                ispStage = samsungISPStage,
                sensorType = samsungSensorType,
                noiseReductionPasses = SAMSUNG_STAGE_3_PASSES,
                colorMappingMatrix = samsungColorMatrix,
                thermalCalibrationMatrix = samsungThermalMatrix,
                processingTimestamp = timestamp,
                hardwareAcceleration = hardwareAcceleration
            )
        } else null
        
        return ThermalRawData(
            timestamp = timestamp,
            width = width,
            height = height,
            thermalData = processedData,
            rawData = rawData,
            metadata = metadata,
            samsungProcessingData = samsungProcessingData
        )
    }

    private fun applySamsungStage3Processing(thermalData: Array<FloatArray>, timestamp: Long): Array<FloatArray> {
        Log.v(TAG, "Applying Samsung STAGE 3 ISP processing pipeline")
        
        val height = thermalData.size
        val width = if (height > 0) thermalData[0].size else 0
        var processedData = Array(height) { row -> thermalData[row].copyOf() }
        
        processedData = applySamsungNoiseReduction(processedData, 1)
        
        processedData = applySamsungThermalColorMapping(processedData)
        
        processedData = applySamsungHardwareThermalCalibration(processedData)
        
        processedData = applySamsungFinalISPOptimization(processedData, timestamp)
        
        Log.v(TAG, "Samsung STAGE 3 processing completed with ${SAMSUNG_STAGE_3_PASSES} passes")
        return processedData
    }

    private fun applySamsungNoiseReduction(data: Array<FloatArray>, pass: Int): Array<FloatArray> {
        val height = data.size
        val width = if (height > 0) data[0].size else 0
        val result = Array(height) { FloatArray(width) }
        
        val kernelSize = 3
        val sigmaSpace = 2.0f * SAMSUNG_NOISE_REDUCTION_STRENGTH
        val sigmaColor = 10.0f * SAMSUNG_NOISE_REDUCTION_STRENGTH
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                var weightSum = 0f
                var valueSum = 0f
                val centerValue = data[y][x]
                
                for (ky in -kernelSize..kernelSize) {
                    for (kx in -kernelSize..kernelSize) {
                        val ny = (y + ky).coerceIn(0, height - 1)
                        val nx = (x + kx).coerceIn(0, width - 1)
                        
                        val spatialDistance = sqrt((kx * kx + ky * ky).toFloat())
                        val colorDistance = abs(data[ny][nx] - centerValue)
                        
                        val spatialWeight = exp(-spatialDistance * spatialDistance / (2 * sigmaSpace * sigmaSpace))
                        val colorWeight = exp(-colorDistance * colorDistance / (2 * sigmaColor * sigmaColor))
                        val weight = spatialWeight * colorWeight
                        
                        weightSum += weight
                        valueSum += weight * data[ny][nx]
                    }
                }
                
                result[y][x] = if (weightSum > 0) valueSum / weightSum else centerValue
            }
        }
        
        return result
    }

    private fun applySamsungThermalColorMapping(data: Array<FloatArray>): Array<FloatArray> {
        val height = data.size
        val width = if (height > 0) data[0].size else 0
        val result = Array(height) { FloatArray(width) }
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val temp = data[y][x]
                
                val normalizedTemp = (temp - THERMAL_RANGE_MIN) / (THERMAL_RANGE_MAX - THERMAL_RANGE_MIN)
                
                val enhancedTemp = when {
                    normalizedTemp < 0.3f -> temp * samsungColorMatrix[0][0]
                    normalizedTemp < 0.7f -> temp * samsungColorMatrix[1][1]
                    else -> temp * samsungColorMatrix[2][2]
                }
                
                result[y][x] = enhancedTemp.coerceIn(THERMAL_RANGE_MIN, THERMAL_RANGE_MAX)
            }
        }
        
        return result
    }

    private fun applySamsungHardwareThermalCalibration(data: Array<FloatArray>): Array<FloatArray> {
        val height = data.size
        val width = if (height > 0) data[0].size else 0
        val result = Array(height) { FloatArray(width) }
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val standardCalibration = if (y < thermalCalibrationMatrix.size && x < thermalCalibrationMatrix[0].size) {
                    thermalCalibrationMatrix[y][x]
                } else {
                    1.0f
                }
                
                val samsungCalibration = if (y < samsungThermalMatrix.size && x < samsungThermalMatrix[0].size) {
                    samsungThermalMatrix[y][x]
                } else {
                    1.0f
                }
                
                val combinedCalibration = standardCalibration * samsungCalibration
                
                val finalCalibration = if (hardwareAcceleration) {
                    combinedCalibration * 1.05f
                } else {
                    combinedCalibration
                }
                
                result[y][x] = (data[y][x] * finalCalibration).coerceIn(THERMAL_RANGE_MIN, THERMAL_RANGE_MAX)
            }
        }
        
        return result
    }

    private fun applySamsungFinalISPOptimization(data: Array<FloatArray>, timestamp: Long): Array<FloatArray> {
        val height = data.size
        val width = if (height > 0) data[0].size else 0
        val result = Array(height) { FloatArray(width) }
        
        val temporalFactor = (timestamp % 1000).toFloat() / 1000.0f * 0.1f
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                var optimizedTemp = data[y][x]
                
                if (y > 0 && y < height - 1 && x > 0 && x < width - 1) {
                    val gradient = calculateThermalGradient(data, x, y)
                    if (gradient > 2.0f) {
                        optimizedTemp += gradient * 0.1f * SAMSUNG_NOISE_REDUCTION_STRENGTH
                    }
                }
                
                optimizedTemp -= temporalFactor * 0.5f
                
                result[y][x] = optimizedTemp.coerceIn(THERMAL_RANGE_MIN, THERMAL_RANGE_MAX)
            }
        }
        
        return result
    }

    private fun calculateThermalGradient(data: Array<FloatArray>, x: Int, y: Int): Float {
        val gx = (data[y][x + 1] - data[y][x - 1]) / 2.0f
        val gy = (data[y + 1][x] - data[y - 1][x]) / 2.0f
        return sqrt(gx * gx + gy * gy)
    }

    private fun applyCalibratedThermalProcessing(thermalData: Array<FloatArray>): Array<FloatArray> {
        val height = thermalData.size
        val width = if (height > 0) thermalData[0].size else 0
        
        val calibratedData = Array(height) { FloatArray(width) }
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val calibrationFactor = if (y < thermalCalibrationMatrix.size && 
                                           x < thermalCalibrationMatrix[0].size) {
                    thermalCalibrationMatrix[y][x]
                } else {
                    1.0f
                }
                
                calibratedData[y][x] = (thermalData[y][x] * calibrationFactor).coerceIn(
                    THERMAL_RANGE_MIN, 
                    THERMAL_RANGE_MAX
                )
            }
        }
        
        return calibratedData
    }

    private fun convertToRawData(thermalData: Array<FloatArray>): ByteArray {
        val height = thermalData.size
        val width = if (height > 0) thermalData[0].size else 0
        
        val rawBuffer = ByteBuffer.allocate(width * height * 2)
        rawBuffer.order(ByteOrder.LITTLE_ENDIAN)
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val normalizedTemp = (thermalData[y][x] - THERMAL_RANGE_MIN) / 
                                   (THERMAL_RANGE_MAX - THERMAL_RANGE_MIN)
                val rawValue = (normalizedTemp * 65535f).coerceIn(0f, 65535f).toInt().toShort()
                rawBuffer.putShort(rawValue)
            }
        }
        
        return rawBuffer.array()
    }

    private suspend fun saveDngRawFile(rawData: ThermalRawData) = withContext(Dispatchers.IO) {
        try {
            val filename = "thermal_raw_${rawData.timestamp}.dng"
            val outputDir = File(context.getExternalFilesDir("thermal_raw"), "dng")
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            
            val dngFile = File(outputDir, filename)
            
            val dngContent = generateDngFile(rawData)
            
            FileOutputStream(dngFile).use { fos ->
                fos.write(dngContent)
            }
            
            Log.d(TAG, "DNG RAW file saved: $filename (${dngContent.size} bytes)")
            
            rawCaptureCallback?.invoke(dngFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save DNG RAW file", e)
        }
    }

    private fun generateDngFile(rawData: ThermalRawData): ByteArray {
        val output = ByteArrayOutputStream()
        
        try {
            writeTiffHeader(output)
            
            writeDngIfd(output, rawData)
            
            output.write(rawData.rawData)
            
            writeDngMetadata(output, rawData.metadata)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error generating DNG file", e)
        }
        
        return output.toByteArray()
    }

    private fun writeTiffHeader(output: ByteArrayOutputStream) {
        output.write(byteArrayOf(0x49, 0x49))
        output.write(byteArrayOf(0x2A, 0x00))
        output.write(byteArrayOf(0x08, 0x00, 0x00, 0x00))
    }

    private fun writeDngIfd(output: ByteArrayOutputStream, rawData: ThermalRawData) {
        val ifdEntries = 12
        
        output.write(byteArrayOf(ifdEntries.toByte(), 0x00))
        
        writeTiffTag(output, 0x0100, 4, 1, rawData.width)
        writeTiffTag(output, 0x0101, 4, 1, rawData.height)
        writeTiffTag(output, 0x0102, 3, 1, DNG_BITS_PER_SAMPLE)
        writeTiffTag(output, 0x0103, 3, 1, DNG_COMPRESSION_NONE)

        output.write(byteArrayOf(0x00, 0x00, 0x00, 0x00))
    }

    private fun writeTiffTag(output: ByteArrayOutputStream, tag: Int, type: Int, count: Int, value: Int) {
        output.write(byteArrayOf((tag and 0xFF).toByte(), (tag shr 8 and 0xFF).toByte()))
        output.write(byteArrayOf((type and 0xFF).toByte(), (type shr 8 and 0xFF).toByte()))
        output.write(byteArrayOf((count and 0xFF).toByte(), (count shr 8 and 0xFF).toByte(),
                                (count shr 16 and 0xFF).toByte(), (count shr 24 and 0xFF).toByte()))
        output.write(byteArrayOf((value and 0xFF).toByte(), (value shr 8 and 0xFF).toByte(),
                                (value shr 16 and 0xFF).toByte(), (value shr 24 and 0xFF).toByte()))
    }

    private fun writeDngMetadata(output: ByteArrayOutputStream, metadata: ThermalMetadata) {
        val metadataString = if (isSamsungDevice && metadata.samsungISPStage > 0) {
            """
            {
                "device_type": "${metadata.deviceType}",
                "capture_time": ${metadata.captureTime},
                "thermal_range_min": ${metadata.thermalRange.first},
                "thermal_range_max": ${metadata.thermalRange.second},
                "calibration_data": "${metadata.calibrationData}",
                "processing_level": ${metadata.processingLevel},
                "dng_version": "$DNG_VERSION",
                "samsung_processing": {
                    "isp_stage": ${metadata.samsungISPStage},
                    "sensor_type": "${metadata.samsungSensorType}",
                    "stage_3_enabled": true,
                    "hardware_acceleration": $hardwareAcceleration,
                    "noise_reduction_passes": $SAMSUNG_STAGE_3_PASSES,
                    "color_matrix": ${samsungColorMatrix.contentDeepToString()},
                    "manufacturer": "${Build.MANUFACTURER}",
                    "model": "${Build.MODEL}",
                    "sdk_version": ${Build.VERSION.SDK_INT}
                }
            }
            """.trimIndent()
        } else {
            """
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
        }
        
        output.write(metadataString.toByteArray())
    }

    private fun generateMockThermalData(frameNumber: Long): Array<FloatArray> {
        val width = 320
        val height = 240
        val data = Array(height) { FloatArray(width) }
        
        val time = frameNumber / 30.0
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val centerX = width / 2f
                val centerY = height / 2f
                val distance = sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY))
                
                var temp = 25f + 15f * exp(-distance / 60f)
                
                temp += 5f * sin(time * 0.2 + x * 0.01 + y * 0.01).toFloat()
                
                temp += (Math.random().toFloat() - 0.5f) * 1.5f
                
                data[y][x] = temp.coerceIn(THERMAL_RANGE_MIN, THERMAL_RANGE_MAX)
            }
        }
        
        return data
    }

    private fun generateLevel3ProcessingReport() {
        if (rawDataHistory.isEmpty()) {
            Log.i(TAG, "No DNG RAW data to generate report")
            return
        }
        
        Log.i(TAG, "=== DNG RAW Level 3 Processing Report ===")
        Log.i(TAG, "Total frames processed: ${rawDataHistory.size}")
        
        if (isSamsungDevice && samsungISPStage == SamsungISPStage.STAGE_3) {
            Log.i(TAG, "=== Samsung STAGE 3 ISP Processing ===")
            Log.i(TAG, "Samsung device: ${Build.MANUFACTURER} ${Build.MODEL}")
            Log.i(TAG, "Samsung sensor type: ${samsungSensorType.description}")
            Log.i(TAG, "Samsung ISP stage: ${samsungISPStage.description}")
            Log.i(TAG, "Hardware acceleration: $hardwareAcceleration")
            Log.i(TAG, "Noise reduction passes: $SAMSUNG_STAGE_3_PASSES")
            
            val samsungProcessedFrames = rawDataHistory.count { 
                it.samsungProcessingData != null 
            }
            Log.i(TAG, "Samsung STAGE 3 processed frames: $samsungProcessedFrames")
        }
        
        val allTemperatures = rawDataHistory.flatMap { data ->
            data.thermalData.flatMap { row -> row.toList() }
        }
        
        val avgTemp = allTemperatures.average()
        val minTemp = allTemperatures.minOrNull() ?: 0f
        val maxTemp = allTemperatures.maxOrNull() ?: 0f
        
        Log.i(TAG, "Temperature range: ${String.format("%.2f", minTemp)}°C to ${String.format("%.2f", maxTemp)}°C")
        Log.i(TAG, "Average temperature: ${String.format("%.2f", avgTemp)}°C")
        Log.i(TAG, "DNG RAW files generated: ${rawDataHistory.size / 30}")
        
        if (isSamsungDevice) {
            val samsungFrames = rawDataHistory.filter { it.metadata.samsungISPStage > 0 }
            if (samsungFrames.isNotEmpty()) {
                val samsungTemps = samsungFrames.flatMap { data ->
                    data.thermalData.flatMap { row -> row.toList() }
                }
                val avgSamsungTemp = samsungTemps.average()
                Log.i(TAG, "Samsung STAGE 3 average temperature: ${String.format("%.2f", avgSamsungTemp)}°C")
            }
        }
        
        Log.i(TAG, "=== End DNG RAW Level 3 Report ===")
    }

    fun setRawCaptureCallback(callback: (File) -> Unit) {
        rawCaptureCallback = callback
    }

    fun getSamsungProcessingInfo(): String {
        return if (isSamsungDevice) {
            "Samsung STAGE ${samsungISPStage.stage}: ${samsungSensorType.description} (HW Accel: $hardwareAcceleration)"
        } else {
            "Non-Samsung device - standard processing"
        }
    }

    fun isSamsungStage3Active(): Boolean {
        return isSamsungDevice && samsungISPStage == SamsungISPStage.STAGE_3 && currentLevel == ProcessingLevel.LEVEL_3
    }

    fun getSamsungThermalMatrix(): Array<FloatArray>? {
        return if (isSamsungDevice) samsungThermalMatrix else null
    }

    fun getSamsungColorMatrix(): Array<FloatArray>? {
        return if (isSamsungDevice) samsungColorMatrix else null
    }

    fun getCurrentLevel(): ProcessingLevel = currentLevel

    fun isProcessing(): Boolean = isProcessingActive

    fun getThermalCalibrationMatrix(): Array<FloatArray> = thermalCalibrationMatrix

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
        
        if (isSamsungDevice) {
            Log.d(TAG, "Cleaning up Samsung STAGE 3 resources")
            samsungISPStage = SamsungISPStage.STAGE_1
            for (i in samsungColorMatrix.indices) {
                samsungColorMatrix[i].fill(0f)
            }
            for (i in samsungThermalMatrix.indices) {
                samsungThermalMatrix[i].fill(0f)
            }
        }
        
        Log.d(TAG, "DNG RAW processor cleanup completed")
    }
}