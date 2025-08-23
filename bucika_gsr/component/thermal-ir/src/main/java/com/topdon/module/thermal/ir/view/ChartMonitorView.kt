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

    private fun initChart() {
        synchronized(this) {
            this.setTouchEnabled(true)
            this.onChartGestureListener = this
            this.isDragEnabled = true
            this.setDrawGridBackground(false)
            this.description = null
            this.setBackgroundResource(R.color.chart_bg)
            this.setScaleEnabled(true)
            this.setPinchZoom(false)
            this.isDoubleTapToZoomEnabled = false
            this.isScaleYEnabled = false
            this.isScaleXEnabled = true
            this.setExtraOffsets(
                0f,
                0f,
                SizeUtils.dp2px(8f).toFloat(),
                SizeUtils.dp2px(4f).toFloat()
            )
            setNoDataText(context.getString(R.string.http_code998))
            setNoDataTextColor(ContextCompat.getColor(context, R.color.chart_text))
            val mv = MyMarkerView(context, R.layout.marker_lay)
            mv.chartView = this
            marker = mv
            val data = LineData()
            data.setValueTextColor(textColor)
            this.data = data
            val l = this.legend
            l.form = Legend.LegendForm.CIRCLE
            l.textColor = textColor
            l.isEnabled = false
            val xAxis = this.xAxis
            xAxis.textColor = textColor
            xAxis.setDrawGridLines(false)
            xAxis.gridColor = axisChartColors
            xAxis.axisLineColor = 0x00000000
            xAxis.setAvoidFirstLastClipping(true)
            xAxis.isEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.isGranularityEnabled = true
            xAxis.textSize = 8f
            val leftAxis = this.axisLeft
            leftAxis.textColor = textColor
            leftAxis.axisLineColor = 0x00000000
            leftAxis.setDrawGridLines(true)
            leftAxis.gridColor = axisChartColors
            leftAxis.gridLineWidth = 1.5f
            leftAxis.setLabelCount(6, true)
            leftAxis.valueFormatter = YValueFormatter()
            leftAxis.textSize = 8f

            this.axisRight.isEnabled = false
        }
    }

    private var startTime = 0L

    fun addPointToChart(bean: ThermalEntity, timeType: Int = 1, selectType: Int = 1) {
        synchronized(this) {
            try {
                if (bean.createTime == 0L) {
                    Log.w("123", "createTime = 0L, bean:${bean}")
                    return
                }
                val lineData: LineData = this.data
                var volDataSet = lineData.getDataSetByIndex(0)
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
                        if (volDataSet == null) {
                            volDataSet = createSet(0, "line max temp")
                            lineData.addDataSet(volDataSet)
                            Log.w("123", "volDataSet.entryCount:${volDataSet.entryCount}")
                        }
                        val entity = Entry(x, bean.thermalMax)
                        entity.data = bean
                        volDataSet.addEntry(entity)

                        var secondDataSet = lineData.getDataSetByIndex(1)
                        if (secondDataSet == null) {
                            secondDataSet = createSet(1, "line min temp")
                            lineData.addDataSet(secondDataSet)
                        }
                        val secondEntity = Entry(x, bean.thermalMin)
                        secondEntity.data = bean
                        secondDataSet.addEntry(secondEntity)
                    }
                    else -> {
                        if (volDataSet == null) {
                            volDataSet = createSet(0, "fence max temp")
                            lineData.addDataSet(volDataSet)
                        }
                        val entity = Entry(x, bean.thermalMax)
                        entity.data = bean
                        volDataSet.addEntry(entity)

                        var secondDataSet = lineData.getDataSetByIndex(1)
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
                setVisibleXRangeMinimum(ChartTools.getMinimum(type = timeType) / 2)
                setVisibleXRangeMaximum(ChartTools.getMaximum(type = timeType))
                ChartTools.setX(this, timeType)
                if ((highestVisibleX + ChartTools.getMinimum(timeType) / 2f) > xChartMax) {
                    moveViewToX(xChartMax)
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

    private fun createSet(index: Int, label: String): LineDataSet {
        val set = LineDataSet(null, label)
        set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
        set.setDrawFilled(false)
        set.fillDrawable = ContextCompat.getDrawable(context, bgChartColors[index])
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.color = ContextCompat.getColor(context, lineChartColors[index])
        set.circleHoleColor = ContextCompat.getColor(context, linePointColors[index])
        set.setCircleColor(ContextCompat.getColor(context, lineChartColors[index]))
        set.valueTextColor = Color.WHITE
        set.lineWidth = 2f
        set.circleRadius = 1f
        set.fillAlpha = 200
        set.valueTextSize = 10f
        set.setDrawValues(false)
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
        highlightValue(null)
    }

    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {

    }

}