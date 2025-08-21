package com.topdon.tc001

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.SizeUtils
import com.elvishew.xlog.XLog
import com.energy.iruvc.ircmd.IRCMDType
import com.energy.iruvc.ircmd.IRUtils
import com.energy.iruvc.utils.CommonParams
import com.example.thermal_lite.IrConst
import com.example.thermal_lite.util.CommonUtil
import com.example.thermal_lite.util.IRTool
import com.infisense.usbir.utils.OpencvTools
import com.topdon.lib.core.utils.BitmapUtils
import com.infisense.usbir.utils.PseudocodeUtils.changePseudocodeModeByOld
import com.infisense.usbir.view.ITsTempListener
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.bean.event.ReportCreateEvent
import com.topdon.lib.core.common.ProductType.PRODUCT_NAME_TC001LITE
import com.topdon.lib.core.common.ProductType.PRODUCT_NAME_TS
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.ScreenTool
import com.topdon.lib.core.tools.TimeTool
import com.topdon.lib.core.tools.ToastTools
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.core.tools.UnitTools.showToCValue
import com.topdon.lib.core.tools.UnitTools.showUnitValue
import com.topdon.lib.core.utils.ImageUtils
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.dialog.TipWaterMarkDialog
import com.topdon.lib.core.utils.Constants.IS_REPORT_FIRST
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.lib.ui.widget.seekbar.OnRangeChangedListener
import com.topdon.lib.ui.widget.seekbar.RangeSeekBar
import com.topdon.libcom.dialog.ColorPickDialog
import com.topdon.libcom.dialog.TempAlarmSetDialog
import com.topdon.lms.sdk.LMS.mContext
import com.topdon.menu.constant.FenceType
import com.topdon.menu.constant.SettingType
import com.topdon.module.thermal.ir.event.GalleryAddEvent
import com.topdon.module.thermal.ir.event.ImageGalleryEvent
import com.topdon.module.thermal.ir.frame.FrameStruct
import com.topdon.module.thermal.ir.frame.FrameTool
import com.topdon.module.thermal.ir.frame.ImageParams
import com.topdon.module.thermal.ir.report.bean.ImageTempBean
import com.topdon.module.thermal.ir.view.TemperatureBaseView.Mode
import com.topdon.module.thermal.ir.viewmodel.IRGalleryEditViewModel
import com.topdon.pseudo.activity.PseudoSetActivity
import com.topdon.pseudo.bean.CustomPseudoBean
import kotlinx.android.synthetic.main.activity_ir_gallery_edit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 图片二次编辑
 */
@Route(path = RouterConfig.IR_GALLERY_EDIT)
class IRGalleryEditActivity : BaseActivity(), View.OnClickListener, ITsTempListener {


    private var isShowC: Boolean = false

    /**
     * 从上一界面传递过来的，当前是否为 TC007 设备类型.
     * true-TC007 false-其他插件式设备
     */
    private var isTC007 = false

    private val imageWidth = 256
    private val imageHeight = 192
    private val viewModel: IRGalleryEditViewModel by viewModels()
    private var filePath = ""

    //    private var mCapital = ByteArray(1024)
    private var mFrame = ByteArray(192 * 256 * 4)
    private val frameTool by lazy { FrameTool() }

    //图像参数
    private var pseudocodeMode = 3
    private var leftValue = 0f
    private var rightValue = 10000f
    private var max = 10000f
    private var min = 0f
    private var rotate = ImageParams.ROTATE_270
    private var struct: FrameStruct = FrameStruct() //首部信息
    private var ts_data_H: ByteArray? = null
    private var ts_data_L: ByteArray? = null

    override fun initContentView(): Int = R.layout.activity_ir_gallery_edit

    override fun initView() {
        initIntent()
        initUI()
        initListener()
        initRecycler()
        initObserve()
    }

    private fun initIntent() {
        lifecycleScope.launch{
            ts_data_H = CommonUtil.getAssetData(this@IRGalleryEditActivity, "ts/TS001_H.bin")
            ts_data_L = CommonUtil.getAssetData(this@IRGalleryEditActivity, "ts/TS001_L.bin")

            if (BaseApplication.instance.tau_data_H == null){
                BaseApplication.instance.tau_data_H = CommonUtil.getAssetData(mContext, IrConst.TAU_HIGH_GAIN_ASSET_PATH)
            }
            if (BaseApplication.instance.tau_data_L == null){
                BaseApplication.instance.tau_data_L = CommonUtil.getAssetData(mContext, IrConst.TAU_LOW_GAIN_ASSET_PATH)
            }
        }
        if (intent.hasExtra(ExtraKeyConfig.FILE_ABSOLUTE_PATH)) {
            filePath = intent.getStringExtra(ExtraKeyConfig.FILE_ABSOLUTE_PATH)!!
        }
        isReportPick = intent.getBooleanExtra(ExtraKeyConfig.IS_PICK_REPORT_IMG, false)
        isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)

        edit_recycler_second.fenceSelectType = FenceType.DEL
        temperature_view.isShowName = isReportPick
        temperature_view.mode = Mode.CLEAR
        temperature_view.setITsTempListener(this)
        if (isTC007){
            temperature_seekbar?.progressHeight = SizeUtils.dp2px(10f)
        }
    }

    private fun initObserve() {
        viewModel.resultLiveData.observe(this) {
//            System.arraycopy(it.capital, 0, mCapital, 0, it.capital.size)
            System.arraycopy(it.frame, 0, mFrame, 0, it.frame.size)
            showImage(it.capital, it.frame)
        }
    }

    override fun initData() {
        viewModel.initData(filePath)

        edit_recycler_first.isBarSelect = true
        color_bar_view.isVisible = true
    }

    private fun initListener() {
        temperature_iv_lock.setOnClickListener(this)
        temperature_iv_input.setOnClickListener(this)
    }

    private fun setRotate(rotate: ImageParams) {
        if (rotate == ImageParams.ROTATE_270 || rotate == ImageParams.ROTATE_90) {
            temperature_view.setImageSize(imageHeight, imageWidth)
        } else {
            temperature_view.setImageSize(imageWidth, imageHeight)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun showImage(capital: ByteArray, frame: ByteArray) {
        lifecycleScope.launch {
            frameTool.read(frame)
            struct = FrameStruct(capital)
            frameTool.initStruct(struct)
            isShowC = SharedManager.getTemperature() == 1
            rotate = frameTool.initRotate()
            pseudocodeMode = struct.pseudo
            setRotate(rotate)
            delay(200)
            updateImage(
                frameTool.getScrPseudoColorScaledBitmap(
                    changePseudocodeModeByOld(pseudocodeMode),
                    rotate = rotate,
                    customPseudoBean = struct.customPseudoBean,
                    maxTemperature = tempCorrect(frameTool.getSrcTemp().maxTemperature),
                    minTemperature = tempCorrect(frameTool.getSrcTemp().minTemperature),
                    isAmplify = struct.isAmplify
                )
            )

            val tempResult = frameTool.getSrcTemp()
            if (!struct.customPseudoBean.isUseCustomPseudo) {
                struct.customPseudoBean.maxTemp = tempCorrect(tempResult.maxTemperature)
                struct.customPseudoBean.minTemp = tempCorrect(tempResult.minTemperature)
                edit_recycler_second.setPseudoColor(pseudocodeMode)
            }
//        伪彩条默认处于打开状态
//        color_bar_view.isVisible = struct.isShowPseudoBar
//        adapter.enPseudoColorBar(struct.isShowPseudoBar)

            edit_recycler_second.setSettingSelected(SettingType.ALARM, struct.alarmBean.isHighOpen || struct.alarmBean.isLowOpen)
            edit_recycler_second.setSettingSelected(SettingType.WATERMARK, struct.watermarkBean.isOpen)
            edit_recycler_second.setSettingSelected(SettingType.FONT,
                struct.textColor != 0xffffffff.toInt() || struct.textSize != SizeUtils.sp2px(14f))
            temperature_view.textColor = struct.textColor
            temperature_view.tempTextSize = struct.textSize
            temperature_view.setData(frameTool.getTempBytes(rotate = rotate))
            updateTemperatureSeekBar(false, R.drawable.svg_pseudo_bar_lock, "lock")//加锁
            temperature_seekbar.setPseudocode(pseudocodeMode)
            temperature_seekbar.setOnRangeChangedListener(object : OnRangeChangedListener {
                override fun onRangeChanged(
                    view: RangeSeekBar?,
                    leftValue: Float,
                    rightValue: Float,
                    isFromUser: Boolean,
                    tempMode: Int
                ) {
                    if (leftValue < rightValue) {
                        max = rightValue
                        min = leftValue
                    } else {
                        max = leftValue
                        min = rightValue
                    }
                    if (!struct.customPseudoBean.isUseCustomPseudo) {
                        updateImage(
                            frameTool.getScrPseudoColorScaledBitmap(
                                changePseudocodeModeByOld(pseudocodeMode),
                                showToCValue(max),
                                showToCValue(min),
                                rotate,
                                struct.customPseudoBean,
                                maxTemperature = tempCorrect(frameTool.getSrcTemp().maxTemperature),
                                minTemperature = tempCorrect(frameTool.getSrcTemp().minTemperature),
                                struct.isAmplify
                            )
                        )
                    }
                }

                override fun onStartTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                    //调整开始
                }

                override fun onStopTrackingTouch(view: RangeSeekBar?, isLeft: Boolean) {
                    //调整结束
                }

            })
            temperature_seekbar.setIndicatorTextStringFormat("%.1f")
            if (struct.customPseudoBean.isUseCustomPseudo) {
                tv_temp_content.visibility = View.VISIBLE
                tv_temp_content.text = "Max:${UnitTools.showC(tempCorrect(tempResult.maxTemperature),isShowC)}\nMin:${UnitTools.showC(tempCorrect(tempResult.minTemperature),isShowC)}"
                rightValue = showUnitValue(struct.customPseudoBean.maxTemp,isShowC)
                leftValue = showUnitValue(struct.customPseudoBean.minTemp,isShowC)
                temperature_iv_input.setImageResource(R.drawable.ir_model)
                temperature_iv_lock.visibility = View.INVISIBLE
                temperature_seekbar.setColorList(struct.customPseudoBean.getColorList(struct.isTC007())?.reversedArray())
                temperature_seekbar.setPlaces(struct.customPseudoBean.getPlaceList())
            } else {
                tv_temp_content.visibility = View.GONE
                tv_temp_content.text = "Max:${UnitTools.showC(tempCorrect(tempResult.maxTemperature),isShowC)}\nMin:${UnitTools.showC(tempCorrect(tempResult.minTemperature),isShowC)}"
                rightValue = showUnitValue(tempCorrect(tempResult.maxTemperature),isShowC)
                leftValue = showUnitValue(tempCorrect(tempResult.minTemperature),isShowC)
                temperature_iv_input.setImageResource(R.drawable.ic_color_edit)
                temperature_iv_lock.visibility = View.VISIBLE
            }
            temperature_seekbar.setRange(leftValue, rightValue, 0.1f) //初始温度范围
            temperature_seekbar.setProgress(leftValue, rightValue) //初始位置
            if (ScreenTool.isIPad(this@IRGalleryEditActivity)) {
                color_bar_view.setPadding(0, SizeUtils.dp2px(40f), 0, SizeUtils.dp2px(40f))
            }
        }
    }

    /**
     * 更新图像
     */
    private fun updateImage(bitmap: Bitmap?) {
        bitmap?.let {
            val params = ir_image_view.layoutParams as ConstraintLayout.LayoutParams
            params.dimensionRatio = "${bitmap.width}:${bitmap.height}"
            runOnUiThread {
                ir_image_view.layoutParams = params
            }
            if (struct.watermarkBean.isOpen) {
                val width = ScreenUtil.getScreenWidth(this)
                val height = (width * bitmap.height / bitmap.width.toFloat()).toInt()
                ir_image_view.setImageBitmap(
                    BitmapUtils.drawCenterLable(
                        Bitmap.createScaledBitmap(it, width, height, true),
                        struct.watermarkBean.title,
                        struct.watermarkBean.address,
                        if (struct.watermarkBean.isAddTime) TimeTool.getNowTime() else "",
                        if (temperature_seekbar.isVisible){
                            temperature_seekbar.measuredWidth
                        }else{
                            0
                        }
                    )
                )
            } else {
                ir_image_view.setImageBitmap(it)
            }
        }
    }

    /**
     * 一级菜单
     */
    private fun initRecycler() {
        edit_recycler_first.onTabClickListener = {
            when (it) {
                0 -> edit_recycler_second.selectPosition(1) //点线面
                1 -> edit_recycler_second.selectPosition(3) //伪彩颜色
                2 -> edit_recycler_second.selectPosition(4) //设置
            }
        }
        edit_recycler_first.onBarClickListener = {
            color_bar_view.isVisible = it
        }

        edit_recycler_second.onFenceListener = { fenceType, isSelected ->
            when (fenceType) {
                FenceType.POINT -> temperature_view.mode = Mode.POINT
                FenceType.LINE -> temperature_view.mode = Mode.LINE
                FenceType.RECT -> temperature_view.mode = Mode.RECT
                FenceType.DEL -> temperature_view.mode = Mode.CLEAR
                FenceType.FULL -> temperature_view.isShowFull = isSelected
                FenceType.TREND -> {
                    //2D编辑没有趋势图
                }
            }
        }
        edit_recycler_second.onColorListener = { _, it, _ ->
            if (struct.customPseudoBean.isUseCustomPseudo) {
                TipDialog.Builder(this)
                    .setTitleMessage(getString(R.string.app_tip))
                    .setMessage(R.string.tip_change_pseudo_mode)
                    .setPositiveListener(R.string.app_yes) {
                        struct.customPseudoBean.isUseCustomPseudo = false
                        setDefLimit()
                        setPColor(it)
                        updateImageAndSeekbarColorList(struct.customPseudoBean)
                    }.setCancelListener(R.string.app_no) {

                    }
                    .create().show()
            } else {
                setPColor(it)
            }
        }
        edit_recycler_second.onSettingListener = { type, _ ->
            setSettingValue(type)
        }
    }

    /**
     * 最高最低温复原
     */
    private fun setDefLimit() {
        val tempResult = frameTool.getSrcTemp()
        rightValue = showUnitValue(tempCorrect(tempResult.maxTemperature),isShowC)
        leftValue = showUnitValue(tempCorrect(tempResult.minTemperature),isShowC)
        temperature_seekbar.setRange(leftValue, rightValue, 0.1f) //初始温度范围
        temperature_seekbar.setProgress(leftValue, rightValue) //初始位置
    }

    //设置伪彩
    private fun setPColor(code: Int) {
        pseudocodeMode = code
        temperature_seekbar.setPseudocode(pseudocodeMode)
        updateImage(
            frameTool.getScrPseudoColorScaledBitmap(
                changePseudocodeModeByOld(pseudocodeMode),
                showToCValue(max),
                showToCValue(min),
                rotate,
                struct.customPseudoBean,
                maxTemperature = tempCorrect(frameTool.getSrcTemp().maxTemperature),
                minTemperature = tempCorrect(frameTool.getSrcTemp().minTemperature),
                struct.isAmplify
            )
        )
        temperature_seekbar.setColorList(struct.customPseudoBean.getColorList(struct.isTC007())?.reversedArray())
        temperature_seekbar.setPlaces(struct.customPseudoBean.getPlaceList())
        edit_recycler_second.setPseudoColor(code)
    }

    private var tempAlarmSetDialog: TempAlarmSetDialog? = null
    private fun setSettingValue(type: SettingType) {
        when (type) {
            SettingType.ALARM -> {
                // 预警
                if (tempAlarmSetDialog == null) {
                    tempAlarmSetDialog = TempAlarmSetDialog(this, true)
                    tempAlarmSetDialog?.onSaveListener = {
                        edit_recycler_second.setSettingSelected(SettingType.ALARM, it.isHighOpen || it.isLowOpen)
                        struct.alarmBean = it
                        frameTool.initStruct(struct)
                        updateImage(
                            frameTool.getScrPseudoColorScaledBitmap(
                                changePseudocodeModeByOld(pseudocodeMode),
                                showToCValue(max),
                                showToCValue(min),
                                rotate,
                                struct.customPseudoBean,
                                maxTemperature = tempCorrect(frameTool.getSrcTemp().maxTemperature),
                                minTemperature = tempCorrect(frameTool.getSrcTemp().minTemperature),
                                struct.isAmplify
                            )
                        )
                    }
                }
                tempAlarmSetDialog?.alarmBean = struct.alarmBean
                tempAlarmSetDialog?.show()
            }
            SettingType.FONT -> {//字体颜色
                val colorPickDialog = ColorPickDialog(this, temperature_view.textColor,temperature_view.tempTextSize)
                colorPickDialog.onPickListener = { it: Int, textSize: Int ->
                    temperature_view?.textColor = it
                    struct.textSize = SizeUtils.sp2px(textSize.toFloat())
                    temperature_view?.tempTextSize = SizeUtils.sp2px(textSize.toFloat())
                    edit_recycler_second.setSettingSelected(SettingType.FONT,
                        it != 0xffffffff.toInt() || textSize != SizeUtils.sp2px(14f))
                }
                colorPickDialog.show()
            }
            SettingType.WATERMARK -> { //水印
                TipWaterMarkDialog.Builder(this, struct.watermarkBean)
                    .setCancelListener {
                        struct.watermarkBean = it
                        frameTool.initStruct(struct)
                        edit_recycler_second.setSettingSelected(SettingType.WATERMARK, it.isOpen)
                        updateImage(
                            frameTool.getScrPseudoColorScaledBitmap(
                                changePseudocodeModeByOld(pseudocodeMode),
                                showToCValue(max),
                                showToCValue(min),
                                rotate,
                                struct.customPseudoBean,
                                maxTemperature = tempCorrect(frameTool.getSrcTemp().maxTemperature),
                                minTemperature = tempCorrect(frameTool.getSrcTemp().minTemperature),
                                struct.isAmplify
                            )
                        )
                    }
                    .create().show()
            }
            else -> {
                //其他设置选项 2D 编辑没有
            }
        }
    }

    private fun updateTemperatureSeekBar(isEnabled: Boolean, resource: Int, content: String) {
        temperature_seekbar.isEnabled = isEnabled
        temperature_iv_lock.setImageResource(resource)
        temperature_iv_lock.contentDescription = content
        if (isEnabled) {
            temperature_seekbar.leftSeekBar.indicatorBackgroundColor = 0xffe17606.toInt()
            temperature_seekbar.rightSeekBar.indicatorBackgroundColor = 0xffe17606.toInt()
        } else {
            temperature_seekbar.leftSeekBar.indicatorBackgroundColor = 0
            temperature_seekbar.rightSeekBar.indicatorBackgroundColor = 0
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            temperature_iv_lock -> {
                if (temperature_iv_lock.contentDescription == "lock") {
                    updateTemperatureSeekBar(
                        true,
                        R.drawable.svg_pseudo_bar_unlock,
                        "unlock"
                    )//解锁
                } else {
                    setDefLimit()
                    updateTemperatureSeekBar(false, R.drawable.svg_pseudo_bar_lock, "lock")//加锁
                }
            }
            temperature_iv_input -> {
                val intent = Intent(this, PseudoSetActivity::class.java)
                intent.putExtra(ExtraKeyConfig.IS_TC007, isTC007)
                intent.putExtra(ExtraKeyConfig.CUSTOM_PSEUDO_BEAN, struct.customPseudoBean)
                pseudoSetResult.launch(intent)
            }
        }
    }

    private val pseudoSetResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val tmp = it.data?.getParcelableExtra(ExtraKeyConfig.CUSTOM_PSEUDO_BEAN)
                    ?: CustomPseudoBean()
                updateImageAndSeekbarColorList(tmp)
                temperature_seekbar.setColorList(tmp.getColorList(struct.isTC007())?.reversedArray())
                temperature_seekbar.setPlaces(tmp.getPlaceList())
//            customPseudoBean.saveToShared()

            }
        }

    //更新自定义伪彩的颜色的属性值
    private fun updateImageAndSeekbarColorList(customPseudoBean: CustomPseudoBean?) {
        customPseudoBean?.let {
            updateImage(
                frameTool.getScrPseudoColorScaledBitmap(
                    changePseudocodeModeByOld(
                        pseudocodeMode
                    ), rotate = rotate, customPseudoBean = customPseudoBean,
                    maxTemperature = tempCorrect(frameTool.getSrcTemp().maxTemperature),
                    minTemperature = tempCorrect(frameTool.getSrcTemp().minTemperature),
                    isAmplify = struct.isAmplify
                )
            )
            if (it.isUseCustomPseudo) {
                temperature_iv_lock.visibility = View.INVISIBLE
                tv_temp_content.visibility = View.VISIBLE
                updateTemperatureSeekBar(false, R.drawable.svg_pseudo_bar_lock, "lock")//加锁
                temperature_seekbar.setRangeAndPro(
                    UnitTools.showUnitValue(it.minTemp,isShowC),
                    UnitTools.showUnitValue(it.maxTemp,isShowC), UnitTools.showUnitValue(it.minTemp,isShowC),
                    UnitTools.showUnitValue(it.maxTemp,isShowC)
                )
                edit_recycler_second.setPseudoColor(-1)
                temperature_iv_input.setImageResource(R.drawable.ir_model)
            } else {
                temperature_iv_lock.visibility = View.VISIBLE
                if (struct.customPseudoBean.isUseCustomPseudo) {
                    setDefLimit()
                }
                tv_temp_content.visibility = View.GONE
                edit_recycler_second.setPseudoColor(pseudocodeMode)
                temperature_iv_input.setImageResource(R.drawable.ic_color_edit)
            }
            struct.customPseudoBean = customPseudoBean
            temperature_seekbar.setColorList(customPseudoBean.getColorList(struct.isTC007())?.reversedArray())
            temperature_seekbar.setPlaces(customPseudoBean.getPlaceList())
        }
//        tv_temp_content.visibility = View.VISIBLE
    }

    /**
     * 从上一界面传递过来的，是否从生成报告拾取图片中跳转过来.
     */
    private var isReportPick = false
    private fun initUI() {
        isReportPick = intent.getBooleanExtra(ExtraKeyConfig.IS_PICK_REPORT_IMG, false)
        title_view.setLeftClickListener {
            if (isReportPick) {
                finish()
            } else {
                saveImage()
            }
        }
        title_view.setRightText(if (isReportPick) R.string.app_next else R.string.person_save)
        title_view.setRightClickListener {
            if (!isReportPick) {
                updateIconSave()
            } else {
                showLoadingDialog()
                lifecycleScope.launch(Dispatchers.IO) {
                    // 获取展示图像信息的图层数据
                    var irBitmap = if (struct.isAmplify){
                        //超分四倍使用原始图像继续超分一次
                        OpencvTools.supImageFourExToBitmap(frameTool.getBaseBitmap(rotate))
                    }else{
                        ir_image_view.drawToBitmap()
                    }
                    if (temperature_view.mode != Mode.CLEAR) {
                        // 获取温度图层的数据，包括点线框，温度值等，重新合成bitmap
                        irBitmap = BitmapUtils.mergeBitmap(irBitmap, temperature_view.drawToBitmap(), 0, 0)
                    }
                    // 合并伪彩条
                    if (color_bar_view.visibility == View.VISIBLE) {
                        irBitmap = BitmapUtils.mergeBitmap(irBitmap, color_bar_view.drawToBitmap(), 0, 0)
                    }
                    // 保存图片
                    val fileAbsolutePath = ImageUtils.saveToCache(this@IRGalleryEditActivity, irBitmap)
                    launch(Dispatchers.Main) {
                        dismissLoadingDialog()
                        if (intent.getBooleanExtra(IS_REPORT_FIRST, true)) {
                            ARouter.getInstance().build(RouterConfig.REPORT_CREATE_FIRST)
                                .withBoolean(ExtraKeyConfig.IS_TC007, isTC007)
                                .withString(ExtraKeyConfig.FILE_ABSOLUTE_PATH, fileAbsolutePath)
                                .withParcelable(ExtraKeyConfig.IMAGE_TEMP_BEAN, buildImageTempBean())
                                .navigation(this@IRGalleryEditActivity)
                        } else {
                            ARouter.getInstance().build(RouterConfig.REPORT_CREATE_SECOND)
                                .withBoolean(ExtraKeyConfig.IS_TC007, isTC007)
                                .withString(ExtraKeyConfig.FILE_ABSOLUTE_PATH, fileAbsolutePath)
                                .withParcelable(ExtraKeyConfig.IMAGE_TEMP_BEAN, buildImageTempBean())
                                .withParcelable(ExtraKeyConfig.REPORT_INFO, intent.getParcelableExtra(ExtraKeyConfig.REPORT_INFO))
                                .withParcelable(ExtraKeyConfig.REPORT_CONDITION, intent.getParcelableExtra(ExtraKeyConfig.REPORT_CONDITION))
                                .withParcelableArrayList(ExtraKeyConfig.REPORT_IR_LIST, intent.getParcelableArrayListExtra(ExtraKeyConfig.REPORT_IR_LIST))
                                .navigation(this@IRGalleryEditActivity)
                        }
                    }
                }
            }
        }
        edit_recycler_second.selectPosition(-1)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReportCreate(event: ReportCreateEvent) {
        if (isReportPick) {
            finish()
        }
    }

    private fun keepOneDigit(float: Float) = String.format(Locale.ENGLISH, "%.1f", float)

    private fun buildImageTempBean(): ImageTempBean {
        var full: ImageTempBean.TempBean? = null
        if (temperature_view.isShowFull) {
            temperature_view.fullInfo?.let {
                val max = keepOneDigit(tempCorrect(it.maxTemperature))
                val min = keepOneDigit(tempCorrect(it.minTemperature))
                full = ImageTempBean.TempBean(max, min)
            }
        }

        val pointList = arrayListOf<ImageTempBean.TempBean>()
        for (temp in temperature_view.tempListData.pointTemps) {
            if (temp.type != -99) {
                pointList.add(ImageTempBean.TempBean(keepOneDigit(tempCorrect(temp.maxTemperature))))
            }
        }

        val lineList = arrayListOf<ImageTempBean.TempBean>()
        for (temp in temperature_view.tempListData.lineTemps) {
            if (temp.type != -99) {
                val max = keepOneDigit(tempCorrect(temp.maxTemperature))
                val min = keepOneDigit(tempCorrect(temp.minTemperature))
                val average = keepOneDigit(temp.averageTemperature)
                lineList.add(ImageTempBean.TempBean(max, min, average))
            }
        }

        val rectList = arrayListOf<ImageTempBean.TempBean>()
        for (temp in temperature_view.tempListData.rectangleTemps) {
            if (temp.type != -99) {
                val max = keepOneDigit(tempCorrect(temp.maxTemperature))
                val min = keepOneDigit(tempCorrect(temp.minTemperature))
                val average = keepOneDigit(temp.averageTemperature)
                rectList.add(ImageTempBean.TempBean(max, min, average))
            }
        }

        return ImageTempBean(full, pointList, lineList, rectList)
    }


    private fun saveImage() {
        TipDialog.Builder(this)
            .setTitleMessage(getString(R.string.app_tip))
            .setMessage(R.string.app_save_image)
            .setPositiveListener(R.string.app_yes) {
                updateIconSave()
            }.setCancelListener(R.string.app_no) {
                finish()
            }
            .create().show()

    }

    private fun updateIconSave() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 获取展示图像信息的图层数据
            var irBitmap = if (struct.isAmplify){
                //超分四倍使用原始图像继续超分一次
                OpencvTools.supImageFourExToBitmap(frameTool.getBaseBitmap(rotate))
            }else{
                ir_image_view.drawToBitmap()
            }
            if (temperature_view.mode != Mode.CLEAR) {
                // 获取温度图层的数据，包括点线框，温度值等，重新合成bitmap
                irBitmap = BitmapUtils.mergeBitmap(irBitmap, temperature_view.drawToBitmap(), 0, 0)
            }
            // 合并伪彩条
            if (color_bar_view.visibility == View.VISIBLE) {
                irBitmap = BitmapUtils.mergeBitmap(irBitmap, color_bar_view.drawToBitmap(), 0, 0)
            }
            // 保存图片
            var name: String
            irBitmap.let {
                name = ImageUtils.save(bitmap = it,isTC007)
            }
            ImageUtils.saveFrame(bs = mFrame, capital = getCapital(), name = name)
            ToastTools.showShort(R.string.tip_photo_saved)
            EventBus.getDefault().post(GalleryAddEvent())
            MediaScannerConnection.scanFile(
                this@IRGalleryEditActivity,
                arrayOf(FileConfig.lineGalleryDir),
                null,
                null
            )
            EventBus.getDefault().post(ImageGalleryEvent())
            finish()
        }
    }

    private fun getCapital(): ByteArray {
        val capital: ByteArray? //首部
        capital = FrameStruct.toCode(
            name = struct.name,
            width = struct.width,
            height = struct.height,
            rotate = struct.rotate,
            pseudo = pseudocodeMode,
            initRotate = struct.initRotate,
            correctRotate = struct.correctRotate,
            customPseudoBean = struct.customPseudoBean,
            isShowPseudoBar = color_bar_view.isVisible,
            textColor = temperature_view.textColor,
            watermarkBean = struct.watermarkBean,
            alarmBean = struct.alarmBean,
            gainStatus = struct.gainStatus,
            textSize = struct.textSize,
            struct.environment,
            struct.distance,
            struct.radiation,
            false
        )
        return capital
    }

    override fun onBackPressed() {
        lifecycleScope.launch {
            if (isReportPick) {
                finish()
            } else {
                saveImage()
            }
        }
    }

    override fun tempCorrectByTs(temp: Float?): Float {
        var tmp = temp
        try {
            tmp = tempCorrect(temp!!)
        } catch (e: Exception) {
            XLog.i("温度校正失败: ${e.message}")
        }
        return tmp!!
    }

    /**
     * 单点修正过程
     */
    private fun tempCorrect(
        temp: Float): Float {
        var newTemp = temp
        try {
            if (struct == null || struct.distance <= 0 || struct.radiation <= 0){
                return temp
            }
            val paramsArray = floatArrayOf(
                temp, struct.radiation, struct.environment,
                struct.environment, struct.distance, 0.8f
            )
            if (struct.name.startsWith(PRODUCT_NAME_TS)) {
                if (ts_data_H == null || ts_data_L == null) return temp
                newTemp = IRUtils.temperatureCorrection(
                    IRCMDType.USB_IR_256_384,
                    CommonParams.ProductType.WN256_ADVANCED,
                    paramsArray[0],
                    ts_data_H,
                    ts_data_L,
                    paramsArray[1],
                    paramsArray[2],
                    paramsArray[3],
                    paramsArray[4],
                    paramsArray[5],
                    0,
                    if (struct.gainStatus == 1){
                        CommonParams.GainStatus.HIGH_GAIN
                    }else{
                        CommonParams.GainStatus.LOW_GAIN
                    }
                )
            }else if (struct.name.startsWith(PRODUCT_NAME_TC001LITE)){
                //lite的模组
                if (BaseApplication.instance.tau_data_H == null || BaseApplication.instance.tau_data_L == null) return temp
                newTemp = IRTool.temperatureCorrection(temp,paramsArray,BaseApplication.instance.tau_data_H!!,
                    BaseApplication.instance.tau_data_L!!,struct.gainStatus)
            }
        }catch (e : Exception){
            XLog.e("$TAG:tempCorrect-${e.message}")
        }finally {
            return newTemp
        }
    }
}