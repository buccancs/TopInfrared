package com.topdon.module.thermal.chart

import android.annotation.SuppressLint
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.topdon.lib.core.common.SharedManager
import java.text.SimpleDateFormat
import java.util.*

class MyValueFormatter(private val startTime: Long, private val type: Int = 1) :
    IndexAxisValueFormatter() {

    companion object {
        const val TYPE_TIME_SECOND = 1
        const val TYPE_TIME_MINUTE = 2
        const val TYPE_TIME_HOUR = 3
        const val TYPE_TIME_DAY = 4
    }

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        val time = startTime + value.toLong()
        return showDateSecond(time)
    }

    @SuppressLint("SimpleDateFormat")
    fun showDateSecond(time: Long): String {
        val date = Date(time)
        val pattern = when (type) {
            TYPE_TIME_SECOND -> "HH:mm:ss"
            TYPE_TIME_MINUTE -> "HH:mm"
            TYPE_TIME_HOUR -> "HH:00"
            TYPE_TIME_DAY -> "yy-MM-dd"
            else -> "HH:mm:ss"
        }
        val dateFormat = SimpleDateFormat(pattern)
        val timeZone = TimeZone.getTimeZone(SharedManager.getShowZone())
        dateFormat.timeZone = timeZone
        return dateFormat.format(date)
    }
}
