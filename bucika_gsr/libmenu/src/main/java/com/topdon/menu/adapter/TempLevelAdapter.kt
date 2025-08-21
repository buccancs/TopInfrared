package com.topdon.menu.adapter

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.menu.R
import com.topdon.menu.constant.MenuType

/**
 * 测温模式-菜单6-高低温档 菜单所用 Adapter，单选且必须选中其中一个.
 *
 * 低温档(高增益)、高温档(低增益)、自动切换
 *
 * Created by LCG on 2024/11/28.
 */
@SuppressLint("NotifyDataSetChanged")
internal class TempLevelAdapter(menuType: MenuType) : BaseMenuAdapter() {
    /**
     * 是否使用华氏度作为单位
     *
     * true-华氏度 false-摄氏度
     */
    var isUnitF = false
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    /**
     * 当前选中的档位 code.
     *
     * 由于历史遗留（已保存在 SharedPreferences 中），这里 code 取值为
     * - 自动切换：-1
     * - 高温(低增益)：0
     * - 常温(高增益)：1
     */
    var selectCode: Int = 1
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    /**
     * 菜单点击事件监听，单选。
     */
    var onTempLevelListener: ((code: Int) -> Unit)? = null



    private val dataList: ArrayList<Data> = ArrayList(6)

    init {
        dataList.add(Data(R.string.thermal_normal_temperature, R.drawable.selector_menu2_temp_level_1, IntRange(-20, 150), 1))
        if (menuType == MenuType.Lite) {
            dataList.add(Data(R.string.thermal_high_temperature, R.drawable.selector_menu2_temp_level_1, IntRange(150, 450), 0))
        } else {
            dataList.add(Data(R.string.thermal_high_temperature, R.drawable.selector_menu2_temp_level_1, IntRange(150, 550), 0))
        }
        dataList.add(Data(R.string.thermal_automatic, R.drawable.selector_menu2_temp_level_2, code = -1))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: Data = dataList[position]
        holder.binding.ivIcon.setImageResource(data.drawableId)
        holder.binding.tvText.setText(data.stringId)
        holder.binding.tvIconText.text = data.range?.getTempStr(isUnitF) ?: ""
        holder.binding.ivIcon.isSelected = data.code == selectCode
        holder.binding.tvText.isSelected = data.code == selectCode
        holder.binding.tvIconText.isSelected = data.code == selectCode
        holder.binding.clRoot.setOnClickListener {
            if (data.code != selectCode) {
                selectCode = data.code
                onTempLevelListener?.invoke(data.code)
            }
        }
    }

    private fun IntRange.getTempStr(isUnitF: Boolean): String = if (isUnitF) {
        "${c2f(start)}\n~\n${c2f(endInclusive)}°F"
    } else {
        "${start}\n~\n${endInclusive}°C"
    }

    /**
     * 将指定 摄氏度°C 转换为 华氏度°F
     */
    private fun c2f(cValue: Int): Int = (cValue * 1.8f + 32).toInt()

    override fun getItemCount(): Int = dataList.size

    data class Data(@StringRes val stringId: Int, @DrawableRes val drawableId: Int, val range: IntRange? = null, val code: Int)
}