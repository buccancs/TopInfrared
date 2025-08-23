package com.topdon.thermal07.activity

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.lib.core.view.TitleView
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.event.CorrectionFinishEvent
import com.topdon.module.thermal.ir.view.TimeDownView
import com.topdon.tc004.activity.video.PlayFragment
import kotlinx.coroutines.launch
import org.easydarwin.video.Client
import org.greenrobot.eventbus.EventBus

class IR07CorrectionFourActivity : BaseActivity() {

    companion object {
        private const val RTSP_URL = "rtsp://192.168.40.1/stream0"
    }

    val time = 60

    private var isSuccess = true

    private lateinit var time_down_view: TimeDownView

    override fun initContentView(): Int = R.layout.activity_ir_correction_four

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val titleView: TitleView = findViewById(R.id.title_view)
        titleView.setLeftClickListener { showExitDialog() }

        time_down_view = findViewById(R.id.time_down_view)

        if (savedInstanceState == null) {
            val playFragment = PlayFragment.newInstance(RTSP_URL, Client.TRANSTYPE_TCP, 1, null,true)
            supportFragmentManager.beginTransaction().setReorderingAllowed(true).add(R.id.fragment_container_view, playFragment).commit()
        }

        time_down_view.postDelayed({
            if (time_down_view.downTimeWatcher == null){
                time_down_view.setOnTimeDownListener(object : TimeDownView.DownTimeWatcher{
                    override fun onTime(num: Int) {
                        if (num == 20){
                            lifecycleScope.launch {
                                isSuccess = TC007Repository.correction()
                            }
                        }
                    }
                    override fun onLastTime(num: Int) {

                    }
                    override fun onLastTimeFinish(num: Int) {
                        try {
                            if (!this@IR07CorrectionFourActivity.isFinishing){
                                TipDialog.Builder(this@IR07CorrectionFourActivity)
                                    .setMessage(if (isSuccess) R.string.correction_complete else R.string.correction_fail)
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
        showExitDialog()
    }

    private fun showExitDialog() {
        TipDialog.Builder(this)
            .setTitleMessage(getString(R.string.app_tip))
            .setMessage(R.string.tips_cancel_correction)
            .setPositiveListener(R.string.app_yes) {
                exit()
            }.setCancelListener(R.string.app_no){
            }
            .create().show()
    }

    private fun exit() {
        time_down_view.cancel()
        EventBus.getDefault().post(CorrectionFinishEvent())
        finish()
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        if (!isTS004) {
            exit()
        }
    }

    override fun onStop() {
        super.onStop()
        exit()
    }

    override fun initData() {}
}