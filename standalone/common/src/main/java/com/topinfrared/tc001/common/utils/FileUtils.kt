package com.topinfrared.tc001.common.utils

import android.content.Context
import android.os.Environment
import com.topinfrared.tc001.common.constants.TC001Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {

    fun getThermalDirectory(context: Context): File {
        val externalDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val thermalDir = File(externalDir, TC001Constants.APP_FOLDER)
        
        if (!thermalDir.exists()) {
            thermalDir.mkdirs()
        }
        
        return thermalDir
    }

    fun getImagesDirectory(context: Context): File {
        val thermalDir = getThermalDirectory(context)
        val imagesDir = File(thermalDir, TC001Constants.IMAGES_FOLDER)
        
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        
        return imagesDir
    }

    fun getVideosDirectory(context: Context): File {
        val thermalDir = getThermalDirectory(context)
        val videosDir = File(thermalDir, TC001Constants.VIDEOS_FOLDER)
        
        if (!videosDir.exists()) {
            videosDir.mkdirs()
        }
        
        return videosDir
    }

    fun generateImageFilename(): String {
        val timestamp = SimpleDateFormat(
            TC001Constants.DATE_FORMAT, 
            Locale.getDefault()
        ).format(Date())
        
        return TC001Constants.IMAGE_NAME_PATTERN.format(timestamp)
    }

    fun generateVideoFilename(): String {
        val timestamp = SimpleDateFormat(
            TC001Constants.DATE_FORMAT, 
            Locale.getDefault()
        ).format(Date())
        
        return TC001Constants.VIDEO_NAME_PATTERN.format(timestamp)
    }

    fun generateTimestamp(): String {
        return SimpleDateFormat(
            TC001Constants.DATE_FORMAT, 
            Locale.getDefault()
        ).format(Date())
    }

    fun getAllThermalImages(context: Context): List<File> {
        val imagesDir = getImagesDirectory(context)
        val imageFiles = imagesDir.listFiles { _, name ->
            name.lowercase().endsWith(".${TC001Constants.IMAGE_FORMAT}")
        }
        
        return imageFiles?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun getAllThermalVideos(context: Context): List<File> {
        val videosDir = getVideosDirectory(context)
        val videoFiles = videosDir.listFiles { _, name ->
            name.lowercase().endsWith(".${TC001Constants.VIDEO_FORMAT}")
        }
        
        return videoFiles?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    fun getTotalStorageUsed(context: Context): Long {
        val thermalDir = getThermalDirectory(context)
        return calculateDirectorySize(thermalDir)
    }
    
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        
        if (directory.exists()) {
            directory.walkTopDown().forEach { file ->
                if (file.isFile) {
                    size += file.length()
                }
            }
        }
        
        return size
    }

    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toFloat()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024f
            unitIndex++
        }
        
        return "%.1f %s".format(size, units[unitIndex])
    }
}