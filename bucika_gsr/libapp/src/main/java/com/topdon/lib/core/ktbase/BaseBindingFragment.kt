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

/**
 * 使用 DataBinding 的基础 Fragment.
 *
 * 由于 BaseFragment 子类实在太多没法一下子全改完，等全部改完再来优化继承.
 *
 * Created by LCG on 2024/11/5.
 */
abstract class BaseBindingFragment<B : ViewDataBinding> : Fragment() {

    /**
     * 在 [onDestroyView] 要将 binding 置为 null，
     * 而将 binding 声明为可为 null 类型使用太过麻烦，使用该变量做一重包装避免该问题.
     */
    private var _binding: B? = null
    /**
     * 注意：由于 Fragment 存在时间比其视图长，binding 将在 [onDestroyView] 置为 null.
     *
     * 仅可在 [onCreateView] 与 [onDestroyView] 之间访问.
     */
    protected val binding: B get() = _binding!!


    /**
     * 子类实现该方法，返回使用 DataBinding 的 layout 资源 Id.
     */
    @LayoutRes
    protected abstract fun initContentLayoutId(): Int
    /**
     * 子类实现该方法，执行 onViewCreated 之后的初始化逻辑.
     */
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


    /**
     * 新版 LMS 风格的加载中弹框.
     */
    private var loadingDialog: LoadingDialog? = null
    /**
     * 显示加载中弹框.
     */
    fun showLoadingDialog(@StringRes resId: Int) {
        showLoadingDialog(getString(resId))
    }
    /**
     * 显示加载中弹框.
     */
    fun showLoadingDialog(text: CharSequence?) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(requireContext())
        }
        loadingDialog?.setTips(text)
        loadingDialog?.show()
    }
    /**
     * 关闭加载中弹框.
     */
    fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }
}