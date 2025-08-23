package com.topdon.module.thermal.ir.overlay

import android.graphics.*
import android.text.TextPaint
import java.text.SimpleDateFormat
import java.util.*

class TemperatureOverlayManager {
    
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    private val temperaturePaint = TextPaint().apply {
        color = Color.RED
        textSize = 36f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }
    
    private val backgroundPaint = Paint().apply {
        color = Color.argb(128, 0, 0, 0)
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
        
        drawCrosshair(canvas, x, y)
        
        drawTemperatureReading(canvas, temperature, x, y)
        
        drawTimestamp(canvas, timestamp, mode)
        
        if (gsrValue != null && skinTemperature != null) {
            drawGSRData(canvas, gsrValue, skinTemperature)
        }
        
        drawRecordingMode(canvas, mode)
        
        return overlayBitmap
    }

    private fun drawCrosshair(canvas: Canvas, x: Float, y: Float) {
        val crosshairSize = 30f
        
        canvas.drawLine(
            x - crosshairSize, y,
            x + crosshairSize, y,
            crosshairPaint
        )
        
        canvas.drawLine(
            x, y - crosshairSize,
            x, y + crosshairSize,
            crosshairPaint
        )
        
        canvas.drawCircle(x, y, 5f, crosshairPaint)
    }

    private fun drawTemperatureReading(canvas: Canvas, temperature: Float, x: Float, y: Float) {
        val tempText = "${temperature}°C"
        val textWidth = temperaturePaint.measureText(tempText)
        val textHeight = temperaturePaint.textSize
        
        val textX = x - textWidth / 2
        val textY = y - 40f
        
        val backgroundRect = RectF(
            textX - 8f,
            textY - textHeight,
            textX + textWidth + 8f,
            textY + 8f
        )
        canvas.drawRoundRect(backgroundRect, 8f, 8f, backgroundPaint)
        
        canvas.drawText(tempText, textX, textY, temperaturePaint)
    }

    private fun drawTimestamp(canvas: Canvas, timestamp: Long, mode: String) {
        val timeText = dateFormat.format(Date(timestamp))
        val modeText = "[$mode]"
        
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
        
        canvas.drawText(timeText, 24f, 16f + timestampPaint.textSize, timestampPaint)
        canvas.drawText(modeText, 24f, 16f + timestampPaint.textSize * 2, timestampPaint)
    }

    private fun drawGSRData(canvas: Canvas, gsrValue: Double, skinTemperature: Double) {
        val gsrText = "GSR: ${String.format("%.3f", gsrValue)}µS"
        val skinText = "Skin: ${String.format("%.2f", skinTemperature)}°C"
        
        val maxWidth = Math.max(
            gsrPaint.measureText(gsrText),
            gsrPaint.measureText(skinText)
        )
        
        val rightX = canvas.width - 24f - maxWidth
        
        val backgroundRect = RectF(
            rightX - 8f, 16f,
            canvas.width - 16f,
            16f + gsrPaint.textSize * 2 + 16f
        )
        canvas.drawRoundRect(backgroundRect, 8f, 8f, backgroundPaint)
        
        canvas.drawText(gsrText, rightX, 16f + gsrPaint.textSize, gsrPaint)
        canvas.drawText(skinText, rightX, 16f + gsrPaint.textSize * 2, gsrPaint)
    }

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
        
        val backgroundRect = RectF(
            textX - 8f, textY - temperaturePaint.textSize,
            textX + textWidth + 8f, textY + 8f
        )
        canvas.drawRoundRect(backgroundRect, 8f, 8f, backgroundPaint)
        
        val recordingPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        canvas.drawCircle(textX + 16f, textY - temperaturePaint.textSize / 2, 8f, recordingPaint)
        
        canvas.drawText(modeText, textX, textY, temperaturePaint)
    }

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

    fun addGridOverlay(frame: Bitmap, gridSize: Int = 8): Bitmap {
        val overlayBitmap = frame.copy(frame.config, true)
        val canvas = Canvas(overlayBitmap)
        
        val gridPaint = Paint().apply {
            color = Color.argb(64, 255, 255, 255)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        
        val stepX = canvas.width / gridSize.toFloat()
        val stepY = canvas.height / gridSize.toFloat()
        
        for (i in 1 until gridSize) {
            val x = i * stepX
            canvas.drawLine(x, 0f, x, canvas.height.toFloat(), gridPaint)
        }
        
        for (i in 1 until gridSize) {
            val y = i * stepY
            canvas.drawLine(0f, y, canvas.width.toFloat(), y, gridPaint)
        }
        
        return overlayBitmap
    }
}