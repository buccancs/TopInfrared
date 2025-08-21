package com.topdon.module.thermal.ir.utils

import android.graphics.Bitmap
import android.util.Log
import android.widget.ImageView
import androidx.annotation.FloatRange
import com.topdon.module.thermal.ir.R
import org.bytedeco.opencv.global.opencv_core.BORDER_DEFAULT
import org.bytedeco.opencv.global.opencv_core.CV_16S
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.pow

/**
 * @author: CaiSongL
 * @date: 2023/4/4 14:28
 */
object IRImageUtil {

    /**
     * 伽马对比度
     * @param contrast      对比度 1: 复位  0: 增强   2: 减弱变灰
     * @param brightness    亮度
     */
    fun showContrast(
        imageView: ImageView,
        @FloatRange(from = 0.0, to = 2.0) contrast: Double,
        @FloatRange(from = -255.0, to = 255.0) brightness: Double
    ) {
        try {
            val lookUpTable = Mat(1, 256, CvType.CV_8U)
            val lookUpTableData = ByteArray((lookUpTable.total() * lookUpTable.channels()).toInt())
            Log.w("123", "lookUpTableData: ${lookUpTableData.size}")
            Log.w("123", "contrast: $contrast")
            for (i in 0 until lookUpTable.cols()) {
                if (i % 10 == 0) {
                    Log.i("123", "${i}, lutGamma x: ${i / 255.0}, ${lutGamma(x = i / 255.0, gamma = contrast) * 255.0}")
                }
                lookUpTableData[i] = (lutGamma(x = i / 255.0, gamma = contrast) * 255.0).toInt().toByte()
            }
            Log.w("123", "lookUpTableData: ${lookUpTableData[1].toUByte()}")
            lookUpTable.put(0, 0, lookUpTableData)
            val srcMat = Utils.loadResource(com.blankj.utilcode.util.Utils.getApp(), R.drawable.ic_main_menu_battery) //BGR
            val dstMat = Mat()
            Core.LUT(srcMat, lookUpTable, dstMat) //对比度
            Core.add(dstMat, Scalar(brightness, brightness, brightness), dstMat) //亮度
            val resultMat = Mat()
            Imgproc.cvtColor(dstMat, resultMat, Imgproc.COLOR_BGR2RGBA) //android对应图像格式
            val bitmap = Bitmap.createBitmap(resultMat.size().width.toInt(), resultMat.size().height.toInt(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(resultMat, bitmap)
            imageView.setImageBitmap(bitmap)
            srcMat.release()
            dstMat.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * 伽马曲线
     * https://www.cnblogs.com/AlgrithmsRookie/p/13212369.html
     * @param a     [0 ~ 1]交界点
     * @param gamma 变化强度
     */
    private fun lutGamma(@FloatRange(from = 0.0, to = 1.0) x: Double, a: Double = 0.5, gamma: Double): Double {
        val y = if (x <= a) {
            a - a * ((1 - x / a).pow(gamma))
        } else {
            a + (1 - a) * (((x - a) / (1 - a)).pow(gamma))
        }
        return y
    }

    /**
     * 锐化
     * @param sharpen [1,3,5]
     *
     * kernel_size  锐化程度,设置是奇正数
     */
    private fun showSharpen(imageView: ImageView, @FloatRange(from = 0.0, to = 2.55) sharpen: Double) {
        Log.i("123", "show sharpen: $sharpen")
        val scale = 1.0
        val delta = 0.0
        val kernelSize = 3 //锐化程度

        val srcMat = Utils.loadResource(com.blankj.utilcode.util.Utils.getApp(), R.drawable.ic_main_menu_battery) //BGR
        val dstMat = Mat(srcMat.rows(), srcMat.cols(), srcMat.type())
        val preGray = Mat()
        val absDst = Mat()
        Log.i("123", "start kernel_size: $kernelSize")
        Imgproc.GaussianBlur(srcMat, srcMat, Size(3.0, 3.0), 0.0, 0.0, BORDER_DEFAULT) //高斯平滑
        Imgproc.cvtColor(srcMat, preGray, Imgproc.COLOR_BGR2GRAY)
        Log.w("123", "cvtColor preGray: $preGray")
        Imgproc.Laplacian(srcMat, dstMat, CV_16S, kernelSize, scale, delta, BORDER_DEFAULT) //锐化
        Log.w("123", "Laplacian dstMat: $dstMat")
        Core.convertScaleAbs(dstMat, absDst)
        Log.w("123", "convertScaleAbs absDst: $absDst")
        val preMat = Mat()
        Core.addWeighted(srcMat, 1.0, absDst, sharpen, 0.0, preMat) //融合
        Imgproc.cvtColor(preMat, dstMat, Imgproc.COLOR_BGR2RGBA) //android对应图像格式
        val bitmap = Bitmap.createBitmap(dstMat.size().width.toInt(), dstMat.size().height.toInt(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(dstMat, bitmap)
        imageView.setImageBitmap(bitmap)
        srcMat.release()
        dstMat.release()
    }
}