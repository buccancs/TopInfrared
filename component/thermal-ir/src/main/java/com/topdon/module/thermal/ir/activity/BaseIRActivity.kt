package com.topdon.module.thermal.ir.activity

import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.libcom.bean.SaveSettingBean

abstract class BaseIRActivity : BaseActivity() {
    
    protected val saveSetBean = SaveSettingBean()
}