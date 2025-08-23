package com.topdon.module.thermal.ir.activity

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.event.ManualFinishBean
import kotlinx.android.synthetic.main.activity_manual_step1.tv_manual
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConfig.MANUAL_START)
class ManualStep1Activity : BaseActivity() {

    override fun initContentView(): Int = R.layout.activity_manual_step1

    override fun initView() {
        tv_manual.setOnClickListener {
            startActivity(Intent(this, ManualStep2Activity::class.java))
        }
    }

    override fun initData() {
    }

    override fun disConnected() {
        super.disConnected()
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onManualFinishBean(manualFinishBean: ManualFinishBean) {
        finish()
    }
}