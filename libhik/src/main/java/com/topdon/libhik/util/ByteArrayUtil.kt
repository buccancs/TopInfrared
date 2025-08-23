package com.topdon.libhik.util

internal object ByteArrayUtil {
    
    internal fun ByteArray.toInt(index: Int): Int = try {
        (this[index].toInt() and 0xff) or (this[index + 1].toInt() and 0xff shl 8) or (this[index + 2].toInt() and 0xff shl 16) or (this[index + 3].toInt() and 0xff shl 24)
    } catch (_: IndexOutOfBoundsException) {
        0
    }

    internal fun ByteArray.toFloat(index: Int): Float = try {
        java.lang.Float.intBitsToFloat((this[index].toInt() and 0xff) or (this[index + 1].toInt() and 0xff shl 8) or (this[index + 2].toInt() and 0xff shl 16) or (this[index + 3].toInt() and 0xff shl 24))
    } catch (_: IndexOutOfBoundsException) {
        0f
    }

    internal fun ByteArray.toStr(startIndex: Int, size: Int): String = try {
        var validCount = 0
        for (i in startIndex until (startIndex + size)) {
            if (this[i] == 0.toByte()) {
                break
            }
            validCount++
        }
        val nameBytes = ByteArray(validCount)
        System.arraycopy(this, startIndex, nameBytes, 0, validCount)
        String(nameBytes)
    } catch (_: IndexOutOfBoundsException) {
        ""
    }

    internal fun ByteArray.buildPrintStr(startIndex: Int, size: Int): String = try {
        val stringBuilder = StringBuilder()
        for (i in startIndex until (startIndex + size)) {
            val str: String = (this[i].toInt() and 0xff).toString(16)
            if (str.length < 2) {
                stringBuilder.append("0")
            }
            stringBuilder.append(str)
            if (i < startIndex + size - 1) {
                stringBuilder.append(" ")
            }
        }
        stringBuilder.toString()
    } catch (_: IndexOutOfBoundsException) {
        "数组越界了哥们"
    }
}