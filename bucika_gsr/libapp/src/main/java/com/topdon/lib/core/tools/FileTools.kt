package com.topdon.lib.core.tools

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.blankj.utilcode.util.Utils
import java.io.File
import java.lang.Exception

object FileTools {

    fun getFileSize(path: String): String {
        var str = ""
        try {
            val file = File(path)
            var len = file.length()

            if (len < 1024) {
                str = "${len}Byte"
            } else if (len < 1024 * 1024) {
                str = "${len / 1024}KB"
            } else if (len < 1024 * 1024 * 1024) {
                str = "${len / 1024 / 1024}MB"
            }
        } catch (e: Exception) {
            str = "0KB"
        }
        return str
    }

    fun getUri(file: File): Uri {
        val authority = "${Utils.getApp().packageName}.fileprovider"
        return FileProvider.getUriForFile(Utils.getApp(), authority, file)
    }

    fun getImagePathFromURI(path: String): Uri? {
        val cr: ContentResolver = Utils.getApp().contentResolver
        val buffer = StringBuffer()
        buffer.append("(").append(MediaStore.Images.ImageColumns.DATA)
            .append("=").append("'").append(path).append("'")
            .append(")")
        val cur: Cursor? = cr.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.ImageColumns._ID),
            buffer.toString(),
            null,
            null
        )
        var index = 0
        if (cur == null) {
            return null
        }
        cur.moveToFirst()
        while (!cur.isAfterLast) {
            index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID)
            index = cur.getInt(index)
            cur.moveToNext()
        }
        return if (index != 0) {
            Uri.parse("content://media/external/images/media/$index")
        } else null
    }
}