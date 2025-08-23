package com.topdon.module.user.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.repository.ProductBean
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.lib.core.repository.TS004Repository
import com.topdon.lms.sdk.utils.TLog
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.module.user.R
import kotlinx.android.synthetic.main.activity_device_details.*
import kotlinx.coroutines.launch

@Route(path = RouterConfig.DEVICE_INFORMATION)
class DeviceDetailsActivity : BaseActivity(), View.OnClickListener {

    private var isTC007 = false

    override fun initContentView() = R.layout.activity_device_details

    override fun initView() {
        isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)
        cl_layout_copy.setOnClickListener(this)
    }

    override fun initData() {
        getDeviceDetails()
    }

    private fun getDeviceDetails() {
        lifecycleScope.launch {
            if (isTC007) {
                val productBean: ProductBean? = TC007Repository.getProductInfo()
                if (productBean == null) {
                    TToast.shortToast(this@DeviceDetailsActivity, R.string.operation_failed_tips)
                } else {
                    tv_sn_value.text = productBean.ProductSN
                    tv_device_model_value.text = productBean.ProductName
                }
            } else {
                val deviceDetailsBean = TS004Repository.getDeviceInfo()
                if (deviceDetailsBean?.isSuccess()!!) {
                    TLog.d("ts004-->response", "${deviceDetailsBean.data}")
                    tv_sn_value.text = deviceDetailsBean.data!!.sn
                    tv_device_model_value.text = deviceDetailsBean.data!!.model
                } else {
                    TToast.shortToast(this@DeviceDetailsActivity, R.string.operation_failed_tips)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            cl_layout_copy -> {
                val text = "${tv_sn.text}:${tv_sn_value.text}  ${tv_device_model.text}:${tv_device_model_value.text}"
                val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                val mClipData = ClipData.newPlainText("text", text)
                cm!!.setPrimaryClip(mClipData)
                TToast.shortToast(this@DeviceDetailsActivity, R.string.ts004_copy_success)
            }
        }
    }

}