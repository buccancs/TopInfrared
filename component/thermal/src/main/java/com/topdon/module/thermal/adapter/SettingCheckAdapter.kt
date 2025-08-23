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

class SettingCheckAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var datas = arrayOf("1s", "5s", "10s", "30s", "1min", "5min")
    private var dataTimes = arrayOf(1, 5, 10, 30, 60, 300)

    var listener: OnItemClickListener? = null
    var selectTime = 0

    fun setCheck(index: Int) {
        this.selectTime = index
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_setting_check, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            holder.btn.text = datas[position]
            if (position == selectTime) {
                holder.btn.setBackgroundResource(R.drawable.ui_radio_active_btn)
                holder.btn.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                holder.btn.setBackgroundResource(R.drawable.ui_radio_btn)
                holder.btn.setTextColor(ContextCompat.getColor(context, R.color.font_gray))
            }
            holder.btn.setOnClickListener {
                Log.w("123", "文件: ${datas[position]}")
                listener?.onClick(position, dataTimes[position])
            }

        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btn: Button = itemView.item_setting_check_btn
    }

    interface OnItemClickListener {
        fun onClick(index: Int, time: Int)
    }

}