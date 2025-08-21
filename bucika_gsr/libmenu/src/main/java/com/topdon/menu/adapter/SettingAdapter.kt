package com.topdon.menu.adapter

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.menu.R
import com.topdon.menu.constant.MenuType
import com.topdon.menu.constant.SettingType

/**
 * 设置菜单所用 Adapter，所有选项互相独立，可多选.
 *
 * - 单光：   伪彩条、对比度、锐度、警示、旋转、字体、镜像
 * - 双光：   伪彩条、对比度、锐度、警示、旋转、字体
 * - Lite：  伪彩条、对比度、警示、旋转、字体、镜像
 * - TC007： 伪彩条、对比度、锐度、警示、字体、镜像
 * - 2D 编辑：警示、字体、水印
 *
 * - TS001 观测：指南针、旋转、镜像、对比度
 *
 * Created by LCG on 2024/11/28.
 */
@SuppressLint("NotifyDataSetChanged")
internal class SettingAdapter(menuType: MenuType = MenuType.SINGLE_LIGHT, isObserver: Boolean = false) : BaseMenuAdapter() {
    /**
     * 设置菜单点击事件监听。
     * isSelected：点击时是否处于选中状态
     */
    var onSettingListener: ((settingType: SettingType, isSelected: Boolean) -> Unit)? = null


    /**
     * 这里有几个坑：
     * - 对于机芯而言，256x192 横屏尺寸才是旋转角度为 0 的未旋转状态；
     * 对于APP而言，192x256 竖屏尺寸(机芯旋转角度270)才是旋转角度为 0 的未旋转状态。
     * - 对某供应商而言，机芯里的旋转角度是逆时针旋转角度，而非一般理解的顺时针旋转角度。
     *
     * 考虑到旧代码兼容，这个属性用来放 **机芯旋转角度**
     */
    var rotateAngle: Int = 270
        set(value) {
            if (field != value) {
                field = value
                setSelected(SettingType.ROTATE, value != 270)
            }
        }

    /**
     * 设置指定选项的选中状态，旋转不要调这个方法，因为旋转有 4 个状态
     */
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
            if (menuType == MenuType.GALLERY_EDIT) {//2D编辑
                dataList.add(Data(R.string.temp_alarm_alarm, R.drawable.selector_menu2_setting_6, SettingType.ALARM))
                dataList.add(Data(R.string.menu_thermal_font, R.drawable.selector_menu2_setting_7, SettingType.FONT))
                dataList.add(Data(R.string.app_watemarking, R.drawable.selector_menu2_setting_9, SettingType.WATERMARK))
            } else {
                dataList.add(Data(R.string.thermal_pseudo, R.drawable.selector_menu2_setting_1, SettingType.PSEUDO_BAR))
                dataList.add(Data(R.string.thermal_contrast, R.drawable.selector_menu2_setting_2, SettingType.CONTRAST))
                if (menuType != MenuType.Lite) {// Lite 没有细节(锐度)
                    dataList.add(Data(R.string.thermal_sharpen, R.drawable.selector_menu2_setting_3, SettingType.DETAIL))
                }
                dataList.add(Data(R.string.temp_alarm_alarm, R.drawable.selector_menu2_setting_6, SettingType.ALARM))
                if (menuType != MenuType.TC007) {// TC007 没有旋转
                    dataList.add(Data(R.string.thermal_rotate, R.drawable.selector_menu2_setting_4, SettingType.ROTATE))
                }
                dataList.add(Data(R.string.menu_thermal_font, R.drawable.selector_menu2_setting_7, SettingType.FONT))
                if (menuType != MenuType.DOUBLE_LIGHT) {// TC001 Plus 没有镜像
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
            //警示、字体、水印是以生效才视为高亮选中的，这里先保持旧代码逻辑，
            //菜单的选中刷新丢给上层的 listener 去做，后面有空再考虑更改
//            data.isSelected = !data.isSelected
//            holder.binding.ivIcon.isSelected = data.isSelected
//            holder.binding.tvText.isSelected = data.isSelected
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