package com.topdon.lib.core.ktbase

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.topdon.lib.core.R
import com.topdon.lib.core.bean.event.SocketStateEvent
import com.topdon.lib.core.bean.event.device.DeviceConnectEvent
import com.topdon.lib.core.dialog.LoadingDialog
import com.trello.rxlifecycle2.components.support.RxFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * create by fylder on 2018/7/13
 **/
abstract class BaseFragment : RxFragment() {

    val TAG = BaseFragment::class.java.simpleName

    abstract fun initContentView(): Int

    abstract fun initView()

    abstract fun initData()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(initContentView(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        initView()
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            // 不在最前端显示 相当于调用了onPause();

        } else {  // 在最前端显示 相当于调用了onResume();
            //网络数据刷新
            initData()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }




    /**
     * 新版 LMS 风格的加载中弹框.
     */
    private var loadingDialog: LoadingDialog? = null
    /**
     * 显示 LMS 风格的加载中弹框.
     */
    fun showLoadingDialog(@StringRes resId: Int = 0) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(requireContext())
        }
        loadingDialog?.setTips(if (resId == 0) R.string.tip_loading else resId)
        loadingDialog?.show()
    }
    /**
     * 显示 LMS 风格的加载中弹框.
     */
    fun showLoadingDialog(text: CharSequence) {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(requireContext())
        }
        loadingDialog?.setTips(text)
        loadingDialog?.show()
    }
    /**
     * 关闭 LMS 风格的加载中弹框.
     */
    fun dismissLoadingDialog() {
        loadingDialog?.dismiss()
    }



    @Subscribe(threadMode = ThreadMode.MAIN)
    fun getConnectState(event: DeviceConnectEvent) {
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
}