package com.topdon.lib.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.bean.ObserveBean
import com.topdon.lib.ui.R
import com.topdon.lib.ui.bean.ColorBean
import com.topdon.lib.ui.config.CameraHelp
import com.topdon.menu.constant.TargetType
import kotlinx.android.synthetic.main.ui_item_menu_second_view.view.*

@Deprecated("旧的标靶菜单，已重构过了")
class MenuTargetAdapter (val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var listener: ((code: Int) -> Unit)? = null

    /**
     * 设置指定选项的选中状态
     */
    fun setSelected(targetType: TargetType, isSelected: Boolean) {
        when (targetType) {
            TargetType.MODE -> secondBean[0].isSelect = isSelected
            TargetType.STYLE -> secondBean[1].isSelect = isSelected
            TargetType.COLOR -> secondBean[2].isSelect = isSelected
            TargetType.DELETE -> secondBean[3].isSelect = isSelected
            TargetType.HELP -> secondBean[4].isSelect = isSelected
        }
        notifyDataSetChanged()
    }

    private val secondBean = arrayListOf(
        ColorBean(R.drawable.selector_menu2_target_1_person, context.getString(R.string.main_tab_second_measure_mode), CameraHelp.TYPE_SET_MEASURE_MODE),
        ColorBean(R.drawable.selector_menu2_target_2_style, context.getString(R.string.main_tab_first_target), CameraHelp.TYPE_SET_TARGET_MODE),
//      ColorBean(R.drawable.ic_menu_second_zoom, context.getString(R.string.main_tab_second_zoom), CameraHelp.TYPE_SET_TARGET_ZOOM),
        ColorBean(R.drawable.selector_menu2_target_3_color, context.getString(R.string.main_tab_second_target_color), CameraHelp.TYPE_SET_TARGET_COLOR),
        ColorBean(R.drawable.selector_menu2_del, context.getString(R.string.thermal_delete), CameraHelp.TYPE_SET_TARGET_DELETE),
        ColorBean(R.drawable.selector_menu2_target_4_help, context.getString(R.string.main_tab_second_target_help), CameraHelp.TYPE_SET_TARGET_HELP),
    )

    /**
     * 刷新测量模式图标
     */
    fun upCurrentMeasureMode(measureMode: Int){
        secondBean.clear()
        when (measureMode) {
            ObserveBean.TYPE_MEASURE_PERSON -> {
                secondBean.add(ColorBean(R.drawable.selector_menu2_target_1_person, context.getString(R.string.main_tab_second_measure_mode), CameraHelp.TYPE_SET_MEASURE_MODE))
            }
            ObserveBean.TYPE_MEASURE_SHEEP -> {
                secondBean.add(ColorBean(R.drawable.selector_menu2_target_1_sheep, context.getString(R.string.main_tab_second_measure_mode), CameraHelp.TYPE_SET_MEASURE_MODE))
            }
            ObserveBean.TYPE_MEASURE_DOG -> {
                secondBean.add(ColorBean(R.drawable.selector_menu2_target_1_dog, context.getString(R.string.main_tab_second_measure_mode), CameraHelp.TYPE_SET_MEASURE_MODE))
            }
            ObserveBean.TYPE_MEASURE_BIRD -> {
                secondBean.add(ColorBean(R.drawable.selector_menu2_target_1_bird, context.getString(R.string.main_tab_second_measure_mode), CameraHelp.TYPE_SET_MEASURE_MODE))
            }
        }
        secondBean.add(ColorBean(R.drawable.selector_menu2_target_2_style, context.getString(R.string.main_tab_first_target), CameraHelp.TYPE_SET_TARGET_MODE))
        secondBean.add(ColorBean(R.drawable.selector_menu2_target_3_color, context.getString(R.string.main_tab_second_target_color), CameraHelp.TYPE_SET_TARGET_COLOR))
        secondBean.add(ColorBean(R.drawable.selector_menu2_del, context.getString(R.string.thermal_delete), CameraHelp.TYPE_SET_TARGET_DELETE))
        secondBean.add(ColorBean(R.drawable.selector_menu2_target_4_help, context.getString(R.string.main_tab_second_target_help), CameraHelp.TYPE_SET_TARGET_HELP))
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ui_item_menu_second_view, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemView) {
            val bean = secondBean[position]
            holder.name.text = bean.name
            holder.img.setImageResource(bean.res)

            holder.img.isSelected = bean.isSelect
            if (bean.isSelect) {
                holder.name.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                holder.name.setTextColor(ContextCompat.getColor(context, R.color.font_third_color))
            }

            holder.lay.setOnClickListener {
                listener?.invoke(bean.code)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return secondBean.size
    }

    inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lay: View = itemView.item_menu_tab_lay
        val img: ImageView = itemView.item_menu_tab_img
        val name: TextView = itemView.item_menu_tab_text
        init {
//            val canSeeCount = 4.5 //一屏占4个
//            val with = (ScreenUtils.getScreenWidth() / canSeeCount).toInt()
            itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//            val imageSize = (ScreenUtils.getScreenWidth() * 62 / 375f).toInt()
//            val layoutParams = itemView.item_menu_tab_img.layoutParams
//            layoutParams.width = imageSize
//            layoutParams.height = imageSize
//            itemView.item_menu_tab_img.layoutParams = layoutParams
        }
    }
}