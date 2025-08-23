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

    private val mIrPid = 0x5830
    private val mIrFps = 25
    private var mIrCameraWidth = 0
    private var mIrCameraHeight = 0
    private var mImageWidth = 0
    private var mImageHeight = 0

    private val mVlPid = 12337
    private val mVlFps = 30
    private val mVlCameraWidth = 1280
    private val mVlCameraHeight = 720

    private val mDualWidth = 480
    private val mDualHeight = 640
    private var mPseudoColors: Array<ByteArray?> = arrayOf()
    private var mFullScreenLayoutParams: FrameLayout.LayoutParams? = null
    private var sId : String = ""

    private val INIT_ALIGN_DATA = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    private var alignScaleX = 0f
    private var alignScaleY = 0f
    private var canOperate = false
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

    private fun initDataFlowMode(dataFlowMode: CommonParams.DataFlowMode) {
        if (dataFlowMode == CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT) {
            
            mIrCameraWidth = Const.SENSOR_WIDTH
            mIrCameraHeight = Const.SENSOR_HEIGHT
            mImageWidth = mIrCameraHeight / 2
            mImageHeight = mIrCameraWidth
        }
    }

    public override fun initData() {
        dualTextureView?.post {
            alignScaleX = dualTextureView!!.measuredWidth.toFloat() / mDualWidth.toFloat()
            alignScaleY = dualTextureView!!.measuredHeight.toFloat() / mDualHeight.toFloat()
        }

    }

    private fun initDualCamera() {
        mDualView = DualViewWithManualAlignExternalCamera(
            mImageWidth, mImageHeight,
            mVlCameraHeight, mVlCameraWidth, mDualWidth, mDualHeight,
            dualTextureView, USBMonitorDualManager.getInstance().irUvcCamera,
            mDefaultDataFlowMode
        )

        initPsedocolor()

        mDualView!!.dualUVCCamera.setFusion(DualCameraParams.FusionType.LPYFusion)

        USBMonitorDualManager.getInstance().ircmd.setPropAutoShutterParameter(
            CommonParams.PropAutoShutterParameter.SHUTTER_PROP_SWITCH,
            CommonParams.PropAutoShutterParameterValue.StatusSwith.ON
        )
        mDualView!!.setHandler(mIrDualHandler)
    }

    private fun initPsedocolor() {
        val am = assets
        var `is`: InputStream
        try {
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

            mDualView!!.dualUVCCamera.setPseudocolor(CommonParams.PseudoColorUsbDualType.IRONBOW_MODE)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    open fun initDefIntegralArgsDISP_VALUE(typeLoadParameters: DualCameraParams.TypeLoadParameters) {
        lifecycleScope.launch{
            val parameters = IRCmdTool.getDualBytes(USBMonitorDualManager.getInstance().ircmd)
            val data = mDualView!!.dualUVCCamera.loadParameters(parameters, typeLoadParameters)
            dualDisp = IRCmdTool.dispNumber
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

    private fun takePhoto() {
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

    private fun finishAlign(isSavePara: Boolean) {
        if (!canOperate) {
            return
        }
    }

    fun updateSaveButton() {
        if (ivTakePhoto!!.visibility == View.INVISIBLE) {
            ivTakePhoto!!.visibility = View.VISIBLE
            ivTakePhoto!!.setOnClickListener {
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
