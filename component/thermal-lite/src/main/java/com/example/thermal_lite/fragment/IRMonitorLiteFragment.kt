package com.example.thermal_lite.fragment

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.energy.ac020library.bean.IrcmdError
import com.energy.commoncomponent.bean.RotateDegree
import com.energy.irutilslibrary.LibIRTempAC020
import com.energy.irutilslibrary.bean.GainStatus
import com.energy.iruvc.sdkisp.LibIRProcess
import com.energy.iruvc.utils.CommonParams
import com.energy.iruvc.utils.Line
import com.energy.iruvc.utils.SynchronizedBitmap
import com.energy.iruvccamera.usb.USBMonitor
import com.example.thermal_lite.R
import com.example.thermal_lite.activity.IRMonitorLiteActivity
import com.example.thermal_lite.camera.CameraPreviewManager
import com.example.thermal_lite.camera.DeviceControlManager
import com.example.thermal_lite.camera.DeviceIrcmdControlManager
import com.example.thermal_lite.camera.OnUSBConnectListener
import com.example.thermal_lite.camera.USBMonitorManager
import com.example.thermal_lite.ui.activity.IrDisplayActivity.HANDLE_INIT_FAIL
import com.example.thermal_lite.ui.activity.IrDisplayActivity.HANDLE_SHOW_FPS
import com.example.thermal_lite.ui.activity.IrDisplayActivity.HANDLE_SHOW_SUN_PROTECT_FLAG
import com.example.thermal_lite.ui.activity.IrDisplayActivity.HANDLE_SHOW_TOAST
import com.example.thermal_lite.ui.activity.IrDisplayActivity.HIDE_LOADING
import com.example.thermal_lite.ui.activity.IrDisplayActivity.PREVIEW_FAIL
import com.example.thermal_lite.ui.activity.IrDisplayActivity.SHOW_LOADING
import com.example.thermal_lite.util.IRTool
import com.infisense.usbir.view.ITsTempListener
import com.infisense.usbir.view.TemperatureView
import com.infisense.usbir.view.TemperatureView.REGION_MODE_LINE
import com.infisense.usbir.view.TemperatureView.REGION_MODE_POINT
import com.infisense.usbir.view.TemperatureView.REGION_MODE_RECTANGLE
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.lib.ui.dialog.PageFragment
import com.topdon.module.thermal.ir.bean.DataBean
import com.topdon.module.thermal.ir.bean.SelectPositionBean
import com.topdon.module.thermal.ir.event.ThermalActionEvent
import com.topdon.module.thermal.ir.repository.ConfigRepository
import kotlinx.android.synthetic.main.fragment_lite_ir_monitor.cameraView
import kotlinx.android.synthetic.main.fragment_lite_ir_monitor.temperatureView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ResourceBundle
import java.util.ResourceBundle.getBundle

class IRMonitorLiteFragment : BaseFragment(), ITsTempListener {

    private var configJob: Job ?= null
    protected var isConfigWait = true
    protected var temperatureBytes = ByteArray(192 * 256 * 2)
    var rotateAngle = 270
    private val imageRes = LibIRProcess.ImageRes_t()
    val dstTempBytes = ByteArray(192*256*2)
    private var mProgressDialog: ProgressDialog? = null
    private var temperaturerun = false

    private var mPreviewWidth = 256
    private var mPreviewHeight = 192
    protected var ctrlBlock: USBMonitor.UsbControlBlock ?= null
    private var mOnUSBConnectListener: OnUSBConnectListener? = null
    private val syncimage = SynchronizedBitmap()
    var frameReady = false
    private var shutterHandler: Handler ?= null
    private var shutterRunnable: Runnable ?= null
    private var shutterCount = 0
    protected var isPause = false
    protected var isPick = false

    companion object{
        fun newInstance(isPick: Boolean): IRMonitorLiteFragment {
            val fragment = IRMonitorLiteFragment()
            val bundle = Bundle()
            bundle.putBoolean("isPick", isPick)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun initContentView(): Int {
        return R.layout.fragment_lite_ir_monitor
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey("isPick") == true){
            isPick = requireArguments().getBoolean("isPick")
        }
    }

    override fun initView() {
        lifecycleScope.launch {
            showLoadingDialog()
            delay(1000)
            imageRes.width = 256.toChar()
            imageRes.height = 192.toChar()
            initPreviewManager()
            initCameraSize()
            initUSBMonitorManager()
            DeviceControlManager.getInstance().init()
            USBMonitorManager.getInstance().registerMonitor()

            configJob = lifecycleScope.launch {
                while (isConfigWait && isActive) {
                    delay(200)
                }
                delay(500)
                if (isPick){
                    CameraPreviewManager.getInstance().setPseudocolorMode( SaveSettingUtil.pseudoColorMode)
                }else{
                    CameraPreviewManager.getInstance().setPseudocolorMode(3)
                }
                CameraPreviewManager.getInstance().setColorList(null,null, false,0f,0f)
                CameraPreviewManager.getInstance().alarmBean = null
                IRTool.setAutoShutter(true)
                IRTool.basicGlobalContrastLevelSet((50).toInt())
                IRTool.basicMirrorAndFlipStatusSet(false)
                IRTool.basicImageDetailEnhanceLevelSet(50)
                CameraPreviewManager.getInstance()?.setLimit(
                    Float.MAX_VALUE, Float.MIN_VALUE,
                    0, 0
                )
                shutterHandler = Handler(Looper.getMainLooper())
                fun takePicture() {
                    shutterCount++
                    try {
                        IRTool.setOneShutter()
                    }catch (e : RuntimeException){
                    }
                }
                shutterRunnable = object : Runnable {
                    override fun run() {
                        if (shutterCount < 4) {
                            shutterHandler?.postDelayed(this, 5000L)
                            takePicture()
                        }
                    }
                }
                shutterHandler?.postDelayed(shutterRunnable!!,300)
                delay(2000)
                withContext(Dispatchers.IO){
                    IRTool.basicGainSet(SaveSettingUtil.temperatureMode)
                }
            }
        }
    }

    suspend fun autoStart() : Boolean{
        return IRTool.autoStart()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun action(event: ThermalActionEvent) {
        temperatureView.isEnabled = true
        Log.w("123", "event:${event.action}")
        when (event.action) {
            2001 -> {
                temperatureView.visibility = View.VISIBLE
                temperatureView.temperatureRegionMode = REGION_MODE_POINT
                readPosition(1)
            }
            2002 -> {
                temperatureView.visibility = View.VISIBLE
                temperatureView.temperatureRegionMode = REGION_MODE_LINE
                readPosition(2)
            }
            2003 -> {
                temperatureView.visibility = View.VISIBLE
                temperatureView.temperatureRegionMode = REGION_MODE_RECTANGLE
                readPosition(3)
            }
        }
    }
    private var showTask: Job? = null

    private fun readPosition(type: Int) {
        if (showTask != null && showTask!!.isActive) {
            showTask!!.cancel()
            showTask = null
        }
        showTask = lifecycleScope.launch {
            while (true) {
                delay(1000)
                updateTemp(type)
            }
        }
    }

    fun stopTask(){
        showTask?.cancel()
    }

    private fun updateTemp(type: Int) {
        var result: SelectPositionBean? = null
        val contentRectF = RectF(0f,0f,192f,256f)
        when (type) {
            1 -> {
                if (temperatureView.point != null &&
                    contentRectF.contains(temperatureView.point.x.toFloat(),
                        temperatureView.point.y.toFloat()
                    )) {
                    result = SelectPositionBean(1, temperatureView.point)
                }
            }
            2 -> {
                if (temperatureView.line != null) {
                    result = SelectPositionBean(
                        2,
                        temperatureView.line.start,
                        temperatureView.line.end
                    )
                }
            }
            3 -> {
                if (temperatureView.rectangle != null &&
                    contentRectF.contains(
                        RectF(
                            temperatureView.rectangle.left.toFloat(),
                            temperatureView.rectangle.top.toFloat(),
                            temperatureView.rectangle.right.toFloat(),
                            temperatureView.rectangle.bottom.toFloat()
                        )
                    )) {
                    result = SelectPositionBean(
                        3,
                        Point(
                            temperatureView.rectangle.left,
                            temperatureView.rectangle.top
                        ),
                        Point(
                            temperatureView.rectangle.right,
                            temperatureView.rectangle.bottom
                        )
                    )
                }
            }
        }
        if (requireActivity() is IRMonitorLiteActivity){
            val activity = requireActivity() as IRMonitorLiteActivity
            activity.select(result)
        }
    }
    override fun initData() {

    }
    val mLiteHandler: Handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == SHOW_LOADING) {
                Log.d(TAG, "SHOW_LOADING")
                showLoadingDialog()
            } else if (msg.what == HIDE_LOADING) {
                Log.d(TAG, "HIDE_LOADING")
                dismissLoadingDialog()
                frameReady = true
                isConfigWait = false
            } else if (msg.what == HANDLE_INIT_FAIL) {
                Log.d(TAG, "HANDLE_INIT_FAIL")
                dismissLoadingDialog()
                Toast.makeText(requireActivity(), "handle init fail !", Toast.LENGTH_LONG).show()
            } else if (msg.what == HANDLE_SHOW_TOAST) {
                val message = msg.obj as String
                Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
            } else if (msg.what == PREVIEW_FAIL) {
                dismissLoadingDialog()
                Toast.makeText(requireActivity(), "preview fail !", Toast.LENGTH_LONG).show()
            } else if (msg.what == HANDLE_SHOW_FPS) {
                val fps = msg.obj as Double
            } else if (msg.what == HANDLE_SHOW_SUN_PROTECT_FLAG) {
                Toast.makeText(requireActivity(), "Sun protected", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initUSBMonitorManager() {
        USBMonitorManager.getInstance().init()
        mOnUSBConnectListener = object : OnUSBConnectListener {
            override fun onAttach(device: UsbDevice?) {
            }

            override fun onGranted(usbDevice: UsbDevice?, granted: Boolean) {
            }

            override fun onDetach(device: UsbDevice?) {
                requireActivity().finish()
            }

            override fun onConnect(
                device: UsbDevice?,
                ctrlBlock: USBMonitor.UsbControlBlock?,
                createNew: Boolean
            ) {
                this@IRMonitorLiteFragment.ctrlBlock = ctrlBlock
                DeviceControlManager.getInstance().handleStartPreview(ctrlBlock)
            }

            override fun onDisconnect(device: UsbDevice?, ctrlBlock: USBMonitor.UsbControlBlock?) {
            }

            override fun onCancel(device: UsbDevice?) {
            }

            override fun onCompleteInit() {
            }
        }
        USBMonitorManager.getInstance()
            .addOnUSBConnectListener(IRMonitorLiteFragment::class.java.name, mOnUSBConnectListener)
    }
    private fun initPreviewManager() {
        config = ConfigRepository.readConfig(false)
        CameraPreviewManager.getInstance().init(cameraView, mLiteHandler)
        CameraPreviewManager.getInstance().imageRotate = RotateDegree.DEGREE_270
        CameraPreviewManager.getInstance().setOnTempDataChangeCallback { data ->
            if (data != null) {
                System.arraycopy(data, 0, temperatureBytes, 0, temperatureBytes.size)
            }
            when (rotateAngle) {
                270 -> {
                    LibIRProcess.rotateLeft90(temperatureBytes, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, dstTempBytes)
                }
                0 -> {
                    LibIRProcess.rotate180(temperatureBytes, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, dstTempBytes)
                }
                90 -> {
                    LibIRProcess.rotateRight90(temperatureBytes, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, dstTempBytes)
                }
                180 -> {
                    System.arraycopy(temperatureBytes, 0, dstTempBytes, 0, dstTempBytes.size)
                }
            }
            temperatureView.setTemperature(dstTempBytes)
        }
        temperatureView.setMonitor(true)
        temperatureView.start()
    }

    private fun initCameraSize() {
        temperatureView.setTextSize(SaveSettingUtil.tempTextSize)
        temperatureView.setSyncimage(syncimage)
        temperatureView.setTemperature(dstTempBytes)
        temperatureView.setUseIRISP(false)
        temperatureView.post {
            lifecycleScope.launch {
                if (!temperaturerun) {
                    temperaturerun = true
                    temperatureView.visibility = View.VISIBLE
                    delay(1000)
                    temperatureView.setImageSize(mPreviewHeight, mPreviewWidth, this@IRMonitorLiteFragment)
                    temperatureView.temperatureRegionMode = TemperatureView.REGION_MODE_CLEAN
                }
            }
        }

    }

    fun restTempView(){
        temperatureView.restView()
        temperatureView.clear()
    }

    fun getTemperatureView() : TemperatureView{
        return temperatureView
    }

    fun addTempLine(selectBean: SelectPositionBean) {
        temperatureView.visibility = View.VISIBLE
        temperatureView.isEnabled = false
        when (selectBean.type) {
            1 -> {
                temperatureView.addScalePoint(selectBean.startPosition)
                temperatureView.temperatureRegionMode = REGION_MODE_POINT
            }
            2 -> {
                temperatureView.addScaleLine(
                    Line(
                        selectBean.startPosition,
                        selectBean.endPosition
                    )
                )
                temperatureView.temperatureRegionMode = REGION_MODE_LINE
            }
            3 -> {
                temperatureView.addScaleRectangle(
                    Rect(
                        selectBean.startPosition!!.x,
                        selectBean.startPosition!!.y,
                        selectBean.endPosition!!.x,
                        selectBean.endPosition!!.y,
                    )
                )
                temperatureView.temperatureRegionMode = REGION_MODE_RECTANGLE
            }
        }
        temperatureView.drawLine()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (isPause){
            DeviceControlManager.getInstance().handleResumeDualPreview()
            isPause = false
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        isPause = true
        DeviceControlManager.getInstance().handlePauseDualPreview()
    }

    fun closeFragment(){
        try {
            DeviceControlManager.getInstance().handlePauseDualPreview()
            DeviceControlManager.getInstance().handleStopPreview()
            USBMonitorManager.getInstance().unregisterMonitor()
            if (mOnUSBConnectListener != null) {
                USBMonitorManager.getInstance()
                    .removeOnUSBConnectListener(IRMonitorLiteFragment::class.java.name)
                mOnUSBConnectListener = null
            }
            USBMonitorManager.getInstance().destroyMonitor()
            DeviceControlManager.getInstance().release()
            CameraPreviewManager.getInstance().releaseSource()
        }catch (e : Exception){
            XLog.e("$TAG:lite销毁异常--${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        temperatureView.stop()
        shutterRunnable?.let {
            shutterHandler?.removeCallbacks(it)
        }
        try {
            if (mOnUSBConnectListener != null) {
                DeviceControlManager.getInstance().handleStopPreview()
                USBMonitorManager.getInstance().unregisterMonitor()
                USBMonitorManager.getInstance()
                    .removeOnUSBConnectListener(IRMonitorLiteFragment::class.java.name)
                mOnUSBConnectListener = null
                USBMonitorManager.getInstance().destroyMonitor()
                DeviceControlManager.getInstance().release()
                CameraPreviewManager.getInstance().releaseSource()
            }
        }catch (e : Exception){
            XLog.e("$TAG:lite销毁异常--${e.message}")
        }
    }

    var config : DataBean?= null
    val basicGainGetValue = IntArray(1)
    var basicGainGetTime = 0L

    override fun tempCorrectByTs(temp: Float?): Float {
        var tempNew = temp
        try {
            if (config == null){
                config = ConfigRepository.readConfig(false)
            }
            if (isConfigWait){
                return temp!!
            }
            val defModel = DataBean()
            if (config!!.radiation == defModel.radiation &&
                defModel.environment == config!!.environment &&
                defModel.distance == config!!.distance){
                return temp!!
            }

            if (System.currentTimeMillis() - basicGainGetTime > 5000L){
                try {
                    val basicGainGet: IrcmdError? = DeviceIrcmdControlManager.getInstance().getIrcmdEngine()
                        ?.basicGainGet(basicGainGetValue)
                }catch (e : Exception){
                    XLog.e("增益获取失败")
                }
                basicGainGetTime = System.currentTimeMillis()
            }
            val params_array = floatArrayOf(
                temp!!, config!!.radiation, config!!.environment,
                config!!.environment, config!!.distance, 0.8f
            )
            if (BaseApplication.instance.tau_data_H == null || BaseApplication.instance.tau_data_L == null) return temp
            tempNew = LibIRTempAC020.temperatureCorrection(
                params_array[0],
                BaseApplication.instance.tau_data_H,
                BaseApplication.instance.tau_data_L,
                params_array[1],
                params_array[2],
                params_array[3],
                params_array[4],
                params_array[5],
                if (basicGainGetValue[0] == 0) GainStatus.LOW_GAIN else GainStatus.HIGH_GAIN
            )
            Log.i(
                TAG,
                "temp correct, oldTemp = " + params_array[0] + " newtemp = " + tempNew +
                        " ems = " + params_array[1] + " ta = " + params_array[2] + " " +
                        "distance = " + params_array[4] + " hum = " + params_array[5] +" basicGain = "+basicGainGetValue[0]
            )
        }catch (e : Exception){
            XLog.e("$TAG--温度修正异常：${e.message}")
        }finally {
            return tempNew ?: 0f
        }
    }

    fun getBitmap() : Bitmap{
        return Bitmap.createScaledBitmap(CameraPreviewManager.getInstance().scaledBitmap(true),
            cameraView!!.width, cameraView!!.height, true)
    }
}