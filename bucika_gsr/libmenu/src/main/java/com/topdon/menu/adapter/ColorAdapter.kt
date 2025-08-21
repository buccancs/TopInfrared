package com.topdon.menu.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topdon.menu.view.ColorView
import com.topdon.menu.util.PseudoColorConfig

/**
 * 测温模式-菜单3-伪彩/观测模式-菜单4-伪彩 所用 Adapter，只支持单选.
 *
 * Created by LCG on 2024/11/12.
 */
@SuppressLint("NotifyDataSetChanged")
internal class ColorAdapter : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    /**
     * 当前选中的伪彩代号.
     */
    var selectCode = -1
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }
    /**
     * 选中变更事件监听.
     * index-选中伪彩在列表中的 index，也就 TC007 要用
     * code-伪彩代号，由于历史遗留（2D编辑的数据、保存设置开关的伪彩）没法改了
     * size-预设伪彩数量，也就 TC007 要用
     */
    var onColorListener: ((index: Int, code: Int, size: Int) -> Unit)? = null


    /**
     * 这里的 code 来源不详，由于历史遗留（2D编辑的数据、保存设置开关的伪彩都按这个保存）没法改了
     * 1-白热 3-铁红 4-彩虹1 5-彩虹2 6-彩虹3 7-红热 8-热铁 9-彩虹4 10-彩虹5 11-黑热
     */
    private val colorCodeArray: IntArray = intArrayOf(1, 3, 4, 5, 6, 7, 8, 9, 10, 11)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //按照UI图，宽度与屏幕宽度比例为 62:375
        val width: Int = (parent.context.resources.displayMetrics.widthPixels * 62f / 375).toInt()
        val colorView = ColorView(parent.context)
        colorView.layoutParams = ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(colorView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val code: Int = colorCodeArray[position]
        holder.colorView.isSelected = code == selectCode
        holder.colorView.refreshColor(PseudoColorConfig.getColors(code), PseudoColorConfig.getPositions(code))
        holder.colorView.setOnClickListener {
            if (selectCode != code) {
                selectCode = code
                onColorListener?.invoke(position, code, colorCodeArray.size)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int = colorCodeArray.size

    class ViewHolder(val colorView: ColorView) : RecyclerView.ViewHolder(colorView)

}