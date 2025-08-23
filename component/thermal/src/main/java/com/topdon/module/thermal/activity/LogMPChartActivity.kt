package com.topdon.module.thermal.activity

import android.graphics.Color
import android.util.Log
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.SizeUtils
import com.elvishew.xlog.XLog
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.db.entity.ThermalEntity
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.ToastTools
import com.topdon.module.thermal.R
import com.topdon.module.thermal.adapter.SettingTimeAdapter
import com.topdon.module.thermal.chart.MyValueFormatter
import com.topdon.module.thermal.view.MyMarkerView
import com.topdon.module.thermal.viewmodel.LogViewModel
import kotlinx.android.synthetic.main.activity_log_mp_chart.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Route(path = RouterConfig.THERMAL_LOG_MP_CHART)
class LogMPChartActivity : BaseActivity(), OnChartValueSelectedListener {

    private val viewModel: LogViewModel by viewModels()

    private val adapter: SettingTimeAdapter by lazy { SettingTimeAdapter(this) }

    private lateinit var chart: LineChart
    private var selectType = 1

    override fun initContentView() = R.layout.activity_log_mp_chart

    override fun initView() {
        setTitleText(R.string.app_record)
        chart = log_chart_time_chart
        log_chart_time_recycler.layoutManager = GridLayoutManager(this, 4)
        log_chart_time_recycler.adapter = adapter
        adapter.listener = object : SettingTimeAdapter.OnItemClickListener {
            override fun onClick(index: Int, time: Int) {
                chart.highlightValue(null)
                selectType = index + 1
                queryLog()
            }
        }
        viewModel.resultLiveData.observe(this) {
            dismissLoading()
            try {
                initEntry(it.dataList)
            } catch (e: Exception) {
                XLog.e("刷新图表异常:${e.message}")
                ToastTools.showShort("图表异常，请重新加载")
            }
        }
        clearEntity(true)
    }

    override fun initData() {
        queryLog()
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun queryLog() {
        showLoading()
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.queryLogVolsByStartTime(
                type = SharedManager.getSelectFenceType(),
                selectTimeType = selectType
            )
        }
    }

    private fun initChart() {
        chart.clear()
        chart.setOnChartValueSelectedListener(this)
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setDrawGridBackground(false)
        chart.description = null
        chart.setBackgroundResource(R.color.chart_bg)
        chart.setScaleEnabled(true)
        chart.setPinchZoom(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.isScaleYEnabled = false
        chart.setExtraOffsets(
            0f,
            0f,
            SizeUtils.dp2px(8f).toFloat(),
            SizeUtils.dp2px(4f).toFloat()
        )
        chart.setNoDataText(getString(R.string.lms_http_code998))
        chart.setNoDataTextColor(textColor)
        val mv = MyMarkerView(this, R.layout.marker_lay)
        mv.chartView = chart
        chart.marker = mv
        val data = LineData()
        data.setValueTextColor(textColor)
        chart.data = data
        val l = chart.legend
        l.form = Legend.LegendForm.CIRCLE
        l.textColor = textColor
        l.isEnabled = false
        val xAxis = chart.xAxis
        xAxis.textColor = textColor
        xAxis.setDrawGridLines(true)
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.isEnabled = true
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisLineColor = textColor
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.textSize = 9f
        val leftAxis = chart.axisLeft
        leftAxis.textSize = 9f
        leftAxis.textColor = textColor
        leftAxis.setDrawGridLines(true)
        leftAxis.setLabelCount(6, false)
        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false
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
    private val textColor by lazy { ContextCompat.getColor(this, R.color.chart_text) }

    private fun createSet(index: Int, label: String): LineDataSet {
        val set = LineDataSet(null, label)
        set.mode = LineDataSet.Mode.LINEAR
        set.setDrawFilled(false)
        set.fillDrawable = ContextCompat.getDrawable(this, bgChartColors[index])
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.color = ContextCompat.getColor(this, lineChartColors[index])
        set.setCircleColor(ContextCompat.getColor(this, R.color.white))
        set.valueTextColor = Color.WHITE
        set.lineWidth = 2f
        set.circleRadius = 1f
        set.setCircleColor(ContextCompat.getColor(this, lineChartColors[index]))
        set.fillAlpha = 200
        set.valueTextSize = 10f
        set.setDrawValues(false)
        return set
    }

    private fun initEntry(data: ArrayList<ThermalEntity>) {
        synchronized(chart) {
            lifecycleScope.launch(Dispatchers.IO) {
                clearEntity(data.size == 0)
                if (data.size == 0) {
                    return@launch
                }
                Log.i("chart", "update chart start")
                val lineData: LineData = chart.data
                if (lineData != null) {
                    Log.w(
                        "123",
                        "时间区间:${(data.last().createTime - data.first().createTime) / 1000}"
                    )
                    val startTime = data[0].createTime
                    Log.w("123", "设置初始时间startTime:$startTime")
                    chart.xAxis.valueFormatter =
                        MyValueFormatter(startTime = startTime, type = selectType)
                    XLog.w("chart init startTime:$startTime")
                    data[0].type = "default"
                    when (data[0].type) {
                        "point" -> {
                            var set = lineData.getDataSetByIndex(0)
                            if (set == null) {
                                set = createSet(2, "temp")
                                lineData.addDataSet(set)
                            }
                            data.forEach {
                                val x = (it.createTime - startTime).toFloat()
                                val entity = Entry(x, it.thermal)
                                entity.data = it
                                set.addEntry(entity)
                            }
                            XLog.w("DataSet:${set.entryCount}")
                        }
                        "line" -> {
                            var maxDataSet = lineData.getDataSetByIndex(0)
                            if (maxDataSet == null) {
                                maxDataSet = createSet(0, "line maxTemp")
                                lineData.addDataSet(maxDataSet)
                            }

                            var minDataSet = lineData.getDataSetByIndex(1)
                            if (minDataSet == null) {
                                minDataSet = createSet(1, "line minTemp")
                                lineData.addDataSet(minDataSet)
                            }
                            Log.w("123", "两条曲线")
                            data.forEach {
                                val x = (it.createTime - startTime).toFloat()
                                val entity = Entry(x, it.thermalMax)
                                entity.data = it
                                maxDataSet.addEntry(entity)
                                val entityMin = Entry(x, it.thermalMin)
                                entityMin.data = it
                                minDataSet.addEntry(entityMin)
                            }
                            XLog.w("DataSet:${maxDataSet.entryCount}")
                        }
                        else -> {
                            var maxTempDataSet = lineData.getDataSetByIndex(0)
                            if (maxTempDataSet == null) {
                                maxTempDataSet = createSet(0, "fence maxTemp")
                                lineData.addDataSet(maxTempDataSet)
                            }
                            var centerTempDataSet = lineData.getDataSetByIndex(1)
                            if (centerTempDataSet == null) {
                                centerTempDataSet = createSet(1, "fence minTemp")
                                lineData.addDataSet(centerTempDataSet)
                            }
                            data.forEach {
                                val x = (it.createTime - startTime).toFloat()
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
                    chart.notifyDataSetChanged()
                    chart.setVisibleXRangeMinimum(getMinimum())
                    chart.setVisibleXRangeMaximum(getMaximum())
                    chart.xAxis.setLabelCount(5, false)
                    chart.moveViewToX(chart.xChartMax)
                    chart.zoom(1f, 1f, chart.xChartMax, 0f)
                }
                Log.w("chart", "update chart finish")
            }
        }
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {

    }

    override fun onNothingSelected() {

    }

    private fun getLabCount(count: Int): Int {
        return when (count) {
            in 0..2 -> 1
            in 3..4 -> 2
            in 5..6 -> 3
            in 7..9 -> 4
            else -> 5
        }
    }

    private fun getMinimum(): Float {
        val min = when (selectType) {
            1 -> 1 * 10 * 1000f
            2 -> 10 * 60 * 1000f
            3 -> 10 * 60 * 60 * 1000f
            4 -> 10 * 24 * 60 * 60 * 1000f
            else -> 1 * 10 * 1000f
        }
        return min
    }

    private fun getMaximum(): Float {
        return getMinimum() * 50f
    }

    private fun clearEntity(isEmpty: Boolean) {
        initChart()
        if (isEmpty) {
            chart.clear()
        } else {
            chart.clearValues()
        }
    }

}