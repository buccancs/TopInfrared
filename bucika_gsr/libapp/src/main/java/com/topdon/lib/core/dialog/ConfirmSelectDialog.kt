package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_confirm_select.view.*

class ConfirmSelectDialog(context: Context) : Dialog(context, R.style.InfoDialog), View.OnClickListener {

    var onConfirmClickListener: ((isSelect: Boolean) -> Unit)? = null

    fun setShowIcon(isShowIcon: Boolean) {
        rootView.iv_icon.isVisible = isShowIcon
    }

    fun setTitleRes(@StringRes titleRes: Int) {
        rootView.tv_title.setText(titleRes)
    }

    fun setTitleStr(titleStr: String) {
        rootView.tv_title.text = titleStr
    }

    fun setShowMessage(isShowMessage: Boolean) {
        rootView.rl_message.isVisible = isShowMessage
    }

    fun setMessageRes(@StringRes messageRes: Int) {
        rootView.tv_message.setText(messageRes)
    }

    fun setShowCancel(isShowCancel: Boolean) {
        rootView.tv_cancel.isVisible = isShowCancel
    }
    
    fun setCancelText(@StringRes cancelRes: Int) {
        rootView.tv_cancel.setText(cancelRes)
    }

    fun setConfirmText(@StringRes confirmRes: Int) {
        rootView.tv_confirm.setText(confirmRes)
    }

    private val rootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_select, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setContentView(rootView)

        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = (ScreenUtil.getScreenWidth(context) * 0.72).toInt()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }

        rootView.rl_message.setOnClickListener(this)
        rootView.tv_cancel.setOnClickListener(this)
        rootView.tv_confirm.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            rootView.rl_message -> {
                rootView.iv_select.isSelected = !rootView.iv_select.isSelected
            }
            rootView.tv_cancel -> {
                dismiss()
            }
            rootView.tv_confirm -> {
                dismiss()
                onConfirmClickListener?.invoke(rootView.iv_select.isSelected)
            }
        }
    }
}