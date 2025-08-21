package com.topdon.menu.adapter

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.menu.R
import com.topdon.menu.constant.TempPointType

/**
 * 观测模式-菜单5-高低温点 菜单所用 Adapter，按旧逻辑存在全部未选择的状态。
 *
 * - 高温点、低温点 互相独立，可多选
 * - {高温点、低温点} 与 删除 互斥
 *
 * Created by LCG on 2024/11/28.
 */
@SuppressLint("NotifyDataSetChanged")
internal class TempPointAdapter : BaseMenuAdapter() {
    /**
     * 观测模式-菜单5-高低温点 点击事件监听.
     */
    var onTempPointListener: ((type: TempPointType, isSelected: Boolean) -> Unit)? = null

    /**
     * 设置 高温点 或 低稳点 的选中状态。
     */
    fun setSelected(tempPointType: TempPointType, isSelected: Boolean) {
        for (i in dataArray.indices) {
            if (dataArray[i].tempPointType == tempPointType) {
                dataArray[i].isSelected = isSelected
                notifyItemChanged(i)
                break
            }
        }
    }

    /**
     * 清除所有菜单的选中状态。
     * 这里维持原有逻辑，后续考虑是否直接给选中删除得了。
     */
    fun clearAllSelect() {
        for (data in dataArray) {
            data.isSelected = false
        }
        notifyDataSetChanged()
    }


    private val dataArray: Array<Data> = arrayOf(
        Data(R.string.main_tab_second_high_temperature_point, R.drawable.selector_menu2_temp_point_1, TempPointType.HIGH),
        Data(R.string.main_tab_second_low_temperature_point, R.drawable.selector_menu2_temp_point_2, TempPointType.LOW),
        Data(R.string.thermal_delete, R.drawable.selector_menu2_del, TempPointType.DELETE),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: Data = dataArray[position]
        holder.binding.ivIcon.setImageResource(data.drawableId)
        holder.binding.tvText.setText(data.stringId)
        holder.binding.ivIcon.isSelected = data.isSelected
        holder.binding.tvText.isSelected = data.isSelected
        holder.binding.clRoot.setOnClickListener {
            if (data.tempPointType == TempPointType.DELETE) {
                if (!data.isSelected) {//选中时再次删除没卵用，未选中时才处理
                    for (temp in dataArray) {
                        temp.isSelected = temp.tempPointType == TempPointType.DELETE
                    }
                    notifyDataSetChanged()
                    onTempPointListener?.invoke(TempPointType.DELETE, true)
                }
            } else {
                data.isSelected = !data.isSelected
                holder.binding.ivIcon.isSelected = data.isSelected
                holder.binding.tvText.isSelected = data.isSelected
                if (data.isSelected) {//选中高温点、低温点时要把“删除”设为未选中；取消选中时不耦合删除
                    for (i in dataArray.indices) {
                        if (dataArray[i].tempPointType == TempPointType.DELETE && dataArray[i].isSelected) {
                            dataArray[i].isSelected = false
                            notifyItemChanged(i)
                        }
                    }
                }
                onTempPointListener?.invoke(data.tempPointType, data.isSelected)
            }
        }
    }

    override fun getItemCount(): Int = dataArray.size

    data class Data(
        @StringRes val stringId: Int,
        @DrawableRes val drawableId: Int,
        val tempPointType: TempPointType,
        var isSelected: Boolean = false,
    )
}