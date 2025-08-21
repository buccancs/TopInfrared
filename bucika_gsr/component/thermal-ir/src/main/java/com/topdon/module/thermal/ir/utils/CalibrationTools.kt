package com.topdon.module.thermal.ir.utils

import android.util.Log
import com.elvishew.xlog.XLog
import com.energy.iruvc.ircmd.IRCMD
import com.energy.iruvc.utils.CommonParams
import com.energy.iruvc.utils.SynchronizedBitmap

/**
 * 温度标定
 */
object CalibrationTools {

    /**
     * 单点标定
     * 对准黑体 - 设置温度
     */
    fun sign(irCmd: IRCMD, singlePointTemp: Int): Boolean {
        var success = false
        // 标定前需要重置测温参数,否则温度标定不准
        if (irCmd.restoreDefaultConfig(CommonParams.DefaultConfigType.DEF_CFG_TPD) == 0) {
            irCmd.restoreDefaultConfig(CommonParams.DefaultConfigType.DEF_CFG_TPD)
            val result = irCmd.setTPDKtBtRecalPoint(CommonParams.TPDKtBtRecalPointType.RECAL_1_POINT, singlePointTemp)
            if (result == 0) {
                success = true
            } else {
                XLog.w("单点标定失败")
            }
        } else {
            XLog.w("单点标定失败")
        }
        return success
    }

    /**
     * 温度标定
     * 低温(100 ~ 400)
     */
    fun pointFirst(irCmd: IRCMD, pointTemp: Int): Boolean {
        var success = false
        // 标定前需要重置测温参数,否则温度标定不准
        if (irCmd.restoreDefaultConfig(CommonParams.DefaultConfigType.DEF_CFG_TPD) == 0) {
            val result = irCmd.setTPDKtBtRecalPoint(CommonParams.TPDKtBtRecalPointType.RECAL_2_POINT_FIRST, pointTemp + 273)
            if (result == 0) {
                success = true
            } else {
                XLog.w("低温标定失败")
            }
        } else {
            XLog.w("低温标定失败")
        }
        return success
    }

    /**
     * 温度标定
     * 高温(20 ~ 100)
     *
     * 提交完低温之后才能提交高温
     */
    fun pointEnd(irCmd: IRCMD, pointTemp: Int): Boolean {
        var success = false
        // 标定前需要重置测温参数,否则温度标定不准
        if (irCmd.restoreDefaultConfig(CommonParams.DefaultConfigType.DEF_CFG_TPD) == 0) {
            val result = irCmd.setTPDKtBtRecalPoint(CommonParams.TPDKtBtRecalPointType.RECAL_2_POINT_END, pointTemp + 273)
            if (result == 0) {
                success = true
            } else {
                Log.w("123", "失败")
            }
        } else {
            Log.w("123", "失败")
        }
        return success
    }

    /**
     * 锅盖标定 - 步骤一准备
     *
     */
    fun potReady(irCmd: IRCMD): Boolean {
        return irCmd.rmCoverStsSwitch(CommonParams.RMCoverStsSwitchStatus.RMCOVER_DIS) == 0 //关闭锅盖校正
    }

    /**
     * 锅盖标定 - 步骤二开始
     *
     * @param gainType 默认GAIN_1
     * CommonParams.RMCoverAutoCalcType.GAIN_1
     * CommonParams.RMCoverAutoCalcType.GAIN_2
     * CommonParams.RMCoverAutoCalcType.GAIN_4
     */
    fun potStart(irCmd: IRCMD, type: Int) {
        val gainType = when (type) {
            1 -> CommonParams.RMCoverAutoCalcType.GAIN_1
            2 -> CommonParams.RMCoverAutoCalcType.GAIN_2
            4 -> CommonParams.RMCoverAutoCalcType.GAIN_4
            else -> CommonParams.RMCoverAutoCalcType.GAIN_1
        }
        irCmd.rmCoverAutoCalc(gainType) //发送锅盖标定
        irCmd.rmCoverStsSwitch(CommonParams.RMCoverStsSwitchStatus.RMCOVER_EN) //打开锅盖校正
    }

    /**
     * 取消标定
     */
    fun cancelCalibration(irCmd: IRCMD) {
        irCmd.restoreDefaultConfig(CommonParams.DefaultConfigType.DEF_CFG_TPD)
    }

    /**
     * 恢复出厂标定
     */
    fun reset(irCmd: IRCMD) {
        irCmd.restoreDefaultConfig(CommonParams.DefaultConfigType.DEF_CFG_ALL)
    }

    /**
     * 查询增益模式
     * @return true: 高增益    false: 低增益
     */
    fun queryGain(irCmd: IRCMD): Boolean {
        val value = IntArray(1)
        irCmd.getPropTPDParams(CommonParams.PropTPDParams.TPD_PROP_GAIN_SEL, value)
        return value[0] == 1
    }

    /**
     * 设置增益模式
     * @param type 1: 打开    0: 关闭
     *
     */
    fun setGain(irCmd: IRCMD, type: Int) {
        if (type == 1) {
            irCmd.setPropTPDParams(CommonParams.PropTPDParams.TPD_PROP_GAIN_SEL, CommonParams.PropTPDParamsValue.GAINSELStatus.GAIN_SEL_HIGH)
        } else {
            irCmd.setPropTPDParams(CommonParams.PropTPDParams.TPD_PROP_GAIN_SEL, CommonParams.PropTPDParamsValue.GAINSELStatus.GAIN_SEL_LOW)
        }
    }

    /**
     * 查询Tpd
     */
    fun queryTpd(irCmd: IRCMD, params: CommonParams.PropTPDParams): Int {
        val value = IntArray(1)
        irCmd.getPropTPDParams(params, value)
        return value[0]
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
     * 控制锅盖标定开关
     */
    fun stsSwitch(irCmd: IRCMD?, flag: Boolean) {
        if (flag) {
            irCmd?.rmCoverStsSwitch(CommonParams.RMCoverStsSwitchStatus.RMCOVER_EN)
        } else {
            irCmd?.rmCoverStsSwitch(CommonParams.RMCoverStsSwitchStatus.RMCOVER_DIS)
        }
    }

    /**
     * 锅盖标定 - 步骤二开始
     *
     * @param gainType 默认GAIN_1
     * CommonParams.RMCoverAutoCalcType.GAIN_1
     * CommonParams.RMCoverAutoCalcType.GAIN_2
     * CommonParams.RMCoverAutoCalcType.GAIN_4
     */
    fun pot(irCmd: IRCMD, type: Int) {
        val gainType = when (type) {
            1 -> CommonParams.RMCoverAutoCalcType.GAIN_1
            2 -> CommonParams.RMCoverAutoCalcType.GAIN_2
            4 -> CommonParams.RMCoverAutoCalcType.GAIN_4
            else -> CommonParams.RMCoverAutoCalcType.GAIN_1
        }
        irCmd.rmCoverAutoCalc(gainType) //发送锅盖标定
    }

    /**
     * 自动快门
     */
    fun autoShutter(irCmd: IRCMD?, flag: Boolean) {
        val data = if (flag) CommonParams.PropAutoShutterParameterValue.StatusSwith.ON else CommonParams.PropAutoShutterParameterValue.StatusSwith.OFF
        irCmd?.setPropAutoShutterParameter(CommonParams.PropAutoShutterParameter.SHUTTER_PROP_SWITCH, data)
    }

    /**
     * TPD_PROP_DISTANCE不给设置
     * 设置距离 unit:cnt(128cnt=1m)
     * @param value 0 ~ 25600
     */
    fun setTpdDis(irCmd: IRCMD?, value: Int) {
        val data = CommonParams.PropTPDParamsValue.NumberType(value.toString())
        setTpdParams(irCmd = irCmd, params = CommonParams.PropTPDParams.TPD_PROP_DISTANCE, value = data)
    }

    /**
     * 设置发射率 unit:cnt(128cnt=1)
     * @param value 1 ~ 128
     */
    fun setTpdEms(irCmd: IRCMD?, value: Int) {
        val data = CommonParams.PropTPDParamsValue.NumberType(value.toString())
        setTpdParams(irCmd = irCmd, params = CommonParams.PropTPDParams.TPD_PROP_EMS, value = data)
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
}