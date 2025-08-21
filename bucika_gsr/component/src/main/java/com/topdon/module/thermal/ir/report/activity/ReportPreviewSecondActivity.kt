package com.topdon.module.thermal.ir.report.activity

import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.topdon.lib.core.bean.event.ReportCreateEvent
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseViewModelActivity
import com.topdon.lib.core.tools.FileTools
import com.topdon.lib.core.tools.GlideLoader
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.socket.WebSocketProxy
import com.topdon.lib.core.utils.NetWorkUtils
import com.topdon.libcom.PDFHelp
import com.topdon.lms.sdk.LMS
import com.topdon.lms.sdk.utils.StringUtils
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.module.thermal.ir.report.view.ReportIRShowView
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.report.bean.ReportBean
import com.topdon.module.thermal.ir.report.viewmodel.UpReportViewModel
import kotlinx.android.synthetic.main.activity_report_preview_second.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File

/**
 * 生成报告第2步的预览界面.
 *
 * 需要传递
 * - 是否 TC007: [ExtraKeyConfig.IS_TC007]
 * - 一份报告所有信息 [ExtraKeyConfig.REPORT_BEAN]
 */
@Route(path = RouterConfig.REPORT_PREVIEW_SECOND)
class ReportPreviewSecondActivity: BaseViewModelActivity<UpReportViewModel>(), View.OnClickListener {

    /**
     * 从上一界面传递过来的，当前是否为 TC007 设备类型.
     * true-TC007 false-其他插件式设备
     */
    private var isTC007 = false

    /**
     * 从上一界面传递过来的，报告所有信息.
     */
    private var reportBean: ReportBean? = null

    /**
     * 当前预览页面已生成的 PDF 文件绝对路径
     */
    private var pdfFilePath: String? = null


    override fun initContentView() = R.layout.activity_report_preview_second

    override fun providerVMClass() = UpReportViewModel::class.java

    override fun initView() {
        reportBean = intent.getParcelableExtra(ExtraKeyConfig.REPORT_BEAN)
        isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)

        title_view.setTitleText(R.string.album_edit_preview)
        title_view.setLeftDrawable(R.drawable.svg_arrow_left_e8)
        title_view.setRightDrawable(R.drawable.ic_report_exit_svg)
        title_view.setLeftClickListener {
            finish()
        }
        title_view.setRightClickListener {
            TipDialog.Builder(this)
                .setMessage(R.string.album_report_exit_tips)
                .setPositiveListener(R.string.app_ok){
                    EventBus.getDefault().post(ReportCreateEvent())
                    finish()
                }
                .setCancelListener(R.string.app_cancel){
                }
                .setCanceled(false)
                .create().show()
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
                    val drawable = GlideLoader.getDrawable(this@ReportPreviewSecondActivity, irList[i].picture_url)
                    reportShowView.setImageDrawable(drawable)
                }
                ll_content.addView(reportShowView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }

        tv_to_pdf.setOnClickListener(this)
        tv_complete.setOnClickListener(this)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                // 要是当前已连接 TS004、TC007，切到流量上，不然登录注册意见反馈那些没网
                if (WebSocketProxy.getInstance().isConnected()) {
                    NetWorkUtils.connectivityManager.bindProcessToNetwork(null)
                }
            }
        })
    }

    override fun initData() {
        viewModel.commonBeanLD.observe(this) {
            dismissCameraLoading()
            if (it.code == LMS.SUCCESS) {
                EventBus.getDefault().post(ReportCreateEvent())
                ARouter.getInstance().build(RouterConfig.REPORT_LIST)
                    .withBoolean(ExtraKeyConfig.IS_TC007, isTC007)
                    .navigation(this)
                finish()
            } else {
                ToastUtils.showShort(StringUtils.getResString(this, it.code.toString()))
            }
        }
        viewModel.exceptionLD.observe(this) {
            dismissCameraLoading()
            requestError(it)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_to_pdf -> {//生成PDF
                saveWithPDF()
            }
            tv_complete -> {//完成

                if (LMS.getInstance().isLogin) {
                    if (!NetworkUtils.isConnected()) {
                        TToast.shortToast(this, R.string.setting_http_error)
                        return
                    }
                    showCameraLoading()
                    viewModel.upload(isTC007, reportBean)
                } else {
                    LMS.getInstance().activityLogin()
                }
            }
        }
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
                    tv_to_pdf.text = getString(R.string.battery_share)
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