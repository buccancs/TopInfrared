package com.infisense.usbir.utils

import android.widget.Switch
import com.energy.iruvc.utils.CommonParams

/**
 * @author: CaiSongL
 * @date: 2023/6/2 9:36
 */
object PseudocodeUtils {


    fun changeDualPseudocodeModelByOld(oldPseudocodeMode : Int) : CommonParams.PseudoColorUsbDualType {
        return when (oldPseudocodeMode) {
            1 -> {
                CommonParams.PseudoColorUsbDualType.WHITE_HOT_MODE
            }
            3 -> {
                CommonParams.PseudoColorUsbDualType.IRONBOW_MODE
            }
            4 -> {
                CommonParams.PseudoColorUsbDualType.RAINBOW_MODE
            }
            5 -> {
                CommonParams.PseudoColorUsbDualType.AURORA_MODE
            }
            6 -> {
                CommonParams.PseudoColorUsbDualType.MEDICAL_MODE
            }
            7 -> {
                CommonParams.PseudoColorUsbDualType.RED_HOT_MODE
            }
            8 -> {
                CommonParams.PseudoColorUsbDualType.JUNGLE_MODE
            }
            9 -> {
                CommonParams.PseudoColorUsbDualType.MEDICAL_MODE
            }
            10 -> {
                CommonParams.PseudoColorUsbDualType.NIGHT_MODE
            }
            11 -> {
                CommonParams.PseudoColorUsbDualType.BLACK_HOT_MODE
            }
            else -> {
                CommonParams.PseudoColorUsbDualType.IRONBOW_MODE
            }
        }
    }

    /**
     * 旧版sdk的兼容
     */
    fun changePseudocodeModeByOld(oldPseudocodeMode : Int) : CommonParams.PseudoColorType {
        return when (oldPseudocodeMode) {
            1 -> {
                CommonParams.PseudoColorType.PSEUDO_1
            }
            3 -> {
                CommonParams.PseudoColorType.PSEUDO_3
            }
            4 -> {
                CommonParams.PseudoColorType.PSEUDO_4
            }
            5 -> {
                CommonParams.PseudoColorType.PSEUDO_5
            }
            6 -> {
                CommonParams.PseudoColorType.PSEUDO_6
            }
            7 -> {
                CommonParams.PseudoColorType.PSEUDO_7
            }
            8 -> {
                CommonParams.PseudoColorType.PSEUDO_8
            }
            9 -> {
                CommonParams.PseudoColorType.PSEUDO_9
            }
            10 -> {
                CommonParams.PseudoColorType.PSEUDO_10
            }
            11 -> {
                CommonParams.PseudoColorType.PSEUDO_11
            }
            else -> {
                CommonParams.PseudoColorType.PSEUDO_1
            }
        }
    }

    fun changePseudocodeModeByNew(pseudoColorType : CommonParams.PseudoColorType) : Int {
        return when (pseudoColorType) {
            CommonParams.PseudoColorType.PSEUDO_1 -> {
                1
            }
            CommonParams.PseudoColorType.PSEUDO_3 -> {
                3
            }
            CommonParams.PseudoColorType.PSEUDO_4 -> {
                4
            }
            CommonParams.PseudoColorType.PSEUDO_5 -> {
                5
            }
            CommonParams.PseudoColorType.PSEUDO_6 -> {
                6
            }
            CommonParams.PseudoColorType.PSEUDO_7 -> {
                7
            }
            CommonParams.PseudoColorType.PSEUDO_8 -> {
                8
            }
            CommonParams.PseudoColorType.PSEUDO_9 -> {
                9
            }
            CommonParams.PseudoColorType.PSEUDO_10 -> {
                10
            }
            CommonParams.PseudoColorType.PSEUDO_11 -> {
                11
            }
            else -> {
                1
            }
        }
    }


}