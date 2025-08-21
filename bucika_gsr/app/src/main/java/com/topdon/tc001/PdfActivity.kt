package com.topdon.tc001

import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import kotlinx.android.synthetic.main.activity_pdf.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * create by fylder on 2018/8/9
 **/
@Route(path = RouterConfig.PDF)
class PdfActivity : BaseActivity() {

    override fun initContentView() = R.layout.activity_pdf

    override fun initView() {
        //本地说明书
        pdf_view.fromAsset(if (intent.getBooleanExtra("isTS001", false)) "TC001.pdf" else "TS004.pdf")
            .enableSwipe(true) // allows to block changing pages using swipe
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .defaultPage(0)
            .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
            .password(null)
            .scrollHandle(null)
            .enableAntialiasing(true) // improve rendering a little bit on low-res screens
            // spacing between pages in dp. To define spacing color, set view background
            .spacing(0)
            .load()
    }

    override fun initData() {
        val tc001File = File(getExternalFilesDir("pdf")!!, "TC001.pdf")
        if (!tc001File.exists()) {
            copyBigDataToSD("TC001.pdf", tc001File)
        }

        val tc004File = File(getExternalFilesDir("pdf")!!, "TS004.pdf")
        if (!tc004File.exists()) {
            copyBigDataToSD("TS004.pdf", tc004File)
        }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    //复制assets文件
    @Throws(IOException::class)
    private fun copyBigDataToSD(assetsName: String, targetFile: File) {
        val myOutput: OutputStream = FileOutputStream(targetFile)
        val myInput = assets.open(assetsName)
        val buffer = ByteArray(1024)
        var length: Int = myInput.read(buffer)
        while (length > 0) {
            myOutput.write(buffer, 0, length)
            length = myInput.read(buffer)
        }
        myOutput.flush()
        myInput.close()
        myOutput.close()
    }

}