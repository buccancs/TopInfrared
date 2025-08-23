package com.topdon.lib.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class ImageEditView : View {
    companion object {
        
        private const val PAINT_WIDTH = 6
        
        private const val HALF_PAINT_WIDTH = 3
        
        private const val ARROW_WIDTH = 30
        
        private const val PAINT_COLOR = 0xffe22400.toInt()
    }

    enum class Type {
        
        CIRCLE,

        RECT,

        ARROW,
    }

    var type: Type = Type.CIRCLE

    var color: Int
        get() = paint.color
        set(value) {
            paint.color = value
            invalidate()
        }

    var sourceBitmap: Bitmap? = null
        set(value) {
            if (value == null) {
                return
            }
            if (width == 0 || height == 0) {
                bgBitmap = null
            } else {
                bgBitmap = Bitmap.createScaledBitmap(value, width, height, true)
                invalidate()
            }
            field = value
        }

    private var hasEditData = false
    
    private var bgBitmap: Bitmap? = null
    
    private var editBitmap: Bitmap? = null

    private var canvas: Canvas? = null

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    private val path = Path()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        paint.color = PAINT_COLOR
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = PAINT_WIDTH.toFloat()
        paint.isDither = true
    }

    fun clear() {
        hasEditData = false
        canvas?.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    fun buildResultBitmap(): Bitmap? {
        val bgBitmap = this.bgBitmap ?: return null
        val editBitmap = this.editBitmap
        if (hasEditData && editBitmap != null) {
            val canvas = Canvas(bgBitmap)
            canvas.drawBitmap(editBitmap, 0f, 0f, null)
        }
        return bgBitmap
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val oldBitmap = editBitmap
        if (oldBitmap == null || oldBitmap.width != measuredWidth || oldBitmap.height != measuredHeight) {
            val newBitmap = if (oldBitmap == null) {
                Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createScaledBitmap(oldBitmap, measuredWidth, measuredHeight, true)
            }
            canvas = Canvas(newBitmap)
            editBitmap = newBitmap
        }
        sourceBitmap?.let {
            if (bgBitmap == null) {
                bgBitmap = Bitmap.createScaledBitmap(it, measuredWidth, measuredHeight, true)
            } else {
                if (bgBitmap?.width != measuredWidth || bgBitmap?.height != measuredHeight) {
                    bgBitmap = Bitmap.createScaledBitmap(it, measuredWidth, measuredHeight, true)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        bgBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        editBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        drawEdit(canvas)
    }

    private fun drawEdit(canvas: Canvas?) {
        if (downX == 0 && downY == 0 && currentX == 0 && currentY == 0) {
            return
        }
        when (type) {
            Type.CIRCLE -> {
                paint.style = Paint.Style.STROKE
                val left = downX.coerceAtMost(currentX).toFloat()
                val top = downY.coerceAtMost(currentY).toFloat()
                val right = downX.coerceAtLeast(currentX).toFloat()
                val bottom = downY.coerceAtLeast(currentY).toFloat()
                canvas?.drawOval(left, top, right, bottom, paint)
            }
            Type.RECT -> {
                paint.style = Paint.Style.STROKE
                val left = downX.coerceAtMost(currentX).toFloat()
                val top = downY.coerceAtMost(currentY).toFloat()
                val right = downX.coerceAtLeast(currentX).toFloat()
                val bottom = downY.coerceAtLeast(currentY).toFloat()
                canvas?.drawRect(left, top, right, bottom, paint)
            }
            Type.ARROW -> {
                if (abs(downX - currentX) < ARROW_WIDTH && abs(downY - currentY) < ARROW_WIDTH) {
                    return
                }

                paint.style = Paint.Style.FILL
                path.reset()

                if (downX == currentX) {
                    val endY = if (downY > currentY) currentY + PAINT_WIDTH else (currentY - PAINT_WIDTH)
                    canvas?.drawLine(downX.toFloat(), downY.toFloat(), currentX.toFloat(), endY.toFloat(), paint)

                    val triangleH: Float = (ARROW_WIDTH / 2) * sqrt(3f)
                    val y: Float = if (downY > currentY) currentY + triangleH else (currentY - triangleH)

                    val x1: Float = downX - (ARROW_WIDTH / 2f)
                    val x2: Float = downX + (ARROW_WIDTH / 2f)

                    path.moveTo(currentX.toFloat(), currentY.toFloat())
                    path.lineTo(x1, y)
                    path.lineTo(x2, y)
                    path.close()
                    canvas?.drawPath(path, paint)
                } else if (downY == currentY) {
                    val endX = if (downX > currentX) currentX + PAINT_WIDTH else (currentX - PAINT_WIDTH)
                    canvas?.drawLine(downX.toFloat(), downY.toFloat(), endX.toFloat(), currentY.toFloat(), paint)

                    val triangleH: Float = (ARROW_WIDTH / 2) * sqrt(3f)
                    val x: Float = if (downX > currentX) currentX + triangleH else (currentX - triangleH)

                    val y1: Float = downY - (ARROW_WIDTH / 2f)
                    val y2: Float = downY + (ARROW_WIDTH / 2f)

                    path.moveTo(currentX.toFloat(), currentY.toFloat())
                    path.lineTo(x, y1)
                    path.lineTo(x, y2)
                    path.close()
                    canvas?.drawPath(path, paint)
                } else {
                    val k1: Float = (downY - currentY).toFloat() / (downX - currentX).toFloat()
                    val b1: Float = downY - k1 * downX
                    val a1: Float = -b1 / k1

                    val backWidth = PAINT_WIDTH
                    val endY: Float = if (k1 > 0) {
                        val hypotenuse: Float = sqrt((currentX - a1).pow(2) + currentY.toFloat().pow(2))
                        if (currentX > downX) {
                            currentY * (hypotenuse - backWidth) / hypotenuse
                        } else {
                            currentY * (hypotenuse + backWidth) / hypotenuse
                        }
                    } else {
                        val hypotenuse: Float = sqrt((a1 - currentX).pow(2) + currentY.toFloat().pow(2))
                        if (currentX > downX) {
                            currentY * (hypotenuse + backWidth) / hypotenuse
                        } else {
                            currentY * (hypotenuse - backWidth) / hypotenuse
                        }
                    }
                    val endX = (endY - b1) / k1
                    canvas?.drawLine(downX.toFloat(), downY.toFloat(), endX, endY, paint)

                    val triangleH: Float = (ARROW_WIDTH / 2) * sqrt(3f)
                    val y: Float = if (k1 > 0) {
                        val hypotenuse: Float = sqrt((currentX - a1).pow(2) + currentY.toFloat().pow(2))
                        if (currentX > downX) {
                            currentY * (hypotenuse - triangleH) / hypotenuse
                        } else {
                            currentY * (hypotenuse + triangleH) / hypotenuse
                        }
                    } else {
                        val hypotenuse: Float = sqrt((a1 - currentX).pow(2) + currentY.toFloat().pow(2))
                        if (currentX > downX) {
                            currentY * (hypotenuse + triangleH) / hypotenuse
                        } else {
                            currentY * (hypotenuse - triangleH) / hypotenuse
                        }
                    }
                    val x = (y - b1) / k1

                    val k2: Float = -1 / k1
                    val b2: Float = y - k2 * x
                    val a2: Float = -b2 / k2

                    val hypotenuse2: Float = sqrt((if (k2 > 0) x - a2 else (a2 - x)).pow(2) + y.pow(2))
                    val yLeft = y * (hypotenuse2 - ARROW_WIDTH / 2) / hypotenuse2
                    val yRight = y * (hypotenuse2 + ARROW_WIDTH / 2) / hypotenuse2
                    val xLeft = (yLeft - b2) / k2
                    val xRight = (yRight - b2) / k2

                    path.moveTo(currentX.toFloat(), currentY.toFloat())
                    path.lineTo(xLeft, yLeft)
                    path.lineTo(xRight, yRight)
                    path.close()
                    canvas?.drawPath(path, paint)
                }
            }
        }
    }

    private var downX = 0
    private var downY = 0
    private var currentX = 0
    private var currentY = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null || !isEnabled) {
            return false
        }
        currentX = event.x.toInt().coerceAtLeast(HALF_PAINT_WIDTH).coerceAtMost(width - HALF_PAINT_WIDTH)
        currentY = event.y.toInt().coerceAtLeast(HALF_PAINT_WIDTH).coerceAtMost(height - HALF_PAINT_WIDTH)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x.toInt().coerceAtLeast(HALF_PAINT_WIDTH).coerceAtMost(width - HALF_PAINT_WIDTH)
                downY = event.y.toInt().coerceAtLeast(HALF_PAINT_WIDTH).coerceAtMost(height - HALF_PAINT_WIDTH)
            }
            MotionEvent.ACTION_MOVE -> {
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                drawEdit(canvas)
                downX = 0
                downY = 0
                currentX = 0
                currentY = 0
                hasEditData = true
                invalidate()
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        canvas = null
        sourceBitmap = null
        bgBitmap = null
        editBitmap = null
    }
}