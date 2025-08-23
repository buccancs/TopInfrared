package com.topdon.module.user.activity

import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.module.user.R
import kotlinx.android.synthetic.main.activity_auto_save.*

@Route(path = RouterConfig.AUTO_SAVE)
class AutoSaveActivity :BaseActivity(){

    override fun initContentView() = R.layout.activity_auto_save

    override fun initView() {
        setting_item_save_select.isChecked = SharedManager.is04AutoSync
        setting_item_save_select.setOnCheckedChangeListener { _, isChecked ->
            SharedManager.is04AutoSync = isChecked
        }
    }

    override fun initData() {
    }
}