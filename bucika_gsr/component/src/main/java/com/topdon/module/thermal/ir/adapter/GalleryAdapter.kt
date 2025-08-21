package com.topdon.module.thermal.ir.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.bean.GalleryBean
import com.topdon.lib.core.bean.GalleryTitle
import com.topdon.lib.core.tools.GlideLoader
import com.topdon.lib.core.tools.TimeTool
import com.topdon.module.thermal.ir.R
import kotlinx.android.synthetic.main.item_gallery_head_lay.view.*
import kotlinx.android.synthetic.main.item_gallery_lay.view.*

/**
 * 照片或视频
 */
@SuppressLint("NotifyDataSetChanged")
class GalleryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEAD = 0
        private const val TYPE_DATA = 1
    }

    /**
     * 当前显示的数据列表，包含有标题 item.
     */
    val dataList: ArrayList<GalleryBean> = ArrayList()

    /**
     * 编辑模式下，当前选中的 position 列表.
     */
    val selectList: ArrayList<Int> = ArrayList()

    /**
     * 是否为 TS004 远端模式，处于该模式会有下载图标.
     */
    var isTS004Remote = false
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    /**
     * 当前是否处于编辑模式.
     */
    var isEditMode = false
        set(value) {
            if (field != value) {
                field = value
                if (!value) {
                    selectList.clear()
                    selectCallback?.invoke(selectList)
                }
                notifyDataSetChanged()
            }
        }


    /**
     * 非编辑模式下 item 长按进入编辑模式事件监听.
     */
    var onLongEditListener: (() -> Unit)? = null
    /**
     * 选中数量变更回调.
     * data 当前选中的 item position 列表
     */
    var selectCallback: ((data: ArrayList<Int>) -> Unit)? = null
    /**
     * 非编辑模式时，item 点击事件监听.
     */
    var itemClickCallback: ((position: Int) -> Unit)? = null


    fun refreshList(newList: List<GalleryBean>) {
        dataList.clear()
        dataList.addAll(newList)
        notifyDataSetChanged()
    }

    fun buildSelectList(): ArrayList<GalleryBean> {
        val resultList: ArrayList<GalleryBean> = ArrayList()
        selectList.forEach {
            resultList.add(dataList[it])
        }
        return resultList
    }

    fun selectAll() {
        var dataCount = 0
        dataList.forEach {
            if (it !is GalleryTitle) {
                dataCount++
            }
        }
        if (selectList.size >= dataCount) {
            selectList.clear()
        } else {
            selectList.clear()
            for (i in 0 until dataList.size) {
                if (dataList[i] !is GalleryTitle) {
                    selectList.add(i)
                }
            }
        }
        selectCallback?.invoke(selectList)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] is GalleryTitle) {
            TYPE_HEAD
        } else {
            TYPE_DATA
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEAD) {
            ItemHeadView(LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_head_lay, parent, false))
        } else {
            ItemView(LayoutInflater.from(parent.context).inflate(R.layout.item_gallery_lay, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = dataList[position]
        if (holder is ItemView) {
            GlideLoader.load(holder.img, data.thumb)
            if (data.name.uppercase().endsWith(".MP4")) {
                holder.info.text = TimeTool.showVideoTime(data.duration)
                holder.itemView.iv_video_time.isVisible = true
            } else {
                holder.info.text = ""
                holder.itemView.iv_video_time.isVisible = false
            }

            holder.itemView.iv_has_download.isVisible = isTS004Remote && data.hasDownload

            holder.itemView.iv_check.isVisible = isEditMode
            holder.itemView.iv_check.isSelected = selectList.contains(position)

            holder.img.setOnClickListener {
                if (isEditMode) {
                    if (selectList.contains(position)) {
                        selectList.remove(position)
                    } else {
                        selectList.add(position)
                    }
                    selectCallback?.invoke(selectList)

                    holder.itemView.iv_check.isSelected = selectList.contains(position)
                } else {
                    itemClickCallback?.invoke(position)
                }
            }
            holder.img.setOnLongClickListener {
                if (!isEditMode) {
                    selectList.add(position)
                    selectCallback?.invoke(selectList)
                    holder.itemView.iv_check.isVisible = true
                    holder.itemView.iv_check.isSelected = true
                    isEditMode = true
                    onLongEditListener?.invoke()
                }
                return@setOnLongClickListener true
            }
        } else if (holder is ItemHeadView) {
            holder.name.text = TimeTool.showDateType(data.timeMillis, 4)
            holder.name.setTextColor(0x80ffffff.toInt())
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ItemHeadView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.item_gallery_head_text
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: ImageView = itemView.item_gallery_img
        val info: TextView = itemView.item_gallery_text
    }


}