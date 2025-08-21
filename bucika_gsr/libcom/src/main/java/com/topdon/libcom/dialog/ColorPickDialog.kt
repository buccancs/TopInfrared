package com.topdon.libcom.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.SizeUtils
import com.jaygoo.widget.DefRangeSeekBar
import com.jaygoo.widget.OnRangeChangedListener
import com.topdon.lib.core.utils.ScreenUtil
import com.topdon.libcom.R
import com.topdon.libcom.util.ColorUtils
import kotlinx.android.synthetic.main.dialog_color_pick.nifty_slider_view
import kotlinx.android.synthetic.main.dialog_color_pick.tv_nifty_left
import kotlinx.android.synthetic.main.dialog_color_pick.tv_nifty_right
import kotlinx.android.synthetic.main.dialog_color_pick.tv_size_title
import kotlinx.android.synthetic.main.dialog_color_pick.tv_size_value
import kotlinx.android.synthetic.main.dialog_color_pick.view.*

/**
 * 颜色拾取弹框.
 *
 * Created by chenggeng.lin on 2023/12/18.
 */
class ColorPickDialog(context: Context, @ColorInt private var color: Int,var textSize: Int,var textSizeIsDP : Boolean = false) : Dialog(context, R.style.InfoDialog), View.OnClickListener {

    /**
     * 颜色值拾取事件监听.
     */
    var onPickListener: ((color: Int,textSize : Int) -> Unit)? = null

    private val rootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_color_pick, null)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setContentView(rootView)

        window?.let {
            val layoutParams = it.attributes
            layoutParams.width = (ScreenUtil.getScreenWidth(context) * 0.9).toInt()
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            it.attributes = layoutParams
        }

        val activeTrackColor =
            ColorUtils.setColorAlpha(ContextCompat.getColor(context, R.color.we_read_theme_color), 0.1f)
        val iconTintColor =
            ColorUtils.setColorAlpha(ContextCompat.getColor(context, R.color.we_read_theme_color), 0.7f)

        when (color) {
            0xff0000ff.toInt() -> rootView.view_color1.isSelected = true
            0xffff0000.toInt() -> rootView.view_color2.isSelected = true
            0xff00ff00.toInt() -> rootView.view_color3.isSelected = true
            0xffffff00.toInt() -> rootView.view_color4.isSelected = true
            0xff000000.toInt() -> rootView.view_color5.isSelected = true
            0xffffffff.toInt() -> rootView.view_color6.isSelected = true
            else -> rootView.color_select_view.selectColor(color)
        }

        rootView.color_select_view.onSelectListener = {
            unSelect6Color()
            color = it
        }
        if (textSize != -1){
            tv_size_title.visibility = View.VISIBLE
            tv_size_value.visibility = View.VISIBLE
            tv_nifty_left.visibility = View.VISIBLE
            tv_nifty_right.visibility = View.VISIBLE
            nifty_slider_view.visibility = View.VISIBLE
            nifty_slider_view.setOnRangeChangedListener(object : OnRangeChangedListener{
                override fun onRangeChanged(
                    view: DefRangeSeekBar?,
                    leftValue: Float,
                    rightValue: Float,
                    isFromUser: Boolean
                ) {
                    var text = "标准"
                    text = if (leftValue <= 0){
                        textSize = 14
                        context.getString(R.string.temp_text_standard)
                    }else if(leftValue <= 50){
                        textSize = 16
                        context.getString(R.string.temp_text_big)
                    }else{
                        textSize = 18
                        context.getString(R.string.temp_text_sup_big)
                    }
                    tv_size_value?.text = text
                }

                override fun onStartTrackingTouch(view: DefRangeSeekBar?, isLeft: Boolean) {
                }

                override fun onStopTrackingTouch(view: DefRangeSeekBar?, isLeft: Boolean) {
                }

            })
            nifty_slider_view.setProgress(textSizeToNifyValue(textSize,textSizeIsDP))
        }else{
            nifty_slider_view.visibility = View.GONE
        }
        rootView.view_color1.setOnClickListener(this)
        rootView.view_color2.setOnClickListener(this)
        rootView.view_color3.setOnClickListener(this)
        rootView.view_color4.setOnClickListener(this)
        rootView.view_color5.setOnClickListener(this)
        rootView.view_color6.setOnClickListener(this)
        rootView.rl_close.setOnClickListener(this)
        rootView.tv_save.setOnClickListener(this)
    }

    private fun textSizeToNifyValue(size: Int, isTC007: Boolean) : Float{
        if (isTC007){
            return when(size){
                14 -> 0f
                16 -> 50f
                else -> 100f
            }
        }
        return when(size){
            SizeUtils.sp2px(14f) -> 0f
            SizeUtils.sp2px(16f) -> 50f
            else -> 100f
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            rootView.rl_close -> dismiss()

            rootView.tv_save -> {//保存
                dismiss()
                onPickListener?.invoke(color,textSize)
            }

            rootView.view_color1 -> {
                unSelect6Color()
                rootView.color_select_view.reset()
                rootView.view_color1.isSelected = true
                color = 0xff0000ff.toInt()
            }
            rootView.view_color2 -> {
                unSelect6Color()
                rootView.color_select_view.reset()
                rootView.view_color2.isSelected = true
                color = 0xffff0000.toInt()
            }
            rootView.view_color3 -> {
                unSelect6Color()
                rootView.color_select_view.reset()
                rootView.view_color3.isSelected = true
                color = 0xff00ff00.toInt()
            }
            rootView.view_color4 -> {
                unSelect6Color()
                rootView.color_select_view.reset()
                rootView.view_color4.isSelected = true
                color = 0xffffff00.toInt()
            }
            rootView.view_color5 -> {
                unSelect6Color()
                rootView.color_select_view.reset()
                rootView.view_color5.isSelected = true
                color = 0xff000000.toInt()
            }
            rootView.view_color6 -> {
                unSelect6Color()
                rootView.color_select_view.reset()
                rootView.view_color6.isSelected = true
                color = 0xffffffff.toInt()
            }
        }
    }

    /**
     * 将 6 个固定的颜色按钮重置为未选中状态.
     */
    private fun unSelect6Color() {
        rootView.view_color1.isSelected = false
        rootView.view_color2.isSelected = false
        rootView.view_color3.isSelected = false
        rootView.view_color4.isSelected = false
        rootView.view_color5.isSelected = false
        rootView.view_color6.isSelected = false
    }
}