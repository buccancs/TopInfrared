package com.topdon.module.thermal.ir.view

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.elvishew.xlog.XLog
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.topdon.lib.core.bean.tools.ThermalBean
import com.topdon.lib.core.db.entity.ThermalEntity
import com.topdon.lib.core.tools.TimeTool
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.chart.IRMyValueFormatter
import com.topdon.module.thermal.ir.chart.YValueFormatter
import com.topdon.module.thermal.ir.utils.ChartTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChartMonitorView : LineChart, OnChartGestureListener {

    private val mHandler by lazy { Handler(Looper.getMainLooper()) }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initChart()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mHandler.removeCallbacksAndMessages(null)
    }

    private val textColor by lazy { ContextCompat.getColor(context, R.color.chart_text) }
    private val axisChartColors by lazy { ContextCompat.getColor(context, R.color.chart_axis) }
    private val axisLine by lazy { ContextCompat.getColor(context, R.color.circle_white) }

    //MPChart
    private fun initChart() {
        synchronized(this) {
            this.setTouchEnabled(true)
            this.onChartGestureListener = this
            this.isDragEnabled = true
            this.setDrawGridBackground(false)
            this.description = null//图标描述文本
            this.setBackgroundResource(R.color.chart_bg)
            this.setScaleEnabled(true)//缩放
            this.setPinchZoom(false)//禁用后，可以分别在x轴和y轴上进行缩放
            this.isDoubleTapToZoomEnabled = false//双击不可缩放
            this.isScaleYEnabled = false//禁止Y轴缩放
            this.isScaleXEnabled = true//禁止X轴缩放
            this.setExtraOffsets(
                0f,
                0f,
                SizeUtils.dp2px(8f).toFloat(),
                SizeUtils.dp2px(4f).toFloat()
            )//图表区域偏移
            setNoDataText(context.getString(R.string.http_code998))
            setNoDataTextColor(ContextCompat.getColor(context, R.color.chart_text))
            val mv = MyMarkerView(context, R.layout.marker_lay)
            mv.chartView = this
            marker = mv//设置点击坐标显示提示框
            val data = LineData()
            data.setValueTextColor(textColor)
            this.data = data
            val l = this.legend
            l.form = Legend.LegendForm.CIRCLE
            l.textColor = textColor
            l.isEnabled = false//隐藏曲线标签
            //x轴
            val xAxis = this.xAxis
            xAxis.textColor = textColor
            xAxis.setDrawGridLines(false)//竖向格线
            xAxis.gridColor = axisChartColors //x轴网格颜色
            xAxis.axisLineColor = 0x00000000 //x轴颜色
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.isEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true//重复值不显示
            xAxis.textSize = 8f
            //y轴
            val leftAxis = this.axisLeft
            leftAxis.textColor = textColor //y轴文本颜色
            leftAxis.axisLineColor = 0x00000000 //y轴颜色
            leftAxis.setDrawGridLines(true)//横向格线
            leftAxis.gridColor = axisChartColors //y轴网格颜色
            leftAxis.gridLineWidth = 1.5f
            leftAxis.setLabelCount(6, true)
            leftAxis.valueFormatter = YValueFormatter()//设置小数点一位
            leftAxis.textSize = 8f

            this.axisRight.isEnabled = false
        }
    }

    private var startTime = 0L

    /**
     * 秒更新图表数据
     * @param timeType 时分秒
     *
     */
    fun addPointToChart(bean: ThermalEntity, timeType: Int = 1, selectType: Int = 1) {
        synchronized(this) {
            try {
                if (bean.createTime == 0L) {
                    Log.w("123", "createTime = 0L, bean:${bean}")
                    return
                }
                val lineData: LineData = this.data
                var volDataSet = lineData.getDataSetByIndex(0) //读取x为0的坐标点
                if (volDataSet == null) {
                    startTime = bean.createTime
                    xAxis.valueFormatter =
                        IRMyValueFormatter(startTime = startTime, type = timeType)
                }
                val x = ChartTools.getChartX(
                    x = bean.createTime,
                    startTime = startTime,
                    type = timeType
                ).toFloat()
                when (selectType) {
                    1 -> {
                        if (volDataSet == null) {
                            volDataSet = createSet(0, "point temp")
                            lineData.addDataSet(volDataSet)
                            Log.w("123", "volDataSet.entryCount:${volDataSet.entryCount}")
                        }
                        val entity = Entry(x, bean.thermal)
                        entity.data = bean
                        volDataSet.addEntry(entity)
                        Log.w("123", "添加一个数据:$entity")
                    }
                    2 -> {
                        //第一条线
                        if (volDataSet == null) {
                            volDataSet = createSet(0, "line max temp")
                            lineData.addDataSet(volDataSet)
                            Log.w("123", "volDataSet.entryCount:${volDataSet.entryCount}")
                        }
                        val entity = Entry(x, bean.thermalMax)
                        entity.data = bean
                        volDataSet.addEntry(entity)

                        //第二条线
                        var secondDataSet = lineData.getDataSetByIndex(1) //读取x为0的坐标点
                        if (secondDataSet == null) {
                            secondDataSet = createSet(1, "line min temp")
                            lineData.addDataSet(secondDataSet)
                        }
                        val secondEntity = Entry(x, bean.thermalMin)
                        secondEntity.data = bean
                        secondDataSet.addEntry(secondEntity)
                    }
                    else -> {
                        //第一条线
                        if (volDataSet == null) {
                            volDataSet = createSet(0, "fence max temp")
                            lineData.addDataSet(volDataSet)
                        }
                        val entity = Entry(x, bean.thermalMax)
                        entity.data = bean
                        volDataSet.addEntry(entity)

                        //第二条线
                        var secondDataSet = lineData.getDataSetByIndex(1) //读取x为0的坐标点
                        if (secondDataSet == null) {
                            secondDataSet = createSet(1, "fence min temp")
                            lineData.addDataSet(secondDataSet)
                        }
                        val secondEntity = Entry(x, bean.thermalMin)
                        secondEntity.data = bean
                        secondDataSet.addEntry(secondEntity)
                    }
                }

                lineData.notifyDataChanged()
                notifyDataSetChanged()
                setVisibleXRangeMinimum(ChartTools.getMinimum(type = timeType) / 2)//设置显示X轴区间大小
                setVisibleXRangeMaximum(ChartTools.getMaximum(type = timeType))//设置显示X轴区间大小
                ChartTools.setX(this, timeType)
//                ChartTools.setY(this)
                //结尾点出现在界面才移动最新数据
                if ((highestVisibleX + ChartTools.getMinimum(timeType) / 2f) > xChartMax) {
                    moveViewToX(xChartMax)//移动到最右端
                }
                if (volDataSet.entryCount == 10) {
                    zoom(100f, 1f, xChartMax, 0f)
                }
                return@synchronized
            } catch (e: Exception) {
                Log.e("123", "添加数据时异常:${e.message}")
                return@synchronized
            }
        }
    }

    private val bgChartColors = intArrayOf(
        R.drawable.bg_chart_fill,
        R.drawable.bg_chart_fill2,
        R.drawable.bg_chart_fill3
    )
    private val lineChartColors = intArrayOf(
        R.color.chart_line_max,
        R.color.chart_line_min,
        R.color.chart_line_center
    )
    private val linePointColors = intArrayOf(
        R.color.chart_point_max,
        R.color.chart_point_min,
        R.color.chart_point_center
    )

    /**
     * 曲线样式
     */
    private fun createSet(index: Int, label: String): LineDataSet {
        val set = LineDataSet(null, label)
        set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        set.setDrawFilled(false)
        set.fillDrawable = ContextCompat.getDrawable(context, bgChartColors[index])//设置填充颜色渐变
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.color = ContextCompat.getColor(context, lineChartColors[index])//曲线颜色
        set.circleHoleColor = ContextCompat.getColor(context, linePointColors[index])//坐标圆心颜色
        set.setCircleColor(ContextCompat.getColor(context, lineChartColors[index]))//坐标颜色
        set.valueTextColor = Color.WHITE
        set.lineWidth = 2f
        set.circleRadius = 1f//坐标点半径
        set.fillAlpha = 200
        set.valueTextSize = 10f
        set.setDrawValues(false)//设置是否显示坐标值文本
        return set
    }

    override fun onChartGestureStart(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {

    }

    override fun onChartGestureEnd(
        me: MotionEvent?,
        lastPerformedGesture: ChartTouchListener.ChartGesture?
    ) {

    }

    override fun onChartLongPressed(me: MotionEvent?) {

    }

    override fun onChartDoubleTapped(me: MotionEvent?) {

    }

    override fun onChartSingleTapped(me: MotionEvent?) {

    }

    override fun onChartFling(
        me1: MotionEvent?,
        me2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ) {

    }

    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
        //缩放时关闭
        highlightValue(null)
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {

    }

}