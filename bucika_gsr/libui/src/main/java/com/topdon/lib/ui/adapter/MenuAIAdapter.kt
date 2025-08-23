package com.topdon.lib.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.bean.ObserveBean
import com.topdon.lib.ui.R
import com.topdon.lib.ui.bean.ColorBean
import kotlinx.android.synthetic.main.ui_item_menu_second_view.view.*

@Deprecated("旧的高低温源菜单，已重构过了")
class MenuAIAdapter (val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    var selectCode: Int = -1
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    var onTempSourceListener: ((code: Int) -> Unit)? = null

    private val secondBean = arrayListOf(
        ColorBean(R.drawable.selector_menu2_source_1_auto, context.getString(R.string.main_tab_second_dynamic_recognition), ObserveBean.TYPE_DYN_R),
        ColorBean(R.drawable.selector_menu2_source_2_high, context.getString(R.string.main_tab_second_high_temperature_source), ObserveBean.TYPE_TMP_H_S),
        ColorBean(R.drawable.selector_menu2_source_3_low, context.getString(R.string.main_tab_second_low_temperature_source), ObserveBean.TYPE_TMP_L_S),
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ui_item_menu_second_view, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            holder.img.setImageResource(secondBean[position].res)
            holder.lay.setOnClickListener {
                selectCode = secondBean[position].code
                onTempSourceListener?.invoke(secondBean[position].code)
            }
            holder.img.isSelected = secondBean[position].code == selectCode
            holder.name.text = secondBean[position].name
            holder.name.isSelected = secondBean[position].code == selectCode
            holder.name.setTextColor(
                if (secondBean[position].code == selectCode) ContextCompat.getColor(context, R.color.white)
                else ContextCompat.getColor(context, R.color.font_third_color)
            )
        }
    }

    override fun getItemCount(): Int {
        return secondBean.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lay: View = itemView.item_menu_tab_lay
        val img: ImageView = itemView.item_menu_tab_img
        val name: TextView = itemView.item_menu_tab_text
    }
}