package com.topdon.lib.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.bean.CameraItemBean
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.tools.DeviceTools
import com.topdon.lib.ui.R
import com.topdon.lib.ui.bean.TemperatureBean
import kotlinx.android.synthetic.main.ui_item_menu_five_view.view.*

@Deprecated("旧的温度档位菜单，已重构过了")
class MenuFiveNightAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onTempLevelListener: ((index: Int) -> Unit)? = null

    private var selectedCode = SaveSettingUtil.temperatureMode

    fun selected(code: Int) {
        selectedCode = code
        notifyDataSetChanged()
    }

    private val fiveBean = arrayListOf(
        TemperatureBean(
            R.drawable.selector_menu2_temp_level_1,
            context.getString(R.string.thermal_normal_temperature),
            getTempStr(-20, 150),
            CameraItemBean.TYPE_TMP_C
        ),
        if (DeviceTools.isTC001LiteConnect()) {
            TemperatureBean(
                R.drawable.selector_menu2_temp_level_1,
                context.getString(R.string.thermal_high_temperature),
                getTempStr(150, 450),
                CameraItemBean.TYPE_TMP_H
            )
        } else {
            TemperatureBean(
                R.drawable.selector_menu2_temp_level_1,
                context.getString(R.string.thermal_high_temperature),
                getTempStr(150, 550),
                CameraItemBean.TYPE_TMP_H
            )
        },
        TemperatureBean(
            R.drawable.selector_menu2_temp_level_2,
            context.getString(R.string.thermal_automatic),
            "",
            CameraItemBean.TYPE_TMP_ZD
        ),
    )

    private fun getTempStr(min: Int, max: Int): String = if (SharedManager.getTemperature() == 1) {
        "${min}\n~\n${max}°C"
    } else {
        "${(min * 1.8 + 32).toInt()}\n~\n${(max * 1.8 + 32).toInt()}°F"
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ui_item_menu_five_view, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            holder.img.setImageResource(fiveBean[position].res)
            holder.lay.setOnClickListener {
                onTempLevelListener?.invoke(fiveBean[position].code)
                selected(fiveBean[position].code)
            }
            holder.img.isSelected = fiveBean[position].code == selectedCode
            holder.name.text = fiveBean[position].name
            holder.info.text = fiveBean[position].info
            holder.name.isSelected = fiveBean[position].code == selectedCode
            holder.info.isSelected = fiveBean[position].code == selectedCode
            holder.name.setTextColor(
                if (fiveBean[position].code == selectedCode) ContextCompat.getColor(context, R.color.white)
                else ContextCompat.getColor(context, R.color.font_third_color)
            )
            holder.info.setTextColor(
                if (fiveBean[position].code == selectedCode) ContextCompat.getColor(context, R.color.color_FFBA42)
                else ContextCompat.getColor(context, R.color.font_third_color)
            )
        }
    }

    override fun getItemCount(): Int {
        return fiveBean.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        init {
//            val canSeeCount = itemCount.toFloat() //一屏可见的 item 数量，目前都是全都显示完
//            val with = (ScreenUtils.getScreenWidth() / canSeeCount).toInt()
//            itemView.layoutParams = ViewGroup.LayoutParams(with, ViewGroup.LayoutParams.WRAP_CONTENT)
//            val imageSize = (ScreenUtils.getScreenWidth() * 62 / 375f).toInt()
//            val layoutParams = itemView.item_menu_tab_fl.layoutParams
//            layoutParams.width = imageSize
//            layoutParams.height = imageSize
//            itemView.item_menu_tab_fl.layoutParams = layoutParams
//        }
        val lay: View = itemView.item_menu_tab_lay
        val img: ImageView = itemView.item_menu_tab_img
        val name: TextView = itemView.item_menu_tab_text
        val info: TextView = itemView.item_menu_tab_info_text
    }

}