package com.topdon.lib.core.config

import android.content.Context
import android.os.Build
import android.os.Environment
import com.blankj.utilcode.util.Utils
import com.topdon.lib.core.repository.GalleryRepository.DirType
import com.topdon.lib.core.utils.CommUtils
import java.io.File

object FileConfig {
    /**
     * 获取房屋检测缓存目录下指定文件.
     * 注意，不执行子文件创建逻辑，若有需要需自行创建.
     */
    fun getDetectImageDir(context: Context, child: String): File {
        val externalDir = context.getExternalFilesDir("detect")
        return if (externalDir == null) {
            val fileDir = File(context.filesDir, "detect")
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }
            File(fileDir, child)
        } else {
            File(externalDir, child)
        }
    }

    /**
     * 获取房屋检测-签名图片缓存目录.
     * 注意，不执行子文件创建逻辑，若有需要需自行创建.
     */
    fun getSignImageDir(context: Context, child: String): File {
        val externalDir = context.getExternalFilesDir("sign")
        return if (externalDir == null) {
            val fileDir = File(context.filesDir, "sign")
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }
            File(fileDir, child)
        } else {
            File(externalDir, child)
        }
    }

    /**
     * 固件升级包安装目录.
     */
    fun getFirmwareFile(filename: String): File = File(Utils.getApp().getExternalFilesDir("firmware"), filename)

    /**
     * 图片报告路径.
     */
    @JvmStatic
    fun getPdfDir(): String {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
            val path = dir + File.separator + CommUtils.getAppName() + File.separator + "pdf"
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
            path
        } else {
            Environment.DIRECTORY_DOCUMENTS + "/${CommUtils.getAppName()}/pdf"
        }
    }

    /**
     * 温度监控导出 Excel 目录.
     */
    @JvmStatic
    val excelDir: String
        get() {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
                val path = dir + File.separator + CommUtils.getAppName() + File.separator + "excel"
                val file = File(path)
                if (!file.exists()) {
                    file.mkdirs()
                }
                path
            } else {
                Environment.DIRECTORY_DOCUMENTS + "/${CommUtils.getAppName()}/excel"
            }
        }


    /**
     * 原有图库目录
     */
    @JvmStatic
    val gallerySourDir: String
        get() {
            val result = Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath + File.separator + "TopInfrared"
            val file = File(result)
            if (!file.exists()) {
                file.mkdirs()
            }
            return result
        }

    /**
     * 老 APP TC001 图库目录，仅用于相册迁移
     */
    @JvmStatic
    val oldTc001GalleryDir: String
        get() {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
            val path = dir + File.separator + "TC001"
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
            return path
        }

    fun getGalleryDirByType(currentDirType : DirType) : String = when (currentDirType) {
        DirType.LINE -> lineGalleryDir
        DirType.TC007 -> tc007GalleryDir
        else -> ts004GalleryDir
    }

    /**
     * 有线设备 图库目录
     */
    @JvmStatic
    val lineGalleryDir: String
        get() {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
            val path = dir + File.separator + CommUtils.getAppName()
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
            return path
        }

    /**
     * TS004 手机本地图库目录
     */
    @JvmStatic
    val ts004GalleryDir: String
        get() {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
            val path = dir + File.separator + "TS004"
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
            return path
        }

    /**
     * TC007 手机本地图库目录
     */
    @JvmStatic
    val tc007GalleryDir: String
        get() {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath
            val path = dir + File.separator + "TC007"
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
            return path
        }

    /**
     * 有线设备 手机本地图库温度数据目录
     */
    @JvmStatic
    val lineIrGalleryDir: String
        get() {
            val dir = Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath
            val path = dir + File.separator + "${CommUtils.getAppName()}-ir"
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
            return path
        }

    /**
     * TC007 手机本地图库温度数据目录
     */
    @JvmStatic
    val tc007IrGalleryDir: String
        get() {
            val dir = Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath
            val path = dir + File.separator + "TC007-ir"
            val file = File(path)
            if (!file.exists()) {
                file.mkdirs()
            }
            return path
        }



    /**
     * 外部存储/Documents/APP名称/house
     */
    @JvmStatic
    val documentsDir: String
        get() {
            return if (Build.VERSION.SDK_INT < 29) {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
                val path = dir + File.separator + CommUtils.getAppName() + "/house"
                val file = File(path)
                if (!file.exists()) {
                    file.mkdirs()
                }
                path
            } else {
                Environment.DIRECTORY_DOCUMENTS + File.separator + CommUtils.getAppName() + "/house/"
            }
        }
}