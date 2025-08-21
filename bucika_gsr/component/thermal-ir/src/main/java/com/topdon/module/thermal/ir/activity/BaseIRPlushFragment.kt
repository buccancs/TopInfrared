package com.topdon.module.thermal.ir.activity

import android.graphics.ImageFormat
import android.hardware.usb.UsbDevice
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.SizeUtils
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
import com.infisense.usbdual.Const
import com.infisense.usbdual.camera.DualViewWithExternalCameraCommonApi
import com.infisense.usbdual.camera.IRUVCDual
import com.infisense.usbdual.camera.USBMonitorManager
import com.infisense.usbdual.inf.OnUSBConnectListener
import com.infisense.usbir.extension.setAutoShutter
import com.infisense.usbir.extension.setContrast
import com.infisense.usbir.extension.setMirror
import com.infisense.usbir.extension.setPropDdeLevel
import com.infisense.usbir.utils.FileUtil
import com.infisense.usbir.utils.PseudocodeUtils
import com.infisense.usbir.utils.ScreenUtils
import com.infisense.usbir.view.ITsTempListener
import com.infisense.usbir.view.TemperatureView
import com.topdon.lib.core.bean.CameraItemBean
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.menu.constant.SettingType
import com.topdon.module.thermal.ir.repository.ConfigRepository
import com.topdon.module.thermal.ir.utils.DualParamsUtil
import com.topdon.module.thermal.ir.utils.IRCmdTool
import com.topdon.module.thermal.ir.utils.IRCmdTool.getSNStr
import kotlinx.android.synthetic.main.activity_thermal_ir_night.thermal_recycler_night
import kotlinx.android.synthetic.main.activity_thermal_ir_night.tv_type_ind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream


/**
 * 双光的初始化
 * 双光的
 */
abstract class BaseIRPlushFragment : BaseFragment(), OnUSBConnectListener,ITsTempListener,
    IIRFrameCallback {

    val INIT_ALIGN_DATA = floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f)

    /**
     * 使用 DualUVCCamera 进行画面预览、获取回调数据的关键工具类.
     *
     * 注意：这个命名有问题，虽然叫 View，但却不是 View!
     */
    protected var dualView: DualViewWithExternalCameraCommonApi? = null

    /**
     * 伪彩颜色模式，默认 IRONBOW_MODE(铁红)
     */
    protected var pseudoColorModeDual = CommonParams.PseudoColorUsbDualType.IRONBOW_MODE



    /**
     * 是否已开始可见光及红外的预览.
     * true-已调用完相关预览方法，即将或正在展示预览画面
     * false-尚未执行预览相关的初始化.
     * 使用该变量避免在已初始化过的 dualStart 方法中弹出加载中弹框.
     */
    private var hasStartPreview = false
    protected var ircmd: IRCMD? = null

    //热成像设备sn,可作为唯一id，此sn并非艾睿烧录的，是内部烧录的
    protected var snStr = ""

    /** 默认数据流模式：图像+温度复合数据 */
    protected var defaultDataFlowMode = CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT
    /**
     * ir camera
     * 22576 - 0x5830
     * 22592 - 0x5840
     */
    private var irPid = 0x5830
    private var irFps = 25
    private var irCameraWidth // 传感器的原始宽度
            = 0
    private var irCameraHeight // 传感器的原始高度
            = 0
    private var irTempHeight // 温度数据高度
            = 0
    private var imageWidth // 经过旋转后的图像宽度
            = 0
    private var imageHeight // 经过旋转后的图像高度
            = 0
    protected var temperatureSrc: ByteArray ?= null

    protected var mCurrentFusionType = DualParamsUtil.fusionTypeToParams(SaveSettingUtil.fusionType)
    private var syncimage = SynchronizedBitmap()
    protected var isConfigWait = true
    protected var pseudoColorMode = SaveSettingUtil.pseudoColorMode

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

    protected var isrun = false

    // 是否使用IRISP算法集成
    protected val isUseIRISP = false

    protected var fullScreenlayoutParams: FrameLayout.LayoutParams? = null

    protected var psedocolor: Array<ByteArray> ?= null

    protected var dualRotate = 0;

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
     * 子类实现该方法，在 USBMonitor 的 onConnect 阶段，执行创建 DualView 后的相应处理.
     */
    abstract suspend fun onDualViewCreate(dualView: DualViewWithExternalCameraCommonApi?)


    open fun initdata() {

    }

    /**
     * @param dataFlowMode
     */
    open fun initDataFlowMode(dataFlowMode: CommonParams.DataFlowMode) {
        when (dataFlowMode) {
            CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT -> {
                /**
                 * 图像+温度
                 */
                irCameraWidth = 256 // 传感器的原始宽度
                irCameraHeight = 384 // 传感器的原始高度
                irTempHeight = 192
                imageWidth = irCameraHeight - irTempHeight
                imageHeight = irCameraWidth
                temperatureSrc = ByteArray(imageWidth * imageHeight * 2)
            }
            CommonParams.DataFlowMode.IMAGE_OUTPUT -> {
                /**
                 * 图像
                 */
                irCameraWidth = 256 // 传感器的原始宽度
                irCameraHeight = 192 // 传感器的原始高度
                irTempHeight = 0
                imageWidth = irCameraHeight - irTempHeight
                imageHeight = irCameraWidth
                temperatureSrc = ByteArray(imageWidth * imageHeight * 2)
            }
            CommonParams.DataFlowMode.TEMP_OUTPUT -> {
                /**
                 * 温度
                 */
                irCameraWidth = 256 // 传感器的原始宽度
                irCameraHeight = 192 // 传感器的原始高度
                irTempHeight = 0
                imageWidth = irCameraHeight - irTempHeight
                imageHeight = irCameraWidth
                temperatureSrc = ByteArray(imageWidth * imageHeight * 2)
            }
            else -> {
                irCameraWidth = 256 // 传感器的原始宽度
                irCameraHeight = 192 // 传感器的原始高度
                irTempHeight = 0
                imageWidth = irCameraHeight - irTempHeight
                imageHeight = irCameraWidth
                temperatureSrc = ByteArray(imageWidth * imageHeight * 2)
            }
        }
    }



    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        dualStart()
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * 是否是双光设备
     */
    abstract fun isDualIR() : Boolean

    abstract fun setTemperatureViewType()


    override fun initView() {
        if (isDualIR()){
            getTemperatureDualView().setTextSize(SaveSettingUtil.tempTextSize)
            initDataFlowMode(defaultDataFlowMode)
            initIrDualdata()
        }
    }

    private fun initIrDualdata() {
        // 计算画面的宽高，避免被拉伸变形
        var width = 0
        var height = 0
        val screenWidth: Int = ScreenUtils.getScreenWidth(context)
        val screenHeight: Int = ScreenUtils.getScreenHeight(context) - SizeUtils.dp2px(52f)
        if (screenWidth > screenHeight) {
            width = screenHeight * imageWidth / imageHeight
            height = screenHeight
        } else {
            width = screenWidth
            height = screenWidth * imageHeight / imageWidth
        }
        fullScreenlayoutParams = FrameLayout.LayoutParams(
            width,
            height
        )
        getSurfaceView().layoutParams = fullScreenlayoutParams
        getTemperatureDualView().layoutParams = fullScreenlayoutParams
        USBMonitorManager.getInstance().init(irPid, isUseIRISP, defaultDataFlowMode)
        USBMonitorManager.getInstance().addOnUSBConnectListener(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        mIrHandler.removeCallbacksAndMessages(null)
        USBMonitorManager.getInstance().removeOnUSBConnectListener(this)
    }

    open fun initPseudocolor() {
        val am = requireContext().assets
        var `is`: InputStream? = null
        try {
            //加载伪彩，虽然用不上这个伪彩，但是sdk限制必须初始化一个才能正常出图
            psedocolor = Array(11) { ByteArray(0) }
            `is` = am.open("pseudocolor/White_Hot.bin")
            var lenth = `is`.available()
            psedocolor!![0] = ByteArray(lenth + 1)
            if (`is`.read(psedocolor!![0]) != lenth) {
                Log.d(
                    TAG,
                    "read file fail "
                )
            }
            psedocolor!![0][lenth] = 0
            dualView!!.getDualUVCCamera().loadPseudocolor(
                CommonParams.PseudoColorUsbDualType.WHITE_HOT_MODE,
                psedocolor!![0]
            )
            // 这里可以设置初始化融合模式
            setFusion(mCurrentFusionType)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    open fun setFusion(fusion : DualCameraParams.FusionType) {
        dualView?.setCurrentFusionType(fusion)
        getTemperatureDualView().setCurrentFusionType(fusion)
        if (fusion == DualCameraParams.FusionType.IROnlyNoFusion) {
            getTemperatureDualView().setImageSize(Const.IR_HEIGHT, Const.IR_WIDTH,null)
        } else {
            getTemperatureDualView().setImageSize(dualCameraWidth, dualCameraHeight,null)
        }
    }

    val calibrationDataSize = 192
    val SAVE_DUAL_BIN = "dual_calibration_parameters2.bin"

    /**
     * 一体式
     */
    open fun initDefIntegralArgsDISP_VALUE(typeLoadParameters: DualCameraParams.TypeLoadParameters) {
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
    open fun setDispViewData(dualDisp : Int){

    }
    open fun restartDualCamera() {
        if (isrun) {
            USBMonitorManager.getInstance().isReStart = true
            dualStop()
            SystemClock.sleep(200)
            dualStart()
        }
    }

    override fun onStop() {
        super.onStop()
        dualStop()
    }

    open fun dualStart() {
        if (!isDualIR()){
            return
        }
        Log.d(
            TAG,
            "dualStart"
        )
        /**
         * 打开红外模组
         * 需要确认好模组的pid和分辨率
         */
        USBMonitorManager.getInstance().registerUSB()
        //在USBMonitorManager onConnect回调中打开可见光模组
        //
//        getTemperatureDualView().setTemperatureRegionMode(View.FOCUSABLES_TOUCH_MODE)
        getTemperatureDualView().setUseIRISP(isUseIRISP)
        if (mCurrentFusionType == DualCameraParams.FusionType.IROnlyNoFusion) {
            getTemperatureDualView().setImageSize(Const.IR_HEIGHT, Const.IR_WIDTH,null)
        } else {
            getTemperatureDualView().setImageSize(dualCameraWidth, dualCameraHeight,null)
        }
        setTemperatureViewType()
        getTemperatureDualView().start()
    }
    /**
     *
     */
    var mIrHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            Log.d(
                TAG,
                "USBMonitorManager 收到消息${msg.what}"
            )
            if (!isDualIR()){
                return
            }
            if (msg.what == Const.RESTART_USB) {
                Log.d(
                    TAG,
                    "restartDualCamera"
                )
                restartDualCamera()
            } else if (msg.what == Const.HANDLE_CONNECT) {
                Log.d(
                    TAG,
                    "USBMonitorManager HANDLE_CONNECT"
                )
                // 避免冲突，需要延时
                /**
                 * 开可见光相机
                 * 需要确认好模组的pid和分辨率
                 */
                lifecycleScope.launch(Dispatchers.Main){
                    startVLCamera(vlPid, vlFps, vlCameraWidth, vlCameraHeight)
                    initDualCamera()
                    // 一体式
                    initDefIntegralArgsDISP_VALUE(DualCameraParams.TypeLoadParameters.ROTATE_270)
                }
            } else if (msg.what == Const.HANDLE_REGISTER) {
                USBMonitorManager.getInstance().registerUSB()
            } else if (msg.what == Const.SHOW_LOADING) {
                Log.d(
                    TAG,
                    "SHOW_LOADING"
                )
                showLoadingDialog()
            } else if (msg.what == Const.HIDE_LOADING) {
                Log.d(
                    TAG,
                    "HIDE_LOADING"
                )
                dismissLoadingDialog()
            } else if (msg.what == Const.SHOW_RESTART_MESSAGE) {
                Toast.makeText(
                    context,
                    "please restart app or reinsert device",
                    Toast.LENGTH_SHORT
                ).show()
                activity?.finish()
            }
        }
    }
    open fun initDualCamera() {
        if (!isDualIR()){
            return
        }
        if (dualView != null) {
            return
        }
        Log.d(
            TAG,
            "initDualCamera"
        )
        dualView = DualViewWithExternalCameraCommonApi(
            getSurfaceView(),
            USBMonitorManager.getInstance().uvcCamera, defaultDataFlowMode,
            irCameraWidth, irCameraHeight - irTempHeight,
            vlCameraWidth, vlCameraHeight, dualCameraWidth, dualCameraHeight,
            isUseIRISP,dualRotate,this
        )
        dualView?.addFrameCallback(getTemperatureDualView())
        //
        getTemperatureDualView().setDualUVCCamera(dualView!!.getDualUVCCamera())
        initPseudocolor()
        // 这里可以设置初始化融合模式
//        setFusion(mCurrentFusionType)
//        dualView!!.startPreview()
        dualView?.setHandler(mIrHandler)
        isrun = true
    }
    /**
     * 可见光模组
     *
     * @param pid          模组的pid
     * @param cameraWidth  模组的分辨率宽
     * @param cameraHeight 模组的分辨率高
     */
    open fun startVLCamera(pid: Int, fps: Int, cameraWidth: Int, cameraHeight: Int) {
        if (!isDualIR()){
            return
        }
        Log.i(
            TAG,
            "startVLCamera"
        )
        vlUVCCamera = IRUVCDual(cameraWidth,
            cameraHeight,
            context,
            pid,
            fps,
            object : ConnectCallback {
                override fun onCameraOpened(uvcCamera: UVCCamera) {
                    Log.i(
                        TAG,
                        "ConnectCallback-startVLCamera-onCameraOpened"
                    )
                }

                override fun onIRCMDCreate(ircmd: IRCMD) {
                    setUVCCameraICMD(ircmd)
                }
            },
            IFrameCallback { frame ->
                Log.i(
                    TAG,
                    "startVLCamera-onFrame->frame.length = " + frame.size
                )
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
    open fun setUVCCameraICMD(ircmd: IRCMD) {
        this@BaseIRPlushFragment.ircmd = ircmd
        snStr = getSNStr(ircmd)
        isConfigWait = false
        Log.i(
            TAG,
            "ConnectCallback-startVLCamera-onIRCMDCreate"
        )
//        getTemperatureDualView().setIrcmd(ircmd)
//        popupCalibration.setIrcmd(ircmd)
//        popupImage.setIrcmd(ircmd)
//        popupOthers.setIrcmd(ircmd)
//        getTemperatureDualView().setIrcmd(ircmd)
//        // 画面旋转设置
//        popupCalibration.setRotate(true)
//        popupImage.setRotate(true)
    }

    override fun onStart() {
        super.onStart()
        if (!isrun) {
            isrun = true
            //恢复配置
            configParam()
        }
    }

    private var isFirst = true
    private var configJob: Job? = null
    private val timeMillis = 150L
    //配置
    private fun configParam() {
        configJob = lifecycleScope.launch{
            while (isConfigWait && isActive) {
                delay(200)
            }
            delay(500)
            val config = ConfigRepository.readConfig(false)
            val disChar = (config.distance * 128).toInt() //距离(米)
            val emsChar = (config.radiation * 128).toInt() //发射率
            XLog.w("设置TPD_PROP DISTANCE:${disChar}, EMS:${emsChar}}")
            delay(timeMillis)
            //发射率
            /// Emissivity property. unit:1/128, range:1-128(0.01-1)
            ircmd?.setPropTPDParams(
                CommonParams.PropTPDParams.TPD_PROP_EMS,
                CommonParams.PropTPDParamsValue.NumberType(emsChar.toString())
            )
            delay(timeMillis)
            //距离
            ircmd?.setPropTPDParams(
                CommonParams.PropTPDParams.TPD_PROP_DISTANCE,
                CommonParams.PropTPDParamsValue.NumberType(disChar.toString())
            )
            // 自动快门
            delay(timeMillis)
            XLog.w("设置TPD_PROP DISTANCE:${disChar}, EMS:${emsChar}}")
            if (isFirst && isrun) {
                //恢复镜像
                ircmd?.setMirror(false)
                // 自动快门
                delay(timeMillis)
                withContext(Dispatchers.IO) {
                    // 部分机型在关闭自动快门，初始会花屏
                    ircmd?.setAutoShutter(true)
                    isFirst = false
                }
                //重置锐度（细节）
                ircmd?.setPropDdeLevel(2)
                //重置对比度
                ircmd?.setContrast(128)
            }
            ircmd?.setPropImageParams(
                CommonParams.PropImageParams.IMAGE_PROP_ONOFF_AGC,
                CommonParams.PropImageParamsValue.StatusSwith.ON
            )
            //手动快门
            if (syncimage.type == 1) {
                ircmd?.tc1bShutterManual()
            } else {
                ircmd?.updateOOCOrB(CommonParams.UpdateOOCOrBType.B_UPDATE)
            }
            XLog.w("设置TPD_PROP DISTANCE2:${disChar}, EMS:${emsChar}}")
        }
    }


    open fun dualStop() {
        if (!isDualIR()){
            return
        }
        Log.d(
            TAG,
            "USBMonitorManager dualStop"
        )
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
            Log.d(
                TAG,
                "正常回收完毕 dualStop"
            )
        }
    }

    override fun onAttach(device: UsbDevice?) {
        Log.d(
            TAG,
            "USBMonitorManager onAttach"
        )
    }

    override fun onGranted(usbDevice: UsbDevice?, granted: Boolean) {
        Log.d(
            TAG,
            "USBMonitorManager onGranted"
        )
    }

    override fun onDettach(device: UsbDevice?) {
        Log.d(
            TAG,
            "USBMonitorManager onDettach"
        )
    }

    override fun onConnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?, createNew: Boolean) {
        Log.d(
            TAG,
            "USBMonitorManager onConnect测试"
        )
        mIrHandler.sendEmptyMessage(Const.HANDLE_CONNECT)
    }

    override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
        XLog.d(
            TAG,
            "USBMonitorManager onDisconnect"
        )
    }

    override fun onCancel(device: UsbDevice?) {
        Log.d(
            TAG,
            "USBMonitorManager onCancel"
        )
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


}