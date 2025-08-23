package com.topdon.house.fragment

import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.topdon.house.R
import com.topdon.house.activity.DetectAddActivity
import com.topdon.house.activity.ReportAddActivity
import com.topdon.house.adapter.HouseAdapter
import com.topdon.house.event.HouseDetectAddEvent
import com.topdon.house.event.HouseDetectEditEvent
import com.topdon.house.popup.ThreePickPopup
import com.topdon.house.viewmodel.DetectViewModel
import com.topdon.house.viewmodel.TabViewModel
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.HouseDetect
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.lms.sdk.weiget.TToast
import kotlinx.android.synthetic.main.fragment_detect_list.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

internal class DetectListFragment : BaseFragment(), View.OnClickListener {
    private lateinit var adapter: HouseAdapter

    private val tabViewModel: TabViewModel by activityViewModels()

    private val viewModel: DetectViewModel by activityViewModels()

    override fun initContentView(): Int = R.layout.fragment_detect_list

    override fun initView() {
        cl_del.isEnabled = false
        iv_del.isEnabled = false
        tv_del.isEnabled = false

        adapter = HouseAdapter(requireContext(), true)
        adapter.onItemClickListener = {
            val intent = Intent(context, ReportAddActivity::class.java)
            intent.putExtra(ExtraKeyConfig.DETECT_ID, adapter.dataList[it].id)
            intent.putExtra(ExtraKeyConfig.IS_TC007, arguments?.getBoolean(ExtraKeyConfig.IS_TC007, false) ?: false)
            startActivity(intent)
        }
        adapter.onMoreClickListener = { position, v ->
            ThreePickPopup(requireContext(), arrayListOf(R.string.app_edit, R.string.paste, R.string.report_delete)) {
                when (it) {
                    0 -> {
                        val intent = Intent(requireContext(), DetectAddActivity::class.java)
                        intent.putExtra(ExtraKeyConfig.DETECT_ID, adapter.dataList[position].id)
                        startActivity(intent)
                    }
                    1 -> {
                        viewModel.copyDetect(position, adapter.dataList[position] as HouseDetect)
                    }
                    2 -> {
                        TipDialog.Builder(requireContext())
                            .setTitleMessage(getString(R.string.monitor_report_delete))
                            .setMessage(R.string.report_delete_tips)
                            .setCancelListener(R.string.app_cancel)
                            .setPositiveListener(R.string.thermal_delete) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val houseDetect: HouseDetect = adapter.dataList[position] as HouseDetect
                                    AppDatabase.getInstance().houseDetectDao().deleteDetect(houseDetect)
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

        viewModel.detectListLD.observe(viewLifecycleOwner) {
            group_empty.isVisible = it.isEmpty()
            recycler_view.isVisible = it.isNotEmpty()
            adapter.refresh(it)
        }
        viewModel.detectLD.observe(viewLifecycleOwner) {
            if (it != null) {
                for (i in adapter.dataList.indices) {
                    if (adapter.dataList[i].id == it.id) {
                        adapter.dataList[i] = it
                        adapter.notifyItemChanged(i)
                        break
                    }
                }
            }
        }
        viewModel.copyDetectLD.observe(viewLifecycleOwner) {
            TToast.shortToast(requireContext(), R.string.ts004_copy_success)
            adapter.dataList.add(it.first + 1, it.second)
            adapter.notifyItemInserted(it.first + 1)
        }
        viewModel.queryAll()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDetectCreate(event: HouseDetectAddEvent) {
        viewModel.queryAll()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDetectUpdate(event: HouseDetectEditEvent) {
        viewModel.queryById(event.id)
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
                            val resultArray: Array<HouseDetect> = Array(adapter.selectIndexList.size) {
                                adapter.dataList[adapter.selectIndexList[it]] as HouseDetect
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