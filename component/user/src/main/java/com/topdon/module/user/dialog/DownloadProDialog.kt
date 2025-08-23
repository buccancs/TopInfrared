package com.topdon.module.user.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.module.user.R
import kotlinx.android.synthetic.main.dialog_download_pro.view.*
import java.text.DecimalFormat

class DownloadProDialog(context: Context) : Dialog(context, R.style.InfoDialog) {

    private val rootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_download_pro, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(rootView)

        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = (ScreenUtil.getScreenWidth(context) * 0.72).toInt()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }
    }

    fun refreshProgress(current: Long, total: Long) {
        val progress = (current * 100f / total).toInt()
        rootView.tv_size.text = "${context.getString(R.string.detail_len)}: ${getFileSizeStr(current)}/${getFileSizeStr(total)}"
        rootView.progress_bar.progress = progress
        rootView.tv_progress.text = "${progress}%"
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
}