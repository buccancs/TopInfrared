package com.topdon.lib.core.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.PopupWindow
import android.widget.TextView
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.R
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.tools.UnitTools
import kotlinx.android.synthetic.main.dialog_tip_emissivity.view.*

/**
 * des:
 * author: CaiSongL
 * date: 2024/4/7 14:59
 **/
class EmissivityTipPopup(val context: Context, val isTC007: Boolean) {
    private var text: String = ""
    private var radiation: Float = 0f
    private var distance: Float = 0f
    private var environment: Float = 0f
    private var popupWindow: PopupWindow? = null
    private lateinit var view: View
    private var titleText: TextView? = null
    private var messageText: TextView? = null
    private var checkBox: CheckBox? = null
    private var closeEvent: ((check: Boolean) -> Unit)? = null

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        view = inflater.inflate(R.layout.layout_popup_tip_emissivity, null)
    }

    fun setTitle(title: String): EmissivityTipPopup {
        titleText?.text = title
        return this
    }

    fun setMessage(message: String): EmissivityTipPopup {
        messageText?.text = message
        return this
    }

    fun setDataBean(environment: Float,distance : Float,radiation : Float,text : String): EmissivityTipPopup {
        this.environment = environment
        this.distance = distance
        this.radiation = radiation
        this.text = text
        return this
    }

    fun setCancelListener(event: ((check: Boolean) -> Unit)?): EmissivityTipPopup {
        this.closeEvent = event
        return this
    }

    fun build(): PopupWindow {
        if (popupWindow == null) {
            view.tv_environment_title.text = context.getString(R.string.thermal_config_environment) + ":"
            view.tv_distance_title.text = context.getString(R.string.thermal_config_distance) + ":"

            view.tv_title.visibility = View.GONE
            if (text.isNotEmpty()){
                view.tv_emissivity_materials.text = text
                view.tv_emissivity_materials.visibility = View.VISIBLE
            }else{
                view.tv_emissivity_materials.visibility = View.GONE
            }
            view.dialog_tip_cancel_btn.visibility = View.GONE
            view.dialog_tip_success_btn.text = context.getString(R.string.tc_modify_params)
            view.dialog_tip_check.visibility = View.GONE
            view.tv_emissivity.text = "${context?.getString(R.string.thermal_config_radiation)}: ${
                NumberTools.to02(radiation)}"
            view.tv_environment_value.text = UnitTools.showC(environment)
            view.tv_distance_value.text = "${NumberTools.to02(distance)}m"
            popupWindow = PopupWindow(
                view,
                SizeUtils.dp2px(275f),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            popupWindow?.apply {
                isFocusable = true
                isOutsideTouchable = true
                isTouchable = true
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 必要时可以替换为其他Drawable
            }
            view.dialog_tip_success_btn.setOnClickListener {
                ARouter.getInstance().build(RouterConfig.IR_SETTING).withBoolean(ExtraKeyConfig.IS_TC007, isTC007).navigation(context)
                dismiss()
            }
        }
        // 设置PopupWindow的其他属性和监听器...
        return popupWindow!!
    }

    fun show(anchorView: View) {
        popupWindow?.showAtLocation(anchorView, Gravity.CENTER, -SizeUtils.dp2px(10f), 0)
    }

    fun dismiss() {
        popupWindow?.dismiss()
        closeEvent?.invoke(checkBox?.isChecked ?: false)
    }
}