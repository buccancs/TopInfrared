package com.topdon.module.thermal.ir.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.SurfaceView
import com.energy.iruvc.sdkisp.LibIRProcess
import com.energy.iruvc.sdkisp.LibIRProcess.ImageRes_t
import com.energy.iruvc.utils.CommonParams.IRPROCSRCFMTType
import com.energy.iruvc.utils.CommonParams.PseudoColorType
import com.infisense.usbir.utils.IRImageHelp
import com.infisense.usbir.utils.OpencvTools
import com.infisense.usbir.utils.PseudocodeUtils
import com.topdon.lib.core.bean.AlarmBean
import com.topdon.pseudo.bean.CustomPseudoBean
import java.nio.ByteBuffer

/**
 * 进行 Hik 模组预览的 SurfaceView.
 *
 * Created by LCG on 2024/11/30.
 */
class HikSurfaceView : SurfaceView {
    companion object {
        /**
         * 超分放大倍数
         */
        private const val MULTIPLE = 2
    }


    /**
     * 是否开启超分
     */
    var isOpenAmplify: Boolean = false
        set(value) {
            field = value
            val isPortrait = rotateAngle == 90 || rotateAngle == 270
            val width = (if (isPortrait) 192 else 256) * (if (value) MULTIPLE else 1)
            val height = (if (isPortrait) 256 else 192) * (if (value) MULTIPLE else 1)
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }

    /**
     * 热成像画面逆时针旋转角度，取值 0、90、180、270，默认 270
     */
    @Volatile
    var rotateAngle: Int = 270
        set(value) {
            field = value
            val isPortrait = value == 90 || value == 270
            val width = (if (isPortrait) 192 else 256) * (if (isOpenAmplify) MULTIPLE else 1)
            val height = (if (isPortrait) 256 else 192) * (if (isOpenAmplify) MULTIPLE else 1)
            bitmap.reconfigure(width, height, bitmap.config)
        }

    /**
     * 温度报警配置信息，用于绘制描边或矩形.
     */
    var alarmBean = AlarmBean()

    /**
     * 等温尺限制的低温值，单位摄氏度，MIN_VALUE 表示未设置
     */
    var limitTempMin = Float.MIN_VALUE
    /**
     * 等温尺限制的高温值，单位摄氏度，MAX_VALUE 表示未设置
     */
    var limitTempMax = Float.MAX_VALUE


    /**
     * 温度报警用来描边的工具类.
     */
    private val irImageHelp = IRImageHelp()

    /**
     * 刷新自定义渲染配置
     */
    fun refreshCustomPseudo(it: CustomPseudoBean) {
        irImageHelp.setColorList(it.getColorList(), it.getPlaceList(), it.isUseGray, it.maxTemp, it.minTemp)
    }


    /**
     * 当前使用伪彩.
     */
    @Volatile
    private var pseudoType: PseudoColorType = PseudoColorType.PSEUDO_3
    /**
     * 设置当前使用的伪彩代号
     *
     * 1-白热 3-铁红 4-彩虹1 5-彩虹2 6-彩虹3 7-红热 8-热铁 9-彩虹4 10-彩虹5 11-黑热
     */
    fun setPseudoCode(code: Int) {
        pseudoType = PseudocodeUtils.changePseudocodeModeByOld(code)
    }


    /**
     * 用于温度及画面旋转参数的尺寸.
     */
    private val imageRes = ImageRes_t()
    /**
     * 当前显示图像的 Bitmap.
     */
    private var bitmap: Bitmap = Bitmap.createBitmap(192, 256, Bitmap.Config.ARGB_8888)
    /**
     * 未旋转前的 ARGB 数组.
     */
    private val sourceArgbArray = ByteArray(256 * 192 * 4)
    /**
     * 旋转后的 ARGB 数组.
     */
    private val rotateArgbArray = ByteArray(256 * 192 * 4)
    /**
     * 超分后的 ARGB 数组.
     */
    private val amplifyArray = ByteArray(256 * MULTIPLE * 192 * MULTIPLE * 4)
    /**
     * 温度数组
     */
    private val tempArray = ByteArray(256 * 192 * 2)


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        imageRes.width = 256.toChar()
        imageRes.height = 192.toChar()
    }


    /**
     * 获取缩放为当前 View 尺寸的图像.
     */
    fun getScaleBitmap(): Bitmap = synchronized(this) {
        Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    /**
     * 使用指定的 YUV 数据刷新画面
     */
    fun refresh(yuvArray: ByteArray, newTempArray: ByteArray) {
        //原始数据的宽高，即不应用旋转的宽高
        val sourceWidth = 256
        val sourceHeight = 192

        System.arraycopy(newTempArray, 0, tempArray, 0, tempArray.size)

        //自定义渲染时使用白热伪彩，当置灰模式时范围外直接不用改
        val pseudo: PseudoColorType = if (irImageHelp.getColorList() == null) pseudoType else PseudoColorType.PSEUDO_1
        LibIRProcess.convertYuyvMapToARGBPseudocolor(yuvArray, (sourceWidth * sourceHeight).toLong(), pseudo, sourceArgbArray)
        //自定义渲染
        irImageHelp.customPseudoColor(sourceArgbArray, tempArray, sourceWidth, sourceHeight)
        //等温尺
        irImageHelp.setPseudoColorMaxMin(sourceArgbArray, tempArray, limitTempMax, limitTempMin, sourceWidth, sourceHeight)
        //温度报警描边或矩形
        val newArray = irImageHelp.contourDetection(alarmBean, sourceArgbArray, tempArray, sourceWidth, sourceHeight) ?: sourceArgbArray
        //旋转
        when (rotateAngle) {
            90 -> LibIRProcess.rotateLeft90(newArray, imageRes, IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, rotateArgbArray)
            180 -> LibIRProcess.rotate180(newArray, imageRes, IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, rotateArgbArray)
            270 -> LibIRProcess.rotateRight90(newArray, imageRes, IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, rotateArgbArray)
            else  -> System.arraycopy(newArray, 0, rotateArgbArray, 0, rotateArgbArray.size)
        }
        //超分
        if (isOpenAmplify) {
            val width: Int = if (rotateAngle == 90 || rotateAngle == 270) sourceWidth else sourceHeight
            val height: Int = if (rotateAngle == 90 || rotateAngle == 270) sourceHeight else sourceWidth
            OpencvTools.supImage(rotateArgbArray, width, height, amplifyArray)
        }

        synchronized(this) {
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(if (isOpenAmplify) amplifyArray else rotateArgbArray))
        }

        val canvas: Canvas = holder.lockCanvas() ?: return
        canvas.drawBitmap(bitmap, null, Rect(0, 0, width, height), null)
        holder.unlockCanvasAndPost(canvas)
    }
}