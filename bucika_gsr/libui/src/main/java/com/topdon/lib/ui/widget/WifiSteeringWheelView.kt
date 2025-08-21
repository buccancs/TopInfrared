package com.topdon.lib.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import com.topdon.lib.ui.R
import kotlinx.android.synthetic.main.ui_wifi_steering_wheel_view.view.*

/**
 * 校准方向
 */
class WifiSteeringWheelView : LinearLayout, OnClickListener {

    var listener: ((action: Int, moveX: Int,moveY:Int) -> Unit)? = null
    var moveX = 0
    var moveY = 0
    var rotationIR = 270
    set(value) {
        field = value
        if (value == 270 || value == 90){
            tv_confirm?.rotation = 270f
            rotation = 90f
        }else{
            tv_confirm?.rotation = 0f
            rotation = 0f
        }
        requestLayout()
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun initView() {
        inflate(context, R.layout.ui_wifi_steering_wheel_view, this)
        steering_wheel_start_btn.setOnClickListener(this)
        steering_wheel_center_btn.setOnClickListener(this)
        steering_wheel_end_btn.setOnClickListener(this)
        steering_wheel_top_btn.setOnClickListener(this)
        steering_wheel_bottom_btn.setOnClickListener(this)
        if (rotationIR == 270 || rotationIR == 90){
            tv_confirm.rotation = 270f
            rotation = 90f
        }else{
            tv_confirm.rotation = 0f
            rotation = 0f
        }
    }

    val moveI = 2
    override fun onClick(v: View?) {
        when (v) {
            steering_wheel_start_btn -> {
//                moveY -= moveI
                listener?.invoke(-1, moveX,moveY)
            }
            steering_wheel_center_btn -> {
                listener?.invoke(0, moveX,moveY)
            }
            steering_wheel_top_btn -> {
//                moveX += moveI
                listener?.invoke(2, moveX,moveY)
            }
            steering_wheel_bottom_btn ->{
//                moveX -= moveI
                listener?.invoke(3, moveX,moveY)
            }
            steering_wheel_end_btn -> {
//                moveY += moveI
                listener?.invoke(1,moveX,moveY)
            }
        }
    }


}