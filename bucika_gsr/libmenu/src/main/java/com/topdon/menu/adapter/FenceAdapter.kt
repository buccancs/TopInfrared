package com.topdon.menu.adapter

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.menu.constant.MenuType
import com.topdon.menu.R
import com.topdon.menu.constant.FenceType

/**
 * 点、线、面、全图、趋势图(可选)、删除 菜单 Adapter.
 *
 * - 单光：   点、线、面、全图、趋势图、删除
 * - 双光：   点、线、面、全图、趋势图、删除
 * - Lite：  点、线、面、全图、趋势图、删除
 * - TC007： 点、线、面、全图、趋势图、删除
 * - 2D 编辑：点、线、面、全图、删除
 *
 * 点、线、面、趋势图、全图 与 删除互斥
 *
 * 点、线、面、趋势图 互斥，全图独立可选
 *
 * Created by LCG on 2024/11/18.
 */
@SuppressLint("NotifyDataSetChanged")
internal class FenceAdapter(menuType: MenuType) : BaseMenuAdapter() {
    /**
     * 当前选中的菜单类型，若为 null 表示所有都未选中.
     */
    var selectType: FenceType? = null
        set(value) {
            when (value) {
                FenceType.FULL -> isFullSelect = true
                FenceType.DEL -> isFullSelect = false
                else -> {//点、线、面、趋势图，不会影响全图状态

                }
            }
            field = value
            notifyDataSetChanged()
        }
    /**
     * 全图是否已选中.
     */
    private var isFullSelect: Boolean = false

    /**
     * 菜单点击事件监听，目前都是单选，等后续有空重构了，再搞成 IOS 那样“全图”可以多选。
     */
    var onFenceListener: ((fenceType: FenceType, isSelected: Boolean) -> Unit)? = null



    private val dataList: ArrayList<Data> = ArrayList(6)

    init {
        dataList.add(Data(R.string.thermal_point, R.drawable.selector_menu2_fence_point, FenceType.POINT))
        dataList.add(Data(R.string.thermal_line, R.drawable.selector_menu2_fence_line, FenceType.LINE))
        dataList.add(Data(R.string.thermal_rect, R.drawable.selector_menu2_fence_rect, FenceType.RECT))
        dataList.add(Data(R.string.thermal_full_rect, R.drawable.selector_menu2_fence_full, FenceType.FULL))
        if (menuType != MenuType.GALLERY_EDIT) {//2D编辑的菜单没有趋势图
            dataList.add(Data(R.string.thermal_trend, R.drawable.selector_menu2_fence_trend, FenceType.TREND))
        }
        dataList.add(Data(R.string.thermal_delete, R.drawable.selector_menu2_del, FenceType.DEL))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: Data = dataList[position]
        holder.binding.ivIcon.setImageResource(data.drawableId)
        holder.binding.tvText.setText(data.stringId)
        holder.binding.ivIcon.isSelected = if (data.fenceType == FenceType.FULL) isFullSelect else data.fenceType == selectType
        holder.binding.tvText.isSelected = if (data.fenceType == FenceType.FULL) isFullSelect else data.fenceType == selectType
        holder.binding.clRoot.setOnClickListener {
            if (data.fenceType == FenceType.FULL) {
                isFullSelect = !isFullSelect
                onFenceListener?.invoke(data.fenceType, isFullSelect)
                if (selectType == FenceType.DEL) {
                    selectType = FenceType.FULL
                    notifyDataSetChanged()
                } else {
                    holder.binding.ivIcon.isSelected = isFullSelect
                    holder.binding.tvText.isSelected = isFullSelect
                }
            } else {
                if (data.fenceType != selectType) {
                    selectType = data.fenceType
                    onFenceListener?.invoke(data.fenceType, true)
                }
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    data class Data(@StringRes val stringId: Int, @DrawableRes val drawableId: Int, val fenceType: FenceType)
}