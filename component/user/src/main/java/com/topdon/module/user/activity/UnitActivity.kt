package com.topdon.module.user.activity

import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.module.user.R
import kotlinx.android.synthetic.main.activity_unit.*

@Route(path = RouterConfig.UNIT)
class UnitActivity : BaseActivity() {

    override fun initContentView() = R.layout.activity_unit

    override fun initView() {
        title_view.setRightClickListener {
            SharedManager.setTemperature(if (iv_degrees_celsius.isVisible) 1 else 0)
            finish()
        }

        iv_degrees_celsius.isVisible = SharedManager.getTemperature() == 1
        iv_fahrenheit.isVisible = SharedManager.getTemperature() == 0

        constraint_degrees_celsius.setOnClickListener {
            iv_degrees_celsius.isVisible = true
            iv_fahrenheit.isVisible = false
        }
        constraint_fahrenheit.setOnClickListener {
            iv_degrees_celsius.isVisible = false
            iv_fahrenheit.isVisible = true
        }
    }

    override fun initData() {

    }

}

