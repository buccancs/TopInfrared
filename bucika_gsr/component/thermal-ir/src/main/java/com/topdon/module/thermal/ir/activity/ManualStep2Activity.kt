package com.topdon.module.thermal.ir.activity

import android.app.Activity
import android.app.ProgressDialog
import android.graphics.ImageFormat
import android.hardware.usb.UsbDevice
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.SizeUtils
import com.energy.iruvc.ircmd.IRCMD
import com.energy.iruvc.usb.USBMonitor
import com.energy.iruvc.utils.CommonParams
import com.energy.iruvc.utils.DualCameraParams
import com.infisense.usbdual.Const
import com.infisense.usbdual.camera.DualViewWithManualAlignExternalCamera
import com.infisense.usbdual.camera.USBMonitorDualManager
import com.infisense.usbdual.camera.USBMonitorManager
import com.infisense.usbdual.inf.OnUSBConnectListener
import com.infisense.usbir.utils.HexDump
import com.infisense.usbir.utils.ScreenUtils
import com.infisense.usbir.utils.SharedPreferencesUtil
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.utils.ByteUtils.toLittleBytes
import com.topdon.lms.sdk.weiget.LmsLoadDialog
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.event.ManualFinishBean
import com.topdon.module.thermal.ir.utils.IRCmdTool
import com.topdon.module.thermal.ir.view.MoveImageView
import kotlinx.android.synthetic.main.activity_manual_step2.iv_tips
import kotlinx.android.synthetic.main.activity_manual_step2.ll_seek_bar
import kotlinx.android.synthetic.main.activity_manual_step2.tv_tips
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.io.InputStream

/**
 * Created by fengjibo on 2024/1/10.
 */
class ManualStep2Activity : BaseActivity(), OnUSBConnectListener,
    View.OnClickListener {


    override fun initContentView(): Int {
        return R.layout.activity_manual_step2
    }

    private var snStr = ""
    private var mThisActivity: Activity? = null
    private var mProgressDialog: LmsLoadDialog? = null
    private var mDualView: DualViewWithManualAlignExternalCamera? = null
    private val mDefaultDataFlowMode = CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT
    protected var dualDisp: Int = 0

    /**
     * ir camera
     * 22576 - 0x5830
     * 22592 - 0x5840
     */
    private val mIrPid = 0x5830
    private val mIrFps = 25
    private var mIrCameraWidth = 0 // 传感器的原始宽度
    private var mIrCameraHeight = 0 // 传感器的原始高度
    private var mImageWidth = 0 // 经过旋转后的图像宽度
    private var mImageHeight = 0 // 经过旋转后的图像高度

    /**
     * vl camera
     * 12341 - 0x3035  30 fps 640*480
     * 38704 - 0x9730  25 fps 1280*720
     */
    private val mVlPid = 12337
    private val mVlFps = 30 // 该分辨率支持的帧率
    private val mVlCameraWidth = 1280
    private val mVlCameraHeight = 720

    /**
     * 融合分辨率
     */
    private val mDualWidth = 480
    private val mDualHeight = 640
    private var mPseudoColors: Array<ByteArray?> = arrayOf()
    private var mFullScreenLayoutParams: FrameLayout.LayoutParams? = null
    private var sId : String = ""

    /**
     * 手动配准的初始化参数
     */
    private val INIT_ALIGN_DATA = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    private var alignScaleX = 0f //图和屏幕缩放比
    private var alignScaleY = 0f //图和屏幕缩放比
    private var canOperate = false //是否可以操作
    private val mIrDualHandler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == SHOW_LOADING) {
                Log.d(TAG, "SHOW_LOADING")
                showLoadingDialog()
            } else if (msg.what == HIDE_LOADING) {
                Log.d(TAG, "HIDE_LOADING")
                hideLoadingDialog()
            } else if (msg.what == HANDLE_CONNECT) {
                initDualCamera()
                //加载配准参数
                initDefIntegralArgsDISP_VALUE(DualCameraParams.TypeLoadParameters.ROTATE_270)
            } else if (msg.what == HIDE_LOADING_FINISH) {
                hideLoadingDialog()
                finish()
            }
        }
    }
    var ivTakePhoto: TextView? = null
    var seek_bar: SeekBar? = null
    var moveImageView: MoveImageView? = null
    var dualTextureView: SurfaceView? = null
    /**
     * 上一次执行 move 或 旋转操作的时间戳.
     */
    private var beforeTime = 0L
    public override fun initView() {
        ivTakePhoto = findViewById(R.id.tv_photo_or_confirm)
        seek_bar = findViewById(R.id.seek_bar)
        dualTextureView = findViewById(R.id.dualTextureView)
        moveImageView = findViewById(R.id.moveImageView)
        mThisActivity = this
        ivTakePhoto?.setVisibility(View.VISIBLE)
        ivTakePhoto?.setOnClickListener(View.OnClickListener {
            if (!canOperate){
                //拍照
                takePhoto()
                ivTakePhoto?.setText(R.string.app_ok)
                tv_tips.text = getString(R.string.dual_light_correction_tips_3)
                iv_tips.visibility = View.GONE
                ll_seek_bar.visibility = View.VISIBLE
            }else{
                SharedManager.setManualAngle(snStr,seek_bar!!.progress)
                val byteArray = ByteArray(24)
                mDualView?.dualUVCCamera?.setAlignFinish()
                mDualView?.dualUVCCamera?.getManualRegistration(byteArray)
                SharedManager.setManualData(snStr,byteArray)
                EventBus.getDefault().post(ManualFinishBean())
                finish()
            }
        })
        seek_bar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (canOperate && fromUser) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - beforeTime > OPERATE_INTERVAL) {
                        beforeTime = currentTime
                        mDualView?.dualUVCCamera?.setAlignRotateParameter(((progress - 1000) / 100f).toLittleBytes())
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        ll_seek_bar.visibility = View.GONE
        seek_bar?.max = 2000
        seek_bar?.setEnabled(false)
        moveImageView?.setEnabled(false)
        //初始化相机类
        initDataFlowMode(mDefaultDataFlowMode)
        initData()
        USBMonitorDualManager.getInstance()
            .init(
                mIrPid, mIrFps, mIrCameraWidth, mIrCameraHeight, 1.0f,
                mVlPid, mVlFps, mVlCameraWidth, mVlCameraHeight, 0.6f
            ) { frame ->
                if (mDualView != null && mDualView!!.dualUVCCamera != null) {
                    mDualView!!.dualUVCCamera.updateFrame(
                        ImageFormat.FLEX_RGB_888,
                        frame,
                        mVlCameraWidth,
                        mVlCameraHeight
                    )
                }
            }
        USBMonitorDualManager.getInstance().addOnUSBConnectListener(this)
    }

    /**
     * @param dataFlowMode
     */
    private fun initDataFlowMode(dataFlowMode: CommonParams.DataFlowMode) {
        if (dataFlowMode == CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT) {
            /**
             * 图像+温度
             */
            mIrCameraWidth = Const.SENSOR_WIDTH // 传感器的原始宽度
            mIrCameraHeight = Const.SENSOR_HEIGHT // 传感器的原始高度
            mImageWidth = mIrCameraHeight / 2
            mImageHeight = mIrCameraWidth
        }
    }

    /**
     *
     */
    public override fun initData() {
        // 计算画面的宽高，避免被拉伸变形
//        var width = 0
//        var height = 0
//        val screenWidth = ScreenUtils.getScreenWidth(this)
//        val screenHeight = ScreenUtils.getScreenHeight(this) - SizeUtils.dp2px(52f)
//        Log.d(TAG, "initdata screenWidth : $screenWidth screenHeight: $screenHeight")
//        Log.d(TAG, "initdata imageWidth : $mImageWidth imageHeight: $mImageHeight")
//        if (screenWidth > screenHeight) {
//            width = screenHeight * mImageWidth / mImageHeight
//            height = screenHeight
//        } else {
//            width = screenWidth
//            height = screenWidth * mImageHeight / mImageWidth
//        }
//        mFullScreenLayoutParams = FrameLayout.LayoutParams(width, height)
//        dualTextureView!!.setLayoutParams(mFullScreenLayoutParams)
//        moveImageView!!.setLayoutParams(mFullScreenLayoutParams)
        dualTextureView?.post {
            alignScaleX = dualTextureView!!.measuredWidth.toFloat() / mDualWidth.toFloat()
            alignScaleY = dualTextureView!!.measuredHeight.toFloat() / mDualHeight.toFloat()
        }

    }

    private fun initDualCamera() {
        //初始化双光预览相关的类
        mDualView = DualViewWithManualAlignExternalCamera(
            mImageWidth, mImageHeight,
            mVlCameraHeight, mVlCameraWidth, mDualWidth, mDualHeight,
            dualTextureView, USBMonitorDualManager.getInstance().irUvcCamera,
            mDefaultDataFlowMode
        )

        //初始化伪彩
        initPsedocolor()

        //设置初始化融合模式,一般选择LPYFusion
        mDualView!!.dualUVCCamera.setFusion(DualCameraParams.FusionType.LPYFusion)

        //打开自动快门逻辑
        USBMonitorDualManager.getInstance().ircmd.setPropAutoShutterParameter(
            CommonParams.PropAutoShutterParameter.SHUTTER_PROP_SWITCH,
            CommonParams.PropAutoShutterParameterValue.StatusSwith.ON
        )
        mDualView!!.setHandler(mIrDualHandler)
    }

    /**
     * 加载伪彩，设置镜头方向，伪彩，融合模式等等
     */
    private fun initPsedocolor() {
        val am = assets
        var `is`: InputStream
        try {
            //加载伪彩
            mPseudoColors = arrayOfNulls(11)
            `is` = am.open("pseudocolor/White_Hot.bin")
            var lenth = `is`.available()
            mPseudoColors[0] = ByteArray(lenth + 1)
            if (`is`.read(mPseudoColors[0]) != lenth) {
                Log.d(Companion.TAG, "read file fail ")
            }
            mPseudoColors[0]!![lenth] = 0
            mDualView!!.dualUVCCamera.loadPseudocolor(
                CommonParams.PseudoColorUsbDualType.WHITE_HOT_MODE,
                mPseudoColors[0]
            )
            `is` = am.open("pseudocolor/Black_Hot.bin")
            lenth = `is`.available()
            mPseudoColors[1] = ByteArray(lenth + 1)
            if (`is`.read(mPseudoColors[1]) != lenth) {
                Log.d(Companion.TAG, "read file fail ")
            }
            mPseudoColors[1]!![lenth] = 1
            mDualView!!.dualUVCCamera.loadPseudocolor(
                CommonParams.PseudoColorUsbDualType.BLACK_HOT_MODE,
                mPseudoColors[1]
            )
            `is` = am.open("pseudocolor/new_Rainbow.bin")
            lenth = `is`.available()
            mPseudoColors[2] = ByteArray(lenth + 1)
            if (`is`.read(mPseudoColors[2]) != lenth) {
                Log.d(Companion.TAG, "read file fail ")
            }
            mPseudoColors[2]!![lenth] = 2
            mDualView!!.dualUVCCamera.loadPseudocolor(
                CommonParams.PseudoColorUsbDualType.RAINBOW_MODE,
                mPseudoColors[2]
            )
            `is` = am.open("pseudocolor/Ironbow.bin")
            lenth = `is`.available()
            mPseudoColors[3] = ByteArray(lenth + 1)
            if (`is`.read(mPseudoColors[3]) != lenth) {
                Log.d(Companion.TAG, "read file fail ")
            }
            mPseudoColors[3]!![lenth] = 3
            mDualView!!.dualUVCCamera.loadPseudocolor(
                CommonParams.PseudoColorUsbDualType.IRONBOW_MODE,
                mPseudoColors[3]
            )

            // 这里可以设置初始化伪彩
            mDualView!!.dualUVCCamera.setPseudocolor(CommonParams.PseudoColorUsbDualType.IRONBOW_MODE)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 一体式结构，双光配准的数据，可从手机固定位置读取，如可从NV分区读写
     * 目前使用的是人工配准的方式，提供配准后的数据文件放在asset目录下
     */
    open fun initDefIntegralArgsDISP_VALUE(typeLoadParameters: DualCameraParams.TypeLoadParameters) {
        lifecycleScope.launch{
            val parameters = IRCmdTool.getDualBytes(USBMonitorDualManager.getInstance().ircmd)
            val data = mDualView!!.dualUVCCamera.loadParameters(parameters, typeLoadParameters)
            dualDisp = IRCmdTool.dispNumber
            // 初始化默认值
            mDualView?.dualUVCCamera?.setDisp(dualDisp)
            mDualView?.startPreview()
            Log.e("机芯数据加载成功","初始化完成:")
        }
    }

    fun onViewClicked(view: View?) {}
    override fun onStart() {
        Log.w(Companion.TAG, "onStart")
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (canOperate) {
            dualTextureView!!.post {
                if (mDualView != null) {
                    mDualView!!.onDraw()
                }
            }
            return
        }
        showLoadingDialog()
        dualStart()
    }

    /**
     *
     */
    private fun dualStart() {
        userStop = false
        USBMonitorDualManager.getInstance().registerUSB()
    }

    override fun onAttach(device: UsbDevice) {}
    override fun onGranted(usbDevice: UsbDevice, granted: Boolean) {}
    override fun onDettach(device: UsbDevice) {}
    override fun onConnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean) {
        mIrDualHandler.sendEmptyMessage(HANDLE_CONNECT)
    }

    override fun onDisconnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock) {
        if (!canOperate && !userStop){
            EventBus.getDefault().post(ManualFinishBean())
            finish()
        }
    }
    override fun onCancel(device: UsbDevice) {}
    override fun onIRCMDInit(ircmd: IRCMD) {
        snStr = IRCmdTool.getSNStr(ircmd)
        seek_bar?.progress = SharedManager.getManualAngle(snStr)
    }
    override fun onCompleteInit() {}
    override fun onSetPreviewSizeFail() {}
    private fun showLoadingDialog() {
        setButtonEnable(false)
        if (mProgressDialog == null) {
            mProgressDialog = LmsLoadDialog(this@ManualStep2Activity)
            mProgressDialog!!.show()
        } else {
            if (!mProgressDialog!!.isShowing) {
                mProgressDialog!!.show()
            }
        }
    }

    private fun hideLoadingDialog() {
        setButtonEnable(true)
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
            mProgressDialog = null
        }
    }

    override fun onClick(v: View) {
        onViewClicked(v)
    }


    var userStop = false

    /**
     * 停止预览
     */
    private fun dualStop() {
        userStop = true
        if (mDualView != null) {
            mDualView!!.dualUVCCamera.onPausePreview()
            SystemClock.sleep(100)
            mDualView!!.stopPreview()
            SystemClock.sleep(200)
            USBMonitorDualManager.getInstance().stopIrUVCCamera()
            USBMonitorDualManager.getInstance().stopVlUVCCamera()
            SystemClock.sleep(100)
        }
        mDualView!!.destroyPreview()
        USBMonitorDualManager.getInstance().unregisterUSB()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        if (canOperate) {
            dualStopWithAlign();
            return
        }
        //停止预览
        dualStop()
    }

    override fun onDestroy() {
        Log.w(Companion.TAG, "onDestroy")
        super.onDestroy()
        USBMonitorDualManager.getInstance().removeOnUSBConnectListener(this)
        USBMonitorDualManager.getInstance().onRelease()
    }

    private fun dualStopWithAlign() {
        mDualView!!.dualUVCCamera.setAlignFinish()
        SystemClock.sleep(200)
        mDualView!!.destroyPreview()
        SystemClock.sleep(100)
        USBMonitorDualManager.getInstance().unregisterUSB()
        USBMonitorDualManager.getInstance().stopIrUVCCamera()
    }

    /**
     * 拍照功能
     */
    private fun takePhoto() {
        //拍照
        if (mDualView != null) {
            canOperate = true
            mDualView!!.stopPreview()
            USBMonitorDualManager.getInstance().stopVlUVCCamera()
            mDualView!!.startAlign()
            seek_bar!!.postDelayed({
                seek_bar!!.setEnabled(true)
                moveImageView!!.setEnabled(true)
                initListener()
            }, 500)
        }
    }

    /**
     * 处理移动数据
     */
    private fun handleMove(preX: Float, preY: Float, curX: Float, curY: Float) {
        if (!canOperate) {
            return
        }
        Log.d(Companion.TAG, "prex :$preX prey : $preY curx : $curX cury : $curY")
        if (mDualView != null) {
            updateSaveButton()
            val newSrc = ByteArray(8)
            val xSrc = ByteArray(4)
            HexDump.float2byte((curX - preX) / alignScaleX, xSrc)
            System.arraycopy(xSrc, 0, newSrc, 0, 4)
            val ySrc = ByteArray(4)
            HexDump.float2byte((curY - preY) / alignScaleY, ySrc)
            System.arraycopy(ySrc, 0, newSrc, 4, 4)
            mDualView!!.dualUVCCamera.setAlignTranslateParameter(newSrc)
        }
    }

    /**
     * 处理角度数据
     */
    private fun handleAngle(angle: Float) {
        if (!canOperate) {
            return
        }
        Log.d(Companion.TAG, "angle :$angle")
        if (mDualView != null) {
            val newSrc = ByteArray(4)
            val xSrc = ByteArray(4)
            HexDump.float2byte(angle, xSrc)
            System.arraycopy(xSrc, 0, newSrc, 0, 4)
            mDualView!!.dualUVCCamera.setAlignRotateParameter(newSrc)
        }
    }

    /**
     * 停止校准
     */
    private fun finishAlign(isSavePara: Boolean) {
        if (!canOperate) {
            return
        }
    }

    fun updateSaveButton() {
        if (ivTakePhoto!!.visibility == View.INVISIBLE) {
            ivTakePhoto!!.visibility = View.VISIBLE
            ivTakePhoto!!.setOnClickListener { //保存图片
                val message = Message.obtain()
                message.what = SHOW_LOADING
                message.obj = ""
                mIrDualHandler.sendMessage(message)
                finishSafe(true)
            }
        }
    }

    fun setButtonEnable(isEnable: Boolean) {
        ivTakePhoto!!.setEnabled(isEnable)
    }

    private fun initListener() {
        moveImageView!!.setOnMoveListener { preX, preY, curX, curY ->
            handleMove(
                preX,
                preY,
                curX,
                curY
            )
        }
    }

    private fun finishSafe(isSavePara: Boolean) {
        Thread {
            finishAlign(isSavePara)
            canOperate = false
            mIrDualHandler.sendEmptyMessage(HIDE_LOADING_FINISH)
        }.start()
    }

    companion object {
        private const val TAG = "ManualStep2Activity"
        const val SHOW_LOADING = 1003
        const val HIDE_LOADING = 1004
        const val HIDE_LOADING_FINISH = 1005
        const val HANDLE_CONNECT = 10001
        private const val OPERATE_INTERVAL = 100
        private const val MIN_CLICK_DELAY_TIME = 100
        private var lastClickTime: Long = 0

        //最多70毫秒执行一次move
        fun delayMoveTime(): Boolean {
            var flag = false
            val curClickTime = System.currentTimeMillis()
            if (curClickTime - lastClickTime < MIN_CLICK_DELAY_TIME) {
                flag = false
            } else {
                flag = true
                lastClickTime = System.currentTimeMillis()
            }
            Log.d(TAG, "ACTION_MOVE isFastClick flag : $flag")
            return flag
        }
    }
}
