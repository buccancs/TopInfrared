package com.topdon.lib.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.ui.R
import com.topdon.lib.ui.bean.ColorBean
import com.topdon.lib.ui.config.CameraHelp
import kotlinx.android.synthetic.main.ui_item_menu_second_view.view.*

@Deprecated("旧的高低温点菜单，已重构过了")
class MenuSecondNightAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val curMultipleArray: HashMap<Int, Int> by lazy { hashMapOf() }

    var multipleListener: ((Int, Boolean) -> Unit)? = null

    fun clearMultipleSelected() {
        curMultipleArray.clear()
        notifyDataSetChanged()
    }

    private val secondBean = arrayListOf(
        ColorBean(R.drawable.selector_menu2_temp_point_1, context.getString(R.string.main_tab_second_high_temperature_point), CameraHelp.TYPE_SET_HIGHTEMP),
        ColorBean(R.drawable.selector_menu2_temp_point_2, context.getString(R.string.main_tab_second_low_temperature_point), CameraHelp.TYPE_SET_LOWTEMP),
        ColorBean(R.drawable.selector_menu2_del, context.getString(R.string.thermal_delete), CameraHelp.TYPE_SET_DETELE),
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemView(LayoutInflater.from(parent.context).inflate(R.layout.ui_item_menu_second_view, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            holder.img.setImageResource(secondBean[position].res)
            holder.name.text = secondBean[position].name

            holder.itemView.item_menu_tab_lay.setOnClickListener {
                multipleChoice(position)
            }

            holder.img.isSelected = curMultipleArray.contains(position)
            holder.name.isSelected = curMultipleArray.contains(position)
            holder.name.setTextColor(
                if (curMultipleArray.contains(position)) ContextCompat.getColor(context, R.color.white)
                else ContextCompat.getColor(context, R.color.font_third_color)
            )
        }
    }

    private fun multipleChoice(position: Int) {
        //1.计算curMultipleArray
        if (position == secondBean.size - 1) {
            curMultipleArray.clear()
            curMultipleArray[position] = secondBean[position].code
        } else {
            if (curMultipleArray.contains(position)) {
                curMultipleArray.remove(position)
            } else {
                curMultipleArray[position] = secondBean[position].code
            }
            if (curMultipleArray.contains(secondBean.size - 1)) {
                curMultipleArray.remove(secondBean.size - 1)
            }
        }
        //2.执行listener
        multipleListener?.invoke(secondBean[position].code, curMultipleArray.contains(position))
        //3.刷新数据
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return secondBean.size
    }

    class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lay: View = itemView.item_menu_tab_lay
        val img: ImageView = itemView.item_menu_tab_img
        val name: TextView = itemView.item_menu_tab_text
    }
}