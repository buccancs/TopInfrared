package com.topdon.menu.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topdon.menu.view.ColorView
import com.topdon.menu.util.PseudoColorConfig

@SuppressLint("NotifyDataSetChanged")
internal class ColorAdapter : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    var selectCode = -1
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }
    
    var onColorListener: ((index: Int, code: Int, size: Int) -> Unit)? = null

    private val colorCodeArray: IntArray = intArrayOf(1, 3, 4, 5, 6, 7, 8, 9, 10, 11)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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