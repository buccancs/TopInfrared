package com.topdon.lib.core.view

import android.content.Context
import android.util.AttributeSet

class MainTitleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : TitleView(context, attrs) {


    override fun initView() {
        tvLeft = addTextView(context)
        tvRight1 = addTextView(context)
        tvRight2 = addTextView(context,2f,40f)
        tvRight3 = addTextView(context)
        tvTitle = addTextView(context)
    }

}