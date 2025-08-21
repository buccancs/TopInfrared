package com.topdon.module.thermal.ir.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.event.CorrectionFinishEvent
import com.topdon.module.thermal.ir.fragment.IRCorrectionFragment
import com.topdon.module.thermal.ir.view.TimeDownView
import kotlinx.android.synthetic.main.activity_ir_correction_four.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

/**
 *
 * 锅盖矫正
 * @author: CaiSongL
 * @date: 2023/8/4 9:06
 */
@Route(path = RouterConfig.IR_CORRECTION_FOUR)
class IRCorrectionFourActivity : BaseActivity() {

    val time = 60

    override fun initContentView(): Int = R.layout.activity_ir_correction_four

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title_view.setLeftClickListener {
            TipDialog.Builder(this)
                .setTitleMessage(getString(R.string.app_tip))
                .setMessage(R.string.tips_cancel_correction)
                .setPositiveListener(R.string.app_yes) {
                    EventBus.getDefault().post(CorrectionFinishEvent())
                    finish()
                }.setCancelListener(R.string.app_no){
                }
                .create().show()
        }


        val irFragment = if (savedInstanceState == null) {
            IRCorrectionFragment()
        } else {
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as IRCorrectionFragment
        }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container_view, irFragment)
                .commit()
        }


        time_down_view.postDelayed({
            //开始矫正
            if (time_down_view.downTimeWatcher == null){
                time_down_view.setOnTimeDownListener(object : TimeDownView.DownTimeWatcher{
                    override fun onTime(num: Int) {
                        if (num == 50){
                            lifecycleScope.launch(Dispatchers.IO) {
                                irFragment.autoStart()
                            }
                        }
                    }
                    override fun onLastTime(num: Int) {

                    }
                    override fun onLastTimeFinish(num: Int) {
                        try {
                            if (!this@IRCorrectionFourActivity.isFinishing){
                                TipDialog.Builder(this@IRCorrectionFourActivity)
                                    .setMessage(R.string.correction_complete)
                                    .setPositiveListener(R.string.app_confirm) {
                                        EventBus.getDefault().post(CorrectionFinishEvent())
                                        finish()
                                    }
                                    .create().show()
                            }
                        }catch (e : Exception){

                        }
                    }
                })
            }
            time_down_view.downSecond(time,false)
        },2000)
    }

    override fun initView() {
    }

    override fun onBackPressed() {
        TipDialog.Builder(this)
            .setTitleMessage(getString(R.string.app_tip))
            .setMessage(R.string.tips_cancel_correction)
            .setPositiveListener(R.string.app_yes) {
                EventBus.getDefault().post(CorrectionFinishEvent())
                super.onBackPressed()
            }.setCancelListener(R.string.app_no){
            }
            .create().show()
    }

    override fun disConnected() {
        super.disConnected()
        time_down_view.cancel()
        EventBus.getDefault().post(CorrectionFinishEvent())
        finish()
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().post(CorrectionFinishEvent())
        finish()
    }

    override fun initData() {

    }

    override fun onDestroy() {
        super.onDestroy()
        time_down_view.cancel()
    }
}