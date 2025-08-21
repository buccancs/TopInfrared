package com.topdon.module.thermal.ir.report.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import com.topdon.lib.core.tools.UnitTools
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.report.bean.ImageTempBean
import kotlinx.android.synthetic.main.item_report_ir_input.view.*
import kotlinx.android.synthetic.main.view_report_ir_input.view.*

class ReportIRInputView: LinearLayout {

    companion object {
        private const val TYPE_FULL = 0 //全图
        private const val TYPE_POINT = 1//点
        private const val TYPE_LINE = 2 //线
        private const val TYPE_RECT = 3 //面
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @SuppressLint("SetTextI18n")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        inflate(context, R.layout.view_report_ir_input, this)

        cl_explain.et_item.inputType = InputType.TYPE_CLASS_TEXT
        cl_explain.et_item.filters = arrayOf(LengthFilter(150))

        setSwitchListener(cl_max.switch_item, cl_max.et_item)
        setSwitchListener(cl_min.switch_item, cl_min.et_item)
        setSwitchListener(cl_average.switch_item, cl_average.et_item)
        setSwitchListener(cl_explain.switch_item, cl_explain.et_item)

        val typeArray = context.obtainStyledAttributes(attrs, R.styleable.ReportIRInputView)
        val type = typeArray.getInt(R.styleable.ReportIRInputView_type, TYPE_FULL)
        val index = typeArray.getInt(R.styleable.ReportIRInputView_index, 0)
        typeArray.recycle()

        cl_title.isVisible = index == 0
        view_line.isVisible = index > 0

        when (type) {
            TYPE_FULL -> {
                tv_title.setText(R.string.thermal_full_rect)
                cl_min.isVisible = true
                cl_average.isVisible = false
                cl_max.tv_item_name.text = context.getString(R.string.chart_temperature_high) + " (${UnitTools.showUnit()})"
                cl_min.tv_item_name.text = context.getString(R.string.chart_temperature_low) + " (${UnitTools.showUnit()})"
                cl_explain.tv_item_name.text = context.getString(R.string.album_report_comment)
            }
            TYPE_POINT -> {
                tv_title.text = context.getString(R.string.thermal_point) + "(P)"
                cl_min.isVisible = false
                cl_average.isVisible = false
                cl_max.tv_item_name.text = "P${index + 1} " + context.getString(R.string.chart_temperature) + " (${UnitTools.showUnit()})"
                cl_explain.tv_item_name.text = "P${index + 1} " + context.getString(R.string.album_report_comment)
            }
            TYPE_LINE -> {
                tv_title.text = context.getString(R.string.thermal_line) + "(L)"
                cl_min.isVisible = true
                cl_average.isVisible = true
                cl_max.tv_item_name.text = "L${index + 1} " + context.getString(R.string.chart_temperature_high) + " (${UnitTools.showUnit()})"
                cl_min.tv_item_name.text = "L${index + 1} " + context.getString(R.string.chart_temperature_low) + " (${UnitTools.showUnit()})"
                cl_average.tv_item_name.text = "L${index + 1} " + context.getString(R.string.album_report_mean_temperature) + " (${UnitTools.showUnit()})"
                cl_explain.tv_item_name.text = "L${index + 1} " + context.getString(R.string.album_report_comment)
            }
            TYPE_RECT -> {
                tv_title.text = context.getString(R.string.thermal_rect) + "(R)"
                cl_min.isVisible = true
                cl_average.isVisible = true
                cl_max.tv_item_name.text = "R${index + 1} " + context.getString(R.string.chart_temperature_high) + " (${UnitTools.showUnit()})"
                cl_min.tv_item_name.text = "R${index + 1} " + context.getString(R.string.chart_temperature_low) + " (${UnitTools.showUnit()})"
                cl_average.tv_item_name.text = "R${index + 1} " + context.getString(R.string.album_report_mean_temperature) + " (${UnitTools.showUnit()})"
                cl_explain.tv_item_name.text = "R${index + 1} " + context.getString(R.string.album_report_comment)
            }
        }
    }

    fun isSwitchMaxCheck() = cl_max.switch_item.isChecked
    fun isSwitchMinCheck() = cl_min.switch_item.isChecked
    fun isSwitchAverageCheck() = cl_average.switch_item.isChecked
    fun isSwitchExplainCheck() = cl_explain.switch_item.isChecked

    fun getMaxInput() = cl_max.et_item.text.toString()
    fun getMinInput() = cl_min.et_item.text.toString()
    fun getAverageInput() = cl_average.et_item.text.toString()
    fun getExplainInput() = cl_explain.et_item.text.toString()

    fun refreshData(tempBean: ImageTempBean.TempBean?) {
        tempBean?.max?.let {
            cl_max.et_item.setText(UnitTools.showUnitValue(it.toFloat())?.toString())
        }
        tempBean?.min?.let {
            cl_min.et_item.setText(UnitTools.showUnitValue(it.toFloat())?.toString())
        }
        tempBean?.average?.let {
            cl_average.et_item.setText(UnitTools.showUnitValue(it.toFloat())?.toString())
        }
        cl_explain.et_item.setText("")
    }

    private fun setSwitchListener(switchCompat: SwitchCompat, editText: EditText) {
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            editText.isVisible = isChecked
        }
    }
}