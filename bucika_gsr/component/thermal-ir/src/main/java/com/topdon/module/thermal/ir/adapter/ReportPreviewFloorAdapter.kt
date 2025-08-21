package com.topdon.module.thermal.ir.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.AppUtils
import com.topdon.lib.core.bean.GalleryBean
import com.topdon.lib.core.bean.HouseRepPreviewProjectItemBean
import com.topdon.lib.core.utils.AppUtil
import com.topdon.module.thermal.ir.R
import kotlinx.android.synthetic.main.item_gallery_head_lay.view.*
import kotlinx.android.synthetic.main.item_gallery_lay.view.*
import kotlinx.android.synthetic.main.item_report_floor.view.lly_album
import kotlinx.android.synthetic.main.item_report_floor.view.rcy_album
import kotlinx.android.synthetic.main.item_report_floor.view.rcy_category
import kotlinx.android.synthetic.main.item_report_floor.view.rcy_report
import kotlinx.android.synthetic.main.item_report_floor.view.tv_floor_number
import kotlinx.android.synthetic.main.item_report_floor_child.view.iv_problem
import kotlinx.android.synthetic.main.item_report_floor_child.view.iv_repair
import kotlinx.android.synthetic.main.item_report_floor_child.view.iv_replace
import kotlinx.android.synthetic.main.item_report_floor_child.view.lly_content
import kotlinx.android.synthetic.main.item_report_floor_child.view.rly_parent
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_problem
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_project
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_remark
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_repair
import kotlinx.android.synthetic.main.item_report_floor_child.view.tv_replace

@SuppressLint("NotifyDataSetChanged")
class ReportPreviewFloorAdapter(
    val cxt: Context,
    var dataList: List<HouseRepPreviewProjectItemBean>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemView(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_report_floor_child, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val bean = dataList[position]
        holder.itemView.iv_problem.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        holder.itemView.iv_repair.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        holder.itemView.iv_replace.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        holder.itemView.tv_problem.visibility = if (position == 0) View.VISIBLE else View.INVISIBLE
        holder.itemView.tv_repair.visibility = if (position == 0) View.VISIBLE else View.INVISIBLE
        holder.itemView.tv_replace.visibility = if (position == 0) View.VISIBLE else View.INVISIBLE
        holder.itemView.rly_parent.setBackgroundColor(
            if (position == 0) Color.parseColor("#393643") else Color.parseColor(
                "#23202E"
            )
        )

        if (position == 0) {
            holder.itemView.tv_project.text = cxt.getString(R.string.pdf_project_item)
            holder.itemView.tv_remark.text = cxt.getString(R.string.report_remark)
        } else {
            holder.itemView.tv_project.text = bean.projectName
            holder.itemView.tv_remark.text = bean.remark
            when (bean.state) {
                1 -> {
                    holder.itemView.iv_problem.visibility = View.VISIBLE
                    holder.itemView.iv_repair.visibility = View.INVISIBLE
                    holder.itemView.iv_replace.visibility = View.INVISIBLE
                }

                2 -> {
                    holder.itemView.iv_problem.visibility = View.INVISIBLE
                    holder.itemView.iv_repair.visibility = View.VISIBLE
                    holder.itemView.iv_replace.visibility = View.INVISIBLE
                }

                3 -> {
                    holder.itemView.iv_problem.visibility = View.INVISIBLE
                    holder.itemView.iv_repair.visibility = View.INVISIBLE
                    holder.itemView.iv_replace.visibility = View.VISIBLE
                }

                else -> {
                    holder.itemView.iv_problem.visibility = View.INVISIBLE
                    holder.itemView.iv_repair.visibility = View.INVISIBLE
                    holder.itemView.iv_replace.visibility = View.INVISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProject: TextView = itemView.tv_project
        val tvProblem: TextView = itemView.tv_problem
        val ivProblemState: ImageView = itemView.iv_problem
        val tvRepair: TextView = itemView.tv_repair
        val ivRepairState: ImageView = itemView.iv_repair
        val tvReplace: TextView = itemView.tv_replace
        val ivReplaceState: ImageView = itemView.iv_replace
        val tvRemark: TextView = itemView.tv_remark
    }
}