package com.topdon.module.thermal.ir.activity

import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.bean.GalleryTitle
import com.topdon.lib.core.bean.event.ReportCreateEvent
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.FileTools.getUri
import com.topdon.lib.core.tools.ToastTools
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.repository.GalleryRepository.DirType
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.adapter.GalleryAdapter
import com.topdon.lib.core.bean.event.GalleryDelEvent
import com.topdon.lib.core.utils.Constants.IS_REPORT_FIRST
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.module.thermal.ir.viewmodel.IRGalleryViewModel
import kotlinx.android.synthetic.main.activity_report_pick_img.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

/**
 * 生成报告图片拾取.
 *
 * 需要传递参数：
 * - 是否 TC007: [ExtraKeyConfig.IS_TC007] 进入目录不同
 * - [ExtraKeyConfig.REPORT_INFO] - 报告信息
 * - [ExtraKeyConfig.REPORT_CONDITION] - 检测条件
 * - [ExtraKeyConfig.REPORT_IR_LIST] - 当前已添加的图片对应数据列表
 */
@Route(path = RouterConfig.REPORT_PICK_IMG)
class ReportPickImgActivity : BaseActivity(), View.OnClickListener {

    /**
     * 从上一界面传递过来的，当前是否为 TC007 设备类型.
     * true-TC007 false-其他插件式设备
     */
    private var isTC007 = false

    private val viewModel: IRGalleryViewModel by viewModels()

    private val adapter = GalleryAdapter()

    override fun initContentView() = R.layout.activity_report_pick_img

    override fun initView() {
        isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)

        title_view.setRightDrawable(R.drawable.ic_toolbar_check_svg)
        title_view.setRightClickListener { setEditMode(true) }

        initRecycler()

        cl_share.setOnClickListener(this)
        cl_delete.setOnClickListener(this)

        showLoadingDialog()

        viewModel.showListLD.observe(this) {
            adapter.refreshList(it)
            dismissLoadingDialog()
        }
        viewModel.deleteResultLD.observe(this) {
            if (it) {
                TToast.shortToast(this@ReportPickImgActivity, R.string.test_results_delete_success)
                adapter.isEditMode = false
                EventBus.getDefault().post(GalleryDelEvent())
                MediaScannerConnection.scanFile(this, arrayOf(if (isTC007) FileConfig.tc007GalleryDir else FileConfig.lineGalleryDir), null, null)
                viewModel.queryAllReportImg(if (isTC007) DirType.TC007 else DirType.LINE)
            } else {
                TToast.shortToast(this@ReportPickImgActivity, R.string.test_results_delete_failed)
            }
        }
        viewModel.queryAllReportImg(if (isTC007) DirType.TC007 else DirType.LINE)
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onReportCreate(event: ReportCreateEvent) {
        finish()
    }

    override fun initData() {

    }

    override fun onBackPressed() {
        if (adapter.isEditMode) {
            setEditMode(false)
        } else {
            super.onBackPressed()
        }
    }

    private fun setEditMode(isEditMode: Boolean) {
        adapter.isEditMode = isEditMode
        group_bottom.isVisible = isEditMode
        title_view.setTitleText(if (isEditMode) getString(R.string.chosen_item, adapter.selectList.size) else getString(R.string.app_gallery))
        title_view.setLeftDrawable(if (isEditMode) R.drawable.svg_x_cc else R.drawable.ic_back_white_svg)
        title_view.setLeftClickListener {
            if (isEditMode) {
                setEditMode(false)
            } else {
                finish()
            }
        }
        title_view.setRightDrawable(if (isEditMode) 0 else R.drawable.ic_toolbar_check_svg)
        title_view.setRightText(if (isEditMode) getString(R.string.report_select_all) else "")
        title_view.setRightClickListener {
            if (isEditMode) {
                adapter.selectAll()
            } else {
                setEditMode(true)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            cl_share -> {
                shareImage()
            }
            cl_delete -> {
                deleteImage()
            }
        }
    }

    private fun initRecycler() {
        val spanCount = 3
        val gridLayoutManager = GridLayoutManager(this, spanCount)
        //动态设置span
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.dataList[position] is GalleryTitle) spanCount else 1
            }
        }
        ir_gallery_recycler.adapter = adapter
        ir_gallery_recycler.layoutManager = gridLayoutManager

        adapter.onLongEditListener = {
            // adapter 里面的切换编辑太乱了，先这么顶着
            group_bottom.isVisible = true
            title_view.setTitleText(getString(R.string.chosen_item, adapter.selectList.size))
            title_view.setLeftDrawable(R.drawable.svg_x_cc)
            title_view.setLeftClickListener {
                setEditMode(false)
            }
            title_view.setRightDrawable(0)
            title_view.setRightText(R.string.report_select_all)
            title_view.setRightClickListener {
                adapter.selectAll()
            }
        }

        adapter.selectCallback = {
            title_view.setTitleText(getString(R.string.chosen_item, it.size))
        }
        adapter.itemClickCallback = {
            val data = adapter.dataList[it]
            val fileName = data.name.substringBeforeLast(".")
            val irPath = "${FileConfig.lineIrGalleryDir}/${fileName}.ir"
            if (File(irPath).exists()) {
                ARouter.getInstance().build(RouterConfig.IR_GALLERY_EDIT)
                    .withBoolean(ExtraKeyConfig.IS_TC007, isTC007)
                    .withBoolean(ExtraKeyConfig.IS_PICK_REPORT_IMG, true)
                    .withBoolean(IS_REPORT_FIRST, false)
                    .withString(ExtraKeyConfig.FILE_ABSOLUTE_PATH, irPath)
                    .withParcelable(ExtraKeyConfig.REPORT_INFO, intent.getParcelableExtra(ExtraKeyConfig.REPORT_INFO))
                    .withParcelable(ExtraKeyConfig.REPORT_CONDITION, intent.getParcelableExtra(ExtraKeyConfig.REPORT_CONDITION))
                    .withParcelableArrayList(ExtraKeyConfig.REPORT_IR_LIST, intent.getParcelableArrayListExtra(ExtraKeyConfig.REPORT_IR_LIST))
                    .navigation(this)
            } else {
                ToastTools.showShort(R.string.album_report_on_edit)
            }
        }
    }

    private fun deleteImage() {
        val deleteList = adapter.buildSelectList()
        if (deleteList.size > 0) {
            TipDialog.Builder(this)
                .setMessage(getString(
                        R.string.tip_delete_chosen,
                        deleteList.size
                    ))
                .setPositiveListener(R.string.app_confirm) {
                    viewModel.delete(deleteList, if (isTC007) DirType.TC007 else DirType.LINE, true)
                }.setCancelListener(R.string.app_cancel)
                .create().show()
        } else {
            ToastTools.showShort(getString(R.string.tip_least_select))
        }
    }

    private fun shareImage() {
        val data = adapter.buildSelectList()
        if (data.size == 0) {
            ToastTools.showShort(getString(R.string.tip_least_select))
            return
        }
        if (data.size > 9) {
            ToastTools.showShort(getString(R.string.Limite_di_9carte))
            return
        }
        val imageUris = ArrayList<Uri>()
        val shareIntent = Intent()
        if (data.size == 1) {
            if (data[0].name.uppercase().endsWith(".MP4")) {
                shareIntent.type = "video/*"
            } else {
                shareIntent.type = "image/*"
            }
            shareIntent.action = Intent.ACTION_SEND
            val uri = getUri(File(data[0].path))
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        } else {
            shareIntent.type = "video/*"
            for (bean in data) {
                imageUris.add(getUri(File(bean.path)))
            }
            shareIntent.action = Intent.ACTION_SEND_MULTIPLE
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUris)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.battery_share)))
    }
}

