package com.topdon.lib.core.bean.base

import android.text.TextUtils

class Resp<T> {

    var code: String = ""
    var msg: String = ""
    var data: T? = null

    fun isSuccess(): Boolean {
        return TextUtils.equals(code, "0")
    }

    override fun toString(): String {
        return "Resp(code='$code', msg='$msg', data=$data)"
    }

}
