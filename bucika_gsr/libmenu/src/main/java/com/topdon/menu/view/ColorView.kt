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

/**
 * 测温模式-菜单3-伪彩/观测模式-菜单4-伪彩 其中一个伪彩块.
 *
 * 这个 View 仅在菜单-伪彩中使用，太过定制化不能通用，故而里面很多尺寸、比例直接写死。
 *
 * 仅提供一个方法 [refreshColor] 用于刷新 UI.
 *
 * Created by LCG on 2024/11/12.
 */
class ColorView : View {
    /**
     * 伪彩渐变颜色值数组.
     */
    var colors: IntArray = intArrayOf(0xfffbda00.toInt(), 0xffea0e0e.toInt(), 0xff6907af.toInt())
    /**
     * 伪彩渐变颜色对应的位置数组.
     */
    var positions: FloatArray = floatArrayOf(0f, 0.5f, 1f)



    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    /**
     * 已选中时 paint 的 shader.
     */
    private var shaderSelectYes = LinearGradient(0f, 0f, 0f, 0f, colors, positions, Shader.TileMode.CLAMP)
    /**
     * 未选中时 paint 的 shader.
     */
    private var shaderSelectNot = LinearGradient(0f, 0f, 0f, 0f, colors, positions, Shader.TileMode.CLAMP)
    /**
     * 选中时的底部三角形
     */
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
        val barHeight: Int = (width * 73f / 62).toInt()    //62和73是根据UI图 选中时含描边在内色块宽高比 62:73
        val triangleSize: Int = (width * 12f / 62).toInt() //62和12是根据UI图 三角形宽度12，总宽度62
        val margin: Int = SizeUtils.dp2px(4f)      //色块和三角形中间有 4dp 间距
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
        val barHeight: Int = (width * 73f / 62).toInt() //62和73是根据UI图 选中时含描边在内色块宽高比 62:73

        if (isSelected) {
            val strokeSize: Float = SizeUtils.dp2px(2f).toFloat() //描边宽度2dp
            val selectBarHeight: Int = (barHeight - strokeSize * 2).toInt()
            paint.shader = null
            canvas.drawRoundRect(0f, 0f, width.toFloat(), barHeight.toFloat(), radius, radius, paint)
            paint.shader = shaderSelectYes
            canvas.drawRoundRect(strokeSize, strokeSize, width - strokeSize, strokeSize + selectBarHeight, radius, radius, paint)
            triangleDrawable.draw(canvas)
        } else {
            val normalBarWidth: Int = (width * 50f / 62).toInt() //未选中时宽度50，整体宽度62
            val normalBarHeight: Int = (normalBarWidth * 60f / 50).toInt() //宽高比为 50:60
            val top: Float = ((barHeight - normalBarHeight) / 2).toFloat()
            val left: Float = ((width - normalBarWidth) / 2).toFloat()
            paint.shader = shaderSelectNot
            canvas.drawRoundRect(left, top, width - left, top + normalBarHeight, radius, radius, paint)
        }
    }

    /**
     * 使用指定的颜色及位置重新绘制.
     */
    fun refreshColor(colors: IntArray, positions: FloatArray) {
        this.colors = colors
        this.positions = positions
        refreshShader()
        invalidate()
    }

    private fun refreshShader() {
        val strokeSize: Float = SizeUtils.dp2px(2f).toFloat() //描边宽度2dp
        val barHeight: Int = (measuredWidth * 73f / 62).toInt() //62和73是根据UI图 选中时含描边在内色块宽高比 62:73
        val selectBarHeight: Int = (barHeight - strokeSize * 2).toInt()
        shaderSelectYes = LinearGradient(0f, strokeSize, 0f, strokeSize + selectBarHeight, colors, positions, Shader.TileMode.CLAMP)

        val normalBarWidth: Int = (measuredWidth * 50f / 62).toInt() //未选中时宽度50，整体宽度62
        val normalBarHeight: Int = (normalBarWidth * 60f / 50).toInt() //宽高比为 50:60
        val top: Float = ((barHeight - normalBarHeight) / 2).toFloat()
        shaderSelectNot = LinearGradient(0f, top, 0f, top + normalBarHeight, colors, positions, Shader.TileMode.CLAMP)
    }
}