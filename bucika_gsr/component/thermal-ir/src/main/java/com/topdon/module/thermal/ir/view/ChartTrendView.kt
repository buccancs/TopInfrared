package com.topdon.module.thermal.ir.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.topdon.lib.core.tools.UnitTools
import com.topdon.module.thermal.ir.R

class ChartTrendView : LineChart {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        val textColor: Int = ContextCompat.getColor(context, R.color.chart_text)
        val axisChartColors: Int = ContextCompat.getColor(context, R.color.chart_axis)

        this.isDragEnabled = false
        this.isScaleYEnabled = false //禁止Y轴缩放
        this.isScaleXEnabled = false //禁止X轴缩放
        this.isDoubleTapToZoomEnabled = false//双击不可缩放
        this.setScaleEnabled(false)//缩放
        this.setPinchZoom(false)//禁用后，可以分别在x轴和y轴上进行缩放
        this.setTouchEnabled(true)
        this.setDrawGridBackground(false)
        this.description = null//图标描述文本
        this.axisRight.isEnabled = false //不绘制右侧Y轴
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

        legend.form = Legend.LegendForm.CIRCLE
        legend.textColor = textColor
        legend.isEnabled = false//隐藏曲线标签

        //x轴
        val xAxis = this.xAxis
        xAxis.textColor = textColor
        xAxis.setDrawGridLines(false)//竖向格线
        xAxis.axisLineColor = 0x00000000 //x轴颜色
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.isEnabled = true
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true//重复值不显示
        xAxis.textSize = 11f
        xAxis.isJumpFirstLabel = false
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 10f
        xAxis.setLabelCount(3, true)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                if (value < 5) {
                    return "A"
                }
                if (value > 5) {
                    return "B"
                }
                return ""
            }
        }

        //y轴
        val leftAxis = this.axisLeft
        leftAxis.textColor = textColor //y轴文本颜色
        leftAxis.axisLineColor = 0x00000000 //y轴颜色
        leftAxis.setDrawGridLines(true)//横向格线
        leftAxis.gridColor = axisChartColors //y轴网格颜色
        leftAxis.gridLineWidth = 1.5f
        leftAxis.setLabelCount(6, true)
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = ""
        }
        leftAxis.textSize = 11f
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 50f

        data = LineData()
    }

    fun setToEmpty() {
        axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = ""
        }
        data = LineData()
        invalidate()
    }

    /**
     * 根据指定的数据刷新折线图数据
     * @param tempList 温度值列表，单位摄氏度
     */
    fun refresh(tempList: List<Float>) {
        if (tempList.isEmpty()) {
            setToEmpty()
            return
        }

        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = (tempList.size - 1).toFloat()
        xAxis.setLabelCount(3, true)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                if (value < tempList.size / 3) {
                    return "A"
                }
                if (value > tempList.size * 2 / 3) {
                    return "B"
                }
                return ""
            }
        }

        var max = tempList.first()
        var min = tempList.first()
        val entryList: ArrayList<Entry> = ArrayList(tempList.size)
        for (i in tempList.indices) {
            val tempValue = tempList[i]
            max = max.coerceAtLeast(tempValue)
            min = min.coerceAtMost(tempValue)
            entryList.add(Entry(i.toFloat(), UnitTools.showUnitValue(tempValue)))
        }
        val maxUnit = UnitTools.showUnitValue(max)
        val minUnit = UnitTools.showUnitValue(min)
        axisLeft.axisMaximum = (maxUnit + (maxUnit - minUnit) / 3).coerceAtLeast(maxUnit + 0.3f)
        axisLeft.axisMinimum = (minUnit - (maxUnit - minUnit) / 3).coerceAtMost(minUnit - 0.3f)
        axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String = "${String.format("%.1f", value)}${UnitTools.showUnit()}"
        }

        val lineDataSet = LineDataSet(entryList, "point temp")
        lineDataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        lineDataSet.color = 0xffffffff.toInt()//曲线颜色
        lineDataSet.circleHoleColor = 0xffffffff.toInt()//坐标圆心颜色
        lineDataSet.setCircleColor(0xffffffff.toInt())//坐标颜色
        lineDataSet.valueTextColor = Color.WHITE
        lineDataSet.lineWidth = 2f
        lineDataSet.circleRadius = 1f//坐标点半径
        lineDataSet.fillAlpha = 200
        lineDataSet.valueTextSize = 10f
        lineDataSet.setDrawValues(false)//设置是否显示坐标值文本

        data = LineData(lineDataSet)
        invalidate()
    }
}