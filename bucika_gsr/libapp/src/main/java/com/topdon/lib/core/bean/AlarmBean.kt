package com.topdon.lib.core.bean

import java.nio.ByteBuffer

/**
 * @author: CaiSongL
 * @date: 2023/5/16 15:56
 */
data class AlarmBean(
    var isHighOpen: Boolean = false,
    var isLowOpen: Boolean = false,
    var highTemp: Float = Float.MAX_VALUE,
    var lowTemp: Float = Float.MIN_VALUE,

    var isMarkOpen: Boolean = true,
    var highColor: Int = 0xffff0000.toInt(),
    var lowColor: Int = 0xff0000ff.toInt(),
    var markType: Int = TYPE_ALARM_MARK_STROKE,

    var isRingtoneOpen: Boolean = false,
    var ringtoneType: Int = 0,
) {
    companion object {
        const val TYPE_ALARM_MARK_STROKE = 1
        const val TYPE_ALARM_MARK_MATRIX = 2

        fun loadFromArray(data: ByteArray): AlarmBean {
            val buffer = ByteBuffer.wrap(data)
            val isHighOpen = buffer.get() == 1.toByte()
            val isLowOpen = buffer.get() == 1.toByte()
            val highTemp = buffer.float
            val lowTemp = buffer.float

            val isMarkOpen = buffer.get() == 1.toByte()
            val highColor = buffer.int
            val lowColor = buffer.int
            val markType = buffer.int

            val isRingtoneOpen = buffer.get() == 1.toByte()
            val ringtoneType = buffer.int

            return AlarmBean(
                isHighOpen = isHighOpen,
                isLowOpen = isLowOpen,
                highTemp = highTemp,
                lowTemp = lowTemp,

                isMarkOpen = isMarkOpen,
                highColor = if (highColor == 0) 0xffff0000.toInt() else highColor,
                lowColor = if (lowColor == 0) 0xff0fa752.toInt() else lowColor,
                markType = if (markType == 0) 1 else markType,

                isRingtoneOpen = isRingtoneOpen,
                ringtoneType = ringtoneType,
            )
        }
    }


    fun toByteArray(): ByteArray = ByteBuffer.allocate(28)
        .put(if (isHighOpen) 1 else 0)
        .put(if (isLowOpen) 1 else 0)
        .putFloat(highTemp)
        .putFloat(lowTemp)
        .put(if (isMarkOpen) 1 else 0)
        .putInt(highColor)
        .putInt(lowColor)
        .putInt(markType)
        .put(if (isRingtoneOpen) 1 else 0)
        .putInt(ringtoneType)
        .array()

    /**
     * 判断温度报警是否开启
     */
    fun isOpen(): Boolean = isHighOpen || isLowOpen
}