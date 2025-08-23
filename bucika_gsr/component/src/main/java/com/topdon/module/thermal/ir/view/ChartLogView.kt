package com.topdon.module.thermal.ir.view

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
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
import com.topdon.lib.core.db.entity.ThermalEntity
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.chart.IRMyValueFormatter
import com.topdon.module.thermal.ir.chart.YValueFormatter
import com.topdon.module.thermal.ir.utils.ChartTools
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChartLogView : LineChart {

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
            this.isDragEnabled = true
            this.setDrawGridBackground(false)
            this.description = null
            this.setBackgroundResource(R.color.chart_bg)
            this.setScaleEnabled(false)
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

    fun initEntry(data: ArrayList<ThermalEntity>, type: Int = 1) {
        synchronized(this) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    clearEntity(data.size == 0)
                } catch (e: Exception) {
                    Log.e("chart", "clearEntity error: ${e.message}")
                }
                Log.w("chart", "clearEntity finish")
                if (data.size == 0) {
                    return@launch
                }
                Log.w("chart", "update chart start")
                val lineData: LineData = this@ChartLogView.data
                if (lineData != null) {
                    val startTime = data[0].createTime / 1000 * 1000
                    xAxis.valueFormatter = IRMyValueFormatter(startTime = startTime, type = type)
                    XLog.w("chart init startTime:$startTime")
                    when (data[0].type) {
                        "point" -> {
                            var set = lineData.getDataSetByIndex(0)
                            if (set == null) {
                                set = createSet(0, "point temp")
                                lineData.addDataSet(set)
                            }
                            Log.w("123", "一条曲线")
                            data.forEach {
                                val x = ChartTools.getChartX(
                                    x = it.createTime,
                                    startTime = startTime,
                                    type = type
                                ).toFloat()
                                val entity = Entry(x, it.thermal)
                                entity.data = it
                                set.addEntry(entity)
                            }
                            XLog.w("DataSet:${set.entryCount}")
                        }
                        "line" -> {
                            var maxDataSet = lineData.getDataSetByIndex(0)
                            if (maxDataSet == null) {
                                maxDataSet = createSet(0, "line max temp")

                            }

                            var minDataSet = lineData.getDataSetByIndex(1)
                            if (minDataSet == null) {
                                minDataSet = createSet(1, "line min temp")

                            }
                            Log.w("123", "两条曲线")
                            data.forEach {
                                val x = ChartTools.getChartX(
                                    x = it.createTime,
                                    startTime = startTime,
                                    type = type
                                ).toFloat()
                                val entity = Entry(x, it.thermalMax)
                                entity.data = it
                                maxDataSet.addEntry(entity)
                                val entityMin = Entry(x, it.thermalMin)
                                entityMin.data = it
                                minDataSet.addEntry(entityMin)
                            }
                            lineData.addDataSet(maxDataSet)
                            lineData.addDataSet(minDataSet)
                            XLog.w("DataSet:${maxDataSet.entryCount}")
                        }
                        else -> {
                            var maxTempDataSet = lineData.getDataSetByIndex(0)
                            if (maxTempDataSet == null) {
                                maxTempDataSet = createSet(0, "fence max temp")
                                lineData.addDataSet(maxTempDataSet)
                            }
                            var centerTempDataSet = lineData.getDataSetByIndex(1)
                            if (centerTempDataSet == null) {
                                centerTempDataSet = createSet(1, "fence min temp")
                                lineData.addDataSet(centerTempDataSet)
                            }
                            Log.w("123", "三条曲线")
                            data.forEach {
                                val x = ChartTools.getChartX(
                                    x = it.createTime,
                                    startTime = startTime,
                                    type = type
                                ).toFloat()
                                val entityMax = Entry(x, it.thermalMax)
                                entityMax.data = it
                                maxTempDataSet.addEntry(entityMax)
                                val entity = Entry(x, it.thermalMin)
                                entity.data = it
                                centerTempDataSet.addEntry(entity)
                            }
                            XLog.w("DataSet:${centerTempDataSet.entryCount}")
                        }
                    }
                    lineData.notifyDataChanged()
                    notifyDataSetChanged()
                    moveViewToX(xChartMin)
                    setVisibleXRangeMinimum(ChartTools.getMinimum(type = type) / 2)
                    setVisibleXRangeMaximum(ChartTools.getMaximum(type = type))
                    zoom(1f, 1f, xChartMin, 0f)
                    ChartTools.setX(this@ChartLogView, type)
                }
                Log.w("chart", "update chart finish")
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

    private fun clearEntity(isEmpty: Boolean) {
        initChart()
        if (isEmpty) {
            clear()
        } else {
            clearValues()
        }
    }

}