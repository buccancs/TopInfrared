package com.topdon.tc001

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.elvishew.xlog.XLog
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseViewModelActivity
import com.topdon.tc001.viewmodel.PolicyViewModel
import kotlinx.android.synthetic.main.activity_policy.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = RouterConfig.POLICY)
class PolicyActivity : BaseViewModelActivity<PolicyViewModel>() {

    private val mHandler = Handler(Looper.getMainLooper())

    companion object {
        const val KEY_THEME_TYPE = "key_theme_type"
        const val KEY_USE_TYPE = "key_use_type"
    }

    private var themeType = 1
    private var themeStr = ""
    private var reloadCount = 1
    private var keyUseType = 0

    override fun providerVMClass() = PolicyViewModel::class.java

    override fun initContentView() = R.layout.activity_policy

    override fun initView() {
        if (intent.hasExtra(KEY_THEME_TYPE)) {
            themeType = intent.getIntExtra(KEY_THEME_TYPE, 1)
        }
        if (intent.hasExtra(KEY_USE_TYPE)) {
            keyUseType = intent.getIntExtra(KEY_USE_TYPE, 0)
        }
        themeStr = when (themeType) {
            1 -> getString(R.string.user_services_agreement)
            2 -> getString(R.string.privacy_policy)
            3 -> getString(R.string.third_party_components)
            else -> getString(R.string.user_services_agreement)
        }

        title_view.setTitleText(themeStr)
        viewModel.htmlViewData.observe(this) {
            dismissCameraLoading()
            if (it.action == 1) {
                initWeb(it.body ?: "")
            } else {
                loadHttp(policy_web)
                delayShowWebView()
            }
        }
        if (keyUseType != 0) {
            loadHttpWhenNotInit(policy_web)
            delayShowWebView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun delayShowWebView() {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(200)
            launch(Dispatchers.Main) {
                policy_web.visibility = View.VISIBLE
            }
        }
    }

    override fun initData() {
        if (keyUseType == 0) {
            showCameraLoading()
            viewModel.getUrl(themeType)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWeb(url: String) {
        policy_web.visibility = View.INVISIBLE
        val webSettings: WebSettings = policy_web.settings
        webSettings.javaScriptEnabled = true

        policy_web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.w("123", "onPageFinished url: $url")
            }
        }

        policy_web.webChromeClient = object : WebChromeClient() {

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (title!!.contains("404") && reloadCount > 0) {
                    loadHttp(view!!)
                    delayShowWebView()
                } else {
                    mHandler.postDelayed({
                        policy_web.visibility = View.VISIBLE
                    }, 200)
                }
            }

        }

        policy_web.settings.defaultTextEncodingName = "utf-8"
        policy_web.loadDataWithBaseURL(null, url, "text/html", "utf-8", null)

    }

    fun getHtmlData(htmlBody: String, fontColor: String, backgroundColor: String): String {
        val head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width: 100%; width:100%; height:auto;}video{max-width: 100%; width:100%; height:auto;}*{margin:0px;}body{font-size:16px;color: ${fontColor}; background-color: ${backgroundColor};}</style>" + "</head>"
        return "<html>$head<body>$htmlBody</body></html>"
    }

    override fun httpErrorTip(text: String, requestUrl: String) {
        XLog.w("声明接口异常,打开默认链接")
        loadHttp(policy_web)
        delayShowWebView()
    }

    fun loadHttpWhenNotInit(view: WebView) {
        reloadCount--
        when (themeType) {
            1 -> {
                view.loadUrl("https://plat.topdon.com/topdon-plat/out-user/baseinfo/template/getHtmlContentById?softCode=${BaseApplication.instance.getSoftWareCode()}&language=1&type=21")
            }

            2 -> {
                view.loadUrl("https://plat.topdon.com/topdon-plat/out-user/baseinfo/template/getHtmlContentById?softCode=${BaseApplication.instance.getSoftWareCode()}&language=1&type=22")
            }

            3 -> {
                view.loadUrl("file:///android_asset/web/third_statement.html")
            }
        }
    }

    fun loadHttp(view: WebView) {
        reloadCount--
        when (themeType) {
            1 -> {
                if (BaseApplication.instance.isDomestic()) {
                    view.loadUrl("file:///android_asset/web/services_agreement_default_inside_china.html")
                } else {
                    view.loadUrl("file:///android_asset/web/services_agreement_default.html")
                }
            }

            2 -> {
                if (BaseApplication.instance.isDomestic()) {
                    view.loadUrl("file:///android_asset/web/privacy_default_inside_china.html")
                } else {
                    view.loadUrl("file:///android_asset/web/privacy_default.html")
                }
            }

            3 -> {
                view.loadUrl("file:///android_asset/web/third_statement.html")
            }
        }
    }
}