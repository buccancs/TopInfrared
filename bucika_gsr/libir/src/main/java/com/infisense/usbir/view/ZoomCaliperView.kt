package com.infisense.usbir.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.hardware.camera2.*
import android.os.Build
import android.util.AttributeSet
import android.util.Size
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Magnifier
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.infisense.usbir.R
import com.infisense.usbir.utils.TargetUtils
import com.topdon.lib.core.bean.ObserveBean


/**
 * 缩放view基类
 */
class ZoomCaliperView : LinearLayout, ScaleGestureDetector.OnScaleGestureListener{
    private var centerX: Float = Float.MAX_VALUE
    private var centerY: Float = Float.MAX_VALUE
    private var cameraCharacteristics: CameraCharacteristics ?= null
    private var isReverse : Boolean = false
    private lateinit var mTextureView : View
    private var canScale = false
    private var def_caliper = 180f //2米是出厂测量数据标准
    var magnifier : Magnifier ?= null
    var textureMagnifier : Magnifier ?= null
    var m : Float = 0.0f

    var zoomViewCloseListener : (() -> Unit) ?= null

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
        inflate(context, R.layout.zoom_bb, this)
        mTextureView = findViewById(R.id.camera_texture)
        lis = ScaleGestureDetector(context, this)
        originalBitmap = (resources.getDrawable(R.drawable.svg_ic_target_horizontal_person_green) as BitmapDrawable).bitmap
//        pxBitmapHeight = originalBitmap.height.toFloat()
        originalBitmapWidth = originalBitmap.width.toFloat()
        originalBitmapHeight = originalBitmap.height.toFloat()
//        setCaliperM(50f)
        onResumeView()
    }

    fun setImageSize(imageHeight: Int, imageWidth: Int, parentViewWidth: Int, parentViewHeight: Int) {
        if (this.imageHeight == imageHeight && this.imageWidth == imageWidth){
            return
        }
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
        showBitmapHeightWidth = pxBitmapHeight * originalBitmapWidth / originalBitmapHeight * xscale
        val layoutParams =  mTextureView.layoutParams
        layoutParams.width  = showBitmapHeightWidth.toInt()
        layoutParams.height = showBitmapHeight.toInt()
//        Log.e("测试","旋转后的宽高：标靶"+showBitmapHeight+"///"+imageHeight+"---")
        mTextureView.layoutParams = layoutParams
        (mTextureView as ImageView ).setImageBitmap(originalBitmap)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    private var startX = 0f//记录落点到控件的距离
    private var startY = 0f
    private var moveX = 0f
    private var moveY = 0f
    private var parentViewW = 0f
    private var parentViewH = 0f
    private var isScale = false
    private var scale = 1f
    private var scaleW = 0f//单边缩放长度
    private var scaleH = 0f

    //原始图片
    private lateinit var originalBitmap: Bitmap
    private var imageWidth = 0
    private var imageHeight = 0
    private var parentViewWidth = 0f
    private var parentViewHeight = 0f
    private var xscale = 0f
    private var yscale = 0f
    private var originalBitmapWidth = 0f
    private var originalBitmapHeight = 0f

    private var pxBitmapHeight = 200f

    private var showBitmapHeightWidth = 0f
    private var showBitmapHeight = 0f

    private lateinit var lis: ScaleGestureDetector
    var isCheckChildView = false
    var contentWith = 0
    var contentHeight = 0


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (canScale && isScale && event.action != MotionEvent.ACTION_UP) {
            return lis.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                scaleW = mTextureView.width * (scale - 1) / 2f
                scaleH = mTextureView.height * (scale - 1) / 2f
                startX = event.x - mTextureView.x
                startY = event.y - mTextureView.y
                val view: View = mTextureView.parent as View
                parentViewW = view.measuredWidth.toFloat()
                parentViewH = view.measuredHeight.toFloat()
                isCheckChildView = isTouchPointInView(mTextureView, event.rawX.toInt(), event.rawY.toInt())
            }
            MotionEvent.ACTION_MOVE -> {
                if (isCheckChildView){
                    //滑动
                    moveX = event.x - startX
                    moveY = event.y - startY
                    //越界归位
                    if(m < 100f && m >= 50f){
                        contentWith   = (mTextureView.measuredWidth / 2).toInt()
                        contentHeight = (mTextureView.measuredHeight / 2).toInt()
                        if (moveX < (- contentWith / 2)) moveX = (- contentWith / 2).toFloat()
                        if (moveY < (- contentHeight / 2)) moveY = (- contentHeight / 2).toFloat()
                        if (moveX > parentViewW  - contentWith * 4 / 3 ) {
                            moveX = parentViewW - contentWith * 4 / 3
                        }
                        if (parentViewH > parentViewW){
                            if (moveY > parentViewH  - contentHeight * 4 / 3) {
                                moveY = parentViewH  - contentHeight * 4 / 3
                            }
                        }else{
                            if (moveY > parentViewH  - contentHeight * 4 / 3 ) {
                                moveY = parentViewH - contentHeight * 4 / 3
                            }
                        }
                    }else if (m <= 20f){
                        contentWith   = (mTextureView.measuredWidth / 2f).toInt()
                        contentHeight = (mTextureView.measuredHeight / 2f).toInt()
                        if (moveX < (- contentWith / 2)) moveX = (- contentWith / 2).toFloat()
                        if (moveY < (- contentHeight / 2)) moveY = (- contentHeight / 2).toFloat()
                        if (moveX > parentViewW  - contentWith ) {
                            moveX = parentViewW - contentWith
                        }
                        if (parentViewH > parentViewW){
                            if (moveY > parentViewH  - contentHeight) {
                                moveY = parentViewH  - contentHeight
                            }
                        }else{
                            if (moveY > parentViewH  - contentHeight ) {
                                moveY = parentViewH - contentHeight
                            }
                        }
                    }else{
                        contentWith   = mTextureView.width
                        contentHeight = mTextureView.height
                        if (moveX < (- contentWith / 2)) moveX = (- contentWith / 2).toFloat()
                        if (moveY < (- contentHeight / 2)) moveY = (- contentHeight / 2).toFloat()
                        if (moveX > parentViewW - mTextureView.width / 2) {
                            moveX = parentViewW - mTextureView.width / 2
                        }
                        if (moveY > parentViewH - mTextureView.height / 2) {
                            moveY = parentViewH - mTextureView.height / 2
                        }
                    }
                    mTextureView.x = moveX
                    mTextureView.y = moveY
                    centerX = mTextureView.x + mTextureView.measuredWidth / 2
                    centerY = mTextureView.y + mTextureView.measuredHeight / 2
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && m < 100f) {
                        magnifier?.show(centerX, centerY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                isCheckChildView = false
                isScale = false
                val startX = viewX
                val startY = viewY
                if ((viewX < 0 && startX <  -mTextureView.width * scale + SizeUtils.dp2px(10f)) ||
                    (startX > 0 && startX > parentViewW - SizeUtils.dp2px(10f)) ||
                    (startY < 0 && startY < -mTextureView.height * scale + SizeUtils.dp2px(10f)) ||
                    (startY > 0 && startY > parentViewH - SizeUtils.dp2px(10f))){
                    zoomViewCloseListener?.invoke()
                }
            }
        }
        var canTouch = isCheckChildView
        if (canScale){
            canTouch = lis.onTouchEvent(event)
        }
//        if (!isCheckChildView){
//            parentView.requestFocus()
//        }
        return canTouch
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }
    private fun isTouchPointInView(targetView: View?, xAxis: Int, yAxis: Int): Boolean {
        if (targetView == null) {
            return false
        }
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        val left = location[0]
        val top = location[1]
        val right = left + targetView.measuredWidth
        val bottom = top + targetView.measuredHeight
        return (yAxis >= top) && (yAxis <= bottom) && (xAxis >= left) && (xAxis <= right)
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        //缩放
        isScale = true
        detector?.let {
            val scaleFactor = it.scaleFactor - 1
            scale += scaleFactor
            mTextureView.scaleX = scale
            mTextureView.scaleY = scale
        }
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        isScale = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }

    /**预览大小 */
    private var mPreviewSize: Size? = null



    fun setRotation(isReverse : Boolean){
        this.isReverse = isReverse
        updateRotation()
    }

    private fun updateRotation(){
        if(isReverse){
            mTextureView.rotation = 180f
        }else{
            mTextureView.rotation = 0f
        }
    }

    private fun onResumeView() {
    }


    val viewX: Float
        get() = mTextureView.x - (viewWidth - mTextureView.width)/2
    val viewY: Float
        get() = mTextureView.y - (viewHeight - mTextureView.height)/2
    val viewAlpha: Float
        get() = mTextureView.alpha
    val viewWidth: Float
        get() = mTextureView.width * scale
    val viewHeight: Float
        get() = mTextureView.height * scale
    val viewScale: Float
        get() = scale

    fun setCameraAlpha(alpha : Float){
        mTextureView?.alpha = 1 - alpha
    }

    fun setCaliperM(m: Float){
        scale = m / def_caliper
        mTextureView.scaleX = scale
        mTextureView.scaleY = scale
        invalidate()
    }
    private var curChooseMeasureMode: Int = ObserveBean.TYPE_MEASURE_PERSON
    private var curChooseTargetMode: Int = ObserveBean.TYPE_TARGET_HORIZONTAL
    fun updateSelectBitmap(targetMeasureMode: Int, targetType: Int, targetColorType: Int, parentCameraView : View?){
        if(curChooseTargetMode == targetType && curChooseMeasureMode == targetMeasureMode){
            return
        }
        curChooseMeasureMode = targetMeasureMode
        curChooseTargetMode = targetType
        updateTargetBitmap(targetMeasureMode, targetType, targetColorType, parentCameraView)
    }
    fun updateTargetBitmap(targetMeasureMode: Int, targetType: Int, targetColorType: Int, parentCameraView : View?){
        this.visibility = View.VISIBLE
        m = TargetUtils.getMeasureSize(targetMeasureMode)
        val targetIcon = TargetUtils.getSelectTargetDraw(targetMeasureMode, targetType, targetColorType)
        originalBitmap = (resources.getDrawable( targetIcon) as BitmapDrawable).bitmap
        (mTextureView as ImageView ).setImageBitmap(originalBitmap)
//        Log.e("测试","旋转后的宽高updateSelectBitmap"+parentCameraView!!.width+"---"+parentCameraView!!.height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            magnifier?.dismiss()
            if (m >= 100f){
                setCaliperM(def_caliper)
                mTextureView.visibility = View.VISIBLE
                textureMagnifier?.dismiss()
                magnifier?.dismiss()
                invalidate()
                return
            }
            if (parentCameraView != null){
                val builder = Magnifier.Builder(parentCameraView)
                if (m < 50f){
                    setCaliperM(def_caliper / 2)
                    mTextureView.visibility = View.INVISIBLE
                    builder.setInitialZoom(4f)
                    builder.setCornerRadius(SizeUtils.dp2px(282f).toFloat())
                    builder.setClippingEnabled(false)
                    builder.setOverlay(ContextCompat.getDrawable(context,targetIcon))
                    builder.setSize(
                        SizeUtils.dp2px(282f),
                        SizeUtils.dp2px(282f)
                    )
                    magnifier = builder.build()
                }else if (m >= 50f && m < 100f){
                    setCaliperM(def_caliper / 2)
                    mTextureView.visibility = View.VISIBLE
//                    builder.setInitialZoom(1.15f)
                    builder.setInitialZoom(2f)
//                    builder.setOverlay(ContextCompat.getDrawable(context,targetIcon))
                    builder.setCornerRadius(SizeUtils.dp2px(282f).toFloat())
                    builder.setClippingEnabled(false)
                    builder.setSize(
                        SizeUtils.dp2px(282f),
                        SizeUtils.dp2px(282f)
                    )
                    magnifier = builder.build()
                }

            }
            requestLayout()
            mTextureView.postDelayed(Runnable {
//                if (centerX == Float.MAX_VALUE && centerY == Float.MAX_VALUE){
                centerX = parentCameraView!!.measuredWidth.toFloat() / 2
                centerY = parentCameraView!!.measuredHeight.toFloat() / 2
                mTextureView.x = centerX  - mTextureView.measuredWidth / 2
                mTextureView.y = centerY - mTextureView.measuredHeight / 2
//                }
                magnifier?.show(centerX, centerY)
            },200)
        }
    }
    fun hideView(){
        this.visibility = GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            magnifier?.dismiss()
        }
    }

    fun showView(){
        this.visibility = VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            magnifier?.show(centerX,centerY)
        }
    }

    fun updateMagnifier(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            magnifier?.update()
        }
    }

    /**
     * 还原
     */
    fun del(reductionXY: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            magnifier?.dismiss()
        }
        curChooseMeasureMode = ObserveBean.TYPE_MEASURE_PERSON
        curChooseTargetMode = ObserveBean.TYPE_TARGET_HORIZONTAL
        if(this.visibility == View.VISIBLE){
            this.visibility = GONE
            if (reductionXY){
                centerX = Float.MAX_VALUE
                centerY = Float.MAX_VALUE
            }else{
                val parent = parent as ViewGroup
                centerX = parent.measuredWidth.toFloat() / 2
                centerY = parent.measuredHeight.toFloat() / 2
                mTextureView.x = centerX  - mTextureView.width / 2
                mTextureView.y = centerY - mTextureView.height / 2
            }
        }

    }

    fun updateCenter(){
        val parent = parent as ViewGroup
        centerX = parent.measuredWidth.toFloat() / 2
        centerY = parent.measuredHeight.toFloat() / 2
        mTextureView.x = centerX  - mTextureView.width / 2
        mTextureView.y = centerY - mTextureView.height / 2
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            magnifier?.show(centerX, centerY)
        }
    }


}
