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

/**
 * TS004 远端图库删除提示弹框.
 *
 * Created by LCG on 2024/2/29.
 */
class ConfirmSelectDialog(context: Context) : Dialog(context, R.style.InfoDialog), View.OnClickListener {

    var onConfirmClickListener: ((isSelect: Boolean) -> Unit)? = null

    /**
     * 是否显示顶部信息图标，默认不显示.
     */
    fun setShowIcon(isShowIcon: Boolean) {
        rootView.iv_icon.isVisible = isShowIcon
    }

    fun setTitleRes(@StringRes titleRes: Int) {
        rootView.tv_title.setText(titleRes)
    }

    fun setTitleStr(titleStr: String) {
        rootView.tv_title.text = titleStr
    }

    /**
     * 是否显示提示文字及选中效果，默认不显示.
     */
    fun setShowMessage(isShowMessage: Boolean) {
        rootView.rl_message.isVisible = isShowMessage
    }

    fun setMessageRes(@StringRes messageRes: Int) {
        rootView.tv_message.setText(messageRes)
    }

    /**
     * 是否显示取消按钮，默认显示且默认文字为“取消”.
     */
    fun setShowCancel(isShowCancel: Boolean) {
        rootView.tv_cancel.isVisible = isShowCancel
    }
    /**
     * 设置取消按钮文字，默认为“取消”.
     */
    fun setCancelText(@StringRes cancelRes: Int) {
        rootView.tv_cancel.setText(cancelRes)
    }

    /**
     * 设置确认按钮文字，默认为“删除"
     */
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
            rootView.rl_message -> {//选中状态
                rootView.iv_select.isSelected = !rootView.iv_select.isSelected
            }
            rootView.tv_cancel -> {//取消
                dismiss()
            }
            rootView.tv_confirm -> {//确认
                dismiss()
                onConfirmClickListener?.invoke(rootView.iv_select.isSelected)
            }
        }
    }
}