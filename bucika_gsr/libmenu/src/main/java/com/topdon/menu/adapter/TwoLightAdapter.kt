package com.topdon.menu.adapter

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.topdon.menu.R
import com.topdon.menu.constant.MenuType
import com.topdon.menu.constant.TwoLightType

/**
 * 测温模式-菜单3-双光 菜单所用 Adapter.
 *
 * - 单光：  画中画、融合度
 * - Lite： 画中画、融合度
 * - 双光：  双光1、双光2、红外、可见光、配准、画中画、融合度
 * - TC007：双光、红外、可见光、配准、画中画、融合度
 * - 2D编辑：无该菜单
 *
 * 单光、Lite：画中画、融合度 独立可选
 *
 * 双光：双光1、双光2、红外、可见光 互斥； 配准、画中画、融合度 独立可选
 *
 * TC007：双光、红外、可见光、画中画 互斥；配准、融合度 独立可选
 *
 * Created by LCG on 2024/11/20.
 */
@SuppressLint("NotifyDataSetChanged")
internal class TwoLightAdapter(private val menuType: MenuType) : BaseMenuAdapter() {

    /**
     * 双光菜单点击事件监听。
     */
    var onTwoLightListener: ((twoLightType: TwoLightType, isSelected: Boolean) -> Unit)? = null

    /**
     * 当前单选的双光类型
     * - 单光：  不应该使用这个属性
     * - Lite： 不应该使用这个属性
     * - 双光：  双光1、双光2、红外、可见光
     * - TC007：双光、红外、可见光、画中画
     */
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
                        //TC007 时无论双光1还是双光2都视为双光
                        data.isSelected = data.twoLightType == TwoLightType.TWO_LIGHT_2
                    } else {
                        data.isSelected = data.twoLightType == value
                    }
                }
            }
            notifyDataSetChanged()
        }

    /**
     * 设置多选状态
     * - 单光：  画中画、融合度
     * - Lite： 画中画、融合度
     * - 双光：  配准、画中画、融合度
     * - TC007：配准、、融合度
     */
    fun setSelected(twoLightType: TwoLightType, isSelected: Boolean) {
        if (twoLightType == TwoLightType.TWO_LIGHT_1 || twoLightType == TwoLightType.TWO_LIGHT_2) {//双光1、双光2
            return
        }
        if (twoLightType == TwoLightType.IR || twoLightType == TwoLightType.LIGHT) {//单红外、可见光
            return
        }
        if (menuType == MenuType.TC007 && twoLightType == TwoLightType.P_IN_P) {//TC007 时的画中画
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
            if (data.isSingle) {//单选
                if (!data.isSelected) {//单选的情况下重复点击忽略掉
                    twoLightType = data.twoLightType
                    onTwoLightListener?.invoke(data.twoLightType, true)
                }
            } else {//多选
                data.isSelected = !data.isSelected
                holder.binding.ivIcon.isSelected = data.isSelected
                holder.binding.tvText.isSelected = data.isSelected
                onTwoLightListener?.invoke(data.twoLightType, data.isSelected)
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    /**
     * @param isSingle 是否单选，目前只有1组互斥的单选，故而 Boolean 足够用了
     * @param isSelected 当前是否选中
     */
    data class Data(
        @StringRes val stringId: Int,
        @DrawableRes val drawableId: Int,
        val twoLightType: TwoLightType,
        val isSingle: Boolean,
        var isSelected: Boolean = false,
    )
}