package com.topdon.module.thermal.ir.view.compass

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.view.drawToBitmap
import com.blankj.utilcode.util.SizeUtils
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.utils.getPixelLinear
import com.topdon.module.thermal.ir.utils.getValuesBetween
import com.topdon.module.thermal.ir.utils.realX
import com.topdon.module.thermal.ir.utils.realY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 横向的指南针View
 */
class LinearCompassView : View {

    private val paint = Paint()
    private val textPaint = Paint()
    private val markerPaint = Paint()
    private val shortLinePaint = Paint()
    private val longLinePaint = Paint()
    private val positionPaint = Paint()
    private lateinit var canvas: Canvas

    private var lineColor: Int = Color.WHITE
    private var textColor: Int = Color.WHITE
    private var shortLineColor: Int = Color.WHITE
    private var longLineColor: Int = Color.WHITE
    private var positionColor: Int = Color.WHITE
    private var centerAzimuthColor = Color.WHITE
    private var textSize: Float = SizeUtils.sp2px(13f).toFloat()
    private var shortLineSize = SizeUtils.sp2px(0.5f).toFloat()
    private var longLineSize = SizeUtils.sp2px(0.5f).toFloat()
    private var positionSize = SizeUtils.sp2px(11f).toFloat()
    private var markerSize = SizeUtils.sp2px(2f).toFloat()
    private var backgroundColor = Color.BLACK

    private var lastDrawTime = 0L //执行时间
    private var step = 1000/10 //一秒绘制的帧数
    private val scope = CoroutineScope(EmptyCoroutineContext)
    var curBitmap:Bitmap?= null //当前view的bitmap

    constructor(context: Context) : this(context, null) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.LinearCompassView, 0, 0)
        lineColor = attributes.getColor(R.styleable.LinearCompassView_lineColor, Color.WHITE)
        textColor = attributes.getColor(R.styleable.LinearCompassView_textColor, Color.WHITE)
        backgroundColor =
            attributes.getColor(R.styleable.LinearCompassView_backgroundColor, Color.BLACK)
        shortLineColor =
            attributes.getColor(R.styleable.LinearCompassView_shortLineColor, Color.WHITE)
        longLineColor =
            attributes.getColor(R.styleable.LinearCompassView_longLineColor, Color.WHITE)
        positionColor =
            attributes.getColor(R.styleable.LinearCompassView_positionColor, Color.WHITE)
        centerAzimuthColor =
            attributes.getColor(R.styleable.LinearCompassView_markerColor, Color.WHITE)
        shortLineSize = attributes.getDimension(
            R.styleable.LinearCompassView_shortLineSize,
            SizeUtils.sp2px(0.5f).toFloat()
        )
        longLineSize = attributes.getDimension(
            R.styleable.LinearCompassView_longLineSize,
            SizeUtils.sp2px(0.5f).toFloat()
        )
        positionSize = attributes.getDimension(
            R.styleable.LinearCompassView_positionSize,
            SizeUtils.sp2px(11f).toFloat()
        )
        markerSize = attributes.getDimension(
            R.styleable.LinearCompassView_markerSize,
            SizeUtils.sp2px(2f).toFloat()
        )
        attributes.recycle()
        initView()
    }

    private fun initView() {
        paint.color = backgroundColor
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = 1f
        paint.isAntiAlias = true

        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.style = Paint.Style.FILL_AND_STROKE
        textPaint.isAntiAlias = true
        textPaint.strokeWidth = 1f

        markerPaint.color = centerAzimuthColor
        markerPaint.strokeWidth = markerSize
        markerPaint.style = Paint.Style.FILL_AND_STROKE
        markerPaint.isAntiAlias = true

        shortLinePaint.color = shortLineColor
        shortLinePaint.strokeWidth = shortLineSize
        shortLinePaint.style = Paint.Style.STROKE
        shortLinePaint.isAntiAlias = true

        longLinePaint.color = longLineColor
        longLinePaint.strokeWidth = longLineSize
        longLinePaint.style = Paint.Style.STROKE
        longLinePaint.isAntiAlias = true

        positionPaint.color = positionColor
        positionPaint.textSize = positionSize
        positionPaint.style = Paint.Style.FILL_AND_STROKE
        positionPaint.isAntiAlias = true
        positionPaint.strokeWidth = 1f
    }

    private var showAzimuthArrow = true

    private var azimuth = 0f
    private var range = 180f

    private var text: String = ""

    private fun getRawMinimum() = azimuth - range / 2
    private fun getRawMaximum() = azimuth + range / 2

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        this.canvas = canvas
//        drawBackGround()
        drawAzimuthArrow()
        drawCompassLine()
    }

    //绘制背景
    private fun drawBackGround() {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    //绘制角度
    private fun drawAzimuthArrow() {
        if (!showAzimuthArrow) {
            return
        }
        val endWidth = width / 2f
        val endHeight = (3 / 10f) * height
        canvas.drawText(text, realX(text, endWidth,textPaint), realY(text, endHeight,textPaint), textPaint)
    }

    //绘制标记线
    private fun drawCompassLine() {
        //计算指南针的线有几等份
//        val values = getValuesBetween(getRawMinimum(), getRawMaximum(), 5f).map { it.toInt() }
        drawCompass()
        val bottomHeight = height * 7 / 10f
        canvas.drawLine(0f, (bottomHeight - 1), width.toFloat(), bottomHeight, shortLinePaint)
        //在中间位置绘制标志线
        canvas.drawLine(
            width / 2f + markerSize / 2,
            height * (3 / 10f),
            width / 2f + markerSize / 2,
            height * (7 / 10f),
            markerPaint
        )
    }

    fun setCurAzimuth(azimuth: Int) {
        scope.launch(Dispatchers.IO) {
            this@LinearCompassView.azimuth = azimuth.toFloat()
            this@LinearCompassView.text = azimuth.toString()
            var curTime = System.currentTimeMillis()
            if(curTime - lastDrawTime> step) {
                lastDrawTime = curTime
                launch(Dispatchers.Main) {
                    curBitmap = this@LinearCompassView.drawToBitmap()
                    invalidate()
                }
            }
        }
    }

    private fun drawCompass() {
        getValuesBetween(getRawMinimum(), getRawMaximum(), 5f).map {
            it.toInt()
        }.toMutableList().forEach {
            //计算实际X的坐标
            val x = toPixel(it.toFloat())

            // 最短：15度 最长：90度 起始点x坐标
            val lineHeight = when {
                it % 90 == 0 -> (3 / 10f) * height
                it % 15 == 0 -> (4 / 10f) * height
                else -> (5 / 10f) * height
            }
            //起始点y
            val bottomHeight = height * 7 / 10f

            //绘制标记线
            when {
                it % 90 == 0 -> canvas.drawLine(x, lineHeight, x, bottomHeight, longLinePaint)
                else -> canvas.drawLine(x, lineHeight, x, bottomHeight, shortLinePaint)
            }

            //绘制底部方位文本
            if (it % 45 == 0) {
                val coord = getPositionText(it)
                canvas.drawText(coord, realX(coord, x,positionPaint), realY(coord, height - 2f,positionPaint), positionPaint)
            }
        }
    }

    private fun getPositionText(position: Int): String = when (position) {
        -90, 270 -> resources.getString(R.string.compass_west)
        -45, 315 -> resources.getString(R.string.compass_northwest)
        0, 360 -> resources.getString(R.string.compass_north)
        45, 405 -> resources.getString(R.string.compass_northeast)
        90, 450 -> resources.getString(R.string.compass_east)
        135, 495 -> resources.getString(R.string.compass_southeast)
        -180, 180 -> resources.getString(R.string.compass_south)
        -135, 225 -> resources.getString(R.string.compass_southwest)
        else -> ""
    }

    private fun toPixel(bearing: Float): Float {
        return getPixelLinear(
            bearing,
            azimuth,
            width.toFloat(),
            range
        )
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scope.cancel()
    }
}