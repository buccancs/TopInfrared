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

/**
 *
 * 锅盖矫正
 * @author: CaiSongL
 * @date: 2023/8/4 9:06
 *
 * 需要传递参数：
 * - [ExtraKeyConfig.IS_TC007] - 当前设备是否为 TC007
 */
@Route(path = RouterConfig.IR_CORRECTION_TWO)
class IRCorrectionTwoActivity : BaseActivity() {

    /**
     * 从上一界面传递过来的，当前是否为 TC007 设备类型.
     * true-TC007 false-其他插件式设备
     */
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
                    if (DeviceTools.isTC001LiteConnect()){
                        ARouter.getInstance().build(RouterConfig.IR_CORRECTION_THREE_LITE).navigation(this)
                    } else if (DeviceTools.isHikConnect()) {
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