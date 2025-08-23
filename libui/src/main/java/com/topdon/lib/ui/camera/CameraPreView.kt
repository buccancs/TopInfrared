package com.topdon.lib.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintSet
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.topdon.lib.core.listener.BitmapViewListener
import com.topdon.lib.ui.R
import kotlinx.android.synthetic.main.camera_lay.view.*
import java.util.*

class CameraPreView : LinearLayout, ScaleGestureDetector.OnScaleGestureListener,
    BitmapViewListener {
    private var cameraCharacteristics: CameraCharacteristics ?= null
    private var isReverse : Boolean = false
    private lateinit var mTextureView: TextureView
    private var cameraWidth = 0

    var isPreviewing = false

    var cameraPreViewCloseListener : (() -> Unit) ?= null

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
        inflate(context, R.layout.camera_lay, this)
        mTextureView = findViewById(R.id.camera_texture)
        mTextureView.post { cameraWidth = mTextureView.width }
        lis = ScaleGestureDetector(context, this)
        onResumeView()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isPreviewing = false
        mCameraDevice?.close()
        mCameraHandler?.removeCallbacksAndMessages(null)
    }

    private var startX = 0f
    private var startY = 0f
    private var moveX = 0f
    private var moveY = 0f
    private var parentViewW = 0f
    private var parentViewH = 0f
    private var isScale = false
    private var scale = 1f
    private var scaleW = 0f
    private var scaleH = 0f

    private lateinit var lis: ScaleGestureDetector

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isScale && event.action != MotionEvent.ACTION_UP) {
            return lis.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                scaleW = mTextureView.width * (scale - 1) / 2f
                scaleH = mTextureView.height * (scale - 1) / 2f
                startX = event.x - mTextureView.x
                startY = event.y - mTextureView.y
                val view: View = mTextureView.parent as View
                parentViewW = view.width.toFloat()
                parentViewH = view.height.toFloat()
            }
            MotionEvent.ACTION_MOVE -> {
                moveX = event.x - startX
                moveY = event.y - startY

                mTextureView.x = moveX
                mTextureView.y = moveY
            }
            MotionEvent.ACTION_UP -> {
                isScale = false
                val startX = viewX
                val startY = viewY
                if ((viewX < 0 && startX <  -mTextureView.width * scale + SizeUtils.dp2px(10f)) ||
                    (startX > 0 && startX > parentViewW - SizeUtils.dp2px(10f)) ||
                    (startY < 0 && startY < -mTextureView.height * scale + SizeUtils.dp2px(10f)) ||
                    (startY > 0 && startY > parentViewH - SizeUtils.dp2px(10f))){
                    cameraPreViewCloseListener?.invoke()
                }
            }
        }
        return lis.onTouchEvent(event)
    }
    
    public fun getBitmap() : Bitmap? {
        return mTextureView.bitmap
    }
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        isScale = true
        detector?.let {
            val scaleFactor = it.scaleFactor - 1
            if (scaleFactor < 0){
                if (scale > 0.1){
                    scale += scaleFactor
                    mTextureView.scaleX = scale
                    mTextureView.scaleY = scale
                }
            }else{
                scale += scaleFactor
                mTextureView.scaleX = scale
                mTextureView.scaleY = scale
            }
        }
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        isScale = true
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
    }
    fun onResume(){
        if (mCameraDevice!=null){
            mCameraDevice?.close()
            openCamera()
        }
    }

    private val REQUEST_CAMERA_CODE = 0x100

    private var mImageView: ImageView? = null

    private lateinit var mCameraId: String

    private var mCaptureSize: Size? = null

    private var mImageReader: ImageReader? = null

    private var mCameraHandler: Handler? = null

    private var mCameraDevice: CameraDevice? = null

    private var mPreviewSize: Size? = null

    private lateinit var mCaptureBuilder: CaptureRequest.Builder

    private var mCameraCaptureSession: CameraCaptureSession? = null

    private var mCameraManager: CameraManager? = null

    private val mStateCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            override fun onOpened(@NonNull camera: CameraDevice) {
                XLog.i("开启预览")
                mCameraDevice = camera
                takePreview()
            }

            override fun onDisconnected(@NonNull camera: CameraDevice) {
                XLog.i("关闭预览")
                isPreviewing = false
            }

            override fun onError(@NonNull camera: CameraDevice, error: Int) {
                isPreviewing = false
                camera.close()
                mCameraDevice = null
                XLog.e("预览异常 error: $error")
            }
        }

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

    private fun takePreview() {
        updateRotation()
        val surfaceTexture = mTextureView.surfaceTexture
        surfaceTexture!!.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
        val previewSurface = Surface(surfaceTexture)
        try {
            mCaptureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mCaptureBuilder.addTarget(previewSurface)
            mCameraDevice!!.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(@NonNull session: CameraCaptureSession) {
                        try {
                            val captureRequest = mCaptureBuilder.build()
                            mCameraCaptureSession = session
                            mCameraCaptureSession?.setRepeatingRequest(
                                captureRequest,
                                null,
                                mCameraHandler
                            )
                        } catch (e: CameraAccessException) {
                            XLog.e("相机异常：${e.printStackTrace()}")
                        }
                    }

                    override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {
                        XLog.e("配置失败")
                    }
                },
                mCameraHandler
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun onResumeView() {
        mTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                XLog.w("width:$width, height:$height")
                setUpCamera(width, height)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    private fun setUpCamera(width: Int, height: Int) {
        mCameraHandler = Handler(Looper.getMainLooper())
        mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in mCameraManager!!.cameraIdList) {
                XLog.i("camera id: $cameraId")
                cameraCharacteristics = mCameraManager!!.getCameraCharacteristics(cameraId)
                val facing = cameraCharacteristics?.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) continue
                val map = cameraCharacteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                val mapList = map.getOutputSizes(SurfaceTexture::class.java)

                mPreviewSize = getOptimalSize(mapList, width, height)
                val constraintSet = ConstraintSet()
                constraintSet.clone(camera_lay_root)
                constraintSet.constrainHeight(mTextureView.id,width * mPreviewSize!!.width / mPreviewSize!!.height)
                constraintSet.applyTo(camera_lay_root);
                XLog.w("mPreviewSize:${mPreviewSize}")
                val sizes = map.getOutputSizes(ImageFormat.JPEG)
                XLog.w("size:${sizes.toList()}")
                val w = 1000
                val h = w * sizes[0].height / sizes[0].width
                XLog.w("选取比例 w:${sizes[0].width}, h:${sizes[0].height}")
                XLog.w("调整后 w: ${w}, h:${h}")
                mCameraId = cameraId
                break
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e("123", "设置相机参数:${e.message}")
        }
    }

    private fun getOptimalSize(sizeMap: Array<Size>, width: Int, height: Int): Size {
        val sizeList: MutableList<Size> = ArrayList()
        for (option in sizeMap) {
            if (width > height) {
                if (option.width > width && option.height > height) {
                    sizeList.add(option)
                }
            } else {
                if (option.width > height && option.height > width) {
                    sizeList.add(option)
                }
            }
        }
        return if (sizeList.size > 0) {
            Collections.min(sizeList) { lhs, rhs ->
                java.lang.Long.signum((lhs.width * lhs.height - rhs.width * rhs.height).toLong())
            }
        } else sizeMap[0]
    }

    @SuppressLint("MissingPermission")
    fun openCamera() {
        isPreviewing = true
        try {
            mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            mCameraManager!!.openCamera(mCameraId, mStateCallback, mCameraHandler)
        } catch (e: Exception) {
            isPreviewing = false
            XLog.e("打开相机失败:${e.message}")
            ToastUtils.showShort("打开相机失败")
        }
    }

    @SuppressLint("MissingPermission")
    fun closeCamera() {
        isPreviewing = false
        try {
            mCameraDevice?.close()
            mTextureView.x = 0f
            mTextureView.y = 0f
            mTextureView.scaleX = 1f
            mTextureView.scaleY = 1f
            scale = 1f
        } catch (e: Exception) {
            XLog.e("关闭相机失败:${e.message}")
            ToastUtils.showShort("关闭相机失败")
        }
    }

    override val viewX: Float
        get() = mTextureView.x - (viewWidth - mTextureView.width)/2
    override val viewY: Float
        get() = mTextureView.y - (viewHeight - mTextureView.height)/2
    override val viewAlpha: Float
        get() = mTextureView.alpha
    override val viewWidth: Float
        get() = mTextureView.width * scale
    override val viewHeight: Float
        get() = mTextureView.height * scale
    override val viewScale: Float
        get() = scale

    fun setCameraAlpha(alpha : Float){
        mTextureView?.alpha = 1 - alpha
    }

    fun setZoom(zoomLeve : Int){
        scale = zoomLeve * 0.5f
        mTextureView.scaleX = scale
        mTextureView.scaleY = scale
        invalidate()
    }

}
