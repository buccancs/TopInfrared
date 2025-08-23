package com.topdon.lib.core.ktbase

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.topdon.lib.core.R
import com.topdon.lib.core.dialog.MsgDialog
import kotlinx.coroutines.TimeoutCancellationException
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseViewModelActivity<VM : BaseViewModel> : BaseActivity() {

    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        initVM()
        super.onCreate(savedInstanceState)
    }

    private fun initVM() {
        providerVMClass().let {
            viewModel = ViewModelProvider(this).get(it)
            lifecycle.addObserver(viewModel)
        }
    }

    abstract fun providerVMClass(): Class<VM>

    protected fun requestError(it: Exception?) {
        it?.run {
            when (it) {
                is TimeoutCancellationException -> httpErrorTip(getString(R.string.http_time_out), "")
                is CancellationException -> Log.d("${TAG}--->接口请求取消", it.message.toString())
                else -> httpErrorTip(getString(R.string.http_code_z5004), "")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(viewModel)
    }

    open fun httpErrorTip(text: String, requestUrl: String) {
        MsgDialog.Builder(this)
            .setMessage(text)
            .setImg(R.drawable.ic_tip_error_svg)
            .create().show()
    }
}
