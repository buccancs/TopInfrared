package com.topinfrared.tc001.standalone.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.topinfrared.tc001.standalone.thermal.TC001ThermalManager

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
        alpha = 64 // Semi-transparent
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    companion object {
        private const val TAG = "ThermalOverlayView"
    }
    
    init {
        setBackgroundColor(Color.TRANSPARENT)
    }
    
    fun setTemperatureMeasureMode(mode: TC001ThermalManager.TempMode) {
        currentMode = mode
        // Clear previous measurements when mode changes
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
            // Draw crosshair
            val size = 20f
            canvas.drawLine(point.x - size, point.y, point.x + size, point.y, paint)
            canvas.drawLine(point.x, point.y - size, point.x, point.y + size, paint)
            
            // Draw circle around point
            canvas.drawCircle(point.x, point.y, size * 1.5f, paint)
            
            // Mock temperature reading
            val temperature = getMockTemperature(point.x, point.y)
            val tempText = String.format("%.1f°C", temperature)
            
            // Draw temperature text with background
            val textBounds = Rect()
            textPaint.getTextBounds(tempText, 0, tempText.length, textBounds)
            
            val textX = point.x - textBounds.width() / 2
            val textY = point.y - size * 3
            
            canvas.drawText(tempText, textX, textY, textPaint)
        }
    }
    
    private fun drawLineMeasurement(canvas: Canvas) {
        lineStart?.let { start ->
            lineEnd?.let { end ->
                // Draw line
                canvas.drawLine(start.x, start.y, end.x, end.y, paint)
                
                // Draw endpoints
                canvas.drawCircle(start.x, start.y, 8f, paint)
                canvas.drawCircle(end.x, end.y, 8f, paint)
                
                // Calculate center point for temperature display
                val centerX = (start.x + end.x) / 2
                val centerY = (start.y + end.y) / 2
                
                // Mock temperature readings
                val startTemp = getMockTemperature(start.x, start.y)
                val endTemp = getMockTemperature(end.x, end.y)
                val avgTemp = (startTemp + endTemp) / 2
                
                val tempText = String.format("%.1f°C - %.1f°C\nAvg: %.1f°C", startTemp, endTemp, avgTemp)
                
                canvas.drawText(tempText, centerX - 60f, centerY - 40f, textPaint)
            }
        }
    }
    
    private fun drawAreaMeasurement(canvas: Canvas) {
        areaTopLeft?.let { topLeft ->
            areaBottomRight?.let { bottomRight ->
                val left = minOf(topLeft.x, bottomRight.x)
                val top = minOf(topLeft.y, bottomRight.y)
                val right = maxOf(topLeft.x, bottomRight.x)
                val bottom = maxOf(topLeft.y, bottomRight.y)
                
                val rect = RectF(left, top, right, bottom)
                
                // Draw rectangle with fill
                canvas.drawRect(rect, fillPaint)
                canvas.drawRect(rect, paint)
                
                // Mock temperature analysis for area
                val centerX = (left + right) / 2
                val centerY = (top + bottom) / 2
                
                val minTemp = getMockTemperature(left, top) - 2f
                val maxTemp = getMockTemperature(right, bottom) + 2f
                val avgTemp = getMockTemperature(centerX, centerY)
                
                val tempText = String.format("Min: %.1f°C\nMax: %.1f°C\nAvg: %.1f°C", minTemp, maxTemp, avgTemp)
                
                val textX = left + 10f
                val textY = top + 40f
                
                canvas.drawText(tempText, textX, textY, textPaint)
            }
        }
    }
    
    private fun getMockTemperature(x: Float, y: Float): Float {
        // Mock temperature calculation based on position
        // This would interface with real TC001 temperature data
        val normalizedX = x / width
        val normalizedY = y / height
        val distance = kotlin.math.sqrt((normalizedX - 0.5) * (normalizedX - 0.5) + (normalizedY - 0.5) * (normalizedY - 0.5))
        
        // Base temperature with hot spot in center
        val baseTemp = 20f
        val hotSpot = 25f * kotlin.math.exp(-distance * 4).toFloat()
        
        return baseTemp + hotSpot + (-2f + (Math.random().toFloat() * 4f)) // Add some noise
    }
}