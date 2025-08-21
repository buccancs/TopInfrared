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

    //MPChart
    private fun initChart() {
        synchronized(this) {
            this.setTouchEnabled(true)
            this.isDragEnabled = true
            this.setDrawGridBackground(false)
            this.description = null//图标描述文本
            this.setBackgroundResource(R.color.chart_bg)
            this.setScaleEnabled(false)//缩放
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
                    val startTime = data[0].createTime / 1000 * 1000  //毫秒 (毫秒归零,否则有可能x对应不上时间)
                    xAxis.valueFormatter = IRMyValueFormatter(startTime = startTime, type = type)
                    XLog.w("chart init startTime:$startTime")
//                    data[0].type = "default"
                    when (data[0].type) {
                        "point" -> {
                            var set = lineData.getDataSetByIndex(0)//读取x为0的坐标点
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
                            var maxDataSet = lineData.getDataSetByIndex(0)//读取x为0的坐标点
                            if (maxDataSet == null) {
                                maxDataSet = createSet(0, "line max temp")

                            }

                            var minDataSet = lineData.getDataSetByIndex(1)//读取x为0的坐标点
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
//                                Log.w("123", "x: $x")
                                //max
                                val entity = Entry(x, it.thermalMax)
                                entity.data = it
                                maxDataSet.addEntry(entity)
                                //min
                                val entityMin = Entry(x, it.thermalMin)
                                entityMin.data = it
                                minDataSet.addEntry(entityMin)
                            }
                            lineData.addDataSet(maxDataSet)
                            lineData.addDataSet(minDataSet)
                            XLog.w("DataSet:${maxDataSet.entryCount}")
                        }
                        else -> {
                            //max
                            var maxTempDataSet = lineData.getDataSetByIndex(0)//读取x为0的坐标点
                            if (maxTempDataSet == null) {
                                maxTempDataSet = createSet(0, "fence max temp")
                                lineData.addDataSet(maxTempDataSet)
                            }
                            //center
                            var centerTempDataSet = lineData.getDataSetByIndex(1)//读取x为0的坐标点
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
                                //max
                                val entityMax = Entry(x, it.thermalMax)
                                entityMax.data = it
                                maxTempDataSet.addEntry(entityMax)
                                //min
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
                    setVisibleXRangeMinimum(ChartTools.getMinimum(type = type) / 2)//设置显示X轴区间大小
                    setVisibleXRangeMaximum(ChartTools.getMaximum(type = type))//设置显示X轴区间大小
                    zoom(1f, 1f, xChartMin, 0f)//默认无缩放，全部显示
                    ChartTools.setX(this@ChartLogView, type)
//                    ChartTools.setY(this@ChartTempView)
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

    private fun clearEntity(isEmpty: Boolean) {
        initChart()
        if (isEmpty) {
            clear() //无数据显示
        } else {
            clearValues()
        }
    }

}