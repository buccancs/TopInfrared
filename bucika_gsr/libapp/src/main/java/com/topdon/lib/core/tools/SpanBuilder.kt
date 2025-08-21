package com.topdon.lib.core.tools

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ReplacementSpan
import android.view.View
import android.view.View.OnClickListener
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import java.lang.ref.WeakReference

/**
 *
 * Created by LCG on 2024/2/19.
 */
class SpanBuilder : SpannableStringBuilder {

    constructor() : super()

    constructor(text: CharSequence) : super(text)

    constructor(text: CharSequence, start: Int, end: Int) : super(text, start, end)

    fun appendDrawable(context: Context, @DrawableRes resourceId: Int, @Px wantHeight: Int): SpanBuilder {
        this.append(" ")
        val oldLength = this.length
        this.append("a")
        this.setSpan(MyImageSpan(context, resourceId, wantHeight), oldLength, this.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        this.append(" ")
        return this
    }

    fun appendColor(text: CharSequence, @ColorInt color: Int): SpanBuilder {
        if (text.isEmpty()) {//搞个空字符串过来干嘛
            return this
        }
        val oldLength = this.length
        this.append(text)
        this.setSpan(ForegroundColorSpan(color), oldLength, this.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        return this
    }

    fun appendColorAndClick(text: CharSequence, @ColorInt color: Int, listener: OnClickListener): SpanBuilder {
        if (text.isEmpty()) {//搞个空字符串过来干嘛
            return this
        }
        val oldLength = this.length
        this.append(text)
        this.setSpan(MyClickSpan(listener, color, false), oldLength, this.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        return this
    }

    fun appendColorAndClick(context: Context, @StringRes resId: Int, formatArg: String, @ColorInt color: Int, hasUnderLine: Boolean = false, listener: OnClickListener): SpanBuilder {
        append(context.getString(resId, formatArg))
        val startIndex: Int = lastIndexOf(formatArg)
        val endIndex: Int = startIndex + formatArg.length
        this.setSpan(MyClickSpan(listener, color, hasUnderLine), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        return this
    }

    private class MyClickSpan(val listener: OnClickListener, val color: Int, val hasUnderLine: Boolean) : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            ds.color = color
            ds.isUnderlineText = hasUnderLine
        }

        override fun onClick(widget: View) {
            listener.onClick(widget)
        }
    }

    private class MyImageSpan(val context: Context, @DrawableRes val resourceId: Int, @Px val wantHeight: Int) : ReplacementSpan() {

        private var weakReference: WeakReference<Drawable>? = null

        fun getCachedDrawable(): Drawable {
            val weakDrawable = weakReference?.get()
            if (weakDrawable != null) {
                return weakDrawable
            }

            val drawable: Drawable = ContextCompat.getDrawable(context, resourceId)!!
            drawable.setBounds(0, 0, (drawable.intrinsicWidth * wantHeight * 1f / drawable.intrinsicHeight).toInt(), wantHeight)
            weakReference = WeakReference(drawable)

            return drawable
        }

        override fun getSize(paint: Paint, text: CharSequence?, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
            val rect = getCachedDrawable().bounds
            if (fm != null) {
                fm.ascent = -rect.bottom
                fm.descent = 0
                fm.top = fm.ascent
                fm.bottom = fm.descent
            }
            return rect.right
        }

        override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
            val drawable: Drawable = getCachedDrawable()
            val transY = top + (bottom - top) / 2f - drawable.getBounds().height() / 2f
            canvas.save()
            canvas.translate(x, transY)
            drawable.draw(canvas)
            canvas.restore()
        }
    }
}