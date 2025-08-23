package com.topdon.house.activity

import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ToastUtils
import com.topdon.house.R
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.ktbase.BaseActivity
import kotlinx.android.synthetic.main.activity_sign_input.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignInputActivity : BaseActivity(), View.OnClickListener {
    override fun isLockPortrait(): Boolean = false

    override fun initContentView(): Int = R.layout.activity_sign_input

    override fun initView() {
        title_view.setTitleText(if (intent.getBooleanExtra(ExtraKeyConfig.IS_PICK_INSPECTOR, false)) R.string.inspector_signature else R.string.house_owner_signature)

        cl_save.setOnClickListener(this)
        cl_clear.setOnClickListener(this)

        sign_view.onSignChangeListener = {
            cl_save.alpha = if (it) 1f else 0.5f
        }
    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        when (v) {
            cl_save -> {
                if (!sign_view.hasSign) {
                    ToastUtils.showShort(getString(R.string.house_sign_finish_tips))
                    return
                }

                val whiteBitmap: Bitmap = sign_view.bitmap ?: return
                showLoadingDialog()
                lifecycleScope.launch(Dispatchers.IO) {
                    val currentTime = System.currentTimeMillis()
                    val whiteFile = FileConfig.getSignImageDir(this@SignInputActivity, "sign${currentTime}_white.png")
                    ImageUtils.save(whiteBitmap, whiteFile, Bitmap.CompressFormat.PNG)

                    val blackBitmap: Bitmap = whiteBitmap.copy(Bitmap.Config.ARGB_8888, true)
                    for (y in 0 until blackBitmap.height) {
                        for (x in 0 until blackBitmap.width) {
                            if (blackBitmap.getPixel(x, y) == 0xffffffff.toInt()) {
                                blackBitmap.setPixel(x, y, 0xff000000.toInt())
                            }
                        }
                    }
                    val blackFile = FileConfig.getSignImageDir(this@SignInputActivity, "sign${currentTime}_black.png")
                    ImageUtils.save(blackBitmap, blackFile, Bitmap.CompressFormat.PNG)

                    withContext(Dispatchers.Main) {
                        val resultIntent = Intent()
                        resultIntent.putExtra(ExtraKeyConfig.IS_PICK_INSPECTOR, intent.getBooleanExtra(ExtraKeyConfig.IS_PICK_INSPECTOR, false))
                        resultIntent.putExtra(ExtraKeyConfig.RESULT_PATH_WHITE, whiteFile.absolutePath)
                        resultIntent.putExtra(ExtraKeyConfig.RESULT_PATH_BLACK, blackFile.absolutePath)
                        setResult(RESULT_OK, resultIntent)
                        dismissLoadingDialog()
                        finish()
                    }
                }
            }
            cl_clear -> {
                sign_view.clear()
            }
        }
    }
}