package com.topdon.lib.core.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt

/**
 * 颜色选择自定义 View.
 * 只支持在已经定死的 120 种颜色中拾取一种.
 */
class ColorSelectView : View {
    companion object {
        /**
         * 选中描边宽度，单位 dp.
         */
        private const val DEFAULT_STROKE_WIDTH = 3

        private val ROW_COLOR_1 = intArrayOf(0xFFFEFFFE.toInt(), 0xFFEBEBEB.toInt(), 0xFFD6D6D6.toInt(), 0xFFC2C2C2.toInt(), 0xFFADADAD.toInt(), 0xFF999999.toInt(), 0xFF858585.toInt(), 0xFF707070.toInt(), 0xFF5C5C5C.toInt(), 0xFF474747.toInt(), 0xFF333333.toInt(), 0xFF000000.toInt())
        private val ROW_COLOR_2 = intArrayOf(0xFF00374A.toInt(), 0xFF011D57.toInt(), 0xFF11053B.toInt(), 0xFF2E063D.toInt(), 0xFF3C071B.toInt(), 0xFF5C0701.toInt(), 0xFF5A1C00.toInt(), 0xFF583300.toInt(), 0xFF563D00.toInt(), 0xFF666100.toInt(), 0xFF4F5504.toInt(), 0xFF263E0F.toInt())
        private val ROW_COLOR_3 = intArrayOf(0xFF004D65.toInt(), 0xFF012F7B.toInt(), 0xFF1A0A52.toInt(), 0xFF450D59.toInt(), 0xFF551029.toInt(), 0xFF831100.toInt(), 0xFF7B2900.toInt(), 0xFF7A4A00.toInt(), 0xFF785800.toInt(), 0xFF8D8602.toInt(), 0xFF6F760A.toInt(), 0xFF38571A.toInt())
        private val ROW_COLOR_4 = intArrayOf(0xFF016E8F.toInt(), 0xFF0042A9.toInt(), 0xFF2C0977.toInt(), 0xFF61187C.toInt(), 0xFF791A3D.toInt(), 0xFFB51A00.toInt(), 0xFFAD3E00.toInt(), 0xFFA96800.toInt(), 0xFFA67B01.toInt(), 0xFFC4BC00.toInt(), 0xFF9BA50E.toInt(), 0xFF4E7A27.toInt())
        private val ROW_COLOR_5 = intArrayOf(0xFF008CB4.toInt(), 0xFF0056D6.toInt(), 0xFF371A94.toInt(), 0xFF7A219E.toInt(), 0xFF99244F.toInt(), 0xFFE22400.toInt(), 0xFFDA5100.toInt(), 0xFFD38301.toInt(), 0xFFD19D01.toInt(), 0xFFF5EC00.toInt(), 0xFFC3D117.toInt(), 0xFF669D34.toInt())
        private val ROW_COLOR_6 = intArrayOf(0xFF00A1D8.toInt(), 0xFF0061FD.toInt(), 0xFF4D22B2.toInt(), 0xFF982ABC.toInt(), 0xFFB92D5D.toInt(), 0xFFFF4015.toInt(), 0xFFFF6A00.toInt(), 0xFFFFAB01.toInt(), 0xFFFCC700.toInt(), 0xFFFEFB41.toInt(), 0xFFD9EC37.toInt(), 0xFF76BB40.toInt())
        private val ROW_COLOR_7 = intArrayOf(0xFF01C7FC.toInt(), 0xFF3A87FD.toInt(), 0xFF5E30EB.toInt(), 0xFFBE38F3.toInt(), 0xFFE63B7A.toInt(), 0xFFFE6250.toInt(), 0xFFFE8648.toInt(), 0xFFFEB43F.toInt(), 0xFFFECB3E.toInt(), 0xFFFFF76B.toInt(), 0xFFE4EF65.toInt(), 0xFF96D35F.toInt())
        private val ROW_COLOR_8 = intArrayOf(0xFF52D6FC.toInt(), 0xFF74A7FF.toInt(), 0xFF864FFD.toInt(), 0xFFD357FE.toInt(), 0xFFEE719E.toInt(), 0xFFFF8C82.toInt(), 0xFFFEA57D.toInt(), 0xFFFEC777.toInt(), 0xFFFED977.toInt(), 0xFFFFF994.toInt(), 0xFFEAF28F.toInt(), 0xFFB1DD8B.toInt())
        private val ROW_COLOR_9 = intArrayOf(0xFF93E3FC.toInt(), 0xFFA7C6FF.toInt(), 0xFFB18CFE.toInt(), 0xFFE292FE.toInt(), 0xFFF4A4C0.toInt(), 0xFFFFB5AF.toInt(), 0xFFFFC5AB.toInt(), 0xFFFED9A8.toInt(), 0xFFFDE4A8.toInt(), 0xFFFFFBB9.toInt(), 0xFFF1F7B7.toInt(), 0xFFCDE8B5.toInt())
        private val ROW_COLOR_10 = intArrayOf(0xFFCBF0FF.toInt(), 0xFFD2E2FE.toInt(), 0xFFD8C9FE.toInt(), 0xFFEFCAFE.toInt(), 0xFFF9D3E0.toInt(), 0xFFFFDAD8.toInt(), 0xFFFFE2D6.toInt(), 0xFFFEECD4.toInt(), 0xFFFEF1D5.toInt(), 0xFFFDFBDD.toInt(), 0xFFF6FADB.toInt(), 0xFFDEEED4.toInt())

        private val COLOR = arrayOf(ROW_COLOR_1, ROW_COLOR_2, ROW_COLOR_3, ROW_COLOR_4, ROW_COLOR_5, ROW_COLOR_6, ROW_COLOR_7, ROW_COLOR_8, ROW_COLOR_9, ROW_COLOR_10)


        private fun getRowFromColor(@ColorInt color: Int): Int = when (color) {
            0xFFFEFFFE.toInt(), 0xFFEBEBEB.toInt(), 0xFFD6D6D6.toInt(), 0xFFC2C2C2.toInt(), 0xFFADADAD.toInt(), 0xFF999999.toInt(), 0xFF858585.toInt(), 0xFF707070.toInt(), 0xFF5C5C5C.toInt(), 0xFF474747.toInt(), 0xFF333333.toInt(), 0xFF000000.toInt() -> 0
            0xFF00374A.toInt(), 0xFF011D57.toInt(), 0xFF11053B.toInt(), 0xFF2E063D.toInt(), 0xFF3C071B.toInt(), 0xFF5C0701.toInt(), 0xFF5A1C00.toInt(), 0xFF583300.toInt(), 0xFF563D00.toInt(), 0xFF666100.toInt(), 0xFF4F5504.toInt(), 0xFF263E0F.toInt() -> 1
            0xFF004D65.toInt(), 0xFF012F7B.toInt(), 0xFF1A0A52.toInt(), 0xFF450D59.toInt(), 0xFF551029.toInt(), 0xFF831100.toInt(), 0xFF7B2900.toInt(), 0xFF7A4A00.toInt(), 0xFF785800.toInt(), 0xFF8D8602.toInt(), 0xFF6F760A.toInt(), 0xFF38571A.toInt() -> 2
            0xFF016E8F.toInt(), 0xFF0042A9.toInt(), 0xFF2C0977.toInt(), 0xFF61187C.toInt(), 0xFF791A3D.toInt(), 0xFFB51A00.toInt(), 0xFFAD3E00.toInt(), 0xFFA96800.toInt(), 0xFFA67B01.toInt(), 0xFFC4BC00.toInt(), 0xFF9BA50E.toInt(), 0xFF4E7A27.toInt() -> 3
            0xFF008CB4.toInt(), 0xFF0056D6.toInt(), 0xFF371A94.toInt(), 0xFF7A219E.toInt(), 0xFF99244F.toInt(), 0xFFE22400.toInt(), 0xFFDA5100.toInt(), 0xFFD38301.toInt(), 0xFFD19D01.toInt(), 0xFFF5EC00.toInt(), 0xFFC3D117.toInt(), 0xFF669D34.toInt() -> 4
            0xFF00A1D8.toInt(), 0xFF0061FD.toInt(), 0xFF4D22B2.toInt(), 0xFF982ABC.toInt(), 0xFFB92D5D.toInt(), 0xFFFF4015.toInt(), 0xFFFF6A00.toInt(), 0xFFFFAB01.toInt(), 0xFFFCC700.toInt(), 0xFFFEFB41.toInt(), 0xFFD9EC37.toInt(), 0xFF76BB40.toInt() -> 5
            0xFF01C7FC.toInt(), 0xFF3A87FD.toInt(), 0xFF5E30EB.toInt(), 0xFFBE38F3.toInt(), 0xFFE63B7A.toInt(), 0xFFFE6250.toInt(), 0xFFFE8648.toInt(), 0xFFFEB43F.toInt(), 0xFFFECB3E.toInt(), 0xFFFFF76B.toInt(), 0xFFE4EF65.toInt(), 0xFF96D35F.toInt() -> 6
            0xFF52D6FC.toInt(), 0xFF74A7FF.toInt(), 0xFF864FFD.toInt(), 0xFFD357FE.toInt(), 0xFFEE719E.toInt(), 0xFFFF8C82.toInt(), 0xFFFEA57D.toInt(), 0xFFFEC777.toInt(), 0xFFFED977.toInt(), 0xFFFFF994.toInt(), 0xFFEAF28F.toInt(), 0xFFB1DD8B.toInt() -> 7
            0xFF93E3FC.toInt(), 0xFFA7C6FF.toInt(), 0xFFB18CFE.toInt(), 0xFFE292FE.toInt(), 0xFFF4A4C0.toInt(), 0xFFFFB5AF.toInt(), 0xFFFFC5AB.toInt(), 0xFFFED9A8.toInt(), 0xFFFDE4A8.toInt(), 0xFFFFFBB9.toInt(), 0xFFF1F7B7.toInt(), 0xFFCDE8B5.toInt() -> 8
            0xFFCBF0FF.toInt(), 0xFFD2E2FE.toInt(), 0xFFD8C9FE.toInt(), 0xFFEFCAFE.toInt(), 0xFFF9D3E0.toInt(), 0xFFFFDAD8.toInt(), 0xFFFFE2D6.toInt(), 0xFFFEECD4.toInt(), 0xFFFEF1D5.toInt(), 0xFFFDFBDD.toInt(), 0xFFF6FADB.toInt(), 0xFFDEEED4.toInt() -> 9
            else -> -1
        }

        private fun getColumnFromColor(@ColorInt color: Int): Int = when (color) {
            0xFFFEFFFE.toInt(), 0xFF00374A.toInt(), 0xFF004D65.toInt(), 0xFF016E8F.toInt(), 0xFF008CB4.toInt(), 0xFF00A1D8.toInt(), 0xFF01C7FC.toInt(), 0xFF52D6FC.toInt(), 0xFF93E3FC.toInt(), 0xFFCBF0FF.toInt() -> 0
            0xFFEBEBEB.toInt(), 0xFF011D57.toInt(), 0xFF012F7B.toInt(), 0xFF0042A9.toInt(), 0xFF0056D6.toInt(), 0xFF0061FD.toInt(), 0xFF3A87FD.toInt(), 0xFF74A7FF.toInt(), 0xFFA7C6FF.toInt(), 0xFFD2E2FE.toInt() -> 1
            0xFFD6D6D6.toInt(), 0xFF11053B.toInt(), 0xFF1A0A52.toInt(), 0xFF2C0977.toInt(), 0xFF371A94.toInt(), 0xFF4D22B2.toInt(), 0xFF5E30EB.toInt(), 0xFF864FFD.toInt(), 0xFFB18CFE.toInt(), 0xFFD8C9FE.toInt() -> 2
            0xFFC2C2C2.toInt(), 0xFF2E063D.toInt(), 0xFF450D59.toInt(), 0xFF61187C.toInt(), 0xFF7A219E.toInt(), 0xFF982ABC.toInt(), 0xFFBE38F3.toInt(), 0xFFD357FE.toInt(), 0xFFE292FE.toInt(), 0xFFEFCAFE.toInt() -> 3
            0xFFADADAD.toInt(), 0xFF3C071B.toInt(), 0xFF551029.toInt(), 0xFF791A3D.toInt(), 0xFF99244F.toInt(), 0xFFB92D5D.toInt(), 0xFFE63B7A.toInt(), 0xFFEE719E.toInt(), 0xFFF4A4C0.toInt(), 0xFFF9D3E0.toInt() -> 4
            0xFF999999.toInt(), 0xFF5C0701.toInt(), 0xFF831100.toInt(), 0xFFB51A00.toInt(), 0xFFE22400.toInt(), 0xFFFF4015.toInt(), 0xFFFE6250.toInt(), 0xFFFF8C82.toInt(), 0xFFFFB5AF.toInt(), 0xFFFFDAD8.toInt() -> 5
            0xFF858585.toInt(), 0xFF5A1C00.toInt(), 0xFF7B2900.toInt(), 0xFFAD3E00.toInt(), 0xFFDA5100.toInt(), 0xFFFF6A00.toInt(), 0xFFFE8648.toInt(), 0xFFFEA57D.toInt(), 0xFFFFC5AB.toInt(), 0xFFFFE2D6.toInt() -> 6
            0xFF707070.toInt(), 0xFF583300.toInt(), 0xFF7A4A00.toInt(), 0xFFA96800.toInt(), 0xFFD38301.toInt(), 0xFFFFAB01.toInt(), 0xFFFEB43F.toInt(), 0xFFFEC777.toInt(), 0xFFFED9A8.toInt(), 0xFFFEECD4.toInt() -> 7
            0xFF5C5C5C.toInt(), 0xFF563D00.toInt(), 0xFF785800.toInt(), 0xFFA67B01.toInt(), 0xFFD19D01.toInt(), 0xFFFCC700.toInt(), 0xFFFECB3E.toInt(), 0xFFFED977.toInt(), 0xFFFDE4A8.toInt(), 0xFFFEF1D5.toInt() -> 8
            0xFF474747.toInt(), 0xFF666100.toInt(), 0xFF8D8602.toInt(), 0xFFC4BC00.toInt(), 0xFFF5EC00.toInt(), 0xFFFEFB41.toInt(), 0xFFFFF76B.toInt(), 0xFFFFF994.toInt(), 0xFFFFFBB9.toInt(), 0xFFFDFBDD.toInt() -> 9
            0xFF333333.toInt(), 0xFF4F5504.toInt(), 0xFF6F760A.toInt(), 0xFF9BA50E.toInt(), 0xFFC3D117.toInt(), 0xFFD9EC37.toInt(), 0xFFE4EF65.toInt(), 0xFFEAF28F.toInt(), 0xFFF1F7B7.toInt(), 0xFFF6FADB.toInt() -> 10
            0xFF000000.toInt(), 0xFF263E0F.toInt(), 0xFF38571A.toInt(), 0xFF4E7A27.toInt(), 0xFF669D34.toInt(), 0xFF76BB40.toInt(), 0xFF96D35F.toInt(), 0xFFB1DD8B.toInt(), 0xFFCDE8B5.toInt(), 0xFFDEEED4.toInt() -> 11
            else -> -1
        }
    }

    /**
     * 是否需要整体描边.
     */
    var isNeedStroke: Boolean = false
        set(value) {
            invalidate()
            field = value
        }

    /**
     * 颜色拾取事件监听.
     */
    var onSelectListener: ((color: Int) -> Unit)? = null

    fun reset() {
        currentRow = -1
        currentColumn = -1
        invalidate()
    }

    fun selectColor(@ColorInt color: Int) {
        currentRow = getRowFromColor(color)
        currentColumn = getColumnFromColor(color)
        invalidate()
    }


    private var currentRow: Int = -1   //当前选中行
    private var currentColumn: Int = -1//当前选中列

    private val widthPixels: Int//屏幕宽度
    private val density: Float  //屏幕缩放等级，用于dp与px转换
    private val strokeWidth: Int//描边宽度，单位px

    private val path = Path()
    private val itemPaint = Paint()  //未选中画笔
    private val itemSelectPaint = Paint()//选中描边效果画笔
    private val strokePaint = Paint()    //整体描边效果画笔

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        widthPixels = context.resources.displayMetrics.widthPixels
        density = context.resources.displayMetrics.density
        strokeWidth = dp2px(DEFAULT_STROKE_WIDTH.toFloat())

        itemPaint.isAntiAlias = true
        itemPaint.style = Paint.Style.FILL

        itemSelectPaint.isAntiAlias = true
        itemSelectPaint.style = Paint.Style.STROKE
        itemSelectPaint.color = 0xffffffff.toInt()
        itemSelectPaint.strokeWidth = strokeWidth.toFloat()

        strokePaint.isAntiAlias = true
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = 0xff999999.toInt()
        strokePaint.strokeWidth = strokeWidth / 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val itemSize = ((if (widthMode == MeasureSpec.UNSPECIFIED) widthPixels else widthSize - strokeWidth) / 12f).toInt()
        val width = itemSize * 12 + strokeWidth

        val wantHeight = itemSize * 10 + strokeWidth
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> wantHeight.coerceAtMost(heightSize)
            MeasureSpec.UNSPECIFIED -> wantHeight
            else -> wantHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val itemSize = (measuredWidth - strokeWidth) / 12f
        val connerSize = itemSize * 8f / 26f
        val margin = strokeWidth / 2f

        //绘制描边
        if (isNeedStroke) {
            path.rewind()
            path.moveTo(margin, margin + connerSize)
            path.quadTo(margin, margin, margin + connerSize, margin)
            path.lineTo(margin + itemSize * 12 - connerSize, margin)
            path.quadTo(margin + itemSize * 12, margin, margin + itemSize * 12, margin + connerSize)
            path.lineTo(margin + itemSize * 12, margin + itemSize * 10 - connerSize)
            path.quadTo(margin + itemSize * 12, margin + itemSize * 10, margin + itemSize * 12 - connerSize, margin + itemSize * 10)
            path.lineTo(margin + connerSize, margin + itemSize * 10)
            path.quadTo(margin, margin + itemSize * 10, margin, margin + itemSize * 10 - connerSize)
            path.close()
            canvas.drawPath(path, strokePaint)
        }
        
        for (row in 0 until 10) {
            for (column in 0 until 12) {
                itemPaint.color = COLOR[row][column]
                if (row == 0 && column == 0) {
                    path.rewind()
                    path.moveTo(margin, margin + connerSize)
                    path.quadTo(margin, margin, margin + connerSize, margin)
                    path.lineTo(margin + itemSize, margin)
                    path.lineTo(margin + itemSize, margin + itemSize)
                    path.lineTo(margin, margin + itemSize)
                    path.close()
                    canvas.drawPath(path, itemPaint)
                } else if (row == 0 && column == 11) {
                    path.rewind()
                    path.moveTo(margin + itemSize * 12 - connerSize, margin)
                    path.quadTo(margin + itemSize * 12, margin, margin + itemSize * 12, margin + connerSize)
                    path.lineTo(margin + itemSize * 12, margin + itemSize)
                    path.lineTo(margin + itemSize * 11, margin + itemSize)
                    path.lineTo(margin + itemSize * 11, margin)
                    path.close()
                    canvas.drawPath(path, itemPaint)
                } else if (row == 9 && column == 0) {
                    path.rewind()
                    path.moveTo(margin + connerSize, margin + itemSize * 10)
                    path.quadTo(margin, margin + itemSize * 10, margin, margin + itemSize * 10 - connerSize)
                    path.lineTo(margin, margin + itemSize * 9)
                    path.lineTo(margin + itemSize, margin + itemSize * 9)
                    path.lineTo(margin + itemSize, margin + itemSize * 10)
                    path.close()
                    canvas.drawPath(path, itemPaint)
                } else if (row == 9 && column == 11) {
                    path.rewind()
                    path.moveTo(margin + itemSize * 12, margin + itemSize * 10 - connerSize)
                    path.quadTo(margin + itemSize * 12, margin + itemSize * 10, margin + itemSize * 12 - connerSize, margin + itemSize * 10)
                    path.lineTo(margin + itemSize * 11, margin + itemSize * 10)
                    path.lineTo(margin + itemSize * 11, margin + itemSize * 9)
                    path.lineTo(margin + itemSize * 12, margin + itemSize * 9)
                    path.close()
                    canvas.drawPath(path, itemPaint)
                } else {
                    val left = margin + itemSize * column
                    val top = margin + itemSize * row
                    canvas.drawRect(left, top, left + itemSize, top + itemSize, itemPaint)
                }
            }
        }

        if (currentRow >= 0 && currentColumn >= 0) {
            val left = margin + itemSize * currentColumn
            val top = margin + itemSize * currentRow
            val right = left + itemSize
            val bottom = top + itemSize
            path.rewind()
            if (currentRow == 0 && currentColumn == 0) {
                path.moveTo(left, top + connerSize)
                path.quadTo(left, top, left + connerSize, top)
            } else {
                path.moveTo(left, top + strokeWidth)
                path.quadTo(left, top, left + strokeWidth, top)
            }
            if (currentRow == 0 && currentColumn == 11) {
                path.lineTo(right - connerSize, top)
                path.quadTo(right, top, right, top + connerSize)
            } else {
                path.lineTo(right - strokeWidth, top)
                path.quadTo(right, top, right, top + strokeWidth)
            }
            if (currentRow == 9 && currentColumn == 11) {
                path.lineTo(right, bottom - connerSize)
                path.quadTo(right, bottom, right - connerSize, bottom)
            } else {
                path.lineTo(right, bottom - strokeWidth)
                path.quadTo(right, bottom, right - strokeWidth, bottom)
            }
            if (currentRow == 9 && currentColumn == 0) {
                path.lineTo(left + connerSize, bottom)
                path.quadTo(left, bottom, left, bottom - connerSize)
            } else {
                path.lineTo(left + strokeWidth, bottom)
                path.quadTo(left, bottom, left, bottom - strokeWidth)
            }
            path.close()
            canvas?.drawPath(path, itemSelectPaint)
        }
    }

    private var downRow = 0
    private var downColumn = 0

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        val x = event.x.toInt() - strokeWidth / 2
        val y = event.y.toInt() - strokeWidth / 2

        val itemSize = (measuredWidth - strokeWidth) / 12
        val column = (x / itemSize + (if (x % itemSize > 0) 1 else 0) - 1).coerceAtMost(11).coerceAtLeast(0)
        val row = (y / itemSize + (if (y % itemSize > 0) 1 else 0) - 1).coerceAtMost(9).coerceAtLeast(0)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downRow = row
                downColumn = column
            }
            MotionEvent.ACTION_UP -> {
                if (row == downRow && column == downColumn) {
                    currentRow = row
                    currentColumn = column
                    invalidate()

                    onSelectListener?.invoke(COLOR[row][column])
                }
            }
        }
        return true
    }




    private fun dp2px(dpValue: Float): Int {
        return (dpValue * density + 0.5f).toInt()
    }
}