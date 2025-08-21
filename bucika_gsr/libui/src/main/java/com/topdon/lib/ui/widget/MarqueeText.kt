package com.topdon.lib.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MarqueeText : AppCompatTextView {

    constructor (context: Context) : super(context)


    constructor (context: Context, attrs: AttributeSet) : super(context, attrs)


    constructor (context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )


    override fun isFocused(): Boolean {
        return true
    }

}