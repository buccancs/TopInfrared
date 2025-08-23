package com.topdon.module.thermal.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.tools.GlideLoader
import com.topdon.module.thermal.R
import kotlinx.android.synthetic.main.item_gallery.view.*

class GalleryAdapter(val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var listener: OnItemClickListener? = null

    var datas = arrayListOf<String>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            GlideLoader.load(holder.img, datas[position])
            holder.lay.setOnClickListener {
                Log.w("123", "文件: ${datas[position]}")
                listener?.onClick(position, datas[position])
            }
            holder.lay.setOnLongClickListener(View.OnLongClickListener {
                Log.w("123", "文件: ${datas[position]}")
                listener?.onLongClick(position, datas[position])
                return@OnLongClickListener true
            })
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lay = itemView.item_gallery_lay
        val img = itemView.item_gallery_img
    }

    interface OnItemClickListener {
        fun onClick(index: Int, path: String)
        fun onLongClick(index: Int, path: String)
    }

}