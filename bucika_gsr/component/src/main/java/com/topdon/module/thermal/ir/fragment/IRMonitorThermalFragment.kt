package com.topdon.module.thermal.ir.fragment

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.yt.jni.Usbcontorl
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.energy.iruvc.ircmd.IRCMD
import com.energy.iruvc.utils.CommonParams
import com.energy.iruvc.utils.CommonUtils
import com.energy.iruvc.utils.SynchronizedBitmap
import com.energy.iruvc.uvc.ConnectCallback
import com.energy.iruvc.uvc.UVCCamera
import com.infisense.usbir.camera.IRUVCTC
import com.infisense.usbir.config.MsgCode
import com.infisense.usbir.event.IRMsgEvent
import com.infisense.usbir.event.PreviewComplete
import com.infisense.usbir.thread.ImageThreadTC
import com.infisense.usbir.utils.USBMonitorCallback
import com.infisense.usbir.view.ITsTempListener
import com.infisense.usbir.view.TemperatureView.*
import com.topdon.lib.core.bean.event.device.DeviceCameraEvent
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.config.DeviceConfig
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.activity.IRMonitorActivity
import com.topdon.module.thermal.ir.bean.SelectPositionBean
import com.topdon.module.thermal.ir.event.ThermalActionEvent
import com.topdon.module.thermal.ir.repository.ConfigRepository
import kotlinx.android.synthetic.main.activity_thermal_ir_night.cameraView
import kotlinx.android.synthetic.main.fragment_ir_monitor_thermal.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 热成像选取点
 */
class IRMonitorThermalFragment : BaseFragment(),ITsTempListener {

    /** 默认数据流模式：图像+温度复合数据 */
    protected var defaultDataFlowMode = CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT

    private var ircmd: IRCMD? = null
    private var gainStatus = CommonParams.GainStatus.HIGH_GAIN

    override fun initContentView() = R.layout.fragment_ir_monitor_thermal

    private var rotateAngle = 270 //校对默认角度270
    private var ts_data_H: ByteArray? = null
    private var ts_data_L: ByteArray? = null
    private var isPick = false

    companion object{
        fun newInstance(isPick: Boolean): IRMonitorThermalFragment {
            val fragment = IRMonitorThermalFragment()
            val bundle = Bundle()
            bundle.putBoolean("isPick", isPick)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun initView() {
        ts_data_H = CommonUtils.getTauData(context, "ts/TS001_H.bin")
        ts_data_L = CommonUtils.getTauData(context, "ts/TS001_L.bin")
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initDataIR()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments?.containsKey("isPick") == true){
            isPick = requireArguments().getBoolean("isPick")
        }
    }

    override fun initData() {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun action(event: ThermalActionEvent) {
        temperatureView.isEnabled = true
        Log.w("123", "event:${event.action}")
        when (event.action) {
            2001 -> {
                //点
                temperatureView.visibility = View.VISIBLE
                temperatureView.temperatureRegionMode = REGION_MODE_POINT
                readPosition(1)
            }
            2002 -> {
                //线
                temperatureView.visibility = View.VISIBLE
                temperatureView.temperatureRegionMode = REGION_MODE_LINE
                readPosition(2)
            }
            2003 -> {
                //面
                temperatureView.visibility = View.VISIBLE
                temperatureView.temperatureRegionMode = REGION_MODE_RECTANGLE
                readPosition(3)
            }
        }
    }

    private var imageThread: ImageThreadTC? = null
    private var bitmap: Bitmap? = null
    private var iruvc: IRUVCTC? = null
    private val cameraWidth = 256
    private val cameraHeight = 384
    private val tempHeight = 192
    private var imageWidth = cameraWidth
    private var imageHeight = cameraHeight - tempHeight
    private val image = ByteArray(imageWidth * imageHeight * 2)
    private val temperature = ByteArray(imageWidth * imageHeight * 2)
    private val syncimage = SynchronizedBitmap()
    private var isrun = false
    private var pseudocolorMode = 3
    private var temperaturerun = false
    private var isTS001 = false

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun irEvent(event: IRMsgEvent) {
        if (event.code == MsgCode.RESTART_USB) {
            restartusbcamera()
        }
    }

    /**
     * 初始数据
     */
    private fun initDataIR() {
        imageWidth = cameraHeight - tempHeight
        imageHeight = cameraWidth
        temperatureView.setTextSize(SaveSettingUtil.tempTextSize)
        if (ScreenUtil.isPortrait(requireContext())) {
            bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
            temperatureView.setImageSize(imageWidth, imageHeight,this@IRMonitorThermalFragment)
            rotateAngle = DeviceConfig.S_ROTATE_ANGLE
        } else {
            bitmap = Bitmap.createBitmap(imageHeight, imageWidth, Bitmap.Config.ARGB_8888)
            temperatureView.setImageSize(imageHeight, imageWidth,this@IRMonitorThermalFragment)
            rotateAngle = DeviceConfig.ROTATE_ANGLE
        }
        cameraView!!.setSyncimage(syncimage)
        cameraView!!.bitmap = bitmap
        temperatureView.setSyncimage(syncimage)
        temperatureView.setTemperature(temperature)
        temperatureView.isEnabled = false
        setViewLay()
        // 某些特定客户的特殊设备需要使用该命令关闭sensor
        if (Usbcontorl.isload) {
            Usbcontorl.usb3803_mode_setting(1) //打开5V
            Log.w("123", "打开5V")
        }
        //初始全局测温
        temperatureView.post {
            if (!temperaturerun) {
                temperaturerun = true
                //需等待渲染完成再显示
                temperatureView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 图像信号处理
     */
    private fun startISP() {

        try {
            imageThread = ImageThreadTC(context, imageWidth, imageHeight)
            imageThread!!.setDataFlowMode(defaultDataFlowMode)
            imageThread!!.setSyncImage(syncimage)
            imageThread!!.setImageSrc(image)
            imageThread!!.setTemperatureSrc(temperature)
            imageThread!!.setBitmap(bitmap)
            imageThread?.setRotate(rotateAngle)
            imageThread!!.setRotate(true)
            imageThread!!.start()
        }catch (e : Exception){
            Log.e("图像线程重复启动",e.message.toString())
        }
    }

    /**
     *
     */
    private fun startUSB(isRestart : Boolean) {
        iruvc = IRUVCTC(cameraWidth, cameraHeight, context, syncimage,
            defaultDataFlowMode, object : ConnectCallback {
                override fun onCameraOpened(uvcCamera: UVCCamera) {

                }

                override fun onIRCMDCreate(ircmd: IRCMD) {
                    Log.i(
                        TAG,
                        "ConnectCallback->onIRCMDCreate"
                    )
                    this@IRMonitorThermalFragment.ircmd = ircmd
                    //重置镜像为非镜像
                    ircmd.setPropImageParams(
                        CommonParams.PropImageParams.IMAGE_PROP_SEL_MIRROR_FLIP,

                        CommonParams.PropImageParamsValue.MirrorFlipType.NO_MIRROR_FLIP
                    )
                    // 需要等IRCMD初始化完成之后才可以调用
//                    ircmd?.setPseudoColor(CommonParams.PreviewPathChannel.PREVIEW_PATH0, CommonParams.PseudoColorType.PSEUDO_1)
                    val fwBuildVersionInfoBytes = ByteArray(50)
                    ircmd?.getDeviceInfo(
                        CommonParams.DeviceInfoType.DEV_INFO_FW_BUILD_VERSION_INFO,
                        fwBuildVersionInfoBytes
                    ) //ok
                    val value = IntArray(1)
                    val arm = String(fwBuildVersionInfoBytes.copyOfRange(0, 8))
                    isTS001 = arm.contains("Mini256", true)
                    ircmd!!.getPropTPDParams(CommonParams.PropTPDParams.TPD_PROP_GAIN_SEL, value)
                    Log.d(TAG, "TPD_PROP_GAIN_SEL=" + value[0])
                    gainStatus = if (value[0] == 1) {
                        // 当前机芯为高增益
                        CommonParams.GainStatus.HIGH_GAIN
                        // 等效大气透过率表
                    } else {
                        // 当前机芯为低增益
                        CommonParams.GainStatus.LOW_GAIN
                    }
                }
            }, object : USBMonitorCallback {
                override fun onAttach() {}
                override fun onGranted() {}
                override fun onConnect() {}
                override fun onDisconnect() {}
                override fun onDettach() {
                    activity?.finish()
                }

                override fun onCancel() {
                    activity?.finish()
                }
            })
        iruvc!!.isRestart = isRestart
        iruvc!!.setImageSrc(image)
        iruvc!!.setTemperatureSrc(temperature)
        iruvc!!.setRotate(rotateAngle)
        iruvc!!.registerUSB()
    }

    /**
     *
     */
    private fun restartusbcamera() {
        if (iruvc != null) {
            iruvc!!.stopPreview()
            iruvc!!.unregisterUSB()
        }
        startUSB(true)
    }

    override fun onStart() {
        super.onStart()
        Log.w(TAG, "onStart")
        if (!isrun) {
            //初始配置,伪彩铁红
            if (isPick){
                pseudocolorMode = SaveSettingUtil.pseudoColorMode
            }else{
                pseudocolorMode = 3
            }
            startUSB(false)
            startISP()
            temperatureView.start()
            cameraView!!.start()
            isrun = true
            //恢复配置
            configParam()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.w(TAG, "onStop")
        if (iruvc != null) {
            iruvc!!.stopPreview()
            iruvc!!.unregisterUSB()
        }
        imageThread?.interrupt()
        syncimage.valid = false
        temperatureView.stop()
        cameraView?.stop()
        isrun = false
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.w(TAG, "onDestroy")
        try {
            imageThread?.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, "imageThread.join(): catch an interrupted exception")
        }
        // 某些特定客户的特殊设备需要使用该命令关闭sensor
//        if (Usbcontorl.isload) {
//            Usbcontorl.usb3803_mode_setting(0) //关闭5V
//        }
//        if (tempinfo != 0L) {
//            Libircmd.temp_correction_release(tempinfo)
//        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun iruvctc(event: PreviewComplete) {
        dealY16ModePreviewComplete()
    }

    private fun dealY16ModePreviewComplete() {
        isConfigWait = false
        iruvc!!.setFrameReady(true)
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

    //获取选取点
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
        val activity = requireActivity() as IRMonitorActivity
        activity.select(result)
    }

    private fun setViewLay() {
        thermal_lay.post {
            if (ScreenUtil.isPortrait(requireContext())) {
                val params = thermal_lay.layoutParams
                params.width = ScreenUtil.getScreenWidth(requireContext())
                params.height = params.width * imageHeight / imageWidth
                thermal_lay.layoutParams = params
            } else {
                // 横屏
                val params = thermal_lay.layoutParams
                params.height = thermal_lay.height
                params.width = params.height * imageHeight / imageWidth
                thermal_lay.layoutParams = params
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun cameraEvent(event: DeviceCameraEvent) {
        when (event.action) {
            100 -> {
                //准备图像
                showLoadingDialog()
            }
            101 -> {
                //显示图像
                lifecycleScope.launch {
                    delay(500)
                    isConfigWait = false
                    delay(1000)
                    dismissLoadingDialog()
                }
            }
        }
    }

    private var isConfigWait = true

    //配置
    private fun configParam() {
        lifecycleScope.launch {
            imageThread?.pseudocolorMode = pseudocolorMode//设置伪彩
            isConfigWait = true
            while (isConfigWait) {
                delay(100)
            }
            val config = ConfigRepository.readConfig(false)
            val disChar = (config.distance * 128).toInt() //距离(米)
            val emsChar = (config.radiation * 128).toInt() //发射率
//            val tuChar = (config.environment * 10).toInt().toChar() //环境温度
            XLog.w("设置TPD_PROP DISTANCE:${disChar.toInt()}, EMS:${emsChar.toInt()}}")
            val timeMillis = 250L
            delay(timeMillis)
            //发射率
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
            iruvc?.let {
                // 部分机型在关闭自动快门，初始会花屏
                withContext(Dispatchers.IO){
                    if (SaveSettingUtil.isAutoShutter) {
                        ircmd?.setPropAutoShutterParameter(
                            CommonParams.PropAutoShutterParameter.SHUTTER_PROP_SWITCH,
                            CommonParams.PropAutoShutterParameterValue.StatusSwith.ON
                        )
                    }else{
                        ircmd?.setPropAutoShutterParameter(
                            CommonParams.PropAutoShutterParameter.SHUTTER_PROP_SWITCH,
                            CommonParams.PropAutoShutterParameterValue.StatusSwith.OFF
                        )
                    }
                }
            }
            //复位对比度、细节
            delay(timeMillis)
            ircmd?.setPropImageParams(
                CommonParams.PropImageParams.IMAGE_PROP_LEVEL_CONTRAST,
                CommonParams.PropImageParamsValue.NumberType(128.toString())
            )
            delay(timeMillis)
            ircmd?.setPropImageParams(
                CommonParams.PropImageParams.IMAGE_PROP_LEVEL_DDE,
                CommonParams.PropImageParamsValue.DDEType.DDE_2
            )
            delay(timeMillis)
            ircmd?.setPropImageParams(
                CommonParams.PropImageParams.IMAGE_PROP_ONOFF_AGC,
                CommonParams.PropImageParamsValue.StatusSwith.ON
            )
        }
    }

    fun getBitmap() : Bitmap{
        return cameraView.scaledBitmap
    }

    fun startCoverStsSwitchReady() : Int{
        // 锅盖标定-准备
        return  ircmd?.rmCoverStsSwitch(CommonParams.RMCoverStsSwitchStatus.RMCOVER_DIS) ?: 1
    }
    fun startCoverStsSwitch() : Int{
        // 锅盖标定-准备
        ircmd?.rmCoverAutoCalc(CommonParams.RMCoverAutoCalcType.GAIN_1)
        return ircmd?.rmCoverStsSwitch(CommonParams.RMCoverStsSwitchStatus.RMCOVER_DIS) ?: 1
    }

    override fun tempCorrectByTs(temp: Float?): Float {
        var tmp = temp
        try {
            tmp = tempCorrect(temp!!, gainStatus, 0)
        } catch (e: Exception) {
            XLog.i("温度校正失败: ${e.message}")
        }
        return tmp!!
    }


    /**
     * 单点修正过程
     */
    private fun tempCorrect(
        temp: Float,
        gainStatus: CommonParams.GainStatus, tempInfo: Long
    ): Float {
        return temp


    }
}