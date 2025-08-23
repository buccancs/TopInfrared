package com.topdon.transfer

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topdon.lib.core.utils.ScreenUtil
import kotlinx.android.synthetic.main.dialog_transfer.view.*

class TransferDialog(context: Context) : Dialog(context, R.style.InfoDialog) {

    var max: Int
        get() = contentView.seek_bar.max
        set(value) {
            contentView.seek_bar.max = value
        }

    var progress: Int
        get() = contentView.seek_bar.progress
        set(value) {
            contentView.seek_bar.progress = value
        }

    private val contentView: View = LayoutInflater.from(context).inflate(R.layout.dialog_transfer, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        contentView.seek_bar.isEnabled = false
        setContentView(contentView)

        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = (ScreenUtil.getScreenWidth(context) * 0.84f).toInt()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }
    }
}