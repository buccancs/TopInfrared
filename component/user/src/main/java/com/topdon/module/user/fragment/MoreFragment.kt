package com.topdon.module.user.fragment

import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import androidx.core.view.isVisible
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.module.user.R
import kotlinx.android.synthetic.main.fragment_more.*

/**
 * 插件式 "更多" 页面
 *
 * 需要传递参数：
 * - [ExtraKeyConfig.IS_TC007] - 当前设备是否为 TC007
 */
@Route(path = RouterConfig.TC_MORE)
class MoreFragment : BaseFragment(), View.OnClickListener {

    /**
     * TC001插件式 "更多" 页面
     */

    override fun initContentView() = R.layout.fragment_more

    override fun initView() {
        setting_item_model.setOnClickListener(this)//温度修正
        setting_item_correction.setOnClickListener(this)//图像校正
        setting_item_unit.setOnClickListener(this)//温度单温

        // Hide non-TC001 specific settings
        setting_version.isVisible = false
        setting_device_information.isVisible = false
        setting_reset.isVisible = false
        setting_item_dual.isVisible = false

        setting_item_auto_show.isChecked = SharedManager.isConnectAutoOpen
        setting_item_auto_show.setOnCheckedChangeListener { _, isChecked ->
            SharedManager.isConnectAutoOpen = isChecked
        }

        setting_item_config_select.isChecked = SaveSettingUtil.isSaveSetting
        setting_item_config_select.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                TipDialog.Builder(requireContext())
                    .setMessage(R.string.save_setting_tips)
                    .setPositiveListener(R.string.app_ok) {
                        SaveSettingUtil.isSaveSetting = true
                    }
                    .setCancelListener(R.string.app_cancel) {
                        setting_item_config_select.isChecked = false
                    }
                    .setCanceled(false)
                    .create().show()
            } else {
                SaveSettingUtil.reset()
                SaveSettingUtil.isSaveSetting = false
            }
        }
    }

    override fun initData() {
    }

    override fun connected() {
        // TC001 device connected - no special handling needed
    }

    override fun disConnected() {
        // TC001 device disconnected
    }

    override fun onSocketConnected(isTS004: Boolean) {
        // TC001 only supports USB connection, no socket connection
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        // TC001 only supports USB connection, no socket connection
    }

    override fun onClick(v: View?) {
       when(v){
           setting_item_model -> {//温度修正
               ARouter.getInstance().build(RouterConfig.IR_SETTING).withBoolean(ExtraKeyConfig.IS_TC007, false).navigation(requireContext())
           }
           setting_item_unit -> {//温度单位
               ARouter.getInstance().build(RouterConfig.UNIT).navigation(requireContext())
           }
           setting_item_correction->{//图像校正
               ARouter.getInstance().build(RouterConfig.IR_CORRECTION).withBoolean(ExtraKeyConfig.IS_TC007, false).navigation(requireContext())
           }
       }
    }

}