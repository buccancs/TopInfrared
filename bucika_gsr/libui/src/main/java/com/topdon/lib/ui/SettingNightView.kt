package com.topdon.lib.ui

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.ui_setting_view.view.*

class SettingNightView : LinearLayout {

    var isRightArrowVisible: Boolean
        get() = endImg.isVisible
        set(value) {
            endImg.isVisible = value
        }

    fun setRightTextId(@StringRes resId: Int) {
        val tvEnd: TextView = findViewById(R.id.tv_end)
        tvEnd.isVisible = resId != 0
        if (resId != 0) {
            tvEnd.setText(resId)
        }
    }



    private var iconRes: Int = 0
    private var contentStr: String = ""
    private var moreShow: Boolean = true
    private var lineShow: Boolean = false
    private var iconShow: Boolean = false

    private lateinit var headImg: ImageView
    private lateinit var endImg: ImageView
    private lateinit var contentText: TextView

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingNightView)
        for (i in 0 until ta.indexCount) {
            when (ta.getIndex(i)) {
                R.styleable.SettingNightView_setting_icon_night -> iconRes =
                    ta.getResourceId(
                        R.styleable.SettingNightView_setting_icon_night,
                        R.drawable.ic_setting_default_svg
                    )
                R.styleable.SettingNightView_setting_text_night -> contentStr =
                    ta.getString(R.styleable.SettingNightView_setting_text_night).toString()
                R.styleable.SettingNightView_setting_more_night -> moreShow =
                    ta.getBoolean(R.styleable.SettingNightView_setting_more_night, true)
                R.styleable.SettingNightView_setting_line_night -> lineShow =
                    ta.getBoolean(R.styleable.SettingNightView_setting_line_night, false)
                R.styleable.SettingNightView_setting_icon_show_night -> iconShow =
                    ta.getBoolean(R.styleable.SettingNightView_setting_icon_show_night, false)
            }
        }
        ta.recycle()
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun initView() {
        inflate(context, R.layout.ui_setting_view_night, this)
        contentText = findViewById(R.id.item_setting_text)
        headImg = findViewById(R.id.item_setting_image)
        endImg = findViewById(R.id.item_setting_end_image)

        headImg.setImageResource(iconRes)
        if (iconShow) {
            headImg.visibility = View.VISIBLE
        } else {
            headImg.visibility = View.GONE
        }
        contentText.text = contentStr
        if (moreShow) {
            endImg.visibility = View.VISIBLE
        } else {
            endImg.visibility = View.GONE
        }
        item_setting_line.visibility = if (lineShow) View.VISIBLE else View.GONE
    }
}