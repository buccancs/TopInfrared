package com.topdon.menu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topdon.menu.databinding.ItemMenuBinding

/**
 * 除伪彩颜色外，所有菜单 Adapter 共有逻辑抽取.
 *
 * Created by LCG on 2024/11/29.
 */
internal abstract class BaseMenuAdapter : RecyclerView.Adapter<BaseMenuAdapter.ViewHolder>() {
    companion object {
        private const val VIEW_TYPE_DEFAULT = 0
        private const val VIEW_TYPE_FIRST = 1
        private const val VIEW_TYPE_LAST = 2
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> VIEW_TYPE_FIRST
        itemCount - 1 -> VIEW_TYPE_LAST
        else -> VIEW_TYPE_DEFAULT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val widthPixels: Int = parent.context.resources.displayMetrics.widthPixels

        //计算及设置图标尺寸
        val iconSize: Int = (widthPixels * 62 / 375f).toInt() // 62、375 是按 UI 图比例
        val iconParams: ViewGroup.LayoutParams = binding.ivIcon.layoutParams
        iconParams.width = iconSize
        iconParams.height = iconSize

        //计算及设置整体尺寸
        if (itemCount <= 4) {
            binding.root.layoutParams.width = (widthPixels / itemCount.toFloat()).toInt()
        } else {
            val bigMargin: Int = (widthPixels * 24 / 375f).toInt()  //按 UI 图最左最右间距为 24
            val smallMargin: Int = (widthPixels * 8 / 375f).toInt() //按 UI 图每个 item 间距为 16
            when (viewType) {
                VIEW_TYPE_FIRST -> binding.root.setPadding(bigMargin, 0, smallMargin, 0)
                VIEW_TYPE_LAST -> binding.root.setPadding(smallMargin, 0, bigMargin, 0)
                else -> binding.root.setPadding(smallMargin, 0, smallMargin, 0)
            }
        }
        return ViewHolder(binding)
    }

    class ViewHolder(val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root)
}