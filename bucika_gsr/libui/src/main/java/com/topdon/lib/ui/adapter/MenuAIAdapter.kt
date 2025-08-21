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
    /**
     * 当前选中的选项 code.
     *
     * 由于历史遗留（已保存在 SharedPreferences 中），这里 code 取值为
     * - 什么都未选中：-1
     * - 动态识别：0
     * - 高温源：1
     * - 低温源：2
     */
    var selectCode: Int = -1
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    /**
     * 观测模式-菜单2-高低温源 点击事件监听，单选。
     */
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
//        init {
//            val canSeeCount = itemCount.toFloat() //一屏可见的 item 数量，目前都是全都显示完
//            val with = (ScreenUtils.getScreenWidth() / canSeeCount).toInt()
//            itemView.layoutParams = ViewGroup.LayoutParams(with, ViewGroup.LayoutParams.WRAP_CONTENT)
//            val imageSize = (ScreenUtils.getScreenWidth() * 62 / 375f).toInt()
//            val layoutParams = itemView.item_menu_tab_img.layoutParams
//            layoutParams.width = imageSize
//            layoutParams.height = imageSize
//            itemView.item_menu_tab_img.layoutParams = layoutParams
//        }
        val lay: View = itemView.item_menu_tab_lay
        val img: ImageView = itemView.item_menu_tab_img
        val name: TextView = itemView.item_menu_tab_text
    }
}