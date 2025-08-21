package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_long_text.view.*

/**
 * 展示很长文字的弹框.
 *
 * Created by LCG on 2024/2/2.
 */
class LongTextDialog(context: Context, val title: String?, val content: String?) : Dialog(context, R.style.InfoDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        val rootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_long_text, null)
        rootView.tv_title.text = title
        rootView.tv_text.text = content
        setContentView(rootView)
        rootView.tv_i_know.setOnClickListener {
            dismiss()
        }


        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = (ScreenUtil.getScreenWidth(context) * 0.74f).toInt()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }
    }
}