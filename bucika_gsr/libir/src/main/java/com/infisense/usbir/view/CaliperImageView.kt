package com.infisense.usbir.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import com.infisense.usbir.R

/**
 * 卡尺图片
 * @author: CaiSongL
 * @date: 2023/10/25 13:31
 */
class CaliperImageView : AppCompatImageView {

    private var showBitmapWidth: Float = 0f
    private var showBitmapHeight: Float = 0F
    private var yscale: Float = 1f
    private var xscale: Float = 1f
    private var parentViewHeight: Float = 0f
    private var parentViewWidth: Float = 0f
    private var imageHeight: Int = 0
    private var imageWidth: Int = 0
    private var originalBitmapHeight: Float = 0f
    private var originalBitmapWidth: Float = 0f
    private var originalBitmap: Bitmap? = null
    private val pxBitmapHeight = 150f
    private var l: Int = 0
    private var r: Int = 0
    private var t: Int = 0
    private var b: Int = 0

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }



    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
    private fun initView() {
        originalBitmap = (resources.getDrawable(R.drawable.svg_ic_target_horizontal_person_green) as BitmapDrawable).bitmap
        originalBitmapWidth = originalBitmap?.width?.toFloat() ?: 0f
        originalBitmapHeight = originalBitmap?.height?.toFloat() ?: 0f
        visibility = View.GONE
    }
    fun setImageSize(imageWidth: Int, imageHeight: Int, parentViewWidth: Int, parentViewHeight: Int) {
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        if (parentViewWidth > 0){
            this.parentViewWidth = parentViewWidth.toFloat()
        }else{
            this.parentViewWidth = (parent as ViewGroup).measuredWidth.toFloat()
        }
        if (parentViewHeight > 0){
            this.parentViewHeight = parentViewHeight.toFloat()
        }else{
            this.parentViewHeight = (parent as ViewGroup).measuredHeight.toFloat()
        }
        if (parentViewWidth > 0) {
            xscale = parentViewWidth.toFloat() / imageWidth.toFloat()
        }
        if (parentViewHeight > 0) {
            yscale = parentViewHeight.toFloat() / imageHeight.toFloat()
        }
        showBitmapHeight = pxBitmapHeight * yscale
        showBitmapWidth = pxBitmapHeight * originalBitmapWidth / originalBitmapHeight * xscale
        visibility = View.VISIBLE
        val layoutParams =  this.layoutParams
        layoutParams.width  = showBitmapWidth.toInt()
        layoutParams.height = showBitmapHeight.toInt()
        this.layoutParams = layoutParams
        if (l== 0 && t == 0 && r == 0 && b == 0){
            l = (parentViewWidth/2 - showBitmapWidth / 2).toInt()
            r = (parentViewWidth/2 + showBitmapWidth / 2).toInt()
            t = (parentViewHeight/2 - showBitmapHeight / 2).toInt()
            b = (parentViewHeight/2 + showBitmapHeight / 2).toInt()
        }
        layout(l, t, r, b)
        requestLayout()
    }

    override fun layout(l: Int, t: Int, r: Int, b: Int) {
        super.layout(l, t, r, b)
    }

    private var downX = 0f
    private var downY = 0f

    private val downTime: Long = 0

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if (this.isEnabled) {
            when (event.getAction()) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.getX()
                    downY = event.getY()
                }
                MotionEvent.ACTION_MOVE -> {
                    val xDistance: Float = event.getX() - downX
                    val yDistance: Float = event.getY() - downY
                    if (xDistance != 0f && yDistance != 0f) {
                        l = (left + xDistance).toInt()
                        r = (right + xDistance).toInt()
                        t = (top + yDistance).toInt()
                        b = (bottom + yDistance).toInt()
                        layout(l, t, r, b)
                    }
                }
                MotionEvent.ACTION_UP -> isPressed = false
                MotionEvent.ACTION_CANCEL -> isPressed = false
                else -> {}
            }
            return true
        }
        return false
    }
}