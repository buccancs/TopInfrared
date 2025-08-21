package com.topdon.module.thermal.ir.activity

import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.libcom.bean.SaveSettingBean

/**
 * 英菲 插件式热成像统一父 Activity，抽取相同逻辑到此处.
 *
 * Created by LCG on 2023/12/6.
 */
abstract class BaseIRActivity : BaseActivity() {
    /**
     * 保存设置开关影响的相关配置项.
     */
    protected val saveSetBean = SaveSettingBean()
}