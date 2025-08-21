package com.topdon.module.thermal.ir.fragment

import android.content.Intent
import android.view.View
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.house.activity.HouseHomeActivity
import com.topdon.lib.core.bean.event.WinterClickEvent
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.lib.core.socket.WebSocketProxy
import com.topdon.lib.core.tools.DeviceTools
import com.topdon.lms.sdk.UrlConstant
import com.topdon.lms.sdk.utils.LanguageUtil
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.activity.IRThermalNightActivity
import com.topdon.module.thermal.ir.activity.IRThermalPlusActivity
import com.topdon.module.thermal.ir.activity.MonitoryHomeActivity
import kotlinx.android.synthetic.main.fragment_ability.*
import org.greenrobot.eventbus.EventBus

/**
 * 功能 Tab 页
 *
 * 需要传递参数：
 * - [ExtraKeyConfig.IS_TC007] - 当前设备是否为 TC007（不使用，透传）
 */
class AbilityFragment : BaseFragment(), View.OnClickListener {
    private var mIsTC007 = false

    override fun initContentView() = R.layout.fragment_ability

    override fun initView() {
        mIsTC007 = arguments?.getBoolean(ExtraKeyConfig.IS_TC007, false) ?: false
        iv_winter.setOnClickListener(this)
        view_monitory.setOnClickListener(this)
        view_house.setOnClickListener(this)
        view_car.setOnClickListener(this)
    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_winter -> {//冬季特辑入口
                SharedManager.hasClickWinter = true
                EventBus.getDefault().post(WinterClickEvent())
                val url = if (UrlConstant.BASE_URL == "https://api.topdon.com/") {
                    "https://app.topdon.com/h5/share/#/detectionGuidanceIndex?showHeader=1&" +
                            "languageId=${LanguageUtil.getLanguageId(requireContext())}"
                } else {
                    "http://172.16.66.77:8081/#/detectionGuidanceIndex?languageId=1&showHeader=1"
                }
                ARouter.getInstance().build(RouterConfig.WEB_VIEW)
                    .withString(ExtraKeyConfig.URL, url)
                    .navigation(requireContext())
            }
            view_monitory -> {//温度监控
                val intent = Intent(requireContext(), MonitoryHomeActivity::class.java)
                intent.putExtra(ExtraKeyConfig.IS_TC007, mIsTC007)
                startActivity(intent)
            }

            view_house -> {//房屋检测
                val intent = Intent(requireContext(), HouseHomeActivity::class.java)
                intent.putExtra(ExtraKeyConfig.IS_TC007, mIsTC007)
                startActivity(intent)
            }

            view_car -> {//汽车检测
                if (mIsTC007) {
                    if (WebSocketProxy.getInstance().isConnected()) {
                        ARouter.getInstance().build(RouterConfig.IR_THERMAL_07)
                            .withBoolean(ExtraKeyConfig.IS_CAR_DETECT_ENTER, true)
                            .navigation(requireContext())
                    }
                } else {
                    if (DeviceTools.isTC001PlusConnect()) {
                        var intent = Intent(requireContext(), IRThermalPlusActivity::class.java)
                        intent.putExtra(ExtraKeyConfig.IS_CAR_DETECT_ENTER, true)
                        startActivity(intent)
                    } else if (DeviceTools.isTC001LiteConnect()) {
                        ARouter.getInstance().build(RouterConfig.IR_TCLITE)
                            .withBoolean(ExtraKeyConfig.IS_CAR_DETECT_ENTER, true)
                            .navigation(activity)
                    } else if (DeviceTools.isHikConnect()) {
                        ARouter.getInstance().build(RouterConfig.IR_HIK_MAIN)
                            .withBoolean(ExtraKeyConfig.IS_CAR_DETECT_ENTER, true)
                            .navigation(activity)
                    } else if (DeviceTools.isConnect(isSendConnectEvent = false, true)) {
                        var intent = Intent(requireContext(), IRThermalNightActivity::class.java)
                        intent.putExtra(ExtraKeyConfig.IS_CAR_DETECT_ENTER, true)
                        startActivity(intent)
                    } else {
                        TipDialog.Builder(requireContext())
                            .setMessage(R.string.device_connect_tip)
                            .setPositiveListener(R.string.app_confirm)
                            .create().show()
                    }
                }
            }
        }
    }
}