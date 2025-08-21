package com.topdon.module.thermal.ir.report.activity

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ToastUtils
import com.topdon.lib.core.bean.event.ReportCreateEvent
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.core.tools.GlideLoader
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.report.bean.*
import kotlinx.android.synthetic.main.activity_report_create_second.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 生成报告第2步（共2步）.
 *
 * 需要传递
 * - 必选：是否 TC007: [ExtraKeyConfig.IS_TC007] 透传，再次拾取图片时进入目录不同，上传报告参数不同
 * - 必选：当前编辑的图片绝对路径 [ExtraKeyConfig.FILE_ABSOLUTE_PATH]
 * - 必选：当前编辑的图片点线面全图温度数据 [ExtraKeyConfig.IMAGE_TEMP_BEAN]
 * - 必选：报告信息 [ExtraKeyConfig.REPORT_INFO]
 * - 可选：检测条件 [ExtraKeyConfig.REPORT_CONDITION]
 * - 可选：当前已确认的图片信息列表 [ExtraKeyConfig.REPORT_IR_LIST]
 */
@Route(path = RouterConfig.REPORT_CREATE_SECOND)
class ReportCreateSecondActivity: BaseActivity(), View.OnClickListener {

    /**
     * 当前已添加的图片信息列表.
     */
    private var reportIRList: ArrayList<ReportIRBean> = ArrayList(0)


    /**
     * 从上一界面传递过来的，添加的图片绝对路径.
     */
    private var currentFilePath: String = ""
    /**
     * 从上一界面传递过来的，当前编辑的图片点线面全图温度数据
     */
    private var imageTempBean: ImageTempBean? = null



    override fun initContentView() = R.layout.activity_report_create_second

    override fun initView() {
        currentFilePath = intent.getStringExtra(ExtraKeyConfig.FILE_ABSOLUTE_PATH)!!
        imageTempBean = intent.getParcelableExtra(ExtraKeyConfig.IMAGE_TEMP_BEAN)
        reportIRList = intent.getParcelableArrayListExtra(ExtraKeyConfig.REPORT_IR_LIST) ?: ArrayList(10)

        refreshImg(currentFilePath)
        refreshData(imageTempBean)

        tv_add_image.setOnClickListener(this)
        tv_preview.setOnClickListener(this)
    }

    override fun initData() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReportCreate(event: ReportCreateEvent) {
        finish()
    }

    private fun refreshImg(absolutePath: String?) {
        lifecycleScope.launch {
            val drawable = GlideLoader.getDrawable(this@ReportCreateSecondActivity, absolutePath)
            val isLand = (drawable?.intrinsicWidth ?: 0) > (drawable?.intrinsicHeight ?: 0)
            val width = (ScreenUtil.getScreenWidth(this@ReportCreateSecondActivity) * (if (isLand) 234 else 175) / 375f).toInt()
            val layoutParams = iv_image.layoutParams
            layoutParams.width = width
            layoutParams.height = (width * (drawable?.intrinsicHeight ?: 0).toFloat() / (drawable?.intrinsicWidth ?: 1)).toInt()
            iv_image.layoutParams = layoutParams
            iv_image.setImageDrawable(drawable)
        }
    }

    private fun refreshData(imageTempBean: ImageTempBean?) {
        scroll_view.scrollTo(0, 0)

        report_temp_view_full.isVisible = imageTempBean?.full != null
        report_temp_view_full.refreshData(imageTempBean?.full)

        report_temp_view_point1.isVisible = (imageTempBean?.pointList?.size ?: 0) > 0
        if ((imageTempBean?.pointList?.size ?: 0) > 0) {
            report_temp_view_point1.refreshData(imageTempBean?.pointList?.get(0))
        }
        report_temp_view_point2.isVisible = (imageTempBean?.pointList?.size ?: 0) > 1
        if ((imageTempBean?.pointList?.size ?: 0) > 1) {
            report_temp_view_point2.refreshData(imageTempBean?.pointList?.get(1))
        }
        report_temp_view_point3.isVisible = (imageTempBean?.pointList?.size ?: 0) > 2
        if ((imageTempBean?.pointList?.size ?: 0) > 2) {
            report_temp_view_point3.refreshData(imageTempBean?.pointList?.get(2))
        }
        report_temp_view_point4.isVisible = (imageTempBean?.pointList?.size ?: 0) > 3
        if ((imageTempBean?.pointList?.size ?: 0) > 3) {
            report_temp_view_point4.refreshData(imageTempBean?.pointList?.get(3))
        }
        report_temp_view_point5.isVisible = (imageTempBean?.pointList?.size ?: 0) > 4
        if ((imageTempBean?.pointList?.size ?: 0) > 4) {
            report_temp_view_point5.refreshData(imageTempBean?.pointList?.get(4))
        }

        report_temp_view_line1.isVisible = (imageTempBean?.lineList?.size ?: 0) > 0
        if ((imageTempBean?.lineList?.size ?: 0) > 0) {
            report_temp_view_line1.refreshData(imageTempBean?.lineList?.get(0))
        }
        report_temp_view_line2.isVisible = (imageTempBean?.lineList?.size ?: 0) > 1
        if ((imageTempBean?.lineList?.size ?: 0) > 1) {
            report_temp_view_line2.refreshData(imageTempBean?.lineList?.get(1))
        }
        report_temp_view_line3.isVisible = (imageTempBean?.lineList?.size ?: 0) > 2
        if ((imageTempBean?.lineList?.size ?: 0) > 2) {
            report_temp_view_line3.refreshData(imageTempBean?.lineList?.get(2))
        }
        report_temp_view_line4.isVisible = (imageTempBean?.lineList?.size ?: 0) > 3
        if ((imageTempBean?.lineList?.size ?: 0) > 3) {
            report_temp_view_line4.refreshData(imageTempBean?.lineList?.get(3))
        }
        report_temp_view_line5.isVisible = (imageTempBean?.lineList?.size ?: 0) > 4
        if ((imageTempBean?.lineList?.size ?: 0) > 4) {
            report_temp_view_line5.refreshData(imageTempBean?.lineList?.get(4))
        }

        report_temp_view_rect1.isVisible = (imageTempBean?.rectList?.size ?: 0) > 0
        if ((imageTempBean?.rectList?.size ?: 0) > 0) {
            report_temp_view_rect1.refreshData(imageTempBean?.rectList?.get(0))
        }
        report_temp_view_rect2.isVisible = (imageTempBean?.rectList?.size ?: 0) > 1
        if ((imageTempBean?.rectList?.size ?: 0) > 1) {
            report_temp_view_rect2.refreshData(imageTempBean?.rectList?.get(1))
        }
        report_temp_view_rect3.isVisible = (imageTempBean?.rectList?.size ?: 0) > 2
        if ((imageTempBean?.rectList?.size ?: 0) > 2) {
            report_temp_view_rect3.refreshData(imageTempBean?.rectList?.get(2))
        }
        report_temp_view_rect4.isVisible = (imageTempBean?.rectList?.size ?: 0) > 3
        if ((imageTempBean?.rectList?.size ?: 0) > 3) {
            report_temp_view_rect4.refreshData(imageTempBean?.rectList?.get(3))
        }
        report_temp_view_rect5.isVisible = (imageTempBean?.rectList?.size ?: 0) > 4
        if ((imageTempBean?.rectList?.size ?: 0) > 4) {
            report_temp_view_rect5.refreshData(imageTempBean?.rectList?.get(4))
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_add_image -> {//添加图片
                if (reportIRList.size >= 9) {
                    ToastUtils.showShort(R.string.album_report_max_image_tips)
                    return
                }
                val reportIRBeanList = ArrayList<ReportIRBean>(reportIRList)
                reportIRBeanList.add(buildReportIr(currentFilePath))
                ARouter.getInstance()
                    .build(RouterConfig.REPORT_PICK_IMG)
                    .withBoolean(ExtraKeyConfig.IS_TC007, intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false))
                    .withParcelable(ExtraKeyConfig.REPORT_INFO, intent.getParcelableExtra(ExtraKeyConfig.REPORT_INFO))
                    .withParcelable(ExtraKeyConfig.REPORT_CONDITION, intent.getParcelableExtra(ExtraKeyConfig.REPORT_CONDITION))
                    .withParcelableArrayList(ExtraKeyConfig.REPORT_IR_LIST, reportIRBeanList)
                    .navigation(this)
            }
            tv_preview -> {//预览
                val appLanguage = SharedManager.getLanguage(this)
                val sdkVersion = "1.2.8_23050619"
                val reportInfoBean: ReportInfoBean? = intent.getParcelableExtra(ExtraKeyConfig.REPORT_INFO)
                val conditionBean: ReportConditionBean? = intent.getParcelableExtra(ExtraKeyConfig.REPORT_CONDITION)
                val reportIRBeanList = ArrayList<ReportIRBean>(reportIRList)
                reportIRBeanList.add(buildReportIr(currentFilePath))
                val reportBean = ReportBean(SoftwareInfo(appLanguage, sdkVersion), reportInfoBean!!, conditionBean!!, reportIRBeanList)
                ARouter.getInstance().build(RouterConfig.REPORT_PREVIEW_SECOND)
                    .withBoolean(ExtraKeyConfig.IS_TC007, intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false))
                    .withParcelable(ExtraKeyConfig.REPORT_BEAN, reportBean)
                    .navigation(this)
            }
        }
    }

    private fun buildReportIr(filePath: String): ReportIRBean {
        val full: ReportTempBean? = if (imageTempBean?.full != null) {
            ReportTempBean(
                if (report_temp_view_full.getMaxInput().isNotEmpty()) report_temp_view_full.getMaxInput() + UnitTools.showUnit() else "",
                if (report_temp_view_full.isSwitchMaxCheck() && report_temp_view_full.getMaxInput().isNotEmpty()) 1 else 0,
                if (report_temp_view_full.getMinInput().isNotEmpty()) report_temp_view_full.getMinInput() + UnitTools.showUnit() else "",
                if (report_temp_view_full.isSwitchMinCheck() && report_temp_view_full.getMinInput().isNotEmpty()) 1 else 0,
                report_temp_view_full.getExplainInput(),
                if (report_temp_view_full.isSwitchExplainCheck() && report_temp_view_full.getExplainInput().isNotEmpty()) 1 else 0
            )
        } else {
            null
        }

        val pointList = buildReportTempBeanList(1)
        val lienList = buildReportTempBeanList(2)
        val rectList = buildReportTempBeanList(3)
        return ReportIRBean("", filePath, full, pointList, lienList, rectList)
    }

    /**
     * 构建报告点线面数据列表.
     * @param type 1-点 2-线 3-面
     */
    private fun buildReportTempBeanList(type: Int): ArrayList<ReportTempBean> {
        val size = when (type) {
            1 -> imageTempBean?.pointList?.size ?: 0
            2 -> imageTempBean?.lineList?.size ?: 0
            else -> imageTempBean?.rectList?.size ?: 0
        }
        val resultList = ArrayList<ReportTempBean>(size)
        for (i in 0 until size) {
            val reportTempView = when (type) {
                1 -> { //点
                    when (i) {
                        0 -> report_temp_view_point1
                        1 -> report_temp_view_point2
                        2 -> report_temp_view_point3
                        3 -> report_temp_view_point4
                        else -> report_temp_view_point5
                    }
                }
                2 -> { //线
                    when (i) {
                        0 -> report_temp_view_line1
                        1 -> report_temp_view_line2
                        2 -> report_temp_view_line3
                        3 -> report_temp_view_line4
                        else -> report_temp_view_line5
                    }
                }
                else -> { //面
                    when (i) {
                        0 -> report_temp_view_rect1
                        1 -> report_temp_view_rect2
                        else -> report_temp_view_rect3
                    }
                }
            }
            val reportTempBean = if (type == 1) {//点的数据封装不太一样
                ReportTempBean(
                    if (reportTempView.getMaxInput().isNotEmpty()) reportTempView.getMaxInput() + UnitTools.showUnit() else "",
                    if (reportTempView.isSwitchMaxCheck() && reportTempView.getMaxInput().isNotEmpty()) 1 else 0,
                    reportTempView.getExplainInput(),
                    if (reportTempView.isSwitchExplainCheck() && reportTempView.getExplainInput().isNotEmpty()) 1 else 0
                )
            } else {
                ReportTempBean(
                    if (reportTempView.getMaxInput().isNotEmpty()) reportTempView.getMaxInput() + UnitTools.showUnit() else "",
                    if (reportTempView.isSwitchMaxCheck() && reportTempView.getMaxInput().isNotEmpty()) 1 else 0,
                    if (reportTempView.getMinInput().isNotEmpty()) reportTempView.getMinInput() + UnitTools.showUnit() else "",
                    if (reportTempView.isSwitchMinCheck() && reportTempView.getMinInput().isNotEmpty()) 1 else 0,
                    reportTempView.getExplainInput(),
                    if (reportTempView.isSwitchExplainCheck() && reportTempView.getExplainInput().isNotEmpty()) 1 else 0,
                    if (reportTempView.getAverageInput().isNotEmpty()) reportTempView.getAverageInput() + UnitTools.showUnit() else "",
                    if (reportTempView.isSwitchAverageCheck() && reportTempView.getAverageInput().isNotEmpty()) 1 else 0
                )
            }
            resultList.add(reportTempBean)
        }
        return resultList
    }

}