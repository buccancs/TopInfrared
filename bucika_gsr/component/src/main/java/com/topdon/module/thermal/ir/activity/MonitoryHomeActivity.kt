package com.topdon.module.thermal.ir.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.event.MonitorSaveEvent
import com.topdon.module.thermal.ir.fragment.IRMonitorCaptureFragment
import com.topdon.module.thermal.ir.fragment.IRMonitorHistoryFragment
import kotlinx.android.synthetic.main.activity_monitor_home.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 温度监控 Tab 页，包含
 * - 历史 [IRMonitorHistoryFragment]
 * - 实时 [IRMonitorCaptureFragment]
 *
 * 需要传递参数：
 * - [ExtraKeyConfig.IS_TC007] - 当前设备是否为 TC007
 *
 * Created by LCG on 2024/8/20.
 */
class MonitoryHomeActivity : BaseActivity() {
    override fun initContentView(): Int = R.layout.activity_monitor_home

    override fun initView() {
        val isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)
        view_pager2.adapter = ViewPagerAdapter(this, isTC007)
        TabLayoutMediator(tab_layout, view_pager2) { tab, position ->
            tab.setText(if (position == 0) R.string.chart_history else R.string.chart_real_time)
        }.attach()
    }

    override fun initData() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMonitorCreate(event: MonitorSaveEvent) {
        view_pager2.currentItem = 0
    }

    private class ViewPagerAdapter(activity: MonitoryHomeActivity, val isTC007: Boolean) : FragmentStateAdapter(activity) {
        override fun getItemCount() = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) {
                IRMonitorHistoryFragment()
            } else {
                val fragment = IRMonitorCaptureFragment()
                fragment.arguments = Bundle().also { it.putBoolean(ExtraKeyConfig.IS_TC007, isTC007) }
                fragment
            }
        }
    }
}