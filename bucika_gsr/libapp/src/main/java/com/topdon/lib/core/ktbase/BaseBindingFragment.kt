package com.topdon.lib.core.ktbase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.topdon.lib.core.bean.event.SocketStateEvent
import com.topdon.lib.core.bean.event.device.DeviceConnectEvent
import com.topdon.lib.core.dialog.LoadingDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

abstract class BaseBindingFragment<B : ViewDataBinding> : Fragment() {

    private var _binding: B? = null
    
    protected val binding: B get() = _binding!!

    @LayoutRes
    protected abstract fun initContentLayoutId(): Int
    
    protected abstract fun initView(savedInstanceState: Bundle?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DataBindingUtil.inflate(inflater, initContentLayoutId(), container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        _binding?.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        initView(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
        _binding = null
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
    
    fun showLoadingDialog(@StringRes resId: Int) {
        showLoadingDialog(getString(resId))
    }
    
    fun showLoadingDialog(text: CharSequence?) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(requireContext())
        }
        loadingDialog?.setTips(text)
        loadingDialog?.show()
    }
    
    fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }
}