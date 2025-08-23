package com.topdon.lib.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.tools.ToastTools
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.lib.ui.R
import com.topdon.lib.ui.adapter.ColorSelectAdapter
import kotlinx.android.synthetic.main.dialog_thermal_input.view.*
import java.math.BigDecimal

class ThermalInputDialog : Dialog {

    private var action = 100

    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    override fun onBackPressed() {

    }

    class Builder {
        var dialog: ThermalInputDialog? = null

        private var context: Context? = null

        private var listener: ((s: Int) -> Unit)? = null

        private var message: Spanned? = null
        private var positiveStr: String? = null
        private var cancelStr: String? = null
        private var positiveEvent: ((up: Float, down: Float, upColor: Int, downColor: Int) -> Unit)? =
            null
        private var cancelEvent: (() -> Unit)? = null
        private var canceled = false
        private var saturation = 0
        private var upColor = Color.parseColor("#FFF3812F")
        private var downColor = Color.parseColor("#FF28C445")
        private var selectColor = 0
        private var max = 0f
        private var min = 0f
        private var maxColor = 0
        private var minColor = 0

        private lateinit var messageText: TextView
        private lateinit var successBtn: Button
        private lateinit var cancelBtn: Button
        private lateinit var upEdit: EditText
        private lateinit var downEdit: EditText
        private lateinit var upUnit: TextView
        private lateinit var downUnit: TextView
        private lateinit var colorPickerView: ColorPickerView
        private lateinit var recycler: RecyclerView
        private var isIconEdit: Boolean = false

        constructor(context: Context) {
            this.context = context
        }

        fun setMessage(message: String): Builder {
            this.message = HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY)
            return this
        }

        fun setMessage(message: Spanned): Builder {
            this.message = message
            return this
        }

        fun setMessage(@StringRes message: Int): Builder {
            this.message =
                HtmlCompat.fromHtml(context!!.getString(message), HtmlCompat.FROM_HTML_MODE_LEGACY)
            return this
        }

        fun setIconEdit(isIconEdit: Boolean): Builder {
            this.isIconEdit =isIconEdit
            return this
        }

        fun setEditNum(max: Float, min: Float): Builder {
            this.max = max
            this.min = min
            return this
        }

        fun setNum(max: Float, min: Float): Builder {
            if (SharedManager.getTemperature() == 1) {
                this.max = max
                this.min = min
            } else {
                this.max = UnitTools.toF(max)
                this.min = UnitTools.toF(min)
            }
            return this
        }

        fun setColor(@ColorInt maxColor: Int, @ColorInt minColor: Int): Builder {
            this.maxColor = maxColor
            this.minColor = minColor
            return this
        }

        @JvmOverloads
        fun setPositiveListener(
            @StringRes strRes: Int,
            event: ((up: Float, down: Float, upColor: Int, downColor: Int) -> Unit)? = null
        ): Builder {
            return setPositiveListener(context!!.getString(strRes), event)
        }

        @JvmOverloads
        fun setPositiveListener(
            str: String,
            event: ((up: Float, down: Float, upColor: Int, downColor: Int) -> Unit)? = null
        ): Builder {
            this.positiveStr = str
            this.positiveEvent = event
            return this
        }

        @JvmOverloads
        fun setCancelListener(@StringRes strRes: Int, event: (() -> Unit)? = null): Builder {
            return setCancelListener(context!!.getString(strRes), event)
        }

        @JvmOverloads
        fun setCancelListener(str: String, event: (() -> Unit)? = null): Builder {
            this.cancelStr = str
            this.cancelEvent = event
            return this
        }

        fun setCanceled(canceled: Boolean): Builder {
            this.canceled = canceled
            return this
        }

        fun setSaturation(s: Int): Builder {
            this.saturation = s
            return this
        }

        fun setListener(event: ((s: Int) -> Unit)? = null): Builder {
            this.listener = event
            return this
        }

        fun dismiss() {
            this.dialog!!.dismiss()
        }

        private val adapter by lazy { ColorSelectAdapter(context!!) }

        fun create(): ThermalInputDialog {
            if (dialog == null) {
                dialog = ThermalInputDialog(context!!, R.style.InfoDialog)
            }
            val inflater =
                context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater.inflate(R.layout.dialog_thermal_input, null)
            messageText = view.dialog_tip_msg_text
            successBtn = view.dialog_tip_success_btn
            cancelBtn = view.dialog_tip_cancel_btn
            upEdit = view.dialog_up_edit
            downEdit = view.dialog_down_edit
            upUnit = view.dialog_up_unit_text
            downUnit = view.dialog_down_unit_text
            colorPickerView = view.color_picker_view
            recycler = view.color_picker_recycler
            view.color_picker_view_lay.visibility = View.GONE
            view.dialog_input_lay.visibility = View.VISIBLE
            if(isIconEdit){
                view.dialog_up_color.visibility = View.GONE
                view.dialog_down_color.visibility = View.GONE
            }else{
                view.dialog_up_color.visibility = View.VISIBLE
                view.dialog_down_color.visibility = View.VISIBLE
            }
            messageText.text = message
            if (maxColor != 0) upColor = maxColor
            if (minColor != 0) downColor = minColor
            upUnit.text = UnitTools.showUnit()
            downUnit.text = UnitTools.showUnit()
            view.dialog_up_color.setColorFilter(upColor)
            view.dialog_down_color.setColorFilter(downColor)
            colorPickerView.setInitialColor(upColor)

            recycler.layoutManager = GridLayoutManager(context!!, 6)
            recycler.adapter = adapter
            adapter.listener = { _, color ->
                selectColor = color
                colorPickerView.setInitialColor(Color.parseColor("#FFFFFFFF"))
            }

            dialog!!.addContentView(
                view,
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            )
            val lp = dialog!!.window!!.attributes
            val wRatio =
                if (context!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    0.85
                } else {
                    0.35
                }
            lp.width = (ScreenUtil.getScreenWidth(context!!) * wRatio).toInt()
            dialog!!.window!!.attributes = lp

            dialog!!.setCanceledOnTouchOutside(canceled)
            successBtn.setOnClickListener {

                if (view.color_picker_view_lay.isVisible) {
                    view.color_picker_view_lay.visibility = View.GONE
                    view.dialog_input_lay.visibility = View.VISIBLE
                    messageText.text = message
                    if (dialog!!.action == 201) {
                        if (selectColor != 0) {
                            upColor = selectColor
                        }
                        view.dialog_up_color.setColorFilter(upColor)
                    }
                    if (dialog!!.action == 301) {
                        if (selectColor != 0) {
                            downColor = selectColor
                        }
                        view.dialog_down_color.setColorFilter(downColor)
                    }
                    dialog!!.action = 100
                    return@setOnClickListener
                }
                if (upEdit.text.isNullOrEmpty() || downEdit.text.isNullOrEmpty()){
                    ToastTools.showShort(com.topdon.lib.core.R.string.ui_fill_in_the_complete)
                    return@setOnClickListener
                }

                val upValue = upEdit.text.trim().toString()
                val downValue = downEdit.text.trim().toString()
                try {
                    if (upValue.toFloat() < downValue.toFloat()){
                        ToastTools.showShort(com.topdon.lib.core.R.string.tip_input_format)
                        return@setOnClickListener
                    }
                }catch (e:Exception){
                    ToastTools.showShort(com.topdon.lib.core.R.string.tip_input_format)
                    return@setOnClickListener
                }
                if (sub(upValue,downValue) < 0.1f){
                    ToastTools.showShort(com.topdon.lib.core.R.string.tip_input_format)
                    return@setOnClickListener
                }
                if ((upValue.isNotBlank() && downValue.isNotBlank()) && upValue.toFloat() < downValue.toFloat()) {
                    ToastTools.showShort(R.string.tip_input_format)
                    return@setOnClickListener
                }

                dismiss()
                if(isIconEdit){
                    positiveEvent?.invoke(
                        if (upValue.isBlank()) -273f else upValue.toFloat(),
                        if (downValue.isBlank()) -273f else downValue.toFloat(),
                        upColor,
                        downColor
                    )
                }else{
                    if (SharedManager.getTemperature() == 1) {
                        positiveEvent?.invoke(
                            if (upValue.isBlank()) -273f else upValue.toFloat(),
                            if (downValue.isBlank()) -273f else downValue.toFloat(),
                            upColor,
                            downColor
                        )
                    } else {
                        positiveEvent?.invoke(
                            if (upValue.isBlank()) -273f else UnitTools.toC(upValue.toFloat()),
                            if (downValue.isBlank()) -273f else UnitTools.toC(downValue.toFloat()),
                            upColor,
                            downColor
                        )
                    }
                }
            }
            cancelBtn.setOnClickListener {
                if (view.color_picker_view_lay.isVisible) {
                    view.color_picker_view_lay.visibility = View.GONE
                    view.dialog_input_lay.visibility = View.VISIBLE
                    messageText.text = message
                    dialog!!.action = 100
                    return@setOnClickListener
                }
                dismiss()
                cancelEvent?.invoke()
            }
            view.dialog_up_color.setOnClickListener {
                dialog!!.action = 201
                view.color_picker_view_lay.visibility = View.VISIBLE
                view.dialog_input_lay.visibility = View.GONE
                messageText.text = context!!.getString(R.string.color_board)
                colorPickerView.setInitialColor(upColor)
            }
            view.dialog_down_color.setOnClickListener {
                dialog!!.action = 301
                view.color_picker_view_lay.visibility = View.VISIBLE
                view.dialog_input_lay.visibility = View.GONE
                messageText.text = context!!.getString(R.string.color_board)
                colorPickerView.setInitialColor(downColor)
            }
            colorPickerView.setColorListener(object : ColorEnvelopeListener {

                override fun onColorSelected(envelope: ColorEnvelope, fromUser: Boolean) {
                    if ("#${envelope.hexCode}" != "#FFFFFFFF") {
                        adapter.selected(-1)
                        selectColor = 0
                    }
                    if (dialog!!.action == 201) {
                        upColor = Color.parseColor("#${envelope.hexCode}")
                    } else if (dialog!!.action == 301) {
                        downColor = Color.parseColor("#${envelope.hexCode}")
                    }
                }

            })
            if ((max == -273f && SharedManager.getTemperature() == 1) || (max == -459.4f && SharedManager.getTemperature() != 1)) {
                upEdit.setText("")
            } else {
                upEdit.setText(NumberTools.scale(max, 1).toString())
            }
            if ((min == -273f && SharedManager.getTemperature() == 1) || (min == -459.4f && SharedManager.getTemperature() != 1)) {
                downEdit.setText("")
            } else {
                downEdit.setText(NumberTools.scale(min, 1).toString())
            }
            if (positiveStr != null) {
                successBtn.text = positiveStr
            }
            if (!TextUtils.isEmpty(cancelStr)) {
                cancelBtn.visibility = View.VISIBLE
                cancelBtn.text = cancelStr
            } else {
                cancelBtn.visibility = View.GONE
                cancelBtn.text = ""
            }
            if (message != null) {
                messageText.visibility = View.VISIBLE
                messageText.setText(message, TextView.BufferType.NORMAL)
            } else {
                messageText.visibility = View.GONE
            }
            dialog!!.setContentView(view)
            return dialog as ThermalInputDialog
        }

        fun sub(doubleValA: String?, doubleValB: String?): Float {
            val a2 = BigDecimal(doubleValA)
            val b2 = BigDecimal(doubleValB)
            return a2.subtract(b2).toFloat()
        }
    }

}