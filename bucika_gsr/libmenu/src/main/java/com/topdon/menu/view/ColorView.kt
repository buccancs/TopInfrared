package com.topdon.menu.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.topdon.menu.R

class ColorView : View {
    
    var colors: IntArray = intArrayOf(0xfffbda00.toInt(), 0xffea0e0e.toInt(), 0xff6907af.toInt())
    
    var positions: FloatArray = floatArrayOf(0f, 0.5f, 1f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var shaderSelectYes = LinearGradient(0f, 0f, 0f, 0f, colors, positions, Shader.TileMode.CLAMP)
    
    private var shaderSelectNot = LinearGradient(0f, 0f, 0f, 0f, colors, positions, Shader.TileMode.CLAMP)
    
    private val triangleDrawable: Drawable

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        paint.color = 0xffffffff.toInt()
        triangleDrawable = ContextCompat.getDrawable(context, R.drawable.svg_color_select)!!
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int = if (widthMode == MeasureSpec.UNSPECIFIED) 100 else widthSize
        val barHeight: Int = (width * 73f / 62).toInt()
        val triangleSize: Int = (width * 12f / 62).toInt()
        val margin: Int = SizeUtils.dp2px(4f)
        val wantHeight: Int = barHeight + margin + triangleSize
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> wantHeight.coerceAtMost(heightSize)
            else -> wantHeight
        }
        setMeasuredDimension(width, height)

        refreshShader()
        triangleDrawable.setBounds((width - triangleSize) / 2, barHeight + margin, (width - triangleSize) / 2 + triangleSize, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius: Float = SizeUtils.dp2px(10f).toFloat()
        val barHeight: Int = (width * 73f / 62).toInt()

        if (isSelected) {
            val strokeSize: Float = SizeUtils.dp2px(2f).toFloat()
            val selectBarHeight: Int = (barHeight - strokeSize * 2).toInt()
            paint.shader = null
            canvas.drawRoundRect(0f, 0f, width.toFloat(), barHeight.toFloat(), radius, radius, paint)
            paint.shader = shaderSelectYes
            canvas.drawRoundRect(strokeSize, strokeSize, width - strokeSize, strokeSize + selectBarHeight, radius, radius, paint)
            triangleDrawable.draw(canvas)
        } else {
            val normalBarWidth: Int = (width * 50f / 62).toInt()
            val normalBarHeight: Int = (normalBarWidth * 60f / 50).toInt()
            val top: Float = ((barHeight - normalBarHeight) / 2).toFloat()
            val left: Float = ((width - normalBarWidth) / 2).toFloat()
            paint.shader = shaderSelectNot
            canvas.drawRoundRect(left, top, width - left, top + normalBarHeight, radius, radius, paint)
        }
    }

    fun refreshColor(colors: IntArray, positions: FloatArray) {
        this.colors = colors
        this.positions = positions
        refreshShader()
        invalidate()
    }

    private fun refreshShader() {
        val strokeSize: Float = SizeUtils.dp2px(2f).toFloat()
        val barHeight: Int = (measuredWidth * 73f / 62).toInt()
        val selectBarHeight: Int = (barHeight - strokeSize * 2).toInt()
        shaderSelectYes = LinearGradient(0f, strokeSize, 0f, strokeSize + selectBarHeight, colors, positions, Shader.TileMode.CLAMP)

        val normalBarWidth: Int = (measuredWidth * 50f / 62).toInt()
        val normalBarHeight: Int = (normalBarWidth * 60f / 50).toInt()
        val top: Float = ((barHeight - normalBarHeight) / 2).toFloat()
        shaderSelectNot = LinearGradient(0f, top, 0f, top + normalBarHeight, colors, positions, Shader.TileMode.CLAMP)
    }
}