package com.topdon.module.thermal.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.db.entity.ThermalEntity
import com.topdon.lib.core.tools.GlideLoader
import com.topdon.lib.core.tools.TimeTool
import com.topdon.module.thermal.R
import kotlinx.android.synthetic.main.item_gallery.view.*
import kotlinx.android.synthetic.main.item_log.view.*

class MonitorLogAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var listener: OnItemClickListener? = null

    var datas = arrayListOf<ThermalEntity>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            val data = datas[position]
            holder.indexText.text = "${position + 1}"
            holder.timeText.text = TimeTool.showTimeSecond(data.createTime)
            holder.lay.setOnClickListener {
                listener?.onClick(position, data.thermalId)
            }
            holder.lay.setOnLongClickListener(View.OnLongClickListener {
                listener?.onLongClick(position, data.thermalId)
                return@OnLongClickListener true
            })
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lay = itemView.item_log_lay
        val indexText = itemView.item_log_index_text
        val timeText = itemView.item_log_time_text
    }

    interface OnItemClickListener {
        fun onClick(index: Int, thermalId: String)
        fun onLongClick(index: Int, thermalId: String)
    }

}