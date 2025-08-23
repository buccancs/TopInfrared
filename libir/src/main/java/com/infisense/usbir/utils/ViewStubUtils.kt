package com.infisense.usbir.utils

import android.view.View
import android.view.ViewStub
import android.view.animation.AnimationUtils
import com.infisense.usbir.extension.goneAlphaAnimation
import com.infisense.usbir.extension.visibleAlphaAnimation

object ViewStubUtils {
    fun showViewStub(viewStub: ViewStub?, isShow: Boolean, callback: ((view: View?) -> Unit)?) {
        if (viewStub != null) {
            if (isShow) {
                try {
                    val view = viewStub.inflate()
                    callback?.invoke(view)
                } catch (e: Exception) {
                    viewStub.visibility = View.VISIBLE
                }
            } else {
                viewStub.visibility = View.GONE
            }
        }
    }
}