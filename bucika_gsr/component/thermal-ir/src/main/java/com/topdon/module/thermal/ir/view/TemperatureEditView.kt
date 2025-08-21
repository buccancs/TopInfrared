package com.topdon.module.thermal.ir.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.energy.iruvc.sdkisp.LibIRTemp
import com.energy.iruvc.utils.Line
import com.infisense.usbir.utils.TempDrawHelper.Companion.correct
import com.infisense.usbir.view.ITsTempListener
import java.lang.ref.WeakReference

/**
 * 2D 编辑 点线面温度图层 View.
 */
class TemperatureEditView : TemperatureBaseView {

    override var mode: Mode
        get() = super.mode
        set(value) {
            super.mode = value
            if (mode == Mode.CLEAR) {
                tempListData.pointTemps.clear()
                tempListData.lineTemps.clear()
                tempListData.rectangleTemps.clear()
                for (i in 0 until 3) {
                    val tmp = irtemp.TemperatureSampleResult()
                    tmp.type = -99
                    tempListData.pointTemps.add(tmp)
                    tempListData.lineTemps.add(tmp)
                    tempListData.rectangleTemps.add(tmp)
                }
            }
        }


    class TemperatureList {
        var pointTemps = arrayListOf<LibIRTemp.TemperatureSampleResult>()
        var lineTemps = arrayListOf<LibIRTemp.TemperatureSampleResult>()
        var rectangleTemps = arrayListOf<LibIRTemp.TemperatureSampleResult>()
    }
    var tempListData = TemperatureList()

    private var irtemp: LibIRTemp = LibIRTemp()
    private var irTempData: ByteArray = byteArrayOf()
    var fullInfo: LibIRTemp.TemperatureSampleResult? = null



    /**
     * 是否显示点线面名字.
     */
    var isShowName = false
        set(value) {
            field = value
            invalidate()
        }

    private var iTsTempListenerWeakReference: WeakReference<ITsTempListener>? = null

    fun setITsTempListener(listener: ITsTempListener) {
        iTsTempListenerWeakReference = WeakReference(listener)
    }

    private fun getTSTemp(temp: Float): Float = iTsTempListenerWeakReference?.get()?.tempCorrectByTs(temp) ?: temp



    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        tempListData.pointTemps.clear()
        tempListData.lineTemps.clear()
        tempListData.rectangleTemps.clear()
        for (i in 0 until 3) {
            val tmp = irtemp.TemperatureSampleResult()
            tmp.type = -99
            tempListData.pointTemps.add(tmp)
            tempListData.lineTemps.add(tmp)
            tempListData.rectangleTemps.add(tmp)
        }
    }


    override fun setImageSize(imageWidth: Int, imageHeight: Int) {
        super.setImageSize(imageWidth, imageHeight)
        irtemp = LibIRTemp(imageWidth, imageHeight)
    }

    fun setData(bytes: ByteArray) {
        irTempData = bytes
        irtemp.setTempData(irTempData)
        fullInfo = irtemp.getTemperatureOfRect(Rect(0, 0, imageWidth, imageHeight))
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        //绘制点
        for (i in pointList.indices) {
            val result = drawOnePoint(canvas, pointList[i], i) ?: continue
            tempListData.pointTemps[i] = result
        }
        operatePoint?.let { drawOnePoint(canvas, it, pointList.size + 1) }

        //绘制线
        for (i in lineList.indices) {
            val result = drawOneLine(canvas, lineList[i], i) ?: continue
            tempListData.lineTemps[i] = result
        }
        operateLine?.let { drawOneLine(canvas, it, lineList.size + 1) }

        //绘制面
        for (i in rectList.indices) {
            val result = drawOneRect(canvas, rectList[i], i) ?: continue
            tempListData.rectangleTemps[i] = result
        }
        operateRect?.let { drawOneRect(canvas, it, rectList.size + 1) }

        if (isShowFull) {
            fullInfo?.let {
                val maxX: Int = (it.maxTemperaturePixel.x * xScale).correct(width)
                val maxY: Int = (it.maxTemperaturePixel.y * yScale).correct(height)
                drawCircle(canvas, maxX, maxY, true)
                drawTempText(canvas, maxX, maxY, getTSTemp(it.maxTemperature))

                val minX: Int = (it.minTemperaturePixel.x * xScale).correct(width)
                val minY: Int = (it.minTemperaturePixel.y * yScale).correct(height)
                drawCircle(canvas, minX, minY, false)
                drawTempText(canvas, minX, minY, getTSTemp(it.minTemperature))
            }

            val centerX = width / 2
            val centerY = height / 2
            val centerResult = irtemp.getTemperatureOfPoint(Point(imageWidth / 2, imageHeight / 2))
            drawPoint(canvas, Point(centerX, centerY))
            drawTempText(canvas, centerX, centerY, getTSTemp(centerResult.maxTemperature))
        }
    }

    /**
     * 绘制一个十字架、实心圆、温度文字、点名称.
     * @param point 以 View 尺寸为坐标系的点
     */
    private fun drawOnePoint(canvas: Canvas, point: Point, index: Int): LibIRTemp.TemperatureSampleResult? {
        val result = try {
            irtemp.getTemperatureOfPoint(Point((point.x / xScale).toInt(), (point.y / yScale).toInt()))
        } catch (_: IllegalArgumentException) {
            //当 View 尺寸变更就会导致 xScale、yScale 变更，而已绘制的点线面坐标还是未变更前的坐标
            //以 旧坐标及新 scale 去计算温度坐标的话，就有可能超出温度坐标范围从而抛出异常，这里捕获
            return null
        }
        drawPoint(canvas, point)
        drawCircle(canvas, point.x, point.y, true)
        drawTempText(canvas, point.x, point.y, getTSTemp(result.maxTemperature))
        if (isShowName) {
            drawPointName(canvas, "P${index + 1}", point)
        }
        return result
    }

    /**
     * 绘制一条线段、高低温实心圆、高低温温度文字、线名称.
     * @param line 以 View 尺寸为坐标系的线
     */
    private fun drawOneLine(canvas: Canvas, line: Line, index: Int): LibIRTemp.TemperatureSampleResult? {
        drawLine(canvas, line)

        val tempStartX: Int = (line.start.x / xScale).toInt()
        val tempStartY: Int = (line.start.y / yScale).toInt()
        val tempStopX: Int = (line.end.x / xScale).toInt()
        val tempStopY: Int = (line.end.y / yScale).toInt()
        if (tempStartX == tempStopX && tempStartY == tempStopY) {
            return null
        }

        val result = try {
            irtemp.getTemperatureOfLine(Line(Point(tempStartX, tempStartY), Point(tempStopX, tempStopY)))
        } catch (_: IllegalArgumentException) {
            //当 View 尺寸变更就会导致 xScale、yScale 变更，而已绘制的点线面坐标还是未变更前的坐标
            //以 旧坐标及新 scale 去计算温度坐标的话，就有可能超出温度坐标范围从而抛出异常，这里捕获
            return null
        }
        val maxX: Int = (result.maxTemperaturePixel.x * xScale).correct(width)
        val maxY: Int = (result.maxTemperaturePixel.y * yScale).correct(height)
        val minX: Int = (result.minTemperaturePixel.x * xScale).correct(width)
        val minY: Int = (result.minTemperaturePixel.y * yScale).correct(height)
        drawCircle(canvas, maxX, maxY, true)
        drawCircle(canvas, minX, minY, false)
        drawTempText(canvas, maxX, maxY, getTSTemp(result.maxTemperature))
        drawTempText(canvas, minX, minY, getTSTemp(result.minTemperature))

        if (isShowName) {
            drawLineName(canvas, "L${index + 1}", line)
        }
        return result
    }

    /**
     * 绘制一个矩形、高低温实心圆、高低温温度文字、面名称.
     * @param rect 以 View 尺寸为坐标系的线
     */
    private fun drawOneRect(canvas: Canvas, rect: Rect, index: Int): LibIRTemp.TemperatureSampleResult? {
        drawRect(canvas, rect)

        // rect 里的数据在 touch 事件已处理过了，left < right, top < bottom
        val left = (rect.left / xScale).toInt()
        val top = (rect.top / yScale).toInt()
        val right = (rect.right / xScale).toInt()
        val bottom = (rect.bottom / yScale).toInt()
        if (left == right || top == bottom) {
            return null
        }
        val result = try {
            irtemp.getTemperatureOfRect(Rect(left, top, right, bottom))
        } catch (_: IllegalArgumentException) {
            //当 View 尺寸变更就会导致 xScale、yScale 变更，而已绘制的点线面坐标还是未变更前的坐标
            //以 旧坐标及新 scale 去计算温度坐标的话，就有可能超出温度坐标范围从而抛出异常，这里捕获
            return null
        }
        val maxX: Int = (result.maxTemperaturePixel.x * xScale).correct(width)
        val maxY: Int = (result.maxTemperaturePixel.y * yScale).correct(height)
        val minX: Int = (result.minTemperaturePixel.x * xScale).correct(width)
        val minY: Int = (result.minTemperaturePixel.y * yScale).correct(height)
        drawCircle(canvas, maxX, maxY, true)
        drawCircle(canvas, minX, minY, false)
        drawTempText(canvas, maxX, maxY, getTSTemp(result.maxTemperature))
        drawTempText(canvas, minX, minY, getTSTemp(result.minTemperature))

        if (isShowName) {
            drawRectName(canvas, "R${index + 1}", rect)
        }
        return result
    }
}