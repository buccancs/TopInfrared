package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_loading.view.*

/**
 * 新版 UI 的那个 LMS 的加载中弹框，由于 LMS 的弹框没有文字，只好自己再搞一个了。
 *
 * Created by LCG on 2024/4/12.
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.TransparentDialog) {

    fun setTips(@StringRes resId: Int) {
        rootView.tv_tips.setText(resId)
        rootView.tv_tips.isVisible = true
    }

    fun setTips(text: CharSequence?) {
        rootView.tv_tips.text = text
        rootView.tv_tips.isVisible = text?.isNotEmpty() == true
    }


    private val rootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(rootView)

        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = (ScreenUtil.getScreenWidth(context) * if (ScreenUtil.isPortrait(context)) 0.3 else 0.15).toInt()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }
    }

}