package com.topdon.libcom.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.topdon.lib.core.tools.UnitTools
import com.topdon.libcom.AlarmHelp
import com.topdon.libcom.R
import com.topdon.lib.core.bean.AlarmBean
import com.topdon.lib.core.common.SaveSettingUtil
import com.topdon.lib.core.tools.ToastTools
import kotlinx.android.synthetic.main.dialog_temp_alarm_set.*

class TempAlarmSetDialog(
    context: Context,
    private val isEdit: Boolean,
) : Dialog(context, R.style.app_compat_dialog), CompoundButton.OnCheckedChangeListener {

    var alarmBean = AlarmBean()
        set(value) {
            field = value.copy()
        }

    /**
     * 保存点击事件监听.
     */
    var onSaveListener: ((alarmBean: AlarmBean) -> Unit)? = null

    /**
     * 用于播放报警铃声.
     */
    private var mediaPlayer: MediaPlayer? = null

    public var hideAlarmMark = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(LayoutInflater.from(context).inflate(R.layout.dialog_temp_alarm_set, null))
        initView()

        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.attributes = layoutParams
        }
    }

    override fun onBackPressed() {
        dismiss()
    }

    private fun initView() {
        cl_root.setOnClickListener { dismiss() }
        cl_close.setOnClickListener { dismiss() }
        tv_save.setOnClickListener { save() }
        iv_ringtone1.setOnClickListener { selectRingtone(0) }
        iv_ringtone2.setOnClickListener { selectRingtone(1) }
        iv_ringtone3.setOnClickListener { selectRingtone(2) }
        iv_ringtone4.setOnClickListener { selectRingtone(3) }
        iv_ringtone5.setOnClickListener { selectRingtone(4) }
        switch_alarm_high.setOnCheckedChangeListener(this)
        switch_alarm_low.setOnCheckedChangeListener(this)
        switch_alarm_mark.setOnCheckedChangeListener(this)
        switch_alarm_ringtone.setOnCheckedChangeListener(this)

        img_mark_high.setOnClickListener {
            showColorDialog(true)
        }
        img_mark_low.setOnClickListener {
            showColorDialog(false)
        }
        iv_check_stoke.setOnClickListener {
            if (!iv_check_stoke.isSelected) {
                iv_check_stoke.isSelected = true
                iv_check_matrix.isSelected = false
                alarmBean.markType = AlarmBean.TYPE_ALARM_MARK_STROKE
            }
        }
        iv_check_matrix.setOnClickListener {
            if (!iv_check_matrix.isSelected) {
                iv_check_stoke.isSelected = false
                iv_check_matrix.isSelected = true
                alarmBean.markType = AlarmBean.TYPE_ALARM_MARK_MATRIX
            }
        }

        tv_alarm_high_unit.text = UnitTools.showUnit()
        tv_alarm_low_unit.text = UnitTools.showUnit()
    }

    override fun show() {
        super.show()
        refreshAlarmView()
    }

    private fun refreshAlarmView() {
        switch_alarm_high.isChecked = alarmBean.isHighOpen
        switch_alarm_low.isChecked = alarmBean.isLowOpen
        switch_alarm_mark.isChecked = isEdit || alarmBean.isMarkOpen
        if (!isEdit) {
            switch_alarm_ringtone.isChecked = alarmBean.isRingtoneOpen
        }
        iv_check_stoke.isSelected = alarmBean.markType == AlarmBean.TYPE_ALARM_MARK_STROKE
        iv_check_matrix.isSelected = alarmBean.markType == AlarmBean.TYPE_ALARM_MARK_MATRIX
        Glide.with(context).load(ColorDrawable(alarmBean.highColor)).into(img_c_alarm_high)
        Glide.with(context).load(ColorDrawable(alarmBean.lowColor)).into(img_c_alarm_low)

        et_alarm_high.isEnabled = switch_alarm_high.isChecked
        et_alarm_low.isEnabled = switch_alarm_low.isChecked
        cl_alarm_mark.isVisible = isEdit || switch_alarm_mark.isChecked
        cl_ringtone_select.isVisible = !isEdit && switch_alarm_ringtone.isChecked
        tv_alarm_ringtone.isVisible = !isEdit
        switch_alarm_ringtone.isVisible = !isEdit
        if (hideAlarmMark){
            tv_alarm_mark.visibility = View.GONE
            switch_alarm_mark.visibility = View.GONE
            cl_alarm_mark.visibility = View.GONE
        }
        switch_alarm_mark.isVisible = !isEdit
        if (alarmBean.highTemp == Float.MAX_VALUE) {
            et_alarm_high.setText("")
        } else {
            et_alarm_high.setText(UnitTools.showUnitValue(alarmBean.highTemp).toString())
        }
        if (alarmBean.lowTemp == Float.MIN_VALUE) {
            et_alarm_low.setText("")
        } else {
            et_alarm_low.setText(UnitTools.showUnitValue(alarmBean.lowTemp).toString())
        }
        iv_ringtone1.isSelected = false
        iv_ringtone2.isSelected = false
        iv_ringtone3.isSelected = false
        iv_ringtone4.isSelected = false
        iv_ringtone5.isSelected = false
        when (alarmBean.ringtoneType) {
            0 -> iv_ringtone1.isSelected = true
            1 -> iv_ringtone2.isSelected = true
            2 -> iv_ringtone3.isSelected = true
            3 -> iv_ringtone4.isSelected = true
            4 -> iv_ringtone5.isSelected = true
        }
    }

    private fun save() {
        try {
            val inputHigh = if (switch_alarm_high.isChecked) {
                if (et_alarm_high.text.isNotEmpty()) UnitTools.showToCValue(et_alarm_high.text.toString().toFloat()) else null
            } else {
                null
            }
            val inputLow = if (switch_alarm_low.isChecked) {
                if (et_alarm_low.text.isNotEmpty()) UnitTools.showToCValue(et_alarm_low.text.toString().toFloat()) else null
            } else {
                null
            }
            if (inputHigh != null && inputLow != null && inputLow > inputHigh) {
                ToastTools.showShort(R.string.tip_input_format)
                return
            }
        } catch (e: Exception) {
            ToastTools.showShort(R.string.tip_input_format)
            return
        }

        val inputHigh = if (et_alarm_high.text.isNotEmpty()) et_alarm_high.text.toString() else ""
        val inputLow = if (et_alarm_low.text.isNotEmpty()) et_alarm_low.text.toString() else ""
        var highValue: Float? = null
        var lowValue: Float? = null
        try {
            highValue = if (inputHigh.isNotEmpty()) UnitTools.showToCValue(inputHigh.toFloat()) else null
            lowValue = if (inputLow.isNotEmpty()) UnitTools.showToCValue(inputLow.toFloat()) else null
        } catch (_: Exception) {

        }
        alarmBean.highTemp = highValue ?: Float.MAX_VALUE
        alarmBean.lowTemp = lowValue ?: Float.MIN_VALUE
        alarmBean.isHighOpen = switch_alarm_high.isChecked
        alarmBean.isLowOpen = switch_alarm_low.isChecked
        alarmBean.isRingtoneOpen = switch_alarm_ringtone.isChecked

        onSaveListener?.invoke(alarmBean)

        dismiss()
    }

    private fun showColorDialog(isHigh: Boolean) {
        val colorPickDialog = ColorPickDialog(context, if (isHigh) alarmBean.highColor else alarmBean.lowColor,-1)
        colorPickDialog.onPickListener = { it: Int, i1: Int ->
            if (isHigh) {
                alarmBean.highColor = it
                Glide.with(context).load(ColorDrawable(it)).into(img_c_alarm_high)
            } else {
                alarmBean.lowColor = it
                Glide.with(context).load(ColorDrawable(it)).into(img_c_alarm_low)
            }
        }
        colorPickDialog.show()
    }

    override fun dismiss() {
        super.dismiss()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (_: Exception) {

        }

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.switch_alarm_high -> {//高温报警
                et_alarm_high.isEnabled = isChecked
                alarmBean.isHighOpen = isChecked
            }

            R.id.switch_alarm_low -> {//低温报警
                et_alarm_low.isEnabled = isChecked
                alarmBean.isLowOpen = isChecked
            }

            R.id.switch_alarm_mark -> {//区域标记
                cl_alarm_mark.isVisible = isChecked
                alarmBean.isMarkOpen = isChecked
            }

            R.id.switch_alarm_ringtone -> {//报警铃声
                cl_ringtone_select.isVisible = isChecked
                if (isChecked) {
                    selectRingtone(alarmBean.ringtoneType)
                } else {
                    selectRingtone(null)
                }
            }
        }
    }

    /**
     * 设置当前选中的铃声，null 表示关闭.
     */
    private fun selectRingtone(position: Int?) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        } catch (_: Exception) {
        }
        if (position == null) {
            return
        }
        alarmBean.ringtoneType = position

        iv_ringtone1.isSelected = false
        iv_ringtone2.isSelected = false
        iv_ringtone3.isSelected = false
        iv_ringtone4.isSelected = false
        iv_ringtone5.isSelected = false
        when (position) {
            0 -> iv_ringtone1.isSelected = true
            1 -> iv_ringtone2.isSelected = true
            2 -> iv_ringtone3.isSelected = true
            3 -> iv_ringtone4.isSelected = true
            4 -> iv_ringtone5.isSelected = true
        }
        when (position) {
            0 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone1)
            1 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone2)
            2 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone3)
            3 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone4)
            4 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone5)
        }
        mediaPlayer?.start()
    }
}