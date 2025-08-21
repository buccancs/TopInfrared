package com.topdon.lib.core.bean

import com.topdon.lib.core.utils.ByteUtils
import com.topdon.lib.core.utils.CommUtils

data class WatermarkBean(
    var isOpen: Boolean = false,
    var title: String = CommUtils.getAppName(),//最多30个字符
    var address: String = "",//最多80个字符
    var isAddTime: Boolean = false,
) {
    companion object {
        fun loadFromArray(data: ByteArray): WatermarkBean {

            val titleLen = ByteUtils.bigBytesToInt(data[1], data[2], data[3], data[4])
            val titleBytes = ByteArray(titleLen)
            System.arraycopy(data, 5, titleBytes, 0, titleBytes.size)

            val addressLen = ByteUtils.bigBytesToInt(data[125], data[126], data[127], data[128])
            val addressBytes = ByteArray(addressLen)
            System.arraycopy(data, 129, addressBytes, 0, addressBytes.size)

            return WatermarkBean(
                isOpen = data[0].toInt() == 1,
                title = if (titleLen == 0) "" else String(titleBytes),
                address = if (addressLen == 0) "" else String(addressBytes),
                isAddTime = data[449].toInt() == 1,
            )
        }
    }

    fun toByteArray(): ByteArray {
        val result = ByteArray(450)

        val titleByteArray = title.toByteArray()
        val addressByteArray = address.toByteArray()

        result[0] = if (isOpen) 1 else 0

        result[1] = (titleByteArray.size ushr 24).toByte()
        result[2] = (titleByteArray.size ushr 16).toByte()
        result[3] = (titleByteArray.size ushr 8).toByte()
        result[4] = titleByteArray.size.toByte()
        System.arraycopy(titleByteArray, 0, result, 5, titleByteArray.size)

        result[125] = (addressByteArray.size ushr 24).toByte()
        result[126] = (addressByteArray.size ushr 16).toByte()
        result[127] = (addressByteArray.size ushr 8).toByte()
        result[128] = addressByteArray.size.toByte()
        System.arraycopy(addressByteArray, 0, result, 129, addressByteArray.size)

        result[449] = if (isAddTime) 1 else 0

        return result
    }
}
