package com.topdon.module.thermal.ir.activity

import android.annotation.SuppressLint
import android.location.*
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ToastUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.bean.ContinuousBean
import com.topdon.lib.core.bean.WatermarkBean
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.TimeTool
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.ui.listener.SingleClickListener
import com.topdon.lib.core.utils.CommUtils
import com.topdon.module.thermal.ir.BuildConfig
import com.topdon.module.thermal.ir.R
import kotlinx.android.synthetic.main.activity_ir_camera_setting.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@Route(path = RouterConfig.IR_CAMERA_SETTING)
class IRCameraSettingActivity : BaseActivity() {

    companion object{
        const val KEY_PRODUCT_TYPE = "key_product_type"
    }

    private var locationManager: LocationManager? = null
    private var locationProvider: String? = null

    private var watermarkBean: WatermarkBean = SharedManager.watermarkBean
    private var continuousBean: ContinuousBean = SharedManager.continuousBean
    private var productName = ""

    private val permissionList = listOf(
        Permission.ACCESS_FINE_LOCATION,
        Permission.ACCESS_COARSE_LOCATION
    )

    override fun initContentView(): Int = R.layout.activity_ir_camera_setting

    override fun initView() {
        productName = intent.getStringExtra(KEY_PRODUCT_TYPE) ?: ""
        if (isTC007()){
            watermarkBean = SharedManager.wifiWatermarkBean
            continuousBean = SharedManager.continuousBean
        }else{
            watermarkBean = SharedManager.watermarkBean
            continuousBean = SharedManager.continuousBean
        }

        bar_pick_view_time.setProgressAndRefresh((continuousBean.continuaTime / 100).toInt())
        bar_pick_view_time.onStopTrackingTouch = { progress, _ ->
            continuousBean.continuaTime = progress.toLong() * 100
            SharedManager.continuousBean = continuousBean
        }
        bar_pick_view_time.valueFormatListener = {
            (it / 10).toString() + if (it % 10 == 0) "" else ("." + (it % 10).toString())
        }

        bar_pick_view_count.setProgressAndRefresh(continuousBean.count)
        bar_pick_view_count.onStopTrackingTouch = { progress, _ ->
            continuousBean.count = progress
            SharedManager.continuousBean = continuousBean
        }

        switch_time.isChecked = watermarkBean.isAddTime
        switch_watermark.isChecked = watermarkBean.isOpen
        switch_delay.isChecked = continuousBean.isOpen

        cl_delay_more.isVisible = continuousBean.isOpen
        cl_watermark_more.isVisible = watermarkBean.isOpen
        cl_show_ep.isVisible = watermarkBean.isOpen

        tv_time_show.text = TimeTool.getNowTime()
        tv_time_show.isVisible = watermarkBean.isAddTime

        tv_address.inputType = InputType.TYPE_NULL
        if (TextUtils.isEmpty(watermarkBean.address)){
            tv_address.visibility = View.GONE
        }else{
            tv_address.visibility = View.VISIBLE
            tv_address.text = watermarkBean.address
        }
        ed_title.setText(watermarkBean.title)
        ed_address.setText(watermarkBean.address)
        tv_title_show.text = watermarkBean.title
        switch_delay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                cl_delay_more.visibility = View.VISIBLE
            }else{
                cl_delay_more.visibility = View.GONE
            }
            continuousBean.isOpen = isChecked
            SharedManager.continuousBean = continuousBean
        }
        switch_watermark.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                cl_watermark_more.visibility = View.VISIBLE
                cl_show_ep.visibility = View.VISIBLE
            }else{
                cl_watermark_more.visibility = View.GONE
                cl_show_ep.visibility = View.GONE
            }
            watermarkBean.isOpen = isChecked
        }
        switch_time.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                tv_time_show.text = TimeTool.getNowTime()
                tv_time_show.visibility = View.VISIBLE
            }else{
                tv_time_show.visibility = View.GONE
            }
            watermarkBean.isAddTime = isChecked
        }
        ed_title.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {

                watermarkBean.title = ed_title.text.toString()
                tv_title_show.text = watermarkBean.title
            }
        })
        ed_address.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                watermarkBean.address = ed_address.text.toString()
                tv_address.text = watermarkBean.address
                if (!watermarkBean.address.isNullOrEmpty()){
                    tv_address.visibility = View.VISIBLE
                }else{
                    tv_address.visibility = View.GONE
                }
            }
        })
        img_location.setOnClickListener(object : SingleClickListener() {
            override fun onSingleClick() {
                checkStoragePermission()
            }
        })
        ly_auto.visibility = if (isTC007()) View.GONE else View.VISIBLE
    }

    fun isTC007() : Boolean
    {
        return productName.contains("TC007")
    }
    @SuppressLint("MissingPermission")
    private fun getLocation() : String? {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val providers = locationManager?.getProviders(true)
        locationProvider = if (providers!!.contains(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
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

    var locationListener: LocationListener = object : LocationListener {
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Toast.makeText(
                this@IRCameraSettingActivity, provider, Toast.LENGTH_SHORT
            ).show()
        }

        override fun onProviderEnabled(provider: String) {
            Toast.makeText(
                this@IRCameraSettingActivity, "GPS打开", Toast.LENGTH_SHORT
            ).show()
            getLocation()
        }

        override fun onProviderDisabled(provider: String) {
            Toast.makeText(
                this@IRCameraSettingActivity, "GPS关闭", Toast.LENGTH_SHORT
            ).show()
        }

        override fun onLocationChanged(location: Location) {
            if (location != null) {
                Toast.makeText(
                    this@IRCameraSettingActivity, location.longitude.toString() + " " +
                            location.latitude + "", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        val providers: List<String> = locationManager!!.getProviders(true)
        var bestLocation: Location? = null
        for (provider in providers) {
            val l: Location = locationManager!!.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                bestLocation = l
            }
        }
        return bestLocation
    }

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

    override fun onPause() {
        super.onPause()
        if (isTC007()){
            SharedManager.wifiWatermarkBean = watermarkBean
        }else{
            SharedManager.watermarkBean = watermarkBean
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun initData() {
    }

    private fun initLocationPermission() {
        XXPermissions.with(this@IRCameraSettingActivity)
            .permission(
                permissionList
            ).request(object :OnPermissionCallback{
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
                                ToastUtils.showShort(R.string.get_Location_failed)
                            }else{
                                watermarkBean.address = addressText as String
                                ed_address.setText(addressText)
                                tv_address.visibility = View.VISIBLE
                                tv_address.setText(addressText)
                            }
                        }
                    }else{
                        ToastUtils.showShort(R.string.scan_ble_tip_authorize)
                    }
                }
                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    if (never) {
                        if (BaseApplication.instance.isDomestic()){
                            ToastUtils.showShort(getString(R.string.app_location_content))
                        }else{
                            TipDialog.Builder(this@IRCameraSettingActivity)
                                .setTitleMessage(getString(R.string.app_tip))
                                .setMessage(getString(R.string.app_location_content))
                                .setPositiveListener(R.string.app_open){
                                    XXPermissions.startPermissionActivity(this@IRCameraSettingActivity, permissions);
                                }
                                .setCancelListener(R.string.app_cancel){
                                }
                                .setCanceled(true)
                                .create().show()
                        }
                    } else {
                        ToastUtils.showShort(R.string.scan_ble_tip_authorize)
                    }
                }

            })
    }

    private fun checkStoragePermission() {
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
}