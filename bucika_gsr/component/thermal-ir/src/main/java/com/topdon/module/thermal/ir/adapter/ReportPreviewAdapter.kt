package com.topdon.module.thermal.ir.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.CollectionUtils
import com.topdon.house.activity.ImagesDetailActivity
import com.topdon.lib.core.bean.HouseRepPreviewItemBean
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.view.DetectHorizontalScrollView.OnScrollStopListner
import kotlinx.android.synthetic.main.item_gallery_head_lay.view.*
import kotlinx.android.synthetic.main.item_gallery_lay.view.*
import kotlinx.android.synthetic.main.item_report_floor.view.fly_project
import kotlinx.android.synthetic.main.item_report_floor.view.hsv_report
import kotlinx.android.synthetic.main.item_report_floor.view.lly_album
import kotlinx.android.synthetic.main.item_report_floor.view.rcy_album
import kotlinx.android.synthetic.main.item_report_floor.view.rcy_category
import kotlinx.android.synthetic.main.item_report_floor.view.rcy_report
import kotlinx.android.synthetic.main.item_report_floor.view.tv_floor_number
import kotlinx.android.synthetic.main.item_report_floor.view.view_category_mask


@SuppressLint("NotifyDataSetChanged")
class ReportPreviewAdapter(private val cxt: Context, var dataList: List<HouseRepPreviewItemBean>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemView(
            LayoutInflater.from(parent.context).inflate(R.layout.item_report_floor, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = dataList[position]
        holder.itemView.tv_floor_number.text = data.itemName

        holder.itemView.rcy_report.layoutManager = LinearLayoutManager(cxt)
        val reportPreviewAdapter =
            ReportPreviewFloorAdapter(cxt, data.projectItemBeans)
        holder.itemView.rcy_report?.adapter = reportPreviewAdapter

        if (CollectionUtils.isNotEmpty(data.projectItemBeans)) {
            holder.itemView.fly_project.visibility = View.VISIBLE
            holder.itemView.rcy_category.layoutManager = LinearLayoutManager(cxt)
            val reportCategoryAdapter =
                ReportPreviewFloorAdapter(cxt, data.projectItemBeans)
            holder.itemView.rcy_category?.adapter = reportCategoryAdapter
        } else {
            holder.itemView.fly_project.visibility = View.GONE
        }

        if (CollectionUtils.isNotEmpty(data.albumItemBeans)) {
            holder.itemView.lly_album.visibility = View.VISIBLE
            holder.itemView.rcy_album.layoutManager = GridLayoutManager(cxt, 3)
            val albumAdapter = ReportPreviewAlbumAdapter(cxt, data.albumItemBeans)
            holder.itemView.rcy_album?.adapter = albumAdapter
            albumAdapter.jumpListener = { _, position ->
                var intent = Intent(cxt, ImagesDetailActivity::class.java)
                var photos = ArrayList<String>()
                data.albumItemBeans.forEach {
                    photos.add(it.photoPath)
                }
                intent.putExtra(ExtraKeyConfig.IMAGE_PATH_LIST, photos)
                intent.putExtra(ExtraKeyConfig.CURRENT_ITEM, position)
                cxt.startActivity(intent)
            }
        } else {
            holder.itemView.lly_album.visibility = View.GONE
        }

        holder.itemView.hsv_report.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                holder.itemView.hsv_report.startScrollerTask()
            }
            false
        }

        holder.itemView.hsv_report.setOnScrollStopListner(object : OnScrollStopListner {
            override fun onScrollToRightEdge() {
                holder.itemView.view_category_mask.visibility = View.VISIBLE
            }

            override fun onScrollToMiddle() {
                holder.itemView.view_category_mask.visibility = View.VISIBLE
            }

            override fun onScrollToLeftEdge() {
                holder.itemView.view_category_mask.visibility = View.GONE
            }

            override fun onScrollStoped() {
            }

            override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
                if (holder.itemView.view_category_mask.visibility == View.VISIBLE) {
                    return
                }
                holder.itemView.view_category_mask.visibility = View.VISIBLE
            }
        })
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFloorNo: TextView = itemView.tv_floor_number
        val rcyReportFloor: RecyclerView = itemView.rcy_report
        val rcyCategory: RecyclerView = itemView.rcy_category
        val llyAlbum: LinearLayout = itemView.lly_album
        val rcyAlbum: RecyclerView = itemView.rcy_album
    }
}