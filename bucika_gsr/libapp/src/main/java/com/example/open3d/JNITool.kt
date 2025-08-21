package com.example.open3d


/**
 * 统一的c++调试
 * @author: CaiSongL
 * @date: 2023/10/23 17:57
 */
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

    /**
     * 轮廓检测的处理
     * @param image ByteArray
     * @param temperature ByteArray
     * @param image_h Int
     * @param image_w Int
     * @param high_t Float
     * @param low_t Float
     * @param color_h Int
     * @param color_l Int
     * @param type Int ： 圈住的轮廓画法，1：直接轮廓绘制，2、矩阵绘制
     * @return ByteArray
     */
    external fun draw_edge_from_temp_reigon_bitmap_argb_psd(image : ByteArray,temperature : ByteArray, image_h : Int,
                                                            image_w : Int,high_t : Float, low_t : Float,
                                                            color_h : Int, color_l:Int,type : Int) : ByteArray

}