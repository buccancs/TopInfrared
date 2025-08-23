package com.guide.zm04c.matrix

import android.util.Log
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object Logger {

    @JvmStatic
    fun e(clazz: Class<*>, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(clazz.simpleName, msg + "")
        }
    }

    @JvmStatic
    fun e(tag: String?, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg + "")
        }
    }

    @JvmStatic
    fun w(clazz: Class<*>, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.w(clazz.simpleName, msg + "")
        }
    }

    @JvmStatic
    fun w(tag: String?, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg + "")
        }
    }

    @JvmStatic
    fun i(clazz: Class<*>, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.i(clazz.simpleName, msg + "")
        }
    }

    @JvmStatic
    fun i(tag: String?, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg + "")
        }
    }

    @JvmStatic
    fun d(clazz: Class<*>, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(clazz.simpleName, msg + "")
        }
    }

    @JvmStatic
    fun d(tag: String?, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg + "")
        }
    }

    @JvmStatic
    fun v(clazz: Class<*>, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.v(clazz.simpleName, msg + "")
        }
    }

    fun v(tag: String?, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg + "")
        }
    }

    private val MYLOG_PATH_SDCARD_DIR = "/sdcard/Guide/log"

    private val MYLOGFILEName = "Log.txt"

    private val myLogSdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    private val logfile = SimpleDateFormat("yyyy-MM-dd")

    fun f(tag: String, text: String) {
        val nowtime = Date()
        val needWriteFiel = logfile.format(nowtime)
        val needWriteMessage = myLogSdf.format(nowtime) + "    " + "    " + tag + "    " + text
        val dirsFile = File(MYLOG_PATH_SDCARD_DIR)
        if (!dirsFile.exists()) {
            dirsFile.mkdirs()
        }
        val file = File(dirsFile.toString(), needWriteFiel + MYLOGFILEName)
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: Exception) {
            }
        }
        try {
            val filerWriter = FileWriter(file, true)
            val bufWriter = BufferedWriter(filerWriter)
            bufWriter.write(needWriteMessage)
            bufWriter.newLine()
            bufWriter.close()
            filerWriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}