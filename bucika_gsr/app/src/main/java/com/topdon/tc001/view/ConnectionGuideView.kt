package com.topdon.tc001.view

import android.content.Context
import android.content.res.TypedArray
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.topdon.tc001.R


class ConnectionGuideView: LinearLayout {
    private var iconRes: Int = 0
    private var contentStr: String = ""
    private var iconShow: Boolean = false
    private lateinit var guideIcon: ImageView
    private lateinit var contentText: TextView

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val ta: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ConnectionGuideView)
        for (i in 0 until ta.indexCount) {
            when (ta.getIndex(i)) {
                R.styleable.ConnectionGuideView_guide_icon ->iconRes =
                    ta.getResourceId(R.styleable.ConnectionGuideView_guide_icon, 0)
               R.styleable.ConnectionGuideView_guide_text -> contentStr =
                    ta.getString(R.styleable.ConnectionGuideView_guide_text).toString()
                R.styleable.ConnectionGuideView_guide_icon_show -> iconShow =
                    ta.getBoolean(R.styleable.ConnectionGuideView_guide_icon_show, false)
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
        inflate(context, R.layout.ui_main_connection_guide, this)
        contentText = findViewById(R.id.tv_content)
        guideIcon = findViewById(R.id.iv_icon)
        guideIcon.setImageResource(iconRes)
        contentText.text = contentStr
        guideIcon.visibility = if(iconShow)View.VISIBLE else View.GONE
    }

    fun setText(text: CharSequence?) {
        if (contentText == null || TextUtils.isEmpty(text)) return
        contentText.text = text
        contentText.movementMethod = LinkMovementMethod.getInstance()
    }

    fun getText(): String{
        if (contentText == null) return ""
        return contentText.text.toString()
    }

    fun setHighlightColor(color: Int){
        if (contentText == null) return
        contentText.highlightColor = color
    }

    fun getCompoundDrawables(content : String){
        if (contentText == null) return
        var mContent = "$content  "//插入空格是为了后面替换图片
        val spannableString = SpannableString(mContent)
        val drawable = context.getDrawable(R.drawable.ic_connection_press_tip)
        drawable!!.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
        spannableString.setSpan(ImageSpan(drawable), mContent.length - 1, mContent.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        contentText.text = spannableString
    }

}