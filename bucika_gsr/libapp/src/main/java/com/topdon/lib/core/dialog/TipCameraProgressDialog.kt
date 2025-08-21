package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil


/**
 * 提示窗
 * create by fylder on 2018/6/15
 **/
class TipCameraProgressDialog : Dialog {


    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)


    class Builder {
        var dialog: TipCameraProgressDialog? = null

        private var context: Context? = null
        private var canceleable = true

        constructor(context: Context) {
            this.context = context
        }

        fun setCanceleable(cancel: Boolean): Builder {
            this.canceleable = cancel
            return this
        }

        fun dismiss() {
            this.dialog!!.dismiss()
        }

        fun create(): TipCameraProgressDialog {
            if (dialog == null) {
                dialog = TipCameraProgressDialog(context!!, R.style.InfoDialog)
            }
            val inflater =
                context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_tip_camera_progress, null)
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
            dialog!!.setCancelable(canceleable)
            dialog!!.setContentView(view)
            return dialog as TipCameraProgressDialog
        }
    }



    /**
     * 提交回调
     */
    interface OnClickListener {
        fun onClick(dialog: DialogInterface)
    }
}