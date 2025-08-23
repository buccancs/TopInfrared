package com.topdon.module.thermal.ir.activity

import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.socket.WebSocketProxy
import com.topdon.lib.core.tools.DeviceTools
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.event.CorrectionFinishEvent
import kotlinx.android.synthetic.main.activity_ir_correction_two.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@Route(path = RouterConfig.IR_CORRECTION_TWO)
class IRCorrectionTwoActivity : BaseActivity() {

    private var isTC007 = false

    override fun initContentView(): Int = R.layout.activity_ir_correction_two

    override fun initView() {
        isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)

        iv_sketch_map.setImageResource(if (isTC007) R.drawable.ic_corrected_tc007 else R.drawable.ic_corrected_line)

        if (if (isTC007) WebSocketProxy.getInstance().isTC007Connect() else DeviceTools.isConnect()) {
            tv_correction.setBackgroundResource(R.drawable.bg_corners05_solid_theme)
        } else {
            tv_correction.setBackgroundResource(R.drawable.bg_corners05_solid_50_theme)
        }

        tv_correction.setOnClickListener {
            if (if (isTC007) WebSocketProxy.getInstance().isTC007Connect() else DeviceTools.isConnect()) {
                if (isTC007) {
                    ARouter.getInstance().build(RouterConfig.IR_CORRECTION_07).navigation(this)
                } else {
                    if (false){
                        ARouter.getInstance().build(RouterConfig.IR_CORRECTION_THREE_LITE).navigation(this)
                    } else if (false) {
                        ARouter.getInstance().build(RouterConfig.IR_HIK_CORRECT_THREE).navigation(this)
                    } else{
                        startActivity(Intent(this, IRCorrectionThreeActivity::class.java))
                    }
                }
            }
        }
    }

    override fun connected() {
        if (!isTC007) {
            tv_correction.setBackgroundResource(R.drawable.bg_corners05_solid_theme)
        }
    }

    override fun disConnected() {
        if (!isTC007) {
            tv_correction.setBackgroundResource(R.drawable.bg_corners05_solid_50_theme)
        }
    }

    override fun onSocketConnected(isTS004: Boolean) {
        if (isTC007 && !isTS004) {
            tv_correction.setBackgroundResource(R.drawable.bg_corners05_solid_theme)
        }
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        if (isTC007 && !isTS004) {
            tv_correction.setBackgroundResource(R.drawable.bg_corners05_solid_50_theme)
        }
    }

    override fun initData() {}

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun finishCorrection(event: CorrectionFinishEvent) {
        finish()
    }
}