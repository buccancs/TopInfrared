package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.annotation.StringRes
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_tip_progress.view.*


/**
 * 提示窗
 * create by fylder on 2018/6/15
 **/
class TipProgressDialog : Dialog {


    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)


    class Builder {
        var dialog: TipProgressDialog? = null

        private var context: Context? = null

        private var message: String? = null
        private var canceleable = true

        private var messageText: TextView? = null

        constructor(context: Context) {
            this.context = context
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes message: Int): Builder {
            this.message = context!!.getString(message)
            return this
        }

        fun setCanceleable(cancal: Boolean): Builder {
            this.canceleable = cancal
            return this
        }

        fun dismiss() {
            this.dialog!!.dismiss()
        }


        fun create(): TipProgressDialog {
            if (dialog == null) {
                dialog = TipProgressDialog(context!!, R.style.InfoDialog)
            }
            val inflater =
                context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_tip_progress, null)
            messageText = view.dialog_tip_load_msg

            dialog!!.addContentView(
                view,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )
            val lp = dialog!!.window!!.attributes
            val wRatio =
                if (context!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏
                    0.52
                } else {
                    //横屏
                    0.35
                }
            lp.width = (ScreenUtil.getScreenWidth(context!!) * wRatio).toInt() //设置宽度
            dialog!!.window!!.attributes = lp

            dialog!!.setCanceledOnTouchOutside(canceleable)
            //msg
            if (message != null) {
                messageText?.visibility = View.VISIBLE
                messageText?.setText(message, TextView.BufferType.NORMAL)
            } else {
                messageText?.visibility = View.GONE
            }

            dialog!!.setContentView(view)
            return dialog as TipProgressDialog
        }
    }


    /**
     * 提交回调
     */
    interface OnClickListener {
        fun onClick(dialog: DialogInterface)
    }
}