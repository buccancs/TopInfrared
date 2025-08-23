package com.topdon.menu.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.topdon.menu.databinding.ItemMenuBinding

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

        val iconSize: Int = (widthPixels * 62 / 375f).toInt()
        val iconParams: ViewGroup.LayoutParams = binding.ivIcon.layoutParams
        iconParams.width = iconSize
        iconParams.height = iconSize

        if (itemCount <= 4) {
            binding.root.layoutParams.width = (widthPixels / itemCount.toFloat()).toInt()
        } else {
            val bigMargin: Int = (widthPixels * 24 / 375f).toInt()
            val smallMargin: Int = (widthPixels * 8 / 375f).toInt()
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