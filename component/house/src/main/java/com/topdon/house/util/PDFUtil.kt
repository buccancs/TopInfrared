package com.topdon.house.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.widget.TextView
import androidx.core.view.isVisible
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.UriUtils
import com.bumptech.glide.Glide
import com.topdon.house.R
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.db.entity.HouseReport
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.pdf_head.view.*
import kotlinx.android.synthetic.main.pdf_foot.view.*
import kotlinx.android.synthetic.main.pdf_tab_item.view.*
import kotlinx.android.synthetic.main.pdf_image_line.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object PDFUtil {

    suspend fun delAllPDF(context: Context) = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < 29) {
            val files: Array<File> = File(FileConfig.documentsDir).listFiles() ?: return@withContext
            for (file in files) {
                if (file.isFile) {
                    file.delete()
                }
            }
        } else {
            try {
                val resolver: ContentResolver = context.contentResolver
                val contentUri: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
                val selectionArgs: Array<String> = arrayOf(FileConfig.documentsDir)
                resolver.delete(contentUri, selection, selectionArgs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun delPDF(context: Context, houseReport: HouseReport): Boolean = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < 29) {
            return@withContext FileUtils.delete(File(FileConfig.documentsDir, houseReport.getPdfFileName()))
        } else {
            val resolver: ContentResolver = context.contentResolver
            val contentUri: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
            val selectionArgs: Array<String> = arrayOf(FileConfig.documentsDir, houseReport.getPdfFileName())
            val delCount = resolver.delete(contentUri, selection, selectionArgs)
            return@withContext delCount > 0
        }
    }

    @SuppressLint("InflateParams")
    suspend fun savePDF(context: Context, houseReport: HouseReport): Uri? = withContext(Dispatchers.IO) {
        val pageWidth = ScreenUtil.getScreenWidth(context).coerceAtMost(ScreenUtil.getScreenHeight(context))
        val pageHeight = (pageWidth * 297f / 210f).toInt()

        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1)
            .setContentRect(Rect(0, 0, pageWidth, pageHeight))
            .create()
        var page: PdfDocument.Page = pdfDocument.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        val headView = buildHeadView(context, houseReport)
        headView.draw(canvas)
        canvas.translate(0f, headView.height.toFloat())

        var hasUseHeight = headView.height
        val margin = SizeUtils.dp2px(6f)
        val marginPaint = Paint()
        marginPaint.color = 0xfff5f5f7.toInt()

        for (dirBean in houseReport.dirList) {
            val titleText = TextView(context)
            titleText.text = dirBean.dirName
            titleText.textSize = 8f
            titleText.paint.isFakeBoldText = true
            titleText.setTextColor(0xff333333.toInt())
            titleText.setPadding(SizeUtils.dp2px(13f), SizeUtils.dp2px(13f), SizeUtils.dp2px(13f), SizeUtils.dp2px(3f))
            titleText.measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            titleText.layout(0, 0, titleText.measuredWidth, titleText.measuredHeight)

            var hasAnyItem = false
            for (itemBean in dirBean.itemList) {
                if (itemBean.state != 0 || itemBean.inputText.isNotEmpty()) {
                    hasAnyItem = true
                    break
                }
            }
            if (hasAnyItem) {
                val tabTitleView = LayoutInflater.from(context).inflate(R.layout.pdf_tab_title, null)
                tabTitleView.measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                tabTitleView.layout(0, 0, tabTitleView.measuredWidth, tabTitleView.measuredHeight)

                val tabItemView = LayoutInflater.from(context).inflate(R.layout.pdf_tab_item, null)
                tabItemView.tv_item_name.text = dirBean.itemList[0].itemName
                tabItemView.tv_input.text = dirBean.itemList[0].inputText.ifEmpty { dirBean.itemList[0].getStateStr(context) }
                tabItemView.measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                tabItemView.layout(0, 0, tabItemView.measuredWidth, tabItemView.measuredHeight)

                if (hasUseHeight + margin + titleText.height + tabTitleView.measuredHeight + tabItemView.measuredHeight > pageHeight) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    hasUseHeight = 0
                } else {
                    canvas.drawRect(0f, 0f, pageWidth.toFloat(), margin.toFloat(), marginPaint)
                    canvas.translate(0f, margin.toFloat())
                    hasUseHeight += margin
                }

                titleText.draw(canvas)
                canvas.translate(0f, titleText.height.toFloat())
                hasUseHeight += titleText.height

                tabTitleView.draw(canvas)
                canvas.translate(0f, tabTitleView.height.toFloat())
                hasUseHeight += tabTitleView.height

                for (itemBean in dirBean.itemList) {
                    tabItemView.tv_item_name.text = itemBean.itemName
                    tabItemView.iv_good.isVisible = itemBean.state == 1
                    tabItemView.iv_warn.isVisible = itemBean.state == 2
                    tabItemView.iv_danger.isVisible = itemBean.state == 3
                    tabItemView.tv_input.text = itemBean.inputText.ifEmpty { itemBean.getStateStr(context) }
                    tabItemView.measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                    tabItemView.layout(0, 0, tabItemView.measuredWidth, tabItemView.measuredHeight)

                    if (hasUseHeight + tabItemView.height > pageHeight) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        canvas.translate(0f, SizeUtils.dp2px(13f).toFloat())
                        hasUseHeight = SizeUtils.dp2px(13f)
                        tabItemView.view_top_line.isVisible = true
                    } else {
                        tabItemView.view_top_line.isVisible = false
                    }

                    tabItemView.draw(canvas)
                    canvas.translate(0f, tabItemView.height.toFloat())
                    hasUseHeight += tabItemView.height
                }
            }

            val dataList: ArrayList<ImageInfo> = ArrayList()
            for (itemBean in dirBean.itemList) {
                if (itemBean.image1.isNotEmpty()) {
                    dataList.add(ImageInfo(itemBean.itemName, itemBean.image1))
                }
                if (itemBean.image2.isNotEmpty()) {
                    dataList.add(ImageInfo(itemBean.itemName, itemBean.image2))
                }
                if (itemBean.image3.isNotEmpty()) {
                    dataList.add(ImageInfo(itemBean.itemName, itemBean.image3))
                }
                if (itemBean.image4.isNotEmpty()) {
                    dataList.add(ImageInfo(itemBean.itemName, itemBean.image4))
                }
            }
            if (dataList.isEmpty()) {
                val paddingBottom = SizeUtils.dp2px(13f).coerceAtMost(pageHeight - hasUseHeight)
                canvas.translate(0f, paddingBottom.toFloat())
                hasUseHeight += paddingBottom
            } else {
                val photoText = TextView(context)
                photoText.textSize = 8f
                photoText.paint.isFakeBoldText = true
                photoText.setText(R.string.album_menu_Photos)
                photoText.setTextColor(0xff333333.toInt())
                photoText.setPadding(SizeUtils.dp2px(13f), SizeUtils.dp2px(10f), SizeUtils.dp2px(13f), 0)
                photoText.measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                photoText.layout(0, 0, photoText.measuredWidth, photoText.measuredHeight)

                val imgLineView = LayoutInflater.from(context).inflate(R.layout.pdf_image_line, null)
                imgLineView.tv_item_name1.text = dataList[0].itemName
                imgLineView.tv_item_name2.text = if (dataList.size > 1) dataList[1].itemName else ""
                imgLineView.tv_item_name3.text = if (dataList.size > 2) dataList[2].itemName else ""
                imgLineView.tv_item_name4.text = if (dataList.size > 3) dataList[3].itemName else ""
                imgLineView.measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                imgLineView.layout(0, 0, imgLineView.measuredWidth, imgLineView.measuredHeight)

                if (hasAnyItem) {
                    if (hasUseHeight + photoText.height + imgLineView.height > pageHeight) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        hasUseHeight = 0
                    }

                    photoText.draw(canvas)
                    canvas.translate(0f, photoText.height.toFloat())
                    hasUseHeight += photoText.height
                } else {
                    if (hasUseHeight + margin + titleText.height + photoText.height + imgLineView.height > pageHeight) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        hasUseHeight = 0
                    } else {
                        canvas.drawRect(0f, 0f, pageWidth.toFloat(), margin.toFloat(), marginPaint)
                        canvas.translate(0f, margin.toFloat())
                        hasUseHeight += margin
                    }

                    titleText.draw(canvas)
                    canvas.translate(0f, titleText.height.toFloat())
                    hasUseHeight += titleText.height

                    photoText.draw(canvas)
                    canvas.translate(0f, photoText.height.toFloat())
                    hasUseHeight += photoText.height
                }

                val lineCount = dataList.size / 4 + if (dataList.size % 4 == 0) 0 else 1
                for (i in 0 until lineCount) {
                    imgLineView.cl_image1.isVisible = dataList.size > i * 4
                    imgLineView.cl_image2.isVisible = dataList.size > i * 4 + 1
                    imgLineView.cl_image3.isVisible = dataList.size > i * 4 + 2
                    imgLineView.cl_image4.isVisible = dataList.size > i * 4 + 3
                    imgLineView.tv_item_name1.text = if (dataList.size > i * 4) dataList[i * 4].itemName else ""
                    imgLineView.tv_item_name2.text = if (dataList.size > i * 4 + 1) dataList[i * 4 + 1].itemName else ""
                    imgLineView.tv_item_name3.text = if (dataList.size > i * 4 + 2) dataList[i * 4 + 2].itemName else ""
                    imgLineView.tv_item_name4.text = if (dataList.size > i * 4 + 3) dataList[i * 4 + 3].itemName else ""

                    imgLineView.measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
                    imgLineView.layout(0, 0, imgLineView.measuredWidth, imgLineView.measuredHeight)

                    for (j in 0 until 4) {
                        if (dataList.size > i * 4 + j) {
                            val imageView = when (j) {
                                0 -> imgLineView.iv_image1
                                1 -> imgLineView.iv_image2
                                2 -> imgLineView.iv_image3
                                else -> imgLineView.iv_image4
                            }
                            val imagePath = dataList[i * 4 + j].imagePath
                            val drawable = Glide.with(context).asDrawable().load(imagePath).submit(imageView.width, imageView.height).get()
                            imageView.setImageDrawable(drawable)
                        }
                    }

                    if (hasUseHeight + imgLineView.height > pageHeight) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        canvas.translate(0f, SizeUtils.dp2px(13f).toFloat())
                        hasUseHeight = SizeUtils.dp2px(13f)
                    }

                    imgLineView.draw(canvas)
                    canvas.translate(0f, imgLineView.height.toFloat())
                    hasUseHeight += imgLineView.height
                }

                val paddingBottom = SizeUtils.dp2px(3f).coerceAtMost(pageHeight - hasUseHeight)
                canvas.translate(0f, paddingBottom.toFloat())
                hasUseHeight += paddingBottom
            }
        }

        val footView = buildFootView(context, houseReport)
        if (hasUseHeight + margin + footView.height > pageHeight) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas
        } else {
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), margin.toFloat(), marginPaint)
            canvas.translate(0f, margin.toFloat())
            hasUseHeight += margin
        }
        footView.draw(canvas)
        pdfDocument.finishPage(page)

        var pdfUri: Uri? = null
        try {
            if (Build.VERSION.SDK_INT < 29) {
                val pdfFile = File(FileConfig.documentsDir, houseReport.getPdfFileName())
                pdfDocument.writeTo(FileOutputStream(pdfFile))
                pdfUri = UriUtils.file2Uri(pdfFile)
            } else {
                val resolver: ContentResolver = context.contentResolver
                val contentUri: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, FileConfig.documentsDir)
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, houseReport.getPdfFileName())
                pdfUri = resolver.insert(contentUri, contentValues)
                if (pdfUri != null) {
                    val outputStream: OutputStream? = resolver.openOutputStream(pdfUri)
                    if (outputStream != null) {
                        pdfDocument.writeTo(outputStream)

                        val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
                        val selectionArgs: Array<String> = arrayOf(FileConfig.documentsDir, houseReport.getPdfFileName())
                        val cursor: Cursor? = resolver.query(contentUri, arrayOf(MediaStore.MediaColumns.DATA), selection, selectionArgs, null)
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                val data: String? = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                                if (data != null) {
                                    pdfUri = UriUtils.file2Uri(File(data))
                                }
                            }
                            cursor.close()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
        return@withContext pdfUri
    }

    private fun getPdfUri(context: Context, pdfFileName: String): Uri? {
        if (Build.VERSION.SDK_INT < 29) {
            val pdfFile = File(FileConfig.documentsDir, pdfFileName)
            return if (pdfFile.exists()) UriUtils.file2Uri(pdfFile) else null
        } else {
            val resolver: ContentResolver = context.contentResolver
            val contentUri: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} = ?"
            val selectionArgs: Array<String> = arrayOf(FileConfig.documentsDir, pdfFileName)
            val cursor: Cursor = resolver.query(contentUri, arrayOf(MediaStore.MediaColumns.DATA), selection, selectionArgs, null) ?: return null
            if (cursor.moveToFirst()) {
                val pdfFile = File(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)) ?: return null)
                cursor.close()
                return if (pdfFile.exists()) UriUtils.file2Uri(pdfFile) else null
            } else {
                cursor.close()
                return null
            }
        }
    }

    @SuppressLint("SetTextI18n,InflateParams")
    private fun buildHeadView(context: Context, houseReport: HouseReport): View {
        val pageWidth = ScreenUtil.getScreenWidth(context).coerceAtMost(ScreenUtil.getScreenHeight(context))
        val headView = LayoutInflater.from(context).inflate(R.layout.pdf_head, null)

        headView.tv_inspector_name_title.text = "${context.getString(R.string.inspector_name)}:"
        headView.tv_house_year_title.text = "${context.getString(R.string.house_build_time)}:"
        headView.tv_house_space_title.text = "${context.getString(R.string.house_space)}:"
        headView.tv_cost_title.text = "${context.getString(R.string.detection_cost)}:"

        headView.tv_report_name.text = houseReport.name
        headView.tv_time.text = context.getString(R.string.detect_time) + ": " + TimeUtils.millis2String(houseReport.detectTime, "yyyy-MM-dd HH:mm")
        headView.tv_house_address.text = houseReport.address

        headView.tv_inspector_name.text = houseReport.inspectorName
        headView.tv_house_year.text = houseReport.year?.toString() ?: "--"
        headView.tv_house_space.text = if (houseReport.houseSpace.isEmpty()) "--" else "${houseReport.houseSpace} ${houseReport.getSpaceUnitStr()}"
        headView.tv_cost.text = if (houseReport.cost.isEmpty()) "--" else "${houseReport.getCostUnitStr()} ${houseReport.cost}"

        val ivWidth = pageWidth - SizeUtils.dp2px(13f + 13f)
        val ivHeight = (pageWidth * 129 / 358f).toInt()
        val drawable = Glide.with(context).asDrawable().load(houseReport.imagePath).submit(ivWidth, ivHeight).get()
        headView.iv_house_image.setImageDrawable(drawable)

        headView.measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        headView.layout(0, 0, headView.measuredWidth, headView.measuredHeight)

        return headView
    }

    @SuppressLint("InflateParams")
    private fun buildFootView(context: Context, houseReport: HouseReport): View {
        val pageWidth = ScreenUtil.getScreenWidth(context).coerceAtMost(ScreenUtil.getScreenHeight(context))
        val footView = LayoutInflater.from(context).inflate(R.layout.pdf_foot, null)
        footView.measure(MeasureSpec.makeMeasureSpec(pageWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        footView.layout(0, 0, footView.measuredWidth, footView.measuredHeight)

        val inspectorWidth = footView.iv_inspector_signature.width
        val inspectorHeight = footView.iv_inspector_signature.height
        val inspectorDrawable = Glide.with(context).asDrawable().load(houseReport.inspectorBlackPath).submit(inspectorWidth, inspectorHeight).get()
        footView.iv_inspector_signature.setImageDrawable(inspectorDrawable)

        val ownerWidth = footView.iv_house_owner_signature.width
        val ownerHeight = footView.iv_house_owner_signature.height
        val ownerDrawable = Glide.with(context).asDrawable().load(houseReport.houseOwnerBlackPath).submit(ownerWidth, ownerHeight).get()
        footView.iv_house_owner_signature.setImageDrawable(ownerDrawable)

        return footView
    }

    private data class ImageInfo(val itemName: String, val imagePath: String)
}