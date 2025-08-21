package com.topdon.module.thermal.ir.activity

import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.ui.dialog.MonitorSelectDialog
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.bean.SelectPositionBean
import com.topdon.module.thermal.ir.event.MonitorSaveEvent
import com.topdon.module.thermal.ir.event.ThermalActionEvent
import kotlinx.android.synthetic.main.activity_ir_monitor.*
import org.greenrobot.eventbus.EventBus

/**
 * 选取区域监听
 */
@Route(path = RouterConfig.IR_THERMAL_MONITOR)
class IRMonitorActivity : BaseActivity(), View.OnClickListener {

    private var selectIndex: SelectPositionBean? = null//选取点

    override fun initContentView() = R.layout.activity_ir_monitor

    override fun initView() {
        motion_btn.setOnClickListener(this)
        motion_start_btn.setOnClickListener(this)
    }

    override fun initData() {

    }

    override fun onClick(v: View?) {
        when (v) {
            motion_btn -> {
                MonitorSelectDialog.Builder(this)
                    .setPositiveListener {
                        updateUI()
                        when (it) {
                            1 -> EventBus.getDefault().post(ThermalActionEvent(action = 2001))
                            2 -> EventBus.getDefault().post(ThermalActionEvent(action = 2002))
                            else -> EventBus.getDefault().post(ThermalActionEvent(action = 2003))
                        }
                    }
                    .create().show()
            }
            motion_start_btn -> {
                if (selectIndex == null) {
                    MonitorSelectDialog.Builder(this)
                        .setPositiveListener {
                            updateUI()
                            when (it) {
                                1 -> EventBus.getDefault().post(ThermalActionEvent(action = 2001))
                                2 -> EventBus.getDefault().post(ThermalActionEvent(action = 2002))
                                else -> EventBus.getDefault().post(ThermalActionEvent(action = 2003))
                            }
                        }
                        .create().show()
                    return
                }
                //开始温度监听
                ARouter.getInstance().build(RouterConfig.IR_MONITOR_CHART)
                    .withParcelable("select", selectIndex)
                    .navigation(this)
                finish()
            }
        }
    }

    fun select(selectIndex: SelectPositionBean?) {
        this.selectIndex = selectIndex
    }

    private fun updateUI() {
        motion_start_btn.visibility = View.VISIBLE
        motion_btn.visibility = View.GONE
    }

    override fun disConnected() {
        super.disConnected()
        finish()
    }


}