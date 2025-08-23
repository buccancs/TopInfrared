package com.topdon.house.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.topdon.house.R
import com.topdon.house.event.HouseReportAddEvent
import com.topdon.house.fragment.DetectListFragment
import com.topdon.house.fragment.ReportListFragment
import com.topdon.house.viewmodel.DetectViewModel
import com.topdon.house.viewmodel.ReportViewModel
import com.topdon.house.viewmodel.TabViewModel
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.ktbase.BaseActivity
import kotlinx.android.synthetic.main.activity_house_home.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HouseHomeActivity : BaseActivity(), View.OnClickListener {
    private val tabViewModel: TabViewModel by viewModels()

    private val detectViewModel: DetectViewModel by viewModels()

    private val reportViewModel: ReportViewModel by viewModels()

    override fun initContentView(): Int = R.layout.activity_house_home

    override fun initView() {
        iv_edit.isEnabled = false
        iv_back.setOnClickListener(this)
        iv_edit.setOnClickListener(this)
        iv_add.setOnClickListener(this)
        iv_exit_edit.setOnClickListener(this)

        val backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                tabViewModel.isEditModeLD.value = false
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback)

        tabViewModel.isEditModeLD.observe(this) {
            backCallback.isEnabled = it
            cl_title_bar.isVisible = !it
            cl_edit_bar.isVisible = it
            tab_layout.isVisible = !it
            view_pager2.isUserInputEnabled = !it
        }
        tabViewModel.selectSizeLD.observe(this) {
            tv_edit_title.text = if (it > 0) getString(R.string.chosen_item, it) else getString(R.string.not_selected)
        }

        detectViewModel.detectListLD.observe(this) {
            if (view_pager2.currentItem == 0) {
                iv_edit.isEnabled = !it.isNullOrEmpty()
            }
        }
        reportViewModel.reportListLD.observe(this) {
            if (view_pager2.currentItem == 1) {
                iv_edit.isEnabled = !it.isNullOrEmpty()
            }
        }

        view_pager2.adapter = ViewPagerAdapter(this)
        view_pager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    iv_edit.isEnabled = !detectViewModel.detectListLD.value.isNullOrEmpty()
                } else {
                    iv_edit.isEnabled = !reportViewModel.reportListLD.value.isNullOrEmpty()
                }
            }
        })
        TabLayoutMediator(tab_layout, view_pager2) { tab, position ->
            tab.setText(if (position == 0) R.string.app_detection else R.string.app_report)
        }.attach()
    }

    override fun initData() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDetectCreate(event: HouseReportAddEvent) {
        view_pager2.currentItem = 1
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_back -> finish()
            iv_edit -> {
                tabViewModel.isEditModeLD.value = true
            }
            iv_add -> {
                val newIntent = Intent(this, DetectAddActivity::class.java)
                newIntent.putExtra(ExtraKeyConfig.IS_TC007, intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false))
                startActivity(newIntent)
            }
            iv_exit_edit -> {
                tabViewModel.isEditModeLD.value = false
            }
        }
    }

    private class ViewPagerAdapter(val activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putBoolean(ExtraKeyConfig.IS_TC007, activity.intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false))
            val fragment = if (position == 0) DetectListFragment() else ReportListFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}