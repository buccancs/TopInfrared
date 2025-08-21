package com.infisense.usbir.utils

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

object SupRUtils {


    /**
     * 是否能开启超分
     */
    fun canOpenSupR() :Boolean{
        return true
    }

    /**
     * 由此统一弹
     */
    fun showOpenSupRTipsDialog(activity : Activity){

    }


    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }


}