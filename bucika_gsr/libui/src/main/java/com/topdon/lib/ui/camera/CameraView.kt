package com.topdon.lib.ui.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.NonNull
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.blankj.utilcode.util.ToastUtils
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.lib.ui.R
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.thread

class CameraView : LinearLayout, ScaleGestureDetector.OnScaleGestureListener {

    /**预览 */
    lateinit var mTextureView: TextureView

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
        mTextureView.alpha = 0.4f
        lis = ScaleGestureDetector(context, this)

        onResumeView()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mCameraDevice?.close()
    }

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
                if (moveX - scaleW < 0f) moveX = 0f + scaleW
                if (moveY - scaleH < 0f) moveY = 0f + scaleH
                if (moveX + scaleW > parentViewW - mTextureView.width) {
                    moveX = parentViewW - mTextureView.width - scaleW
                }
                if (moveY + scaleH > parentViewH - mTextureView.height) {
                    moveY = parentViewH - mTextureView.height - scaleH
                }
                mTextureView.x = moveX
                mTextureView.y = moveY
            }
            MotionEvent.ACTION_UP -> {
                isScale = false//实际以手指抬起设定缩放结束
            }
        }
        return lis.onTouchEvent(event)
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

//////////////////
    /**相机权限请求标识 */
    private val REQUEST_CAMERA_CODE = 0x100

    /**拍照按钮 */
    private var mBtnTake: Button? = null

    /**图片 */
    private var mImageView: ImageView? = null

    /**照相机ID，标识前置后置 */
    private lateinit var mCameraId: String

    /**相机尺寸 */
    private var mCaptureSize: Size? = null

    /**图像读取者 */
    private lateinit var mImageReader: ImageReader

    /**图像主线程Handler */
    private lateinit var mCameraHandler: Handler

    /**相机设备 */
    private var mCameraDevice: CameraDevice? = null

    /**预览大小 */
    private var mPreviewSize: Size? = null

    /**相机请求 */
    private lateinit var mCameraCaptureBuilder: CaptureRequest.Builder

    /**相机拍照捕获会话 */
    private var mCameraCaptureSession: CameraCaptureSession? = null

    /**相机管理者 */
    private var mCameraManager: CameraManager? = null

    /**相机设备状态回调 */
    private val mStateCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            override fun onOpened(@NonNull camera: CameraDevice) {
                // 打开
                mCameraDevice = camera
                // 开始预览
                takePreview()
            }

            override fun onDisconnected(@NonNull camera: CameraDevice) {
                // 断开连接
                camera.close()
                mCameraDevice = null
            }

            override fun onError(@NonNull camera: CameraDevice, error: Int) {
                // 异常
                camera.close()
                mCameraDevice = null
            }
        }

    /**
     * 预览
     */
    private fun takePreview() {
//        mTextureView.rotation = 270f
        mTextureView.rotation = 0f
        // 获取SurfaceTexture
        val surfaceTexture = mTextureView.surfaceTexture
        // 设置默认的缓冲大小
        surfaceTexture!!.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
        // 创建Surface
        val previewSurface = Surface(surfaceTexture)
        try {
            // 创建预览请求
            mCameraCaptureBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            // 将previewSurface添加到预览请求中
            mCameraCaptureBuilder.addTarget(previewSurface)
            // 创建会话
            mCameraDevice!!.createCaptureSession(
                listOf(
                    previewSurface,
                    mImageReader.surface
                ), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(@NonNull session: CameraCaptureSession) {
                        try {
                            // 配置
                            val captureRequest = mCameraCaptureBuilder.build()
                            // 設置session
                            mCameraCaptureSession = session
                            // 设置重复预览请求
                            mCameraCaptureSession!!.setRepeatingRequest(
                                captureRequest,
                                null,
                                mCameraHandler
                            )
                        } catch (e: CameraAccessException) {
                            e.printStackTrace()
                        }
                    }

                    override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {
                        // 配置失败
                    }
                }, mCameraHandler
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
                // 设置相机参数并打开相机
                Log.w("123", "width:$width, height:$height")
                //w:h = 1 / 1.33
                setUpCamera(width, height)
//                openCamera()
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


    /**
     * 打开相机
     */
    @SuppressLint("MissingPermission")
    fun openCamera() {
        // 获取照相机管理者
        try {
            mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
            mCameraManager!!.openCamera(mCameraId, mStateCallback, mCameraHandler)
        } catch (e: Exception) {
            Log.e("123", "打开相机失败:${e.message}")
            ToastUtils.showShort("打开相机失败")
        }
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
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // 遍历所有摄像头
            for (cameraId in cameraManager.cameraIdList) {
                // 相机特性
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                // 获取摄像头是前置还是后置
                val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                // 此处默认打开后置摄像头
                if (null != facing && CameraCharacteristics.LENS_FACING_FRONT == facing) continue
                // 获取StreamConfigurationMap，管理摄像头支持的所有输出格式和尺寸
                val map =
                    cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                // 根据TextureView的尺寸设置预览尺寸
                mPreviewSize =
                    getOptimalSize(
                        map.getOutputSizes(SurfaceTexture::class.java),
                        width,
                        height
                    )
                // 获取相机支持的最大拍照尺寸
                val sizes = map.getOutputSizes(ImageFormat.JPEG)
                val w = 1000
                val h = w * sizes[0].height / sizes[0].width
                mCaptureSize = Size(w, h)
                Log.w("123", "w:${sizes[0].width}, h:${sizes[0].height}")
                Log.w("123", "调整后w:${w}, h:${h}")
//                mCaptureSize = Size(1000, 1000)
//                mCaptureSize =
//                    Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG))) { lhs, rhs ->
//                        java.lang.Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth())
//                    }
                // 此处ImageReader用于拍照所需
                setupImageReader()
                // 为摄像头赋值
                mCameraId = cameraId
                break
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
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
            Collections.min(
                sizeList
            ) { lhs, rhs -> java.lang.Long.signum((lhs.width * lhs.height - rhs.width * rhs.height).toLong()) }
        } else sizeMap[0]
    }

    private var flag = 0

    /**
     * 设置ImageReader
     */
    private fun setupImageReader() {
        // 2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(
            mCaptureSize!!.width,
            mCaptureSize!!.height,
            ImageFormat.JPEG,
            1
        )
        // 设置图像可用监听
        mImageReader.setOnImageAvailableListener({ reader ->
            flag = 1
            // 获取图片
            val image: Image = reader.acquireLatestImage()
            // 提交任务，保存图片
            mCameraHandler.post(ImageSaver(image))
            // 更新UI
            runOnUiThread { // 获取字节缓冲区
                val buffer: ByteBuffer = image.planes[0].buffer
                // 创建数组之前调用此方法，恢复默认设置
                buffer.rewind()
                // 创建与缓冲区内容大小相同的数组
                val bytes = ByteArray(buffer.remaining())
                // 从缓冲区存入字节数组,读取完成之后position在末尾
                buffer[bytes]
                // 获取Bitmap图像
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                // 显示
                if (null != bitmap) {
                    val h = bitmap.height
                    val w = bitmap.width
                    mImageView?.let {
                        val sw = ScreenUtil.getScreenWidth(context)
                        it.layoutParams.height = sw / 2 * w / h
                        it.layoutParams.width = sw / 2
                        it.setImageBitmap(bitmap)
                    }

                }
                flag++
                thread {
                    while (flag < 3) {
//                        delay(100)
                        Thread.sleep(100)
                    }
                    flag = 0
                    image.close()
                }
            }
        }, mCameraHandler)
    }

    /**
     * 保存图片任务
     */
    private inner class ImageSaver(image: Image) : Runnable {

        /**图像 */
        private val mImage: Image = image

        override fun run() {
//            ImageSaverTool().save(mImage)
            flag++
        }

    }

}

