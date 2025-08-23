package com.topdon.lib.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.Utils
import com.elvishew.xlog.XLog
import com.topdon.lib.core.config.FileConfig.lineIrGalleryDir
import java.io.*

object ImageUtils {

    fun saveToCache(context: Context, bitmap: Bitmap): String {
        val cacheFile = context.externalCacheDir ?: context.cacheDir
        val file = File(cacheFile, "Report_${System.currentTimeMillis()}.jpg")
        ImageUtils.save(bitmap, file, Bitmap.CompressFormat.JPEG)
        return file.absolutePath
    }

    fun save(bitmap: Bitmap, isTC007: Boolean = false): String {
        val dicName = if (isTC007) "TC007" else CommUtils.getAppName()
        val fileName = "${dicName}_${System.currentTimeMillis()}.jpg"
        val saveFile = ImageUtils.save2Album(bitmap, dicName, Bitmap.CompressFormat.JPEG)
        return if (saveFile != null) {
            val name = saveFile.name
            name.replace(".JPG", "")
        } else {
            fileName.replace(".JPG", "")
        }
    }

    fun saveImageToApp(bitmap: Bitmap): String {
        val saveFile = File(Utils.getApp().cacheDir, "PinP_${System.currentTimeMillis()}.jpg")
        ImageUtils.save(bitmap,saveFile, Bitmap.CompressFormat.JPEG)
        return saveFile.absolutePath
    }

    fun saveLiteFrame(bs: ByteArray, capital: ByteArray,nuct : ByteArray, name: String) {
        try {
            val dir = lineIrGalleryDir
            val galleryPath = File(dir)
            val fileName = "${name}.ir"
            val file = File(galleryPath, fileName)
            file.writeBytes(capital.plus(bs))
            Log.w("保存帧数据:",file.absolutePath)
        }catch (e: Exception) {
            XLog.e("一帧图像保存异常: ${e.message}")
        }
    }

    fun saveFrame(bs: ByteArray, capital: ByteArray, name: String) {
        try {
            val dir = lineIrGalleryDir
            val galleryPath = File(dir)
            val fileName = "${name}.ir"
            val file = File(galleryPath, fileName)
            file.writeBytes(capital.plus(bs))
            Log.w("保存帧数据:",file.absolutePath)
        }catch (e: Exception) {
            XLog.e("一帧图像保存异常: ${e.message}")
        }
    }

    fun saveOneFrameAGRB(bs: ByteArray, name: String) {
        try {
            val dir = lineIrGalleryDir
            val galleryPath = File(dir)
            val fileName = "${name}.ir"
            val file = File(galleryPath, fileName)
            file.writeBytes(bs)
        }catch (e: Exception) {
            XLog.e("一帧图像保存异常: ${e.message}")
        }
    }
}