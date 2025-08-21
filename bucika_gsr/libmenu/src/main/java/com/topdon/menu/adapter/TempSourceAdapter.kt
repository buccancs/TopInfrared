package com.topdon.menu.adapter

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.menu.R

/**
 * 观测模式-菜单2-高低温源菜单 所用 Adapter，单选可全不选中.
 *
 * 动态识别、高温源、低温源
 *
 * Created by LCG on 2024/11/29.
 */
@SuppressLint("NotifyDataSetChanged")
internal class TempSourceAdapter : BaseMenuAdapter() {
    /**
     * 当前选中的选项 code.
     *
     * 由于历史遗留（已保存在 SharedPreferences 中），这里 code 取值为
     * - 什么都未选中：-1
     * - 动态识别：0
     * - 高温源：1
     * - 低温源：2
     */
    var selectCode: Int = -1
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    /**
     * 观测模式-菜单2-高低温源 点击事件监听，单选。
     */
    var onTempSourceListener: ((code: Int) -> Unit)? = null



    private val dataArray: Array<Data> = arrayOf(
        Data(R.string.main_tab_second_dynamic_recognition, R.drawable.selector_menu2_source_1_auto, 0),
        Data(R.string.main_tab_second_high_temperature_source, R.drawable.selector_menu2_source_2_high, 1),
        Data(R.string.main_tab_second_low_temperature_source, R.drawable.selector_menu2_source_3_low, 2),
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data: Data = dataArray[position]
        holder.binding.ivIcon.setImageResource(data.drawableId)
        holder.binding.tvText.setText(data.stringId)
        holder.binding.ivIcon.isSelected = data.code == selectCode
        holder.binding.tvText.isSelected = data.code == selectCode
        holder.binding.clRoot.setOnClickListener {
            selectCode = if (data.code == selectCode) -1 else data.code
            onTempSourceListener?.invoke(selectCode)
        }
    }

    override fun getItemCount(): Int = dataArray.size

    data class Data(@StringRes val stringId: Int, @DrawableRes val drawableId: Int, val code: Int)
}