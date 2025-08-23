package com.topdon.tc004.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.tc004.R
import com.topdon.tc004.bean.MenuBean
import com.topdon.tc004.bean.MonocularBean
import com.topdon.tc004.config.MonocularHelp
import kotlinx.android.synthetic.main.item_menu_layout.view.item_menu_tab_img
import kotlinx.android.synthetic.main.item_menu_layout.view.item_menu_tab_lay
import kotlinx.android.synthetic.main.item_menu_layout.view.item_menu_tab_text

class MenuSixAdapter (val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener: ((index: Int, code: Int) -> Unit)? = null
    private var selected = -1
    private var rangeEnable = false
    private var pseudoMode = MenuBean.TYPE_MIX
    private var lightLevel = MenuBean.TYPE_LIGHT_MIDDLE
    private var pipEnable = false
    private var gainLevel = MenuBean.TYPE_GAIN_X1

    private fun selected(index: Int) {
        selected = index
        notifyDataSetChanged()
    }
    fun enBlack(pseudoMode: Int) {
        this.pseudoMode = pseudoMode
        notifyDataSetChanged()
    }
    fun enRange(rangeEnable: Boolean) {
        this.rangeEnable = rangeEnable
        notifyDataSetChanged()
    }
    fun enLight(lightLevel: Int) {
        this.lightLevel = lightLevel
        notifyDataSetChanged()
    }
    fun enPip(pipEnable: Boolean) {
        this.pipEnable = pipEnable
        notifyDataSetChanged()
    }
    fun enGain(gainLevel: Int) {
        this.gainLevel = gainLevel
        notifyDataSetChanged()
    }

    private val sixBean = arrayListOf(
        MonocularBean(R.drawable.ic_menu_black_hot_svg, context.getString(R.string.color_p11), MonocularHelp.TYPE_SET_BLACK),
        MonocularBean(R.drawable.ic_menu_range, context.getString(R.string.func_test_distance), MonocularHelp.TYPE_SET_RANGE),
        MonocularBean(R.drawable.ic_menu_light_high_svg, context.getString(R.string.brightness_ios), MonocularHelp.TYPE_SET_LIGHT),
        MonocularBean(R.drawable.ic_menu_pip, context.getString(R.string.thermal_picture_in_camera), MonocularHelp.TYPE_SET_PIP),
        MonocularBean(R.drawable.ic_menu_gain_x1_svg, context.getString(R.string.func_focal_distance), MonocularHelp.TYPE_SET_GAIN),
        MonocularBean(R.drawable.ic_menu_more, context.getString(R.string.search_learn_more), MonocularHelp.TYPE_SET_MORE),
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu_layout, parent, false)
        return ItemView(view)
    }

    override fun getItemCount(): Int {
        return sixBean.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            val bean = sixBean[position]
            holder.name.text = bean.name
            holder.img.setImageResource(bean.res)
            holder.lay.setOnClickListener{
                listener?.invoke(position, bean.code)
                selected(bean.code)
            }
            if(bean.code == MonocularHelp.TYPE_SET_BLACK){
                when (pseudoMode) {
                    MenuBean.TYPE_WHITE_HOT -> {
                        holder.img.setImageResource(R.drawable.ic_menu_white_hot)
                        holder.name.text = context.getString(R.string.color_p1)
                    }
                    MenuBean.TYPE_BLACK_HOT -> {
                        holder.img.setImageResource(R.drawable.ic_menu_black_hot)
                        holder.name.text = context.getString(R.string.color_p11)
                    }
                    MenuBean.TYPE_RED_HOT -> {
                        holder.img.setImageResource(R.drawable.ic_menu_red_hot)
                        holder.name.text = context.getString(R.string.color_p7)
                    }
                    MenuBean.TYPE_MIX -> {
                        holder.img.setImageResource(R.drawable.ic_menu_mix)
                        holder.name.text = context.getString(R.string.color_p12)
                    }
                    MenuBean.TYPE_BIRD -> {
                        holder.img.setImageResource(R.drawable.ic_menu_bird)
                        holder.name.text = context.getString(R.string.color_p13)
                    }
                }
            }

            if(bean.code == MonocularHelp.TYPE_SET_LIGHT){
                when (lightLevel) {
                    in 81..100 -> {
                        holder.img.setImageResource(R.drawable.ic_menu_light_high)
                        holder.name.text = context.getString(R.string.brightness_ios) +": "+ context.getString(R.string.ts004_high)
                    }
                    in 61..80 -> {
                        holder.img.setImageResource(R.drawable.ic_menu_light_middle)
                        holder.name.text = context.getString(R.string.brightness_ios)  +": "+ context.getString(R.string.ts004_middle)
                    }
                    in 0..60 -> {
                        holder.img.setImageResource(R.drawable.ic_menu_light_low)
                        holder.name.text = context.getString(R.string.brightness_ios) + ": "+  context.getString(R.string.ts004_low)
                    }
                }
            }

            if(bean.code == MonocularHelp.TYPE_SET_GAIN){
                when (gainLevel) {
                    MenuBean.TYPE_GAIN_X1 -> {
                        holder.img.setImageResource(R.drawable.ic_menu_gain_x1)
                    }
                    MenuBean.TYPE_GAIN_X2 -> {
                        holder.img.setImageResource(R.drawable.ic_menu_gain_x2)
                    }
                    MenuBean.TYPE_GAIN_X4-> {
                        holder.img.setImageResource(R.drawable.ic_menu_gain_x4)
                    }
                    MenuBean.TYPE_GAIN_X8-> {
                        holder.img.setImageResource(R.drawable.ic_menu_gain_x8)
                    }
                }
            }

            when (bean.code) {
                MonocularHelp.TYPE_SET_RANGE -> {
                    iconUI(rangeEnable, holder.img, holder.name)
                }
                MonocularHelp.TYPE_SET_PIP -> {
                    iconUI(pipEnable, holder.img, holder.name)
                }
                else -> {
                  iconUI(bean.code == selected, holder.img, holder.name)
                }
             }
         }
    }

    private fun iconUI(isActive: Boolean, img: ImageView, nameText: TextView) {
        img.isSelected = isActive
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lay: View = itemView.item_menu_tab_lay
        val img: ImageView = itemView.item_menu_tab_img
        val name: TextView = itemView.item_menu_tab_text
        init {
            if (ScreenUtil.isPortrait(context)) {
                val with = (ScreenUtil.getScreenWidth(context) / 3)
                itemView.layoutParams = ViewGroup.LayoutParams(with, ViewGroup.LayoutParams.WRAP_CONTENT)
                val imageSize = (ScreenUtil.getScreenWidth(context) * 62 / 375f).toInt()
                val layoutParams = itemView.item_menu_tab_img.layoutParams
                layoutParams.width = imageSize
                layoutParams.height = imageSize
                itemView.item_menu_tab_img.layoutParams = layoutParams
            }else{
                val count = 3.5
                val height = (ScreenUtil.getScreenHeight(context) / count).toInt()
                itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, height)
                val imageSize = (ScreenUtil.getScreenHeight(context) * 62 / 375f).toInt()
                val layoutParams = itemView.item_menu_tab_img.layoutParams
                layoutParams.width = imageSize
                layoutParams.height = imageSize
                itemView.item_menu_tab_img.layoutParams = layoutParams
            }
        }
    }
}