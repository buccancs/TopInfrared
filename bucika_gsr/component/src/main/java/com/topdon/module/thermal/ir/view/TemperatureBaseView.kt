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

/**
 * TC007、2D 编辑 点线面温度图层公共逻辑封装.
 *
 * Created by LCG on 2024/5/7.
 */
abstract class TemperatureBaseView : View {

    companion object {
        /**
         * 支持点线面的默认最大数量.
         */
        private const val DEFAULT_MAX_COUNT = 3

        /**
         * 选中操作灵敏度，当 Touch Down 坐标与点线面坐标偏差在该值范围内，视为选中，单位 px.
         */
        private val TOUCH_TOLERANCE = SizeUtils.dp2px(8f)
        /**
         * 删除操作灵敏度，当 Touch UP 与 Touch Down 坐标偏差在该值范围内，视为删除，单位 px.
         */
        private val DELETE_TOLERANCE = SizeUtils.dp2px(2f)
    }


    /**
     * 操作模式，点、线、面、全图、清除.
     */
    enum class Mode {
        POINT,
        LINE,
        RECT,
        TREND,
        FULL,
        CLEAR,
    }

    /**
     * 当前是否显示了全图.
     */
    @Volatile
    var isShowFull: Boolean = true
        set(value) {
            field = value
            if (value && mode == Mode.CLEAR) {
                mode = Mode.FULL
            }
            invalidate()
        }
    /**
     * 当前操作模式：点、线、面、全图、清除。
     */
    @Volatile
    open var mode = Mode.FULL
        set(value) {
            field = value
            if (value == Mode.FULL) {//全图
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


    /**
     * 温度值文字大小，单位 px.
     */
    var tempTextSize: Int
        get() = helper.textSize
        set(value) {
            helper.textSize = value
            invalidate()
        }
    /**
     * 温度值文字、点线面名称文字 颜色值.
     */
    var textColor: Int
        @ColorInt get() = helper.textColor
        set(@ColorInt value) {
            helper.textColor = value
            invalidate()
        }


    /**
     * 由于 Touch 事件导致的点添加、移除、变更事件监听，坐标为通过 [setImageSize] 设置的坐标系
     */
    var onPointListener: ((pointList: List<Point>) -> Unit)? = null
    /**
     * 由于 Touch 事件导致的线添加、移除、变更事件监听，坐标为通过 [setImageSize] 设置的坐标系
     */
    var onLineListener: ((lineList: List<Point>) -> Unit)? = null
    /**
     * 由于 Touch 事件导致的面添加、移除、变更事件监听，坐标为通过 [setImageSize] 设置的坐标系
     */
    var onRectListener: ((rectList: List<Rect>) -> Unit)? = null
    /**
     * 由于 Touch 事件导致的趋势图添加或趋势图移除事件监听.
     *
     * 也就是说：将 [mode] 设置为 [Mode.CLEAR] 不会触发该回调.
     */
    var onTrendOperateListener: ((isAdd: Boolean) -> Unit)? = null



    /**
     * 以 View 尺寸为坐标系，当前已添加的点列表.
     */
    protected val pointList = ArrayList<Point>()
    /**
     * 以 View 尺寸为坐标系，当前已添加的线列表.
     */
    protected val lineList = ArrayList<Line>()
    /**
     * 以 View 尺寸为坐标系，当前已添加的面列表.
     */
    protected val rectList = ArrayList<Rect>()
    /**
     * 以 View 尺寸为坐标系，当前已添加的趋势图直线.
     */
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


    /**
     * 支持点线面的最大数量，默认3.
     */
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




    /* ******************************************** Draw ******************************************** */
    /**
     * 以 View 尺寸为坐标系，在 (x,y) 画一个十字.
     *
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     * @param point 以 View 尺寸为坐标系的点
     */
    protected fun drawPoint(canvas: Canvas, point: Point) {
        helper.drawPoint(canvas, point.x, point.y)
    }

    /**
     * 以 View 尺寸为坐标系，连接 (startX, startY)、(stopX, stopY) 两点绘制一条线段.
     */
    protected fun drawLine(canvas: Canvas, line: Line) {
        // 由于线段与实心点的的绘制是分开的，线段使用当前 View 坐标，而实心点使用温度(192x256)坐标转换为 View 坐标
        // 故而这里需要把当前的坐标，尽量贴近温度坐标的整数倍，否则会出现实心圆偏离直线太远的情况
        val startX: Int = ((line.start.x / xScale).toInt() * xScale).toInt()
        val startY: Int = ((line.start.y / yScale).toInt() * yScale).toInt()
        val stopX: Int = ((line.end.x / xScale).toInt() * xScale).toInt()
        val stopY: Int = ((line.end.y / yScale).toInt() * yScale).toInt()
        helper.drawLine(canvas, startX, startY, stopX, stopY)
    }

    /**
     * 以 View 尺寸为坐标系，按指定范围绘制一个矩形.
     */
    protected fun drawRect(canvas: Canvas, rect: Rect) {
        val left: Int = ((rect.left / xScale).toInt() * xScale).toInt()
        val top: Int = ((rect.top / yScale).toInt() * yScale).toInt()
        val right: Int = ((rect.right / xScale).toInt() * xScale).toInt()
        val bottom: Int = ((rect.bottom / yScale).toInt() * yScale).toInt()
        helper.drawRect(canvas, left, top, right, bottom)
    }



    /**
     * 以 View 尺寸为坐标系，在 (x,y) 画一个实心圆。
     *
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     * @param isMax true-最高温红色 false-最低温蓝色
     */
    protected fun drawCircle(canvas: Canvas, x: Int, y: Int, isMax: Boolean) {
        helper.drawCircle(canvas, x, y, isMax)
    }

    /**
     * 以 View 尺寸为坐标系，指定的 (x,y) 坐标为实心圆圆心，以该实心圆为基准绘制指定文字。
     * 若空间允许则放置在实心圆圆心右上方，否则根据实际情况放置在下方、左方或左下方.
     *
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     * @param x 实心圆圆心的 View 尺寸坐标
     */
    protected fun drawTempText(canvas: Canvas, x: Int, y: Int, temp: Float) {
        helper.drawTempText(canvas, UnitTools.showC(temp), width, x, y)
    }

    /**
     * 以 View 尺寸为坐标系，以指定线段为基准绘制趋势图的 "A"、"B" 文字。
     *
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     */
    protected fun drawTrendText(canvas: Canvas, line: Line) {
        helper.drawTrendText(canvas, width, height, line.start.x, line.start.y, line.end.x, line.end.y)
    }


    /**
     * 以 View 尺寸为坐标系，指定的 (x,y) 坐标为实心圆圆心，以该实心圆为基准绘制指定点名称文字。
     * 若空间允许则放置在实心圆圆心正下方，否则放正上方.
     */
    protected fun drawPointName(canvas: Canvas, name: String, point: Point) {
        // 由于十字与实心点的的绘制是分开的，十字使用当前 View 坐标，而实心点使用温度(192x256)坐标
        // 故而这里需要把当前的坐标，转换为温度坐标的整数倍，否则会出现中心对不上的情况
        val x = ((point.x / xScale).toInt() * xScale).toInt()
        val y = ((point.y / yScale).toInt() * yScale).toInt()
        helper.drawPointName(canvas, name, width, height, x, y)
    }

    /**
     * 以 View 尺寸为坐标系，指定的 线段或矩形 坐标为范围，
     * 以该范围为基准绘制指定线名称文字，放置于范围中心。
     */
    protected fun drawLineName(canvas: Canvas, name: String, line: Line) {
        val startX = ((line.start.x / xScale).toInt() * xScale).toInt()
        val startY = ((line.start.y / yScale).toInt() * yScale).toInt()
        val stopX = ((line.end.x / xScale).toInt() * xScale).toInt()
        val stopY = ((line.end.y / yScale).toInt() * yScale).toInt()
        helper.drawPointRectName(canvas, name, width, height, startX, startY, stopX, stopY)
    }

    /**
     * 以 View 尺寸为坐标系，指定的 线段或矩形 坐标为范围，
     * 以该范围为基准绘制指定线名称文字，放置于范围中心。
     */
    protected fun drawRectName(canvas: Canvas, name: String, rect: Rect) {
        val left: Int = ((rect.left / xScale).toInt() * xScale).toInt()
        val top: Int = ((rect.top / yScale).toInt() * yScale).toInt()
        val right: Int = ((rect.right / xScale).toInt() * xScale).toInt()
        val bottom: Int = ((rect.bottom / yScale).toInt() * yScale).toInt()
        helper.drawPointRectName(canvas, name, width, height, left, top, right, bottom)
    }



    /* **************************************** Touch **************************************** */

    private var downX = 0
    private var downY = 0

    /**
     * 是否为添加 点线面 模式。
     *
     * true-添加一个新点线面 false-移动一个已有点线面
     */
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


    /* **************************************** 点 **************************************** */

    /**
     * Touch 时当前正在操作（添加、移动）的点.
     */
    protected var operatePoint: Point? = null

    private fun touchPoint(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.correctPoint(width)
                downY = event.y.correctPoint(height)
                val point: Point? = pollPoint(downX, downY)
                isAddAction = point == null
                operatePoint = point ?: Point(downX, downY)
                if (point == null && pointList.size == maxCount) {//新增时已达最大数量
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


    /* **************************************** 线 **************************************** */

    /**
     * Touch 时当前正在操作（添加、移动）的线.
     */
    protected var operateLine: Line? = null
    /**
     * Touch 时当前正在操作（添加、移动）的趋势图线.
     */
    protected var operateTrend: Line? = null

    private enum class LineMoveType { ALL, START, END, }
    /**
     * 线移动方式：整体移动、仅变更头、仅变更尾。
     */
    private var lineMoveType = LineMoveType.ALL

    /**
     * 仅整体移动线时，保存 DOWN 状态下的线初始坐标，用于计算移动.
     */
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
                        LineMoveType.ALL -> {//整体移动
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
                    //只有画出来的结果不是一个点才生效
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

    /**
     * 判断指定坐标 (x, y) 是否视为指定 Line 的选中.
     */
    private fun isLineConcat(line: Line?, x: Int, y: Int): Boolean {
        if (line == null) {
            return false
        }
        var tempDistance = (line.end.y - line.start.y) * x - (line.end.x - line.start.x) * y + line.end.x * line.start.y - line.start.x * line.end.y
        tempDistance = (tempDistance / sqrt((line.end.y - line.start.y).toDouble().pow(2.0) + (line.end.x - line.start.x).toDouble().pow(2.0))).toInt()
        return abs(tempDistance) < TOUCH_TOLERANCE && x > min(line.start.x, line.end.x) - TOUCH_TOLERANCE && x < max(line.start.x, line.end.x) + TOUCH_TOLERANCE
    }



    /* **************************************** 面 **************************************** */

    /**
     * Touch 时当前正在操作（添加、移动）的面.
     */
    protected var operateRect: Rect? = null


    private enum class RectMoveType { ALL, EDGE, CORNER, }
    /**
     * 面移动方式：点击面内部-整体移动、点击面4条边-边移动、点击面4个角-角移动。
     */
    private var rectMoveType = RectMoveType.ALL


    private enum class RectMoveEdge { LEFT, TOP, RIGHT, BOTTOM }
    /**
     * 仅边移动模式时，移动的是哪条边.
     */
    private var rectMoveEdge = RectMoveEdge.LEFT


    private enum class RectMoveCorner { LT, RT, RB, LB }
    /**
     * 仅角移动模式时，移动的是哪个角.
     */
    private var rectMoveCorner = RectMoveCorner.LT

    /**
     * 移动面时，保存 DOWN 状态下的面初始坐标，用于计算移动.
     */
    private val downRect = Rect()

    private fun touchRect(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.correct(width)
                downY = event.y.correct(height)
                val rect: Rect? = pollRect(downX, downY)
                if (rect == null) {//插入
                    isAddAction = true
                    operateRect = Rect(downX, downY, downX, downY)
                    if (rectList.size == maxCount) {
                        synchronized(this) {
                            rectList.removeAt(0)
                        }
                    }
                } else {//选取 - 删除
                    isAddAction = false
                    operateRect = rect
                    downRect.set(rect)
                    when (downX) {
                        in rect.left - TOUCH_TOLERANCE .. rect.left + TOUCH_TOLERANCE -> {//选中最左那条边
                            when (downY) {
                                in rect.top - TOUCH_TOLERANCE .. rect.top + TOUCH_TOLERANCE -> {//选中顶边
                                    rectMoveType = RectMoveType.CORNER
                                    rectMoveCorner = RectMoveCorner.LT
                                }
                                in rect.bottom - TOUCH_TOLERANCE .. rect.bottom + TOUCH_TOLERANCE -> {//选中底边
                                    rectMoveType = RectMoveType.CORNER
                                    rectMoveCorner = RectMoveCorner.LB
                                }
                                else -> {
                                    rectMoveType = RectMoveType.EDGE
                                    rectMoveEdge = RectMoveEdge.LEFT
                                }
                            }
                        }
                        in rect.right - TOUCH_TOLERANCE .. rect.right + TOUCH_TOLERANCE -> {//选中最右那条边
                            when (downY) {
                                in rect.top - TOUCH_TOLERANCE .. rect.top + TOUCH_TOLERANCE -> {//选中顶边
                                    rectMoveType = RectMoveType.CORNER
                                    rectMoveCorner = RectMoveCorner.RT
                                }
                                in rect.bottom - TOUCH_TOLERANCE .. rect.bottom + TOUCH_TOLERANCE -> {//选中底边
                                    rectMoveType = RectMoveType.CORNER
                                    rectMoveCorner = RectMoveCorner.RB
                                }
                                else -> {
                                    rectMoveType = RectMoveType.EDGE
                                    rectMoveEdge = RectMoveEdge.RIGHT
                                }
                            }
                        }
                        else -> {//左右都没选中
                            when (downY) {
                                in rect.top - TOUCH_TOLERANCE .. rect.top + TOUCH_TOLERANCE -> {//选中顶边
                                    rectMoveType = RectMoveType.EDGE
                                    rectMoveEdge = RectMoveEdge.TOP
                                }
                                in rect.bottom - TOUCH_TOLERANCE .. rect.bottom + TOUCH_TOLERANCE -> {//选中底边
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
                        RectMoveType.ALL -> {//整体移动
                            val rect: Rect = TempDrawHelper.getRect(width, height)
                            val biasX: Int = if (x < downX) max(x - downX, rect.left - downRect.left) else min(x - downX, rect.right - downRect.right)
                            val biasY: Int = if (y < downY) max(y - downY, rect.top - downRect.top) else min(y - downY, rect.bottom - downRect.bottom)
                            operateRect?.set(downRect.left + biasX, downRect.top + biasY, downRect.right + biasX, downRect.bottom + biasY)
                        }
                        RectMoveType.EDGE -> when (rectMoveEdge) {
                            RectMoveEdge.LEFT -> {//移动左边
                                operateRect?.left = min(x, downRect.right)
                                operateRect?.right = max(x, downRect.right)
                            }
                            RectMoveEdge.TOP -> {//移动上边
                                operateRect?.top = min(y, downRect.bottom)
                                operateRect?.bottom = max(y, downRect.bottom)
                            }
                            RectMoveEdge.RIGHT -> {//移动右边
                                operateRect?.right = max(x, downRect.left)
                                operateRect?.left = min(x, downRect.left)
                            }
                            RectMoveEdge.BOTTOM -> {//移动下边
                                operateRect?.bottom = max(y, downRect.top)
                                operateRect?.top = min(y, downRect.top)
                            }
                        }
                        RectMoveType.CORNER -> when (rectMoveCorner) {
                            RectMoveCorner.LT -> {//移动左上角
                                operateRect?.left = min(x, downRect.right)
                                operateRect?.right = max(x, downRect.right)
                                operateRect?.top = min(y, downRect.bottom)
                                operateRect?.bottom = max(y, downRect.bottom)
                            }
                            RectMoveCorner.RT -> {//移动右上角
                                operateRect?.right = max(x, downRect.left)
                                operateRect?.left = min(x, downRect.left)
                                operateRect?.top = min(y, downRect.bottom)
                                operateRect?.bottom = max(y, downRect.bottom)
                            }
                            RectMoveCorner.RB -> {//移动右下角
                                operateRect?.right = max(x, downRect.left)
                                operateRect?.left = min(x, downRect.left)
                                operateRect?.bottom = max(y, downRect.top)
                                operateRect?.top = min(y, downRect.top)
                            }
                            RectMoveCorner.LB -> {//移动左下角
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
                    //画出来的结果不是一条线才生效
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