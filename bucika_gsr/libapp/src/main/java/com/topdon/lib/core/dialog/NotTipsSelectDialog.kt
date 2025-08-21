package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.topdon.lib.core.R
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_not_tips_select.*

/**
 * 与 TipDialog 类似，不过多了个 “不再提示” 选中效果的提示弹窗.
 *
 * Created by LCG on 2024/10/26.
 */
class NotTipsSelectDialog(context: Context) : Dialog(context, R.style.InfoDialog) {

    @StringRes
    private var tipsResId: Int = 0
    private var onConfirmListener: ((isSelect: Boolean) -> Unit)? = null


    fun setTipsResId(@StringRes tipsResId: Int): NotTipsSelectDialog {
        this.tipsResId = tipsResId
        return this
    }

    /**
     * 点击 “我知道了” 事件监听.
     */
    fun setOnConfirmListener(l: ((isSelect: Boolean) -> Unit)?): NotTipsSelectDialog {
        onConfirmListener = l
        return this
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_not_tips_select)

        if (tipsResId != 0) {
            tv_message.setText(tipsResId)
        }
        tv_select.setOnClickListener {
            it.isSelected = !it.isSelected
        }
        tv_i_know.setOnClickListener {
            onConfirmListener?.invoke(tv_select.isSelected)
            dismiss()
        }

        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = (ScreenUtil.getScreenWidth(context) * 0.73f).toInt()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }
    }
}