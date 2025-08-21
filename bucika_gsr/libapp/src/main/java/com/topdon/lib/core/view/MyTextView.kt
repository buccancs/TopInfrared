package com.topdon.lib.core.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.R

/**
 * 魔改 TextView.
 *
 * 原生 TextView 附加的 drawable 尺寸不可设置，这个 TextView 可以设置高度，宽度等比缩放.
 *
 * 其中 wrap_content 使用原生逻辑，不设置则使用 textSize（默认），指定值>0则使用指定值.
 *
 * Created by chenggeng.lin on 2023/11/21.
 */
class MyTextView : AppCompatTextView {
    /**
     * drawableTop 高度，单位 **px**
     */
    private var topHeight = 0
    /**
     * drawableBottom 高度，单位 **px**
     */
    private var bottomHeight = 0
    /**
     * drawableStart 高度，单位 **px**
     */
    private var startHeight = 0
    /**
     * drawableEnd 高度，单位 **px**
     */
    private var endHeight = 0
    /**
     * drawableLeft 高度，单位 **px**
     */
    private var leftHeight = 0
    /**
     * drawableRight 高度，单位 **px**
     */
    private var rightHeight = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyTextView, defStyleAttr, 0)
        val drawableHeight = typedArray.getDimensionPixelSize(R.styleable.MyTextView_drawable_height, textSize.toInt())
        topHeight = typedArray.getDimensionPixelSize(R.styleable.MyTextView_top_height, drawableHeight)
        bottomHeight = typedArray.getDimensionPixelSize(R.styleable.MyTextView_bottom_height, drawableHeight)
        startHeight = typedArray.getDimensionPixelSize(R.styleable.MyTextView_start_height, drawableHeight)
        endHeight = typedArray.getDimensionPixelSize(R.styleable.MyTextView_end_height, drawableHeight)
        leftHeight = typedArray.getDimensionPixelSize(R.styleable.MyTextView_left_height, drawableHeight)
        rightHeight = typedArray.getDimensionPixelSize(R.styleable.MyTextView_right_height, drawableHeight)
        typedArray.recycle()

        //取出设置的各个Drawable
        val drawables = compoundDrawables
        val relativeDrawables = compoundDrawablesRelative
        val left = drawables[0]
        val top = drawables[1]
        val right = drawables[2]
        val bottom = drawables[3]
        val start = relativeDrawables[0]
        val end = relativeDrawables[2]

        if (start != null || end != null) {
            setCompoundDrawablesRelative(start, top, end, bottom)
        } else {
            setCompoundDrawables(left, top, right, bottom)
        }
    }

    override fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        setDrawableBounds(top, topHeight)
        setDrawableBounds(bottom, bottomHeight)
        setDrawableBounds(left, leftHeight)
        setDrawableBounds(right, rightHeight)
        super.setCompoundDrawables(left, top, right, bottom)
    }

    override fun setCompoundDrawablesWithIntrinsicBounds(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        setCompoundDrawables(left, top, right, bottom)
    }

    override fun setCompoundDrawablesRelative(start: Drawable?, top: Drawable?, end: Drawable?, bottom: Drawable?) {
        setDrawableBounds(top, topHeight)
        setDrawableBounds(bottom, bottomHeight)
        setDrawableBounds(start, startHeight)
        setDrawableBounds(end, endHeight)
        super.setCompoundDrawablesRelative(start, top, end, bottom)
    }

    override fun setCompoundDrawablesRelativeWithIntrinsicBounds(start: Drawable?, top: Drawable?, end: Drawable?, bottom: Drawable?) {
        setCompoundDrawablesRelative(start, top, end, bottom)
    }


    /**
     * 统一设置该 TextView 所有 compound drawable 高度，单位**px**.
     */
    fun setDrawableHeightPx(pxHeight: Int) {
        topHeight = pxHeight
        bottomHeight = pxHeight
        startHeight = pxHeight
        endHeight = pxHeight
        leftHeight = pxHeight
        rightHeight = pxHeight
        invalidate()
    }


    /**
     * 设置 drawableStart 并将其他 drawableXX 置为 null.
     */
    fun setOnlyDrawableStart(drawable: Drawable?) {
        setCompoundDrawablesRelative(drawable, null, null, null)
    }
    /**
     * 设置 drawableStart 并将其他 drawableXX 置为 null.
     */
    fun setOnlyDrawableStart(@DrawableRes start: Int) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(start, 0, 0, 0)
    }

    /**
     * 判断是否有设置任意 drawable.
     * true-至少有一个 drawable false-一个都没有
     */
    fun hasAnyDrawable(): Boolean {
        for (drawable in compoundDrawables) {
            if (drawable != null) {
                return true
            }
        }
        for (drawable in compoundDrawablesRelative) {
            if (drawable != null) {
                return true
            }
        }
        return false
    }

    /**
     * 为指定 drawable 设置指定高度，宽度等比缩放 bounds.
     */
    private fun setDrawableBounds(drawable: Drawable?, height: Int) {
        if (drawable != null && height > 0) {
            drawable.setBounds(0, 0, (height * 1f * drawable.intrinsicWidth / drawable.intrinsicHeight).toInt(), height)
        }
    }
}