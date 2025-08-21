package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_firmware_up.view.*

/**
 * 固件升级有新版本提示弹框.
 * Created by LCG on 2024/3/4.
 */
class FirmwareUpDialog(context: Context) : Dialog(context, R.style.InfoDialog), View.OnClickListener {

    /**
     * 标题文字，如 “发现新版本 V3.50”
     */
    var titleStr: CharSequence?
        get() = rootView.tv_title.text
        set(value) {
            rootView.tv_title.text = value
        }

    /**
     * 文件大小文字，如 “大小: 239.6MB”
     */
    var sizeStr: CharSequence?
        get() = rootView.tv_size.text
        set(value) {
            rootView.tv_size.text = value
        }

    /**
     * 升级内容，一般直接扔从接口拿到的东西
     */
    var contentStr: CharSequence?
        get() = rootView.tv_content.text
        set(value) {
            rootView.tv_content.text = value
        }

    /**
     * 是否显示底部设备重启提示，目前仅固件升级需要显示，默认隐藏(Gone).
     */
    var isShowRestartTips: Boolean
        get() = rootView.tv_restart_tips.isVisible
        set(value) {
            rootView.tv_restart_tips.isVisible = value
        }

    /**
     * 是否显示取消按钮，默认显示.
     */
    var isShowCancel: Boolean
        get() = rootView.tv_cancel.isVisible
        set(value) {
            rootView.tv_cancel.isVisible = value
        }


    /**
     * 取消点击事件监听.
     */
    var onCancelClickListener: (() -> Unit)? = null
    /**
     * 更新点击事件监听.
     */
    var onConfirmClickListener: (() -> Unit)? = null


    private val rootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_firmware_up, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(rootView)

        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = (ScreenUtil.getScreenWidth(context) * 0.72).toInt()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }

        rootView.tv_cancel.setOnClickListener(this)
        rootView.tv_confirm.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            rootView.tv_cancel -> {//取消
                dismiss()
                onCancelClickListener?.invoke()
            }
            rootView.tv_confirm -> {//确认
                dismiss()
                onConfirmClickListener?.invoke()
            }
        }
    }
}