package com.topdon.module.thermal.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.topdon.module.thermal.R
import kotlinx.android.synthetic.main.item_menu_tab_more_view.view.*
import kotlinx.android.synthetic.main.item_menu_tab_view.view.*

class MenuTabAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var listener: OnItemClickListener? = null
    private var type = 0
    private var datas = arrayListOf<Int>()
    private var dataStrList = arrayListOf<String>()
    private var selected = -1

    companion object {
        private const val TYPE_ITEM = 300
        private const val TYPE_ITEM_MORE = 301
    }

    fun selected(index: Int) {
        selected = index
        notifyDataSetChanged()
    }

    private val firstMenus =
        arrayListOf(R.drawable.ic_menu_thermal1001_svg, R.drawable.ic_menu_thermal1002_svg)

    private val secondMenus =
        arrayListOf(
            R.drawable.ic_menu_thermal2002,
            R.drawable.ic_menu_thermal2003,
            R.drawable.ic_menu_thermal2004,
            R.drawable.ic_menu_thermal2001,
            R.drawable.ic_menu_thermal2005,
            R.drawable.ic_menu_thermal2006
        )

    private val secondMenusStr =
        arrayListOf(
            "点",
            "线",
            "面",
            "添加",
            "全图",
            "删除"
        )

    private val fourthMenusStr =
        arrayListOf(
            "旋转",
            "增强",
            "画中画",
            "色带",
        )

    private val thirdMenus =
        arrayListOf(
            R.drawable.ic_menu_thermal3001,
            R.drawable.ic_menu_thermal3002,
            R.drawable.ic_menu_thermal3003,
            R.drawable.ic_menu_thermal3004,
            R.drawable.ic_menu_thermal3005,
            R.drawable.ic_menu_thermal3006,
            R.drawable.ic_menu_thermal3007,
            R.drawable.ic_menu_thermal3008,
            R.drawable.ic_menu_thermal3009,
            R.drawable.ic_menu_thermal3010
        )

    private val fourthMenus =
        arrayListOf(
            R.drawable.ic_menu_thermal4001_svg,
            R.drawable.ic_menu_thermal4002_svg,
            R.drawable.ic_menu_thermal4003_svg,
            R.drawable.ic_menu_thermal4004_svg
        )

    fun initType(type: Int) {
        this.type = type
        datas = when (type) {
            1 -> firstMenus
            2 -> secondMenus
            3 -> thirdMenus
            4 -> fourthMenus
            else -> thirdMenus
        }
        dataStrList = when (type) {
            2 -> secondMenusStr
            4 -> fourthMenusStr
            else -> secondMenusStr
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_menu_tab_view, parent, false)
            ItemView(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_menu_tab_more_view, parent, false)
            ItemMoreView(view)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BaseItemView) {
            holder.img.setImageResource(datas[position])
            holder.lay.setOnClickListener {
                val index = type * 1000 + position + 1
                listener?.onClick(index)
                selected(position)
            }
            holder.img.isSelected = position == selected
            if (holder is ItemView) {
                holder.name.text = dataStrList[position]
                holder.name.isSelected = position == selected
                holder.name.setTextColor(
                    if (position == selected) ContextCompat.getColor(context, R.color.white)
                    else ContextCompat.getColor(context, R.color.font_third_color)
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (type == 3) {
            TYPE_ITEM_MORE
        } else {
            TYPE_ITEM
        }
    }

    open class BaseItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var lay: View
        lateinit var img: ImageView
    }

    inner class ItemView(itemView: View) : BaseItemView(itemView) {
        var name: TextView

        init {
            lay = itemView.item_menu_tab_lay
            img = itemView.item_menu_tab_img
            name = itemView.item_menu_tab_text
        }
    }

    inner class ItemMoreView(itemView: View) : BaseItemView(itemView) {
        init {
            lay = itemView.item_menu_tab_more_lay
            img = itemView.item_menu_tab_more_img
        }
    }

    interface OnItemClickListener {
        fun onClick(index: Int)
    }

}