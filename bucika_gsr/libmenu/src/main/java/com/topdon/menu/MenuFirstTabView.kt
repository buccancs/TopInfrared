package com.topdon.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.topdon.menu.databinding.ViewMenuFirstTabBinding

/**
 * 热成像页面所用底部菜单栏.
 *
 * 共有两种模式：
 * - 测温模式：拍照、点线面、双光、伪彩、设置、高低温档
 * - 观测模式：拍照、高低温源、伪彩、标靶、高低温点、设置
 */
class MenuFirstTabView : FrameLayout, View.OnClickListener {

    /**
     * 当前选中那个 tab，取值 `[0,5]`
     */
    var selectPosition = -1
        set(value) {
            if (field != value) {
                field = value
                binding.ivMenu1.isSelected = value == 0
                binding.ivMenu2.isSelected = value == 1
                binding.ivMenu3.isSelected = value == 2
                binding.ivMenu4.isSelected = value == 3
                binding.ivMenu5.isSelected = value == 4
                binding.ivMenu6.isSelected = value == 5
            }
        }

    /**
     * 是否观测模式，观测模式的图标不同.
     */
    var isObserveMode = false
        set(value) {
            if (field != value) {
                field = value
                binding.ivMenu2.setImageResource(if (value) R.drawable.selector_menu_first_observe_2 else R.drawable.selector_menu_first_2_5)
                binding.ivMenu3.setImageResource(if (value) R.drawable.selector_menu_first_4_3 else R.drawable.selector_menu_first_normal_3)
                binding.ivMenu4.setImageResource(if (value) R.drawable.selector_menu_first_observe_4 else R.drawable.selector_menu_first_4_3)
                binding.ivMenu5.setImageResource(if (value) R.drawable.selector_menu_first_2_5 else R.drawable.selector_menu_first_5_6)
                binding.ivMenu6.setImageResource(if (value) R.drawable.selector_menu_first_5_6 else R.drawable.selector_menu_first_normal_6)
                selectPosition = 0
            }
        }

    var onTabClickListener: ((v: MenuFirstTabView) -> Unit)? = null



    private lateinit var binding: ViewMenuFirstTabBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        if (isInEditMode) {
            LayoutInflater.from(context).inflate(R.layout.view_menu_first_tab, this, true)
        } else {
            binding = ViewMenuFirstTabBinding.inflate(LayoutInflater.from(context), this, true)

            selectPosition = 0
            binding.clMenu1.setOnClickListener(this)
            binding.clMenu2.setOnClickListener(this)
            binding.clMenu3.setOnClickListener(this)
            binding.clMenu4.setOnClickListener(this)
            binding.clMenu5.setOnClickListener(this)
            binding.clMenu6.setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.clMenu1 -> selectPosition = 0
            binding.clMenu2 -> selectPosition = 1
            binding.clMenu3 -> selectPosition = 2
            binding.clMenu4 -> selectPosition = 3
            binding.clMenu5 -> selectPosition = 4
            binding.clMenu6 -> selectPosition = 5
        }
        onTabClickListener?.invoke(this)
    }
}