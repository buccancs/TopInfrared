package com.topdon.module.thermal.ir.report.activity

import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.github.gzuliyujiang.wheelpicker.DatimePicker
import com.github.gzuliyujiang.wheelpicker.annotation.DateMode
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity
import com.github.gzuliyujiang.wheelpicker.entity.TimeEntity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.bean.event.ReportCreateEvent
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.utils.CommUtils
import com.topdon.module.thermal.ir.BuildConfig
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.report.bean.ImageTempBean
import com.topdon.module.thermal.ir.report.bean.ReportConditionBean
import com.topdon.module.thermal.ir.report.bean.ReportInfoBean
import com.topdon.module.thermal.ir.repository.ConfigRepository
import kotlinx.android.synthetic.main.activity_report_create_first.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*

/**
 * 生成报告第1步（共2步）.
 *
 * 需要传递
 * - 是否 TC007: [ExtraKeyConfig.IS_TC007] （环境温度、发射率等不同）
 * - 当前编辑的图片绝对路径: [ExtraKeyConfig.FILE_ABSOLUTE_PATH] （本界面不使用，透传）
 * - 当前编辑的图片点线面全图温度数据: [ExtraKeyConfig.IMAGE_TEMP_BEAN] （本界面不使用，透传）
 */
@Route(path = RouterConfig.REPORT_CREATE_FIRST)
class ReportCreateFirstActivity: BaseActivity(), View.OnClickListener {

    /**
     * 从上一界面传递过来的，当前是否为 TC007 设备类型.
     * true-TC007 false-其他插件式设备
     */
    private var isTC007 = false
    private var locationManager: LocationManager? = null
    private var locationProvider: String? = null

    override fun initContentView() = R.layout.activity_report_create_first

    private val permissionList = listOf(
        Permission.ACCESS_FINE_LOCATION,
        Permission.ACCESS_COARSE_LOCATION
    )

    @SuppressLint("SetTextI18n")
    override fun initView() {
        isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)

        et_report_name.setText("TC${TimeUtils.millis2String(System.currentTimeMillis(), "yyyyMMdd_HHmm")}")
        et_report_author.setText(SaveSettingUtil.reportAuthorName)
        tv_report_date.text = TimeUtils.millis2String(System.currentTimeMillis(), "yyyy.MM.dd HH:mm")
        et_report_watermark.setText(SaveSettingUtil.reportWatermarkText)
        tv_ambient_temperature.text = getString(R.string.thermal_config_environment) + "(${UnitTools.showUnit()})"
        tv_emissivity.text = getString(R.string.album_report_emissivity) + "(0~1)"

        et_report_author.addTextChangedListener {
            SaveSettingUtil.reportAuthorName = it?.toString() ?: ""
        }
        et_report_watermark.addTextChangedListener {
            SaveSettingUtil.reportWatermarkText = it?.toString() ?: ""
        }

        switch_report_author.setOnCheckedChangeListener { _, isChecked ->
            et_report_author.isVisible = isChecked
        }
        switch_report_date.setOnCheckedChangeListener { _, isChecked ->
            tv_report_date.isVisible = isChecked
        }
        switch_report_place.setOnCheckedChangeListener { _, isChecked ->
            et_report_place.isVisible = isChecked
        }
        switch_report_watermark.setOnCheckedChangeListener { _, isChecked ->
            et_report_watermark.isVisible = isChecked
        }
        switch_ambient_humidity.setOnCheckedChangeListener { _, isChecked ->
            tip_seek_humidity.isVisible = isChecked
        }
        switch_ambient_temperature.setOnCheckedChangeListener { _, isChecked ->
            et_ambient_temperature.isVisible = isChecked
        }
        switch_emissivity.setOnCheckedChangeListener { _, isChecked ->
            tip_seek_emissivity.isVisible = isChecked
        }
        switch_test_distance.setOnCheckedChangeListener { _, isChecked ->
            et_test_distance.isVisible = isChecked
        }
        tip_seek_humidity.progress = SaveSettingUtil.reportHumidity
        tip_seek_humidity.onStopTrackingTouch = {
            SaveSettingUtil.reportHumidity = it
        }
        tip_seek_humidity.valueFormatListener = {
            if (it % 10 == 0) "${it / 10}%" else "${it / 10}.${it % 10}%"
        }
        tip_seek_emissivity.valueFormatListener = {
            when (it) {
                0 -> "0"
                100 -> "1"
                else -> if (it < 10) "0.0$it" else "0.$it"
            }
        }

        tv_report_date.setOnClickListener(this)
        tv_preview.setOnClickListener(this)
        tv_next.setOnClickListener(this)
        img_location.setOnClickListener(this)

        readConfig()
    }

    @SuppressLint("SetTextI18n")
    private fun readConfig() {
        var environment = 30f //环境温度
        var distance = 0.25f  //测试距离
        var radiation = 0.95f //发射率
        val config = ConfigRepository.readConfig(isTC007)
        distance = config.distance
        radiation = config.radiation
        environment = config.environment
        et_ambient_temperature.setText(NumberTools.to01(UnitTools.showUnitValue(environment)))
        et_test_distance.setText(NumberTools.to02(distance) + "m")
        tip_seek_emissivity.progress = (radiation * 100).toInt()
    }

    override fun initData() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReportCreate(event: ReportCreateEvent) {
        finish()
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_report_date -> {//报告日期
                selectTime()
            }
            tv_preview -> {//预览
                val reportInfoBean = buildReportInfo()
                val reportConditionBean = buildReportCondition()
                ARouter.getInstance().build(RouterConfig.REPORT_PREVIEW_FIRST)
                    .withParcelable(ExtraKeyConfig.REPORT_INFO, reportInfoBean)
                    .withParcelable(ExtraKeyConfig.REPORT_CONDITION, reportConditionBean)
                    .navigation(this)
            }
            tv_next -> {//下一步
                val reportInfoBean = buildReportInfo()
                val reportConditionBean = buildReportCondition()
                val imageTempBean: ImageTempBean? = intent.getParcelableExtra(ExtraKeyConfig.IMAGE_TEMP_BEAN)
                ARouter.getInstance().build(RouterConfig.REPORT_CREATE_SECOND)
                    .withBoolean(ExtraKeyConfig.IS_TC007, isTC007)
                    .withString(ExtraKeyConfig.FILE_ABSOLUTE_PATH, intent.getStringExtra(ExtraKeyConfig.FILE_ABSOLUTE_PATH))
                    .withParcelable(ExtraKeyConfig.IMAGE_TEMP_BEAN, imageTempBean)
                    .withParcelable(ExtraKeyConfig.REPORT_INFO, reportInfoBean)
                    .withParcelable(ExtraKeyConfig.REPORT_CONDITION, reportConditionBean)
                    .navigation(this)
            }
            img_location -> {
                checkLocationPermission()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() : String? {
        //1.获取位置管理器
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        //2.获取位置提供器，GPS或是NetWork
        val providers = locationManager?.getProviders(true)
        locationProvider = if (providers!!.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            LocationManager.GPS_PROVIDER
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            LocationManager.NETWORK_PROVIDER
        } else {
            return null
        }
        var location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location == null){
            location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }
        return if (location == null){
            null
        }else{
            getAddress(location)

        }
    }

    //获取地址信息:城市、街道等信息
    private fun getAddress(location: Location?): String {
        var result: List<Address?>? = null
        try {
            if (location != null) {
                val gc = Geocoder(this, Locale.getDefault())
                result = gc.getFromLocation(
                    location.latitude,
                    location.longitude, 1
                )
                Log.v("TAG", "获取地址信息：$result")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var str = ""
        if (result!=null && result.isNotEmpty()){
            result?.get(0)?.let {
                str +=  getNullString(it.adminArea)
                if (TextUtils.isEmpty(it.subLocality) && !str.contains(getNullString(it.subAdminArea))){
                    str +=  getNullString(it.subAdminArea)
                }
                if (!str.contains(getNullString(it.locality))){
                    str +=  getNullString(it.locality)
                }
                if (!str.contains(getNullString(it.subLocality))){
                    str +=  getNullString(it.subLocality)
                }
            }
        }
        return str
    }

    private fun getNullString(str : String?):String{
        return if (str.isNullOrEmpty()){
            ""
        }else{
            str
        }
    }

    private fun buildReportInfo(): ReportInfoBean = ReportInfoBean(
        et_report_name.text.toString(),
        et_report_author.text.toString(),
        if (switch_report_author.isChecked && et_report_author.text.isNotEmpty()) 1 else 0,
        tv_report_date.text.toString(),
        if (switch_report_date.isChecked) 1 else 0,
        et_report_place.text.toString(),
        if (switch_report_place.isChecked && et_report_place.text.isNotEmpty()) 1 else 0,
        et_report_watermark.text.toString(),
        if (switch_report_watermark.isChecked && et_report_watermark.text.isNotEmpty()) 1 else 0
    )

    private fun buildReportCondition(): ReportConditionBean {
        val temperature = try {
            "${et_ambient_temperature.text.toString().toFloat()}${UnitTools.showUnit()}"
        } catch (ignore: NumberFormatException) {
            null
        }
        return ReportConditionBean(
            tip_seek_humidity.valueText,
            if (switch_ambient_humidity.isChecked) 1 else 0,
            temperature,
            if (switch_ambient_temperature.isChecked && temperature != null) 1 else 0,
            tip_seek_emissivity.valueText,
            if (switch_emissivity.isChecked) 1 else 0,
            et_test_distance.text.toString(),
            if (switch_test_distance.isChecked && et_test_distance.text.isNotEmpty()) 1 else 0
        )
    }



    /**
     * 当前设置的报告日期时间戳.
     */
    private var startTime = 0L
    /**
     * 显示时间拾取弹窗
     */
    private fun selectTime() {
        val picker = DatimePicker(this)
        picker.setTitle(R.string.chart_start_time)
        picker.setOnDatimePickedListener { year, month, day, hour, minute, second ->
            val timeStr = "$year-$month-$day $hour:$minute:$second"
            val pattern = "yyyy-MM-dd HH:mm:ss"
            val time: Long = SimpleDateFormat(pattern, Locale.getDefault()).parse(timeStr, ParsePosition(0)).time
            tv_report_date.text = TimeUtils.millis2String(time, "yyyy.MM.dd HH:mm")
            startTime = time
        }

        val startTimeEntity = DatimeEntity()
        startTimeEntity.date = DateEntity.target(2020, 1, 1)
        startTimeEntity.time = TimeEntity.target(0, 0, 0)

        val endTimeEntity = DatimeEntity.yearOnFuture(10)
        if (startTime == 0L) {
            //设置当前时间
            picker.wheelLayout.setRange(startTimeEntity, endTimeEntity, DatimeEntity.now())
        } else {
            //设置上一次选中时间
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = startTime
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hours = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            val seconds = calendar.get(Calendar.SECOND)
            val timeEntity = DatimeEntity()
            timeEntity.date = DateEntity.target(year, month, day)
            timeEntity.time = TimeEntity.target(hours, minutes, seconds)
            picker.wheelLayout.setRange(startTimeEntity, endTimeEntity, timeEntity)
        }
        picker.show()
    }

    private fun checkLocationPermission() {
        if (!XXPermissions.isGranted(this, permissionList)) {
            if (BaseApplication.instance.isDomestic()) {
                TipDialog.Builder(this)
                    .setMessage(getString(R.string.permission_request_location_app, CommUtils.getAppName()))
                    .setCancelListener(R.string.app_cancel)
                    .setPositiveListener(R.string.app_confirm) {
                        initLocationPermission()
                    }
                    .create().show()
            } else {
                initLocationPermission()
            }
        } else {
            initLocationPermission()
        }
    }

    private fun initLocationPermission() {
        //定位
        XXPermissions.with(this@ReportCreateFirstActivity)
            .permission(
                permissionList
            ).request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                    if (all){
                        showLoadingDialog(R.string.get_current_address)
                        lifecycleScope.launch{
                            var addressText : String ?= ""
                            withContext(Dispatchers.IO){
                                addressText =  getLocation()
                            }
                            dismissLoadingDialog()
                            if (addressText == null){
                                TipDialog.Builder(this@ReportCreateFirstActivity)
                                    .setMessage(R.string.get_Location_failed)
                                    .setPositiveListener(R.string.app_ok)
                                    .setCanceled(false)
                                    .create().show()
                            }else{
                                et_report_place.setText(addressText)
                            }
                        }
                    }else{
                        ToastUtils.showShort(R.string.scan_ble_tip_authorize)
                    }
                }
                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    if (never) {
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        if (BaseApplication.instance.isDomestic()){
                            ToastUtils.showShort(getString(R.string.app_location_content))
                            return
                        }
                        TipDialog.Builder(this@ReportCreateFirstActivity)
                            .setTitleMessage(getString(R.string.app_tip))
                            .setMessage(getString(R.string.app_location_content))
                            .setPositiveListener(R.string.app_open){
                                XXPermissions.startPermissionActivity(this@ReportCreateFirstActivity, permissions);
                            }
                            .setCancelListener(R.string.app_cancel){
                            }
                            .setCanceled(true)
                            .create().show()
                    } else {
                        ToastUtils.showShort(R.string.scan_ble_tip_authorize)
                    }
                }

            })
    }
}