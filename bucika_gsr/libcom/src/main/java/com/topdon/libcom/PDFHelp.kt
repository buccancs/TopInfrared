package com.topdon.libcom

import android.content.ContentValues
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.UriUtils
import com.blankj.utilcode.util.Utils
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.utils.CommUtils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * @author: CaiSongL
 * @date: 2023/5/5 17:41
 */
object PDFHelp {

    fun savePdfFileByListView(name: String, view: ScrollView, viewList: MutableList<View>, watermarkView: View): String {
        val onePageHeight: Int = (view.width * 297f / 210f).toInt() // A4纸宽高比210:297

        var onePageContentHeight = 0f

        val pdfDocument = PdfDocument()
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null

        val paint = Paint()
        paint.color = 0xff16131e.toInt()

        for (index in 0 until viewList.size) {
            val contentHeight = viewList[index].measuredHeight
            if (onePageContentHeight + contentHeight > onePageHeight) {//超出内容，另起一页
                onePageContentHeight = 0f
                pdfDocument.finishPage(page)
                page = null
            }
            if (page == null) {
                val pageInfo = PageInfo.Builder(view.width, onePageHeight, 1)
                    .setContentRect(Rect(0, 0, view.width, onePageHeight))
                    .create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                canvas.drawRect(0f, 0f, view.width.toFloat(), onePageHeight.toFloat(), paint)

                if (index == 0) {
                    val bgTopDrawable: Drawable? = ContextCompat.getDrawable(view.context, R.drawable.ic_report_create_bg_top)
                    bgTopDrawable?.setBounds(0, 0, view.width, (view.width * 1026 / 1125f).toInt())
                    bgTopDrawable?.draw(canvas)
                }

                canvas.save()
                watermarkView.draw(canvas)
                canvas.restore()
            }

            canvas?.save()
            canvas?.translate((view.width - viewList[index].measuredWidth) / 2f, 0f)
            viewList[index].draw(canvas!!)
            canvas?.restore()

            canvas?.translate(0f, contentHeight.toFloat())
            onePageContentHeight += contentHeight
            if (page != null && index == viewList.size - 1) {
                pdfDocument.finishPage(page)
            }
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val pdfFile = File(FileConfig.getPdfDir(), "$name.pdf")
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()
            return pdfFile.absolutePath
        } else {
            val fileName = "$name.pdf"
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                FileConfig.getPdfDir()
            )
            val contentUri = MediaStore.Files.getContentUri("external")
            val uri = Utils.getApp().contentResolver.insert(contentUri, values)
            return if (uri != null) {
                val outputStream = Utils.getApp().contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    val bos = BufferedOutputStream(outputStream)
                    pdfDocument.writeTo(bos)
                    bos.flush()
                    bos.close()
                }
                Log.w("导出", UriUtils.uri2File(uri).absolutePath)
                UriUtils.uri2File(uri).absolutePath
            } else {
                ""
            }
        }
    }


}