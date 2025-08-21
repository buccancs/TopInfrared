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


/**
 * 相机预览
 */
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
                //滑动
                moveX = event.x - startX
                moveY = event.y - startY
                //根据移动情况，不可见时候关闭
//                if (moveX-scaleW < -mTextureView.width ||
//                    moveX+scaleW > parentViewW ||
//                    moveY - scaleH < -mTextureView.height ||
//                    moveY + scaleH > parentViewH){
//                    cameraPreViewCloseListener?.invoke()
//                }

                //越界归位
//                if (moveX - scaleW < 0f) moveX = 0f + scaleW
//                if (moveY - scaleH < 0f) moveY = 0f + scaleH
//                if (moveX + scaleW > parentViewW - mTextureView.width) {
//                    moveX = parentViewW - mTextureView.width - scaleW
//                }
//                if (moveY + scaleH > parentViewH - mTextureView.height) {
//                    moveY = parentViewH - mTextureView.height - scaleH
//                }
//                Log.e("测试---","/"+(moveX + scaleW)+"///"+(parentViewW - mTextureView.width))
                mTextureView.x = moveX
                mTextureView.y = moveY
            }
            MotionEvent.ACTION_UP -> {
                isScale = false//实际以手指抬起设定缩放结束
                val startX = viewX
                val startY = viewY
//                Log.e("测试","/"+(startX)+"///"+startY+"///"+(mTextureView.width)+"//"+mTextureView.width * scale)
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
    /**
     * 保存图片
     */
    public fun getBitmap() : Bitmap? {
        return mTextureView.bitmap
    }
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        //缩放
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
        //处理切换后台，打开系统相机后，回到app导致预览不更新画面的问题
        if (mCameraDevice!=null){
            mCameraDevice?.close()
            openCamera()
        }
    }

//////////////////
    /**相机权限请求标识 */
    private val REQUEST_CAMERA_CODE = 0x100

    /**图片 */
    private var mImageView: ImageView? = null

    /**照相机ID，标识前置后置 */
    private lateinit var mCameraId: String

    /**相机尺寸 */
    private var mCaptureSize: Size? = null

    /**图像读取者 */
    private var mImageReader: ImageReader? = null

    /**图像主线程Handler */
    private var mCameraHandler: Handler? = null

    /**相机设备 */
    private var mCameraDevice: CameraDevice? = null

    /**预览大小 */
    private var mPreviewSize: Size? = null

    /**相机请求 */
    private lateinit var mCaptureBuilder: CaptureRequest.Builder

    /**相机拍照捕获会话 */
    private var mCameraCaptureSession: CameraCaptureSession? = null

    /**相机管理者 */
    private var mCameraManager: CameraManager? = null

    /**相机设备状态回调 */
    private val mStateCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            override fun onOpened(@NonNull camera: CameraDevice) {
                // 打开
                XLog.i("开启预览")
                mCameraDevice = camera
                takePreview()
            }

            override fun onDisconnected(@NonNull camera: CameraDevice) {
                // 断开连接
                XLog.i("关闭预览")
                isPreviewing = false
//                camera.close()
//                mCameraDevice = null
            }

            override fun onError(@NonNull camera: CameraDevice, error: Int) {
                // 异常
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

    /**
     * 预览
     * 点击开启相机后触发
     */
    private fun takePreview() {
//        mTextureView.rotation = 270f
//        mTextureView.rotation = 0f
        updateRotation()
//        val layoutParams = mTextureView.layoutParams
//        layoutParams.width = cameraWidth / 2
//        mTextureView.layoutParams = layoutParams
        val surfaceTexture = mTextureView.surfaceTexture
        // 设置默认的缓冲大小
        surfaceTexture!!.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
        // 创建Surface
        val previewSurface = Surface(surfaceTexture)
        try {
            // 创建预览请求
            mCaptureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            // 将previewSurface添加到预览请求中
            mCaptureBuilder.addTarget(previewSurface)
            // 创建会话
            mCameraDevice!!.createCaptureSession(
                listOf(previewSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(@NonNull session: CameraCaptureSession) {
                        try {
                            // 配置
                            val captureRequest = mCaptureBuilder.build()
                            // 設置session
                            mCameraCaptureSession = session
                            // 设置重复预览请求
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
                        // 配置失败
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
                // SurfaceTexture可用
                XLog.w("width:$width, height:$height")
                setUpCamera(width, height)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                // SurfaceTexture大小改变
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                // SurfaceTexture 销毁
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // SurfaceTexture 更新
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }


    /**
     * 设置相机参数
     * @param width 宽度
     * @param height 高度
     */
    private fun setUpCamera(width: Int, height: Int) {
        // 创建Handler
        mCameraHandler = Handler(Looper.getMainLooper())
        // 获取摄像头的管理者
        mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // 遍历所有摄像头,找到一个取消遍历
            for (cameraId in mCameraManager!!.cameraIdList) {
                XLog.i("camera id: $cameraId")
                cameraCharacteristics = mCameraManager!!.getCameraCharacteristics(cameraId)
                // 获取摄像头是前置还是后置
                val facing = cameraCharacteristics?.get(CameraCharacteristics.LENS_FACING)
                // 前置摄像头跳过
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) continue
                // 获取StreamConfigurationMap，管理摄像头支持的所有输出格式和尺寸
                val map = cameraCharacteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                // 根据TextureView的尺寸设置预览尺寸
                val mapList = map.getOutputSizes(SurfaceTexture::class.java)

                mPreviewSize = getOptimalSize(mapList, width, height)
                val constraintSet = ConstraintSet()
                constraintSet.clone(camera_lay_root)
                constraintSet.constrainHeight(mTextureView.id,width * mPreviewSize!!.width / mPreviewSize!!.height)
                constraintSet.applyTo(camera_lay_root);
                XLog.w("mPreviewSize:${mPreviewSize}")
                // 获取相机支持的最大拍照尺寸
                val sizes = map.getOutputSizes(ImageFormat.JPEG)
                XLog.w("size:${sizes.toList()}")
                val w = 1000
                val h = w * sizes[0].height / sizes[0].width
//                mCaptureSize = Size(w, h)//影响拍照尺寸
                XLog.w("选取比例 w:${sizes[0].width}, h:${sizes[0].height}")
                XLog.w("调整后 w: ${w}, h:${h}")
                // 此处ImageReader用于拍照所需
//                setupImageReader()
                // 为摄像头赋值
                mCameraId = cameraId
                break
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            Log.e("123", "设置相机参数:${e.message}")
        }
    }

    /**
     * 选择SizeMap中大于并且最接近width和height的size
     * @param sizeMap 可选的尺寸
     * @param width 宽
     * @param height 高
     * @return 最接近width和height的size
     */
    private fun getOptimalSize(sizeMap: Array<Size>, width: Int, height: Int): Size {
        // 创建列表
        val sizeList: MutableList<Size> = ArrayList()
        // 遍历
        for (option in sizeMap) {
            // 判断宽度是否大于高度
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
        // 判断存储Size的列表是否有数据
        return if (sizeList.size > 0) {
            Collections.min(sizeList) { lhs, rhs ->
                java.lang.Long.signum((lhs.width * lhs.height - rhs.width * rhs.height).toLong())
            }
        } else sizeMap[0]
    }

    /**
     * 打开相机
     */
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

    /**
     * 关闭相机
     */
    @SuppressLint("MissingPermission")
    fun closeCamera() {
        isPreviewing = false
        try {
            mCameraDevice?.close()
            //恢复原始状态
            mTextureView.x = 0f
            mTextureView.y = 0f
            mTextureView.scaleX = 1f
            mTextureView.scaleY = 1f
            scale = 1f
//            isReverse = false
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
