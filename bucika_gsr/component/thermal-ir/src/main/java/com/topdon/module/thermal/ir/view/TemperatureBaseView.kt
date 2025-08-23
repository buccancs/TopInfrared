package com.topdon.module.thermal.ir.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import com.blankj.utilcode.util.SizeUtils
import com.energy.iruvc.utils.Line
import com.infisense.usbir.utils.TempDrawHelper
import com.infisense.usbir.utils.TempDrawHelper.Companion.correct
import com.infisense.usbir.utils.TempDrawHelper.Companion.correctPoint
import com.topdon.lib.core.tools.UnitTools
import com.topdon.module.thermal.ir.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

abstract class TemperatureBaseView : View {

    companion object {
        
        private const val DEFAULT_MAX_COUNT = 3

        private val TOUCH_TOLERANCE = SizeUtils.dp2px(8f)
        
        private val DELETE_TOLERANCE = SizeUtils.dp2px(2f)
    }

    enum class Mode {
        POINT,
        LINE,
        RECT,
        TREND,
        FULL,
        CLEAR,
    }

    @Volatile
    var isShowFull: Boolean = true
        set(value) {
            field = value
            if (value && mode == Mode.CLEAR) {
                mode = Mode.FULL
            }
            invalidate()
        }
    
    @Volatile
    open var mode = Mode.FULL
        set(value) {
            field = value
            if (value == Mode.FULL) {
                isShowFull = true
                invalidate()
            } else if (value == Mode.CLEAR) {
                isShowFull = false
                synchronized(this) {
                    pointList.clear()
                    lineList.clear()
                    rectList.clear()
                }
                trendLine = null
                invalidate()
            }
        }

    var tempTextSize: Int
        get() = helper.textSize
        set(value) {
            helper.textSize = value
            invalidate()
        }
    
    var textColor: Int
        @ColorInt get() = helper.textColor
        set(@ColorInt value) {
            helper.textColor = value
            invalidate()
        }

    var onPointListener: ((pointList: List<Point>) -> Unit)? = null
    
    var onLineListener: ((lineList: List<Point>) -> Unit)? = null
    
    var onRectListener: ((rectList: List<Rect>) -> Unit)? = null
    
    var onTrendOperateListener: ((isAdd: Boolean) -> Unit)? = null

    protected val pointList = ArrayList<Point>()
    
    protected val lineList = ArrayList<Line>()
    
    protected val rectList = ArrayList<Rect>()
    
    @Volatile
    protected var trendLine: Line? = null

    protected fun getPointListSafe(): List<Point> = synchronized(this) { pointList }
    protected fun getLineListSafe(): List<Line> = synchronized(this) { lineList }
    protected fun getRectListSafe(): List<Rect> = synchronized(this) { rectList }

    private fun getSourcePointList(): List<Point> {
        val resultList = ArrayList<Point>(pointList.size)
        pointList.forEach {
            resultList.add(Point((it.x / xScale).toInt(), (it.y / yScale).toInt()))
        }
        return resultList
    }

    private fun getSourceLineList(): List<Point> {
        val resultList = ArrayList<Point>(lineList.size * 2)
        lineList.forEach {
            val startPoint = Point((it.start.x / xScale).toInt(), (it.start.y / yScale).toInt())
            val endPoint = Point((it.end.x / xScale).toInt(), (it.end.y / yScale).toInt())
            resultList.add(startPoint)
            resultList.add(endPoint)
        }
        return resultList
    }

    private fun getSourceRectList(): List<Rect> {
        val resultList = ArrayList<Rect>(rectList.size)
        rectList.forEach {
            val left = (it.left / xScale).toInt()
            val right = (it.right / xScale).toInt()
            val top = (it.top / yScale).toInt()
            val bottom = (it.bottom / yScale).toInt()
            resultList.add(Rect(left, top, right, bottom))
        }
        return resultList
    }

    private val helper = TempDrawHelper()

    protected var xScale = 0f
    protected var yScale = 0f
    protected var imageWidth = 0
    protected var imageHeight = 0

    @CallSuper
    open fun setImageSize(imageWidth: Int, imageHeight: Int) {
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.xScale = width.toFloat() / imageWidth.toFloat()
        this.yScale = height.toFloat() / imageHeight.toFloat()
    }

    protected val maxCount: Int

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.TemperatureBaseView)
        maxCount = typeArray.getInt(R.styleable.TemperatureBaseView_maxCount, DEFAULT_MAX_COUNT)
        typeArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        xScale = measuredWidth.toFloat() / imageWidth
        yScale = measuredHeight.toFloat() / imageHeight
    }

    protected fun drawPoint(canvas: Canvas, point: Point) {
        helper.drawPoint(canvas, point.x, point.y)
    }

    protected fun drawLine(canvas: Canvas, line: Line) {
        val startX: Int = ((line.start.x / xScale).toInt() * xScale).toInt()
        val startY: Int = ((line.start.y / yScale).toInt() * yScale).toInt()
        val stopX: Int = ((line.end.x / xScale).toInt() * xScale).toInt()
        val stopY: Int = ((line.end.y / yScale).toInt() * yScale).toInt()
        helper.drawLine(canvas, startX, startY, stopX, stopY)
    }

    protected fun drawRect(canvas: Canvas, rect: Rect) {
        val left: Int = ((rect.left / xScale).toInt() * xScale).toInt()
        val top: Int = ((rect.top / yScale).toInt() * yScale).toInt()
        val right: Int = ((rect.right / xScale).toInt() * xScale).toInt()
        val bottom: Int = ((rect.bottom / yScale).toInt() * yScale).toInt()
        helper.drawRect(canvas, left, top, right, bottom)
    }

    protected fun drawCircle(canvas: Canvas, x: Int, y: Int, isMax: Boolean) {
        helper.drawCircle(canvas, x, y, isMax)
    }

    protected fun drawTempText(canvas: Canvas, x: Int, y: Int, temp: Float) {
        helper.drawTempText(canvas, UnitTools.showC(temp), width, x, y)
    }

    protected fun drawTrendText(canvas: Canvas, line: Line) {
        helper.drawTrendText(canvas, width, height, line.start.x, line.start.y, line.end.x, line.end.y)
    }

    protected fun drawPointName(canvas: Canvas, name: String, point: Point) {
        val x = ((point.x / xScale).toInt() * xScale).toInt()
        val y = ((point.y / yScale).toInt() * yScale).toInt()
        helper.drawPointName(canvas, name, width, height, x, y)
    }

    protected fun drawLineName(canvas: Canvas, name: String, line: Line) {
        val startX = ((line.start.x / xScale).toInt() * xScale).toInt()
        val startY = ((line.start.y / yScale).toInt() * yScale).toInt()
        val stopX = ((line.end.x / xScale).toInt() * xScale).toInt()
        val stopY = ((line.end.y / yScale).toInt() * yScale).toInt()
        helper.drawPointRectName(canvas, name, width, height, startX, startY, stopX, stopY)
    }

    protected fun drawRectName(canvas: Canvas, name: String, rect: Rect) {
        val left: Int = ((rect.left / xScale).toInt() * xScale).toInt()
        val top: Int = ((rect.top / yScale).toInt() * yScale).toInt()
        val right: Int = ((rect.right / xScale).toInt() * xScale).toInt()
        val bottom: Int = ((rect.bottom / yScale).toInt() * yScale).toInt()
        helper.drawPointRectName(canvas, name, width, height, left, top, right, bottom)
    }

    private var downX = 0
    private var downY = 0

    private var isAddAction = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        return when (mode) {
            Mode.POINT -> touchPoint(event)
            Mode.LINE -> touchLine(event, false)
            Mode.RECT -> touchRect(event)
            Mode.TREND -> touchLine(event, true)
            else -> super.onTouchEvent(event)
        }
    }

    protected var operatePoint: Point? = null

    private fun touchPoint(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.correctPoint(width)
                downY = event.y.correctPoint(height)
                val point: Point? = pollPoint(downX, downY)
                isAddAction = point == null
                operatePoint = point ?: Point(downX, downY)
                if (point == null && pointList.size == maxCount) {
                    synchronized(this) {
                        pointList.removeAt(0)
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                operatePoint?.x = event.x.correctPoint(width)
                operatePoint?.y = event.y.correctPoint(height)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val x: Int = event.x.correctPoint(width)
                val y: Int = event.y.correctPoint(height)
                operatePoint?.x = x
                operatePoint?.y = y

                if (isAddAction || abs(x - downX) > DELETE_TOLERANCE || abs(y - downY) > DELETE_TOLERANCE) {
                    synchronized(this) {
                        pointList.add(operatePoint ?: Point())
                    }
                }
                operatePoint = null
                invalidate()

                onPointListener?.invoke(getSourcePointList())
                return true
            }
            else -> return false
        }
    }

    private fun pollPoint(x: Int, y: Int): Point? {
        for (i in pointList.size - 1 downTo  0) {
            val point: Point = pointList[i]
            if (point.x in x - TOUCH_TOLERANCE .. x + TOUCH_TOLERANCE && point.y in y - TOUCH_TOLERANCE .. y + TOUCH_TOLERANCE) {
                return synchronized(this) { pointList.removeAt(i) }
            }
        }
        return null
    }

    protected var operateLine: Line? = null
    
    protected var operateTrend: Line? = null

    private enum class LineMoveType { ALL, START, END, }
    
    private var lineMoveType = LineMoveType.ALL

    private val downLine: Line = Line(Point(0, 0), Point(0, 0))

    private fun touchLine(event: MotionEvent, isTrend: Boolean): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.correct(width)
                downY = event.y.correct(height)
                val line: Line? = pollLine(downX, downY, isTrend)
                if (line == null) {
                    isAddAction = true
                    if (isTrend) {
                        operateTrend = Line(Point(downX, downY), Point(downX, downY))
                        trendLine = null
                        onTrendOperateListener?.invoke(false)
                    } else {
                        operateLine = Line(Point(downX, downY), Point(downX, downY))
                        if (lineList.size == maxCount) {
                            synchronized(this) {
                                lineList.removeAt(0)
                            }
                        }
                    }
                } else {
                    isAddAction = false
                    if (isTrend) {
                        operateTrend = line
                        onTrendOperateListener?.invoke(false)
                    } else {
                        operateLine = line
                    }
                    downLine.start.set(line.start.x, line.start.y)
                    downLine.end.set(line.end.x, line.end.y)
                    lineMoveType = if (downX > line.start.x - TOUCH_TOLERANCE && downX < line.start.x + TOUCH_TOLERANCE &&
                        downY > line.start.y - TOUCH_TOLERANCE && downY < line.start.y + TOUCH_TOLERANCE) {
                        LineMoveType.START
                    } else if (downX > line.end.x - TOUCH_TOLERANCE && downX < line.end.x + TOUCH_TOLERANCE &&
                        downY > line.end.y - TOUCH_TOLERANCE && downY < line.end.y + TOUCH_TOLERANCE) {
                        LineMoveType.END
                    } else {
                        LineMoveType.ALL
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val x: Int = event.x.correct(width)
                val y: Int = event.y.correct(height)
                if (isAddAction) {
                    (if (isTrend) operateTrend else operateLine)?.end?.x = x
                    (if (isTrend) operateTrend else operateLine)?.end?.y = y
                } else {
                    when (lineMoveType) {
                        LineMoveType.ALL -> {
                            val rect: Rect = TempDrawHelper.getRect(width, height)
                            val minX: Int = min(downLine.start.x, downLine.end.x)
                            val maxX: Int = max(downLine.start.x, downLine.end.x)
                            val minY: Int = min(downLine.start.y, downLine.end.y)
                            val maxY: Int = max(downLine.start.y, downLine.end.y)
                            val biasX: Int = if (x < downX) max(x - downX, rect.left - minX) else min(x - downX, rect.right - maxX)
                            val biasY: Int = if (y < downY) max(y - downY, rect.top - minY) else min(y - downY, rect.bottom - maxY)
                            (if (isTrend) operateTrend else operateLine)?.start?.set(downLine.start.x + biasX, downLine.start.y + biasY)
                            (if (isTrend) operateTrend else operateLine)?.end?.set(downLine.end.x + biasX, downLine.end.y + biasY)
                        }
                        LineMoveType.START -> {
                            (if (isTrend) operateTrend else operateLine)?.start?.x = x
                            (if (isTrend) operateTrend else operateLine)?.start?.y = y
                        }
                        LineMoveType.END -> {
                            (if (isTrend) operateTrend else operateLine)?.end?.x = x
                            (if (isTrend) operateTrend else operateLine)?.end?.y = y
                        }
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val x: Int = event.x.correct(width)
                val y: Int = event.y.correct(height)
                val line: Line = (if (isTrend) operateTrend else operateLine) ?: Line(Point(), Point())
                if ((line.start.x / xScale).toInt() != (line.end.x / xScale).toInt() || (line.start.y / yScale).toInt() != (line.end.y / yScale).toInt()) {
                    if (isAddAction || abs(x - downX) > DELETE_TOLERANCE || abs(y - downY) > DELETE_TOLERANCE) {
                        if (isTrend) {
                            trendLine = line
                            onTrendOperateListener?.invoke(true)
                        } else {
                            synchronized(this) {
                                lineList.add(line)
                            }
                        }
                    }
                }
                operateTrend = null
                operateLine = null
                invalidate()

                if (!isTrend) {
                    onLineListener?.invoke(getSourceLineList())
                }
                return true
            }
            else -> return false
        }
    }

    private fun pollLine(x: Int, y: Int, isTrend: Boolean): Line? {
        if (isTrend) {
            val resultLine = trendLine
            if (isLineConcat(resultLine, x, y)) {
                trendLine = null
                return resultLine
            }
        } else {
            for (i in lineList.size - 1 downTo  0) {
                val line: Line = lineList[i]
                if (isLineConcat(line, x, y)) {
                    return synchronized(this) { lineList.removeAt(i) }
                }
            }
        }
        return null
    }

    private fun isLineConcat(line: Line?, x: Int, y: Int): Boolean {
        if (line == null) {
            return false
        }
        var tempDistance = (line.end.y - line.start.y) * x - (line.end.x - line.start.x) * y + line.end.x * line.start.y - line.start.x * line.end.y
        tempDistance = (tempDistance / sqrt((line.end.y - line.start.y).toDouble().pow(2.0) + (line.end.x - line.start.x).toDouble().pow(2.0))).toInt()
        return abs(tempDistance) < TOUCH_TOLERANCE && x > min(line.start.x, line.end.x) - TOUCH_TOLERANCE && x < max(line.start.x, line.end.x) + TOUCH_TOLERANCE
    }

    protected var operateRect: Rect? = null

    private enum class RectMoveType { ALL, EDGE, CORNER, }
    
    private var rectMoveType = RectMoveType.ALL

    private enum class RectMoveEdge { LEFT, TOP, RIGHT, BOTTOM }
    
    private var rectMoveEdge = RectMoveEdge.LEFT

    private enum class RectMoveCorner { LT, RT, RB, LB }
    
    private var rectMoveCorner = RectMoveCorner.LT

    private val downRect = Rect()

    private fun touchRect(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.correct(width)
                downY = event.y.correct(height)
                val rect: Rect? = pollRect(downX, downY)
                if (rect == null) {
                    isAddAction = true
                    operateRect = Rect(downX, downY, downX, downY)
                    if (rectList.size == maxCount) {
                        synchronized(this) {
                            rectList.removeAt(0)
                        }
                    }
                } else {
                    isAddAction = false
                    operateRect = rect
                    downRect.set(rect)
                    when (downX) {
                        in rect.left - TOUCH_TOLERANCE .. rect.left + TOUCH_TOLERANCE -> {
                            when (downY) {
                                in rect.top - TOUCH_TOLERANCE .. rect.top + TOUCH_TOLERANCE -> {
                                    rectMoveType = RectMoveType.CORNER
                                    rectMoveCorner = RectMoveCorner.LT
                                }
                                in rect.bottom - TOUCH_TOLERANCE .. rect.bottom + TOUCH_TOLERANCE -> {
                                    rectMoveType = RectMoveType.CORNER
                                    rectMoveCorner = RectMoveCorner.LB
                                }
                                else -> {
                                    rectMoveType = RectMoveType.EDGE
                                    rectMoveEdge = RectMoveEdge.LEFT
                                }
                            }
                        }
                        in rect.right - TOUCH_TOLERANCE .. rect.right + TOUCH_TOLERANCE -> {
                            when (downY) {
                                in rect.top - TOUCH_TOLERANCE .. rect.top + TOUCH_TOLERANCE -> {
                                    rectMoveType = RectMoveType.CORNER
                                    rectMoveCorner = RectMoveCorner.RT
                                }
                                in rect.bottom - TOUCH_TOLERANCE .. rect.bottom + TOUCH_TOLERANCE -> {
                                    rectMoveType = RectMoveType.CORNER
                                    rectMoveCorner = RectMoveCorner.RB
                                }
                                else -> {
                                    rectMoveType = RectMoveType.EDGE
                                    rectMoveEdge = RectMoveEdge.RIGHT
                                }
                            }
                        }
                        else -> {
                            when (downY) {
                                in rect.top - TOUCH_TOLERANCE .. rect.top + TOUCH_TOLERANCE -> {
                                    rectMoveType = RectMoveType.EDGE
                                    rectMoveEdge = RectMoveEdge.TOP
                                }
                                in rect.bottom - TOUCH_TOLERANCE .. rect.bottom + TOUCH_TOLERANCE -> {
                                    rectMoveType = RectMoveType.EDGE
                                    rectMoveEdge = RectMoveEdge.BOTTOM
                                }
                                else -> {
                                    rectMoveType = RectMoveType.ALL
                                }
                            }
                        }
                    }
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val x: Int = event.x.correct(width)
                val y: Int = event.y.correct(height)
                if (isAddAction) {
                    operateRect?.set(min(downX, x), min(downY, y), max(downX, x), max(downY, y))
                } else {
                    when (rectMoveType) {
                        RectMoveType.ALL -> {
                            val rect: Rect = TempDrawHelper.getRect(width, height)
                            val biasX: Int = if (x < downX) max(x - downX, rect.left - downRect.left) else min(x - downX, rect.right - downRect.right)
                            val biasY: Int = if (y < downY) max(y - downY, rect.top - downRect.top) else min(y - downY, rect.bottom - downRect.bottom)
                            operateRect?.set(downRect.left + biasX, downRect.top + biasY, downRect.right + biasX, downRect.bottom + biasY)
                        }
                        RectMoveType.EDGE -> when (rectMoveEdge) {
                            RectMoveEdge.LEFT -> {
                                operateRect?.left = min(x, downRect.right)
                                operateRect?.right = max(x, downRect.right)
                            }
                            RectMoveEdge.TOP -> {
                                operateRect?.top = min(y, downRect.bottom)
                                operateRect?.bottom = max(y, downRect.bottom)
                            }
                            RectMoveEdge.RIGHT -> {
                                operateRect?.right = max(x, downRect.left)
                                operateRect?.left = min(x, downRect.left)
                            }
                            RectMoveEdge.BOTTOM -> {
                                operateRect?.bottom = max(y, downRect.top)
                                operateRect?.top = min(y, downRect.top)
                            }
                        }
                        RectMoveType.CORNER -> when (rectMoveCorner) {
                            RectMoveCorner.LT -> {
                                operateRect?.left = min(x, downRect.right)
                                operateRect?.right = max(x, downRect.right)
                                operateRect?.top = min(y, downRect.bottom)
                                operateRect?.bottom = max(y, downRect.bottom)
                            }
                            RectMoveCorner.RT -> {
                                operateRect?.right = max(x, downRect.left)
                                operateRect?.left = min(x, downRect.left)
                                operateRect?.top = min(y, downRect.bottom)
                                operateRect?.bottom = max(y, downRect.bottom)
                            }
                            RectMoveCorner.RB -> {
                                operateRect?.right = max(x, downRect.left)
                                operateRect?.left = min(x, downRect.left)
                                operateRect?.bottom = max(y, downRect.top)
                                operateRect?.top = min(y, downRect.top)
                            }
                            RectMoveCorner.LB -> {
                                operateRect?.left = min(x, downRect.right)
                                operateRect?.right = max(x, downRect.right)
                                operateRect?.bottom = max(y, downRect.top)
                                operateRect?.top = min(y, downRect.top)
                            }
                        }
                    }
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val x: Int = event.x.correct(width)
                val y: Int = event.y.correct(height)
                val rect: Rect = operateRect ?: Rect()
                if ((rect.left / xScale).toInt() != (rect.right / xScale).toInt() &&
                    (rect.top / yScale).toInt() != (rect.bottom / yScale).toInt()) {
                    if (isAddAction || abs(x - downX) > DELETE_TOLERANCE || abs(y - downY) > DELETE_TOLERANCE) {
                        synchronized(this) {
                            rectList.add(rect)
                        }
                    }
                }
                operateRect = null
                invalidate()

                onRectListener?.invoke(getSourceRectList())
                return true
            }
            else -> return false
        }
    }

    private fun pollRect(x: Int, y: Int): Rect? {
        for (i in rectList.size - 1 downTo  0) {
            val rect: Rect = rectList[i]
            if (rect.left - TOUCH_TOLERANCE < x && rect.right + TOUCH_TOLERANCE > x &&
                rect.top - TOUCH_TOLERANCE < y && rect.bottom + TOUCH_TOLERANCE > y) {
                return synchronized(this) { rectList.removeAt(i) }
            }
        }
        return null
    }
}