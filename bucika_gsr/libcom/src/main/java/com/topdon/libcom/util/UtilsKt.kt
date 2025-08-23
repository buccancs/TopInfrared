package com.topdon.libcom.util

import com.blankj.utilcode.util.SizeUtils

val Float.dp: Float
    get() =  ColorUtils.dpToPxF(this)
val Int.dp: Int
    get() =  ColorUtils.dpToPx(this)
val Float.sp : Float
    get() = SizeUtils.sp2px(this).toFloat()
val Int.sp : Float
    get() = SizeUtils.sp2px(this.toFloat()).toFloat()

