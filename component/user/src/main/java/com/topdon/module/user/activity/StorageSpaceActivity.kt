package com.topdon.module.user.activity

import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.elvishew.xlog.XLog
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.repository.TS004Repository
import com.topdon.lms.sdk.utils.TLog
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.module.user.R
import com.topdon.module.user.bean.ColorsBean
import kotlinx.android.synthetic.main.activity_storage_space.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@Route(path = RouterConfig.STORAGE_SPACE)
class StorageSpaceActivity : BaseActivity(), View.OnClickListener {
    companion object {
        private fun formatFileSize(fileSize: Long): String = if (fileSize == 0L) {
            "0"
        } else if (fileSize < 1024) {
            DecimalFormat("#.0").format(fileSize.toDouble())
        } else if (fileSize < 1048576) {
            DecimalFormat("#.0").format(fileSize.toDouble() / 1024)
        } else if (fileSize < 1073741824) {
            DecimalFormat("#.0").format(fileSize.toDouble() / 1048576)
        } else {
            DecimalFormat("#.0").format(fileSize.toDouble() / 1073741824)
        }

        private fun getUnit(fileSize: Long): String = if (fileSize < 1024) {
            "B"
        } else if (fileSize < 1048576) {
            "KB"
        } else if (fileSize < 1073741824) {
            "MB"
        } else {
            "GB"
        }
    }
    
    override fun initContentView() = R.layout.activity_storage_space

    override fun initView() {
        tv_format_storage.setOnClickListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun initData() {
        lifecycleScope.launch {
            val freeSpaceBean = TS004Repository.getFreeSpace()
            if (freeSpaceBean == null) {
                TToast.shortToast(this@StorageSpaceActivity, R.string.operation_failed_tips)
            } else {
                TLog.d("ts004", "║ response :$freeSpaceBean")

                tv_progress_value.text = "${(freeSpaceBean.hasUseSize() * 100.0 / freeSpaceBean.total).toInt().coerceAtLeast(1)}"

                tv_used_value.text = formatFileSize(freeSpaceBean.hasUseSize())
                tv_used.text = getUnit(freeSpaceBean.hasUseSize())
                tv_total_value.text = " / " + formatFileSize(freeSpaceBean.total)
                tv_total.text = getUnit(freeSpaceBean.total)

                list_storage_photo.setRightText(formatFileSize(freeSpaceBean.image_size) + getUnit(freeSpaceBean.image_size))
                list_storage_video.setRightText(formatFileSize(freeSpaceBean.video_size) + getUnit(freeSpaceBean.video_size))
                list_storage_system.setRightText(formatFileSize(freeSpaceBean.system) + getUnit(freeSpaceBean.system))

                val systemPercent = (freeSpaceBean.system * 100.0 / freeSpaceBean.total).toInt().coerceAtLeast(1).coerceAtMost(98)
                val imagePercent = (freeSpaceBean.image_size * 100.0 / freeSpaceBean.total).toInt().coerceAtLeast(1).coerceAtMost(98)
                val videoPercent = (freeSpaceBean.video_size * 100.0 / freeSpaceBean.total).toInt().coerceAtLeast(1).coerceAtMost(98)
                val colorList = arrayListOf<ColorsBean>()
                colorList.add(ColorsBean(0, systemPercent, 0xff8d98a9.toInt()))
                colorList.add(ColorsBean(systemPercent, systemPercent + imagePercent, 0xff019dff.toInt()))
                colorList.add(ColorsBean(systemPercent + imagePercent, systemPercent + imagePercent + videoPercent, 0xff70e297.toInt()))
                custom_view_progress.setSegmentPart(colorList)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            tv_format_storage -> {
                TipDialog.Builder(this@StorageSpaceActivity)
                    .setTitleMessage(getString(R.string.more_storage_reset))
                    .setMessage(getString(R.string.more_storage_reset1))
                    .setShowRestartTops(true)
                    .setPositiveListener(R.string.app_ok) {
                        showLoadingDialog()
                        lifecycleScope.launch {
                            val isSuccess = TS004Repository.getFormatStorage()
                            if (isSuccess) {
                                XLog.d("TS004 格式化存储成功，即将断开连接")
                                (application as BaseApplication).disconnectWebSocket()
                                ARouter.getInstance().build(RouterConfig.MAIN).navigation(this@StorageSpaceActivity)
                                finish()
                            } else {
                                delay(500)
                                dismissLoadingDialog()
                                TToast.shortToast(this@StorageSpaceActivity, R.string.operation_failed_tips)
                            }
                        }
                    }
                    .setCancelListener(R.string.app_cancel) {
                    }
                    .setCanceled(true)
                    .create().show()
            }
        }
    }
}
