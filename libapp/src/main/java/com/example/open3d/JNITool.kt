package com.example.open3d

object JNITool {

    init {
        System.loadLibrary("open3d")
    }

    external fun stringFromJNI(): String

    external fun adaptiveThresholdFromJNI(matAddr: Long)
    external fun diff2firstFrameU1(byteArray: ByteArray, img:ByteArray): ByteArray
    external fun diff2firstFrameU4(byteArray: ByteArray, img:ByteArray): ByteArray
    external fun diff2firstFrameU4ByWH(width : Int,height : Int,byteArray: ByteArray, img:ByteArray): ByteArray

    external fun diff2firstFrameByTemp(basteTempArray: ByteArray,nextTempArray:ByteArray,nextImageArray:ByteArray): ByteArray
    external fun diff2firstFrameByTempWH(width : Int,height : Int,basteTempArray: ByteArray,nextTempArray:ByteArray,nextImageArray:ByteArray): ByteArray

    external fun maxTempL(img:ByteArray,temp : ByteArray,width : Int,height: Int,input : Double): ByteArray
    external fun lowTemTrack(img:ByteArray,temp : ByteArray,width : Int,height: Int,input : Double): ByteArray

    external fun draw_edge_from_temp_reigon_bitmap_argb_psd(image : ByteArray,temperature : ByteArray, image_h : Int,
                                                            image_w : Int,high_t : Float, low_t : Float,
                                                            color_h : Int, color_l:Int,type : Int) : ByteArray

}