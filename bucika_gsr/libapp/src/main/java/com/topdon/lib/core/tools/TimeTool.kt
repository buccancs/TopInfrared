package com.topdon.lib.core.tools

import android.annotation.SuppressLint
import android.util.Log
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.utils.CommUtils
import java.io.File
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

object TimeTool {

    fun formatDetectTime(timeMillis: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timeMillis))
    }

    @SuppressLint("SimpleDateFormat")
    fun getNowTime(): String {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        return dateFormat.format(date)
    }

    /**
     * long: 时间戳(毫秒)
     * 精确到秒
     */
    @SuppressLint("SimpleDateFormat")
    fun reportTime(time: Long): String {
        val date = Date(time)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val timeZone = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT))
        dateFormat.timeZone = timeZone
        return dateFormat.format(date)
    }

    /**
     * 时间转时间戳
     * 2021-01-01 00:00:00 => 1609430400000
     */
    @SuppressLint("SimpleDateFormat")
    fun strToTime(timeStr: String): Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val timeZone = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT))
            dateFormat.timeZone = timeZone
            dateFormat.parse(timeStr, ParsePosition(0)).time
        } catch (e: Exception) {
            //2021-01-01 00:00:00
            1609430400000
        }
    }


    /**
     * @param type 1:秒 2:分 3:时 4:天
     */
    @SuppressLint("SimpleDateFormat")
    fun showDateType(time: Long, type: Int = 0): String {
        val date = Date(time)
        //yyyy-MM-dd HH:mm:ss.SSS
        val pattern = when (type) {
            1 -> "HH:mm:ss.SSS"
            2 -> "HH:mm"
            3 -> "MM-dd HH:00"
            4 -> "yyyy-MM-dd"
            else -> "yyyy-MM-dd HH:mm:ss"
        }
        val dateFormat = SimpleDateFormat(pattern)
        val timeZone = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT))
        dateFormat.timeZone = timeZone
        return dateFormat.format(date)
    }

    /**
     * 精度秒转分
     */
    @SuppressLint("SimpleDateFormat")
    fun timeToMinute(time: Long, type: Int): Long {
        val dateFormat = when (type) {
            1 -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss")//秒
            2 -> SimpleDateFormat("yyyy-MM-dd HH:mm:00")//分
            3 -> SimpleDateFormat("yyyy-MM-dd HH:00:00")//时
            4 -> SimpleDateFormat("yyyy-MM-dd 00:00:0")//天
            else -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        }
        val date = Date(time)
        val str = dateFormat.format(date)
        return strToTime(str)
    }

    /**
     * long: 时间戳(毫秒)
     * 精确到分
     */
    @SuppressLint("SimpleDateFormat")
    fun showTimeSecond(time: Long): String {
        val date = Date(time)
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val timeZone = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT))
        dateFormat.timeZone = timeZone
        return dateFormat.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    fun showDateSecond(): String {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val timeZone = TimeZone.getTimeZone(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT))
        dateFormat.timeZone = timeZone
        return dateFormat.format(date)
    }

    /**
     * 视频时长
     */
    @SuppressLint("SimpleDateFormat")
    fun showVideoTime(time: Long): String {
        val totalSeconds = time / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            Formatter().format("%02d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            Formatter().format("%02d:%02d", minutes, seconds).toString()
        }
    }


    /**
     * 视频时长
     */
    @SuppressLint("SimpleDateFormat")
    fun showVideoLongTime(time: Long): String {
        val totalSeconds = time / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return Formatter().format("%02d:%02d:%02d", hours, minutes, seconds).toString()
    }

    fun updateDateTime(file: File): Long {
        var currentTime: Long
        val strName = file.name
        currentTime = 0L
        try {
            currentTime = if (strName.contains("${CommUtils.getAppName()}_")) {
                strName.substring(6, strName.lastIndexOf(".")).toLong()
            } else {
                file.lastModified()
            }
        } catch (e: Exception) {
            Log.e("视频文件名称解析异常", "${e.message}")
        }
        return currentTime
    }
}