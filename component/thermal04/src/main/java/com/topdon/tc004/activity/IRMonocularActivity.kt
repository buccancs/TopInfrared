package com.topdon.tc004.activity

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.*
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.topdon.lib.core.bean.event.GalleryDelEvent
import com.topdon.lib.core.bean.event.SocketMsgEvent
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.http.ts004.TS004URL
import com.topdon.lib.core.ktbase.BaseWifiActivity
import com.topdon.lib.core.repository.*
import com.topdon.lib.core.socket.SocketCmdUtil
import com.topdon.lib.core.tools.GlideLoader
import com.topdon.lib.core.utils.NetWorkUtils
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.lib.core.utils.WsCmdConstants
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.tc004.R
import com.topdon.tc004.activity.video.PlayFragment
import com.topdon.tc004.adapter.MenuSixAdapter
import com.topdon.tc004.bean.MenuBean
import com.topdon.tc004.config.MonocularHelp
import com.topdon.tc004.util.WsUtil
import kotlinx.android.synthetic.main.activity_monocular_ir.*
import kotlinx.coroutines.launch
import org.easydarwin.video.Client
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

@Route(path = RouterConfig.IR_MONOCULAR)
class IRMonocularActivity : BaseWifiActivity() {
    private var mRenderFragment: PlayFragment? = null
    private var mDefaultHot = SharedManager.getHotMode()
    private var mDefaultLight = MenuBean.TYPE_LIGHT_MIDDLE
    private var mDefaultGain = MenuBean.TYPE_GAIN_X1
    private var isRange: Boolean = true
    private var isPip: Boolean = true
    private var isExpand: Boolean = false
    private var isVideo = false
    private var isVideoRecording = false
    private val url = TS004URL.RTSP_URL
    private val sixAdapter by lazy { MenuSixAdapter(this) }

    override fun isLockPortrait(): Boolean = false

    override fun initContentView(): Int {
        setStatusBarVisible(resources.configuration.orientation == 1)
        return R.layout.activity_monocular_ir
    }

    override fun initView() {
        PreferenceManager.getDefaultSharedPreferences(this@IRMonocularActivity)
            .edit()
            .putBoolean("use-sw-codec", true)
            .apply()
        recyclerView.layoutManager = if (ScreenUtil.isPortrait(this)) {
            GridLayoutManager(this, 3)
        } else {
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        }
        recyclerView.adapter = sixAdapter
        sixAdapter.listener = { _, code ->
            setSetting(code)
        }
        initListener()
    }

    override fun initData() {
        lifecycleScope.launch {
            TS004Repository.syncTime()
            TS004Repository.syncTimeZone()
        }

        refreshImg(2)
    }

    private fun initListener() {
        val screenNum = resources.configuration.orientation
        iv_portrait.setOnClickListener {
            requestedOrientation = if (screenNum == 1) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
        iv_back.setOnClickListener {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        iv_expand.setOnClickListener {
            if (isExpand) {
                recyclerView.visibility = View.VISIBLE
                cLayout_bottom.visibility = View.VISIBLE
                iv_expand.setBackgroundResource(R.drawable.ic_menu_down_svg)
                isExpand = false
            } else {
                recyclerView.visibility = View.GONE
                cLayout_bottom.visibility = View.GONE
                iv_expand.setBackgroundResource(R.drawable.ic_menu_up_svg)
                isExpand = true
            }
        }
        camera_gallery_img.setOnClickListener {
            ARouter.getInstance()
                .build(RouterConfig.IR_GALLERY_HOME)
                .withInt(ExtraKeyConfig.DIR_TYPE, GalleryRepository.DirType.TS004_REMOTE.ordinal)
                .navigation()
        }
        camera_setting_img.setOnClickListener {
            resetCamera()
            isVideo = if (isVideo) {
                camera_img.setImageResource(R.drawable.ic_menu_camera)
                camera_setting_img.setImageResource(R.mipmap.ic_menu_video_setting)
                false
            } else {
                camera_img.setImageResource(R.drawable.ic_menu_bottom_video_svg)
                camera_setting_img.setImageResource(R.mipmap.ic_menu_camera_setting)
                true
            }
        }

        camera_img.setOnClickListener {
            centerCamera()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= 29) {
            NetWorkUtils.switchNetwork(true) {
                if (it != null) {
                    TS004Repository.netWork = it
                }
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lifecycleScope.launch {
            val mPseudoColorBean = TS004Repository.getPseudoColor()
            if (mPseudoColorBean?.isSuccess() == true) {
                mDefaultHot = mPseudoColorBean.data?.mode!!
                updatePseudoColorUI()
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
        lifecycleScope.launch {
            val mRangeBean = TS004Repository.getRangeFind()
            if (mRangeBean?.isSuccess() == true) {
                isRange = mRangeBean.data?.state!! == 1
                updateRangeUI()
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
        lifecycleScope.launch {
            val mBrightnessBean = TS004Repository.getPanelParam()
            if (mBrightnessBean?.isSuccess() == true) {
                mDefaultLight = mBrightnessBean.data?.brightness!!
                updateBrightnessUI(true)
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
        lifecycleScope.launch {
            val mPipBean = TS004Repository.getPip()
            if (mPipBean?.isSuccess() == true) {
                isPip = mPipBean.data?.enable!!
                updatePipUI()
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
        lifecycleScope.launch {
            val mZoomBean = TS004Repository.getZoom()
            if (mZoomBean?.isSuccess() == true) {
                mDefaultGain = mZoomBean.data?.factor!!
                updateZoomUI()
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
        lifecycleScope.launch {
            val mRecordStatus = TS004Repository.getRecordStatus()
            if (mRecordStatus?.isSuccess() == true) {
                val status = mRecordStatus.data?.status!!
                updateCamera(status)
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        endVideoRecord()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun connected() {
        
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.getDefaultSharedPreferences(this@IRMonocularActivity)
            .edit()
            .putBoolean("use-sw-codec", true)
            .apply()
        mRenderFragment = if (savedInstanceState == null) {
            val fragment: PlayFragment =
                PlayFragment.newInstance(url, Client.TRANSTYPE_TCP, 1, null)
            supportFragmentManager.beginTransaction().add(R.id.render_holder, fragment).commit()
            fragment
        } else {
            (supportFragmentManager.findFragmentById(R.id.render_holder) as PlayFragment?)!!
        }
    }

    private fun resetCamera() {
        if (isVideo && isVideoRecording) {
            isVideoRecording = false
            video()
        }
    }

    private fun setStatusBarVisible(show: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (show) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            } else {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }
        }
    }

    private fun setSetting(code: Int) {
        when (code) {
            MonocularHelp.TYPE_SET_BLACK -> {
                setPseudoColor()
            }

            MonocularHelp.TYPE_SET_RANGE -> {
                setRangeFind()
            }

            MonocularHelp.TYPE_SET_LIGHT -> {
                setBrightness()
            }

            MonocularHelp.TYPE_SET_PIP -> {
                setPip()
            }

            MonocularHelp.TYPE_SET_GAIN -> {
                setZoom()
            }

            MonocularHelp.TYPE_SET_MORE -> {
                ARouter.getInstance().build(RouterConfig.TS004_MORE).navigation(this)
            }
        }
    }

    private fun updateCamera(enable: Boolean) {
        if (enable) {
            camera_img.setImageResource(R.drawable.ic_menu_bottom_video_recording_svg)
            camera_setting_img.setImageResource(R.mipmap.ic_menu_camera_setting)
            isVideoRecording = true
            isVideo = true
        } else {
            if (isVideo) {
                camera_img.setImageResource(R.drawable.ic_menu_bottom_video_svg)
                camera_setting_img.setImageResource(R.mipmap.ic_menu_camera_setting)
            } else {
                camera_img.setImageResource(R.drawable.ic_menu_camera)
                camera_setting_img.setImageResource(R.mipmap.ic_menu_video_setting)
            }
            isVideoRecording = false
        }
    }

    private fun updatePseudoColorUI() {
        sixAdapter.enBlack(mDefaultHot)
    }

    private fun updateBrightnessUI(isWebsocket: Boolean) {
        when (mDefaultLight) {
            in 81..100 -> {
                if (!isWebsocket) {
                    TToast.shortToast(this@IRMonocularActivity, R.string.ts004_brightness_high)
                }
            }

            in 61..80 -> {
                if (!isWebsocket) {
                    TToast.shortToast(this@IRMonocularActivity, R.string.ts004_brightness_middle)
                }
            }

            in 0..60 -> {
                if (!isWebsocket) {
                    TToast.shortToast(this@IRMonocularActivity, R.string.ts004_brightness_low)
                }
            }
        }
        sixAdapter.enLight(mDefaultLight)
    }

    private fun updateRangeUI() {
        sixAdapter.enRange(isRange)
    }

    private fun updatePipUI() {
        sixAdapter.enPip(isPip)
    }

    private fun updateZoomUI() {
        sixAdapter.enGain(mDefaultGain)
        when (mDefaultGain) {
            MenuBean.TYPE_GAIN_X1 -> {
                mDefaultGain = MenuBean.TYPE_GAIN_X2
            }

            MenuBean.TYPE_GAIN_X2 -> {
                mDefaultGain = MenuBean.TYPE_GAIN_X4
            }

            MenuBean.TYPE_GAIN_X4 -> {
                mDefaultGain = MenuBean.TYPE_GAIN_X8
            }

            MenuBean.TYPE_GAIN_X8 -> {
                mDefaultGain = MenuBean.TYPE_GAIN_X1
            }
        }
    }

    private fun setPseudoColor() {
        lifecycleScope.launch {
            val isSuccess = TS004Repository.setPseudoColor(WsUtil.getWebSocketPseudo(mDefaultHot))
            if (isSuccess) {
                mDefaultHot = WsUtil.getWebSocketPseudo(mDefaultHot)
                updatePseudoColorUI()
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
    }

    private fun setBrightness() {
        lifecycleScope.launch {
            val isSuccess = TS004Repository.setPanelParam(WsUtil.getBrightness(mDefaultLight))
            if (isSuccess) {
                mDefaultLight = WsUtil.getBrightness(mDefaultLight)
                updateBrightnessUI(false)
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
    }

    private fun setRangeFind() {
        lifecycleScope.launch {
            val isSuccess = TS004Repository.setRangeFind(if (!isRange) 1 else 0)
            if (isSuccess) {
                isRange = !isRange
                updateRangeUI()
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
    }

    private fun setPip() {
        lifecycleScope.launch {
            val isSuccess = TS004Repository.setPip(!isPip)
            if (isSuccess) {
                isPip = !isPip
                updatePipUI()
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
    }

    private fun setZoom() {
        lifecycleScope.launch {
            val isSuccess = TS004Repository.setZoom(mDefaultGain)
            if (isSuccess) {
                updateZoomUI()
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
    }

    override fun finish() {
        val fragmentManager: FragmentManager = supportFragmentManager
        if (mRenderFragment != null) {
            fragmentManager.beginTransaction().remove(mRenderFragment!!).commitAllowingStateLoss()
        }
        isExpand = false
        super.finish()
    }

    @SuppressLint("CheckResult")
    private fun centerCamera() {
        XXPermissions.with(this)
            .permission(
                permissionList
            )
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        if (isVideo) {
                            isVideoRecording = if (isVideoRecording) {
                                camera_img.setImageResource(R.drawable.ic_menu_bottom_video_svg)
                                false
                            } else {
                                camera_img.setImageResource(R.drawable.ic_menu_bottom_video_recording_svg)
                                true
                            }
                            video()
                        } else {
                            camera()
                        }
                    } else {
                        TToast.shortToast(this@IRMonocularActivity, R.string.scan_ble_tip_authorize)
                    }
                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        TipDialog.Builder(this@IRMonocularActivity)
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

    private fun camera() {
        setCamera()
    }

    private fun video() {
        lifecycleScope.launch {
            val isSuccess = TS004Repository.setVideo(isVideoRecording)
            if (isSuccess) {
                if (!isVideoRecording) {
                    refreshImg(1)
                }
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
    }

    private fun setCamera() {
        lifecycleScope.launch {
            val isSuccess = TS004Repository.setSnapshot()
            if (isSuccess) {
                refreshImg(0)
                TToast.shortToast(this@IRMonocularActivity, R.string.tip_save_success)
            } else {
                TToast.shortToast(this@IRMonocularActivity, R.string.operation_failed_tips)
            }
        }
    }

    private fun refreshImg(fileType: Int) {
        lifecycleScope.launch {
            val fileList: List<FileBean>? = TS004Repository.getNewestFile(fileType)
            try {
                if (fileList != null) {
                    if (fileList.isEmpty()) {
                        camera_gallery_img.setImageResource(R.mipmap.ic_menu_photo_default)
                        updateDefaultPhotoWH(true)
                    } else {
                        updateDefaultPhotoWH(false)
                        val iconBean: FileBean = fileList[0]
                        GlideLoader.loadGallery(
                            camera_gallery_img,
                            "http://192.168.40.1:8080/DCIM/" + if (iconBean.type == 1) iconBean.thumb else iconBean.name
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("刷新图标状态异常", e.message.toString())
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun galleryDel(event: GalleryDelEvent) {
        refreshImg(2)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSocketMsgEvent(event: SocketMsgEvent) {
        when (SocketCmdUtil.getCmdResponse(event.text)) {
            WsCmdConstants.AR_COMMAND_PSEUDO_COLOR_GET -> {
                val webSocketIp = SocketCmdUtil.getIpResponse(event.text)
                val pseudoColor: WsPseudoColor? = WsUtil.getWsResponse(event.text)
                if (webSocketIp == WsCmdConstants.AR_COMMAND_IP) {
                    mDefaultHot = pseudoColor!!.mode!!
                    updatePseudoColorUI()
                }
            }

            WsCmdConstants.AR_COMMAND_RANGE_FIND_GET -> {
                val webSocketIp = SocketCmdUtil.getIpResponse(event.text)
                val wsRange: WsRange? = WsUtil.getWsResponse(event.text)
                if (webSocketIp == WsCmdConstants.AR_COMMAND_IP) {
                    isRange = wsRange!!.state == 1
                    updateRangeUI()
                }
            }

            WsCmdConstants.AR_COMMAND_PANEL_PARAM_GET -> {
                val webSocketIp = SocketCmdUtil.getIpResponse(event.text)
                val wsBrightness: WsLight? = WsUtil.getWsResponse(event.text)
                if (webSocketIp == WsCmdConstants.AR_COMMAND_IP) {
                    mDefaultLight = wsBrightness?.brightness!!
                    updateBrightnessUI(true)
                }
            }

            WsCmdConstants.AR_COMMAND_PIP_GET -> {
                val webSocketIp = SocketCmdUtil.getIpResponse(event.text)
                val wsPip: WsPip? = WsUtil.getWsResponse(event.text)
                if (webSocketIp == WsCmdConstants.AR_COMMAND_IP) {
                    isPip = wsPip!!.enable == 1
                    updatePipUI()
                }
            }

            WsCmdConstants.AR_COMMAND_ZOOM_GET -> {
                val webSocketIp = SocketCmdUtil.getIpResponse(event.text)
                val wsZoom: WsZoom? = WsUtil.getWsResponse(event.text)
                if (webSocketIp == WsCmdConstants.AR_COMMAND_IP) {
                    mDefaultGain = wsZoom!!.factor!!
                    updateZoomUI()
                }
            }

            WsCmdConstants.AR_COMMAND_SNAPSHOT -> {
                refreshImg(0)
            }

            WsCmdConstants.AR_COMMAND_VRECORD -> {
                try {
                    val data: JSONObject = JSONObject(event.text).getJSONObject("data")
                    val enable: Boolean = data.getBoolean("enable")
                    if (!enable) {
                        refreshImg(1)
                    }
                    updateCamera(enable);
                } catch (_: Exception) {

                }
            }
        }
    }

    override fun onSocketDisConnected(isTS004: Boolean) {
        if (isTS004) {
            ARouter.getInstance().build(RouterConfig.MAIN).navigation(this)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun endVideoRecord() {
        if (isVideoRecording) {
            resetCamera()
        }
    }

    private fun updateDefaultPhotoWH(isDefault: Boolean) {
        val layoutParams = camera_gallery_img.layoutParams
        layoutParams.width = SizeUtils.dp2px(if (isDefault) 74f else 54f)
        layoutParams.height = SizeUtils.dp2px(if (isDefault) 74f else 54f)
        camera_gallery_img.layoutParams = layoutParams
    }
}