package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.*
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_tip_observe.view.*

/**
 * 观测-弹框封装
 */
class TipObserveDialog : Dialog {

    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    class Builder {
        var dialog: TipObserveDialog? = null
        private var context: Context? = null
        private var title: String? = null
        private var message: String? = null
        private var closeEvent: ((check: Boolean) -> Unit)? = null
        private var canceled = false
        private var hasCheck = false

        private lateinit var titleText: TextView
        private lateinit var messageText: TextView
        private lateinit var checkBox: CheckBox
        private lateinit var imgClose: ImageView

        constructor(context: Context) {
            this.context = context
        }

        fun setMessage(message: Int): Builder {
            this.message = context!!.getString(message)
            return this
        }

        fun setTitle(title: Int): Builder {
            this.title = context!!.getString(title)
            return this
        }


        fun setCancelListener(event: ((check: Boolean) -> Unit)? = null): Builder {
            this.closeEvent = event
            return this
        }

        fun setCanceled(canceled: Boolean): Builder {
            this.canceled = canceled
            return this
        }

        fun dismiss() {
            this.dialog!!.dismiss()
        }

        fun create(): TipObserveDialog {
            if (dialog == null) {
                dialog = TipObserveDialog(context!!, R.style.InfoDialog)
            }
            val inflater =
                context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_tip_observe, null)

            view.tv_i_know.setOnClickListener {
                dismiss()
                closeEvent?.invoke(hasCheck)
            }

            titleText = view.tv_title
            messageText = view.dialog_tip_msg_text
            checkBox = view.dialog_tip_check
            imgClose = view.img_close
            dialog!!.addContentView(
                view,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )
            val lp = dialog!!.window!!.attributes
            val wRatio =
                if (context!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏
                    0.75
                } else {
                    //横屏
                    0.35
                }
            lp.width = (ScreenUtil.getScreenWidth(context!!) * wRatio).toInt() //设置宽度
            dialog!!.window!!.attributes = lp

            dialog!!.setCanceledOnTouchOutside(canceled)
            checkBox.isChecked = false
            hasCheck = false
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                hasCheck = isChecked
            }
            imgClose.setOnClickListener {
                dismiss()
                closeEvent?.invoke(hasCheck)
            }
            //title
            if (title != null) {
                titleText.setText(title, TextView.BufferType.NORMAL)
            }
            //msg
            if (message != null) {
                messageText.visibility = View.VISIBLE
                messageText.setText(message, TextView.BufferType.NORMAL)
            } else {
                messageText.visibility = View.GONE
            }
            dialog!!.setContentView(view)
            return dialog as TipObserveDialog
        }
    }
}