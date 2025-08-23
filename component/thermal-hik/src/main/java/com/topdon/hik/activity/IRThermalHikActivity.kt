package com.topdon.hik.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.drawToBitmap
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.example.suplib.wrapper.SupHelp
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.topdon.hik.R
import com.topdon.hik.databinding.ActivityIrThermalHikBinding
import com.topdon.lib.core.bean.CameraItemBean
import com.topdon.lib.core.bean.WatermarkBean
import com.topdon.lib.core.common.ProductType
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.CarDetectDialog
import com.topdon.lib.core.dialog.EmissivityTipPopup
import com.topdon.lib.core.dialog.LongTextDialog
import com.topdon.lib.core.dialog.NotTipsSelectDialog
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.dialog.TipEmissivityDialog
import com.topdon.lib.core.dialog.TipShutterDialog
import com.topdon.lib.core.ktbase.BaseBindingActivity
import com.topdon.lib.core.repository.GalleryRepository
import com.topdon.lib.core.tools.CheckDoubleClick
import com.topdon.lib.core.tools.PermissionTool
import com.topdon.lib.core.tools.ScreenTool
import com.topdon.lib.core.tools.SpanBuilder
import com.topdon.lib.core.tools.TimeTool
import com.topdon.lib.core.tools.ToastTools
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.core.utils.BitmapUtils
import com.topdon.lib.core.utils.ImageUtils
import com.topdon.lib.ui.dialog.TipPreviewDialog
import com.topdon.lib.ui.widget.seekbar.OnRangeChangedListener
import com.topdon.lib.ui.widget.seekbar.RangeSeekBar
import com.topdon.libcom.AlarmHelp
import com.topdon.libcom.bean.SaveSettingBean
import com.topdon.libcom.dialog.ColorPickDialog
import com.topdon.libcom.dialog.TempAlarmSetDialog
import com.topdon.libhik.util.HikHelper
import com.topdon.menu.constant.FenceType
import com.topdon.menu.constant.SettingType
import com.topdon.menu.constant.TwoLightType
import com.topdon.module.thermal.ir.bean.DataBean
import com.topdon.module.thermal.ir.event.GalleryAddEvent
import com.topdon.module.thermal.ir.frame.FrameStruct
import com.topdon.module.thermal.ir.popup.CameraItemPopup
import com.topdon.module.thermal.ir.popup.SeekBarPopup
import com.topdon.module.thermal.ir.repository.ConfigRepository
import com.topdon.module.thermal.ir.utils.IRConfigData
import com.topdon.module.thermal.ir.video.VideoRecordFFmpeg
import com.topdon.module.thermal.ir.view.TemperatureBaseView.Mode
import com.topdon.pseudo.activity.PseudoSetActivity
import com.topdon.pseudo.bean.CustomPseudoBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import kotlin.math.roundToInt

@Route(path = RouterConfig.IR_HIK_MAIN)
class IRThermalHikActivity : BaseBindingActivity<ActivityIrThermalHikBinding>() {

    private val saveSetBean = SaveSettingBean()

    private var customPseudoBean = CustomPseudoBean.loadFromShared()

    private var popupWindow: PopupWindow? = null

    private var limitTempMin = Float.MIN_VALUE
        set(value) {
            field = value
            binding.hikSurfaceView.limitTempMin = value
        }
    
    private var limitTempMax = Float.MAX_VALUE
        set(value) {
            field = value
            binding.hikSurfaceView.limitTempMax = value
        }
    
    private var currentMin: Float = 0f
    
    private var currentMax: Float = 0f

    override fun initContentLayoutId(): Int = R.layout.activity_ir_thermal_hik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HikHelper.bind(this)
        HikHelper.setTempListener {
            binding.temperatureView.refreshTemp(it)
        }
        HikHelper.setFrameListener { yuvArray, tempArray ->
            binding.hikSurfaceView.refresh(yuvArray, tempArray)
        }
        HikHelper.onTimeoutListener = {
            TipDialog.Builder(this)
                .setMessage("机芯出了毛病，5秒了没个回调过来")
                .setPositiveListener(R.string.app_got_it) {
                    finish()
                }
                .create().show()
        }
        HikHelper.onReadyListener = {
            lifecycleScope.launch {
                HikHelper.initConfig()
                HikHelper.setAutoShutter(saveSetBean.isAutoShutter)
                HikHelper.setContrast((saveSetBean.contrastValue * 100 / 255f).toInt())
                HikHelper.setEnhanceLevel((saveSetBean.ddeConfig * 100f / 4).toInt())
                HikHelper.setMirror(saveSetBean.rotateAngle, saveSetBean.isOpenMirror)
                HikHelper.setTemperatureMode(saveSetBean.temperatureMode)

                val config: DataBean = ConfigRepository.readConfig(false)
                HikHelper.setEmissivity((config.radiation * 100).toInt())
                HikHelper.setDistance((config.distance * 100).toInt().coerceAtLeast(30))
            }
        }

        binding.titleView.setRight2Drawable(if (saveSetBean.isOpenAmplify) R.drawable.svg_tisr_on else R.drawable.svg_tisr_off)
        binding.titleView.setLeftClickListener {
            if (!binding.timeDownView.isRunning) {
                finish()
            }
        }
        binding.titleView.setRightClickListener {
            val config: DataBean = ConfigRepository.readConfig(false)
            val emText = IRConfigData.getTextByEmissivity(this, config.radiation)
            EmissivityTipPopup(this, false)
                .setDataBean(config.environment, config.distance, config.radiation, emText)
                .build()
                .showAsDropDown(binding.titleView, 0, 0, Gravity.END)
        }
        binding.titleView.setRight2ClickListener {
            switchAmplify()
        }

        binding.timeDownView.onFinishListener = {
            if (!saveSetBean.isVideoMode) {
                binding.menuSecondView.updateCameraModel()
            }
            centerCamera()
        }

        binding.hikSurfaceView.isOpenAmplify = saveSetBean.isOpenAmplify
        binding.hikSurfaceView.rotateAngle = saveSetBean.rotateAngle
        binding.hikSurfaceView.alarmBean = saveSetBean.alarmBean
        binding.hikSurfaceView.setPseudoCode(saveSetBean.pseudoColorMode)
        if (saveSetBean.isOpenAmplify) {
            lifecycleScope.launch(Dispatchers.IO) {
                SupHelp.getInstance().initA4KCPP()
            }
        }

        binding.cameraPreView.cameraPreViewCloseListener = {
            if (binding.cameraPreView.isPreviewing) {
                popupWindow?.dismiss()
                switchPinPState(false)
            }
        }
        if (saveSetBean.isOpenTwoLight && XXPermissions.isGranted(this, Permission.CAMERA)) {
            binding.cameraPreView.postDelayed(500) {
                switchPinPState(false)
            }
        }

        initPseudoBar()
        initTempView()
        initCarDetect()
        initMenu()
        refreshCustomPseudo(customPseudoBean)
        rotateUI(saveSetBean.isRotatePortrait())

        AlarmHelp.getInstance(this).updateData(saveSetBean.alarmBean)

        lifecycleScope.launch {
            delay(1000)
            showEmissivityTipsDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.menuSecondView.refreshImg()
        AlarmHelp.getInstance(this).onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        stopIfContinua()
        AlarmHelp.getInstance(this).pause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        binding.timeDownView.cancel()
        stopIfVideoing(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.tempLayout.stopAnimation()
        AlarmHelp.getInstance(this).onDestroy(saveSetBean.isSaveSetting)
    }

    override fun disConnected() {
        finish()
    }

    private fun initPseudoBar() {
        binding.clPseudoBar.isVisible = saveSetBean.isOpenPseudoBar
        if (ScreenTool.isIPad(this)) {
            binding.clPseudoBar.setPadding(0, SizeUtils.dp2px(40f), 0, SizeUtils.dp2px(40f))
        }

        binding.ivPseudoBarLock.setOnClickListener {
            if (it.isVisible) {
                it.isSelected = !it.isSelected
                binding.pseudoSeekBar.isEnabled = it.isSelected
                binding.pseudoSeekBar.leftSeekBar.indicatorBackgroundColor = if (it.isSelected) 0xffe17606.toInt() else 0
                binding.pseudoSeekBar.rightSeekBar.indicatorBackgroundColor = if (it.isSelected) 0xffe17606.toInt() else 0
                if (!it.isSelected) {
                    limitTempMin = Float.MIN_VALUE
                    limitTempMax = Float.MAX_VALUE
                    binding.pseudoSeekBar.setRangeAndPro(limitTempMin, limitTempMax, currentMin, currentMax)
                }
            }
        }
        binding.ivPseudoBarInput.setOnClickListener {
            val intent = Intent(this, PseudoSetActivity::class.java)
            intent.putExtra(ExtraKeyConfig.IS_TC007, false)
            pseudoSetResult.launch(intent)
        }

        binding.pseudoSeekBar.isEnabled = false
        binding.pseudoSeekBar.setIndicatorTextDecimalFormat("0.0")
        binding.pseudoSeekBar.setPseudocode(saveSetBean.pseudoColorMode)
        binding.pseudoSeekBar.setOnRangeChangedListener(object : OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar?, leftValue: Float, rightValue: Float, isFromUser: Boolean, tempMode: Int) {
                limitTempMin = if (tempMode == RangeSeekBar.TEMP_MODE_MIN || tempMode == RangeSeekBar.TEMP_MODE_INTERVAL) {
                    UnitTools.showToCValue(leftValue)
                } else {
                    Float.MIN_VALUE
                }
                limitTempMax = if (tempMode == RangeSeekBar.TEMP_MODE_MAX || tempMode == RangeSeekBar.TEMP_MODE_INTERVAL) {
                    UnitTools.showToCValue(rightValue)
                } else {
                    Float.MAX_VALUE
                }
            }

            override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }

            override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initTempView() {
        binding.temperatureView.mode = Mode.FULL
        binding.temperatureView.textColor = saveSetBean.tempTextColor
        binding.temperatureView.tempTextSize = saveSetBean.tempTextSize
        binding.temperatureView.rotateAngle = saveSetBean.rotateAngle
        binding.temperatureView.onTempChangeListener = { min, max ->
            AlarmHelp.getInstance(this).alarmData(max, min, binding.tempLayout)
            currentMin = UnitTools.showUnitValue(min)
            currentMax = UnitTools.showUnitValue(max)
            if (customPseudoBean.isUseCustomPseudo) {
                binding.tvTempContent.text = "Max:${UnitTools.showC(max)}\nMin:${UnitTools.showC(min)}"
            } else {
                val limitMin: Float = if (limitTempMin == Float.MIN_VALUE) Float.MIN_VALUE else UnitTools.showUnitValue(limitTempMin)
                val limitMax: Float = if (limitTempMax == Float.MAX_VALUE) Float.MAX_VALUE else UnitTools.showUnitValue(limitTempMax)
                binding.pseudoSeekBar.setRangeAndPro(limitMin, limitMax, currentMin, currentMax)
            }
            if (cameraState == CameraState.TAKING) {
                binding.clPseudoBar.requestLayout()
                binding.clPseudoBar.updateBitmap()
            }
        }
        binding.temperatureView.onTrendChangeListener = {
            binding.trendView.refreshChart(it)
        }
        binding.temperatureView.onTrendOperateListener = { isAdd ->
            if (isAdd) {
                if (hasClickTrendDel) {
                    hasClickTrendDel = false
                    binding.trendView.expand()
                }
            } else {
                binding.trendView.setToEmpty()
            }
        }
    }

    private fun initCarDetect() {
        binding.llCarDetect.isVisible = intent.getBooleanExtra(ExtraKeyConfig.IS_CAR_DETECT_ENTER, false)
        binding.tvCarDetect.text = SharedManager.getCarDetectInfo().buildString()
        binding.ivCarDetect.setOnClickListener {
            LongTextDialog(this, SharedManager.getCarDetectInfo().item, SharedManager.getCarDetectInfo().description).show()
        }
        binding.tvCarDetect.setOnClickListener {
            CarDetectDialog(this) {
                binding.tvCarDetect.text = it.buildString()
            }
        }
    }

    private var hasClickTrendDel = true
    
    private fun initMenu() {
        binding.menuFirstView.onTabClickListener = {
            popupWindow?.dismiss()
            binding.temperatureView.isEnabled = it.selectPosition == 1
            binding.menuSecondView.selectPosition(it.selectPosition)
        }

        binding.menuSecondView.onCameraClickListener = {
            when (it) {
                0 -> {
                    when (cameraState) {
                        CameraState.INIT -> PermissionTool.requestFile(this) {
                            if (saveSetBean.delayCaptureSecond > 0) {
                                cameraState = CameraState.DELAY
                                binding.menuSecondView.setToRecord(true)
                                binding.timeDownView.downSecond(saveSetBean.delayCaptureSecond)
                            } else {
                                centerCamera()
                            }
                        }
                        CameraState.DELAY -> {
                            cameraState = CameraState.INIT
                            binding.timeDownView.cancel()
                            binding.menuSecondView.updateCameraModel()
                        }
                        CameraState.TAKING -> {
                            if (saveSetBean.isVideoMode) {
                                stopIfVideoing()
                            } else {
                                stopIfContinua()
                            }
                        }
                    }
                }
                1 -> {
                    lifecycleScope.launch {
                        stopIfVideoing()
                        delay(500)
                        ARouter.getInstance()
                            .build(RouterConfig.IR_GALLERY_HOME)
                            .withInt(ExtraKeyConfig.DIR_TYPE, GalleryRepository.DirType.LINE.ordinal)
                            .navigation()
                    }
                }
                2 -> {
                    showCameraItemPopup()
                }
                3 -> {
                    stopIfContinua()
                    saveSetBean.isVideoMode = false
                }
                4 -> {
                    stopIfContinua()
                    saveSetBean.isVideoMode = true
                }
            }
        }
        binding.menuSecondView.onFenceListener = { fenceType, isSelected ->
            when (fenceType) {
                FenceType.POINT -> binding.temperatureView.mode = Mode.POINT
                FenceType.LINE -> binding.temperatureView.mode = Mode.LINE
                FenceType.RECT -> binding.temperatureView.mode = Mode.RECT
                FenceType.FULL -> binding.temperatureView.isShowFull = isSelected
                FenceType.TREND -> {
                    binding.temperatureView.mode = Mode.TREND
                    if (SharedManager.isNeedShowTrendTips) {
                        NotTipsSelectDialog(this)
                            .setTipsResId(R.string.thermal_trend_tips)
                            .setOnConfirmListener {
                                SharedManager.isNeedShowTrendTips = !it
                            }
                            .show()
                    }
                    if (!binding.trendView.isVisible) {
                        binding.trendView.isVisible = true
                        binding.trendView.close()
                    }
                }
                FenceType.DEL -> {
                    hasClickTrendDel = true
                    binding.temperatureView.mode = Mode.CLEAR
                    binding.trendView.isVisible = false
                }
            }
        }
        binding.menuSecondView.onTwoLightListener = { twoLightType, isSelected ->
            popupWindow?.dismiss()
            when (twoLightType) {
                TwoLightType.P_IN_P -> {
                    switchPinPState(true)
                }
                TwoLightType.BLEND_EXTENT -> {
                    if (!binding.cameraPreView.isPreviewing && isSelected) {
                        switchPinPState(false)
                    }
                    if (isSelected) {
                        showBlendExtentPopup()
                    }
                }
                else -> {

                }
            }
        }
        binding.menuSecondView.onColorListener = { _, it, _ ->
            if (customPseudoBean.isUseCustomPseudo) {
                TipDialog.Builder(this)
                    .setTitleMessage(getString(R.string.app_tip))
                    .setMessage(R.string.tip_change_pseudo_mode)
                    .setPositiveListener(R.string.app_yes) {
                        customPseudoBean.isUseCustomPseudo = false
                        customPseudoBean.saveToShared()

                        saveSetBean.pseudoColorMode = it
                        binding.hikSurfaceView.setPseudoCode(it)
                        binding.pseudoSeekBar.setPseudocode(it)
                        refreshCustomPseudo(customPseudoBean)
                    }.setCancelListener(R.string.app_no) {

                    }
                    .create().show()
            } else {
                saveSetBean.pseudoColorMode = it
                binding.hikSurfaceView.setPseudoCode(it)
                binding.pseudoSeekBar.setPseudocode(it)
            }
        }
        binding.menuSecondView.onSettingListener = { type, isSelected ->
            popupWindow?.dismiss()
            when (type) {
                SettingType.PSEUDO_BAR -> {
                    saveSetBean.isOpenPseudoBar = !saveSetBean.isOpenPseudoBar
                    binding.clPseudoBar.isVisible = saveSetBean.isOpenPseudoBar
                    binding.menuSecondView.setSettingSelected(SettingType.PSEUDO_BAR, saveSetBean.isOpenPseudoBar)
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
                    saveSetBean.rotateAngle = if (saveSetBean.rotateAngle == 0) 270 else (saveSetBean.rotateAngle - 90)
                    binding.menuSecondView.setSettingRotate(saveSetBean.rotateAngle)

                    binding.hikSurfaceView.rotateAngle = saveSetBean.rotateAngle

                    binding.temperatureView.rotateAngle = saveSetBean.rotateAngle
                    binding.temperatureView.mode = Mode.CLEAR
                    binding.temperatureView.mode = Mode.FULL
                    binding.menuSecondView.fenceSelectType = FenceType.FULL

                    hasClickTrendDel = true
                    binding.trendView.isVisible = false

                    rotateUI(saveSetBean.isRotatePortrait())
                }
                SettingType.FONT -> {
                    val colorPickDialog = ColorPickDialog(this, saveSetBean.tempTextColor, saveSetBean.tempTextSize)
                    colorPickDialog.onPickListener = { color, size ->
                        saveSetBean.tempTextColor = color
                        saveSetBean.tempTextSize = SizeUtils.sp2px(size.toFloat())
                        binding.temperatureView.textColor = saveSetBean.tempTextColor
                        binding.temperatureView.tempTextSize = saveSetBean.tempTextSize
                        binding.menuSecondView.setSettingSelected(SettingType.FONT, !saveSetBean.isTempTextDefault())
                    }
                    colorPickDialog.show()
                }
                SettingType.MIRROR -> {
                    saveSetBean.isOpenMirror = !isSelected
                    binding.menuSecondView.setSettingSelected(SettingType.MIRROR, !isSelected)
                    lifecycleScope.launch {
                        HikHelper.setMirror(saveSetBean.rotateAngle, !isSelected)
                    }
                }
                SettingType.COMPASS -> {
                }
                SettingType.WATERMARK -> {
                }
            }
        }
        binding.menuSecondView.onTempLevelListener = {
            saveSetBean.temperatureMode = it
            lifecycleScope.launch {
                showLoadingDialog()
                HikHelper.setTemperatureMode(saveSetBean.temperatureMode)
                dismissLoadingDialog()
            }
            if (it == CameraItemBean.TYPE_TMP_H && SharedManager.isTipHighTemp) {
                TipShutterDialog.Builder(this)
                    .setTitle(R.string.tc_high_temp_test)
                    .setMessage(
                        SpanBuilder(getString(R.string.tc_high_temp_test_tips1))
                            .appendDrawable(this, R.drawable.svg_title_temp, SizeUtils.sp2px(24f))
                            .append(getString(R.string.tc_high_temp_test_tips2))
                    )
                    .setCancelListener { isCheck ->
                        SharedManager.isTipHighTemp = !isCheck
                    }
                    .create().show()
            }
            if (it == CameraItemBean.TYPE_TMP_ZD) {
                ToastTools.showShort(R.string.auto_open)
            }
        }

        binding.menuSecondView.isVideoMode = saveSetBean.isVideoMode
        binding.menuSecondView.fenceSelectType = FenceType.FULL
        binding.menuSecondView.setPseudoColor(saveSetBean.pseudoColorMode)
        binding.menuSecondView.setSettingRotate(saveSetBean.rotateAngle)
        binding.menuSecondView.setSettingSelected(SettingType.MIRROR, saveSetBean.isOpenMirror)
        binding.menuSecondView.setSettingSelected(SettingType.PSEUDO_BAR, saveSetBean.isOpenPseudoBar)
        binding.menuSecondView.setSettingSelected(SettingType.ALARM, saveSetBean.alarmBean.isOpen())
        binding.menuSecondView.setSettingSelected(SettingType.FONT, !saveSetBean.isTempTextDefault())
        binding.menuSecondView.setTempLevel(saveSetBean.temperatureMode)
        binding.menuSecondView.isUnitF = SharedManager.getTemperature() == 0
    }

    private var cameraState = CameraState.INIT
    private enum class CameraState {
        
        INIT,
        
        DELAY,
        
        TAKING,
    }

    private var continuousJob: Job? = null
    
    private fun stopIfContinua() {
        if (cameraState == CameraState.TAKING) {
            continuousJob?.cancel()
            cameraState = CameraState.INIT
            binding.menuSecondView.refreshImg()
            binding.tvContinuousTips.isVisible = false
        }
    }

    private fun centerCamera() {
        if (saveSetBean.isVideoMode) {
            startVideo()
        } else {
            cameraState = CameraState.TAKING
            val continuousBean = SharedManager.continuousBean
            if (continuousBean.isOpen) {
                continuousJob = lifecycleScope.launch {
                    binding.tvContinuousTips.isVisible = true
                    flow {
                        repeat(continuousBean.count) {
                            emit(it)
                            delay(continuousBean.continuaTime)
                        }
                    }.collect {
                        camera()
                    }
                    cameraState = CameraState.INIT
                    binding.tvContinuousTips.isVisible = false
                }
            } else {
                lifecycleScope.launch {
                    camera()
                    cameraState = CameraState.INIT
                }
            }
        }
    }

    private var videoRecord: VideoRecordFFmpeg? = null
    
    private var videoTimeJob: Job? = null

    private fun startVideo() {
        if (!VideoRecordFFmpeg.canStartVideoRecord(this)) {
            return
        }
        cameraState = CameraState.TAKING
        binding.clPseudoBar.updateBitmap()
        binding.menuSecondView.setToRecord(false)

        videoRecord = VideoRecordFFmpeg(
            binding.hikSurfaceView,
            binding.cameraPreView,
            binding.temperatureView,
            false,
            binding.clPseudoBar,
            binding.tempLayout,
            carView = binding.llCarDetect
        )
        videoRecord?.stopVideoRecordListener = { isShowVideoRecordTips ->
            runOnUiThread {
                if (isShowVideoRecordTips) {
                    TipDialog.Builder(this)
                        .setMessage(R.string.tip_video_record)
                        .create().show()
                }
                stopIfVideoing()
            }
        }
        videoRecord?.updateAudioState(saveSetBean.isRecordAudio)
        videoRecord?.startRecord()

        videoTimeJob = lifecycleScope.launch {
            flow {
                repeat(60 * 60 * 2) {
                    emit(it)
                    delay(1000)
                }
            }.collect {
                binding.tvVideoTime.text = TimeTool.showVideoTime(it * 1000L)
            }
        }
        binding.tvVideoTime.isVisible = true
    }

    private fun stopIfVideoing(isLifecycle: Boolean = true) {
        if (cameraState == CameraState.TAKING) {
            cameraState = CameraState.INIT
            videoRecord?.stopRecord()

            videoTimeJob?.cancel()
            videoTimeJob = null
            binding.tvVideoTime.isVisible = false

            val scope = if (isLifecycle) lifecycleScope else CoroutineScope(Dispatchers.Main)
            scope.launch(Dispatchers.Main) {
                delay(500)
                binding.menuSecondView.refreshImg()
                EventBus.getDefault().post(GalleryAddEvent())
            }
        }
    }

    private suspend fun camera() = withContext(Dispatchers.Default) {
        withContext(Dispatchers.Main) {
            binding.menuSecondView.setToCamera()
        }

        var bitmap: Bitmap = binding.hikSurfaceView.getScaleBitmap()

        if (saveSetBean.isOpenTwoLight) {
            bitmap = BitmapUtils.mergeBitmapByViewNonNull(bitmap, binding.cameraPreView.getBitmap(), binding.cameraPreView)
            ImageUtils.saveImageToApp(bitmap)
        }

        if (binding.clPseudoBar.isVisible) {
            val seekBarBitmap = binding.clPseudoBar.drawToBitmap()
            bitmap = BitmapUtils.mergeBitmap(
                bitmap,
                seekBarBitmap,
                bitmap.width - seekBarBitmap.width,
                (bitmap.height - seekBarBitmap.height) / 2,
            )
        }

        if (binding.temperatureView.mode != Mode.CLEAR) {
            binding.temperatureView.draw(Canvas(bitmap))
        }

        if (binding.llCarDetect.isVisible) {
            bitmap = BitmapUtils.mergeBitmap(bitmap, binding.llCarDetect.drawToBitmap(), 0, 0)
        }

        val watermarkBean: WatermarkBean = SharedManager.watermarkBean
        if (watermarkBean.isOpen) {
            bitmap = BitmapUtils.drawCenterLable(
                bitmap,
                watermarkBean.title,
                watermarkBean.address,
                if (watermarkBean.isAddTime) TimeTool.getNowTime() else "",
                if (binding.pseudoSeekBar.isVisible) binding.pseudoSeekBar.measuredWidth else 0
            )
        }

        val filename: String = ImageUtils.save(bitmap)

        val emConfig: DataBean = ConfigRepository.readConfig(false)
        val capital = FrameStruct.toCode(
            name = ProductType.PRODUCT_NAME_TC002C_DUO,
            width = if (bitmap.width > bitmap.height) 256 else 192,
            height = if (bitmap.width > bitmap.height) 192 else 256,
            rotate = saveSetBean.rotateAngle,
            pseudo = saveSetBean.pseudoColorMode,
            initRotate = saveSetBean.rotateAngle,
            correctRotate = saveSetBean.rotateAngle,
            customPseudoBean = customPseudoBean,
            isShowPseudoBar = binding.clPseudoBar.isVisible,
            textColor = saveSetBean.tempTextColor,
            watermarkBean = watermarkBean,
            alarmBean = saveSetBean.alarmBean,
            gainStatus = 0,
            textSize = saveSetBean.tempTextSize,
            environment = emConfig.environment,
            distance = emConfig.distance,
            radiation = emConfig.radiation,
            isAmplify = saveSetBean.isOpenAmplify
        )
        ImageUtils.saveFrame(bs = HikHelper.copyFrameData(), capital = capital, name = filename)

        withContext(Dispatchers.Main) {
            binding.menuSecondView.refreshImg()
        }
        EventBus.getDefault().post(GalleryAddEvent())
    }

    private fun switchPinPState(isShowTips: Boolean) {
        if (!CheckDoubleClick.isFastDoubleClick()) {
            if (binding.cameraPreView.isPreviewing) {
                saveSetBean.isOpenTwoLight = false
                binding.cameraPreView.isVisible = false
                binding.cameraPreView.closeCamera()
                binding.menuSecondView.setTwoLightSelected(TwoLightType.P_IN_P, false)
            } else {
                PermissionTool.requestCamera(this) {
                    saveSetBean.isOpenTwoLight = true
                    binding.cameraPreView.isVisible = true
                    binding.cameraPreView.setCameraAlpha(saveSetBean.twoLightAlpha / 100f)
                    binding.cameraPreView.post {
                        binding.cameraPreView.openCamera()
                    }
                    binding.menuSecondView.setTwoLightSelected(TwoLightType.P_IN_P, true)

                    if (isShowTips && SharedManager.isTipPinP) {
                        val dialog = TipPreviewDialog.newInstance()
                        dialog.closeEvent = {
                            SharedManager.isTipPinP = !it
                        }
                        dialog.show(supportFragmentManager, "")
                    }
                }
            }
        }
    }

    private fun rotateUI(isPortrait: Boolean) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.clRoot)
        constraintSet.setDimensionRatio(R.id.thermal_lay, (if (isPortrait) 192/256f else (256/192f)).toString())
        constraintSet.applyTo(binding.clRoot)

        binding.clPseudoBar.requestLayout()
        binding.clPseudoBar.updateBitmap()
    }

    private val pseudoSetResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            lifecycleScope.launch {
                val customPseudoBean: CustomPseudoBean = it.data?.getParcelableExtra(ExtraKeyConfig.CUSTOM_PSEUDO_BEAN) ?: CustomPseudoBean()
                customPseudoBean.saveToShared()
                refreshCustomPseudo(customPseudoBean)
            }
        }
    }

    private fun refreshCustomPseudo(newData: CustomPseudoBean) {
        val isCustom: Boolean = newData.isUseCustomPseudo
        binding.tvTempContent.isVisible = isCustom
        binding.ivPseudoBarLock.isInvisible = isCustom
        binding.ivPseudoBarInput.setImageResource(if (isCustom) R.drawable.ir_model else R.drawable.ic_color_edit)
        binding.pseudoSeekBar.setColorList(newData.getColorList()?.reversedArray())
        binding.pseudoSeekBar.setPlaces(newData.getPlaceList())
        binding.hikSurfaceView.refreshCustomPseudo(newData)
        binding.menuSecondView.setPseudoColor(if (isCustom) -1 else saveSetBean.pseudoColorMode)

        limitTempMin = Float.MIN_VALUE
        limitTempMax = Float.MAX_VALUE
        binding.pseudoSeekBar.setRangeAndPro(limitTempMin, limitTempMax, currentMin, currentMax)

        if (isCustom) {
            val min: Float = UnitTools.showUnitValue(newData.minTemp)
            val max: Float = UnitTools.showUnitValue(newData.maxTemp)
            binding.pseudoSeekBar.isEnabled = false
            binding.ivPseudoBarLock.isSelected = false
            binding.pseudoSeekBar.setRangeAndPro(min, max, min, max)
            binding.pseudoSeekBar.leftSeekBar.indicatorBackgroundColor = 0
            binding.pseudoSeekBar.rightSeekBar.indicatorBackgroundColor = 0
        }
        this.customPseudoBean = newData
    }

    private fun switchAmplify() {
        if (SupHelp.getInstance().loadOpenclSuccess) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        SupHelp.getInstance().initA4KCPP()
                    } catch (e: UnsatisfiedLinkError) {
                        SupHelp.getInstance().loadOpenclSuccess = false
                        XLog.e("超分初始化失败")
                    }
                }
                if (!SupHelp.getInstance().loadOpenclSuccess) {
                    TipDialog.Builder(this@IRThermalHikActivity)
                        .setMessage(R.string.tips_tisr_fail)
                        .setPositiveListener(R.string.app_got_it) {
                        }
                        .create().show()
                    return@launch
                }
                saveSetBean.isOpenAmplify = !saveSetBean.isOpenAmplify
                binding.hikSurfaceView.isOpenAmplify = saveSetBean.isOpenAmplify
                binding.titleView.setRight2Drawable(if (saveSetBean.isOpenAmplify) R.drawable.svg_tisr_on else R.drawable.svg_tisr_off)
                if (saveSetBean.isOpenAmplify) {
                    ToastUtils.showShort(R.string.tips_tisr_on)
                } else {
                    ToastUtils.showShort(R.string.tips_tisr_off)
                }
            }
        } else {
            TipDialog.Builder(this)
                .setMessage(R.string.tips_tisr_fail)
                .setPositiveListener(R.string.app_got_it) {
                }
                .create().show()
        }
    }

    private fun showEmissivityTipsDialog() {
        if (SharedManager.isHideEmissivityTips) {
            return
        }
        val config: DataBean = ConfigRepository.readConfig(false)
        val emText = IRConfigData.getTextByEmissivity(this, config.radiation)
        val dialog = TipEmissivityDialog.Builder(this)
            .setDataBean(config.environment, config.distance, config.radiation, emText)
            .create()
        dialog.onDismissListener = {
            SharedManager.isHideEmissivityTips = it
        }
        dialog.show()
    }

    private fun showCameraItemPopup() {
        if (popupWindow?.isShowing == true) {
            popupWindow?.dismiss()
            return
        }
        val cameraItemPopup = CameraItemPopup(this, saveSetBean)
        cameraItemPopup.onDelayClickListener = { !binding.timeDownView.isRunning }
        cameraItemPopup.onAutoCLickListener = {
            lifecycleScope.launch {
                HikHelper.setAutoShutter(it)
            }
        }
        cameraItemPopup.onShutterClickListener = {
            cameraItemPopup.isShutterSelect = true
            ToastUtils.showShort(R.string.app_Manual_Shutter)
            lifecycleScope.launch {
                HikHelper.shutter()
                cameraItemPopup.isShutterSelect = false
            }
        }
        cameraItemPopup.onAudioCLickListener = {
            if (saveSetBean.isRecordAudio) {
                saveSetBean.isRecordAudio = false
                cameraItemPopup.isAudioSelect = false
                videoRecord?.updateAudioState(false)
            } else {
                PermissionTool.requestRecordAudio(this) {
                    saveSetBean.isRecordAudio = true
                    cameraItemPopup.isAudioSelect = true
                    videoRecord?.updateAudioState(true)
                }
            }
        }
        cameraItemPopup.showAsUp(binding.menuSecondView)
        popupWindow = cameraItemPopup
    }

    private fun showBlendExtentPopup() {
        val seekBarPopup = SeekBarPopup(this, true)
        seekBarPopup.isRealTimeTrigger = true
        seekBarPopup.progress = saveSetBean.twoLightAlpha
        seekBarPopup.onValuePickListener = {
            saveSetBean.twoLightAlpha = it
            binding.cameraPreView.setCameraAlpha(it / 100f)
        }
        seekBarPopup.setOnDismissListener {
            binding.menuSecondView.setTwoLightSelected(TwoLightType.BLEND_EXTENT, false)
        }
        seekBarPopup.show(binding.thermalLay, !saveSetBean.isRotatePortrait())
        popupWindow = seekBarPopup
    }

    private fun showContrastPopup() {
        binding.menuSecondView.setSettingSelected(SettingType.CONTRAST, true)

        val maxContrast = 255
        val seekBarPopup = SeekBarPopup(this)
        seekBarPopup.progress = (saveSetBean.contrastValue * 100 / 255f).toInt()
        seekBarPopup.onValuePickListener = {
            saveSetBean.contrastValue = (it * maxContrast / 100f).toInt().coerceAtMost(maxContrast)
            lifecycleScope.launch {
                HikHelper.setContrast(it)
            }
        }
        seekBarPopup.setOnDismissListener {
            binding.menuSecondView.setSettingSelected(SettingType.CONTRAST, false)
        }
        seekBarPopup.show(binding.thermalLay, !saveSetBean.isRotatePortrait())
        popupWindow = seekBarPopup
    }

    private fun showSharpnessPopup() {
        binding.menuSecondView.setSettingSelected(SettingType.DETAIL, true)

        val maxSharpness = 4
        val seekBarPopup = SeekBarPopup(this)
        seekBarPopup.progress = (saveSetBean.ddeConfig * 100f / maxSharpness).toInt()
        seekBarPopup.onValuePickListener = {
            saveSetBean.ddeConfig = (it * maxSharpness / 100f).roundToInt()
            val newProgress = (saveSetBean.ddeConfig * 100f / maxSharpness).toInt()
            seekBarPopup.progress = newProgress
            lifecycleScope.launch {
                HikHelper.setEnhanceLevel(newProgress)
            }
        }
        seekBarPopup.setOnDismissListener {
            binding.menuSecondView.setSettingSelected(SettingType.DETAIL, false)
        }
        seekBarPopup.show(binding.thermalLay, !saveSetBean.isRotatePortrait())
        popupWindow = seekBarPopup
    }

    private fun showTempAlarmSetDialog() {
        val tempAlarmSetDialog = TempAlarmSetDialog(this, false)
        tempAlarmSetDialog.alarmBean = saveSetBean.alarmBean
        tempAlarmSetDialog.onSaveListener = {
            saveSetBean.alarmBean = it
            binding.menuSecondView.setSettingSelected(SettingType.ALARM, it.isOpen())
            binding.hikSurfaceView.alarmBean = it
            AlarmHelp.getInstance(this).updateData(
                if (it.isLowOpen) it.lowTemp else null,
                if (it.isHighOpen) it.highTemp else null,
                if (it.isRingtoneOpen) it.ringtoneType else null
            )
        }
        tempAlarmSetDialog.show()
    }
}