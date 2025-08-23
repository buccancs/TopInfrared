package com.guide.zm04c.matrix.utils

import android.graphics.Paint
import android.graphics.Rect

class StringUtils {

    companion object {

        fun isBlank(str: String?): Boolean {
            return str == null || str.trim().length == 0
        }

        fun createFileName(tiemStr: String): String {
            return "_" + tiemStr
        }

        fun dateString(date: String): String {
            val year = date.substring(0, 4)
            val month = date.substring(4, 6)
            val day = date.substring(6, 8)
            return "$year-$month-$day"
        }

        fun equals(a: CharSequence?, b: CharSequence?): Boolean {
            if (a === b)
                return true

            if (a != null && b != null && (a.length == b.length)) {
                if (a is String && b is String) {
                    return a == b
                } else {
                    for (i in 0 until a.length) {
                        if (a[i] != b[i])
                            return false
                    }
                    return true
                }
            }
            return false
        }

        public fun getStringSize(str: String, textSizePxVal: Float): IntArray {
            if (textSizePxVal < 0) {
                throw IllegalArgumentException("textSizePxVal > 0 need")
            }
            val paint = Paint()
            paint.textSize = textSizePxVal
            val bounds = Rect()
            if (str.length > 0) {
                paint.getTextBounds(str, 0, str.length, bounds)
            }
            return intArrayOf(bounds.width(), bounds.height())
        }

    }
}