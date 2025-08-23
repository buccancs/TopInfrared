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
                isScale = false
            }
        }
        return lis.onTouchEvent(event)
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
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

    private val REQUEST_CAMERA_CODE = 0x100

    private var mBtnTake: Button? = null

    private var mImageView: ImageView? = null

    private lateinit var mCameraId: String

    private var mCaptureSize: Size? = null

    private lateinit var mImageReader: ImageReader

    private lateinit var mCameraHandler: Handler

    private var mCameraDevice: CameraDevice? = null

    private var mPreviewSize: Size? = null

    private lateinit var mCameraCaptureBuilder: CaptureRequest.Builder

    private var mCameraCaptureSession: CameraCaptureSession? = null

    private var mCameraManager: CameraManager? = null

    private val mStateCallback: CameraDevice.StateCallback =
        object : CameraDevice.StateCallback() {
            override fun onOpened(@NonNull camera: CameraDevice) {
                mCameraDevice = camera
                takePreview()
            }

            override fun onDisconnected(@NonNull camera: CameraDevice) {
                camera.close()
                mCameraDevice = null
            }

            override fun onError(@NonNull camera: CameraDevice, error: Int) {
                camera.close()
                mCameraDevice = null
            }
        }

    private fun takePreview() {
        mTextureView.rotation = 0f
        val surfaceTexture = mTextureView.surfaceTexture
        surfaceTexture!!.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
        val previewSurface = Surface(surfaceTexture)
        try {
            mCameraCaptureBuilder =
                mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            mCameraCaptureBuilder.addTarget(previewSurface)
            mCameraDevice!!.createCaptureSession(
                listOf(
                    previewSurface,
                    mImageReader.surface
                ), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(@NonNull session: CameraCaptureSession) {
                        try {
                            val captureRequest = mCameraCaptureBuilder.build()
                            mCameraCaptureSession = session
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
                Log.w("123", "width:$width, height:$height")
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

    @SuppressLint("MissingPermission")
    fun openCamera() {
        try {
            mCameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager?
            mCameraManager!!.openCamera(mCameraId, mStateCallback, mCameraHandler)
        } catch (e: Exception) {
            Log.e("123", "打开相机失败:${e.message}")
            ToastUtils.showShort("打开相机失败")
        }
    }

    private fun setUpCamera(width: Int, height: Int) {
        mCameraHandler = Handler(Looper.getMainLooper())
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                if (null != facing && CameraCharacteristics.LENS_FACING_FRONT == facing) continue
                val map =
                    cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                mPreviewSize =
                    getOptimalSize(
                        map.getOutputSizes(SurfaceTexture::class.java),
                        width,
                        height
                    )
                val sizes = map.getOutputSizes(ImageFormat.JPEG)
                val w = 1000
                val h = w * sizes[0].height / sizes[0].width
                mCaptureSize = Size(w, h)
                Log.w("123", "w:${sizes[0].width}, h:${sizes[0].height}")
                Log.w("123", "调整后w:${w}, h:${h}")
                setupImageReader()
                mCameraId = cameraId
                break
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
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
            Collections.min(
                sizeList
            ) { lhs, rhs -> java.lang.Long.signum((lhs.width * lhs.height - rhs.width * rhs.height).toLong()) }
        } else sizeMap[0]
    }

    private var flag = 0

    private fun setupImageReader() {
        mImageReader = ImageReader.newInstance(
            mCaptureSize!!.width,
            mCaptureSize!!.height,
            ImageFormat.JPEG,
            1
        )
        mImageReader.setOnImageAvailableListener({ reader ->
            flag = 1
            val image: Image = reader.acquireLatestImage()
            mCameraHandler.post(ImageSaver(image))
            runOnUiThread {
                val buffer: ByteBuffer = image.planes[0].buffer
                buffer.rewind()
                val bytes = ByteArray(buffer.remaining())
                buffer[bytes]
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
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
                        Thread.sleep(100)
                    }
                    flag = 0
                    image.close()
                }
            }
        }, mCameraHandler)
    }

    private inner class ImageSaver(image: Image) : Runnable {

        private val mImage: Image = image

        override fun run() {
            flag++
        }

    }

}

