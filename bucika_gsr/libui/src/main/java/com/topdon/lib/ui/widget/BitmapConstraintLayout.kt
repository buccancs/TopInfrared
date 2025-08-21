package com.topdon.lib.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.drawToBitmap

/**
 * @author: CaiSongL
 * @date: 2023/6/21 17:13
 */
open class BitmapConstraintLayout : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @Volatile
    var viewBitmap: Bitmap? = null

    fun updateBitmap() {
        if (!isShown) {
            return
        }
        try {
            viewBitmap = this.drawToBitmap()
        } catch (_: Exception) {
        }
    }
}