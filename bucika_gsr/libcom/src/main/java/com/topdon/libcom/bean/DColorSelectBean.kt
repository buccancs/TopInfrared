package com.topdon.libcom.bean

@Deprecated("产品要求所有颜色拾取都更改为 ColorPickDialog 那种样式，这个弹框废弃")
data class DColorSelectBean(
    val colorRes: Int,
    val color: String,
    val code: Int,
)
