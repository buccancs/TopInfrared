package com.topdon.module.thermal.ir.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.bean.ObserveBean
import com.topdon.lib.ui.bean.ColorBean
import com.topdon.module.thermal.ir.R
import kotlinx.android.synthetic.main.itme_target_mode.view.*

class MeasureItemAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener: ((index: Int, code: Int) -> Unit)? = null
    private var type = 0
    private var selected = -1

    fun selected(index: Int) {
        selected = index
        notifyDataSetChanged()
    }

    private val secondBean = arrayListOf(
        ColorBean(R.drawable.ic_menu_thermal7001, "1.8m", ObserveBean.TYPE_MEASURE_PERSON),
        ColorBean(R.drawable.ic_menu_thermal7002, "1.0m", ObserveBean.TYPE_MEASURE_SHEEP),
        ColorBean(R.drawable.ic_menu_thermal7003, "0.5m", ObserveBean.TYPE_MEASURE_DOG),
        ColorBean(R.drawable.ic_menu_thermal7004, "0.2m", ObserveBean.TYPE_MEASURE_BIRD),
    )


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itme_target_mode, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            val bean = secondBean[position]
            holder.img.setImageResource(bean.res)
            holder.lay.setOnClickListener {
                listener?.invoke(position, bean.code)
                selected(bean.code)
            }
            holder.img.isSelected = bean.code == selected
            holder.name.visibility = View.VISIBLE
            holder.name.text = bean.name
            holder.name.isSelected = bean.code == selected
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.white)
//               if (position == selected) ContextCompat.getColor(context, R.color.white)
//                else ContextCompat.getColor(context, R.color.font_third_color)
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
//        init {
//            val canSeeCount = 4
//            val with = (ScreenUtils.getScreenWidth() / canSeeCount)
//            itemView.layoutParams = ViewGroup.LayoutParams((with * 0.96).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
//            val imageSize = (ScreenUtils.getScreenWidth() * 29 / 375f).toInt()
//            val layoutParams = itemView.item_menu_tab_img.layoutParams
//            layoutParams.width = imageSize
//            layoutParams.height = imageSize
//            itemView.item_menu_tab_img.layoutParams = layoutParams
//        }
    }
}