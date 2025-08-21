package com.topdon.module.thermal.ir.report.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.tools.FileTools
import com.topdon.lib.core.tools.GlideLoader
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.libcom.PDFHelp
import com.topdon.module.thermal.ir.report.view.ReportIRShowView
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.report.bean.ReportBean
import kotlinx.android.synthetic.main.activity_report_detail.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * 报告详情界面.
 *
 * 需要传递
 * - 一份报告所有信息 [ExtraKeyConfig.REPORT_BEAN]
 */
@Route(path = RouterConfig.REPORT_DETAIL)
class ReportDetailActivity: BaseActivity() {

    /**
     * 从上一界面传递过来的，报告所有信息.
     */
    private var reportBean: ReportBean? = null

    /**
     * 当前预览页面已生成的 PDF 文件绝对路径
     */
    private var pdfFilePath: String? = null


    override fun initContentView() = R.layout.activity_report_detail

    override fun initView() {
        reportBean = intent.getParcelableExtra(ExtraKeyConfig.REPORT_BEAN)

        title_view.setTitleText(R.string.album_edit_report)
        title_view.setLeftDrawable(R.drawable.svg_arrow_left_e8)
        title_view.setRightDrawable(R.drawable.ic_share_black_svg)
        title_view.setLeftClickListener {
            finish()
        }
        title_view.setRightClickListener {
            saveWithPDF()
        }

        report_info_view.refreshInfo(reportBean?.report_info)
        report_info_view.refreshCondition(reportBean?.detection_condition)

        if (reportBean?.report_info?.is_report_watermark == 1) {
            watermark_view.watermarkText = reportBean?.report_info?.report_watermark
        }

        val irList = reportBean?.infrared_data
        if (irList != null) {
            for (i in irList.indices) {
                val reportShowView = ReportIRShowView(this)
                reportShowView.refreshData(i == 0, i == irList.size - 1, irList[i])
                lifecycleScope.launch {
                    val drawable = GlideLoader.getDrawable(this@ReportDetailActivity, irList[i].picture_url)
                    reportShowView.setImageDrawable(drawable)
                }
                ll_content.addView(reportShowView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
    }

    override fun initData() {
    }

    private fun saveWithPDF() {
        if (TextUtils.isEmpty(pdfFilePath)) {
            showCameraLoading()
            lifecycleScope.launch(Dispatchers.IO) {
                val name = reportBean?.report_info?.report_number
                if (name != null) {
                    if (File(FileConfig.getPdfDir() + "/$name.pdf").exists() &&
                        !TextUtils.isEmpty(pdfFilePath)) {
                        lifecycleScope.launch {
                            dismissCameraLoading()
                            actionShare()
                        }
                        return@launch
                    }
                }
                pdfFilePath = PDFHelp.savePdfFileByListView(name?:System.currentTimeMillis().toString(),
                    scroll_view, getPrintViewList(),watermark_view)
                lifecycleScope.launch {
                    dismissCameraLoading()
                    actionShare()
                }
            }
        } else {
            actionShare()
        }
    }

    private fun actionShare() {
        val uri = FileTools.getUri(File(pdfFilePath!!))
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "application/pdf"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.battery_share)))
    }

    /**
     * 获取需要转为 PDF 的所有 View 列表.
     * 注意：水印 View 不在列表内，需要自行处理.
     */
    private fun getPrintViewList(): ArrayList<View> {
        val result = ArrayList<View>()
        result.add(report_info_view)
        val childCount = ll_content.childCount
        for (i in 0 until  childCount) {
            val childView = ll_content.getChildAt(i)
            if (childView is ReportIRShowView) {
                result.addAll(childView.getPrintViewList())
            }
        }
        return result
    }
}