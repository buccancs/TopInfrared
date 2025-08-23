package com.topdon.module.thermal.activity

import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.BarUtils
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.ui.dialog.MonitorSelectDialog
import com.topdon.module.thermal.R
import com.topdon.module.thermal.fragment.event.ThermalActionEvent
import kotlinx.android.synthetic.main.activity_monitor.*
import org.greenrobot.eventbus.EventBus
import java.util.*

@Route(path = RouterConfig.THERMAL_MONITOR)
class MonitorActivity : BaseActivity(), View.OnClickListener {

    companion object {
        const val STATS_START = 101
        const val STATS_MONITOR = 102
        const val STATS_FINISH = 103
    }

    var MONITOR_ACTION = STATS_START

    private var selectType = 1
    private var selectIndex: ArrayList<Int> = arrayListOf()

    override fun initContentView() = R.layout.activity_monitor

    override fun initView() {
        setTitleText(R.string.main_thermal_motion)
        mToolBar!!.setBackgroundColor(blackColor)
        BarUtils.setStatusBarColor(this, blackColor)
        BarUtils.setNavBarColor(window, blackColor)
        motion_log_btn.setOnClickListener(this)
        motion_btn.setOnClickListener(this)
        motion_start_btn.setOnClickListener(this)
    }

    override fun initData() {

    }

    override fun onClick(v: View?) {
        when (v) {
            motion_log_btn -> {
                ARouter.getInstance().build(RouterConfig.THERMAL_LOG_MP_CHART).navigation(this)
            }
            motion_btn -> {
                MonitorSelectDialog.Builder(this)
                    .setTitle("请选择监控类型")
                    .setPositiveListener(object : MonitorSelectDialog.OnClickListener {
                        override fun onClick(select: Int) {
                            updateUI()
                            when (select) {
                                1 -> EventBus.getDefault().post(ThermalActionEvent(action = 2001))
                                2 -> EventBus.getDefault().post(ThermalActionEvent(action = 2002))
                                else -> EventBus.getDefault()
                                    .post(ThermalActionEvent(action = 2003))
                            }
                        }

                    })
                    .setCancelListener(R.string.app_cancel)
                    .create().show()
            }
            motion_start_btn -> {
                ARouter.getInstance().build(RouterConfig.MONITOR_CHART)
                    .withInt("type", selectType)
                    .withIntegerArrayList("select", selectIndex)
                    .navigation(this)
                finish()
            }
        }
    }

    fun select(selectType: Int, selectIndex: ArrayList<Int>) {
        motion_start_btn.isEnabled = true
        this.selectType = selectType
        this.selectIndex = selectIndex
    }

    private fun updateUI() {
        motion_start_btn.isEnabled = false
        motion_start_btn.visibility = View.VISIBLE
        motion_log_btn.visibility = View.GONE
        motion_btn.visibility = View.GONE
    }

    fun updateTime(time: Long) {
        val ss = time % 60
        val mm = time / 60 % 60
        val ssStr = String.format("%02d", ss)
        val mmStr = String.format("%02d", mm)
        motion_start_btn.text = "${mmStr}:${ssStr}"
    }

}