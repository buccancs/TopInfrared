package com.topdon.lib.core.ktbase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.topdon.lib.core.R
import com.topdon.lib.core.bean.event.SocketStateEvent
import com.topdon.lib.core.bean.event.device.DeviceConnectEvent
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.dialog.LoadingDialog
import com.topdon.lib.core.tools.AppLanguageUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

abstract class BaseBindingActivity<B : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var binding: B

    @LayoutRes
    protected abstract fun initContentLayoutId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, initContentLayoutId())
        binding.lifecycleOwner = this
        binding.executePendingBindings()

        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(AppLanguageUtils.attachBaseContext(newBase, SharedManager.getLanguage(newBase!!)))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUSBLineStateChange(event: DeviceConnectEvent) {
        if (event.isConnect) {
            connected()
        } else {
            disConnected()
        }
    }
    protected open fun connected() {

    }
    protected open fun disConnected() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSocketConnectState(event: SocketStateEvent) {
        if (event.isConnect) {
            onSocketConnected(event.isTS004)
        } else {
            onSocketDisConnected(event.isTS004)
        }
    }
    protected open fun onSocketConnected(isTS004: Boolean) {

    }
    protected open fun onSocketDisConnected(isTS004: Boolean) {

    }

    private var loadingDialog: LoadingDialog? = null
    
    fun showLoadingDialog(@StringRes resId: Int = R.string.tip_loading) {
        showLoadingDialog(getString(resId))
    }
    
    fun showLoadingDialog(text: CharSequence?) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this)
        }
        loadingDialog?.setTips(text)
        loadingDialog?.show()
    }
    
    fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }

    protected class TakePhotoResult : ActivityResultContract<File, File?>() {
        private lateinit var file: File

        override fun createIntent(context: Context, input: File): Intent {
            file = input
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            return Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): File? = if (resultCode == RESULT_OK) file else null
    }
}