package com.topdon.tc001

import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.CheckDoubleClick
import com.topdon.lib.core.utils.CommUtils
import com.topdon.lms.sdk.LMS
import com.topdon.lms.sdk.UrlConstant
import com.topdon.tc001.utils.AppVersionUtil
import com.topdon.tc001.utils.VersionUtils
import kotlinx.android.synthetic.main.activity_version.*
import kotlinx.android.synthetic.main.item_new_version.cl_new_version
import kotlinx.android.synthetic.main.item_new_version.tv_new_version
import java.util.*

@Route(path = RouterConfig.VERSION)
class VersionActivity : BaseActivity(), View.OnClickListener {
    override fun initContentView() = R.layout.activity_version

    override fun initView() {
        version_code_text.text = "${getString(R.string.set_version)}V${VersionUtils.getCodeStr(this)}"
        val year = Calendar.getInstance().get(Calendar.YEAR)
        version_year_txt.text = getString(R.string.version_year, "2023-$year")
        version_statement_private_txt.setOnClickListener(this)
        version_statement_policy_txt.setOnClickListener(this)
        version_statement_copyright_txt.setOnClickListener(this)

        setting_version_img.setOnClickListener {
            if (BuildConfig.DEBUG && CheckDoubleClick.isFastDoubleClick()) {
                LMS.getInstance().activityEnv()
            }
        }
        cl_new_version.setOnClickListener {
            if (!CheckDoubleClick.isFastDoubleClick()) {
                checkAppVersion(true)
            }
        }
        setting_version_txt.text = CommUtils.getAppName()
    }

    override fun initData() {
        if (BaseApplication.instance.isDomestic()) {
            checkAppVersion(false)
        }
    }

    override fun onResume() {
        super.onResume()
        SharedManager.setBaseHost(UrlConstant.BASE_URL)
    }

    override fun onClick(v: View?) {
        when (v) {
            version_statement_private_txt -> {
                ARouter.getInstance().build(RouterConfig.POLICY)
                    .withInt(PolicyActivity.KEY_THEME_TYPE, 1)
                    .navigation(this)
            }
            version_statement_policy_txt -> {
                ARouter.getInstance().build(RouterConfig.POLICY)
                    .withInt(PolicyActivity.KEY_THEME_TYPE, 2)
                    .navigation(this)
            }
            version_statement_copyright_txt -> {
                ARouter.getInstance().build(RouterConfig.POLICY)
                    .withInt(PolicyActivity.KEY_THEME_TYPE, 3)
                    .navigation(this)
            }
        }
    }

    private var appVersionUtil: AppVersionUtil?=null
    private fun checkAppVersion(isShow: Boolean) {
        if (appVersionUtil == null) {
            appVersionUtil = AppVersionUtil(this, object : AppVersionUtil.DotIsShowListener {
                override fun isShow(show: Boolean) {
                    cl_new_version.visibility = View.VISIBLE
                }

                override fun version(version: String) {
                    tv_new_version.text = "$version"
                }
            })
        }
        appVersionUtil?.checkVersion(isShow)
    }

}