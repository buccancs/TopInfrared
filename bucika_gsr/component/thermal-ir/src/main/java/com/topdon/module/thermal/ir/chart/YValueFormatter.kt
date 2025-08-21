package com.topdon.module.thermal.ir.chart

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.topdon.lib.core.tools.UnitTools

/**
 * Y轴文本格式
 */
class YValueFormatter : IndexAxisValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        return try {
            String.format("%.1f", value)//检测value是不是数字
            UnitTools.showC(value)
        } catch (e: Exception) {
            UnitTools.showC(value)
        }
    }

}
