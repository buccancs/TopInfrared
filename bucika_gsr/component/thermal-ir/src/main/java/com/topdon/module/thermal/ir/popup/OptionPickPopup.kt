package com.topdon.module.thermal.ir.popup

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.PopupWindow
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.view.MyTextView
import com.topdon.module.thermal.ir.R
import kotlinx.android.synthetic.main.popup_option_pick.view.*

/**
 * 选项拾取 PopupWindow.
 *
 * Created by LCG on 2024/1/5.
 */
class OptionPickPopup(private val context: Context, private val strArray: Array<String>, private val resIdArray: Array<Int>? = null) : PopupWindow() {

    companion object {
        /**
         * 选项文字大小，单位 ***sp***
         */
        private const val TEXT_SIZE_SP: Float = 14f
        /**
         * 选项文字顶部或底部 padding，单位 ***dp***
         */
        private const val TEXT_PADDING: Float = 7f
    }


    /**
     * 一个选项被选中事件监听.
     */
    var onPickListener: ((position: Int, str: String) -> Unit)? = null


    init {
        val textView = TextView(context)
        textView.textSize = TEXT_SIZE_SP

        val fontMetrics = textView.paint.fontMetricsInt

        val canSeeItem: Int = strArray.size.coerceAtMost(2)
        val itemHeight: Int = fontMetrics.bottom - fontMetrics.top + SizeUtils.dp2px(TEXT_PADDING) * 2
        val contentHeight = SizeUtils.dp2px(14f) + itemHeight * canSeeItem
        val contentWidth = (contentHeight * 120f / 81f).toInt()

        contentView = LayoutInflater.from(context).inflate(R.layout.popup_option_pick, null)
        width = contentWidth
        height = contentHeight

        isOutsideTouchable = true

        val adapter = MyAdapter()
        adapter.onItemClickListener = {
            dismiss()
            onPickListener?.invoke(it, strArray[it])
        }
        contentView.recycler_view.adapter = adapter
        contentView.recycler_view.layoutManager = LinearLayoutManager(context)
    }

    fun show(anchor: View) {
        val locationArray = IntArray(2)
        anchor.getLocationInWindow(locationArray)

        val x = locationArray[0] + anchor.width - width + SizeUtils.dp2px(5f)

        if (context.resources.displayMetrics.heightPixels - locationArray[1] - anchor.height > height - SizeUtils.dp2px(5f)) {//在 anchor 底部放得下
            showAtLocation(anchor, Gravity.NO_GRAVITY, x, locationArray[1] + anchor.height - SizeUtils.dp2px(5f))
        } else {//下面放不下就放上面吧
            showAtLocation(anchor, Gravity.NO_GRAVITY, x, (locationArray[1] - height + SizeUtils.dp2px(5f)).coerceAtLeast(0))
        }
    }


    private inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        /**
         * item 点击事件监听.
         */
        var onItemClickListener: ((position: Int) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val textView = MyTextView(context)
            textView.textSize = TEXT_SIZE_SP
            textView.setDrawableHeightPx(SizeUtils.sp2px(18f))
            textView.setTextColor(0xffffffff.toInt())
            textView.setPadding(SizeUtils.dp2px(14f), SizeUtils.dp2px(TEXT_PADDING), SizeUtils.dp2px(14f), SizeUtils.dp2px(TEXT_PADDING))
            textView.compoundDrawablePadding = SizeUtils.dp2px(10f)
            textView.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            return ViewHolder(textView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = strArray[position]
            if (resIdArray != null && position < resIdArray.size) {
                holder.textView.setOnlyDrawableStart(resIdArray[position])
            } else {
                holder.textView.setOnlyDrawableStart(0)
            }
        }

        override fun getItemCount(): Int = strArray.size

        inner class ViewHolder(val textView: MyTextView) : RecyclerView.ViewHolder(textView) {
            init {
                textView.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener?.invoke(position)
                    }
                }
            }
        }
    }
}