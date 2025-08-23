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

class FirmwareUpDialog(context: Context) : Dialog(context, R.style.InfoDialog), View.OnClickListener {

    var titleStr: CharSequence?
        get() = rootView.tv_title.text
        set(value) {
            rootView.tv_title.text = value
        }

    var sizeStr: CharSequence?
        get() = rootView.tv_size.text
        set(value) {
            rootView.tv_size.text = value
        }

    var contentStr: CharSequence?
        get() = rootView.tv_content.text
        set(value) {
            rootView.tv_content.text = value
        }

    var isShowRestartTips: Boolean
        get() = rootView.tv_restart_tips.isVisible
        set(value) {
            rootView.tv_restart_tips.isVisible = value
        }

    var isShowCancel: Boolean
        get() = rootView.tv_cancel.isVisible
        set(value) {
            rootView.tv_cancel.isVisible = value
        }

    var onCancelClickListener: (() -> Unit)? = null
    
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
            rootView.tv_cancel -> {
                dismiss()
                onCancelClickListener?.invoke()
            }
            rootView.tv_confirm -> {
                dismiss()
                onConfirmClickListener?.invoke()
            }
        }
    }
}