package com.topdon.lib.menu

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.view_menu_3d.view.*

class Menu3DView : ConstraintLayout, View.OnClickListener {

    var onVisualClickListener: ((position: Int) -> Unit)? = null
    
    var onMarkClickListener: ((position: Int) -> Unit)? = null
    
    var onPseudoClickListener: ((position: Int) -> Unit)? = null
    
    var onModeClickListener: ((position: Int) -> Unit)? = null

    private var selectIndex = -1

    private val visualAdapter: MenuAdapter
    
    private val markAdapter: MenuAdapter
    
    private val pseudoAdapter: MenuAdapter
    
    private val modeAdapter: MenuAdapter

    private val selectColor: Int = 0xffffffff.toInt()
    
    private val defaultColor: Int = 0x66ffffff

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        inflate(context, R.layout.view_menu_3d, this)
        setBackgroundColor(0xff16131e.toInt())

        view_menu1_visual.setOnClickListener(this)
        view_menu1_mark.setOnClickListener(this)
        view_menu1_pseudo.setOnClickListener(this)
        view_menu1_mode.setOnClickListener(this)

        visualAdapter = MenuAdapter(context, MenuAdapter.Type.VISUAL)
        markAdapter = MenuAdapter(context, MenuAdapter.Type.MARK)
        pseudoAdapter = MenuAdapter(context, MenuAdapter.Type.PSEUDO)
        modeAdapter = MenuAdapter(context, MenuAdapter.Type.MODE)
        visualAdapter.onItemClickListener = { onVisualClickListener?.invoke(it) }
        markAdapter.onItemClickListener = { onMarkClickListener?.invoke(it) }
        pseudoAdapter.onItemClickListener = { onPseudoClickListener?.invoke(it) }
        modeAdapter.onItemClickListener = { onModeClickListener?.invoke(it) }

        val orientation = if (ScreenUtil.isPortrait(context)) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
        recycler_view.layoutManager = LinearLayoutManager(context, orientation, false)
        switchFirstMenu(0)
    }

    override fun onClick(v: View?) {
        when (v) {
            view_menu1_visual -> switchFirstMenu(0)
            view_menu1_mark -> switchFirstMenu(1)
            view_menu1_pseudo -> switchFirstMenu(2)
            view_menu1_mode -> switchFirstMenu(3)
        }
    }

    private fun switchFirstMenu(index: Int) {
        if (selectIndex == index) {
            return
        }
        when (selectIndex) {
            0 -> {
                iv_menu1_visual.isSelected = false
                tv_menu1_visual.setTextColor(defaultColor)
            }
            1 -> {
                iv_menu1_mark.isSelected = false
                tv_menu1_mark.setTextColor(defaultColor)
            }
            2 -> {
                iv_menu1_pseudo.isSelected = false
                tv_menu1_pseudo.setTextColor(defaultColor)
            }
            3 -> {
                iv_menu1_mode.isSelected = false
                tv_menu1_mode.setTextColor(defaultColor)
            }
        }
        when (index) {
            0 -> {
                iv_menu1_visual.isSelected = true
                tv_menu1_visual.setTextColor(selectColor)
                recycler_view.adapter = visualAdapter
            }
            1 -> {
                iv_menu1_mark.isSelected = true
                tv_menu1_mark.setTextColor(selectColor)
                recycler_view.adapter = markAdapter
            }
            2 -> {
                iv_menu1_pseudo.isSelected = true
                tv_menu1_pseudo.setTextColor(selectColor)
                recycler_view.adapter = pseudoAdapter
            }
            3 -> {
                iv_menu1_mode.isSelected = true
                tv_menu1_mode.setTextColor(selectColor)
                recycler_view.adapter = modeAdapter
            }
        }
        this.selectIndex = index
    }
}