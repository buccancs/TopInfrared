package com.topdon.lib.core.utils

import java.util.*

object ByteUtils {

    fun ByteArray.toHexString(separator: String = " ") = asUByteArray().joinToString(separator) {
        it.toString(16).padStart(2, '0').uppercase(Locale.getDefault())
    }

    fun ByteArray.toHexMd5String() = asUByteArray().joinToString(":") {
        it.toString(16).padStart(2, '0').uppercase(Locale.getDefault())
    }

    fun String.hexStringToByteArray() =
        ByteArray(this.length / 2) { this.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    fun UUID.getTag() = toString().substring(4, 8)

    fun ByteArray.bytesToInt() = run {
        var total = 0
        val size = this.size
        for (i in 0 until size) {
            total += this[i].toUByte().toInt().shl((size - i - 1) * 8)
        }
        total
    }

    fun ByteArray.bytesToLong() = run {
        var total = 0L
        val size = this.size
        for (i in 0 until size) {
            total += this[i].toUByte().toInt().shl((size - i - 1) * 8)
        }
        total
    }

    fun Int.toBytes(size: Int) = run {
        var data = byteArrayOf()
        for (i in 0 until size) {
            data = data.plus(this.shr((size - i - 1) * 8).toByte())
        }
        data
    }

    fun String.toBytes(size: Int) = run {
        val data = ByteArray(size)
        val srcBytes = this.toByteArray()
        if (srcBytes.size > size) {
            srcBytes.copyInto(data, 0, 0, size)
        } else {
            srcBytes.copyInto(data, 0, 0, srcBytes.size)
        }
        return@run data
    }

    fun Long.toBytes(size: Int) = run {
        var data = byteArrayOf()
        for (i in 0 until size) {
            data = data.plus(this.shr((size - i - 1) * 8).toByte())
        }
        data
    }

    fun Int.getIndex(index: Int): Int = run {
        val a = this % (1 shl (index * 4))
        return a shr ((index - 1) * 4)
    }

    fun ByteArray.descBytes() = run {
        var data = byteArrayOf()
        for (i in 0 until this.size) {
            data = data.plus(this[this.size - 1 - i])
        }
        return@run data
    }

    fun bigBytesToInt(vararg bytes: Byte): Int {
        val byteCount = bytes.size.coerceAtMost(4)
        var result = 0
        for (i in 0 until byteCount) {
            result = result or (bytes[i].toInt().and(0xff).shl(8 * (byteCount - 1 - i)))
        }
        return result
    }

    fun Float.toLittleBytes(): ByteArray {
        val result = ByteArray(4)
        val floatBit: Int = java.lang.Float.floatToIntBits(this)
        for (i in 0 until 4) {
            result[i] = (floatBit shr (i * 8)).toByte()
        }
        return result
    }
}