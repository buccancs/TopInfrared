package com.topdon.module.thermal.ir.overlay

import android.graphics.*
import android.text.TextPaint
import java.text.SimpleDateFormat
import java.util.*

/**
 * Temperature Overlay Manager for bucika_gsr
 * Handles temperature measurement overlays for recorded videos
 */
class TemperatureOverlayManager {
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    // Paint configurations for different overlay elements
    private val temperaturePaint = TextPaint().apply {
        color = Color.RED
        textSize = 36f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }
    
    private val backgroundPaint = Paint().apply {
        color = Color.argb(128, 0, 0, 0) // Semi-transparent black
        style = Paint.Style.FILL
    }
    
    private val crosshairPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 3f
        style = Paint.Style.STROKE
    }
    
    private val timestampPaint = TextPaint().apply {
        color = Color.WHITE
        textSize = 24f
        isAntiAlias = true
    }
    
    private val gsrPaint = TextPaint().apply {
        color = Color.CYAN
        textSize = 28f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    /**
     * Add comprehensive temperature and GSR overlay to a frame
     */
    fun addOverlayToFrame(
        frame: Bitmap,
        temperature: Float,
        x: Float,
        y: Float,
        gsrValue: Double? = null,
        skinTemperature: Double? = null,
        timestamp: Long = System.currentTimeMillis(),
        mode: String = "Samsung 4K"
    ): Bitmap {
        val overlayBitmap = frame.copy(frame.config, true)
        val canvas = Canvas(overlayBitmap)
        
        // Add crosshair at measurement point
        drawCrosshair(canvas, x, y)
        
        // Add temperature reading with background
        drawTemperatureReading(canvas, temperature, x, y)
        
        // Add timestamp
        drawTimestamp(canvas, timestamp, mode)
        
        // Add GSR data if available
        if (gsrValue != null && skinTemperature != null) {
            drawGSRData(canvas, gsrValue, skinTemperature)
        }
        
        // Add recording mode indicator
        drawRecordingMode(canvas, mode)
        
        return overlayBitmap
    }

    /**
     * Draw crosshair at measurement point
     */
    private fun drawCrosshair(canvas: Canvas, x: Float, y: Float) {
        val crosshairSize = 30f
        
        // Horizontal line
        canvas.drawLine(
            x - crosshairSize, y,
            x + crosshairSize, y,
            crosshairPaint
        )
        
        // Vertical line
        canvas.drawLine(
            x, y - crosshairSize,
            x, y + crosshairSize,
            crosshairPaint
        )
        
        // Center circle
        canvas.drawCircle(x, y, 5f, crosshairPaint)
    }

    /**
     * Draw temperature reading with background
     */
    private fun drawTemperatureReading(canvas: Canvas, temperature: Float, x: Float, y: Float) {
        val tempText = "${temperature}°C"
        val textWidth = temperaturePaint.measureText(tempText)
        val textHeight = temperaturePaint.textSize
        
        // Position text above the measurement point
        val textX = x - textWidth / 2
        val textY = y - 40f
        
        // Draw background rectangle
        val backgroundRect = RectF(
            textX - 8f,
            textY - textHeight,
            textX + textWidth + 8f,
            textY + 8f
        )
        canvas.drawRoundRect(backgroundRect, 8f, 8f, backgroundPaint)
        
        // Draw temperature text
        canvas.drawText(tempText, textX, textY, temperaturePaint)
    }

    /**
     * Draw timestamp in top-left corner
     */
    private fun drawTimestamp(canvas: Canvas, timestamp: Long, mode: String) {
        val timeText = dateFormat.format(Date(timestamp))
        val modeText = "[$mode]"
        
        // Background for timestamp
        val timestampWidth = Math.max(
            timestampPaint.measureText(timeText),
            timestampPaint.measureText(modeText)
        )
        val backgroundRect = RectF(
            16f, 16f,
            16f + timestampWidth + 16f,
            16f + timestampPaint.textSize * 2 + 16f
        )
        canvas.drawRoundRect(backgroundRect, 8f, 8f, backgroundPaint)
        
        // Draw timestamp
        canvas.drawText(timeText, 24f, 16f + timestampPaint.textSize, timestampPaint)
        canvas.drawText(modeText, 24f, 16f + timestampPaint.textSize * 2, timestampPaint)
    }

    /**
     * Draw GSR data in top-right corner
     */
    private fun drawGSRData(canvas: Canvas, gsrValue: Double, skinTemperature: Double) {
        val gsrText = "GSR: ${String.format("%.3f", gsrValue)}µS"
        val skinText = "Skin: ${String.format("%.2f", skinTemperature)}°C"
        
        val maxWidth = Math.max(
            gsrPaint.measureText(gsrText),
            gsrPaint.measureText(skinText)
        )
        
        val rightX = canvas.width - 24f - maxWidth
        
        // Background for GSR data
        val backgroundRect = RectF(
            rightX - 8f, 16f,
            canvas.width - 16f,
            16f + gsrPaint.textSize * 2 + 16f
        )
        canvas.drawRoundRect(backgroundRect, 8f, 8f, backgroundPaint)
        
        // Draw GSR data
        canvas.drawText(gsrText, rightX, 16f + gsrPaint.textSize, gsrPaint)
        canvas.drawText(skinText, rightX, 16f + gsrPaint.textSize * 2, gsrPaint)
    }

    /**
     * Draw recording mode indicator in bottom-right corner
     */
    private fun drawRecordingMode(canvas: Canvas, mode: String) {
        val modeText = when (mode) {
            "Samsung 4K" -> "● REC Samsung 4K 30FPS"
            "RAD DNG Level 3" -> "● REC RAD DNG L3 30FPS"
            "Parallel Dual" -> "● REC Parallel Dual Stream"
            else -> "● REC $mode"
        }
        
        val textWidth = temperaturePaint.measureText(modeText)
        val textX = canvas.width - textWidth - 24f
        val textY = canvas.height - 24f
        
        // Background
        val backgroundRect = RectF(
            textX - 8f, textY - temperaturePaint.textSize,
            textX + textWidth + 8f, textY + 8f
        )
        canvas.drawRoundRect(backgroundRect, 8f, 8f, backgroundPaint)
        
        // Draw recording indicator (red dot for recording)
        val recordingPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        canvas.drawCircle(textX + 16f, textY - temperaturePaint.textSize / 2, 8f, recordingPaint)
        
        // Draw mode text
        canvas.drawText(modeText, textX, textY, temperaturePaint)
    }

    /**
     * Add overlay for parallel recording with dual streams
     */
    fun addDualStreamOverlay(
        thermalFrame: Bitmap,
        visualFrame: Bitmap,
        temperature: Float,
        x: Float,
        y: Float,
        gsrValue: Double?,
        skinTemperature: Double?
    ): Pair<Bitmap, Bitmap> {
        val overlayThermal = addOverlayToFrame(
            thermalFrame, temperature, x, y, 
            gsrValue, skinTemperature, System.currentTimeMillis(), "Thermal"
        )
        
        val overlayVisual = addOverlayToFrame(
            visualFrame, temperature, x, y,
            gsrValue, skinTemperature, System.currentTimeMillis(), "Visual"
        )
        
        return Pair(overlayThermal, overlayVisual)
    }

    /**
     * Create temperature measurement grid overlay
     */
    fun addGridOverlay(frame: Bitmap, gridSize: Int = 8): Bitmap {
        val overlayBitmap = frame.copy(frame.config, true)
        val canvas = Canvas(overlayBitmap)
        
        val gridPaint = Paint().apply {
            color = Color.argb(64, 255, 255, 255) // Semi-transparent white
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        
        val stepX = canvas.width / gridSize.toFloat()
        val stepY = canvas.height / gridSize.toFloat()
        
        // Draw vertical lines
        for (i in 1 until gridSize) {
            val x = i * stepX
            canvas.drawLine(x, 0f, x, canvas.height.toFloat(), gridPaint)
        }
        
        // Draw horizontal lines
        for (i in 1 until gridSize) {
            val y = i * stepY
            canvas.drawLine(0f, y, canvas.width.toFloat(), y, gridPaint)
        }
        
        return overlayBitmap
    }
}