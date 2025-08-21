package com.topdon.module.thermal.ir.frame

import android.graphics.Bitmap
import android.graphics.Rect
import com.elvishew.xlog.XLog
import com.energy.iruvc.sdkisp.LibIRProcess
import com.energy.iruvc.sdkisp.LibIRTemp
import com.energy.iruvc.utils.CommonParams
import com.example.suplib.wrapper.SupHelp
import com.infisense.usbir.tools.ImageTools
import com.infisense.usbir.utils.IRImageHelp
import com.infisense.usbir.utils.OpencvTools
import com.topdon.pseudo.bean.CustomPseudoBean
import java.io.IOException
import java.nio.ByteBuffer

class FrameTool {
    val imageWidth = 256
    val imageHeight = 192
    private val scrImageLen = imageWidth * imageHeight * 2
    private val srcTemperatureLen = imageWidth * imageHeight * 2
    private val imageBytes = ByteArray(scrImageLen)
    val temperatureBytes = ByteArray(srcTemperatureLen)
    private val imageRes = LibIRProcess.ImageRes_t() //原图尺寸
    private var struct: FrameStruct = FrameStruct() //首部信息

    private var maxLimit = -273f
    private var minLimit = -273f
    private var irImageHelp = IRImageHelp()
//    private val scrBitmap = Bitmap.createBitmap(192, 256, Bitmap.Config.ARGB_8888)
    private val supImageData = ByteArray(imageWidth * imageHeight * 4 * 4)
    private var dstArgbBytes: ByteArray ?= null


    fun read(bytes: ByteArray) {
        try {
            val frame = ByteArray(bytes.size)
            System.arraycopy(bytes, 0, frame, 0, frame.size)
            println("bs len: ${frame.size}")
            System.arraycopy(frame, 0, imageBytes, 0, scrImageLen)//图像数据 (192 x 256 x 2) yuv
            System.arraycopy(frame, scrImageLen, temperatureBytes, 0, srcTemperatureLen) //温度数据 (192 x 256 x 2)
            println("imageBytes len: ${imageBytes.size}")
            println("temperatureBytes len: ${temperatureBytes.size}")
        } catch (e: Exception) {
            e.printStackTrace()
            XLog.e("读取一帧原始数据失败: ${e.message}")
        }
    }

    /**
     * 设置图像默认尺寸
     */
    fun initStruct(struct: FrameStruct) {
        this.struct = struct
        imageRes.width = imageWidth.toChar()
        imageRes.height = imageHeight.toChar()
    }

    /**
     * 矫正角度
     */
    fun initRotate(): ImageParams {
        var rotate = ImageParams.ROTATE_0
        when (struct.rotate) {
            0 -> rotate = ImageParams.ROTATE_0
            90 -> rotate = ImageParams.ROTATE_270
            180 -> rotate = ImageParams.ROTATE_180
            270 -> rotate = ImageParams.ROTATE_90
        }
        return rotate
    }

    /**
     * 获取温度数据
     */
    fun getTempBytes(rotate: ImageParams = ImageParams.ROTATE_0): ByteArray {
        val tempBytes = ByteArray(srcTemperatureLen)
        val dstTempBytes = ByteArray(srcTemperatureLen)
        System.arraycopy(temperatureBytes, 0, tempBytes, 0, srcTemperatureLen)
        when (rotate) {
            ImageParams.ROTATE_270 -> LibIRProcess.rotateLeft90(tempBytes, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, dstTempBytes)
            ImageParams.ROTATE_90 -> LibIRProcess.rotateRight90(tempBytes, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, dstTempBytes)
            ImageParams.ROTATE_180 -> LibIRProcess.rotate180(tempBytes, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, dstTempBytes)
            else -> System.arraycopy(temperatureBytes, 0, dstTempBytes, 0, srcTemperatureLen)
        }
        return dstTempBytes
    }

    fun getRotate90Temp(temperatureBytes: ByteArray): ByteArray {
        val tempBytes = ByteArray(temperatureBytes.size)
        val dstTempBytes = ByteArray(temperatureBytes.size)
        System.arraycopy(temperatureBytes, 0, tempBytes, 0, temperatureBytes.size)
        val imgRes = LibIRProcess.ImageRes_t()
        imgRes.height = 192.toChar()
        imgRes.width = 256.toChar()
        LibIRProcess.rotateRight90(tempBytes, imgRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, dstTempBytes)
        return dstTempBytes
    }

    /**
     * 灰度图转伪彩图像
     * yuv -> argb -> 温度尺 -> 旋转 -> bitmap
     */
    fun getScrPseudoColorScaledBitmap(
        pseudoColorMode: CommonParams.PseudoColorType = CommonParams.PseudoColorType.PSEUDO_3,
        max: Float = -273f,
        min: Float = 273f,
        rotate: ImageParams = ImageParams.ROTATE_0,
        customPseudoBean: CustomPseudoBean,
        maxTemperature: Float, minTemperature: Float,
        isAmplify: Boolean,
        ): Bitmap? {
        maxLimit = max
        minLimit = min
        val imageBytesTemp = ByteArray(imageBytes.size)
        System.arraycopy(imageBytes, 0, imageBytesTemp, 0, imageBytesTemp.size)
        val pixNum = imageWidth * imageHeight
        val argbLen = pixNum * 4
        var argbBytes = ByteArray(argbLen)
        dstArgbBytes = ByteArray(argbLen)
        val maxRGB = IntArray(3)
        val minRGB = IntArray(3)
        if (customPseudoBean.isUseCustomPseudo) {
            //自定义渲染模式
            LibIRProcess.convertYuyvMapToARGBPseudocolor(imageBytesTemp, pixNum.toLong(), CommonParams.PseudoColorType.PSEUDO_1, argbBytes)
            val colorList: IntArray? = customPseudoBean.getColorList(struct.isTC007())
            val places: FloatArray? = customPseudoBean.getPlaceList()
            if (colorList != null) {
                val customMaxTemp = customPseudoBean.maxTemp
                val customMinTemp = customPseudoBean.minTemp
                val maxColor: Int = colorList.last()
                val minColor: Int = colorList.first()
                maxRGB[0] = maxColor shr 16 and 0xFF
                maxRGB[1] = maxColor shr 8 and 0xFF
                maxRGB[2] = maxColor and 0xFF
                minRGB[0] = minColor shr 16 and 0xFF
                minRGB[1] = minColor shr 8 and 0xFF
                minRGB[2] = minColor and 0xFF
                var j = 0
                val argbBytesLength = imageWidth * imageHeight * 4
                // 遍历像素点，过滤温度阈值
                var index = 0
                while (index < argbBytesLength) {
                    // 温度换算公式
                    var temperature0: Float =
                        ((temperatureBytes[j].toInt() and 0xff) + (temperatureBytes[j + 1]
                            .toInt() and 0xff) * 256).toFloat()
                    temperature0 = (temperature0 / 64 - 273.15).toFloat()
                    if (temperature0 in customMinTemp..customMaxTemp) {
                        val rgb = OpencvTools.getOneColorByTempUnif(
                            customMaxTemp,
                            customMinTemp,
                            temperature0,
                            colorList,
                            places
                        )
                        if (rgb != null) {
                            argbBytes[index] = rgb[0].toByte()
                            argbBytes[index + 1] = rgb[1].toByte()
                            argbBytes[index + 2] = rgb[2].toByte()
                        }
                    } else if (temperature0 > customMaxTemp) {
                        if (!customPseudoBean.isUseGray) {
                            argbBytes[index] = maxRGB[0].toByte()
                            argbBytes[index + 1] = maxRGB[1].toByte()
                            argbBytes[index + 2] = maxRGB[2].toByte()
                        }
                    } else if (temperature0 < customMinTemp) {
                        if (!customPseudoBean.isUseGray) {
                            argbBytes[index] = minRGB[0].toByte()
                            argbBytes[index + 1] = minRGB[1].toByte()
                            argbBytes[index + 2] = minRGB[2].toByte()
                        }
                    }
                    argbBytes[index + 3] = 255.toByte()
                    index += 4
                    j += 2
                }
            }
        } else {
            LibIRProcess.convertYuyvMapToARGBPseudocolor(imageBytesTemp, pixNum.toLong(), pseudoColorMode, argbBytes)
            if (!(maxLimit == -273f && minLimit == -273f) && !(maxTemperature == maxLimit && minLimit == minTemperature)) {
                ImageTools.dualReadFrame(argbBytes, temperatureBytes, maxLimit, minLimit) //温度尺
            }
        }

        if ((struct.alarmBean.isHighOpen && struct.alarmBean.highTemp != Float.MAX_VALUE) ||
            (struct.alarmBean.isLowOpen && struct.alarmBean.lowTemp != Float.MIN_VALUE)) {
            try {
                argbBytes = irImageHelp.contourDetection(
                    struct.alarmBean,
                    argbBytes, temperatureBytes,
                    imageWidth,
                    imageHeight,
                )!!
            } catch (e: IOException) {

            }
        }

        argbBytesRotate(argbBytes, dstArgbBytes!!, rotate) //旋转
        val dstImageRes = getDstImageRes(rotate)
        var scrBitmap : Bitmap ?= null
        if (isAmplify){
//            scrBitmap = Bitmap.createBitmap(dstImageRes.width.code,
//                dstImageRes.height.code, Bitmap.Config.ARGB_8888)
//            scrBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(dstArgbBytes, 0, argbLen))
//            return OpencvTools.supImageFourExToBitmap(scrBitmap)
            SupHelp.getInstance().initA4KCPP()
            if (SupHelp.getInstance().loadOpenclSuccess){
                OpencvTools.supImage(dstArgbBytes, dstImageRes.height.code,
                    dstImageRes.width.code,supImageData)
                scrBitmap = Bitmap.createBitmap(dstImageRes.width.code * 2,
                    dstImageRes.height.code * 2, Bitmap.Config.ARGB_8888)
                scrBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(supImageData, 0, argbLen * 4))
            }else{
                scrBitmap = Bitmap.createBitmap(dstImageRes.width.code,
                    dstImageRes.height.code, Bitmap.Config.ARGB_8888)
                scrBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(dstArgbBytes, 0, argbLen))
            }
        }else{
            scrBitmap = Bitmap.createBitmap(dstImageRes.width.code,
                dstImageRes.height.code, Bitmap.Config.ARGB_8888)
            scrBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(dstArgbBytes, 0, argbLen))
        }
        return scrBitmap
    }

    /**
     * 获取原始图像的bitmap
     */
    fun getBaseBitmap(rotate : ImageParams) : Bitmap{
        val dstImageRes = getDstImageRes(rotate)
        val scrBitmap = Bitmap.createBitmap(dstImageRes.width.code,
            dstImageRes.height.code, Bitmap.Config.ARGB_8888)
        dstArgbBytes?.let {
            scrBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(it, 0, it.size))
        }
        return scrBitmap
    }

    /**
     * 目标尺寸
     */
    private fun getDstImageRes(rotate: ImageParams): LibIRProcess.ImageRes_t {
        val dstImageRes = LibIRProcess.ImageRes_t() //目标尺寸
        if (rotate == ImageParams.ROTATE_270 || rotate == ImageParams.ROTATE_90) {
            dstImageRes.width = imageRes.height
            dstImageRes.height = imageRes.width
        } else {
            dstImageRes.width = imageRes.width
            dstImageRes.height = imageRes.height
        }
        return dstImageRes
    }

    /**
     * argb像素矩阵旋转
     */
    private fun argbBytesRotate(argbBytes: ByteArray, dstArgbBytes: ByteArray, rotate: ImageParams) {
        when (rotate) {
            ImageParams.ROTATE_270 -> LibIRProcess.rotateLeft90(
                argbBytes,
                imageRes,
                CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888,
                dstArgbBytes
            )
            ImageParams.ROTATE_90 -> LibIRProcess.rotateRight90(
                argbBytes,
                imageRes,
                CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888,
                dstArgbBytes
            )

            ImageParams.ROTATE_180 -> LibIRProcess.rotate180(argbBytes, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, dstArgbBytes)
            else -> System.arraycopy(argbBytes, 0, dstArgbBytes, 0, argbBytes.size)
        }
    }

//    fun getTemp() {
//        // 获取全图最高温和最低温的数据
//        val irTemp = Libirtemp(256, 192)
//        irTemp.settempdata(mixTemperatureBytes)
//        val temperatureSampleEasyResult = irTemp.getTemperatureOfRect(Rect(0, 0, 256, 192))
//        Log.w("123", "mix max: ${temperatureSampleEasyResult.maxTemperature}, min: ${temperatureSampleEasyResult.minTemperature}")
//    }


//    fun getSrcTemp()：Libirt{
//        // 获取全图最高温和最低温的数据
//        val irTemp = Libirtemp(256, 192)
//        irTemp.settempdata(temperatureBytes)
//        val temperatureSampleEasyResult = irTemp.getTemperatureOfRect(Rect(0, 0, 256, 192))
//        temperatureSampleEasyResult.maxTemperaturePixel
//        Log.w("123", "src max: ${temperatureSampleEasyResult.maxTemperature}, min: ${temperatureSampleEasyResult.minTemperature}")
//    }

    /**
     * 全局测温(原数据)
     */
    fun getSrcTemp(): LibIRTemp.TemperatureSampleResult {
        // 获取全图最高温和最低温的数据
        val irTemp = LibIRTemp(imageWidth, imageHeight)
        irTemp.setTempData(temperatureBytes)
        return irTemp.getTemperatureOfRect(Rect(0, 0, imageWidth, imageHeight))
    }


}