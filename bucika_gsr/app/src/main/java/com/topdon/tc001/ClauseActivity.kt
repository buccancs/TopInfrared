package com.topdon.tc001

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.dialog.TipProgressDialog
import com.topdon.lib.core.utils.CommUtils
import com.topdon.lms.sdk.utils.NetworkUtil
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.tc001.app.App
import com.topdon.tc001.utils.VersionUtils
import kotlinx.android.synthetic.main.activity_clause.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

/**
 * 条款
 */
@Route(path = RouterConfig.CLAUSE)
class ClauseActivity : AppCompatActivity() {

    private lateinit var dialog: TipProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clause)
        initView()
    }


    private fun initView() {
        dialog = TipProgressDialog.Builder(this)
            .setMessage(com.topdon.lib.core.R.string.tip_loading)
            .setCanceleable(false)
            .create()

        val year = Calendar.getInstance().get(Calendar.YEAR)
        clause_year_txt.text = getString(R.string.version_year, "2023-$year")

        clause_agree_btn.setOnClickListener {
            confirmInitApp()
        }
        clause_disagree_btn.setOnClickListener {
            //再次弹框确认是否退出
            TipDialog.Builder(this)
                .setMessage(getString(R.string.privacy_tips))
                .setPositiveListener(R.string.privacy_confirm) {
                    confirmInitApp()
                }
                .setCancelListener(R.string.privacy_cancel) {
                    this.finish()
                }
                .setCanceled(true)
                .create().show()
        }
        val keyUseType = if (BaseApplication.instance.isDomestic()) 1 else 0
        clause_item.setOnClickListener {
            if (!NetworkUtil.isConnected(this)) {
                TToast.shortToast(this, R.string.lms_setting_http_error)
            } else {
                //服务条款
                ARouter.getInstance()
                    .build(RouterConfig.POLICY)
                    .withInt(PolicyActivity.KEY_THEME_TYPE, 1)
                    .withInt(PolicyActivity.KEY_USE_TYPE, keyUseType)
                    .navigation(this)
            }
        }
        clause_item2.setOnClickListener {
            if (!NetworkUtil.isConnected(this)) {
                TToast.shortToast(this, R.string.lms_setting_http_error)
            } else {
                //隐私条款
                ARouter.getInstance()
                    .build(RouterConfig.POLICY)
                    .withInt(PolicyActivity.KEY_THEME_TYPE, 2)
                    .withInt(PolicyActivity.KEY_USE_TYPE, keyUseType)
                    .navigation(this)
            }
        }
        clause_item3.setOnClickListener {
            //第三方
            if (!NetworkUtil.isConnected(this)) {
                TToast.shortToast(this, R.string.lms_setting_http_error)
            } else {
                ARouter.getInstance()
                    .build(RouterConfig.POLICY)
                    .withInt(PolicyActivity.KEY_THEME_TYPE, 3)
                    .withInt(PolicyActivity.KEY_USE_TYPE, keyUseType)
                    .navigation(this)
            }
        }

        if (BaseApplication.instance.isDomestic()) {
            tv_privacy.text = "    ${getString(R.string.privacy_agreement_tips_new, CommUtils.getAppName())}"
            tv_privacy.visibility = View.VISIBLE
            tv_privacy.movementMethod = ScrollingMovementMethod.getInstance()
        }
        tv_welcome.text = getString(R.string.welcome_use_app, CommUtils.getAppName())
        tv_version.text = "${getString(R.string.set_version)}V${VersionUtils.getCodeStr(this)}"
        clause_name.text = CommUtils.getAppName()
    }

    private fun confirmInitApp() {
        lifecycleScope.launch {
            showLoading()
            //初始化
            App.delayInit()
            async(Dispatchers.IO) {
                //等待1000ms 初始化结束
                delay(1000)
                return@async
            }.await().let {
                ARouter.getInstance().build(RouterConfig.MAIN).navigation(this@ClauseActivity)
                SharedManager.setHasShowClause(true)
                dismissLoading()
                finish()
            }
        }
    }

    private fun showLoading() {
        dialog.show()
    }

    private fun dismissLoading() {
        dialog.dismiss()
    }
}