package com.topdon.module.thermal.ir.utils

import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.roundToLong


//获取真实的x坐标
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

/**
 * Returns the values between min and max, inclusive, that are divisible by divisor
 * @param min The minimum value
 * @param max The maximum value
 * @param divisor The divisor
 * @return The values between min and max, inclusive, that are divisible by divisor
 */
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

/**
 * Gets the pixel coordinate of a point on the screen given the bearing and azimuth. The point is considered to be on a plane.
 * @param bearing The compass bearing in degrees of the point
 * @param azimuth The compass bearing in degrees that the user is facing (center of the screen)
 * @param viewWidth The size of the view in pixels
 * @param fovWidth The field of view of the camera in degrees
 */
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
    // These will be at most 360 degrees apart, so normalize them to restrict that
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
    // https://stackoverflow.com/questions/14415753/wrap-value-into-range-min-max-without-division
    val range = max - min
    if (value < min) {
        return max - (min - value) % range
    }

    if (value > max) {
        return min + (value - min) % range
    }

    return value
}