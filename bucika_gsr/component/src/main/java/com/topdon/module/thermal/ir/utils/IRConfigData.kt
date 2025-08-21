package com.topdon.module.thermal.ir.utils

import android.content.Context
import com.topdon.module.thermal.ir.R

/**
 * des:
 * author: CaiSongL
 * date: 2024/4/3 11:12
 **/
data class IRConfigData(val name: String, val value: String) {
    companion object {
        fun irConfigData(context: Context): ArrayList<IRConfigData> = arrayListOf(
            IRConfigData(name = context.resources.getString(R.string.reference_item1), value = "0.95"),
            IRConfigData(name = context.resources.getString(R.string.reference_item2), value = "0.94"),
            IRConfigData(name = context.resources.getString(R.string.reference_item3), value = "0.75"),
            IRConfigData(name = context.resources.getString(R.string.reference_item4), value = "0.98"),
            IRConfigData(name = context.resources.getString(R.string.reference_item5), value = "0.95"),
            IRConfigData(name = context.resources.getString(R.string.reference_item6), value = "0.95"),
            IRConfigData(name = context.resources.getString(R.string.reference_item7), value = "0.95"),
            IRConfigData(name = context.resources.getString(R.string.reference_item8), value = "0.90"),
            IRConfigData(name = context.resources.getString(R.string.reference_item9), value = "0.85")
        )

        /**
         * 根据指定的发射率，拼接与该发射率对应的材料文字并返回.
         */
        fun getTextByEmissivity(context: Context, emissivity: Float): String {
            val stringBuilder = StringBuilder()
            for (data in irConfigData(context)) {
                if (emissivity.toString() == data.value) {
                    if (stringBuilder.isEmpty()) {
                        stringBuilder.append(context.getString(R.string.tc_temp_test_materials)).append(" : ")
                    }
                    stringBuilder.append(data.name).append("/")
                }
            }
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
            }
            return stringBuilder.toString()
        }
    }
}