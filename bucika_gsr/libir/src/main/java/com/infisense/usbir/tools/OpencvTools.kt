package com.infisense.usbir.tools



/**
 * @author: CaiSongL
 * @date: 2023/5/18 9:46
 */
object OpencvTools {

//    init {
//        System.loadLibrary("opencv_java4");
//    }
//
//
//    fun mergeBitmap(backBitmap: Bitmap,frontBitmap : Bitmap) : Bitmap?{
//        val time = System.currentTimeMillis()
//        if (backBitmap == null || backBitmap.isRecycled() || frontBitmap == null || frontBitmap.isRecycled()) {
//            return null
//        }
//        var backM = Mat()
//        var frontM = Mat()
//        var dst = Mat()
//        Utils.bitmapToMat(backBitmap, backM)
//        Utils.bitmapToMat(frontBitmap, frontM)
//        addWeighted(backM, 1.0, frontM, 0.0, 0.0, dst)
//        val dstBitmap = Bitmap.createBitmap(backM.width(), backM.height(), Bitmap.Config.ARGB_8888)
//        Utils.matToBitmap(dst,dstBitmap)
//        Log.w("opencv图像合并时间耗时：","${System.currentTimeMillis() - time}")
//        return dstBitmap
//    }

}