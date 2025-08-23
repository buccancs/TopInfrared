package com.topdon.menu.adapter

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.menu.R
import com.topdon.menu.constant.TempPointType

@SuppressLint("NotifyDataSetChanged")
internal class TempPointAdapter : BaseMenuAdapter() {
    
    var onTempPointListener: ((type: TempPointType, isSelected: Boolean) -> Unit)? = null

    fun setSelected(tempPointType: TempPointType, isSelected: Boolean) {
        for (i in dataArray.indices) {
            if (dataArray[i].tempPointType == tempPointType) {
                dataArray[i].isSelected = isSelected
                notifyItemChanged(i)
                break
            }
        }
    }

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
                if (!data.isSelected) {
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
                if (data.isSelected) {
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