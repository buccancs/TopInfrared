package com.topdon.tc004.activity

import android.text.method.LinkMovementMethod
import androidx.core.view.isVisible
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.SpanBuilder
import com.topdon.lib.core.utils.Constants
import com.topdon.tc004.R
import kotlinx.android.synthetic.main.activity_connect_tips.*

@Route(path = RouterConfig.IR_CONNECT_TIPS)
class ConnectTipsActivity : BaseActivity() {

    private var isTC007 = false

    override fun initContentView(): Int = R.layout.activity_connect_tips

    override fun initView() {
        isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)

        title_view.setTitleText(if (isTC007) "TC007" else "TS004")

        if (isTC007) {
            tv_tips1.setText(R.string.tc007_connect_tips1)
        } else {
            tv_tips1.text = SpanBuilder(getString(R.string.ts004_guide_text2))
                .appendDrawable(this, R.drawable.svg_connect_tips_m, SizeUtils.sp2px(18f))
        }
        iv_tips1.setImageResource(if (isTC007) R.drawable.ic_connect_tips_tc007_1 else R.drawable.ic_connect_tips_ts004_1)

        tv_tips2.setText(if (isTC007) R.string.tc007_connect_tips2 else R.string.ts004_guide_text3)
        iv_tips2.setImageResource(if (isTC007) R.drawable.ic_connect_tips_tc007_2 else R.drawable.ic_connect_tips_ts004_2)

        tv_tips3.isVisible = isTC007
        iv_tips3.isVisible = isTC007
        tv_tips4.isVisible = isTC007
        iv_tips4.isVisible = isTC007

        tv_more_help.movementMethod = LinkMovementMethod.getInstance()
        tv_more_help.isVisible = !isTC007
        tv_more_help.text = SpanBuilder().appendColorAndClick(
            this,
            R.string.ts004_guide_text7,
            getString(R.string.ts004_guide_text6),
            0xcc06aaff.toInt(),
            true
        ) {
            ARouter.getInstance()
                .build(RouterConfig.IR_MORE_HELP)
                .withInt(Constants.SETTING_CONNECTION_TYPE, Constants.SETTING_CONNECTION)
                .navigation(this)
        }
    }

    override fun initData() {
    }
}