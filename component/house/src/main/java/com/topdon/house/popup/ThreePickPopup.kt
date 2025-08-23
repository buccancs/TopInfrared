package com.topdon.house.popup

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.topdon.house.R
import kotlinx.android.synthetic.main.popup_three_pick.view.*

internal class ThreePickPopup(val context: Context, strIdArray: List<Int>, private var onPickListener: (position: Int) -> Unit) : PopupWindow(), View.OnClickListener {

    init {
        contentView = LayoutInflater.from(context).inflate(R.layout.popup_three_pick, null)
        contentView.tv_option1.text = context.getString(strIdArray[0])
        contentView.tv_option2.text = context.getString(strIdArray[1])
        if (strIdArray.size >= 3) {
            contentView.tv_option3.text = context.getString(strIdArray[2])
        } else {
            contentView.tv_option3.isVisible = false
            contentView.view_line2.isVisible = false
        }

        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((context.resources.displayMetrics.widthPixels * 0.42).toInt(), View.MeasureSpec.EXACTLY)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.heightPixels, View.MeasureSpec.AT_MOST)
        contentView.measure(widthMeasureSpec, heightMeasureSpec)

        width = contentView.measuredWidth
        height = contentView.measuredHeight

        isOutsideTouchable = true

        contentView.tv_option1.setOnClickListener(this)
        contentView.tv_option2.setOnClickListener(this)
        contentView.tv_option3.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            contentView.tv_option1 -> onPickListener.invoke(0)
            contentView.tv_option2 -> onPickListener.invoke(1)
            contentView.tv_option3 -> onPickListener.invoke(2)
        }
        dismiss()
    }

    fun show(anchor: View, isLeft: Boolean) {
        val heightPixels = context.resources.displayMetrics.heightPixels
        val locationArray = IntArray(2)
        anchor.getLocationInWindow(locationArray)

        val x = if (isLeft) locationArray[0] else locationArray[0] + anchor.width + SizeUtils.dp2px(17f) - width

        if (isLeft) {
            if (locationArray[1] >= height) {
                showAtLocation(anchor, Gravity.NO_GRAVITY, x, locationArray[1] - height)
            } else {
                showAsDropDown(anchor, Gravity.NO_GRAVITY, x, locationArray[1] + anchor.height)
            }
        } else {
            if (heightPixels - locationArray[1] - anchor.height - SizeUtils.dp2px(10f) > height) {
                showAtLocation(anchor, Gravity.NO_GRAVITY, x, locationArray[1] + anchor.height + SizeUtils.dp2px(10f))
            } else {
                showAtLocation(anchor, Gravity.NO_GRAVITY, x, (locationArray[1] - SizeUtils.dp2px(10f) - height).coerceAtLeast(0))
            }
        }
    }
}