package com.topdon.module.thermal.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ScreenUtils
import com.guide.zm04c.matrix.GuideInterface
import com.guide.zm04c.matrix.IrSurfaceView
import com.tbruyelle.rxpermissions2.RxPermissions
import com.topdon.lib.core.bean.tools.ScreenBean
import com.topdon.lib.core.config.FileConfig.galleryPath
import com.topdon.lib.core.tools.ToastTools
import com.topdon.lib.core.utils.ByteUtils.getIndex
import com.topdon.lib.core.utils.ScreenShotUtils
import com.topdon.lib.ui.dialog.SeekDialog
import com.topdon.lib.ui.dialog.ThermalInputDialog
import com.topdon.lib.ui.fence.FenceLineView
import com.topdon.lib.ui.fence.FencePointView
import com.topdon.lib.ui.fence.FenceView
import com.topdon.module.thermal.R
import com.topdon.module.thermal.base.BaseThermalFragment
import com.topdon.module.thermal.fragment.event.ThermalActionEvent
import com.topdon.module.thermal.tools.Fence
import com.topdon.module.thermal.tools.ThermalTool
import com.topdon.module.thermal.tools.medie.IYapVideoProvider
import com.topdon.module.thermal.tools.medie.YapVideoEncoder
import com.topdon.module.thermal.utils.ArrayUtils
import com.topdon.module.thermal.viewmodel.ThermalViewModel
import kotlinx.android.synthetic.main.fragment_thermal.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.math.BigDecimal

class ThermalFragment : BaseThermalFragment(), IYapVideoProvider<Bitmap> {

    private val viewModel: ThermalViewModel by viewModels()

    protected var mIrSurfaceViewLayout: FrameLayout? = null
    protected var mIrSurfaceView: IrSurfaceView? = null

    private val msgLiveData by lazy { MutableLiveData<Int>() }

    override fun initContentView() = R.layout.fragment_thermal

    private fun setViewPosition(imageView: ImageView, index: Int) {
        if (rawWidth == 0 || rawHeight == 0) {
            return
        }
        val vg = imageView.parent as ViewGroup
        val pw = vg.width
        val ph = vg.height
        val y = index / rawWidth
        val x = index - y * rawWidth
        val x1 = x * pw / rawWidth
        val y1 = y * ph / rawHeight
        val maxX = x1 - imageView.width / 2
        val maxY = y1 - imageView.height / 2
        imageView.x = maxX.toFloat()
        imageView.y = maxY.toFloat()
    }

    private var mGuideInterface: GuideInterface? = null

    override fun initView() {
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        rotateType = 3
        mCenterTextView = temp_display
        mMaxTextView = max_temp_display
        mMinTextView = min_temp_display
        maxImg = max_img
        minImg = min_img
        mDisplayFrameLayout = temp_display_layout
        mFenceLayout = fence_lay
        mCameraLayout = temp_camera_layout
        mDisplayFrameLayout!!.visibility = View.GONE
        mFenceLayout!!.visibility = View.GONE
        mIrSurfaceViewLayout = final_ir_layout
        mIrSurfaceView = IrSurfaceView(requireContext())
        val ifrSurfaceViewLayoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER
        )
        mIrSurfaceView!!.layoutParams = ifrSurfaceViewLayoutParams
        mIrSurfaceView!!.setMatrix(ThermalTool.getRotate(rotateType), 256f, 192f)
        mIrSurfaceViewLayout!!.addView(mIrSurfaceView)
        val screenWidth = ScreenUtils.getScreenWidth()
        val screenHeight = screenWidth * 192 / 256
        width = screenWidth
        height = screenHeight
        highCrossWidth = resources.getDimension(R.dimen.high_cross_width).toInt()
        highCrossHeight = resources.getDimension(R.dimen.high_cross_height).toInt()
        mIrSurfaceViewLayout!!.viewTreeObserver.addOnGlobalLayoutListener {
            irSurfaceViewLayoutParams =
                mIrSurfaceViewLayout!!.layoutParams as ConstraintLayout.LayoutParams?
            displayViewLayoutParams = mDisplayFrameLayout!!.layoutParams as FrameLayout.LayoutParams
            fenceLayoutParams = mFenceLayout!!.layoutParams as FrameLayout.LayoutParams
            cameraLayoutParams = mCameraLayout!!.layoutParams as FrameLayout.LayoutParams
            when (rotateType) {
                1, 3 -> {
                    irSurfaceViewWidth = height
                    irSurfaceViewHeight = width
                    if (irSurfaceViewWidth < width) {
                        irSurfaceViewWidth = width
                        irSurfaceViewHeight = screenWidth * 256 / 192
                    }
                }
                0, 2 -> {
                    irSurfaceViewWidth = width
                    irSurfaceViewHeight = height
                }
            }
            irSurfaceViewLayoutParams!!.width = irSurfaceViewWidth
            irSurfaceViewLayoutParams!!.height = irSurfaceViewHeight
            mIrSurfaceViewLayout!!.layoutParams = irSurfaceViewLayoutParams

            displayViewLayoutParams!!.width = irSurfaceViewWidth
            displayViewLayoutParams!!.height = irSurfaceViewHeight
            mDisplayFrameLayout!!.layoutParams = displayViewLayoutParams

            fenceLayoutParams!!.width = irSurfaceViewWidth
            fenceLayoutParams!!.height = irSurfaceViewHeight
            mFenceLayout!!.layoutParams = fenceLayoutParams

            cameraLayoutParams!!.width = irSurfaceViewWidth
            cameraLayoutParams!!.height = irSurfaceViewHeight
            mFenceLayout!!.layoutParams = cameraLayoutParams

        }
        initFence()
        onIrVideoStart()
        mIrSurfaceView!!.post {
            Log.w("123", "w:${mIrSurfaceView!!.width}, h:${mIrSurfaceView!!.height}")
        }

        msgLiveData.observe(this) { msg ->
            if (msg == 0) {
                when (selectType) {
                    0 -> {
                        mCenterTextView!!.visibility = View.VISIBLE
                        mMaxTextView!!.visibility = View.VISIBLE
                        mMinTextView!!.visibility = View.VISIBLE
                        mCenterTextView!!.text = "中心温 $mCenter"
                        mMaxTextView!!.text = "最高温 $mMaxTemp"
                        mMinTextView!!.text = "最低温 $mMinTemp"
                        maxImg!!.visibility = View.GONE
                        minImg!!.visibility = View.GONE
                    }
                    1 -> {
                        mCenterTextView!!.visibility = View.VISIBLE
                        mMaxTextView!!.visibility = View.GONE
                        mMinTextView!!.visibility = View.GONE
                        mCenterTextView!!.text = "温度 $mMaxTemp"
                        maxImg!!.visibility = View.GONE
                        minImg!!.visibility = View.GONE
                    }
                    else -> {
                        mCenterTextView!!.visibility = View.VISIBLE
                        mMaxTextView!!.visibility = View.VISIBLE
                        mMinTextView!!.visibility = View.VISIBLE
                        mCenterTextView!!.text = "中心温 $mCenter"
                        mMaxTextView!!.text = "最高温 $mMaxTemp"
                        mMinTextView!!.text = "最低温 $mMinTemp"
                        maxImg!!.visibility = View.VISIBLE
                        minImg!!.visibility = View.VISIBLE
                        maxImg?.let { setViewPosition(it, maxIndex) }
                        minImg?.let { setViewPosition(it, minIndex) }
                    }
                }

            }
        }
        onTempBtnClick()
    }

    override fun initData() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        onIrVideoStop()
    }

    fun onIrVideoStart() {
        mIsIrVideoStart = if (mIsIrVideoStart) {
            ToastTools.showShort("视频流已开启")
            return
        } else {
            true
        }
        mGuideInterface = GuideInterface()
        val ret = mGuideInterface!!.init(requireContext(), object : GuideInterface.IrDataCallback {
            override fun processIrData(yuv: ByteArray, temp: FloatArray) {
                if (mIrBitmap == null) {
                    mIrBitmap = Bitmap.createBitmap(256, 192, Bitmap.Config.ARGB_8888)
                }
                if (upValue > downValue) {
                    viewModel.yuvArea(yuv, temp, upValue, downValue)
                }
                mGuideInterface!!.yuv2Bitmap(mIrBitmap, yuv)
                try {
                    mIrSurfaceView!!.doDraw(mIrBitmap, mGuideInterface!!.getImageStatus())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (rotateType == 1 || rotateType == 3) {
                    rawWidth = SRC_WIDTH
                    rawHeight = SRC_HEIGHT
                } else {
                    rawWidth = SRC_HEIGHT
                    rawHeight = SRC_WIDTH
                }
                val centerIndex = rawWidth * (rawHeight / 2) + rawWidth / 2
                try {
                    val maxTempIndex = ArrayUtils.getMaxIndex(temp, rotateType, selectIndex)
                    val minTempIndex = ArrayUtils.getMinIndex(temp, rotateType, selectIndex)
                    maxIndex = maxTempIndex
                    minIndex = minTempIndex
                    val rotateData = ArrayUtils.matrixRotate(srcData = temp, rotateType)
                    val bigDecimal = BigDecimal.valueOf(rotateData[centerIndex].toDouble())
                    val maxBigDecimal = BigDecimal.valueOf(rotateData[maxTempIndex].toDouble())
                    val minBigDecimal = BigDecimal.valueOf(rotateData[minTempIndex].toDouble())
                    mCenter = bigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
                    mMaxTemp = maxBigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
                    mMinTemp = minBigDecimal.setScale(1, BigDecimal.ROUND_HALF_UP).toFloat()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "提取温度异常:${e.message}")
                }
            }

        })

        if (ret == 5) {
            Log.w("123", "视频流开启完成")
        } else {
            ToastTools.showShort("视频流开启失败")
        }
    }

    private fun rotateBitmap(origin: Bitmap, rotate: Float): Bitmap? {
        try {
            if (origin == null) {
                return null
            }
            val width = origin.width
            val height = origin.height
            val matrix = Matrix()
            matrix.setRotate(rotate)
            val newBitmap = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
            if (newBitmap.equals(origin)) {
                return newBitmap
            }
            origin.recycle()
            return newBitmap
        } catch (e: Exception) {
            Log.e("123", "error:${e.message}")
            return origin
        }
    }

    fun onIrVideoStop() {
        mIsIrVideoStart = if (!mIsIrVideoStart) {
            ToastTools.showShort("视频流已停止")
            return
        } else {
            false
        }
        mGuideInterface!!.exit()
        mGuideInterface = null
        ToastTools.showShort("视频流停止完成")
    }

    fun onLowRangeBtnClick(view: View?) {
        if (mGuideInterface == null) {
            ToastTools.showShort("请先开启视频流")
            return
        }
        mGuideInterface!!.setRange(1)
        ToastTools.showShort("切换到常温档成功")
    }

    fun onHighRangeBtnClick(view: View?) {
        if (mGuideInterface == null) {
            ToastTools.showShort("请先开启视频流")
            return
        }
        mGuideInterface!!.setRange(2)
        ToastTools.showShort("切换到高温档成功")
    }

    fun onTempBtnClick() {
        if (mGuideInterface == null) {
            ToastTools.showShort("请先开启视频流")
            return
        }
        isDispLayTemp = !isDispLayTemp
        if (isDispLayTemp) {
            mDisplayFrameLayout!!.visibility = View.VISIBLE
            timerJob = lifecycleScope.launch {
                repeat(Int.MAX_VALUE) {
                    msgLiveData.postValue(0)
                    delay(1000)
                }
            }
        } else {
            mDisplayFrameLayout!!.visibility = View.GONE
            if (timerJob != null && timerJob!!.isActive) {
                timerJob!!.cancel()
                timerJob = null
            }
        }
    }

    private var upValue = 0f
    private var downValue = 0f

    private fun addLimit() {
        ThermalInputDialog.Builder(requireContext())
            .setMessage("请设置温度限值")
            .setPositiveListener(R.string.app_confirm) { up, down, _, _ ->
                ToastTools.showShort("设置上限:$up, 下限:$down")
                upValue = up
                downValue = down
            }
            .setCancelListener(R.string.app_cancel)
            .create().show()
    }

    fun onExpertModeClick(view: View?) {
        System.arraycopy(EXPERT_HITS, 1, EXPERT_HITS, 0, EXPERT_HITS.size - 1)
        EXPERT_HITS[EXPERT_HITS.size - 1] = System.currentTimeMillis()
        if (EXPERT_HITS[0] >= System.currentTimeMillis() - EXPERT_MODE_HIT_DURATION) {
            if (mExpertLayout!!.visibility == View.GONE) {
                mExpertLayout!!.visibility = View.VISIBLE
            } else {
                mExpertLayout!!.visibility = View.GONE
            }
            EXPERT_HITS = LongArray(EXPERT_MODE_HIT_COUNT)
        }
    }

    fun onNucShutterClick(view: View?) {
        if (mGuideInterface == null) {
            ToastTools.showShort("请先开启视频流")
            return
        }
        mGuideInterface!!.nuc()
    }

    fun onLut(view: View) {
        mIrSurfaceView!!.setOpenLut()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun action(event: ThermalActionEvent) {
        Log.w("123", "event:${event.action}")
        when (event.action) {
            1001 -> {
                ToastTools.showShort("拍照")
                picture()
            }
            1002 -> {
                ToastTools.showShort("录制")
                video()
            }
            2001 -> {
                clearFenceUI()
                addPoint()
            }
            2002 -> {
                clearFenceUI()
                addLine()
            }
            2003 -> {
                clearFenceUI()
                addFence()
            }
            2004 -> {
                addLimit()
            }
            2006 -> {
                clearFence()
            }
            in 3000..3010 -> {
                setColor(event.action)
            }
            4001 -> {
                rotate()
                clearFence()
            }
            4002 -> {
                enhance()
            }
            4003 -> {
                camera()
            }
            in 5000..5010 -> {
                ToastTools.showShort("全屏")
            }
        }
    }

    private fun clearFence() {
        clearFenceUI()
        upValue = 0f
        downValue = 0f
        selectType = 0
    }

    private fun clearFenceUI() {
        mFenceLayout!!.visibility = View.GONE
        fenceFlag = 0x000
        selectIndex.clear()
        fence_view.clear()
        fence_line_view.clear()
        fence_point_view.clear()
    }

    private fun setColor(action: Int) {
        var type: Int = action % 3000 - 1
        if (type < 0 || type > 10) {
            type = 0
        }
        updatePalette(type)
    }

    private fun updatePalette(index: Int) {
        if (mGuideInterface == null) {
            ToastTools.showShort("请先开启视频流")
            return
        }
        mGuideInterface!!.changePalette(index)
    }

    var fenceFlag = 0x000

    private fun addPoint() {
        showFence(1)
    }

    private fun addLine() {
        showFence(2)
    }

    private fun addFence() {
        showFence(3)
    }

    private fun showFence(index: Int) {
        if (fenceFlag.getIndex(index) == 0) {
            fenceFlag = 1.shl(4 * (index - 1))
            mFenceLayout!!.visibility = View.VISIBLE
            fence_point_view.visibility = if (fenceFlag.getIndex(1) > 0) View.VISIBLE else View.GONE
            fence_line_view.visibility = if (fenceFlag.getIndex(2) > 0) View.VISIBLE else View.GONE
            fence_view.visibility = if (fenceFlag.getIndex(3) > 0) View.VISIBLE else View.GONE
        } else {
            fenceFlag = 0x000
            mFenceLayout!!.visibility = View.GONE
        }
    }

    private var selectType = 0
    private var selectIndex: ArrayList<Int> = arrayListOf()

    private fun initFence() {
        fence_point_view.listener = object : FencePointView.CallBack {
            override fun callback(startPoint: IntArray, srcRect: IntArray) {
                selectType = 1
                selectIndex =
                    Fence(srcRect = srcRect, rotateType = rotateType).getPointIndex(startPoint)
            }

        }
        fence_line_view.listener = object : FenceLineView.CallBack {
            override fun callback(startPoint: IntArray, endPoint: IntArray, srcRect: IntArray) {
                selectType = 2
                selectIndex = Fence(srcRect = srcRect, rotateType = rotateType)
                    .getLineIndex(startPoint, endPoint)
            }
        }
        fence_view.listener = object : FenceView.CallBack {
            override fun callback(startPoint: IntArray, endPoint: IntArray, srcRect: IntArray) {
                selectType = 3
                selectIndex = Fence(srcRect = srcRect, rotateType = rotateType)
                    .getAreaIndex(startPoint, endPoint)
            }

        }
    }

    private fun picture() {
        ScreenShotUtils.shotScreenBitmap(requireContext(), mIrBitmap, 1, ScreenBean())
    }

    var isVideoRunning = false

    private fun video() {
        if (isVideoRunning) {
            Log.w("123", "正在录制")
            return
        }
        val latestResultPath = "${galleryPath}YapBitmapToMp4_${System.currentTimeMillis()}.mp4"
        Log.w("123", "latestResultPath:$latestResultPath")
        YapVideoEncoder(this, File(latestResultPath)).start()
    }

    private fun rotate() {
        rotateType = if (rotateType >= 3) 0 else rotateType + 1
        mIrSurfaceView!!.setMatrix(ThermalTool.getRotate(rotateType), 256f, 192f)
        ToastTools.showShort("旋转:${ThermalTool.getRotate(rotateType)}度")
    }

    private fun enhance() {
        mIrSurfaceView!!.setOpenLut()
        val saturation = mIrSurfaceView?.getSaturationValue() ?: 0
        SeekDialog.Builder(requireContext())
            .setMessage(R.string.thermal_enhance)
            .setSaturation(saturation)
            .setPositiveListener(R.string.app_confirm) {
                mIrSurfaceView?.setSaturationValue(it)
            }
            .setListener {
            }.create().show()
    }

    var isRunCamera = false

    private fun checkCameraPermission() {
        if (!XXPermissions.isGranted(this,Manifest.permission.CAMERA)) {
            if (BaseApplication.instance.isDomestic()) {
                TipDialog.Builder(this)
                    .setMessage(getString(R.string.permission_request_camera))
                    .setCancelListener(R.string.app_cancel)
                    .setPositiveListener(R.string.app_confirm) {
                        camera()
                    }
                    .create().show()
            } else {
                camera()
            }
        } else {
            camera()
        }
    }

    @SuppressLint("CheckResult")
    private fun camera() {
        RxPermissions(this).request(Manifest.permission.CAMERA)
            .subscribe {
                if (isRunCamera) {
                    temp_camera_layout.visibility = View.GONE
                    isRunCamera = false
                } else {
                    temp_camera_layout.visibility = View.VISIBLE
                    temp_camera_view.post {
                        temp_camera_view.openCamera()
                        isRunCamera = true
                    }
                }
            }
    }

    override fun size(): Int = 5 * 60

    override fun next(): Bitmap {
        return if (mIrBitmap == null) {
            Bitmap.createBitmap(256, 192, Bitmap.Config.ARGB_8888)
        } else {
            mIrBitmap!!
        }
    }

    override fun progress(progress: Float) {
        Log.w("123", "progress:$progress")
        isVideoRunning = progress > 0 || progress < 100
    }

}