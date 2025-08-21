package com.topdon.tc001.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.View.MeasureSpec
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.blankj.utilcode.util.SizeUtils
import com.kylecorry.andromeda.core.ui.setCompoundDrawables
import com.topdon.tc001.R

/**
 * 首页-设备列表-删除设备确认 PopupWindow.
 *
 * Created by LCG on 2024/4/24.
 */
class DelPopup(val context: Context) : PopupWindow() {

    var onDelListener: (() -> Unit)? = null

    init {
        val widthPixels = context.resources.displayMetrics.widthPixels
        val textView = TextView(context)
        textView.setPadding(SizeUtils.dp2px(16f))
        textView.setText(R.string.report_delete)
        textView.textSize = 14f
        textView.setTextColor(0xffffffff.toInt())
        textView.compoundDrawablePadding = SizeUtils.dp2px(8f)
        textView.setCompoundDrawables(size = SizeUtils.sp2px(16f), left = R.drawable.svg_main_device_del)
        textView.minWidth = (widthPixels * 128f / 375).toInt()
        textView.setOnClickListener {
            dismiss()
            onDelListener?.invoke()
        }

        val widthMeasureSpec: Int = MeasureSpec.makeMeasureSpec(widthPixels, MeasureSpec.AT_MOST)
        val heightMeasureSpec: Int = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        textView.measure(widthMeasureSpec, heightMeasureSpec)

        val drawable = ContextCompat.getDrawable(context, R.drawable.svg_popup_del_bg)
        drawable?.setBounds(0, 0, textView.measuredWidth, textView.measuredHeight)
        textView.background = drawable

        isOutsideTouchable = true

        contentView = textView
        width = textView.measuredWidth
        height = textView.measuredHeight
    }

    fun show(anchor: View) {
        val locationArray = IntArray(2)
        anchor.getLocationInWindow(locationArray)

        val x = (context.resources.displayMetrics.widthPixels - width) / 2
        val y = locationArray[1] - SizeUtils.dp2px(12f)
        showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
    }
}