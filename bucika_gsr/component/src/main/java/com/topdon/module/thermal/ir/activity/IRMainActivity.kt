package com.topdon.module.thermal.ir.activity

import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.AppUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.bean.event.PDFEvent
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.repository.GalleryRepository.DirType
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.lib.core.socket.WebSocketProxy
import com.topdon.lib.core.tools.DeviceTools
import com.topdon.lib.core.utils.CommUtils
import com.topdon.lib.core.utils.NetWorkUtils
import com.topdon.lib.core.utils.PermissionUtils
import com.topdon.lms.sdk.LMS
import com.topdon.module.thermal.ir.BuildConfig
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.dialog.HomeGuideDialog
import com.topdon.module.thermal.ir.fragment.IRGalleryTabFragment
import com.topdon.module.thermal.ir.fragment.IRThermalFragment
import com.topdon.module.thermal.ir.fragment.AbilityFragment
import com.topdon.module.thermal.ir.fragment.PDFListFragment
import kotlinx.android.synthetic.main.activity_ir_main.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

@Route(path = RouterConfig.IR_MAIN)
class IRMainActivity : BaseActivity(), View.OnClickListener {

    private var isTC007 = false

    override fun initContentView(): Int = R.layout.activity_ir_main

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initView()
    }

    override fun initView() {
        isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)

        view_page.offscreenPageLimit = 5
        view_page.isUserInputEnabled = false
        view_page.adapter = ViewPagerAdapter(this, isTC007)
        view_page.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                refreshTabSelect(position)
            }
        })
        view_page.setCurrentItem(2, false)

        cl_icon_monitor.setOnClickListener(this)
        cl_icon_gallery.setOnClickListener(this)
        view_main_thermal.setOnClickListener(this)
        cl_icon_report.setOnClickListener(this)
        cl_icon_mine.setOnClickListener(this)

        showGuideDialog()
    }

    override fun onResume() {
        super.onResume()
        if (isTC007) {
            if (WebSocketProxy.getInstance().isTC007Connect()) {
                NetWorkUtils.switchNetwork(false)
                iv_main_bg.setImageResource(R.drawable.ic_ir_main_bg_connect)
                lifecycleScope.launch {
                    TC007Repository.syncTime()
                }
                if (SharedManager.isConnect07AutoOpen) {
                    ARouter.getInstance().build(RouterConfig.IR_THERMAL_07).navigation(this)
                }
            } else {
                iv_main_bg.setImageResource(R.drawable.ic_ir_main_bg_disconnect)
            }
        } else {
            if (DeviceTools.isConnect(isAutoRequest = false)) {
                iv_main_bg.setImageResource(R.drawable.ic_ir_main_bg_connect)
            } else {
                iv_main_bg.setImageResource(R.drawable.ic_ir_main_bg_disconnect)
            }
        }
    }

    override fun initData() {
    }

    override fun connected() {
        if (!isTC007) {
            iv_main_bg.setImageResource(R.drawable.ic_ir_main_bg_connect)
        }
    }

    override fun disConnected() {
        if (!isTC007) {
            iv_main_bg.setImageResource(R.drawable.ic_ir_main_bg_disconnect)
        }
    }

    override fun onSocketConnected(isTS004: Boolean) {
        if (!isTS004 && isTC007) {
            iv_main_bg.setImageResource(R.drawable.ic_ir_main_bg_connect)
        }
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        if (!isTS004 && isTC007) {
            iv_main_bg.setImageResource(R.drawable.ic_ir_main_bg_disconnect)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            cl_icon_monitor -> {
                view_page.setCurrentItem(0, false)
            }
            cl_icon_gallery -> {
                checkStoragePermission()
            }
            view_main_thermal -> {
                view_page.setCurrentItem(2, false)
            }
            cl_icon_report -> {
                if (LMS.getInstance().isLogin) {
                    view_page.setCurrentItem(3, false)
                } else {
                    LMS.getInstance().activityLogin(null) {
                        if (it) {
                            view_page.setCurrentItem(3, false)
                            EventBus.getDefault().post(PDFEvent())
                        }
                    }
                }
            }
            cl_icon_mine -> {
                view_page.setCurrentItem(4, false)
            }
        }
    }

    private fun refreshTabSelect(index: Int) {
        iv_icon_monitor.isSelected = false
        tv_icon_monitor.isSelected = false
        iv_icon_gallery.isSelected = false
        tv_icon_gallery.isSelected = false
        iv_icon_report.isSelected = false
        tv_icon_report.isSelected = false
        iv_icon_mine.isSelected = false
        tv_icon_mine.isSelected = false

        when (index) {
            0 -> {
                iv_icon_monitor.isSelected = true
                tv_icon_monitor.isSelected = true
            }
            1 -> {
                iv_icon_gallery.isSelected = true
                tv_icon_gallery.isSelected = true
            }
            3 -> {
                iv_icon_report.isSelected = true
                tv_icon_report.isSelected = true
            }
            4 -> {
                iv_icon_mine.isSelected = true
                tv_icon_mine.isSelected = true
            }
        }
    }

    private fun showGuideDialog() {
        if (SharedManager.homeGuideStep == 0) {
            return
        }

        when (SharedManager.homeGuideStep) {
            1 -> view_page.setCurrentItem(0, false)
            2 -> view_page.setCurrentItem(4, false)
            3 -> view_page.setCurrentItem(2, false)
        }

        val guideDialog = HomeGuideDialog(this, SharedManager.homeGuideStep)
        guideDialog.onNextClickListener = {
            when (it) {
                1 -> {
                    view_page.setCurrentItem(4, false)
                    if (Build.VERSION.SDK_INT < 31) {
                        lifecycleScope.launch {
                            delay(100)
                            guideDialog.blurBg(cl_root)
                        }
                    }
                    SharedManager.homeGuideStep = 2
                }
                2 -> {
                    view_page.setCurrentItem(2, false)
                    if (Build.VERSION.SDK_INT < 31) {
                        lifecycleScope.launch {
                            delay(100)
                            guideDialog.blurBg(cl_root)
                        }
                    }
                    SharedManager.homeGuideStep = 3
                }
                3 -> {
                    SharedManager.homeGuideStep = 0
                }
            }
        }
        guideDialog.onSkinClickListener = {
            SharedManager.homeGuideStep = 0
        }
        guideDialog.setOnDismissListener {
            if (Build.VERSION.SDK_INT >= 31) {
                window?.decorView?.setRenderEffect(null)
            }
        }
        guideDialog.show()

        if (Build.VERSION.SDK_INT >= 31) {
            window?.decorView?.setRenderEffect(RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.MIRROR))
        } else {
            lifecycleScope.launch {
                delay(100)
                guideDialog.blurBg(cl_root)
            }
        }
    }

    private fun checkStoragePermission() {
        val permissionList: List<String> =
            if (this.applicationInfo.targetSdkVersion >= 34){
                listOf(
                    Permission.READ_MEDIA_VIDEO,
                    Permission.READ_MEDIA_IMAGES,
                    Permission.WRITE_EXTERNAL_STORAGE,
                )
            } else if (this.applicationInfo.targetSdkVersion >= 34){
                listOf(
                    Permission.READ_MEDIA_VIDEO,
                    Permission.READ_MEDIA_IMAGES,
                    Permission.WRITE_EXTERNAL_STORAGE,
                )
            } else if (this.applicationInfo.targetSdkVersion == 33) {
            listOf(
                Permission.READ_MEDIA_VIDEO,
                Permission.READ_MEDIA_IMAGES,
                Permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            listOf(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
        }

        if (!XXPermissions.isGranted(this, permissionList)) {
            if (BaseApplication.instance.isDomestic()) {
                TipDialog.Builder(this)
                    .setMessage(getString(R.string.permission_request_storage_app, CommUtils.getAppName()))
                    .setCancelListener(R.string.app_cancel)
                    .setPositiveListener(R.string.app_confirm) {
                        initStoragePermission(permissionList)
                    }
                    .create().show()
            } else {
                initStoragePermission(permissionList)
            }
        } else {
            initStoragePermission(permissionList)
        }
    }

    private fun initStoragePermission(permissionList: List<String>) {
        if (PermissionUtils.isVisualUser()){
            view_page.setCurrentItem(1, false)
            return
        }
        XXPermissions.with(this)
            .permission(permissionList)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        view_page.setCurrentItem(1, false)
                    }
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        TipDialog.Builder(this@IRMainActivity)
                            .setTitleMessage(getString(R.string.app_tip))
                            .setMessage(getString(R.string.app_album_content))
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

    private class ViewPagerAdapter(activity: FragmentActivity, val isTC007: Boolean) : FragmentStateAdapter(activity) {
        override fun getItemCount() = 5

        override fun createFragment(position: Int): Fragment {
            if (position == 1) {
                return IRGalleryTabFragment().apply {
                    arguments = Bundle().also {
                        val dirType = if (isTC007) DirType.TC007.ordinal else DirType.LINE.ordinal
                        it.putBoolean(ExtraKeyConfig.CAN_SWITCH_DIR, false)
                        it.putBoolean(ExtraKeyConfig.HAS_BACK_ICON, false)
                        it.putInt(ExtraKeyConfig.DIR_TYPE, dirType)
                    }
                }
            } else {
                val fragment = when (position) {
                    0 -> AbilityFragment()
                    2 -> IRThermalFragment()
                    3 -> PDFListFragment()
                    else -> ARouter.getInstance().build(RouterConfig.TC_MORE).navigation() as Fragment
                }
                fragment.arguments = Bundle().also { it.putBoolean(ExtraKeyConfig.IS_TC007, isTC007) }
                return fragment
            }
        }
    }
}