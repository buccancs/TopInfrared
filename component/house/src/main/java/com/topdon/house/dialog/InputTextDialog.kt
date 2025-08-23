package com.topdon.house.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topdon.house.R
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.lms.sdk.weiget.TToast
import kotlinx.android.synthetic.main.dialog_input_text.view.*

class InputTextDialog(context: Context, private val inputText: String, private val onConfirmListener: (text: String) -> Unit) :
    Dialog(context, R.style.TextInputDialog), View.OnClickListener {

    private lateinit var contentView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        contentView = LayoutInflater.from(context).inflate(R.layout.dialog_input_text, null)
        contentView.et_input.setText(inputText)
        contentView.et_input.setSelection(0, contentView.et_input.length())
        contentView.et_input.requestFocus()
        contentView.tv_cancel.setOnClickListener(this)
        contentView.tv_confirm.setOnClickListener(this)
        setContentView(contentView)

        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = (ScreenUtil.getScreenWidth(context) * 0.76f).toInt()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            contentView.tv_cancel -> {
                dismiss()
            }
            contentView.tv_confirm -> {
                if (contentView.et_input.text.isEmpty()) {
                    TToast.shortToast(context, R.string.album_report_input_name_tips)
                    return
                }
                dismiss()
                onConfirmListener.invoke(contentView.et_input.text.toString())
            }
        }
    }
}