package com.topdon.module.thermal.ir.report.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.report.bean.ReportInfoBean
import kotlinx.android.synthetic.main.activity_report_preview_first.*

@Route(path = RouterConfig.REPORT_PREVIEW_FIRST)
class ReportPreviewFirstActivity: BaseActivity() {

    override fun initContentView() = R.layout.activity_report_preview_first

    override fun initView() {
        title_view.setLeftDrawable(R.drawable.svg_arrow_left_e8)
        title_view.setLeftClickListener {
            finish()
        }

        val reportInfoBean: ReportInfoBean? = intent.getParcelableExtra(ExtraKeyConfig.REPORT_INFO)
        report_info_view.refreshInfo(reportInfoBean)
        report_info_view.refreshCondition(intent.getParcelableExtra(ExtraKeyConfig.REPORT_CONDITION))

        if (reportInfoBean?.is_report_watermark == 1) {
            watermark_view.watermarkText = reportInfoBean.report_watermark
        }
    }

    override fun initData() {
    }
}