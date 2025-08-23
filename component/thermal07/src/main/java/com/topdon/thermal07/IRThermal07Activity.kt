package com.topdon.thermal07

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.infisense.usbir.utils.OpencvTools
import com.infisense.usbir.utils.ViewStubUtils
import com.ir.tas.base.bean.GWData
import com.ir.tas.base.utils.FileUtils
import com.ir.tas.utils.IXUtil
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.bean.CameraItemBean
import com.topdon.lib.core.bean.event.SocketMsgEvent
import com.topdon.lib.core.common.ProductType.PRODUCT_NAME_TC007
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.common.WifiSaveSettingUtil
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.EmissivityTipPopup
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.dialog.TipEmissivityDialog
import com.topdon.lib.core.dialog.TipShutterDialog
import com.topdon.lib.core.ktbase.BaseWifiActivity
import com.topdon.lib.core.repository.BatteryInfo
import com.topdon.lib.core.repository.Custom
import com.topdon.lib.core.repository.CustomColor
import com.topdon.lib.core.repository.GalleryRepository
import com.topdon.lib.core.repository.Isotherm
import com.topdon.lib.core.repository.IsothermC
import com.topdon.lib.core.repository.PalleteBean
import com.topdon.lib.core.repository.Param
import com.topdon.lib.core.repository.Stander
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.lib.core.repository.WifiAttributeBean
import com.topdon.lib.core.socket.SocketCmdUtil
import com.topdon.lib.core.socket.WebSocketProxy
import com.topdon.lib.core.tools.SpanBuilder
import com.topdon.lib.core.tools.TimeTool
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.core.utils.BitmapUtils
import com.topdon.lib.core.utils.CommUtils
import com.topdon.lib.core.utils.ImageUtils
import com.topdon.lib.core.utils.WsCmdConstants
import com.topdon.lib.ui.widget.seekbar.OnRangeChangedListener
import com.topdon.lib.ui.widget.seekbar.RangeSeekBar
import com.topdon.lib.ui.widget.seekbar.RangeSeekBar.TEMP_MODE_CLOSE
import com.topdon.libcom.AlarmHelp
import com.topdon.libcom.dialog.ColorPickDialog
import com.topdon.libcom.dialog.TempAlarmSetDialog
import com.topdon.module.thermal.ir.activity.IRCameraSettingActivity
import com.topdon.module.thermal.ir.adapter.CameraItemAdapter
import com.topdon.module.thermal.ir.event.GalleryAddEvent
import com.topdon.module.thermal.ir.frame.FrameStruct
import com.topdon.module.thermal.ir.repository.ConfigRepository
import com.topdon.module.thermal.ir.utils.IRConfigData
import com.topdon.module.thermal.ir.video.VideoRecordFFmpeg
import com.topdon.module.thermal.ir.view.TimeDownView
import com.topdon.pseudo.activity.PseudoSetActivity
import com.topdon.pseudo.bean.CustomPseudoBean
import com.topdon.tc004.activity.video.PlayFragment
import com.topdon.lib.core.comm.IrParam
import com.topdon.lib.core.comm.TempFont
import com.topdon.lib.core.dialog.CarDetectDialog
import com.topdon.lib.core.dialog.LongTextDialog
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.lib.core.utils.TemperatureUtil
import com.topdon.libcom.bean.SaveSettingBean
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.menu.constant.FenceType
import com.topdon.menu.constant.SettingType
import com.topdon.menu.constant.TwoLightType
import com.topdon.module.thermal.ir.popup.SeekBarPopup
import com.topdon.module.thermal.ir.view.TemperatureBaseView
import com.topdon.thermal07.util.WifiAttributeChangeU
import kotlinx.android.synthetic.main.activity_ir_thermal07.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.easydarwin.video.Client
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@Route(path = RouterConfig.IR_THERMAL_07)
class IRThermal07Activity : BaseWifiActivity() {
    
    private val saveSetBean = SaveSettingBean(true)

    private var isTouchSeekBar: Boolean = false
    private var pseudoColorMode = WifiSaveSettingUtil.pseudoColorMode
    
    private var popupWindow: PopupWindow? = null

    private var textColor: Int = WifiSaveSettingUtil.tempTextColor
    private var textSize: Int = WifiSaveSettingUtil.tempTextSize
    private var tempAlarmSetDialog: TempAlarmSetDialog? = null
    private var alarmBean = WifiSaveSettingUtil.alarmBean
    private var temperatureMode: Int = WifiSaveSettingUtil.temperatureMode
    private val wifiAttributeBean: WifiAttributeBean by lazy {
        WifiAttributeBean(
            Ratio = WifiSaveSettingUtil.twoLightAlpha,
            X = WifiSaveSettingUtil.registrationX,
            Y = WifiSaveSettingUtil.registrationY
        )
    }
    private var cameraAlpha = WifiSaveSettingUtil.twoLightAlpha
    private var imageWidth = 256
    private var imageHeight = 192
    private var imageEditBytes = ByteArray(imageWidth * imageHeight * 4)
    private var customPseudoBean = CustomPseudoBean.loadFromShared(true)
    private var scheduler: ScheduledExecutorService? = null
    private val task: Runnable by lazy {
        Runnable {
            lifecycleScope.launch {
                XLog.i("快门矫正")
                TC007Repository.setCorrection()
            }
        }
    }

    private var irFile: File? = null

    private var dcFile: File? = null

    private var gwData: GWData? = null

    private var batteryInfo: BatteryInfo? = null
        set(value) {
            if (value != null && field != value) {
                field = value
                tv_battery.text = "${value.getBattery()}%"
                battery_view.battery = value.getBattery() ?: 0
                battery_view.isCharging = value.isCharging()
            }
        }

    private var editMaxValue = Float.MAX_VALUE
    private var editMinValue = Float.MIN_VALUE
    private var realLeftValue = -1f
    private var realRightValue = -1f
    private var isShowC = false

    companion object {
        private const val RTSP_URL = "rtsp://192.168.40.1/stream0"
    }

    private var playFragment: PlayFragment? = null

    private val param by lazy {
        Param(
            contrast = (saveSetBean.contrastValue / 256f * 100).toInt(),
            sharpness = (saveSetBean.ddeConfig / 4f * 100).toInt()
        )
    }

    override fun initContentView(): Int = R.layout.activity_ir_thermal07

    override fun onCreate(savedInstanceState: Bundle?) {
        isShowC = SharedManager.getTemperature() == 1
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            playFragment = PlayFragment.newInstance(RTSP_URL, Client.TRANSTYPE_TCP, 1, null, true)
            supportFragmentManager.beginTransaction().add(R.id.thermal_lay, playFragment!!).commit()
        } else {
            playFragment = supportFragmentManager.findFragmentById(R.id.thermal_lay) as PlayFragment
        }
        temperature_seekbar.setIndicatorTextDecimalFormat("0.0")
        lifecycleScope.launch {
            updateTemperatureSeekBar(false)
        }
        thermal_steering_view.listener = { type: Int, x: Int, y: Int ->
            if (type == 0) {
                thermal_steering_view.visibility = View.GONE
                thermal_recycler_night.setTwoLightSelected(TwoLightType.CORRECT, false)
            } else {
                var moveX = thermal_steering_view.moveX
                var moveY = thermal_steering_view.moveY
                lifecycleScope.launch {
                    when (type) {
                        -1 -> {
                            moveY -= thermal_steering_view.moveI
                        }

                        1 -> {
                            moveY += thermal_steering_view.moveI
                        }

                        2 -> {
                            moveX += thermal_steering_view.moveI
                        }

                        3 -> {
                            moveX -= thermal_steering_view.moveI
                        }
                    }
                    wifiAttributeBean.X = moveX
                    wifiAttributeBean.Y = moveY
                    val data = TC007Repository.setRegistration(wifiAttributeBean)
                    if (data?.isSuccess() == true) {
                        WifiSaveSettingUtil.registrationX = x
                        WifiSaveSettingUtil.registrationY = y
                        thermal_steering_view.moveX = moveX
                        thermal_steering_view.moveY = moveY
                    }
                }
            }
        }
        lifecycleScope.launch {
            val tmpR = TC007Repository.getRegistration(false)?.Data
            tmpR?.let {
                thermal_steering_view.moveX = it.X!!
                thermal_steering_view.moveY = it.Y!!
            }
        }
        WebSocketProxy.getInstance().setOnFrameListener(this) {
            realLeftValue = UnitTools.showUnitValue(it.minValue / 10f, isShowC)
            realRightValue = UnitTools.showUnitValue(it.maxValue / 10f, isShowC)
            if (!customPseudoBean.isUseCustomPseudo) {
                try {
                    temperature_seekbar.setRangeAndPro(
                        if (editMinValue != Float.MIN_VALUE) UnitTools.showUnitValue(
                            editMinValue,
                            isShowC
                        ) else editMinValue,
                        if (editMaxValue != Float.MAX_VALUE) UnitTools.showUnitValue(
                            editMaxValue,
                            isShowC
                        ) else editMaxValue,
                        realLeftValue,
                        realRightValue
                    )
                } catch (e: Exception) {
                    Log.e("温度图层更新失败", e.message.toString())
                }
            }
            if (tv_temp_content.isVisible) {
                try {
                    tv_temp_content.text =
                        "Max:${UnitTools.showC(it.maxValue / 10f, isShowC)}\nMin:${
                            UnitTools.showC(
                                it.minValue / 10f,
                                isShowC
                            )
                        }"
                } catch (e: Exception) {
                    Log.e("温度图层更新失败", e.message.toString())
                }
            }
            try {
                if (isVideo) {
                    cl_seek_bar.requestLayout()
                    cl_seek_bar.updateBitmap()
                }
            } catch (e: Exception) {
                Log.w("伪彩条更新异常:", "${e.message}")
            }
            runOnUiThread {
                AlarmHelp.getInstance(application).alarmData(
                    it.maxValue / 10f,
                    it.minValue / 10f, temp_bg
                )
            }
        }
        thermal_recycler_night.post {
            runOnUiThread {
                thermal_recycler_night.refreshImg(GalleryRepository.DirType.TC007)
            }
        }
        temperature_seekbar.setOnRangeChangedListener(object : OnRangeChangedListener {
            var mode = 0
            override fun onRangeChanged(
                view: RangeSeekBar?,
                leftValue: Float,
                rightValue: Float,
                isFromUser: Boolean,
                tempMode: Int
            ) {
                if (isTouchSeekBar) {
                    editMinValue =
                        if (tempMode == RangeSeekBar.TEMP_MODE_MIN || tempMode == RangeSeekBar.TEMP_MODE_INTERVAL) {
                            UnitTools.showToCValue(leftValue)
                        } else {
                            Float.MIN_VALUE
                        }
                    editMaxValue =
                        if (tempMode == RangeSeekBar.TEMP_MODE_MAX || tempMode == RangeSeekBar.TEMP_MODE_INTERVAL) {
                            UnitTools.showToCValue(rightValue)
                        } else {
                            Float.MAX_VALUE
                        }
                    mode = tempMode
                }
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                isTouchSeekBar = true
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                isTouchSeekBar = false
                val max = ((editMaxValue + 273.15f) * 10).toInt()
                val min = ((editMinValue + 273.15f) * 10).toInt()
                lifecycleScope.launch {
                    val isothermC = IsothermC(
                        mode,
                        max, min, min, max,
                    )
                    TC007Repository.setIsotherm(isothermC)
                }
            }

        })
        if (!SharedManager.is07HideEmissivityTips) {
            showEmissivityTips()
        }
    }

    private fun showEmissivityTips() {
        val config = ConfigRepository.readConfig(false)
        var text = ""
        for (tmp in IRConfigData.irConfigData(this)){
            if (config.radiation.toString() == tmp.value){
                if (text.isEmpty()){
                    text = "${resources.getString(com.topdon.module.thermal.ir.R.string.tc_temp_test_materials)} : "
                }
                text += "${tmp.name}/"
            }
        }
        if (text.isNotEmpty()) {
            text = text.substring(0, text.length - 1)
        }
        val dialog = TipEmissivityDialog.Builder(this@IRThermal07Activity)
            .setDataBean(config.environment, config.distance, config.radiation, text)
            .create()
        dialog.onDismissListener = {
            SharedManager.is07HideEmissivityTips = it
        }
        dialog.show()
    }

    private fun startCorrection() {
        scheduler = Executors.newScheduledThreadPool(1)
        scheduler?.scheduleAtFixedRate(task, 0, 60, TimeUnit.SECONDS)
    }

    private fun stopCorrection() {
        try {
            scheduler?.shutdownNow()
        } catch (e: InstantiationException) {
            XLog.e("线程池回收异常：${e.message}")
        }
    }

    override fun initView() {
        AlarmHelp.getInstance(this).updateData(alarmBean)
        view_menu_first.onTabClickListener = {
            ViewStubUtils.showViewStub(view_stub_camera, false, null)
            popupWindow?.dismiss()
            geometry_view.isEnabled = it.selectPosition == 1
            thermal_recycler_night.selectPosition(it.selectPosition + (if (it.isObserveMode) 10 else 0))
        }
        title_view.setRightClickListener {
            val config = ConfigRepository.readConfig(true)
            var text = ""
            for (tmp in IRConfigData.irConfigData(this)){
                if (config.radiation.toString() == tmp.value){
                    if (text.isEmpty()){
                        text = "${resources.getString(com.topdon.module.thermal.ir.R.string.tc_temp_test_materials)} : "
                    }
                    text += "${tmp.name}/"
                }
            }
            if (text.isNotEmpty()) {
                text = text.substring(0, text.length - 1)
            }
            EmissivityTipPopup(this@IRThermal07Activity, true)
                .setDataBean(config.environment, config.distance, config.radiation, text)
                .build()
                .showAsDropDown(title_view, 0, 0, Gravity.END)
        }
        view_car_detect.findViewById<LinearLayout>(com.topdon.module.thermal.ir.R.id.ll_car_detect_info).setOnClickListener {
            LongTextDialog(this, SharedManager.getCarDetectInfo()?.item, SharedManager.getCarDetectInfo()?.description).show()
        }
        thermal_recycler_night.isVideoMode = WifiSaveSettingUtil.isVideoMode
        thermal_recycler_night.onCameraClickListener = {
            setCamera(it)
        }
        thermal_recycler_night.onFenceListener = { fenceType, isSelected ->
            setTemp(fenceType, isSelected)
        }
        thermal_recycler_night.onColorListener = { index, it, size ->
            if (customPseudoBean.isUseCustomPseudo) {
                TipDialog.Builder(this)
                    .setTitleMessage(getString(com.topdon.module.thermal.ir.R.string.app_tip))
                    .setMessage(com.topdon.module.thermal.ir.R.string.tip_change_pseudo_mode)
                    .setPositiveListener(com.topdon.module.thermal.ir.R.string.app_yes) {
                        customPseudoBean.isUseCustomPseudo = false
                        customPseudoBean.saveToShared(true)
                        setPColor(index, it, true)
                        setDefLimit()
                        updateImageAndSeekbarColorList(customPseudoBean)
                    }.setCancelListener(com.topdon.module.thermal.ir.R.string.app_no) {
                    }
                    .create().show()
            } else {
                lifecycleScope.launch {
                    if (temperature_seekbar.tempMode != TEMP_MODE_CLOSE && (index == 0 || index == size)) {
                        setDefLimit()
                        updateTemperatureSeekBar(false)
                        ToastUtils.showShort(R.string.tc_unsupport_mode)
                    }
                    if (temperature_seekbar.tempMode != TEMP_MODE_CLOSE &&
                        index == 0 ||
                        index == size
                    ) {
                        setPColor(index, it, true) {
                            val max = ((editMaxValue + 273.15f) * 10).toInt()
                            val min = ((editMinValue + 273.15f) * 10).toInt()
                            lifecycleScope.launch {
                                val isothermC = IsothermC(
                                    temperature_seekbar.tempMode,
                                    max, min, min, max,
                                )
                                TC007Repository.setIsotherm(isothermC)
                            }
                        }
                    } else {
                        setPColor(index, it, true)
                    }
                }
            }
        }
        thermal_recycler_night.onSettingListener = { type, isSelected ->
            setSetting(type, isSelected)
        }
        thermal_recycler_night.onTempLevelListener = {
            temperatureMode = it
            setConfigForIr(IrParam.ParamTemperature, temperatureMode, true)
            if (it == CameraItemBean.TYPE_TMP_H && SharedManager.isTipHighTemp) {
                val message =
                    SpanBuilder(getString(com.topdon.module.thermal.ir.R.string.tc_high_temp_test_tips1))
                        .appendDrawable(
                            this@IRThermal07Activity,
                            R.drawable.svg_title_temp, SizeUtils.sp2px(24f)
                        )
                        .append(getString(com.topdon.module.thermal.ir.R.string.tc_high_temp_test_tips2))
                TipShutterDialog
                    .Builder(this)
                    .setTitle(com.topdon.module.thermal.ir.R.string.tc_high_temp_test)
                    .setMessage(message)
                    .setCancelListener { isCheck ->
                        SharedManager.isTipHighTemp = !isCheck
                    }
                    .create().show()
            }
        }
        thermal_recycler_night.onTwoLightListener = { twoLightType, isSelected ->
            setTwoLight(twoLightType, isSelected)
        }

        initConfig()

        thermal_recycler_night.fenceSelectType = FenceType.DEL
        geometry_view.mode = TemperatureBaseView.Mode.CLEAR
        geometry_view.setImageSize(8191, 8191)
        geometry_view.onPointListener = {
            lifecycleScope.launch {
                TC007Repository.setTempPointList(it)
            }
        }
        geometry_view.onLineListener = {
            lifecycleScope.launch {
                TC007Repository.setTempLineList(it)
            }
        }
        geometry_view.onRectListener = {
            lifecycleScope.launch {
                TC007Repository.setTempRectList(it)
            }
        }
        temperature_iv_lock.setOnClickListener {
            if (temperature_iv_lock.visibility != View.VISIBLE) {
                return@setOnClickListener
            }

            if (temperature_iv_lock.contentDescription != "lock") {
                setDefLimit()
            }
            lifecycleScope.launch {
                updateTemperatureSeekBar(temperature_iv_lock.contentDescription == "lock")
            }
        }
        temperature_iv_input.setOnClickListener {
            val intent = Intent(this, PseudoSetActivity::class.java)
            intent.putExtra(ExtraKeyConfig.IS_TC007, true)
            pseudoSetResult.launch(intent)
        }
        thermal_recycler_night.updateCameraModel()
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        if (!isTS004) {
            this.finish()
        }
    }

    private val pseudoSetResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                updateImageAndSeekbarColorList(
                    it.data?.getParcelableExtra(ExtraKeyConfig.CUSTOM_PSEUDO_BEAN)
                        ?: CustomPseudoBean()
                )
            }
        }

    private fun updateImageAndSeekbarColorList(customPseudoBean: CustomPseudoBean?) {
        lifecycleScope.launch {
            customPseudoBean?.let {
                temperature_seekbar.setColorList(customPseudoBean.getColorList(true)?.reversedArray())
                temperature_seekbar.setPlaces(customPseudoBean.getPlaceList())
                if (it.isUseCustomPseudo) {
                    temperature_iv_lock.visibility = View.INVISIBLE
                    tv_temp_content.visibility = View.VISIBLE
                    setDefLimit()
                    updateTemperatureSeekBar(false)
                    temperature_seekbar.setRangeAndPro(
                        UnitTools.showUnitValue(it.minTemp, isShowC),
                        UnitTools.showUnitValue(it.maxTemp, isShowC),
                        UnitTools.showUnitValue(it.minTemp, isShowC),
                        UnitTools.showUnitValue(it.maxTemp, isShowC)
                    )
                    thermal_recycler_night.setPseudoColor(-1)
                    temperature_iv_input.setImageResource(com.topdon.module.thermal.ir.R.drawable.ir_model)
                } else {
                    temperature_iv_lock.visibility = View.VISIBLE
                    thermal_recycler_night.setPseudoColor(pseudoColorMode)
                    if (this@IRThermal07Activity.customPseudoBean.isUseCustomPseudo) {
                        setDefLimit()
                    }
                    setPColor(pseudoColorModeToIndex(pseudoColorMode), pseudoColorMode, true)
                    tv_temp_content.visibility = View.GONE
                    temperature_iv_input.setImageResource(com.topdon.module.thermal.ir.R.drawable.ic_color_edit)
                }
                setCustomPseudoColorList(
                    customPseudoBean.getColorList(true),
                    customPseudoBean.getPlaceList(),
                    customPseudoBean.isUseGray, it.maxTemp, it.minTemp
                )
                this@IRThermal07Activity.customPseudoBean = it
                this@IRThermal07Activity.customPseudoBean.saveToShared(true)
            }
        }
    }

    private suspend fun setCustomPseudoColorList(
        colorList: IntArray?,
        places: FloatArray?,
        isUseGray: Boolean,
        customMaxTemp: Float,
        customMinTemp: Float
    ) {
        colorList?.let {
            val highColor = CustomColor(
                Color.red(it[2]), Color.green(it[2]), Color.blue(
                    colorList[2]
                )
            )
            val middleColor = CustomColor(
                Color.red(it[1]), Color.green(it[1]), Color.blue(
                    colorList[1]
                )
            )
            val lowColor = CustomColor(
                Color.red(it[0]), Color.green(it[0]), Color.blue(
                    colorList[0]
                )
            )
            val custom = Custom(
                customMode = (if (isUseGray) 0 else 1),
                ((customMaxTemp + 273.15f) * 10).toInt(),
                ((customMinTemp + 273.15f) * 10).toInt(),
                highColor, middleColor, lowColor
            )
            val palleteBean = PalleteBean(1, custom = custom)
            TC007Repository.setPallete(palleteBean)
        }
    }

    private fun setDefLimit() {
        editMaxValue = Float.MAX_VALUE
        editMinValue = Float.MIN_VALUE
        temperature_seekbar.tempMode = RangeSeekBar.TEMP_MODE_CLOSE
        temperature_seekbar.setRangeAndPro(
            editMinValue,
            editMaxValue,
            realLeftValue,
            realRightValue
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSocketMsgEvent(event: SocketMsgEvent) {
        if (SocketCmdUtil.getCmdResponse(event.text) == WsCmdConstants.APP_EVENT_HEART_BEATS) {
            try {
                val battery: JSONObject = JSONObject(event.text).getJSONObject("battery")
                batteryInfo =
                    BatteryInfo(battery.getString("status"), battery.getString("remaining"))
            } catch (_: Exception) {

            }
        }
    }

    private fun initConfig() {
        thermal_recycler_night.setSettingSelected(SettingType.ALARM, alarmBean.isHighOpen || alarmBean.isLowOpen)
        lifecycleScope.launch {
            showCameraLoading()
            TC007Repository.setRatio(wifiAttributeBean)
            val red = Color.red(textColor)
            val green = Color.green(textColor)
            val blue = Color.blue(textColor)
            val alpha = 0x00
            val colorInt = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
            val isotherm = Isotherm(colorInt.toLong(), textSize + 6 + (textSize - 14) * 1)
            TC007Repository.setFont(isotherm)
            thermal_recycler_night.setSettingSelected(SettingType.FONT,
                textColor != 0xffffffff.toInt() || textSize != 14)
            param.flipMode = if (saveSetBean.isOpenMirror) 1 else 0
            if (TC007Repository.setParam(param)?.isSuccess() == true) {
                thermal_recycler_night.setSettingSelected(SettingType.MIRROR, saveSetBean.isOpenMirror)
            }
            cl_seek_bar.isVisible = saveSetBean.isOpenPseudoBar
            thermal_recycler_night.setSettingSelected(SettingType.PSEUDO_BAR, saveSetBean.isOpenPseudoBar)
            batteryInfo = TC007Repository.getBatteryInfo()
            val config = ConfigRepository.readConfig(true)
            TC007Repository.setIRConfig(config.environment, config.distance, config.radiation)
            TC007Repository.clearAllTemp()
            val tempFrame = TC007Repository.getTempFrame()
            if (tempFrame) {
                TC007Repository.setTempFrame(true)
            }
            thermal_recycler_night.isUnitF = SharedManager.getTemperature() == 0
            val data = TC007Repository.setEnvAttr(
                SharedManager.getTemperature() == 1,
                WifiAttributeChangeU.getTemperatureModeToWifi(temperatureMode)
            )
            if (data) {
                thermal_recycler_night.setTempLevel(temperatureMode)
            }
            when (WifiSaveSettingUtil.fusionType) {
                SaveSettingUtil.FusionTypeLPYFusion -> {
                    TC007Repository.setMode(4)
                    temperature_iv_input.visibility = View.INVISIBLE
                    temperature_iv_lock.visibility = View.INVISIBLE
                    thermal_recycler_night.twoLightType = TwoLightType.TWO_LIGHT_1
                }
                SaveSettingUtil.FusionTypeMeanFusion -> {
                    TC007Repository.setMode(3)
                    temperature_iv_input.visibility = View.INVISIBLE
                    temperature_iv_lock.visibility = View.INVISIBLE
                    thermal_recycler_night.twoLightType = TwoLightType.TWO_LIGHT_2
                }
                SaveSettingUtil.FusionTypeIROnly -> {
                    TC007Repository.setMode(0)
                    temperature_iv_input.visibility = View.VISIBLE
                    temperature_iv_lock.visibility = View.VISIBLE
                    if (customPseudoBean.isUseCustomPseudo) {
                        setCustomPseudoColorList(
                            customPseudoBean.getColorList(true),
                            customPseudoBean.getPlaceList(),
                            customPseudoBean.isUseGray,
                            customPseudoBean.maxTemp, customPseudoBean.minTemp
                        )
                        updateCustomPseudo()
                    } else {
                        setPColor(pseudoColorModeToIndex(pseudoColorMode), pseudoColorMode, false)
                        temperature_iv_lock.visibility = View.VISIBLE
                        tv_temp_content.visibility = View.GONE
                        temperature_iv_input.setImageResource(com.topdon.module.thermal.ir.R.drawable.ic_color_edit)
                        thermal_recycler_night.setPseudoColor(pseudoColorMode)
                        temperature_seekbar?.setPseudocode(pseudoColorMode)
                    }
                    thermal_recycler_night.twoLightType = TwoLightType.IR
                }
                SaveSettingUtil.FusionTypeVLOnly -> {
                    TC007Repository.setMode(1)
                    temperature_iv_input.visibility = View.INVISIBLE
                    temperature_iv_lock.visibility = View.INVISIBLE
                    thermal_recycler_night.twoLightType = TwoLightType.LIGHT
                }
                SaveSettingUtil.FusionTypeTC007Fusion -> {
                    TC007Repository.setMode(2)
                    temperature_iv_input.visibility = View.INVISIBLE
                    temperature_iv_lock.visibility = View.INVISIBLE
                    thermal_recycler_night.twoLightType = TwoLightType.P_IN_P
                }
            }
            param.flipMode = if (saveSetBean.isOpenMirror) 1 else 0
            TC007Repository.setParam(param)
            dismissCameraLoading()
        }
    }

    private fun updateCustomPseudo() {
        temperature_seekbar.setColorList(customPseudoBean.getColorList(true)?.reversedArray())
        temperature_seekbar.setPlaces(customPseudoBean.getPlaceList())
        temperature_iv_lock.visibility = View.INVISIBLE
        temperature_seekbar.setRangeAndPro(
            UnitTools.showUnitValue(customPseudoBean.minTemp, isShowC),
            UnitTools.showUnitValue(customPseudoBean.maxTemp, isShowC),
            UnitTools.showUnitValue(customPseudoBean.minTemp, isShowC),
            UnitTools.showUnitValue(customPseudoBean.maxTemp, isShowC)
        )
        tv_temp_content.visibility = View.VISIBLE
        thermal_recycler_night.setPseudoColor(-1)
        temperature_iv_input.setImageResource(com.topdon.module.thermal.ir.R.drawable.ir_model)
    }

    private fun setTwoLight(twoLightType: TwoLightType, isSelected: Boolean) {
        popupWindow?.dismiss()
        when (twoLightType) {
            TwoLightType.TWO_LIGHT_1 -> {
                showCustomPseudoDialogOrNo(positiveListener = {
                    lifecycleScope.launch {
                        val data = TC007Repository.setMode(4)
                        temperature_iv_input.visibility = View.INVISIBLE
                        temperature_iv_lock.visibility = View.INVISIBLE
                        XLog.i("设备：${data?.Message}")
                        if (200 == data?.Code) {
                            WifiSaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeLPYFusion
                        }
                    }
                }, cancelListener = {

                })
            }
            TwoLightType.TWO_LIGHT_2 -> {
                showCustomPseudoDialogOrNo(positiveListener = {
                    lifecycleScope.launch {
                        val data = TC007Repository.setMode(3)
                        temperature_iv_input.visibility = View.INVISIBLE
                        temperature_iv_lock.visibility = View.INVISIBLE
                        XLog.i("设备：${data?.Message}")
                        if (200 == data?.Code) {
                            WifiSaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeMeanFusion
                        }
                    }
                }, cancelListener = {
                    thermal_recycler_night.setTwoLightSelected(TwoLightType.BLEND_EXTENT, false)
                })
            }
            TwoLightType.IR -> {
                lifecycleScope.launch {
                    val data = TC007Repository.setMode(0)
                    temperature_iv_input.visibility = View.VISIBLE
                    temperature_iv_lock.visibility = View.VISIBLE
                    if (200 == data?.Code) {
                        WifiSaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeIROnly
                    }
                    XLog.i("设备：${data?.Message}")
                }
                thermal_steering_view.visibility = View.GONE
                thermal_recycler_night.setTwoLightSelected(TwoLightType.CORRECT, false)
            }
            TwoLightType.LIGHT -> {
                showCustomPseudoDialogOrNo(positiveListener = {
                    lifecycleScope.launch {
                        val data = TC007Repository.setMode(1)
                        if (200 == data?.Code) {
                            WifiSaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeVLOnly
                        }
                        XLog.i("设备：${data?.Message}")
                    }
                    temperature_iv_input.visibility = View.INVISIBLE
                    temperature_iv_lock.visibility = View.INVISIBLE
                    thermal_steering_view.visibility = View.GONE
                    thermal_recycler_night.setTwoLightSelected(TwoLightType.CORRECT, false)
                }, cancelListener = {
                    thermal_recycler_night.setTwoLightSelected(TwoLightType.BLEND_EXTENT, false)
                })
            }
            TwoLightType.CORRECT -> {
                if (isSelected) {
                    if (thermal_recycler_night.twoLightType != TwoLightType.TWO_LIGHT_1
                        && thermal_recycler_night.twoLightType != TwoLightType.TWO_LIGHT_2
                    ) {
                        showCustomPseudoDialogOrNo(positiveListener = {
                            lifecycleScope.launch {
                                val data = TC007Repository.setMode(3)
                                thermal_recycler_night.twoLightType = TwoLightType.TWO_LIGHT_2
                                WifiSaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeMeanFusion
                                XLog.i("设备：${data?.Message}")
                                temperature_iv_input.visibility = View.INVISIBLE
                                temperature_iv_lock.visibility = View.INVISIBLE
                                thermal_steering_view.visibility = View.VISIBLE
                            }
                        }, cancelListener = {
                            thermal_recycler_night.setTwoLightSelected(TwoLightType.CORRECT, false)
                        })
                    } else {
                        temperature_iv_input.visibility = View.INVISIBLE
                        temperature_iv_lock.visibility = View.INVISIBLE
                        thermal_steering_view.visibility = View.VISIBLE
                    }
                } else {
                    thermal_steering_view.visibility = View.GONE
                }
            }
            TwoLightType.P_IN_P -> {
                showCustomPseudoDialogOrNo(positiveListener = {
                    lifecycleScope.launch {
                        val data = TC007Repository.setMode(2)
                        if (200 == data?.Code) {
                            temperature_iv_input.visibility = View.INVISIBLE
                            temperature_iv_lock.visibility = View.INVISIBLE
                            WifiSaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeTC007Fusion
                        }
                        XLog.i("设备：${data?.Message}")
                    }
                    thermal_steering_view.visibility = View.GONE
                    thermal_recycler_night.setTwoLightSelected(TwoLightType.CORRECT, false)
                }, cancelListener = {

                })
            }
            TwoLightType.BLEND_EXTENT -> {
                if (isSelected) {
                    showCustomPseudoDialogOrNo(positiveListener = {
                        lifecycleScope.launch {
                            val data = TC007Repository.setMode(3)
                            XLog.i("设备：${data?.Message}")
                            if (200 == data?.Code) {
                                temperature_iv_input.visibility = View.INVISIBLE
                                temperature_iv_lock.visibility = View.INVISIBLE
                                thermal_recycler_night.twoLightType = TwoLightType.TWO_LIGHT_2
                                WifiSaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeMeanFusion
                                showBlendExtentPopup()
                            }
                        }
                    }, cancelListener = {
                        thermal_recycler_night.setTwoLightSelected(TwoLightType.BLEND_EXTENT, false)
                    })
                }
            }
        }
    }

    private fun setSetting(type: SettingType, isSelected: Boolean) {
        popupWindow?.dismiss()
        when (type) {
            SettingType.PSEUDO_BAR -> {
                saveSetBean.isOpenPseudoBar = !saveSetBean.isOpenPseudoBar
                cl_seek_bar.isVisible = saveSetBean.isOpenPseudoBar
                thermal_recycler_night.setSettingSelected(SettingType.PSEUDO_BAR, saveSetBean.isOpenPseudoBar)
            }
            SettingType.CONTRAST -> {
                if (!isSelected) {
                    showContrastPopup()
                }
            }
            SettingType.DETAIL -> {
                if (!isSelected) {
                    showSharpnessPopup()
                }
            }
            SettingType.ALARM -> {
                showTempAlarmSetDialog()
            }
            SettingType.ROTATE -> {
            }
            SettingType.FONT -> {
                val colorPickDialog = ColorPickDialog(this, textColor, textSize, true)
                colorPickDialog.onPickListener = { it: Int, textSize: Int ->
                    setConfigForIr(IrParam.ParamTempFont, TempFont(textSize, it), true)
                }
                colorPickDialog.show()
            }
            SettingType.MIRROR -> {
                saveSetBean.isOpenMirror = !saveSetBean.isOpenMirror
                lifecycleScope.launch {
                    param.flipMode = if (saveSetBean.isOpenMirror) 1 else 0
                    val result = TC007Repository.setParam(param)
                    if (result?.isSuccess() == true) {
                        thermal_recycler_night.setSettingSelected(SettingType.MIRROR, saveSetBean.isOpenMirror)
                    } else {
                        TToast.shortToast(this@IRThermal07Activity, R.string.operation_failed_tips)
                    }
                }
            }
            SettingType.COMPASS -> {
            }
            SettingType.WATERMARK -> {
            }
        }
    }

    private fun showCustomPseudoDialogOrNo(positiveListener: (() -> Unit), cancelListener: (() -> Unit)) {
        if (customPseudoBean.isUseCustomPseudo) {
            TipDialog.Builder(this@IRThermal07Activity)
                .setTitleMessage(getString(com.topdon.module.thermal.ir.R.string.app_tip))
                .setMessage(com.topdon.module.thermal.ir.R.string.pseudo_color_switch)
                .setPositiveListener(com.topdon.module.thermal.ir.R.string.app_confirm) {
                    lifecycleScope.launch {
                        val isothermC = IsothermC(
                            RangeSeekBar.TEMP_MODE_CLOSE,
                            0, 0, 0, 0
                        )
                        val data = TC007Repository.setIsotherm(isothermC)
                        if (data?.isSuccess() == true) {
                            customPseudoBean.isUseCustomPseudo = false
                            updateImageAndSeekbarColorList(customPseudoBean)
                            customPseudoBean.saveToShared(true)
                            setPColor(pseudoColorModeToIndex(3), 3, true)
                            positiveListener.invoke()
                        }
                    }

                }.setCancelListener(R.string.app_cancel) {
                    cancelListener.invoke()
                }
                .create().show()
        } else {
            lifecycleScope.launch {
                val isothermC = IsothermC(
                    0,
                    0, 0, 0, 0
                )
                val data = TC007Repository.setIsotherm(isothermC)
                if (data?.isSuccess() == true) {
                    setDefLimit()
                    updateTemperatureSeekBar(false)
                    positiveListener.invoke()
                }
            }
        }

    }

    private fun showBlendExtentPopup() {
        val seekBarPopup = SeekBarPopup(this, true)
        seekBarPopup.progress = cameraAlpha
        seekBarPopup.onValuePickListener = {
            cameraAlpha = it
            WifiSaveSettingUtil.twoLightAlpha = cameraAlpha
            lifecycleScope.launch {
                wifiAttributeBean.Ratio = cameraAlpha
                TC007Repository.setRatio(wifiAttributeBean)
            }
        }
        seekBarPopup.setOnDismissListener {
            thermal_recycler_night.setTwoLightSelected(TwoLightType.BLEND_EXTENT, false)
        }
        seekBarPopup.show(thermal_lay, true)
        popupWindow = seekBarPopup
    }

    private fun getProductName(): String {
        return PRODUCT_NAME_TC007
    }

    private fun setConfigForIr(type: IrParam, data: Any?, isShowError: Boolean = false, netListener: (() -> Unit)? = null) {
        when (type) {
            IrParam.ParamTemperature -> {
                WifiSaveSettingUtil.temperatureMode = temperatureMode
                lifecycleScope.launch {
                    showCameraLoading()
                    TC007Repository.setEnvAttr(
                        SharedManager.getTemperature() == 1,
                        WifiAttributeChangeU.getTemperatureModeToWifi(temperatureMode)
                    )
                    netListener?.invoke()
                    delay(2000)
                    dismissCameraLoading()
                }
            }

            IrParam.ParamPColor -> {
                lifecycleScope.launch {
                    TC007Repository.setPallete(PalleteBean(0, stander = Stander(data as Int, arrayListOf(200, 100, 80))))
                    netListener?.invoke()
                }
            }

            IrParam.ParamTempFont -> {
                lifecycleScope.launch {
                    val red = Color.red((data as TempFont).textColor)
                    val green = Color.green(data.textColor)
                    val blue = Color.blue(data.textColor)
                    val alpha = 0x00
                    val colorInt = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
                    val isotherm =
                        Isotherm(colorInt.toLong(), data.textSize + 6 + (data.textSize - 14) * 1)
                    val repData = TC007Repository.setFont(isotherm)
                    if (repData?.isSuccess() == true) {
                        textColor = data.textColor
                        textSize = data.textSize
                        WifiSaveSettingUtil.tempTextColor = data.textColor
                        WifiSaveSettingUtil.tempTextSize = data.textSize
                        thermal_recycler_night.setSettingSelected(SettingType.FONT,
                            data.textColor != 0xffffffff.toInt() || textSize != 14)
                    } else {
                        if (isShowError) {
                            TToast.shortToast(this@IRThermal07Activity, R.string.operation_failed_tips)
                        }
                    }
                }
            }
            IrParam.ParamAlarm -> {
                alarmBean.isMarkOpen = false
                WifiSaveSettingUtil.alarmBean = alarmBean
                AlarmHelp.getInstance(this@IRThermal07Activity).updateData(
                    if (alarmBean.isLowOpen) alarmBean.lowTemp else null,
                    if (alarmBean.isHighOpen) alarmBean.highTemp else null,
                    if (alarmBean.isRingtoneOpen) alarmBean.ringtoneType else null
                )
            }

            else -> {

            }
        }

    }

    private fun showTempAlarmSetDialog() {
        if (tempAlarmSetDialog == null) {
            tempAlarmSetDialog = TempAlarmSetDialog(this, false)
            tempAlarmSetDialog?.onSaveListener = {
                thermal_recycler_night.setSettingSelected(SettingType.ALARM, it.isHighOpen || it.isLowOpen)
                alarmBean = it
                setConfigForIr(IrParam.ParamAlarm, alarmBean, true)
            }
        }
        tempAlarmSetDialog?.hideAlarmMark = true
        tempAlarmSetDialog?.alarmBean = alarmBean
        tempAlarmSetDialog?.show()
    }

    private fun showContrastPopup() {
        thermal_recycler_night.setSettingSelected(SettingType.CONTRAST, true)

        val seekBarPopup = SeekBarPopup(this)
        seekBarPopup.progress = NumberTools.scale(saveSetBean.contrastValue / 2.56f, 0).toInt()
        seekBarPopup.onValuePickListener = {
            saveSetBean.contrastValue = (it * 2.56f).toInt().coerceAtMost(255)
            lifecycleScope.launch {
                param.contrast = it
                if (TC007Repository.setParam(param)?.isSuccess() == false) {
                    TToast.shortToast(this@IRThermal07Activity, R.string.operation_failed_tips)
                }
            }
        }
        seekBarPopup.setOnDismissListener {
            thermal_recycler_night.setSettingSelected(SettingType.CONTRAST, false)
        }
        seekBarPopup.show(thermal_lay, true)
        popupWindow = seekBarPopup
    }

    private fun showSharpnessPopup() {
        thermal_recycler_night.setSettingSelected(SettingType.DETAIL, true)

        val maxSharpness = 4
        val seekBarPopup = SeekBarPopup(this)
        seekBarPopup.progress = (saveSetBean.ddeConfig * 100f / maxSharpness).toInt()
        seekBarPopup.onValuePickListener = {
            saveSetBean.ddeConfig = (it * maxSharpness / 100f).roundToInt()
            seekBarPopup.progress = (saveSetBean.ddeConfig * 100f / maxSharpness).toInt()
            lifecycleScope.launch {
                param.sharpness = (saveSetBean.ddeConfig * 100f / maxSharpness).toInt()
                if (TC007Repository.setParam(param)?.isSuccess() == false) {
                    TToast.shortToast(this@IRThermal07Activity, R.string.operation_failed_tips)
                }
            }
        }
        seekBarPopup.setOnDismissListener {
            thermal_recycler_night.setSettingSelected(SettingType.DETAIL, false)
        }
        seekBarPopup.show(thermal_lay, true)
        popupWindow = seekBarPopup
    }

    private fun pseudoColorModeToIndex(code: Int): Int {
        return if (code >= 3) {
            code - 2
        } else {
            code - 1
        }
    }

    private fun setPColor(index: Int, code: Int, isShowError: Boolean = false, netListener: (() -> Unit)? = null) {
        pseudoColorMode = code
        setConfigForIr(IrParam.ParamPColor, index, isShowError, netListener)
        thermal_recycler_night.setPseudoColor(-1)
        temperature_seekbar.setPseudocode(pseudoColorMode)
        
        WifiSaveSettingUtil.pseudoColorMode = pseudoColorMode
        thermal_recycler_night.setPseudoColor(code)
    }

    private suspend fun updateTemperatureSeekBar(isEnabled: Boolean) {
        temperature_seekbar.isEnabled = isEnabled
        temperature_seekbar.drawIndPath(isEnabled)
        temperature_iv_lock.setImageResource(if (isEnabled) com.topdon.module.thermal.ir.R.drawable.svg_pseudo_bar_unlock else com.topdon.module.thermal.ir.R.drawable.svg_pseudo_bar_lock)
        temperature_iv_lock.contentDescription = if (isEnabled) "unlock" else "lock"
        if (isEnabled) {
            temperature_seekbar.leftSeekBar.indicatorBackgroundColor = 0xffe17606.toInt()
            temperature_seekbar.rightSeekBar.indicatorBackgroundColor = 0xffe17606.toInt()
            temperature_seekbar.invalidate()
        } else {
            val max = (editMaxValue + 273.15f * 10).toInt()
            val min = (editMinValue + 273.15f * 10).toInt()
            val isothermC = IsothermC(
                temperature_seekbar.tempMode,
                max, min, min, max
            )
            TC007Repository.setIsotherm(isothermC)
            temperature_seekbar.leftSeekBar.indicatorBackgroundColor = 0
            temperature_seekbar.rightSeekBar.indicatorBackgroundColor = 0
            temperature_seekbar.invalidate()
        }
    }

    private fun setTemp(fenceType: FenceType, isSelected: Boolean) {
        when (fenceType) {
            FenceType.POINT -> {
                geometry_view.mode = TemperatureBaseView.Mode.POINT
                lifecycleScope.launch {
                    TC007Repository.setTempFrame(false)
                }
            }
            FenceType.LINE -> {
                geometry_view.mode = TemperatureBaseView.Mode.LINE
                lifecycleScope.launch {
                    TC007Repository.setTempFrame(false)
                }
            }
            FenceType.RECT -> {
                geometry_view.mode = TemperatureBaseView.Mode.RECT
                lifecycleScope.launch {
                    TC007Repository.setTempFrame(false)
                }
            }
            FenceType.FULL -> {
                geometry_view.isShowFull = isSelected
                geometry_view.mode = TemperatureBaseView.Mode.FULL
                lifecycleScope.launch {
                    TC007Repository.setTempFrame(true)
                }
            }
            FenceType.TREND -> {
            }
            FenceType.DEL -> {
                geometry_view.mode = TemperatureBaseView.Mode.CLEAR
                lifecycleScope.launch {
                    TC007Repository.setTempFrame(false)
                    TC007Repository.clearAllTemp()
                }
            }
        }
    }

    private fun setCamera(actionCode: Int) {
        when (actionCode) {
            0 -> {
                checkStoragePermission()
            }
            1 -> {
                ARouter.getInstance()
                    .build(RouterConfig.IR_GALLERY_HOME)
                    .withInt(ExtraKeyConfig.DIR_TYPE, GalleryRepository.DirType.TC007.ordinal)
                    .navigation()
            }
            2 -> {
                settingCamera()
            }
            3 -> {
                WifiSaveSettingUtil.isVideoMode = false
            }
            4 -> {
                WifiSaveSettingUtil.isVideoMode = true
            }
        }
    }

    private var showCameraSetting = false
    private val cameraItemBeanList by lazy {
        mutableListOf(
            CameraItemBean(
                "延迟", CameraItemBean.TYPE_DELAY,
                time = WifiSaveSettingUtil.delayCaptureSecond
            ),
            CameraItemBean(
                "自动快门", CameraItemBean.TYPE_ZDKM,
                isSel = WifiSaveSettingUtil.isAutoShutter
            ),
            CameraItemBean("手动快门", CameraItemBean.TYPE_SDKM),
            CameraItemBean("设置", CameraItemBean.TYPE_SETTING),
        )
    }

    private var cameraDelaySecond: Int = WifiSaveSettingUtil.delayCaptureSecond
    private var cameraItemAdapter: CameraItemAdapter? = null
    private var isAutoShutter: Boolean = WifiSaveSettingUtil.isAutoShutter

    private fun settingCamera() {
        showCameraSetting = !showCameraSetting
        if (showCameraSetting) {
            ViewStubUtils.showViewStub(view_stub_camera, true, callback = { view: View? ->
                view?.let {
                    val recyclerView =
                        it.findViewById<RecyclerView>(com.topdon.module.thermal.ir.R.id.recycler_view)
                    if (ScreenUtil.isPortrait(this)) {
                        recyclerView.layoutManager =
                            GridLayoutManager(this, cameraItemBeanList.size)
                    } else {
                        recyclerView.layoutManager = GridLayoutManager(
                            this, cameraItemBeanList.size,
                            GridLayoutManager.VERTICAL, false
                        )
                    }
                    cameraItemAdapter = CameraItemAdapter(cameraItemBeanList)
                    cameraItemAdapter?.listener = listener@{ position, _ ->
                        when (cameraItemAdapter!!.data[position].type) {
                            CameraItemBean.TYPE_SETTING -> {
                                ARouter.getInstance().build(RouterConfig.IR_CAMERA_SETTING)
                                    .withString(
                                        IRCameraSettingActivity.KEY_PRODUCT_TYPE,
                                        getProductName()
                                    )
                                    .navigation(this)
                                return@listener
                            }

                            CameraItemBean.TYPE_DELAY -> {
                                if (time_down_view.isRunning) {
                                    return@listener
                                }
                                cameraItemAdapter!!.data[position].changeDelayType()
                                cameraItemAdapter!!.notifyItemChanged(position)
                                when (cameraItemAdapter!!.data[position].time) {
                                    CameraItemBean.DELAY_TIME_0 -> {
                                        ToastUtils.showShort(com.topdon.module.thermal.ir.R.string.off_photography)
                                    }

                                    CameraItemBean.DELAY_TIME_3 -> {
                                        ToastUtils.showShort(com.topdon.module.thermal.ir.R.string.seconds_dalay_3)
                                    }

                                    CameraItemBean.DELAY_TIME_6 -> {
                                        ToastUtils.showShort(com.topdon.module.thermal.ir.R.string.seconds_dalay_6)
                                    }
                                }
                                cameraDelaySecond = cameraItemAdapter!!.data[position].time
                                WifiSaveSettingUtil.delayCaptureSecond = cameraDelaySecond
                            }

                            CameraItemBean.TYPE_AUDIO -> {
                                return@listener
                            }

                            CameraItemBean.TYPE_SDKM -> {
                                lifecycleScope.launch {
                                    cameraItemAdapter!!.data[position].isSel = true
                                    cameraItemAdapter!!.notifyItemChanged(position)
                                    TC007Repository.setCorrection()
                                    cameraItemAdapter!!.data[position].isSel = false
                                    cameraItemAdapter!!.notifyItemChanged(position)
                                }

                                ToastUtils.showShort(com.topdon.module.thermal.ir.R.string.app_Manual_Shutter)
                                return@listener
                            }

                            CameraItemBean.TYPE_ZDKM -> {
                                isAutoShutter = !isAutoShutter
                                WifiSaveSettingUtil.isAutoShutter = isAutoShutter
                                cameraItemAdapter!!.data[position].isSel =
                                    !cameraItemAdapter!!.data[position].isSel
                                cameraItemAdapter!!.notifyItemChanged(position)
                                if (SharedManager.isTipShutter && !isAutoShutter) {
                                    val dialog = TipShutterDialog.Builder(this)
                                        .setMessage(com.topdon.module.thermal.ir.R.string.shutter_tips)
                                        .setCancelListener { isCheck ->
                                            SharedManager.isTipShutter = !isCheck
                                        }
                                        .create()
                                    dialog.show()
                                }
                                if (isAutoShutter) {
                                    startCorrection()
                                } else {
                                    stopCorrection()
                                }
                                return@listener
                            }
                        }
                        cameraItemAdapter!!.data[position].isSel =
                            !cameraItemAdapter!!.data[position].isSel
                        cameraItemAdapter!!.notifyItemChanged(position)
                    }
                    recyclerView.adapter = cameraItemAdapter
                }
            })
        } else {
            ViewStubUtils.showViewStub(view_stub_camera, false, null)
        }
    }

    private fun checkStoragePermission() {
        if (!XXPermissions.isGranted(this, permissionList)) {
            if (BaseApplication.instance.isDomestic()) {
                TipDialog.Builder(this)
                    .setMessage(
                        getString(
                            R.string.permission_request_storage_app,
                            CommUtils.getAppName()
                        )
                    )
                    .setCancelListener(R.string.app_cancel)
                    .setPositiveListener(R.string.app_confirm) {
                        initStoragePermission()
                    }
                    .create().show()
            } else {
                initStoragePermission()
            }
        } else {
            initStoragePermission()
        }
    }

    private fun initStoragePermission() {
        XXPermissions.with(this)
            .permission(
                permissionList
            )
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        if (isVideo) {
                            stopIfVideoing()
                            return
                        }
                        if (time_down_view.isRunning) {
                            time_down_view.cancel()
                            updateDelayView()
                        } else {
                            if (time_down_view.downTimeWatcher == null) {
                                time_down_view.setOnTimeDownListener(object :
                                    TimeDownView.DownTimeWatcher {
                                    override fun onTime(num: Int) {
                                        updateDelayView()
                                    }

                                    override fun onLastTime(num: Int) {
                                    }

                                    override fun onLastTimeFinish(num: Int) {
                                        if (!thermal_recycler_night.isVideoMode) {
                                            camera()
                                        } else {
                                            video()
                                        }
                                    }
                                })
                            }
                            time_down_view.downSecond(cameraDelaySecond)
                        }

                    } else {
                        ToastUtils.showShort(R.string.scan_ble_tip_authorize)
                    }
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        if (BaseApplication.instance.isDomestic()) {
                            ToastUtils.showShort(getString(R.string.app_storage_content))
                            return
                        }
                        TipDialog.Builder(this@IRThermal07Activity)
                            .setTitleMessage(getString(R.string.app_tip))
                            .setMessage(R.string.app_storage_content)
                            .setPositiveListener(R.string.app_open) {
                                AppUtils.launchAppDetailsSettings()
                            }
                            .setCancelListener(R.string.app_cancel) {
                            }
                            .setCanceled(true)
                            .create().show()
                    }
                }
            })
    }

    fun updateDelayView() {
        try {
            if (time_down_view.isRunning) {
                runOnUiThread {
                    thermal_recycler_night.setToRecord(true)
                }
            } else {
                runOnUiThread {
                    thermal_recycler_night.refreshImg(GalleryRepository.DirType.TC007)
                }
            }
        } catch (e: Exception) {
            Log.e("线程", e.message.toString())
        }
    }

    private fun camera() {
        lifecycleScope.launch {
            thermal_recycler_night.setToCamera()
            val photoBean = TC007Repository.getPhoto()
            if (200 == photoBean?.Code) {
                val bytes64 =
                    Base64.decode(photoBean.Data?.IRFile ?: photoBean.Data?.DCFile, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes64!!, 0, bytes64.size)
                photoBean.Data?.DCFile?.let {
                    dcFile = FileUtils.createImgDCFile()
                    FileIOUtils.writeFileFromBytesByChannel(
                        dcFile,
                        Base64.decode(it, Base64.DEFAULT),
                        false
                    )
                }
                photoBean.Data?.IRFile?.let {
                    irFile = FileUtils.createImgIRFile()
                    FileIOUtils.writeFileFromBytesByChannel(
                        irFile,
                        Base64.decode(it, Base64.DEFAULT),
                        false
                    )
                }
                var cameraViewBitmap: Bitmap? =
                    Bitmap.createScaledBitmap(
                        bitmap, thermal_lay.measuredWidth,
                        thermal_lay.measuredHeight, true
                    )
                val isShowPseudoBar = cl_seek_bar.visibility == View.VISIBLE
                if (isShowPseudoBar) {
                    val seekBarBitmap = cl_seek_bar.drawToBitmap()
                    cameraViewBitmap = BitmapUtils.mergeBitmap(
                        cameraViewBitmap,
                        seekBarBitmap,
                        cameraViewBitmap!!.width - seekBarBitmap.width,
                        (cameraViewBitmap.height - seekBarBitmap.height) / 2
                    )
                    seekBarBitmap.recycle()
                }
                val watermarkBean = SharedManager.wifiWatermarkBean
                if (watermarkBean.isOpen) {
                    cameraViewBitmap = BitmapUtils.drawCenterLable(
                        cameraViewBitmap,
                        watermarkBean.title,
                        watermarkBean.address,
                        if (watermarkBean.isAddTime) TimeTool.getNowTime() else "",
                        if (temperature_seekbar.isVisible){
                            temperature_seekbar.measuredWidth
                        }else{
                            0
                        }
                    )
                }
                var name = ""
                cameraViewBitmap?.let {
                    name = ImageUtils.save(bitmap = it, true)
                }
                irFile?.let {
                    gwData = IXUtil.parserInfFile(it, dcFile!!)
                    val irData = OpencvTools.convertSingleByteToDoubleByte(gwData!!.irData)
                    val tempData = OpencvTools.convertCelsiusToOriginalBytes(gwData!!.temp)
                    System.arraycopy(irData, 0, imageEditBytes, 0, irData.size)
                    System.arraycopy(tempData, 0, imageEditBytes, 192 * 256 * 2, tempData.size)
                    val capital = FrameStruct.toCode(
                        name = getProductName(),
                        width = if (cameraViewBitmap!!.height < cameraViewBitmap.width) 256 else 192,
                        height = if (cameraViewBitmap.height < cameraViewBitmap.width) 192 else 256,
                        rotate = 0,
                        pseudo = pseudoColorMode,
                        initRotate = 0,
                        correctRotate = 0,
                        customPseudoBean = customPseudoBean,
                        isShowPseudoBar = true,
                        textColor = textColor,
                        watermarkBean = watermarkBean,
                        alarmBean = alarmBean,
                        0,
                        textSize = SizeUtils.sp2px(textSize.toFloat()),
                        0f,0f,0f,
                        false
                    )
                    ImageUtils.saveFrame(bs = imageEditBytes, capital = capital, name = name)
                }
                launch(Dispatchers.Main) {
                    thermal_recycler_night.refreshImg(GalleryRepository.DirType.TC007)
                }
                EventBus.getDefault().post(GalleryAddEvent())
            } else {
                ToastUtils.showShort(R.string.data_error)
                thermal_recycler_night.refreshImg(GalleryRepository.DirType.TC007)
            }
        }
    }

    private var isVideo = false

    private var videoRecord: VideoRecordFFmpeg? = null

    private fun initVideoRecordFFmpeg() {
        playFragment?.let {
            videoRecord = VideoRecordFFmpeg(
                it.textureView,
                null,
                null,
                false,
                cl_seek_bar,
                temp_bg,
                null, null,
                true,
                carView = lay_car_detect_prompt
            )
        }
    }

    private fun video() {
        if (!isVideo) {
            initVideoRecordFFmpeg()
            if (!videoRecord!!.canStartVideoRecord(null)) {
                return
            }
            videoRecord?.stopVideoRecordListener = { isShowVideoRecordTips ->
                this@IRThermal07Activity.runOnUiThread {
                    if (isShowVideoRecordTips) {
                        try {
                            val dialog = TipDialog.Builder(this@IRThermal07Activity)
                                .setMessage(com.topdon.module.thermal.ir.R.string.tip_video_record)
                                .create()
                            dialog.show()
                        } catch (_: Exception) {
                        }
                    }
                    videoRecord?.stopRecord()
                    isVideo = false
                    videoTimeClose()
                    lifecycleScope.launch(Dispatchers.Main) {
                        delay(500)
                        thermal_recycler_night.refreshImg(GalleryRepository.DirType.TC007)
                    }
                }
            }
            videoRecord?.updateAudioState(false)
            videoRecord?.startRecord(FileConfig.tc007GalleryDir)
            isVideo = true
            lifecycleScope.launch(Dispatchers.Main) {
                thermal_recycler_night.setToRecord(false)
            }
            videoTimeShow()
        } else {
            stopIfVideoing()
        }
    }

    private fun stopIfVideoing() {
        if (isVideo) {
            isVideo = false
            videoRecord?.stopRecord()
            videoTimeClose()
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                thermal_recycler_night.refreshImg(GalleryRepository.DirType.TC007)
                EventBus.getDefault().post(GalleryAddEvent())
            }
        }
    }

    private var flow: Job? = null

    private fun videoTimeShow() {
        flow = lifecycleScope.launch {
            val time = 60 * 60 * 4
            flow {
                repeat(time) {
                    emit(it)
                    delay(1000)
                }
            }.collect {
                launch(Dispatchers.Main) {
                    pop_time_text.text = TimeTool.showVideoTime(it * 1000L)
                }
                if (it == time - 1) {
                    video()
                }
            }
        }
        pop_time_lay.visibility = View.VISIBLE
    }

    private fun videoTimeClose() {
        flow?.cancel()
        flow = null
        pop_time_lay.visibility = View.GONE
    }

    @SuppressLint("CheckResult")
    private fun centerCamera() {
        checkStoragePermission()
    }

    override fun initData() {

    }

    override fun onResume() {
        super.onResume()
        AlarmHelp.getInstance(this).onResume()
        if (isAutoShutter) {
            startCorrection()
        }
        thermal_recycler_night.refreshImg(GalleryRepository.DirType.TC007)
        setCarDetectPrompt()
    }

    override fun onPause() {
        super.onPause()
        stopCorrection()
        AlarmHelp.getInstance(this).pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmHelp.getInstance(application).onDestroy(WifiSaveSettingUtil.isSaveSetting)
        temp_bg.stopAnimation()
        time_down_view.cancel()
        CoroutineScope(Dispatchers.IO).launch {
            TC007Repository.clearAllTemp()
        }
    }

    override fun onStop() {
        super.onStop()
        time_down_view?.cancel()
        if (isVideo) {
            isVideo = false
            videoRecord?.stopRecord()
            videoTimeClose()
            CoroutineScope(Dispatchers.Main).launch {
                delay(500)
                EventBus.getDefault().post(GalleryAddEvent())
            }
            lifecycleScope.launch {
                delay(500)
                thermal_recycler_night.refreshImg(GalleryRepository.DirType.TC007)
            }
        }
    }

    private fun setCarDetectPrompt(){
        var carDetectInfo = SharedManager.getCarDetectInfo()
        var tvDetectPrompt = view_car_detect.findViewById<TextView>(R.id.tv_detect_prompt)
        if(carDetectInfo == null){
            tvDetectPrompt.text =  getString(R.string.abnormal_item1) + TemperatureUtil.getTempStr(40, 70)
        }else{
            var temperature = carDetectInfo.temperature.split("~")
            tvDetectPrompt.text =  carDetectInfo.item + TemperatureUtil.getTempStr(temperature[0].toInt(), temperature[1].toInt())
        }
        lay_car_detect_prompt.visibility = if(intent.getBooleanExtra(ExtraKeyConfig.IS_CAR_DETECT_ENTER,false)) View.VISIBLE else View.GONE
        view_car_detect.findViewById<RelativeLayout>(com.topdon.module.thermal.ir.R.id.rl_content).setOnClickListener {
            CarDetectDialog(this) {
                var temperature = it.temperature.split("~")
                tvDetectPrompt.text =  it.item + TemperatureUtil.getTempStr(temperature[0].toInt(), temperature[1].toInt())
            }.show()
        }
    }
}