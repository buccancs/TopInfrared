package com.topdon.module.thermal.ir.activity

import android.content.Intent
import android.media.MediaScannerConnection
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.BarUtils
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer
import com.topdon.lib.core.bean.GalleryBean
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.FileTools
import com.topdon.lib.core.tools.TimeTool
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.repository.TS004Repository
import com.topdon.lib.core.tools.ToastTools
import com.topdon.module.thermal.ir.R
import com.topdon.lib.core.dialog.ConfirmSelectDialog
import com.topdon.lib.core.bean.event.GalleryDelEvent
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.module.thermal.ir.event.GalleryDownloadEvent
import kotlinx.android.synthetic.main.activity_ir_video_gsy.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager
import java.io.File


@Route(path = RouterConfig.IR_VIDEO_GSY)
class IRVideoGSYActivity : BaseActivity() {

    private var isRemote = false
    private lateinit var data: GalleryBean
    override fun initContentView() = R.layout.activity_ir_video_gsy

    override fun initView() {
        BarUtils.setNavBarColor(this, ContextCompat.getColor(this, R.color.black))

        isRemote = intent.getBooleanExtra("isRemote", false)
        data = intent.getParcelableExtra("data") ?: throw NullPointerException("传递 data")

        cl_bottom.isVisible = isRemote //查看远端时底部才有3个按钮

        if (!isRemote) {
            title_view.setRightDrawable(R.drawable.ic_toolbar_info_svg)
            title_view.setRight2Drawable(R.drawable.ic_toolbar_share_svg)
            title_view.setRight3Drawable(R.drawable.ic_toolbar_delete_svg)
            title_view.setRightClickListener { actionInfo() }
            title_view.setRight2ClickListener { actionShare() }
            title_view.setRight3ClickListener { showDeleteDialog() }
        }

        cl_download.setOnClickListener {
            actionDownload(false)
        }
        cl_share.setOnClickListener {
            if (data.hasDownload) {
                actionShare()
            } else {
                actionDownload(true)
            }
        }
        cl_delete.setOnClickListener {
            showDeleteDialog()
        }

        iv_download.isSelected = data.hasDownload
        iv_download.setImageResource(if (isRemote) R.drawable.selector_download else R.drawable.ic_toolbar_info_svg)

        previewVideo(isRemote, data.path)
    }

    override fun initData() {
    }

    private fun previewVideo(isRemote: Boolean, path: String) {
        PlayerFactory.setPlayManager(Exo2PlayerManager::class.java)
        val url = if (isRemote) {
            path
        } else {
            path.replace("//", "/")
            "file://$path"
        }

        GSYVideoOptionBuilder()
            .setUrl(url)
            .build(gsy_play)
        //界面设置
        gsy_play.isNeedShowWifiTip = false //不显示消耗流量弹框
        gsy_play.titleTextView.visibility = View.GONE
        gsy_play.backButton.visibility = View.GONE
        gsy_play.fullscreenButton.visibility = View.GONE
    }

    private fun actionDownload(isToShare: Boolean) {
        if (data.hasDownload) {//已下载
            if (isToShare) {
                actionShare()
            }
            return
        }
        lifecycleScope.launch {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            showCameraLoading()
            val isSuccess = TS004Repository.download(data.path, File(FileConfig.ts004GalleryDir, data.name))
            MediaScannerConnection.scanFile(this@IRVideoGSYActivity, arrayOf(FileConfig.ts004GalleryDir), null, null)
            dismissCameraLoading()
            if (isSuccess) {
                ToastTools.showShort(R.string.tip_save_success)
                EventBus.getDefault().post(GalleryDownloadEvent(data.name))
                data.hasDownload = true
                iv_download.isSelected = true
                if (isToShare) {
                    actionShare()
                }
            } else {
                ToastTools.showShort(R.string.liveData_save_error)
            }
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun actionInfo() {
        val sizeStr = FileTools.getFileSize(data.path)
        val str = StringBuilder()
        str.append(getString(R.string.detail_date)).append("\n")
        str.append(TimeTool.showDateType(data.timeMillis)).append("\n\n")
        str.append(getString(R.string.detail_info)).append("\n")
//        str.append("尺寸: ").append(whStr).append("\n")
        str.append("${getString(R.string.detail_len)}: ").append(sizeStr).append("\n")
        str.append("${getString(R.string.detail_path)}: ").append(data.path).append("\n")
        TipDialog.Builder(this)
            .setMessage(str.toString())
            .setCanceled(true)
            .create().show()
    }

    private fun actionShare() {
        val uri = FileTools.getUri(File(data.path))
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "video/*"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.battery_share)))
    }

    private fun showDeleteDialog() {
        ConfirmSelectDialog(this).run {
            setTitleRes(R.string.tip_delete)
            setMessageRes(R.string.also_del_from_phone_album)
            setShowMessage(isRemote && data.hasDownload)
            onConfirmClickListener = {
                deleteFile(it)
            }
            show()
        }
    }

    private fun deleteFile(isDelLocal: Boolean) {
        if (isRemote) {
            lifecycleScope.launch {
                showCameraLoading()
                val isSuccess = TS004Repository.deleteFiles(arrayOf(data.id))
                if (isSuccess) {
                    if (isDelLocal) {
                        File(FileConfig.ts004GalleryDir, data.name).delete()
                        MediaScannerConnection.scanFile(this@IRVideoGSYActivity, arrayOf(FileConfig.ts004GalleryDir), null, null)
                    }
                    dismissCameraLoading()
                    ToastTools.showShort(R.string.test_results_delete_success)
                    EventBus.getDefault().post(GalleryDelEvent())
                    finish()
                } else {
                    dismissCameraLoading()
                    TToast.shortToast(this@IRVideoGSYActivity, R.string.test_results_delete_failed)
                }
            }
        } else {
            EventBus.getDefault().post(GalleryDelEvent())
            File(data.path).delete()
            MediaScannerConnection.scanFile(this, arrayOf(FileConfig.ts004GalleryDir), null, null)
            finish()
        }
    }

    override fun onResume() {
        getCurPlay().onVideoResume(false)
        super.onResume()
    }

    override fun onPause() {
        getCurPlay().onVideoPause()
        super.onPause()
    }

    private fun getCurPlay(): GSYVideoPlayer {
        return if (gsy_play.fullWindowPlayer != null) {
            gsy_play.fullWindowPlayer
        } else {
            gsy_play
        }
    }
}