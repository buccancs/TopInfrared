package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_msg.view.*


/**
 * 消息提示窗
 * create by fylder on 2018/6/15
 **/
class MsgDialog : Dialog {

    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)


    class Builder {
        var dialog: MsgDialog? = null

        private var context: Context? = null

        private var imgRes: Int = 0
        private var message: String? = null
        private var positiveClickListener: OnClickListener? = null

        private var tipImg: ImageView? = null
        private var messageText: TextView? = null
        private var closeImg: ImageView? = null

        constructor(context: Context) {
            this.context = context
        }

        fun setImg(@DrawableRes res: Int): Builder {
            this.imgRes = res
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes message: Int): Builder {
            this.message = context!!.getString(message)
            return this
        }

        fun setCloseListener(listener: OnClickListener): Builder {
            this.positiveClickListener = listener
            return this
        }

        fun dismiss() {
            this.dialog!!.dismiss()
        }


        fun create(): MsgDialog {
            if (dialog == null) {
                dialog = MsgDialog(context!!, R.style.InfoDialog)
            }
            val inflater =
                context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_msg, null)
            tipImg = view.dialog_msg_img
            messageText = view.dialog_msg_text
            closeImg = view.dialog_msg_close
            dialog!!.addContentView(
                view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )
            val lp = dialog!!.window!!.attributes
            val wRatio =
                if (context!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏
                    0.9
                } else {
                    //横屏
                    0.3
                }
            lp.width = (ScreenUtil.getScreenWidth(context!!) * wRatio).toInt() //设置宽度
            dialog!!.window!!.attributes = lp

            dialog!!.setCanceledOnTouchOutside(false)
            closeImg!!.setOnClickListener {
                dismiss()
                if (positiveClickListener != null) {
                    positiveClickListener!!.onClick(dialog!!)
                }
            }
            //img
            if (imgRes != 0) {
                tipImg?.visibility = View.VISIBLE
                tipImg?.setImageResource(imgRes)
            } else {
                tipImg?.visibility = View.GONE
            }
            //msg
            if (message != null) {
                messageText?.visibility = View.VISIBLE
                messageText?.setText(message, TextView.BufferType.NORMAL)
            } else {
                messageText?.visibility = View.GONE
            }

            dialog!!.setContentView(view)
            return dialog as MsgDialog
        }
    }


    /**
     * 提交回调
     */
    interface OnClickListener {
        fun onClick(dialog: DialogInterface)
    }
}