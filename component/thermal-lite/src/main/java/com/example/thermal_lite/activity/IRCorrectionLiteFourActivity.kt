package com.example.thermal_lite.activity

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ToastUtils
import com.example.thermal_lite.R
import com.example.thermal_lite.fragment.IRMonitorLiteFragment
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.view.TitleView
import com.topdon.module.thermal.ir.event.CorrectionFinishEvent
import com.topdon.module.thermal.ir.view.TimeDownView
import kotlinx.android.synthetic.main.activity_ir_thermal_lite.time_down_view
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

@Route(path = RouterConfig.IR_CORRECTION_FOUR_LITE)
class IRCorrectionLiteFourActivity : BaseActivity() {

    val time = 60
    var result = false

    override fun initContentView(): Int = R.layout.activity_ir_correction_lite_four

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val titleView: TitleView = findViewById(R.id.title_view)
        titleView.setLeftClickListener {
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
            IRMonitorLiteFragment()
        } else {
            supportFragmentManager.findFragmentById(R.id.fragment_container_view) as IRMonitorLiteFragment
        }
        lifecycleScope.launch {
            delay(1000)
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container_view, irFragment)
                    .commit()
            }
        }

        time_down_view.postDelayed({
            if (time_down_view.downTimeWatcher == null){
                time_down_view.setOnTimeDownListener(object : TimeDownView.DownTimeWatcher{
                    override fun onTime(num: Int) {
                        if (num == 35){
                            lifecycleScope.launch(Dispatchers.IO) {
                               result = irFragment.autoStart()
                            }
                        }
                    }
                    override fun onLastTime(num: Int) {

                    }
                    override fun onLastTimeFinish(num: Int) {
                        try {
                            if (!result){
                                ToastUtils.showShort("标定保存失败，请重新标定")
                                return
                            }
                            if (!this@IRCorrectionLiteFourActivity.isFinishing){
                                TipDialog.Builder(this@IRCorrectionLiteFourActivity)
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