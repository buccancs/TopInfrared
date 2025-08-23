package com.topdon.module.user.fragment

import android.os.Build
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ToastUtils
import com.elvishew.xlog.XLog
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.bean.event.TS004ResetEvent
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.common.WifiSaveSettingUtil
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.ConfirmSelectDialog
import com.topdon.lib.core.dialog.FirmwareUpDialog
import com.topdon.lib.core.ktbase.BaseFragment
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.http.tool.DownloadTool
import com.topdon.lib.core.repository.ProductBean
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.lib.core.socket.WebSocketProxy
import com.topdon.lib.core.tools.DeviceTools
import com.topdon.lib.core.viewmodel.FirmwareViewModel
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.module.user.R
import com.topdon.module.user.dialog.DownloadProDialog
import com.topdon.module.user.dialog.FirmwareInstallDialog
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.android.synthetic.main.layout_upgrade.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.DecimalFormat

@Route(path = RouterConfig.TC_MORE)
class MoreFragment : BaseFragment(), View.OnClickListener {

    private var isTC007 = false
    
    private val firmwareViewModel: FirmwareViewModel by viewModels()

    override fun initContentView() = R.layout.fragment_more

    override fun initView() {
        isTC007 = arguments?.getBoolean(ExtraKeyConfig.IS_TC007, false) ?: false

        setting_item_model.setOnClickListener(this)
        setting_item_correction.setOnClickListener(this)
        setting_item_dual.setOnClickListener(this)
        setting_item_unit.setOnClickListener(this)
        setting_version.setOnClickListener(this)
        setting_device_information.setOnClickListener(this)
        setting_reset.setOnClickListener(this)

        setting_reset.isVisible = false

        setting_version.isVisible = isTC007 && Build.VERSION.SDK_INT >= 29
        setting_device_information.isVisible = isTC007
        setting_item_dual.isVisible = !isTC007 && DeviceTools.isTC001PlusConnect()

        if (isTC007) {
            refresh07Connect(WebSocketProxy.getInstance().isTC007Connect())
        }

        setting_item_auto_show.isChecked = if (isTC007) SharedManager.isConnect07AutoOpen else SharedManager.isConnectAutoOpen
        setting_item_auto_show.setOnCheckedChangeListener { _, isChecked ->
            if (isTC007) {
                SharedManager.isConnect07AutoOpen = isChecked
            } else {
                SharedManager.isConnectAutoOpen = isChecked
            }
        }

        setting_item_config_select.isChecked = if (isTC007) WifiSaveSettingUtil.isSaveSetting else SaveSettingUtil.isSaveSetting
        setting_item_config_select.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                TipDialog.Builder(requireContext())
                    .setMessage(R.string.save_setting_tips)
                    .setPositiveListener(R.string.app_ok) {
                        if (isTC007){
                            WifiSaveSettingUtil.isSaveSetting = true
                        }else{
                            SaveSettingUtil.isSaveSetting = true
                        }
                    }
                    .setCancelListener(R.string.app_cancel) {
                        setting_item_config_select.isChecked = false
                    }
                    .setCanceled(false)
                    .create().show()
            } else {
                if (isTC007){
                    WifiSaveSettingUtil.reset()
                    WifiSaveSettingUtil.isSaveSetting = false
                }else{
                    SaveSettingUtil.reset()
                    SaveSettingUtil.isSaveSetting = false
                }
            }
        }

        firmwareViewModel.firmwareDataLD.observe(this) {
            tv_upgrade_point.isVisible = it != null
            dismissLoadingDialog()
            if (it == null) {
                ToastUtils.showShort(R.string.setting_firmware_update_latest_version)
            } else {
                showFirmwareUpDialog(it)
            }
        }
        firmwareViewModel.failLD.observe(this) {
            dismissLoadingDialog()
            TToast.shortToast(requireContext(), if (it) R.string.upgrade_bind_error else R.string.http_code_z5000)
            tv_upgrade_point.isVisible = false
        }
    }

    override fun initData() {
    }

    override fun connected() {
        setting_item_dual.isVisible = !isTC007 && DeviceTools.isTC001PlusConnect()
    }

    override fun disConnected() {
        setting_item_dual.isVisible = false
    }

    override fun onSocketConnected(isTS004: Boolean) {
        if (!isTS004 && isTC007) {
            refresh07Connect(true)
        }
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        if (!isTS004 && isTC007) {
            refresh07Connect(false)
        }
    }

    override fun onClick(v: View?) {
       when(v){
           setting_item_model -> {
               ARouter.getInstance().build(RouterConfig.IR_SETTING).withBoolean(ExtraKeyConfig.IS_TC007, isTC007).navigation(requireContext())
           }
           setting_item_dual->{
               ARouter.getInstance().build(RouterConfig.MANUAL_START).navigation(requireContext())
           }
           setting_item_unit -> {
               ARouter.getInstance().build(RouterConfig.UNIT).navigation(requireContext())
           }
           setting_item_correction->{
               ARouter.getInstance().build(RouterConfig.IR_CORRECTION).withBoolean(ExtraKeyConfig.IS_TC007, isTC007).navigation(requireContext())
           }
           setting_version -> {
                   val firmwareData = firmwareViewModel.firmwareDataLD.value
                   if (firmwareData != null) {
                       showFirmwareUpDialog(firmwareData)
                   } else {
                       XLog.i("TC007 固件升级 - 点击查询")
                       showLoadingDialog()
                       firmwareViewModel.queryFirmware(false)
                   }
           }
           setting_device_information -> {
               if (WebSocketProxy.getInstance().isTC007Connect()) {
                   ARouter.getInstance()
                       .build(RouterConfig.DEVICE_INFORMATION)
                       .withBoolean(ExtraKeyConfig.IS_TC007, true)
                       .navigation(requireContext())
               }
           }
           setting_reset -> {
               if (WebSocketProxy.getInstance().isTC007Connect()) {
                   restoreFactory()
               }
           }
       }
    }

    private fun refresh07Connect(isConnect: Boolean) {
        setting_device_information.isRightArrowVisible = isConnect
        setting_device_information.setRightTextId(if (isConnect) 0 else R.string.app_no_connect)
        setting_reset.isRightArrowVisible = isConnect
        setting_reset.setRightTextId(if (isConnect) 0 else R.string.app_no_connect)
        tv_right_text.isVisible = isConnect

        if (isConnect) {
            lifecycleScope.launch {
                val productBean: ProductBean? = TC007Repository.getProductInfo()
                if (productBean == null) {
                    TToast.shortToast(requireContext(), R.string.operation_failed_tips)
                } else {
                    item_setting_bottom_text.text = getString(R.string.setting_firmware_update_version) + "V" + productBean.getVersionStr()
                }
            }
        } else {
            item_setting_bottom_text.setText(R.string.setting_firmware_update_version)
        }
    }

    private fun showFirmwareUpDialog(firmwareData: FirmwareViewModel.FirmwareData) {
        val dialog = FirmwareUpDialog(requireContext())
        dialog.titleStr = "${getString(R.string.update_new_version)} ${firmwareData.version}"
        dialog.sizeStr = "${getString(R.string.detail_len)}: ${getFileSizeStr(firmwareData.size)}"
        dialog.contentStr = firmwareData.updateStr
        dialog.isShowRestartTips = true
        dialog.onConfirmClickListener = {
            installFirmware(FileConfig.getFirmwareFile(firmwareData.downUrl))
        }
        dialog.show()
    }

    private fun getFileSizeStr(size: Long): String = if (size < 1024) {
        "${size}B"
    } else if (size < 1024 * 1024) {
        DecimalFormat("#.0").format(size.toDouble() / 1024) + "KB"
    } else if (size < 1024 * 1024 * 1024) {
        DecimalFormat("#.0").format(size.toDouble() / 1024 / 1024) + "MB"
    } else {
        DecimalFormat("#.0").format(size.toDouble() / 1024 / 1024 / 1024) + "GB"
    }

    private fun downloadFirmware(firmwareData: FirmwareViewModel.FirmwareData) {
        lifecycleScope.launch {
            val progressDialog = DownloadProDialog(requireContext())
            progressDialog.show()

            val file = File(requireContext().getExternalFilesDir("firmware"), "TC007${firmwareData.version}.zip")
            val isSuccess = DownloadTool.download(firmwareData.downUrl, file) { current, total ->
                progressDialog.refreshProgress(current, total)
            }
            progressDialog.dismiss()
            if (isSuccess) {
                installFirmware(file)
            } else {
                showReDownloadDialog(firmwareData)
            }
        }
    }

    private fun installFirmware(file: File) {
        lifecycleScope.launch {
            XLog.d("TC007 固件升级 - 开始安装固件升级包")
            val installDialog = FirmwareInstallDialog(requireContext())
            installDialog.show()

            val isSuccess = TC007Repository.updateFirmware(file)
            installDialog.dismiss()
            if (isSuccess) {
                XLog.d("TC007 固件升级 - 固件升级包发送往 TC007 成功，即将断开连接")
                (requireActivity().application as BaseApplication).disconnectWebSocket()
                TipDialog.Builder(requireContext())
                    .setTitleMessage(getString(R.string.app_tip))
                    .setMessage(R.string.firmware_up_success)
                    .setPositiveListener(R.string.app_confirm) {
                        ARouter.getInstance().build(RouterConfig.MAIN).navigation(requireContext())
                        requireActivity().finish()
                    }
                    .setCancelListener(R.string.app_cancel) {

                    }
                    .create().show()
            } else {
                XLog.w("TC007 固件升级 - 固件升级包发送往 TC007 失败!")
                showReInstallDialog(file)
            }
        }
    }

    private fun showReInstallDialog(file: File) {
        val dialog = ConfirmSelectDialog(requireContext())
        dialog.setShowIcon(true)
        dialog.setTitleRes(R.string.ts004_install_tips)
        dialog.setCancelText(R.string.ts004_install_cancel)
        dialog.setConfirmText(R.string.ts004_install_continue)
        dialog.onConfirmClickListener = {
            installFirmware(file)
        }
        dialog.show()
    }

    private fun showReDownloadDialog(firmwareData: FirmwareViewModel.FirmwareData) {
        val dialog = ConfirmSelectDialog(requireContext())
        dialog.setShowIcon(true)
        dialog.setTitleRes(R.string.ts004_download_tips)
        dialog.setCancelText(R.string.ts004_download_cancel)
        dialog.setConfirmText(R.string.ts004_download_continue)
        dialog.onConfirmClickListener = {
            downloadFirmware(firmwareData)
        }
        dialog.show()
    }

    private fun restoreFactory() {
        TipDialog.Builder(requireContext())
            .setTitleMessage(getString(R.string.ts004_reset_tip1, "TC007"))
            .setMessage(getString(R.string.ts004_reset_tip2))
            .setPositiveListener(R.string.app_ok) {
                resetAll()
            }
            .setCancelListener(R.string.app_cancel) {
            }
            .setCanceled(true)
            .create().show()
    }

    private fun resetAll() {
        showLoadingDialog(R.string.ts004_reset_tip3)
        lifecycleScope.launch {
            val isSuccess = TC007Repository.resetToFactory()
            if (isSuccess) {
                XLog.d("TC007 恢复出厂设置成功，即将断开连接")
                TToast.shortToast(requireContext(), R.string.ts004_reset_tip4)
                (requireActivity().application as BaseApplication).disconnectWebSocket()
                EventBus.getDefault().post(TS004ResetEvent())
                ARouter.getInstance().build(RouterConfig.MAIN).navigation(requireContext())
                requireActivity().finish()
            } else {
                TToast.shortToast(requireContext(), R.string.operation_failed_tips)
            }
            delay(500)
            dismissLoadingDialog()
        }
    }
}
