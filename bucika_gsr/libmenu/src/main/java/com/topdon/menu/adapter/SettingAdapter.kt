package com.topdon.menu.adapter

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.menu.R
import com.topdon.menu.constant.MenuType
import com.topdon.menu.constant.SettingType

@SuppressLint("NotifyDataSetChanged")
internal class SettingAdapter(menuType: MenuType = MenuType.SINGLE_LIGHT, isObserver: Boolean = false) : BaseMenuAdapter() {
    
    var onSettingListener: ((settingType: SettingType, isSelected: Boolean) -> Unit)? = null

    var rotateAngle: Int = 270
        set(value) {
            if (field != value) {
                field = value
                setSelected(SettingType.ROTATE, value != 270)
            }
        }

    fun setSelected(settingType: SettingType, isSelected: Boolean) {
        for (i in dataList.indices) {
            if (dataList[i].settingType == settingType) {
                dataList[i].isSelected = isSelected
                notifyItemChanged(i)
                break
            }
        }
    }

    private val dataList: ArrayList<Data> = ArrayList(7)

    init {
        if (isObserver) {
            dataList.add(Data(R.string.main_tab_second_compass, R.drawable.selector_menu2_setting_8, SettingType.COMPASS))
            dataList.add(Data(R.string.thermal_rotate, R.drawable.selector_menu2_setting_4, SettingType.ROTATE))
            dataList.add(Data(R.string.mirror, R.drawable.selector_menu2_setting_5, SettingType.MIRROR))
            dataList.add(Data(R.string.thermal_contrast, R.drawable.selector_menu2_setting_2, SettingType.CONTRAST))
        } else {
            if (menuType == MenuType.GALLERY_EDIT) {
                dataList.add(Data(R.string.temp_alarm_alarm, R.drawable.selector_menu2_setting_6, SettingType.ALARM))
                dataList.add(Data(R.string.menu_thermal_font, R.drawable.selector_menu2_setting_7, SettingType.FONT))
                dataList.add(Data(R.string.app_watemarking, R.drawable.selector_menu2_setting_9, SettingType.WATERMARK))
            } else {
                dataList.add(Data(R.string.thermal_pseudo, R.drawable.selector_menu2_setting_1, SettingType.PSEUDO_BAR))
                dataList.add(Data(R.string.thermal_contrast, R.drawable.selector_menu2_setting_2, SettingType.CONTRAST))
                if (menuType != MenuType.Lite) {
                    dataList.add(Data(R.string.thermal_sharpen, R.drawable.selector_menu2_setting_3, SettingType.DETAIL))
                }
                dataList.add(Data(R.string.temp_alarm_alarm, R.drawable.selector_menu2_setting_6, SettingType.ALARM))
                if (menuType != MenuType.TC007) {
                    dataList.add(Data(R.string.thermal_rotate, R.drawable.selector_menu2_setting_4, SettingType.ROTATE))
                }
                dataList.add(Data(R.string.menu_thermal_font, R.drawable.selector_menu2_setting_7, SettingType.FONT))
                if (menuType != MenuType.DOUBLE_LIGHT) {
                    dataList.add(Data(R.string.mirror, R.drawable.selector_menu2_setting_5, SettingType.MIRROR))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: Data = dataList[position]
        holder.binding.ivIcon.setImageResource(data.drawableId)
        holder.binding.tvText.setText(data.stringId)
        if (data.settingType == SettingType.ROTATE) {
            when (rotateAngle) {
                0 -> holder.binding.ivIcon.setImageLevel(270)
                90 -> holder.binding.ivIcon.setImageLevel(180)
                180 -> holder.binding.ivIcon.setImageLevel(90)
                270 -> holder.binding.ivIcon.setImageLevel(0)
            }
        } else {
            holder.binding.ivIcon.isSelected = data.isSelected
        }
        holder.binding.tvText.isSelected = data.isSelected
        holder.binding.clRoot.setOnClickListener {
            onSettingListener?.invoke(data.settingType, data.isSelected)
        }
    }

    override fun getItemCount(): Int = dataList.size

    data class Data(
        @StringRes val stringId: Int,
        @DrawableRes val drawableId: Int,
        val settingType: SettingType,
        var isSelected: Boolean = false,
    )
}