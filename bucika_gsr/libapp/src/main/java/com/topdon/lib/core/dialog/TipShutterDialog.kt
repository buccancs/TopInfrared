package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.TextView
import androidx.annotation.StringRes
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_tip_shutter.view.*


/**
 * 自动快门提示弹窗
 * @author: CaiSongL
 * @date: 2023/4/13 10:57
 */
class TipShutterDialog : Dialog {


    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    class Builder(private val context: Context) {
        var dialog: TipShutterDialog? = null
        private var titleRes: Int? = null
        private var message: CharSequence? = null
        private var closeEvent: ((check: Boolean) -> Unit)? = null
        private var canceled = false

        fun setTitle(@StringRes resId: Int): Builder {
            this.titleRes = resId
            return this
        }

        fun setMessage(message: CharSequence): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes message: Int): Builder {
            this.message = context.getString(message)
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


        fun create(): TipShutterDialog {
            if (dialog == null) {
                dialog = TipShutterDialog(context, R.style.InfoDialog)
            }
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_tip_shutter, null)
            dialog!!.addContentView(view, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
            dialog!!.setCanceledOnTouchOutside(canceled)

            val lp = dialog!!.window!!.attributes
            lp.width = (ScreenUtil.getScreenWidth(context) * if (ScreenUtil.isPortrait(context)) 0.85 else 0.35).toInt() //设置宽度
            dialog!!.window!!.attributes = lp

            view.tv_i_know.setOnClickListener {
                dismiss()
                closeEvent?.invoke(view.dialog_tip_check.isChecked)
            }
            view.img_close.setOnClickListener {
                dismiss()
                closeEvent?.invoke(view.dialog_tip_check.isChecked)
            }
            if (titleRes != null) {
                view.tv_title.setText(titleRes!!)
            }
            if (message != null) {
                view.dialog_tip_msg_text.visibility = View.VISIBLE
                view.dialog_tip_msg_text.setText(message, TextView.BufferType.NORMAL)
            } else {
                view.dialog_tip_msg_text.visibility = View.GONE
            }
            dialog!!.setContentView(view)
            return dialog as TipShutterDialog
        }
    }

}