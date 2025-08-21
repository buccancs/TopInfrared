package com.topdon.module.thermal.ir.report.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.report.bean.ReportConditionBean
import com.topdon.module.thermal.ir.report.bean.ReportInfoBean
import kotlinx.android.synthetic.main.view_report_info.view.*

/**
 * 报告信息 - 预览 View.
 */
class ReportInfoView: LinearLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.view_report_info, this, true)
    }

    /**
     * 根据指定的报告信息刷新对应 View.
     */
    fun refreshInfo(reportInfoBean: ReportInfoBean?) {
        tv_report_name.text = reportInfoBean?.report_name

        tv_report_author.isVisible = reportInfoBean?.is_report_author == 1
        tv_report_author.text = reportInfoBean?.report_author

        group_report_place.isVisible = reportInfoBean?.is_report_place == 1
        tv_report_place.text = reportInfoBean?.report_place

        tv_report_date.isVisible = reportInfoBean?.is_report_date == 1
        tv_report_date.text = reportInfoBean?.report_date
    }

    /**
     * 根据指定的检测条件信息刷新对应 View.
     */
    fun refreshCondition(conditionBean: ReportConditionBean?) {
        cl_report_condition.isVisible = conditionBean?.is_ambient_humidity == 1
                || conditionBean?.is_ambient_temperature == 1
                || conditionBean?.is_test_distance == 1
                || conditionBean?.is_emissivity == 1

        group_ambient_temperature.isVisible = conditionBean?.is_ambient_temperature == 1
        tv_ambient_temperature.text = conditionBean?.ambient_temperature
        view_line_1.isVisible = conditionBean?.is_ambient_temperature == 1 &&
                (conditionBean.is_ambient_humidity == 1 || conditionBean.is_test_distance == 1 || conditionBean.is_emissivity == 1)

        group_ambient_humidity.isVisible = conditionBean?.is_ambient_humidity == 1
        tv_ambient_humidity.text = conditionBean?.ambient_humidity
        view_line_2.isVisible = conditionBean?.is_ambient_humidity == 1 && (conditionBean.is_test_distance == 1 || conditionBean.is_emissivity == 1)

        group_test_distance.isVisible = conditionBean?.is_test_distance == 1
        tv_test_distance.text = conditionBean?.test_distance
        view_line_3.isVisible = conditionBean?.is_test_distance == 1 && conditionBean.is_emissivity == 1

        group_emissivity.isVisible = conditionBean?.is_emissivity == 1
        tv_emissivity.text = conditionBean?.emissivity
    }

    /**
     * 获取需要转为 PDF 的所有 View 列表.
     */
    fun getPrintViewList(): ArrayList<View> {
        val result = ArrayList<View>()
        result.add(cl_top)
        result.add(cl_report_condition)
        return result
    }
}