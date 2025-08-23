package com.topinfrared.tc001.standalone.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.topinfrared.tc001.standalone.thermal.TC001ThermalManager
import kotlin.math.*

class ThermalOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private var currentMode = TC001ThermalManager.TempMode.POINT
    private var pointLocation: PointF? = null
    private var lineStart: PointF? = null
    private var lineEnd: PointF? = null
    private var areaTopLeft: PointF? = null
    private var areaBottomRight: PointF? = null
    
    private var measurementHistory = mutableListOf<MeasurementData>()
    private var showTemperatureGradient = true
    private var showMeasurementHistory = false
    private val maxHistorySize = 100
    
    private var animationPhase = 0f
    private val animationPaint = Paint()
    private val gradientColors = intArrayOf(
        Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED
    )
    
    data class MeasurementData(
        val timestamp: Long,
        val temperature: Float,
        val location: PointF,
        val mode: TC001ThermalManager.TempMode
    )
    
    private val paint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 32f
        isAntiAlias = true
        setShadowLayer(4f, 2f, 2f, Color.BLACK)
    }
    
    private val fillPaint = Paint().apply {
        color = Color.YELLOW
        this.alpha = 64
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    private val historyPaint = Paint().apply {
        color = Color.WHITE
        this.alpha = 128
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    
    private val gradientPaint = Paint().apply {
        isAntiAlias = true
    }
    
    companion object {
        private const val TAG = "ThermalOverlayView"
    }
    
    init {
        setBackgroundColor(Color.TRANSPARENT)
        
        startAnimation()
    }
    
    private fun startAnimation() {
        post(object : Runnable {
            override fun run() {
                animationPhase = (animationPhase + 0.1f) % (2 * PI.toFloat())
                invalidate()
                postDelayed(this, 50)
            }
        })
    }
    
    fun setTemperatureMeasureMode(mode: TC001ThermalManager.TempMode) {
        currentMode = mode
        when (mode) {
            TC001ThermalManager.TempMode.POINT -> {
                lineStart = null
                lineEnd = null
                areaTopLeft = null
                areaBottomRight = null
            }
            TC001ThermalManager.TempMode.LINE -> {
                pointLocation = null
                areaTopLeft = null
                areaBottomRight = null
            }
            TC001ThermalManager.TempMode.AREA -> {
                pointLocation = null
                lineStart = null
                lineEnd = null
            }
        }
        invalidate()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when (currentMode) {
                    TC001ThermalManager.TempMode.POINT -> {
                        pointLocation = PointF(x, y)
                        invalidate()
                        return true
                    }
                    TC001ThermalManager.TempMode.LINE -> {
                        lineStart = PointF(x, y)
                        lineEnd = null
                        invalidate()
                        return true
                    }
                    TC001ThermalManager.TempMode.AREA -> {
                        areaTopLeft = PointF(x, y)
                        areaBottomRight = null
                        invalidate()
                        return true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                when (currentMode) {
                    TC001ThermalManager.TempMode.LINE -> {
                        if (lineStart != null) {
                            lineEnd = PointF(x, y)
                            invalidate()
                        }
                    }
                    TC001ThermalManager.TempMode.AREA -> {
                        if (areaTopLeft != null) {
                            areaBottomRight = PointF(x, y)
                            invalidate()
                        }
                    }
                    else -> {}
                }
            }
            MotionEvent.ACTION_UP -> {
                when (currentMode) {
                    TC001ThermalManager.TempMode.LINE -> {
                        if (lineStart != null) {
                            lineEnd = PointF(x, y)
                            invalidate()
                        }
                    }
                    TC001ThermalManager.TempMode.AREA -> {
                        if (areaTopLeft != null) {
                            areaBottomRight = PointF(x, y)
                            invalidate()
                        }
                    }
                    else -> {}
                }
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (showTemperatureGradient) {
            drawTemperatureGradient(canvas)
        }
        
        if (showMeasurementHistory) {
            drawMeasurementHistory(canvas)
        }
        
        drawTemperatureMeasurements(canvas)
    }
    
    private fun drawTemperatureMeasurements(canvas: Canvas) {
        when (currentMode) {
            TC001ThermalManager.TempMode.POINT -> {
                drawPointMeasurement(canvas)
            }
            TC001ThermalManager.TempMode.LINE -> {
                drawLineMeasurement(canvas)
            }
            TC001ThermalManager.TempMode.AREA -> {
                drawAreaMeasurement(canvas)
            }
        }
    }
    
    private fun drawPointMeasurement(canvas: Canvas) {
        pointLocation?.let { point ->
            val temperature = getMockTemperature(point.x, point.y)
            
            addToHistory(temperature, point, TC001ThermalManager.TempMode.POINT)
            
            val pulseSize = 20f + 5f * sin(animationPhase)
            
            val alpha = (128 + 127 * sin(animationPhase * 2)).toInt()
            paint.alpha = alpha
            canvas.drawLine(point.x - pulseSize, point.y, point.x + pulseSize, point.y, paint)
            canvas.drawLine(point.x, point.y - pulseSize, point.x, point.y + pulseSize, paint)
            
            paint.alpha = 255
            val circleRadius = pulseSize * 1.5f
            canvas.drawCircle(point.x, point.y, circleRadius, paint)
            
            val tempText = String.format("%.1f°C", temperature)
            val tempColor = getTemperatureColor(temperature)
            textPaint.color = tempColor
            
            val textBounds = Rect()
            textPaint.getTextBounds(tempText, 0, tempText.length, textBounds)
            
            val textX = point.x - textBounds.width() / 2
            val textY = point.y - pulseSize * 3
            
            val textBg = RectF(
                textX - 10f, textY - textBounds.height() - 5f,
                textX + textBounds.width() + 10f, textY + 5f
            )
            val bgPaint = Paint().apply {
                color = Color.BLACK
                this.alpha = 180
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(textBg, 8f, 8f, bgPaint)
            
            canvas.drawText(tempText, textX, textY, textPaint)
        }
    }
    
    private fun drawLineMeasurement(canvas: Canvas) {
        lineStart?.let { start ->
            lineEnd?.let { end ->
                val lineGradient = LinearGradient(
                    start.x, start.y, end.x, end.y,
                    getTemperatureColor(getMockTemperature(start.x, start.y)),
                    getTemperatureColor(getMockTemperature(end.x, end.y)),
                    Shader.TileMode.CLAMP
                )
                gradientPaint.shader = lineGradient
                gradientPaint.strokeWidth = 6f
                gradientPaint.style = Paint.Style.STROKE
                
                canvas.drawLine(start.x, start.y, end.x, end.y, gradientPaint)
                
                val pulseRadius = 8f + 2f * sin(animationPhase)
                canvas.drawCircle(start.x, start.y, pulseRadius, paint)
                canvas.drawCircle(end.x, end.y, pulseRadius, paint)
                
                val centerX = (start.x + end.x) / 2
                val centerY = (start.y + end.y) / 2
                val distance = sqrt((end.x - start.x).pow(2) + (end.y - start.y).pow(2))
                
                val startTemp = getMockTemperature(start.x, start.y)
                val endTemp = getMockTemperature(end.x, end.y)
                val avgTemp = (startTemp + endTemp) / 2
                val maxTemp = kotlin.math.max(startTemp, endTemp)
                val minTemp = kotlin.math.min(startTemp, endTemp)
                
                addToHistory(avgTemp, PointF(centerX, centerY), TC001ThermalManager.TempMode.LINE)
                
                val tempText = String.format(
                    "Line Analysis\n%.1f°C - %.1f°C\nAvg: %.1f°C\nΔT: %.1f°C\nLength: %.0fpx", 
                    minTemp, maxTemp, avgTemp, abs(maxTemp - minTemp), distance
                )
                
                textPaint.color = Color.WHITE
                textPaint.textSize = 28f
                
                val lines = tempText.split("\n")
                var yOffset = centerY - 80f
                
                lines.forEach { line ->
                    val textBounds = Rect()
                    textPaint.getTextBounds(line, 0, line.length, textBounds)
                    val textX = centerX - textBounds.width() / 2
                    
                    val textBg = RectF(
                        textX - 8f, yOffset - textBounds.height() - 2f,
                        textX + textBounds.width() + 8f, yOffset + 2f
                    )
                    val bgPaint = Paint().apply {
                        color = Color.BLACK
                        this.alpha = 200
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(textBg, 6f, 6f, bgPaint)
                    
                    canvas.drawText(line, textX, yOffset, textPaint)
                    yOffset += textBounds.height() + 4f
                }
            }
        }
    }
    
    private fun drawAreaMeasurement(canvas: Canvas) {
        areaTopLeft?.let { topLeft ->
            areaBottomRight?.let { bottomRight ->
                val left = kotlin.math.min(topLeft.x, bottomRight.x)
                val top = kotlin.math.min(topLeft.y, bottomRight.y)
                val right = kotlin.math.max(topLeft.x, bottomRight.x)
                val bottom = kotlin.math.max(topLeft.y, bottomRight.y)
                
                val rect = RectF(left, top, right, bottom)
                val area = (right - left) * (bottom - top)
                
                val thermalGradient = RadialGradient(
                    (left + right) / 2, (top + bottom) / 2,
                    kotlin.math.max(right - left, bottom - top) / 2,
                    gradientColors,
                    floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f),
                    Shader.TileMode.CLAMP
                )
                
                gradientPaint.shader = thermalGradient
                gradientPaint.style = Paint.Style.FILL
                gradientPaint.alpha = 100
                canvas.drawRect(rect, gradientPaint)
                
                val borderWidth = 4f + 2f * sin(animationPhase)
                paint.strokeWidth = borderWidth
                canvas.drawRect(rect, paint)
                
                val centerX = (left + right) / 2
                val centerY = (top + bottom) / 2
                
                val samplePoints = 25
                val tempSamples = mutableListOf<Float>()
                
                for (i in 0 until 5) {
                    for (j in 0 until 5) {
                        val sampleX = left + (right - left) * i / 4f
                        val sampleY = top + (bottom - top) * j / 4f
                        tempSamples.add(getMockTemperature(sampleX, sampleY))
                    }
                }
                
                val minTemp = tempSamples.minOrNull() ?: 0f
                val maxTemp = tempSamples.maxOrNull() ?: 0f
                val avgTemp = tempSamples.average().toFloat()
                val stdDev = sqrt(tempSamples.map { (it - avgTemp).pow(2) }.average()).toFloat()
                
                addToHistory(avgTemp, PointF(centerX, centerY), TC001ThermalManager.TempMode.AREA)
                
                val tempText = String.format(
                    "Area Analysis\nMin: %.1f°C  Max: %.1f°C\nAvg: %.1f°C  σ: %.1f°C\nΔT: %.1f°C\nArea: %.0fpx²",
                    minTemp, maxTemp, avgTemp, stdDev, maxTemp - minTemp, area
                )
                
                textPaint.color = Color.WHITE
                textPaint.textSize = 26f
                
                val lines = tempText.split("\n")
                val textX = left + 15f
                var yOffset = top + 35f
                
                lines.forEach { line ->
                    val textBounds = Rect()
                    textPaint.getTextBounds(line, 0, line.length, textBounds)
                    
                    val textBg = RectF(
                        textX - 8f, yOffset - textBounds.height() - 2f,
                        textX + textBounds.width() + 8f, yOffset + 2f
                    )
                    val bgPaint = Paint().apply {
                        color = Color.BLACK
                        this.alpha = 220
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(textBg, 6f, 6f, bgPaint)
                    
                    canvas.drawText(line, textX, yOffset, textPaint)
                    yOffset += textBounds.height() + 6f
                }
                
                drawHotColdSpots(canvas, rect, tempSamples)
            }
        }
    }
    
    private fun getMockTemperature(x: Float, y: Float): Float {
        val normalizedX = x / width
        val normalizedY = y / height
        
        val centerDistance = sqrt((normalizedX - 0.5).pow(2) + (normalizedY - 0.5).pow(2))
        val cornerDistance = sqrt((normalizedX - 0.8).pow(2) + (normalizedY - 0.2).pow(2))
        
        val baseTemp = 18f + 2f * sin(System.currentTimeMillis() / 10000f)
        
        val centerHotSpot = 28f * exp(-centerDistance * 5).toFloat()
        
        val cornerHotSpot = 15f * exp(-cornerDistance * 8).toFloat()
        
        val edgeCooling = -5f * kotlin.math.max(
            kotlin.math.max(
                kotlin.math.abs(normalizedX - 0.5f) - 0.3f,
                kotlin.math.abs(normalizedY - 0.5f) - 0.3f
            ),
            0f
        )
        
        val noise = (-1.5f + (Math.random().toFloat() * 3f))
        
        return kotlin.math.max(10f, baseTemp + centerHotSpot + cornerHotSpot + edgeCooling + noise)
    }

    private fun getTemperatureColor(temperature: Float): Int {
        val normalizedTemp = ((temperature - 10f) / 40f).coerceIn(0f, 1f)
        
        return when {
            normalizedTemp < 0.2f -> Color.BLUE
            normalizedTemp < 0.4f -> Color.CYAN
            normalizedTemp < 0.6f -> Color.GREEN
            normalizedTemp < 0.8f -> Color.YELLOW
            else -> Color.RED
        }
    }

    private fun addToHistory(temperature: Float, location: PointF, mode: TC001ThermalManager.TempMode) {
        val measurement = MeasurementData(
            System.currentTimeMillis(),
            temperature,
            PointF(location.x, location.y),
            mode
        )
        
        measurementHistory.add(measurement)
        
        if (measurementHistory.size > maxHistorySize) {
            measurementHistory.removeAt(0)
        }
    }

    private fun drawTemperatureGradient(canvas: Canvas) {
        if (width <= 0 || height <= 0) return
        
        val gradientSize = 50
        val bitmap = Bitmap.createBitmap(gradientSize, gradientSize, Bitmap.Config.ARGB_8888)
        val bitmapCanvas = Canvas(bitmap)
        
        for (x in 0 until gradientSize) {
            for (y in 0 until gradientSize) {
                val temp = getMockTemperature(
                    x.toFloat() * width / gradientSize,
                    y.toFloat() * height / gradientSize
                )
                val color = getTemperatureColor(temp)
                val paint = Paint().apply { 
                    this.color = color
                    this.alpha = 30
                }
                bitmapCanvas.drawPoint(x.toFloat(), y.toFloat(), paint)
            }
        }
        
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
        canvas.drawBitmap(scaledBitmap, 0f, 0f, null)
        
        bitmap.recycle()
        scaledBitmap.recycle()
    }

    private fun drawMeasurementHistory(canvas: Canvas) {
        val currentTime = System.currentTimeMillis()
        
        measurementHistory.forEachIndexed { index, measurement ->
            val age = currentTime - measurement.timestamp
            val maxAge = 30000L
            
            if (age < maxAge) {
                val alpha = (255 * (1f - age.toFloat() / maxAge)).toInt()
                historyPaint.alpha = alpha
                
                val radius = 4f - 2f * (age.toFloat() / maxAge)
                canvas.drawCircle(measurement.location.x, measurement.location.y, radius, historyPaint)
            }
        }
    }

    private fun drawHotColdSpots(canvas: Canvas, rect: RectF, tempSamples: List<Float>) {
        val minTemp = tempSamples.minOrNull() ?: return
        val maxTemp = tempSamples.maxOrNull() ?: return
        
        var minIndex = -1
        var maxIndex = -1
        
        tempSamples.forEachIndexed { index, temp ->
            if (temp == minTemp && minIndex == -1) minIndex = index
            if (temp == maxTemp && maxIndex == -1) maxIndex = index
        }
        
        val minX = rect.left + (rect.right - rect.left) * (minIndex % 5) / 4f
        val minY = rect.top + (rect.bottom - rect.top) * (minIndex / 5) / 4f
        val maxX = rect.left + (rect.right - rect.left) * (maxIndex % 5) / 4f
        val maxY = rect.top + (rect.bottom - rect.top) * (maxIndex / 5) / 4f
        
        val coldPaint = Paint().apply {
            color = Color.BLUE
            this.alpha = 150
            style = Paint.Style.FILL
        }
        canvas.drawCircle(minX, minY, 8f, coldPaint)
        
        val hotPaint = Paint().apply {
            color = Color.RED
            this.alpha = 150
            style = Paint.Style.FILL
        }
        canvas.drawCircle(maxX, maxY, 8f, hotPaint)
    }

    fun setShowTemperatureGradient(show: Boolean) {
        showTemperatureGradient = show
        invalidate()
    }
    
    fun setShowMeasurementHistory(show: Boolean) {
        showMeasurementHistory = show
        invalidate()
    }
    
    fun clearMeasurementHistory() {
        measurementHistory.clear()
        invalidate()
    }
    
    fun getMeasurementHistory(): List<MeasurementData> = measurementHistory.toList()
}