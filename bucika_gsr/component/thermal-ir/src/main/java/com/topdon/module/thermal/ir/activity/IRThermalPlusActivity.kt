package com.topdon.module.thermal.ir.activity

import android.graphics.Bitmap
import android.view.SurfaceView
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ToastUtils
import com.energy.iruvc.sdkisp.LibIRProcess
import com.energy.iruvc.utils.CommonParams
import com.energy.iruvc.utils.DualCameraParams
import com.infisense.usbdual.Const
import com.infisense.usbir.utils.IRImageHelp
import com.infisense.usbir.utils.PseudocodeUtils
import com.infisense.usbir.view.TemperatureView
import com.topdon.lib.core.bean.CameraItemBean
import com.topdon.lib.core.common.ProductType.PRODUCT_NAME_TCP
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.tools.ToastTools
import com.topdon.menu.constant.TwoLightType
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.event.GalleryAddEvent
import com.topdon.module.thermal.ir.video.VideoRecordFFmpeg
import kotlinx.android.synthetic.main.activity_thermal_ir_night.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.nio.ByteBuffer


/**
 * 双光设备的界面
 * @author: CaiSongL
 * @date: 2024/1/17 17:47
 */
@Route(path = RouterConfig.IR_FRAME_PLUSH)
class IRThermalPlusActivity : BaseIRPlushActivity() {
    private val irImageHelp by lazy {
        IRImageHelp()
    }


    override fun initContentView() = R.layout.activity_ir_thermal_double

    override fun isDualIR(): Boolean {
        return true
    }

    override fun getSurfaceView(): SurfaceView {
        return dualTextureViewNativeCamera
    }

    override fun getTemperatureDualView(): TemperatureView {
        return temperatureView
    }

    override fun getProductName(): String {
        return PRODUCT_NAME_TCP
    }

    override fun initView() {
        super.initView()
//        findViewById<TextView>(R.id.toolbar_title)?.text = "双光设备"
        cameraView.visibility = View.GONE
        dualTextureViewNativeCamera?.visibility = View.VISIBLE
        thermal_steering_view.listener = { action, moveX ->
            setDisp(action, moveX)
        }

        when (SaveSettingUtil.fusionType) {
            SaveSettingUtil.FusionTypeLPYFusion -> {//双光1
                thermal_recycler_night?.twoLightType = TwoLightType.TWO_LIGHT_1
            }
            SaveSettingUtil.FusionTypeMeanFusion -> {//双光2
                thermal_recycler_night?.twoLightType = TwoLightType.TWO_LIGHT_2
            }
            SaveSettingUtil.FusionTypeIROnly -> {//单红外
                thermal_recycler_night?.twoLightType = TwoLightType.IR
            }
            SaveSettingUtil.FusionTypeVLOnly -> {//可见光
                thermal_recycler_night?.twoLightType = TwoLightType.LIGHT
            }
        }
    }




    /**
     * 执行双光配准.
     * @param action -1左移 1-右移 0确定
     * @param data 当前配准值
     */
    private fun setDisp(action: Int, data: Int) {
        if (action == -1 || action == 1) {
            // 移动
            lifecycleScope.launch(Dispatchers.IO) {
                dualDisp = data
                dualView?.dualUVCCamera!!.setDisp(data)
            }
        } else {
            // 确定
            val oemInfo = ByteArray(1024)
            ircmd?.oemRead(CommonParams.ProductType.P2, oemInfo)
            val dataStr = data.toString()
            System.arraycopy(dataStr.toByteArray(), 0, oemInfo, 194, dataStr.toByteArray().size)
            val result = ircmd?.oemWrite(CommonParams.ProductType.P2,oemInfo)
//            SharedManager.setIrDualDisp(dualDisp)
            if (result == 0){
                // 关闭控件
                if (thermal_steering_view.isVisible) {
                    thermal_steering_view.visibility = View.GONE
                    thermal_recycler_night.setTwoLightSelected(TwoLightType.CORRECT, false)
                }
            }else{
                ToastUtils.showShort(R.string.correction_fail)
            }

        }
    }

    override fun setTwoLight(twoLightType: TwoLightType, isSelected: Boolean) {
        when (twoLightType) {
            TwoLightType.TWO_LIGHT_1 -> {//双光1
                mCurrentFusionType = DualCameraParams.FusionType.LPYFusion
                SaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeLPYFusion
                setFusion(mCurrentFusionType)
            }
            TwoLightType.TWO_LIGHT_2 -> {//双光2
                mCurrentFusionType = DualCameraParams.FusionType.MeanFusion
                SaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeMeanFusion
                setFusion(mCurrentFusionType)
            }
            TwoLightType.IR -> {//单红外
                mCurrentFusionType = DualCameraParams.FusionType.IROnly
                SaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeIROnly
                setFusion(mCurrentFusionType)
                thermal_recycler_night.setTwoLightSelected(TwoLightType.CORRECT, false)
                thermal_steering_view.visibility = View.GONE
            }
            TwoLightType.LIGHT -> {//单可见光
                mCurrentFusionType = DualCameraParams.FusionType.VLOnly
                SaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeVLOnly
                setFusion(mCurrentFusionType)
                thermal_steering_view.visibility = View.GONE
                thermal_recycler_night.setTwoLightSelected(TwoLightType.CORRECT, false)
            }
            TwoLightType.CORRECT -> {//配准
                if (isSelected){
                    thermal_steering_view.visibility = View.VISIBLE
                    if (mCurrentFusionType != DualCameraParams.FusionType.LPYFusion && mCurrentFusionType != DualCameraParams.FusionType.MeanFusion) {
                        mCurrentFusionType = DualCameraParams.FusionType.LPYFusion
                        thermal_recycler_night.twoLightType = TwoLightType.TWO_LIGHT_1
                        SaveSettingUtil.fusionType = SaveSettingUtil.FusionTypeLPYFusion
                        setFusion(DualCameraParams.FusionType.LPYFusion)
                    }
                }else{
                    thermal_steering_view.visibility = View.GONE
                }
            }
            else -> {
                super.setTwoLight(twoLightType, isSelected)
            }
        }
    }

    override fun getCameraViewBitmap(): Bitmap {
        if (imageEditBytes.size != dualView?.frameIrAndTempData?.size) {
            imageEditBytes = ByteArray(dualView!!.frameIrAndTempData.size)
        }
        System.arraycopy(dualView!!.frameIrAndTempData, 0, imageEditBytes, 0, imageEditBytes.size)
        return dualView?.scaledBitmap!!
    }


    override fun setTemperatureViewType() {
        temperatureView.productType = Const.TYPE_IR_DUAL
        cameraView.productType = Const.TYPE_IR_DUAL
    }

    override fun startUSB(isRestart: Boolean, isBadFrames: Boolean) {

    }

    override fun setPColor(code: Int) {
        pseudoColorMode = code
        temperature_seekbar.setPseudocode(pseudoColorMode)
        /**
         * 设置伪彩【set pseudocolor】
         * 固件机芯实现(部分伪彩为预留,设置后可能无效果)
         */
//        dualView?.dualUVCCamera?.setPseudocolor(PseudocodeUtils.changeDualPseudocodeModelByOld(pseudoColorMode))
        SaveSettingUtil.pseudoColorMode = pseudoColorMode
        thermal_recycler_night.setPseudoColor(code)
    }

    override fun startISP() {
        setCustomPseudoColorList(
            customPseudoBean.getColorList(),
            customPseudoBean.getPlaceList(),
            customPseudoBean.isUseGray,
            customPseudoBean.maxTemp, customPseudoBean.minTemp
        )
    }

    override fun setCustomPseudoColorList(
        colorList: IntArray?,
        places: FloatArray?,
        isUseGray: Boolean,
        customMaxTemp: Float,
        customMinTemp: Float
    ) {
        irImageHelp.setColorList(colorList, places, isUseGray,customMaxTemp,customMinTemp)
    }

    override fun setRotate(rotateInt: Int) {
        super.setRotate(rotateInt)
        runOnUiThread {
            thermal_steering_view.rotationIR = rotateInt
        }
        //双光的旋转角度不同
        when (rotateInt) {
            0 -> dualView?.dualUVCCamera?.setImageRotate(DualCameraParams.TypeLoadParameters.ROTATE_90)
            90 -> dualView?.dualUVCCamera?.setImageRotate(DualCameraParams.TypeLoadParameters.ROTATE_180)
            180 -> dualView?.dualUVCCamera?.setImageRotate(DualCameraParams.TypeLoadParameters.ROTATE_270)
            270 -> dualView?.dualUVCCamera?.setImageRotate(DualCameraParams.TypeLoadParameters.ROTATE_0)
        }
    }

    override fun onIrFrame(irFrame: ByteArray?): ByteArray {
        System.arraycopy(irFrame, 0, preIrData, 0, preIrData.size)
        System.arraycopy(irFrame, preIrData.size, preTempData, 0, preTempData.size)
        if (irImageHelp.getColorList() != null){
            //转成灰度图进行自定义伪彩融合处理
            LibIRProcess.convertYuyvMapToARGBPseudocolor(
                preIrData, (Const.IR_WIDTH * Const.IR_HEIGHT).toLong(),
                CommonParams.PseudoColorType.PSEUDO_1, preIrARGBData
            )
        }else{
            LibIRProcess.convertYuyvMapToARGBPseudocolor(
                preIrData, (Const.IR_WIDTH * Const.IR_HEIGHT).toLong(),
                PseudocodeUtils.changePseudocodeModeByOld(pseudoColorMode), preIrARGBData
            )
        }
        irImageHelp.customPseudoColor(preIrARGBData,preTempData,Const.IR_WIDTH,Const.IR_HEIGHT)
        //等温尺处理,展示伪彩的温度范围内信息
        irImageHelp.setPseudoColorMaxMin(
            preIrARGBData, preTempData, editMaxValue,
            editMinValue, Const.IR_WIDTH,Const.IR_HEIGHT)
        //温度监控的轮廓检测，双光的原始图像不管旋转如何，原始数据都不变，（也就是宽高256*192）
       val tempData =irImageHelp.contourDetection(alarmBean,
           preIrARGBData,preTempData,
            Const.IR_HEIGHT,Const.IR_WIDTH)
        System.arraycopy(tempData,0, preIrARGBData, 0, preIrARGBData.size)
        return preIrARGBData
    }

    override fun irStop() {
        try {
            configJob?.cancel()
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
                    thermal_recycler_night.refreshImg()
                }
            }
        } catch (_: Exception) {
        }finally {
            ircmd?.onDestroy()
            ircmd = null
        }
    }

    /**
     * 初始化视频采集组件
     */
    override fun initVideoRecordFFmpeg() {
        videoRecord = VideoRecordFFmpeg(
            cameraView,
            cameraPreview,
            temperatureView,
            curChooseTabPos == 1,
            cl_seek_bar,
            temp_bg,
            compassView, dualView,
            carView = lay_car_detect_prompt
        )
    }

    override fun irStart() {
        if (!isrun) {
            tv_type_ind.isVisible = false
            startUSB(false,false)
            startISP()
            isrun = true
            //恢复配置
            configParam()
            thermal_recycler_night.updateCameraModel()
            initIRConfig()
        }
    }
    override fun setDispViewData(dualDisp: Int) {
        thermal_steering_view.moveX = dualDisp
    }
    override fun autoConfig() {
        lifecycleScope.launch(Dispatchers.IO) {
            dualView?.let {
                if (!it.auto_gain_switch) {
                    switchAutoGain(true)
                    ToastTools.showShort(R.string.auto_open)
                }
                gainSelChar = CameraItemBean.TYPE_TMP_ZD
            }
        }
        dismissCameraLoading()
        thermal_recycler_night.setTempLevel(CameraItemBean.TYPE_TMP_ZD)
    }
    override fun switchAutoGain(boolean: Boolean) {
        dualView?.auto_gain_switch = boolean
    }

}
