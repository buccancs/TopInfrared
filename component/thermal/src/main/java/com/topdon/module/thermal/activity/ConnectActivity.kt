package com.topdon.module.thermal.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.DeviceTools
import com.topdon.module.thermal.R
import kotlinx.android.synthetic.main.activity_connect.*

@Route(path = RouterConfig.CONNECT)
class ConnectActivity : BaseActivity() {

    override fun initContentView() = R.layout.activity_connect

    override fun initView() {
        setTitleText(R.string.app_name)
        val device = DeviceTools.isConnect()
        if (device == null) {
            bluetooth_btn.text = getString(R.string.app_no_connect)
        } else {
            bluetooth_btn.text = getString(R.string.app_connect)
        }

    }

    override fun initData() {

    }

}