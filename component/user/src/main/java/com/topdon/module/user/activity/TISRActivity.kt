package com.topdon.module.user.activity

import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.bean.event.SocketMsgEvent
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.repository.*
import com.topdon.lib.core.socket.SocketCmdUtil
import com.topdon.lib.core.utils.WsCmdConstants
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.module.user.R
import kotlinx.android.synthetic.main.activity_tisr.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

@Route(path = RouterConfig.TISR)
class TISRActivity : BaseActivity(){
    override fun initContentView() = R.layout.activity_tisr

    override fun initView() {
        title_view.setTitleText("TISR")
        setting_item_tisr_select.isChecked = SharedManager.is04TISR
        setting_item_tisr_select.setOnCheckedChangeListener { _, isChecked ->
            updateTISR(if(isChecked) 1 else 0)
            SharedManager.is04TISR = isChecked
        }
    }

    override fun initData() {
        lifecycleScope.launch {
            val tisrBean = TS004Repository.getTISR()
            if(tisrBean?.isSuccess()!!){
                val isTISR = tisrBean.data?.enable!! == 1
                setting_item_tisr_select.isChecked = isTISR
                SharedManager.is04TISR = isTISR
            }else{
                TToast.shortToast(this@TISRActivity, R.string.operation_failed_tips)
            }
        }
    }

    private fun updateTISR(state: Int) {
        lifecycleScope.launch {
            val isSuccess= TS004Repository.setTISR(state)
            if(isSuccess){
            }else{
                TToast.shortToast(this@TISRActivity, R.string.operation_failed_tips)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSocketMsgEvent(event: SocketMsgEvent) {
        when(SocketCmdUtil.getCmdResponse(event.text)){
            WsCmdConstants.AR_COMMAND_TISR_GET -> {
                try {
                    val webSocketIp = SocketCmdUtil.getIpResponse(event.text)
                    if(webSocketIp == WsCmdConstants.AR_COMMAND_IP){
                        val data: JSONObject = JSONObject(event.text).getJSONObject("data")
                        val state: Int = data.getInt("state")
                        val isTISR = state == 1
                        setting_item_tisr_select.isChecked = isTISR
                        SharedManager.is04TISR = isTISR
                    }
                } catch (_: Exception) {

                }
            }
        }
    }

}