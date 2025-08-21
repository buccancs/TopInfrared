package com.topdon.module.thermal.ir.utils

import android.util.Log
import com.blankj.utilcode.util.Utils
import com.elvishew.xlog.XLog
import com.energy.iruvc.dual.DualUVCCamera
import com.energy.iruvc.ircmd.IRCMD
import com.energy.iruvc.utils.CommonParams
import com.energy.iruvc.utils.DualCameraParams
import com.energy.iruvc.utils.SynchronizedBitmap
import com.infisense.usbdual.camera.BaseDualView
import com.infisense.usbdual.camera.USBMonitorManager
import com.infisense.usbir.utils.FileUtil
import com.infisense.usbir.utils.HexDump
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.common.SharedManager
import java.io.IOException
import java.io.InputStream
import kotlin.math.ceil
import kotlin.math.floor

object IRCmdTool {
    val TAG = "IRCmdTool"
    var dispNumber = 30

    fun getDualBytes(irCmd: IRCMD?):ByteArray {
        val calibrationDataSize = 192
        val INIT_ALIGN_DATA = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)

        val oemInfo = ByteArray(512)
        val snData = ByteArray(256)
        val dispData = ByteArray(5)//配准参数
        irCmd?.oemRead(CommonParams.ProductType.P2, oemInfo)
        XLog.w("机芯数据加载成功", "数据读取完成:")
        val calibrationData = ByteArray(calibrationDataSize)
        val productTypeData = ByteArray(2)
        System.arraycopy(oemInfo, 0, calibrationData, 0, calibrationData.size)
        System.arraycopy(oemInfo, calibrationDataSize, productTypeData, 0, productTypeData.size)
        System.arraycopy(oemInfo, calibrationDataSize+productTypeData.size, dispData,
            0, dispData.size)
        System.arraycopy(oemInfo,256,snData,0,snData.size)
        try {
            var str = String(dispData)
            str = str.replace(Regex("[^-\\d]"),"")
            dispNumber = str.toInt()
            if (dispNumber > 60){
                dispNumber = dispNumber/10
            }
            if (dispNumber < -20){
                dispNumber = -20
            }
            XLog.w("配准信息:", ""+dispNumber)
        }catch (e:Exception){
            XLog.w("配准数据异常")
        }
        val snList = String(snData).split(";")
        val snStr = if (snList.isNotEmpty() && snList[0].contains("sn",true)){
            snList[0].replace("SN:","")
        }else{
            ""
        }
        val parameters = ByteArray(calibrationDataSize + 1 + 24)
        if (String(productTypeData) == "TD") {
            System.arraycopy(calibrationData, 0, parameters, 0, calibrationData.size)
            parameters[calibrationDataSize] = 1
            val alignByte = SharedManager.getManualData(snStr)
            System.arraycopy(alignByte, 0, parameters, calibrationDataSize + 1, alignByte.size)
        } else {
            val am = Utils.getApp().assets
            var `is`: InputStream? = null
            val length: Int
            try {
                `is` = am.open("dual_calibration_parameters2.bin")
                length = `is`.available()
                if (`is`.read(parameters) != length) {
                    Log.e(TAG, "read file fail ")
                }
                parameters[length] = 1
                //先从缓存中查找是否有保存的对齐数据，没有用初始化数据
                val alignByte = SharedManager.getManualData(snStr)
                System.arraycopy(alignByte, 0, parameters, calibrationDataSize + 1, alignByte.size)
                XLog.w("机芯没存在校正数据，请联系厂商确认")
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    `is`?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return parameters
    }

    fun getSNStr(irCmd: IRCMD?) : String{
        val oemInfo = ByteArray(512)
        irCmd?.oemRead(CommonParams.ProductType.P2, oemInfo)
        val snData = ByteArray(256)
        System.arraycopy(oemInfo,256,snData,0,snData.size)
        val snList = String(snData).split(";")
        return if (snList.isNotEmpty() && snList[0].contains("sn",true)){
            snList[0].replace("SN:","")
        }else{
            ""
        }
    }


    /**
     * 设置发射率 unit:cnt(128cnt = 1)
     * @param value 1 ~ 128
     */
    fun setTpdEms(irCmd: IRCMD?, value: Int) {
        val data = CommonParams.PropTPDParamsValue.NumberType(value.toString())
        setTpdParams(irCmd = irCmd, params = CommonParams.PropTPDParams.TPD_PROP_EMS, value = data)
    }

    /**
     * 设置距离 unit:cnt(128cnt = 1m, 默认值: 0.25 * 128 = 32)
     * @param value 0 ~ 25600
     *
     * 现有sdk在设置TPD_PROP_DISTANCE抛异常
     */
    fun setTpdDis(irCmd: IRCMD?, value: Int) {
        val data = CommonParams.PropTPDParamsValue.NumberType(value.toString())
        setTpdParams(irCmd = irCmd, params = CommonParams.PropTPDParams.TPD_PROP_DISTANCE, value = data)
    }


    /**
     * 设置对比度
     * @param value 0 ~ 255
     */
    fun setLevelContrast(irCmd: IRCMD?, value: Int) {
        val data = CommonParams.PropImageParamsValue.NumberType(value.toString())
        setImageParams(irCmd = irCmd, params = CommonParams.PropImageParams.IMAGE_PROP_LEVEL_CONTRAST, value = data)
    }

    /**
     * 设置锐化
     * @param value 0 ~ 4
     *
     */
    fun setLevelDdd(irCmd: IRCMD?, value: Int) {
        val data = when (value) {
            0 -> CommonParams.PropImageParamsValue.DDEType.DDE_0
            1 -> CommonParams.PropImageParamsValue.DDEType.DDE_1
            2 -> CommonParams.PropImageParamsValue.DDEType.DDE_2
            3 -> CommonParams.PropImageParamsValue.DDEType.DDE_3
            4 -> CommonParams.PropImageParamsValue.DDEType.DDE_4
            else -> CommonParams.PropImageParamsValue.DDEType.DDE_0
        }
        setImageParams(irCmd = irCmd, params = CommonParams.PropImageParams.IMAGE_PROP_LEVEL_DDE, value = data)
    }

    /**
     * 设置自动增益
     */
    fun setLevelAgc(irCmd: IRCMD?, value: Boolean) {
        val data = if (value) {
            CommonParams.PropImageParamsValue.StatusSwith.ON
        } else {
            CommonParams.PropImageParamsValue.StatusSwith.OFF
        }
        setImageParams(irCmd = irCmd, params = CommonParams.PropImageParams.IMAGE_PROP_ONOFF_AGC, value = data)
    }

    /**
     * 查询增益模式
     * @return 1:高增益(常温)    0:低增益(高温)
     */
    fun getTpdGainSel(irCmd: IRCMD?): Int {
        val result = queryTpdParam(irCmd = irCmd, params = CommonParams.PropTPDParams.TPD_PROP_GAIN_SEL)
        return if (result == CommonParams.PropTPDParamsValue.GAINSELStatus.GAIN_SEL_HIGH.value) {
            1
        } else {
            0
        }
    }

    /**
     * 设置增益模式
     * @param value 1:高增益(常温)    0:低增益(高温)
     */
    fun setTpdGainSel(irCmd: IRCMD?, value: Int): Int {
        val data = if (value == 1) {
            CommonParams.PropTPDParamsValue.GAINSELStatus.GAIN_SEL_HIGH
        } else {
            CommonParams.PropTPDParamsValue.GAINSELStatus.GAIN_SEL_LOW
        }
        return setTpdParams(irCmd = irCmd, params = CommonParams.PropTPDParams.TPD_PROP_GAIN_SEL, value = data)
    }

    /**
     * 查询Tpd
     */
    fun queryTpdParam(irCmd: IRCMD?, params: CommonParams.PropTPDParams): Int {
        val value = IntArray(1)
        irCmd?.getPropTPDParams(params, value)
        return value[0]
    }

    /**
     * 查询Image
     */
    fun queryImageParam(irCmd: IRCMD?, params: CommonParams.PropImageParams): Int {
        val value = IntArray(1)
        irCmd?.getPropImageParams(params, value)
        return value[0]
    }

    /**
     * 设置Tpd
     */
    private fun setTpdParams(irCmd: IRCMD?, params: CommonParams.PropTPDParams, value: CommonParams.PropTPDParamsValue): Int {
        return try {
            irCmd?.setPropTPDParams(params, value) ?: 0
        } catch (e: Exception) {
            XLog.w("设置参数异常[${params.name}]: ${e.message}")
            0
        }
    }

    /**
     * 设置图像参数
     */
    private fun setImageParams(irCmd: IRCMD?, params: CommonParams.PropImageParams, value: CommonParams.PropImageParamsValue): Int {
        return try {
            irCmd?.setPropImageParams(params, value) ?: 0
        } catch (e: Exception) {
            XLog.w("设置参数异常[${params.name}]: ${e.message}")
            0
        }
    }

    /**
     * 配准
     * 水平移动
     * @param value (-20 ~ 60)
     */
    fun setDisp(dualView: BaseDualView?, value: Int): Int {
        return try {
            if (dualView != null) {
                dualView?.dualUVCCamera!!.setDisp(value)
            } else {
                0
            }
        } catch (e: Exception) {
            XLog.w("设置配准异常[${value}]: ${e.message}")
            0
        }
    }

    /**
     * @param moveX 在当前基础上要再偏移的数值
     */
    fun setAlignTranslate(dualView: BaseDualView?, moveX: Int, moveY: Int) {
        val newSrc = ByteArray(8)

        val xSrc = ByteArray(4)
        HexDump.float2byte(moveX.toFloat(), xSrc)
        System.arraycopy(xSrc, 0, newSrc, 0, 4)

        val ySrc = ByteArray(4)
        HexDump.float2byte(moveY.toFloat(), ySrc)
        System.arraycopy(ySrc, 0, newSrc, 4, 4)

        dualView?.dualUVCCamera?.setAlignTranslateParameter(newSrc)
    }

    /**
     * 打快门
     */
    fun shutter(irCmd: IRCMD?, syncImage: SynchronizedBitmap) {
        if (syncImage.type == 1) {
            irCmd?.tc1bShutterManual()
        } else {
            // 执行这段
            irCmd?.updateOOCOrB(CommonParams.UpdateOOCOrBType.B_UPDATE)
        }
    }

    /**
     * 自动快门
     */
    fun autoShutter(irCmd: IRCMD?, flag: Boolean) {
        val data = if (flag) CommonParams.PropAutoShutterParameterValue.StatusSwith.ON else CommonParams.PropAutoShutterParameterValue.StatusSwith.OFF
        irCmd?.setPropAutoShutterParameter(CommonParams.PropAutoShutterParameter.SHUTTER_PROP_SWITCH, data)
    }

    /**
     * 开启等温尺
     * @param highC 温度上限，单位摄氏度
     * @param lowC 温度下限，单位摄氏度
     */
    fun setIsoColorOpen(dualUVCCamera: DualUVCCamera?, highC: Float, lowC: Float) {
        dualUVCCamera?.setIsothermal(DualCameraParams.IsothermalState.ON)
        val normalHighTemp = (highC + 273).toDouble() //单位k
        val normalLowTemp = (lowC + 273).toDouble() //单位k
        val highTemp = ceil(normalHighTemp * 16 * 4).toInt() // 高温向上取整
        val lowTemp = floor(normalLowTemp * 16 * 4).toInt() // 低温向下取整
        val highData = ByteArray(2)
        highData[0] = highTemp.toByte()
        highData[1] = (highTemp shr 8).toByte()
        val lowData = ByteArray(2)
        lowData[0] = lowTemp.toByte()
        lowData[1] = (lowTemp shr 8).toByte()
        val tempHFin = (highData[0].toInt() and 0x00ff) + (highData[1].toInt() and 0x00ff shl 8)
        val tempLFin = (lowData[0].toInt() and 0x00ff) + (lowData[1].toInt() and 0x00ff shl 8)
        dualUVCCamera?.setTempL(tempLFin) //低温
        dualUVCCamera?.setTempH(tempHFin) //高温
    }

    /**
     * 关闭等温尺
     */
    fun setIsoColorClose(dualUVCCamera: DualUVCCamera?) {
        dualUVCCamera?.setIsothermal(DualCameraParams.IsothermalState.OFF)
    }

    /**
     * 放大(仅对热成像有效)
     * ZoomScaleStep.ZOOM_STEP1: 2级倍率
     * ZoomScaleStep.ZOOM_STEP2: 4级倍率
     * ZoomScaleStep.ZOOM_STEP3: 8级倍率
     * ZoomScaleStep.ZOOM_STEP4: 16级倍率
     */
    fun setZoomUp(irCmd: IRCMD?) {
        irCmd?.zoomCenterUp(CommonParams.PreviewPathChannel.PREVIEW_PATH0, CommonParams.ZoomScaleStep.ZOOM_STEP2)
    }

    /**
     * 缩小
     */
    fun setZoomDown(irCmd: IRCMD?) {
        irCmd?.zoomCenterDown(CommonParams.PreviewPathChannel.PREVIEW_PATH0, CommonParams.ZoomScaleStep.ZOOM_STEP2)
    }

}