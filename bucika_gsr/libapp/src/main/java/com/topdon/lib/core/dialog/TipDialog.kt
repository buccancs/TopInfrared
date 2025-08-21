package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.topdon.lib.core.R
import kotlinx.android.synthetic.main.dialog_tip.view.*

/**
 * 提示窗
 * create by fylder on 2018/6/15
 **/
class TipDialog : Dialog {

    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    override fun onBackPressed() {

    }


    class Builder(private val context: Context) {
        var dialog: TipDialog? = null

        private var message: String? = null
        private var titleMessage: String? = null
        private var positiveStr: String? = null
        private var cancelStr: String? = null
        private var positiveEvent: (() -> Unit)? = null
        private var cancelEvent: (() -> Unit)? = null
        private var canceled = false
        private var isShowRestartTips = false

        fun setTitleMessage(message: String): Builder {
            this.titleMessage = message
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes message: Int): Builder {
            this.message = context.getString(message)
            return this
        }

        fun setPositiveListener(@StringRes strRes: Int, event: (() -> Unit)? = null): Builder {
            return setPositiveListener(context.getString(strRes), event)
        }

        fun setPositiveListener(str: String, event: (() -> Unit)? = null): Builder {
            this.positiveStr = str
            this.positiveEvent = event
            return this
        }

        fun setCancelListener(@StringRes strRes: Int, event: (() -> Unit)? = null): Builder {
            return setCancelListener(context.getString(strRes), event)
        }

        fun setCancelListener(str: String, event: (() -> Unit)? = null): Builder {
            this.cancelStr = str
            this.cancelEvent = event
            return this
        }

        fun setCanceled(canceled: Boolean): Builder {
            this.canceled = canceled
            return this
        }

        fun setShowRestartTops(isShowRestartTips: Boolean): Builder {
            this.isShowRestartTips = isShowRestartTips
            return this
        }

        fun dismiss() {
            this.dialog!!.dismiss()
        }


        fun create(): TipDialog {
            if (dialog == null) {
                dialog = TipDialog(context, R.style.InfoDialog)
            }

            val view = LayoutInflater.from(context).inflate(R.layout.dialog_tip, null)
            dialog!!.addContentView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            val isPortrait = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            val widthPixels = context.resources.displayMetrics.widthPixels
            val lp = dialog!!.window!!.attributes
            lp.width = (widthPixels * if (isPortrait) 0.85 else 0.35).toInt() //设置宽度
            dialog!!.window!!.attributes = lp

            dialog!!.setCanceledOnTouchOutside(canceled)
            view.dialog_tip_success_btn.setOnClickListener {
                dismiss()
                positiveEvent?.invoke()
            }
            view.dialog_tip_cancel_btn.setOnClickListener {
                dismiss()
                cancelEvent?.invoke()
            }

            if (positiveStr != null) {
                view.dialog_tip_success_btn.text = positiveStr
            }
            if (!TextUtils.isEmpty(cancelStr)) {
                view.space_margin.visibility = View.VISIBLE
                view.dialog_tip_cancel_btn.visibility = View.VISIBLE
                view.dialog_tip_cancel_btn.text = cancelStr
            } else {
                view.space_margin.visibility = View.GONE
                view.dialog_tip_cancel_btn.visibility = View.GONE
                view.dialog_tip_cancel_btn.text = ""
            }
            //msg
            if (message != null) {
                view.dialog_tip_msg_text.visibility = View.VISIBLE
                view.dialog_tip_msg_text.setText(message, TextView.BufferType.NORMAL)
            } else {
                view.dialog_tip_msg_text.visibility = View.GONE
            }

            //msg
            if (titleMessage != null) {
                view.dialog_tip_title_msg_text.visibility = View.VISIBLE
                view.dialog_tip_title_msg_text.setText(titleMessage, TextView.BufferType.NORMAL)
            } else {
                view.dialog_tip_title_msg_text.visibility = View.GONE
            }

            view.tv_restart_tips.isVisible = isShowRestartTips

            dialog!!.setContentView(view)
            return dialog as TipDialog
        }
    }

}