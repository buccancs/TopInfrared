package com.topdon.house.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.UriUtils
import com.bumptech.glide.Glide
import com.github.gzuliyujiang.wheelpicker.DatimePicker
import com.github.gzuliyujiang.wheelpicker.StrArrayPicker
import com.github.gzuliyujiang.wheelpicker.YearPicker
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity
import com.github.gzuliyujiang.wheelpicker.entity.TimeEntity
import com.topdon.house.R
import com.topdon.house.dialog.ImagePickFromDialog
import com.topdon.house.event.HouseDetectAddEvent
import com.topdon.house.event.HouseDetectEditEvent
import com.topdon.house.viewmodel.DetectViewModel
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.HouseDetect
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.PermissionTool
import com.topdon.lib.core.tools.SpanBuilder
import com.topdon.lib.core.utils.LocationUtil
import com.topdon.lms.sdk.weiget.TToast
import kotlinx.android.synthetic.main.activity_detect_add.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetectAddActivity : BaseActivity(), View.OnClickListener {
    private val viewModel: DetectViewModel  by viewModels()

    private var editId: Long = 0
    
    private var houseDetect = HouseDetect()

    private var inputDetectTime: Long? = null

    override fun initContentView(): Int = R.layout.activity_detect_add

    override fun initView() {
        editId = intent.getLongExtra(ExtraKeyConfig.DETECT_ID, 0)

        title_view.setTitleText(if (editId > 0) R.string.edit_detection_report else R.string.add_detection_report)
        title_view.setLeftClickListener { showExitTipsDialog() }

        tv_create_report.setText(if (editId > 0) R.string.person_save else R.string.create_report)

        viewModel.detectLD.observe(this) {
            houseDetect = it ?: return@observe
            inputDetectTime = houseDetect.detectTime
            et_detect_name.setText(houseDetect.name)
            et_inspector_name.setText(houseDetect.inspectorName)
            tv_detect_time.text = TimeUtils.millis2String(houseDetect.detectTime, "yyyy-MM-dd HH:mm")
            et_house_address.setText(houseDetect.address)

            Glide.with(iv_house_image).load(houseDetect.imagePath).into(iv_house_image)
            iv_house_image_camera.isVisible = false
            tv_house_image_camera.isVisible = false

            tv_house_year.text = houseDetect.year?.toString() ?: ""

            et_house_space.setText(houseDetect.houseSpace)
            tv_house_space_unit.text = resources.getStringArray(R.array.area)[houseDetect.houseSpaceUnit]

            et_cost.setText(houseDetect.cost)
            tv_cost_unit.text = resources.getStringArray(R.array.currency)[houseDetect.costUnit]
        }

        if (editId > 0) {
            viewModel.queryById(editId)
        } else {
            houseDetect.houseSpaceUnit = SharedManager.houseSpaceUnit
            houseDetect.costUnit = SharedManager.costUnit
            tv_house_space_unit.text = resources.getStringArray(R.array.area)[houseDetect.houseSpaceUnit]
            tv_cost_unit.text = resources.getStringArray(R.array.currency)[houseDetect.costUnit]
        }

        tv_detect_time.setOnClickListener(this)
        iv_address_location.setOnClickListener(this)
        iv_house_image.setOnClickListener(this)
        tv_house_year.setOnClickListener(this)
        tv_house_space_unit.setOnClickListener(this)
        tv_cost_unit.setOnClickListener(this)
        tv_create_report.setOnClickListener(this)

        tv_detect_name_title.text = SpanBuilder().appendColor("*", 0xffff4848.toInt()).append(getString(R.string.album_report_name))
        tv_inspector_name_title.text = SpanBuilder().appendColor("*", 0xffff4848.toInt()).append(getString(R.string.inspector_name))
        tv_detect_time_title.text = SpanBuilder().appendColor("*", 0xffff4848.toInt()).append(getString(R.string.detect_time))
        tv_house_address_title.text = SpanBuilder().appendColor("*", 0xffff4848.toInt()).append(getString(R.string.house_detail_address))
        tv_house_image_title.text = SpanBuilder().appendColor("*", 0xffff4848.toInt()).append(getString(R.string.house_image))

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitTipsDialog()
            }
        })
    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_detect_time -> {
                showDetectTimeDialog()
            }
            iv_address_location -> {
                getLocation()
            }
            iv_house_image -> {
                ImagePickFromDialog(this)
                    .setSelectListener {
                        if (it == 0) {
                            PermissionTool.requestImageRead(this) {
                                galleryPickResult.launch("image
    @SuppressLint("MissingPermission")
    private fun getLocation() {
        PermissionTool.requestLocation(this) {
            lifecycleScope.launch {
                showLoadingDialog(R.string.get_current_address)
                val addressText = LocationUtil.getLastLocationStr(this@DetectAddActivity)
                dismissLoadingDialog()
                if (addressText == null) {
                    TToast.shortToast(this@DetectAddActivity, R.string.get_Location_failed)
                } else {
                    houseDetect.address = addressText
                    et_house_address.setText(addressText)
                }
            }
        }
    }

    private val galleryPickResult = registerForActivityResult(ActivityResultContracts.GetContent()) {
        val srcFile: File? = UriUtils.uri2File(it)
        if (srcFile != null) {
            val copyFile = FileConfig.getDetectImageDir(this, "Cover${System.currentTimeMillis()}.png")
            FileUtils.copy(srcFile, copyFile)
            houseDetect.imagePath = copyFile.absolutePath
            Glide.with(iv_house_image).load(copyFile.absolutePath).into(iv_house_image)
            iv_house_image_camera.isVisible = false
            tv_house_image_camera.isVisible = false
        }
    }

    private val lightPhotoResult = registerForActivityResult(TakePhotoResult()) {
        if (it != null) {
            houseDetect.imagePath = it.absolutePath
            Glide.with(iv_house_image).load(it.absolutePath).into(iv_house_image)
            iv_house_image_camera.isVisible = false
            tv_house_image_camera.isVisible = false
        }
    }

    private fun showExitTipsDialog() {
        TipDialog.Builder(this)
            .setMessage(R.string.diy_tip_save)
            .setPositiveListener(R.string.app_exit) {
                finish()
            }
            .setCancelListener(R.string.app_cancel)
            .create().show()
    }

    private fun showDetectTimeDialog() {
        val picker = DatimePicker(this)
        picker.setTitle(R.string.detect_time)
        picker.setOnDatimePickedListener { year, month, day, hour, minute, second ->
            val timeStr = "$year-$month-$day $hour:$minute:$second"
            val pattern = "yyyy-MM-dd HH:mm:ss"
            val time: Long = SimpleDateFormat(pattern, Locale.getDefault()).parse(timeStr, ParsePosition(0))?.time ?: 0
            tv_detect_time.text = TimeUtils.millis2String(time, "yyyy-MM-dd HH:mm")
            inputDetectTime = time
            houseDetect.detectTime = time
        }

        val calendar = Calendar.getInstance()
        val nowYear = calendar.get(Calendar.YEAR)

        val startTimeEntity = DatimeEntity()
        startTimeEntity.date = DateEntity.target(nowYear - 1000, 1, 1)

        val endTimeEntity = DatimeEntity.yearOnFuture(1000)

        val timeEntity = if (inputDetectTime == null) DatimeEntity.now() else DatimeEntity()
        if (inputDetectTime != null) {
            calendar.timeInMillis = inputDetectTime ?: 0
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val hours = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)
            val seconds = calendar.get(Calendar.SECOND)
            timeEntity.date = DateEntity.target(year, month, day)
            timeEntity.time = TimeEntity.target(hours, minutes, seconds)
        }

        picker.wheelLayout.setRange(startTimeEntity, endTimeEntity, timeEntity)
        picker.show()
    }
}