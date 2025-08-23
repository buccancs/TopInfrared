package com.topdon.module.thermal.ir.frame

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.bean.AlarmBean
import com.topdon.lib.core.bean.WatermarkBean
import com.topdon.lib.core.common.ProductType.PRODUCT_NAME_TC007
import com.topdon.lib.core.utils.ByteUtils
import com.topdon.lib.core.utils.ByteUtils.toBytes
import com.topdon.pseudo.bean.CustomPseudoBean

class FrameStruct() {
    companion object {
        
        private const val SIZE = 1024

        fun toCode(
            name : String,
            width: Int,
            height: Int,
            rotate: Int,
            pseudo: Int,
            initRotate: Int,
            correctRotate: Int,
            customPseudoBean: CustomPseudoBean,
            isShowPseudoBar: Boolean,
            textColor: Int,
            watermarkBean: WatermarkBean,
            alarmBean: AlarmBean,
            gainStatus:Int,
            textSize:Int,
            environment:Float,
            distance : Float,
            radiation : Float,
            isAmplify : Boolean
        ): ByteArray {
            val resultArray = ByteArray(SIZE)

            resultArray[0] = (SIZE ushr 8).toByte()
            resultArray[1] = SIZE.toByte()

            val nameBytes = name.toBytes(16)
            System.arraycopy(nameBytes, 0, resultArray, 2, nameBytes.size)

            val verBytes = AppUtils.getAppVersionName().toBytes(8)
            System.arraycopy(verBytes, 0, resultArray, 18, verBytes.size)

            resultArray[26] = (width ushr 8).toByte()
            resultArray[27] = width.toByte()

            resultArray[28] = (height ushr 8).toByte()
            resultArray[29] = height.toByte()

            resultArray[30] = (rotate ushr 8).toByte()
            resultArray[31] = rotate.toByte()

            resultArray[32] = (pseudo ushr 8).toByte()
            resultArray[33] = pseudo.toByte()

            resultArray[34] = (initRotate ushr 8).toByte()
            resultArray[35] = initRotate.toByte()

            resultArray[36] = (correctRotate ushr 8).toByte()
            resultArray[37] = correctRotate.toByte()

            val customPseudoArray = customPseudoBean.toByteArray()
            System.arraycopy(customPseudoArray, 0, resultArray, 81, customPseudoArray.size)

            resultArray[173] = if (isShowPseudoBar) 1 else 0

            resultArray[174] = (textColor ushr 24).toByte()
            resultArray[175] = (textColor ushr 16).toByte()
            resultArray[176] = (textColor ushr 8).toByte()
            resultArray[177] = textColor.toByte()

            val watermarkArray = watermarkBean.toByteArray()
            System.arraycopy(watermarkArray, 0, resultArray, 178, watermarkArray.size)

            val alarmArray = alarmBean.toByteArray()
            System.arraycopy(alarmArray, 0, resultArray, 628, alarmArray.size)
            resultArray[657] = gainStatus.toByte()

            resultArray[658] = (textSize ushr 8).toByte()
            resultArray[659] = textSize.toByte()

            val envBytes = java.nio.ByteBuffer.allocate(4).putFloat(environment).array()
            val distanceBytes = java.nio.ByteBuffer.allocate(4).putFloat(distance).array()
            val radiationBytes = java.nio.ByteBuffer.allocate(4).putFloat(radiation).array()

            System.arraycopy(envBytes, 0, resultArray, 660, 4)
            System.arraycopy(distanceBytes, 0, resultArray, 664, 4)
            System.arraycopy(radiationBytes, 0, resultArray, 668, 4)

            resultArray[672] = if (isAmplify) 1 else 0
            return resultArray
        }
    }

    var len = 0
    var name: String = ""
    var ver: String = ""
    var width = 0
    var height = 0
    var rotate = 0
    var pseudo = 0
    var initRotate = 0
    var correctRotate = 0
    var customPseudoBean = CustomPseudoBean()
    var isShowPseudoBar = false
    var textColor = 0xffffffff.toInt()
    var watermarkBean = WatermarkBean()
    var alarmBean = AlarmBean()
    var gainStatus : Int = 1
    var textSize : Int = SizeUtils.sp2px(14f)
    var environment : Float = 0f
    var distance : Float = 0f
    var radiation : Float = 0f
    var isAmplify : Boolean = false

    constructor(data: ByteArray): this() {
        len = (data[0].toInt() and 0xff shl 8) or (data[1].toInt() and 0xff)

        var nameEndIndex = 17
        for (i in 17 downTo 2) {
            if (data[i].toInt() != 0) {
                nameEndIndex = i
                break
            }
        }
        val nameBytes = ByteArray(nameEndIndex - 1)
        System.arraycopy(data, 2, nameBytes, 0, nameBytes.size)
        name = String(nameBytes)

        val verBytes = ByteArray(8)
        System.arraycopy(data, 18, verBytes, 0, verBytes.size)
        ver = String(verBytes)

        width = (data[26].toInt() and 0xff shl 8) or (data[27].toInt() and 0xff)
        height = (data[28].toInt() and 0xff shl 8) or (data[29].toInt() and 0xff)
        rotate = (data[30].toInt() and 0xff shl 8) or (data[31].toInt() and 0xff)
        pseudo = (data[32].toInt() and 0xff shl 8) or (data[33].toInt() and 0xff)
        initRotate = (data[34].toInt() and 0xff shl 8) or (data[35].toInt() and 0xff)
        correctRotate = (data[36].toInt() and 0xff shl 8) or (data[37].toInt() and 0xff)

        val customPseudoArray = ByteArray(92)
        System.arraycopy(data, 81, customPseudoArray, 0, customPseudoArray.size)
        customPseudoBean = CustomPseudoBean.toCustomPseudoBean(customPseudoArray)

        isShowPseudoBar = data[173].toInt() == 1

        textColor = ByteUtils.bigBytesToInt(data[174], data[175], data[176], data[177])
        if (textColor == 0) {
            textColor = 0xffffffff.toInt()
        }

        val watermarkArray = ByteArray(450)
        System.arraycopy(data, 178, watermarkArray, 0, watermarkArray.size)
        watermarkBean = WatermarkBean.loadFromArray(watermarkArray)

        val alarmArray = ByteArray(28)
        System.arraycopy(data, 628, alarmArray, 0, alarmArray.size)
        alarmBean = AlarmBean.loadFromArray(alarmArray)
        gainStatus = data[657].toInt()
        val tmpTextSize = (data[658].toInt() and 0xff shl 8) or (data[659].toInt() and 0xff)
        if (tmpTextSize >= SizeUtils.sp2px(14f)){
            textSize = tmpTextSize
        }

        val envBytes = data.copyOfRange(660, 664)
        val distanceBytes = data.copyOfRange(664, 668)
        val radiationBytes = data.copyOfRange(668, 672)

        environment = java.nio.ByteBuffer.wrap(envBytes).float
        distance = java.nio.ByteBuffer.wrap(distanceBytes).float
        radiation = java.nio.ByteBuffer.wrap(radiationBytes).float
        isAmplify = data[672].toInt() == 1

    }

    fun isTC007(): Boolean = name == PRODUCT_NAME_TC007

}