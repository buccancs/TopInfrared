package com.topdon.menu.adapter

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.menu.R
import com.topdon.menu.constant.MenuType
import com.topdon.menu.constant.TwoLightType

@SuppressLint("NotifyDataSetChanged")
internal class TwoLightAdapter(private val menuType: MenuType) : BaseMenuAdapter() {

    var onTwoLightListener: ((twoLightType: TwoLightType, isSelected: Boolean) -> Unit)? = null

    var twoLightType: TwoLightType
        get() {
            for (data in dataList) {
                if (data.isSingle && data.isSelected) {
                    return data.twoLightType
                }
            }
            return TwoLightType.TWO_LIGHT_1
        }
        set(value) {
            if (value == TwoLightType.CORRECT || value == TwoLightType.BLEND_EXTENT) {
                return
            }
            if (value == TwoLightType.P_IN_P && menuType != MenuType.TC007) {
                return
            }
            for (data in dataList) {
                if (data.isSingle) {
                    if (menuType == MenuType.TC007 && value == TwoLightType.TWO_LIGHT_1) {
                        data.isSelected = data.twoLightType == TwoLightType.TWO_LIGHT_2
                    } else {
                        data.isSelected = data.twoLightType == value
                    }
                }
            }
            notifyDataSetChanged()
        }

    fun setSelected(twoLightType: TwoLightType, isSelected: Boolean) {
        if (twoLightType == TwoLightType.TWO_LIGHT_1 || twoLightType == TwoLightType.TWO_LIGHT_2) {
            return
        }
        if (twoLightType == TwoLightType.IR || twoLightType == TwoLightType.LIGHT) {
            return
        }
        if (menuType == MenuType.TC007 && twoLightType == TwoLightType.P_IN_P) {
            return
        }
        for (data in dataList) {
            if (data.twoLightType == twoLightType) {
                data.isSelected = isSelected
            }
        }
        notifyDataSetChanged()
    }

    private val dataList: ArrayList<Data> = ArrayList(7)

    init {
        if (menuType == MenuType.DOUBLE_LIGHT || menuType == MenuType.TC007) {
            if (menuType == MenuType.DOUBLE_LIGHT) {
                dataList.add(Data(R.string.dual_menu_1, R.drawable.selector_menu2_two_light_1, TwoLightType.TWO_LIGHT_1, true))
                dataList.add(Data(R.string.dual_menu_2, R.drawable.selector_menu2_two_light_2, TwoLightType.TWO_LIGHT_2, true))
            } else {
                dataList.add(Data(R.string.menu_thermal_merge, R.drawable.selector_menu2_two_light_2, TwoLightType.TWO_LIGHT_2, true))
            }
            dataList.add(Data(R.string.menu_thermal_imaging, R.drawable.selector_menu2_two_light_3, TwoLightType.IR, true))
            dataList.add(Data(R.string.menu_thermal_visible_light, R.drawable.selector_menu2_two_light_4, TwoLightType.LIGHT, true))
            dataList.add(Data(R.string.menu_thermal_registration, R.drawable.selector_menu2_two_light_5, TwoLightType.CORRECT, false))
        }
        dataList.add(Data(R.string.thermal_picture_in_camera, R.drawable.selector_menu2_two_light_6, TwoLightType.P_IN_P, menuType == MenuType.TC007))
        dataList.add(Data(R.string.ios_double_light, R.drawable.selector_menu2_two_light_7, TwoLightType.BLEND_EXTENT, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: Data = dataList[position]
        holder.binding.ivIcon.setImageResource(data.drawableId)
        holder.binding.tvText.setText(data.stringId)
        holder.binding.ivIcon.isSelected = data.isSelected
        holder.binding.tvText.isSelected = data.isSelected
        holder.binding.clRoot.setOnClickListener {
            if (data.isSingle) {
                if (!data.isSelected) {
                    twoLightType = data.twoLightType
                    onTwoLightListener?.invoke(data.twoLightType, true)
                }
            } else {
                data.isSelected = !data.isSelected
                holder.binding.ivIcon.isSelected = data.isSelected
                holder.binding.tvText.isSelected = data.isSelected
                onTwoLightListener?.invoke(data.twoLightType, data.isSelected)
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    data class Data(
        @StringRes val stringId: Int,
        @DrawableRes val drawableId: Int,
        val twoLightType: TwoLightType,
        val isSingle: Boolean,
        var isSelected: Boolean = false,
    )
}