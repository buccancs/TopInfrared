package com.topdon.module.thermal.ir.chart

import android.annotation.SuppressLint
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.topdon.lib.core.common.SharedManager
import com.topdon.module.thermal.ir.utils.ChartTools
import java.text.SimpleDateFormat
import java.util.*

class IRMyValueFormatter(private val startTime: Long, private val type: Int = 1) :
    IndexAxisValueFormatter() {

    companion object {
        const val TYPE_TIME_SECOND = 1
        const val TYPE_TIME_MINUTE = 2
        const val TYPE_TIME_HOUR = 3
        const val TYPE_TIME_DAY = 4
    }

    override fun getFormattedValue(value: Float): String {
        val time = if (value.toLong() % 1000 == 999L) {
            value.toLong() + 1L
        } else {
            value.toLong()
        }
        val realTime = startTime + time * ChartTools.scale(type)
        return showDateSecond(realTime)
    }

    @SuppressLint("SimpleDateFormat")
    fun showDateSecond(time: Long): String {
        val date = Date(time)
        val pattern = when (type) {
            TYPE_TIME_SECOND -> "HH:mm:ss"
            TYPE_TIME_MINUTE -> "HH:mm"
            TYPE_TIME_HOUR -> "HH:00"
            TYPE_TIME_DAY -> "MM-dd"
            else -> "HH:mm:ss"
        }
        val dateFormat = SimpleDateFormat(pattern)
        val timeZone = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT))
        dateFormat.timeZone = timeZone
        return dateFormat.format(date)
    }
}
