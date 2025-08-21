package com.topdon.module.thermal.ir.activity

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.usb.UsbDevice
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.view.SurfaceView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.energy.iruvc.ircmd.IRCMD
import com.energy.iruvc.sdkisp.LibIRProcess
import com.energy.iruvc.usb.USBMonitor
import com.energy.iruvc.utils.CommonParams
import com.energy.iruvc.utils.DualCameraParams
import com.energy.iruvc.utils.IFrameCallback
import com.energy.iruvc.utils.IIRFrameCallback
import com.energy.iruvc.utils.SynchronizedBitmap
import com.energy.iruvc.uvc.ConnectCallback
import com.energy.iruvc.uvc.UVCCamera
import com.example.suplib.wrapper.SupHelp
import com.infisense.usbdual.Const
import com.infisense.usbdual.camera.DualViewWithExternalCameraCommonApi
import com.infisense.usbdual.camera.IRUVCDual
import com.infisense.usbdual.camera.USBMonitorManager
import com.infisense.usbdual.inf.OnUSBConnectListener
import com.infisense.usbir.utils.PseudocodeUtils
import com.infisense.usbir.view.TemperatureView
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.utils.DualParamsUtil
import com.topdon.module.thermal.ir.utils.IRCmdTool
import com.topdon.module.thermal.ir.utils.IRCmdTool.getSNStr
import kotlinx.android.synthetic.main.activity_thermal_ir_night.cameraView
import kotlinx.android.synthetic.main.activity_thermal_ir_night.title_view
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

/**
 * 双光的初始化
 * 双光的
 */
abstract class BaseIRPlushActivity : IRThermalNightActivity(), OnUSBConnectListener, IIRFrameCallback {

    //热成像设备sn,可作为唯一id，此sn并非艾睿烧录的，是内部烧录的
    private var snStr = ""

    /**
     * 使用 DualUVCCamera 进行画面预览、获取回调数据的关键工具类.
     *
     * 注意：这个命名有问题，虽然叫 View，但却不是 View!
     */
    protected var dualView: DualViewWithExternalCameraCommonApi? = null

    /**
     * ir camera
     * 22576 - 0x5830
     * 22592 - 0x5840
     */
    private var irPid = 0x5830


    private var imageWidth = 0 // 经过旋转后的图像宽度
    private var imageHeight = 0 // 经过旋转后的图像高度
    private var syncimage = SynchronizedBitmap()

    protected var mCurrentFusionType = DualParamsUtil.fusionTypeToParams(SaveSettingUtil.fusionType)

    /**
     * vl camera
     * 12341 - 0x3035  30 fps 640*480
     * 38704 - 0x9730  25 fps 1280*720
     * 8833
     */
    private var vlPid = 12337
    private var vlFps = 30 // 该分辨率支持的帧率

    protected var vlCameraWidth = 1280
    protected var vlCameraHeight = 720
    private var vlData = ByteArray(vlCameraWidth * vlCameraHeight * 3) // 存储可见光数据

    /**
     * dual camera
     */
    private var dualCameraWidth = 480
    private var dualCameraHeight = 640

    // 是否使用IRISP算法集成
    private val isUseIRISP = false

    private var psedocolor: Array<ByteArray> ?= null

    protected var dualDisp = 30

    /**
     * camera 相机相关
     */
    private var vlUVCCamera: IRUVCDual? = null



    /**
     * 子类实现该方法，返回用于渲染画面的 SurfaceView
     */
    abstract fun getSurfaceView(): SurfaceView

    /**
     * 子类实现该方法，返回用于显示温度图层的 TemperatureDualView
     */
    abstract fun getTemperatureDualView(): TemperatureView

    /**
     * 是否是双光设备
     */
    abstract fun isDualIR() : Boolean

    abstract fun setTemperatureViewType()


    open fun setDispViewData(dualDisp : Int){

    }




    override fun initView() {
        super.initView()
        if (isDualIR()){
            // defaultDataFlowMode 是 图像+温度，故而 SDK 返回的传感器原始宽度为 256x384
            // 那么一帧图像、一帧温度的尺寸就是 256x(384/2) = 256x192
            // 由于竖屏显示需要旋转，那么最终出图尺寸就是 192x256
            imageWidth = 192
            imageHeight = 256
            USBMonitorManager.getInstance().init(irPid, isUseIRISP, defaultDataFlowMode)
            USBMonitorManager.getInstance().addOnUSBConnectListener(this)
        }
    }

    override fun onResume() {
        super.onResume()
        dualStart()
    }

    override fun onStop() {
        super.onStop()
        dualStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mIrHandler.removeCallbacksAndMessages(null)
        USBMonitorManager.getInstance().removeOnUSBConnectListener(this)
    }

    private fun dualStart() {
        if (!isDualIR()){
            return
        }
        /**
         * 打开红外模组
         * 需要确认好模组的pid和分辨率
         */
        USBMonitorManager.getInstance().registerUSB()
        //在USBMonitorManager onConnect回调中打开可见光模组
        getTemperatureDualView().setUseIRISP(isUseIRISP)
        if (mCurrentFusionType == DualCameraParams.FusionType.IROnlyNoFusion) {
            getTemperatureDualView().setImageSize(Const.IR_HEIGHT, Const.IR_WIDTH,null)
        } else {
            getTemperatureDualView().setImageSize(dualCameraWidth, dualCameraHeight,null)
        }
        setTemperatureViewType()
        getTemperatureDualView().start()
    }

    private var mIrHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (!isDualIR()){
                return
            }
            if (msg.what == Const.RESTART_USB) {
                restartDualCamera()
            } else if (msg.what == Const.HANDLE_CONNECT) {
                // 避免冲突，需要延时
                /**
                 * 开可见光相机
                 * 需要确认好模组的pid和分辨率
                 */
                lifecycleScope.launch(Dispatchers.Main){
                    startVLCamera(vlPid, vlFps, vlCameraWidth, vlCameraHeight)
                    initDualCamera()
                    // 一体式
                    initDefIntegralArgsDISPValue(DualCameraParams.TypeLoadParameters.ROTATE_270)
                }
            } else if (msg.what == Const.HANDLE_REGISTER) {
                USBMonitorManager.getInstance().registerUSB()
            } else if (msg.what == Const.SHOW_LOADING) {
                showCameraLoading()
            } else if (msg.what == Const.HIDE_LOADING) {
                dismissCameraLoading()
            } else if (msg.what == Const.SHOW_RESTART_MESSAGE) {
                Toast.makeText(
                    this@BaseIRPlushActivity,
                    "please restart app or reinsert device",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun restartDualCamera() {
        if (isrun) {
            USBMonitorManager.getInstance().isReStart = true
            dualStop()
            SystemClock.sleep(200)
            dualStart()
        }
    }

    /**
     * 一体式
     */
    private fun initDefIntegralArgsDISPValue(typeLoadParameters: DualCameraParams.TypeLoadParameters) {
        if (!isDualIR()){
            return
        }
        lifecycleScope.launch{
            val parameters = IRCmdTool.getDualBytes(USBMonitorManager.getInstance().ircmd)
            val data = dualView?.dualUVCCamera?.loadParameters(parameters, typeLoadParameters)
            dualDisp = IRCmdTool.dispNumber
            setDispViewData(dualDisp)
            // 初始化默认值
            dualView?.dualUVCCamera?.setDisp(dualDisp)
            dualView?.startPreview()
        }
    }

    private fun initDualCamera() {
        if (!isDualIR()){
            return
        }
        if (dualView != null) {
            return
        }
        val dualRotate: Int = if (saveSetBean.rotateAngle == 270) 0 else (saveSetBean.rotateAngle + 90)
        dualView = DualViewWithExternalCameraCommonApi(
            getSurfaceView(),
            USBMonitorManager.getInstance().uvcCamera, defaultDataFlowMode,
            imageHeight, imageWidth,
            vlCameraWidth, vlCameraHeight,
            dualCameraWidth, dualCameraHeight,
            isUseIRISP,dualRotate,this
        )
        dualView?.addFrameCallback(getTemperatureDualView())
        //
        getTemperatureDualView().setDualUVCCamera(dualView!!.getDualUVCCamera())
        initPseudoColor()
        initAmplify(true)
        // 这里可以设置初始化融合模式
//        setFusion(mCurrentFusionType)
//        dualView!!.startPreview()
        dualView?.setHandler(mIrHandler)
        isrun = true
    }

    private fun initPseudoColor() {
        val am = assets
        var inputStream: InputStream? = null
        try {
            //加载伪彩，虽然用不上这个伪彩，但是sdk限制必须初始化一个才能正常出图
            psedocolor = Array(11) { ByteArray(0) }
            inputStream = am.open("pseudocolor/White_Hot.bin")
            val length = inputStream.available()
            psedocolor!![0] = ByteArray(length + 1)
            if (inputStream.read(psedocolor!![0]) != length) {

            }
            psedocolor!![0][length] = 0
            dualView!!.getDualUVCCamera().loadPseudocolor(
                CommonParams.PseudoColorUsbDualType.WHITE_HOT_MODE,
                psedocolor!![0]
            )
            // 这里可以设置初始化融合模式
            setFusion(mCurrentFusionType)
            inputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    protected fun setFusion(fusion : DualCameraParams.FusionType) {
        dualView?.setCurrentFusionType(fusion)
        getTemperatureDualView().setCurrentFusionType(fusion)
        if (fusion == DualCameraParams.FusionType.IROnlyNoFusion) {
            getTemperatureDualView().setImageSize(Const.IR_HEIGHT, Const.IR_WIDTH,null)
        } else {
            getTemperatureDualView().setImageSize(dualCameraWidth, dualCameraHeight,null)
        }
    }
    /**
     * 可见光模组
     *
     * @param pid          模组的pid
     * @param cameraWidth  模组的分辨率宽
     * @param cameraHeight 模组的分辨率高
     */
    private fun startVLCamera(pid: Int, fps: Int, cameraWidth: Int, cameraHeight: Int) {
        if (!isDualIR()){
            return
        }
        vlUVCCamera = IRUVCDual(cameraWidth,
            cameraHeight,
            this,
            pid,
            fps,
            object : ConnectCallback {
                override fun onCameraOpened(uvcCamera: UVCCamera) {

                }

                override fun onIRCMDCreate(ircmd: IRCMD) {
                    setUVCCameraICMD(ircmd)
                }
            },
            IFrameCallback { frame ->
                if (dualView != null && dualView?.getDualUVCCamera() != null &&
                    Const.isDeviceConnected
                ) {
                    System.arraycopy(frame, 0, vlData, 0, vlData.size)
                    dualView?.getDualUVCCamera()?.updateFrame(
                        ImageFormat.FLEX_RGB_888, vlData, vlCameraWidth,
                        vlCameraHeight
                    )
                }
            })
        vlUVCCamera?.setHandler(mIrHandler)
        vlUVCCamera?.registerUSB()
        vlUVCCamera?.TAG = "mjpeg"
    }

    private fun setUVCCameraICMD(ircmd: IRCMD) {
        this.ircmd = ircmd
        snStr = getSNStr(ircmd)
        isConfigWait = false
//        getTemperatureDualView().setIrcmd(ircmd)
//        popupCalibration.setIrcmd(ircmd)
//        popupImage.setIrcmd(ircmd)
//        popupOthers.setIrcmd(ircmd)
//        getTemperatureDualView().setIrcmd(ircmd)
//        // 画面旋转设置
//        popupCalibration.setRotate(true)
//        popupImage.setRotate(true)
    }

    private fun dualStop() {
        if (!isDualIR()){
            return
        }
        isrun = false
        syncimage.valid = false
        isConfigWait = true
        getTemperatureDualView().stop()
        USBMonitorManager.getInstance().unregisterUSB()
        ircmd?.onDestroy()
        ircmd = null
        SystemClock.sleep(100)
        if (dualView != null) {
            dualView?.removeFrameCallback(getTemperatureDualView())
            dualView?.dualUVCCamera?.onPausePreview()
            USBMonitorManager.getInstance().stopPreview()
            //
            if (vlUVCCamera != null) {
                vlUVCCamera?.unregisterUSB()
                vlUVCCamera?.stopPreview()
                vlUVCCamera = null
            }
            //
            SystemClock.sleep(100)
            dualView?.stopPreview()
            dualView = null
        }
    }

    override fun onAttach(device: UsbDevice?) {

    }

    override fun onGranted(usbDevice: UsbDevice?, granted: Boolean) {

    }

    override fun onDettach(device: UsbDevice?) {

    }

    override fun onConnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?, createNew: Boolean) {
        mIrHandler.sendEmptyMessage(Const.HANDLE_CONNECT)
    }

    override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {

    }

    override fun onCancel(device: UsbDevice?) {

    }

    override fun onIRCMDInit(ircmd: IRCMD?) {
        setUVCCameraICMD(ircmd!!)
    }
    override fun onCompleteInit() {
        mIrHandler.sendEmptyMessage(Const.HIDE_LOADING)
    }

    override fun onSetPreviewSizeFail() {
        mIrHandler.sendEmptyMessage(Const.SHOW_RESTART_MESSAGE)
    }
    //预处理后红外ARGB数据 192 * 256 * 4
    protected val preIrARGBData = ByteArray(256*192*4)
    protected val preIrData = ByteArray(256*192*2)
    protected val preTempData = ByteArray(256*192*2)

    override fun onIrFrame(irFrame: ByteArray?): ByteArray {
        /**
         * @param irFrame 原始红外YUV422数据 + 温度数据 长度 irWidth * irHeight * 2 + irWidth * irHeight * 2
         * @return
         */
        System.arraycopy(irFrame, 0, preIrData, 0, preIrData.size);
        LibIRProcess.convertYuyvMapToARGBPseudocolor(
            preIrData, (Const.IR_WIDTH * Const.IR_HEIGHT).toLong(),
            PseudocodeUtils.changePseudocodeModeByOld(pseudoColorMode), preIrARGBData
        )
        return preIrARGBData
    }

    override fun switchAmplify() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                try {
                    SupHelp.getInstance().initA4KCPP()
                }catch (e : UnsatisfiedLinkError){
                    SupHelp.getInstance().loadOpenclSuccess = false
                    runOnUiThread {
                        TipDialog.Builder(this@BaseIRPlushActivity)
                            .setMessage(R.string.tips_tisr_fail)
                            .setPositiveListener(R.string.app_got_it) {
                            }
                            .create().show()
                    }
                    XLog.e("超分初始化失败")
                }
            }
            if (!SupHelp.getInstance().loadOpenclSuccess){
                return@launch
            }
            isOpenAmplify = !isOpenAmplify
            dualView?.isOpenAmplify = isOpenAmplify

            title_view.setRight2Drawable(if (isOpenAmplify) R.drawable.svg_tisr_on else R.drawable.svg_tisr_off)
            SaveSettingUtil.isOpenAmplify = isOpenAmplify
            if (isOpenAmplify){
                ToastUtils.showShort(R.string.tips_tisr_on)
            }else{
                ToastUtils.showShort(R.string.tips_tisr_off)
            }
        }
    }

    override fun initAmplify(show: Boolean) {
        lifecycleScope.launch {
            title_view.setRight2Drawable(if (isOpenAmplify) R.drawable.svg_tisr_on else R.drawable.svg_tisr_off)
            withContext(Dispatchers.IO){
                if (isOpenAmplify){
                    SupHelp.getInstance().initA4KCPP()
                }
            }
            dualView?.isOpenAmplify = isOpenAmplify
        }
    }



}