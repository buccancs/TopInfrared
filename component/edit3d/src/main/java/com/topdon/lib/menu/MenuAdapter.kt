package com.topdon.lib.menu

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.item_menu_3d.view.*

class MenuAdapter(val context: Context, val type: Type) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {
    
    private var selectIndex = 0

    var onItemClickListener: ((position: Int) -> Unit)? = null

    init {
        if (type == Type.VISUAL){
        }else if (type == Type.MARK){
            selectIndex = -1
        } else if (type == Type.MODE) {
            selectIndex = 2
        }
    }

    companion object {
        private val VISUAL_ARRAY = arrayOf(
            ItemData(R.string.menu_3d_visual_3d, R.drawable.selector_menu2_visual_3d),
            ItemData(R.string.menu_3d_visual_top, R.drawable.selector_menu2_visual_top),
            ItemData(R.string.menu_3d_visual_left, R.drawable.selector_menu2_visual_left),
            ItemData(R.string.menu_3d_visual_right, R.drawable.selector_menu2_visual_right),
            ItemData(R.string.menu_3d_visual_center, R.drawable.selector_menu2_visual_center),
        )
        private val MARK_ARRAY = arrayOf(
            ItemData(R.string.pseudo_custom_title, R.drawable.selector_menu2_mark_custom),
            ItemData(R.string.thermal_high_temperature, R.drawable.selector_menu2_mark_max_temp),
            ItemData(R.string.app_Low_temperature, R.drawable.selector_menu2_mark_min_temp),
            ItemData(R.string.report_delete, R.drawable.selector_menu2_mark_del),
        )
        private val PSEUDO_ARRAY = arrayOf(
            ItemData(R.string.color_p3),
            ItemData(R.string.pseudo_type_black_red),
            ItemData(R.string.pseudo_type_nature),
            ItemData(R.string.pseudo_type_magma),
            ItemData(R.string.color_p8_ios),
        )
        private val MODE_ARRAY = arrayOf(
            ItemData(R.string.thermal_point, R.drawable.selector_menu2_mode_point),
            ItemData(R.string.thermal_line, R.drawable.selector_menu2_mode_line),
            ItemData(R.string.thermal_rect, R.drawable.selector_menu2_mode_rect),
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_menu_3d, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         if(type == Type.PSEUDO){
            holder.itemView.tv_menu.visibility = View.GONE
            holder.itemView.iv_pseudo.visibility = View.VISIBLE
        }else if(type == Type.MODE){
            holder.itemView.tv_menu.visibility = View.VISIBLE
            holder.itemView.iv_pseudo.visibility = View.GONE
        }
        val itemArray = when (type) {
            Type.VISUAL -> VISUAL_ARRAY
            Type.MARK -> MARK_ARRAY
            Type.PSEUDO -> PSEUDO_ARRAY
            Type.MODE -> MODE_ARRAY
        }
        holder.itemView.tv_menu.isSelected = position == selectIndex
        holder.itemView.iv_menu.isSelected = position == selectIndex
        holder.itemView.tv_menu.setText(itemArray[position].textResId)
        holder.itemView.iv_menu.setImageResource(itemArray[position].imgResId)

        if (type == Type.PSEUDO) {
            val drawable = when (position) {
                0 -> buildRectDrawable(selectIndex == 0, 0xff0000ff.toInt(), 0xffff0000.toInt(), 0xffffff00.toInt())
                1 -> buildRectDrawable(selectIndex == 1, 0xff000000.toInt(), 0xffffffff.toInt(), 0xffff0000.toInt())
                2 -> buildRectDrawable(selectIndex == 2, 0xff0000ff.toInt(), 0xff00ff00.toInt(), 0xffffff00.toInt(), 0xffff0000.toInt())
                3 -> buildRectDrawable(selectIndex == 3, 0xff000000.toInt(), 0xffff0000.toInt())
                else  -> buildRectDrawable(selectIndex == position, 0xff0000ff.toInt(), 0xffffff00.toInt())
            }
            holder.itemView.view_pseudo.background = drawable
            holder.itemView.iv_pseudo.visibility = if(position == selectIndex) View.VISIBLE else View.GONE
        }

        if (type == Type.MARK && position == MARK_ARRAY.size - 1) {
            holder.itemView.tv_menu.setTextColor(0x66ffffff)
        }
    }

    private fun buildRectDrawable(isSelect: Boolean, vararg color: Int): Drawable {
        val drawable = GradientDrawable()
        drawable.shape = GradientDrawable.RECTANGLE
        drawable.cornerRadius = SizeUtils.dp2px(10f).toFloat()
        drawable.colors = color
        drawable.orientation = GradientDrawable.Orientation.BOTTOM_TOP

        if (isSelect) {
            val strokeWidth = SizeUtils.dp2px(2f)
            val width = SizeUtils.dp2px(50f)
            val height = SizeUtils.dp2px(60f )
            val selectDrawable = GradientDrawable()
            selectDrawable.shape = GradientDrawable.RECTANGLE
            selectDrawable.setSize(width, height)
            selectDrawable.cornerRadius = SizeUtils.dp2px(10f).toFloat()
            selectDrawable.setColor(0xffffffff.toInt())

            val layerDrawable = LayerDrawable(arrayOf(selectDrawable, drawable))
            layerDrawable.setLayerInset(1, strokeWidth, strokeWidth, strokeWidth, strokeWidth)
            return layerDrawable
        }
        return drawable
    }

    override fun getItemCount() = when (type) {
        Type.VISUAL -> VISUAL_ARRAY.size
        Type.MARK -> MARK_ARRAY.size
        Type.PSEUDO -> PSEUDO_ARRAY.size
        Type.MODE -> MODE_ARRAY.size
    }

    @SuppressLint("NotifyDataSetChanged")
    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        init {
            val canSeeCount = itemCount.toFloat()
            if (ScreenUtil.isPortrait(context)) {
                val with = (ScreenUtil.getScreenWidth(context)/ canSeeCount).toInt()
                rootView.layoutParams = ViewGroup.LayoutParams(with, ViewGroup.LayoutParams.WRAP_CONTENT)

                val imageSize = (ScreenUtil.getScreenWidth(context) * 62 / 375f).toInt()
                val layoutParams = rootView.iv_menu.layoutParams
                layoutParams.width = imageSize
                layoutParams.height = imageSize
                rootView.iv_menu.layoutParams = layoutParams
            } else {
                val constraintSet = ConstraintSet()
                constraintSet.clone(rootView.cl_root)
                constraintSet.setDimensionRatio(R.id.view_bg, (136 * canSeeCount / 436).toString())
                constraintSet.applyTo(rootView.cl_root)
            }

            rootView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    selectIndex = position
                    notifyDataSetChanged()
                    onItemClickListener?.invoke(position)
                }
            }
        }
    }

    data class ItemData(@StringRes val textResId: Int, @DrawableRes val imgResId: Int = 0)

    enum class Type {
        
        VISUAL,

        MARK,

        PSEUDO,

        MODE,
    }
}