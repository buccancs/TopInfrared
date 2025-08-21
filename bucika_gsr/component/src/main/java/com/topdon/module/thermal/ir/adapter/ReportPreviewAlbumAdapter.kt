package com.topdon.module.thermal.ir.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.topdon.house.activity.ImagesDetailActivity
import com.topdon.lib.core.bean.GalleryBean
import com.topdon.lib.core.bean.HouseRepPreviewAlbumItemBean
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.ui.widget.RoundImageView
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.report.bean.ReportData
import kotlinx.android.synthetic.main.activity_report_preview.iv_inspector_signature
import kotlinx.android.synthetic.main.item_gallery_head_lay.view.*
import kotlinx.android.synthetic.main.item_gallery_lay.view.*
import kotlinx.android.synthetic.main.item_report_album_child.view.riv_photo
import kotlinx.android.synthetic.main.item_report_album_child.view.tv_name
import kotlinx.android.synthetic.main.item_report_floor_child.view.iv_problem
import kotlinx.android.synthetic.main.item_report_floor_child.view.iv_repair
import kotlinx.android.synthetic.main.item_report_floor_child.view.iv_replace
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_problem
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_project
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_remark
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_repair
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_replace

@SuppressLint("NotifyDataSetChanged")
class ReportPreviewAlbumAdapter(
    private val cxt: Context,
    private var dataList: List<HouseRepPreviewAlbumItemBean>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var jumpListener: ((item: HouseRepPreviewAlbumItemBean, position: Int) -> Unit)? = null
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemView(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_report_album_child, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val bean = dataList[position]
        Glide.with(cxt).load(bean.photoPath).into(holder.itemView.riv_photo)
        holder.itemView.tv_name.text = bean.title
        holder.itemView.riv_photo.setOnClickListener {
            jumpListener?.invoke(bean,position)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rivPhoto: RoundImageView = itemView.riv_photo
        val tvName: TextView = itemView.tv_name
    }
}