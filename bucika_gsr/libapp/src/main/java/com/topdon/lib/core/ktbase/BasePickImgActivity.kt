package com.topdon.lib.core.ktbase

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.View.MeasureSpec
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.R
import com.topdon.lib.core.dialog.ColorSelectDialog
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.view.ImageEditView
import kotlinx.android.synthetic.main.activity_image_pick_ir_plush.*
import kotlinx.coroutines.launch
import java.io.File

/**
 * des:
 * author: CaiSongL
 * date: 2024/9/3 9:25
 **/
abstract class BasePickImgActivity : BaseActivity(), View.OnClickListener {
    /**
     * String 类型 - 拾取的图片在本地的绝对路径.
     */
    val RESULT_IMAGE_PATH = "RESULT_IMAGE_PATH"
    /**
     * 当前是否已拍了一张照等待完成.
     */
    private var hasTakePhoto = false

    override fun initContentView(): Int {
        return R.layout.activity_image_pick_ir_plush
    }

    override fun initView() {
    }

    override fun initData() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //默认选中画圆
        iv_edit_circle.isSelected = true
        image_edit_view.type = ImageEditView.Type.CIRCLE
        view_color.setBackgroundColor(image_edit_view.color)

        iv_edit_color.setOnClickListener(this)
        iv_edit_circle.setOnClickListener(this)
        iv_edit_rect.setOnClickListener(this)
        iv_edit_arrow.setOnClickListener(this)
        iv_edit_clear.setOnClickListener(this)
        img_pick.setOnClickListener(this)

        title_view.setLeftClickListener {
            if (hasTakePhoto) {
                switchPhotoState(false)
            } else {
                finish()
            }
        }
        title_view.setRightClickListener {
            if (hasTakePhoto) {
                val absolutePath: String = intent.getStringExtra(RESULT_IMAGE_PATH)!!
                ImageUtils.save(image_edit_view.buildResultBitmap(), File(absolutePath), Bitmap.CompressFormat.PNG)
                val intent = Intent()
                intent.putExtra(RESULT_IMAGE_PATH, absolutePath)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        resize()
    }

    private fun resize() {
        val widthPixels = resources.displayMetrics.widthPixels
        val heightPixels = resources.displayMetrics.heightPixels
        title_view.measure(MeasureSpec.makeMeasureSpec(widthPixels, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightPixels, MeasureSpec.AT_MOST))

        val ivPickHeight = SizeUtils.dp2px(60f + 20 + 20) //拍照按钮高度，60dp+上下各20dp margin
        val menuHeight = (widthPixels * 75f / 384).toInt()
        val bottomHeight = ivPickHeight.coerceAtLeast(menuHeight)
        val canUseHeight = heightPixels - title_view.measuredHeight - bottomHeight
        val wantHeight = (widthPixels * 256f / 192).toInt()
        if (wantHeight <= canUseHeight) {//够用
            fragment_container_view.layoutParams = fragment_container_view.layoutParams.apply {
                width = widthPixels
                height = wantHeight
            }
            image_edit_view.layoutParams = image_edit_view.layoutParams.apply {
                width = widthPixels
                height = wantHeight
            }
        } else {
            fragment_container_view.layoutParams = fragment_container_view.layoutParams.apply {
                width = (canUseHeight * 192f / 256).toInt()
                height = canUseHeight
            }
            image_edit_view.layoutParams = image_edit_view.layoutParams.apply {
                width = (canUseHeight * 192f / 256).toInt()
                height = canUseHeight
            }
        }
    }


    open suspend fun getPickBitmap() : Bitmap?{
       return null
    }


    override fun onClick(v: View?) {
        when (v) {
            img_pick -> {
                lifecycleScope.launch {
                    getPickBitmap()?.let {
                        switchPhotoState(true)
                        image_edit_view.sourceBitmap = it
                        image_edit_view.clear()
                    }
                }
            }
            iv_edit_color -> {
                val colorPickDialog = ColorSelectDialog(this, image_edit_view.color)
                colorPickDialog.onPickListener = {
                    image_edit_view.color = it
                    view_color.setBackgroundColor(it)
                }
                colorPickDialog.show()
            }
            iv_edit_circle -> {
                iv_edit_circle.isSelected = true
                iv_edit_rect.isSelected = false
                iv_edit_arrow.isSelected = false
                image_edit_view.type = ImageEditView.Type.CIRCLE
            }
            iv_edit_rect -> {
                iv_edit_circle.isSelected = false
                iv_edit_rect.isSelected = true
                iv_edit_arrow.isSelected = false
                image_edit_view.type = ImageEditView.Type.RECT
            }
            iv_edit_arrow -> {
                iv_edit_circle.isSelected = false
                iv_edit_rect.isSelected = false
                iv_edit_arrow.isSelected = true
                image_edit_view.type = ImageEditView.Type.ARROW
            }
            iv_edit_clear -> image_edit_view.clear()
        }
    }

    override fun onBackPressed() {
        if (hasTakePhoto) {
            switchPhotoState(false)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * 切换 已拍照模式/未拍照模式.
     */
    private fun switchPhotoState(hasTakePhoto: Boolean) {
        this.hasTakePhoto = hasTakePhoto
        image_edit_view.isVisible = hasTakePhoto
        cl_edit_menu.isVisible = hasTakePhoto
        img_pick.isVisible = !hasTakePhoto
        fragment_container_view.isVisible = !hasTakePhoto
        title_view.setRightDrawable(if (hasTakePhoto) R.drawable.app_save else 0)
    }

    /**
     * 显示退出不保存提示弹框
     * @param listener 点击弹框上退出事件监听
     */
    private fun showExitTipsDialog(listener: (() -> Unit)) {
        TipDialog.Builder(this)
            .setMessage(R.string.diy_tip_save)
            .setPositiveListener(R.string.app_exit) {
                listener.invoke()
            }
            .setCancelListener(R.string.app_cancel)
            .create().show()
    }

    override fun disConnected() {
        super.disConnected()
        finish()
    }

}