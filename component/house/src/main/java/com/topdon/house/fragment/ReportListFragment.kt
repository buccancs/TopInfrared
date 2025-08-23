package com.topdon.house.fragment

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.FileUtils
import com.topdon.house.R
import com.topdon.house.activity.DetectAddActivity
import com.topdon.house.adapter.HouseAdapter
import com.topdon.house.dialog.InputTextDialog
import com.topdon.house.event.HouseReportAddEvent
import com.topdon.house.popup.ThreePickPopup
import com.topdon.house.util.PDFUtil
import com.topdon.house.viewmodel.ReportViewModel
import com.topdon.house.viewmodel.TabViewModel
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.HouseReport
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.lms.sdk.weiget.TToast
import kotlinx.android.synthetic.main.fragment_report_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

internal class ReportListFragment : BaseFragment(), View.OnClickListener {
    private lateinit var adapter: HouseAdapter

    private val tabViewModel: TabViewModel by activityViewModels()

    private val viewModel: ReportViewModel by activityViewModels()

    override fun initContentView(): Int = R.layout.fragment_report_list

    override fun initView() {
        cl_del.isEnabled = false
        iv_del.isEnabled = false
        tv_del.isEnabled = false

        adapter = HouseAdapter(requireContext(), false)
        adapter.onItemClickListener = {
            ARouter.getInstance().build(RouterConfig.REPORT_PREVIEW)
                .withBoolean(ExtraKeyConfig.IS_REPORT, true)
                .withLong(ExtraKeyConfig.LONG_ID, adapter.dataList[it].id)
                .navigation(requireContext())
        }
        adapter.onShareClickListener = {
            lifecycleScope.launch {
                showLoadingDialog()
                PDFUtil.delAllPDF(requireContext())
                val pdfUri: Uri? = PDFUtil.savePDF(requireContext(), adapter.dataList[it] as HouseReport)
                dismissLoadingDialog()
                if (pdfUri != null) {
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.putExtra(Intent.EXTRA_STREAM, pdfUri)
                    shareIntent.type = "application/pdf"
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.battery_share)))
                }
            }
        }
        adapter.onMoreClickListener = { position, v ->
            ThreePickPopup(requireContext(), arrayListOf(R.string.app_rename, R.string.report_delete)) {
                if (it == 0) {
                    val houseReport: HouseReport = adapter.dataList[position] as HouseReport
                    InputTextDialog(requireContext(), houseReport.name) { newName ->
                        if (houseReport.name != newName) {
                            FileUtils.delete(File(FileConfig.documentsDir, houseReport.getPdfFileName()))
                            houseReport.name = newName
                            viewModel.update(houseReport)
                            adapter.notifyItemChanged(position)
                        }
                    }.show()
                } else {
                    TipDialog.Builder(requireContext())
                        .setTitleMessage(getString(R.string.monitor_report_delete))
                        .setMessage(R.string.report_delete_tips)
                        .setCancelListener(R.string.app_cancel)
                        .setPositiveListener(R.string.thermal_delete) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                val houseReport: HouseReport = adapter.dataList[position] as HouseReport
                                AppDatabase.getInstance().houseReportDao().deleteReport(houseReport)
                                PDFUtil.delPDF(requireContext(), houseReport)
                                withContext(Dispatchers.Main) {
                                    adapter.dataList.removeAt(position)
                                    adapter.notifyItemRemoved(position)
                                    if (adapter.dataList.isEmpty()) {
                                        viewModel.queryAll()
                                    }
                                    TToast.shortToast(requireContext(), R.string.test_results_delete_success)
                                }
                            }
                        }
                        .create().show()
                }
            }.show(v, false)
        }
        adapter.onSelectChangeListener = {
            tabViewModel.selectSizeLD.value = it
        }
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        recycler_view.adapter = adapter

        tv_add.setOnClickListener(this)
        cl_del.setOnClickListener(this)

        tabViewModel.isEditModeLD.observe(viewLifecycleOwner) {
            adapter.isEditMode = it
            cl_del.isVisible = it
        }
        tabViewModel.selectSizeLD.observe(viewLifecycleOwner) {
            cl_del.isEnabled = it > 0
            iv_del.isEnabled = it > 0
            tv_del.isEnabled = it > 0
        }

        viewModel.reportListLD.observe(viewLifecycleOwner) {
            group_empty.isVisible = it.isEmpty()
            recycler_view.isVisible = it.isNotEmpty()
            adapter.refresh(it)
        }
        viewModel.queryAll()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDetectCreate(event: HouseReportAddEvent) {
        viewModel.queryAll()
    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_add -> {
                val intent = Intent(requireContext(), DetectAddActivity::class.java)
                intent.putExtra(ExtraKeyConfig.IS_TC007, arguments?.getBoolean(ExtraKeyConfig.IS_TC007, false) ?: false)
                startActivity(intent)
            }
            cl_del -> {
                if (adapter.selectIndexList.isNotEmpty()) {
                    TipDialog.Builder(requireContext())
                        .setTitleMessage(getString(R.string.monitor_report_delete))
                        .setMessage(R.string.report_delete_tips)
                        .setCancelListener(R.string.app_cancel)
                        .setPositiveListener(R.string.thermal_delete) {
                            val resultArray: Array<HouseReport> = Array(adapter.selectIndexList.size) {
                                adapter.dataList[adapter.selectIndexList[it]] as HouseReport
                            }
                            viewModel.deleteMore(*resultArray)
                            tabViewModel.isEditModeLD.value = false
                            TToast.shortToast(requireContext(), R.string.test_results_delete_success)
                        }
                        .create().show()
                }
            }
        }
    }
}