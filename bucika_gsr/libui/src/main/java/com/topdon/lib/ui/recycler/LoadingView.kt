package com.topdon.lib.ui.recycler

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.topdon.lib.ui.R

/**
 * 自定义FooterView
 */
class LoadingView : LinearLayout {

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        inflate(context, R.layout.ui_footer_view, this)
    }

}