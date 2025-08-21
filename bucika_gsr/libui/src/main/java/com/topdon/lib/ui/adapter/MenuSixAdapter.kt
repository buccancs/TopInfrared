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
import com.topdon.lib.ui.listener.SingleClickListener
import kotlinx.android.synthetic.main.ui_item_menu_second_view.view.item_menu_tab_img
import kotlinx.android.synthetic.main.ui_item_menu_second_view.view.item_menu_tab_lay
import kotlinx.android.synthetic.main.ui_item_menu_second_view.view.item_menu_tab_text

@Deprecated("看起来是旧版 2D 编辑的菜单，根本没使用了")
class MenuSixAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener: ((index: Int, code: Int) -> Unit)? = null
    private var type = 0
    private var selected = -1
    private var colorEnable = false //伪彩条
    private var contrastEnable = false //对比度
    private var ddeEnable = false //细节

    fun selected(index: Int) {
        selected = index
        notifyDataSetChanged()
    }

    fun enColor(colorEnable: Boolean) {
        this.colorEnable = colorEnable
        notifyDataSetChanged()
    }

    fun enContrast(param: Boolean) {
        this.contrastEnable = param
        notifyDataSetChanged()
    }

    fun enDde(param: Boolean) {
        this.ddeEnable = param
        notifyDataSetChanged()
    }

    private val fourBean = arrayListOf(
        ColorBean(R.drawable.selector_menu2_setting_1, context.getString(R.string.thermal_pseudo), 1),
        ColorBean(R.drawable.selector_menu2_setting_2, context.getString(R.string.thermal_contrast), 2),
        ColorBean(R.drawable.selector_menu2_setting_3, context.getString(R.string.thermal_sharpen), 3),
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ui_item_menu_four_view, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            val bean = fourBean[position]
            holder.name.text = bean.name
            holder.img.setImageResource(bean.res)
            holder.lay.setOnClickListener(object : SingleClickListener() {
                override fun onSingleClick() {
                    listener?.invoke(position, bean.code)
                    selected(bean.code)
                }

            })
            when (bean.code) {
                1 -> {
                    iconUI(colorEnable, holder.img, holder.name)
                }
                2 -> {
                    iconUI(contrastEnable, holder.img, holder.name)
                }
                3 -> {
                    iconUI(ddeEnable, holder.img, holder.name)
                }
                else -> {
                    iconUI(bean.code == selected, holder.img, holder.name)
                }
            }
        }
    }

    // 状态变化
    private fun iconUI(isActive: Boolean, img: ImageView, nameText: TextView) {
        img.isSelected = isActive
        if (isActive) {
            nameText.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            nameText.setTextColor(ContextCompat.getColor(context, R.color.font_third_color))
        }
    }

    override fun getItemCount(): Int {
        return fourBean.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lay: View = itemView.item_menu_tab_lay
        val img: ImageView = itemView.item_menu_tab_img
        val name: TextView = itemView.item_menu_tab_text
    }

}