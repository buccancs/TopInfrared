package com.topdon.module.thermal.ir.utils

import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.roundToLong

fun realX(str: String, x: Float,paint: Paint) = x - textWidth(str,paint) / 2f

fun realY(str: String, y: Float,paint: Paint) = y - textHeight(str,paint) / 4f

fun textWidth(text: String,paint: Paint): Float {
    return textDimensions(text,paint).first
}

fun textHeight(text: String,paint: Paint): Float {
    return textDimensions(text,paint).second
}

val measurementRect = Rect()
fun textDimensions(text: String,paint: Paint): Pair<Float, Float> {
    paint.getTextBounds(text, 0, text.length, measurementRect)
    return measurementRect.width().toFloat() to measurementRect.height().toFloat()
}

fun getValuesBetween(min: Float, max: Float, divisor: Float): List<Float> {
    val values = mutableListOf<Float>()
    val start = min.roundNearest(divisor)
    var i = start
    while (i <= max) {
        if (i >= min) {
            values.add(i)
        }
        i += divisor
    }
    return values
}

fun Float.roundNearest(nearest: Float): Float {
    return (this / nearest).roundToLong() * nearest
}

fun getPixelLinear(
    bearing: Float,
    azimuth: Float,
    viewWidth: Float,
    fovWidth: Float
): Float {
    val newBearing = deltaAngle(azimuth, bearing)
    val wPixelsPerDegree = viewWidth / fovWidth
    return viewWidth / 2f + newBearing * wPixelsPerDegree
}

fun deltaAngle(angle1: Float, angle2: Float): Float {
    val a = normalizeAngle(angle1 - angle2)
    val b = normalizeAngle(angle2 - angle1)
    return if (a < b) {
        -a
    } else {
        b
    }
}

fun normalizeAngle(angle: Float): Float {
    return wrap(angle, 0f, 360f) % 360
}

fun wrap(value: Float, min: Float, max: Float): Float {
    return wrap(value.toDouble(), min.toDouble(), max.toDouble()).toFloat()
}

fun wrap(value: Double, min: Double, max: Double): Double {
    val range = max - min
    if (value < min) {
        return max - (min - value) % range
    }

    if (value > max) {
        return min + (value - min) % range
    }

    return value
}