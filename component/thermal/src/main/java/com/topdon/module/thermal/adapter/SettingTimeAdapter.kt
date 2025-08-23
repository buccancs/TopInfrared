package com.topdon.module.thermal.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.topdon.module.thermal.R
import kotlinx.android.synthetic.main.item_setting_check.view.*
import kotlinx.android.synthetic.main.item_setting_time.view.*

class SettingTimeAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var datas = arrayOf("秒", "分", "时", "天")
    private var dataTimes = arrayOf(1, 2, 3, 4)

    var listener: OnItemClickListener? = null
    var select = 0

    fun setCheck(index: Int) {
        this.select = index
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_setting_time, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            holder.btn.text = datas[position]
            if (position == select) {
                holder.btn.setBackgroundResource(R.drawable.ui_btn_round_theme)
                holder.btn.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                holder.btn.background = null
                holder.btn.setTextColor(ContextCompat.getColor(context, R.color.font_gray))
            }
            holder.btn.setOnClickListener {
                listener?.onClick(position, dataTimes[position])
                setCheck(position)
            }

        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btn: Button = itemView.item_setting_time_btn
    }

    interface OnItemClickListener {
        fun onClick(index: Int, time: Int)
    }

}