package com.topdon.lib.core.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.R
import com.topdon.lib.core.bean.ObserveBean
import com.topdon.lib.core.bean.TargetColorBean
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.itme_target_color.view.*

class TargetColorAdapter(val context: Context,var targetColor: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    var listenerTarget: OnItemClickListener? = null
    var listener: ((index: Int, code: Int) -> Unit)? = null
    private var type = 0

    fun selectedCode(code: Int) {
        targetColor = code
        notifyDataSetChanged()
    }

    private val targetColorBean = arrayListOf(
        TargetColorBean(R.drawable.bg_target_color_green, "", ObserveBean.TYPE_TARGET_COLOR_GREEN),
        TargetColorBean(R.drawable.bg_target_color_red, "", ObserveBean.TYPE_TARGET_COLOR_RED),
        TargetColorBean(R.drawable.bg_target_color_blue, "", ObserveBean.TYPE_TARGET_COLOR_BLUE),
        TargetColorBean(R.drawable.bg_target_color_black, "", ObserveBean.TYPE_TARGET_COLOR_BLACK),
        TargetColorBean(R.drawable.bg_target_color_white, "", ObserveBean.TYPE_TARGET_COLOR_WHITE),
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itme_target_color, parent, false)
        return ItemView(view)
    }

    override fun getItemCount(): Int {
        return targetColorBean.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            val bean = targetColorBean[position]
            holder.img.setImageResource(bean.res)
            holder.lay.setOnClickListener {
                listener?.invoke(position, bean.code)
                listenerTarget?.onClick(position,bean.code)
            }
            iconUI(bean.code == targetColor, holder.img, holder.strokeBg, holder.signBg)
        }
    }

    private fun iconUI(isActive: Boolean, img: ImageView, strokeBg: ImageView, signBg: ImageView) {
        img.isSelected = isActive
        if (isActive) {
            strokeBg.visibility = View.VISIBLE
            signBg.visibility = View.VISIBLE
        } else {
            strokeBg.visibility = View.GONE
            signBg.visibility = View.GONE
        }
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lay: View = itemView.item_menu_tab_lay
        val img: ImageView = itemView.item_target_color
        val strokeBg: ImageView = itemView.item_target_color_stroke
        val signBg: ImageView = itemView.item_target_color_sign
        init {
            val canSeeCount = 5
            val with = (ScreenUtil.getScreenWidth(context) / canSeeCount)
            itemView.layoutParams = ViewGroup.LayoutParams((with * 0.78).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
            val imageSize = (ScreenUtil.getScreenWidth(context) * 30 / 375f).toInt()
            val lpImg = img.layoutParams
            val lpStrokeImg = strokeBg.layoutParams
            lpImg.width = imageSize
            lpImg.height = imageSize
            lpStrokeImg.width = imageSize
            lpStrokeImg.height = imageSize
            img.layoutParams = lpImg
            strokeBg.layoutParams = lpStrokeImg
        }
    }

    interface OnItemClickListener {
        fun onClick(index: Int, code: Int)
    }
}