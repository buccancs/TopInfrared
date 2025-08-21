package com.topdon.module.thermal.ir.fragment

import android.content.Intent
import androidx.core.view.isVisible
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.lib.core.socket.WebSocketProxy
import com.topdon.lib.core.tools.DeviceTools
import com.topdon.lib.core.tools.ToastTools
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.activity.IRMonitorActivity
import kotlinx.android.synthetic.main.fragment_ir_monitor_capture.*

/**
 * 温度监控-实时（即生成温度监控）.
 *
 * 需要传递参数：
 * - [ExtraKeyConfig.IS_TC007] - 当前设备是否为 TC007
 */
class IRMonitorCaptureFragment : BaseFragment() {

    /**
     * 从上一界面传递过来的，当前是否为 TC007 设备类型.
     * true-TC007 false-其他插件式设备
     */
    private var isTC007 = false

    override fun initContentView(): Int = R.layout.fragment_ir_monitor_capture

    override fun initView() {
        isTC007 = arguments?.getBoolean(ExtraKeyConfig.IS_TC007, false) ?: false
        animation_view.setAnimation(if (isTC007) "TC007AnimationJSON.json" else "TDAnimationJSON.json")

        view_start.setOnClickListener {
            if (isTC007) {
                if (WebSocketProxy.getInstance().isTC007Connect()) {
                    ARouter.getInstance().build(RouterConfig.IR_MONITOR_CAPTURE_07).navigation(requireContext())
                } else {
                    ToastTools.showShort(R.string.device_connect_tip)
                }
            } else {
                if (DeviceTools.isConnect()) {
                    if (DeviceTools.isTC001LiteConnect()){
                        ARouter.getInstance().build(RouterConfig.IR_THERMAL_MONITOR_LITE).navigation(requireContext())
                    } else if (DeviceTools.isHikConnect()) {
                        ARouter.getInstance().build(RouterConfig.IR_HIK_MONITOR_CAPTURE1).navigation(requireContext())
                    } else{
                        startActivity(Intent(requireContext(), IRMonitorActivity::class.java))
                    }
                } else {
                    ToastTools.showShort(R.string.device_connect_tip)
                }
            }
        }

        refreshUI(if (isTC007) WebSocketProxy.getInstance().isTC007Connect() else DeviceTools.isConnect())
    }

    override fun onResume() {
        super.onResume()
        refreshUI(if (isTC007) WebSocketProxy.getInstance().isTC007Connect() else DeviceTools.isConnect())
    }

    override fun initData() {
    }

    /**
     * 刷新连接状态
     */
    private fun refreshUI(isConnect: Boolean) {
        animation_view.isVisible = !isConnect
        iv_icon.isVisible = isConnect
        view_start.isVisible = isConnect
        tv_start.isVisible = isConnect
    }

    override fun connected() {
        if (!isTC007) {
            refreshUI(true)
        }
    }

    override fun disConnected() {
        if (!isTC007) {
            refreshUI(false)
        }
    }

    override fun onSocketConnected(isTS004: Boolean) {
        if (isTC007 && !isTS004) {
            refreshUI(true)
        }
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        if (isTC007 && !isTS004) {
            refreshUI(false)
        }
    }
}