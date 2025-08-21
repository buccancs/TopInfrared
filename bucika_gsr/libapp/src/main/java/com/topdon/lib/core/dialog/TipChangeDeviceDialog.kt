package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.annotation.StringRes
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_tip_change_device.view.*

@Deprecated("3.80使用新首页设备列表逻辑，不需要设备切换提示弹框了")
class TipChangeDeviceDialog :Dialog {
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    class Builder {
        var dialog: TipChangeDeviceDialog? = null
        private var context: Context? = null
        private var message: String? = null
        private var closeEvent: ((check: Boolean) -> Unit)? = null
        private var canceled = false
        private var hasCheck = false

        private lateinit var checkBox: CheckBox
        private lateinit var imgClose: ImageView

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


        fun create(): TipChangeDeviceDialog {
            if (dialog == null) {
                dialog = TipChangeDeviceDialog(context!!, R.style.InfoDialog)
            }
            val inflater =
                context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_tip_change_device, null)

            view.tv_i_know.setOnClickListener {
                dismiss()
                closeEvent?.invoke(hasCheck)
            }

            checkBox = view.dialog_tip_check
            imgClose = view.img_close
            dialog!!.addContentView(
                view,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            val lp = dialog!!.window!!.attributes
            val wRatio =
                if (context!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    //竖屏
                    0.85
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
            dialog!!.setContentView(view)
            return dialog as TipChangeDeviceDialog
        }
    }
}