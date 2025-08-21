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

    /**
     * 生成图片报告时存在缓存目录下的临时图片文件.
     */
    fun saveToCache(context: Context, bitmap: Bitmap): String {
        val cacheFile = context.externalCacheDir ?: context.cacheDir
        val file = File(cacheFile, "Report_${System.currentTimeMillis()}.jpg")
        ImageUtils.save(bitmap, file, Bitmap.CompressFormat.JPEG)
        return file.absolutePath
    }

    /**
     * 保存图片到 图库/APP名称 下，文件名称为 APP名称_时间戳.jpg
     * 这里是热成像拍照 和 2D编辑 的图片.
     */
    fun save(bitmap: Bitmap, isTC007: Boolean = false): String {
        // 存储目录，用户可以自定义
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

    /**
     * 热成像拍照时，若开始了可见光，原始图像再叠加可见光的图片，虽然有保存，但却没有使用，原因不明
     */
    fun saveImageToApp(bitmap: Bitmap): String {
        val saveFile = File(Utils.getApp().cacheDir, "PinP_${System.currentTimeMillis()}.jpg")
        ImageUtils.save(bitmap,saveFile, Bitmap.CompressFormat.JPEG)
        return saveFile.absolutePath
    }

    //保存lite模组的原始文件
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

    //保存原始文件
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

    /**
     * 保存一帧的argb数据
     */
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