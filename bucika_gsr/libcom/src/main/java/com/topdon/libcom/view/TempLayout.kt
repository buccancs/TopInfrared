package com.topdon.libcom.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.topdon.libcom.R


/**
 *
 * 高低温闪烁动画
 * @author: CaiSongL
 * @date: 2023/4/28 15:52
 */
class TempLayout : LinearLayout {
    companion object{
        val TYPE_HOT = 1 //高温预警
        val TYPE_LT = 2 //低温预警
        val TYPE_A = 3  //高低温交叉预警
    }

    private var alphaAnimator: ObjectAnimator? = null
    var rootV : View?= null
    var bg : View ? = null
    var isHot : Boolean  = true
    var type  = -1

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    var animatorAlpha = 1f

    private fun initView() {
        rootV = LayoutInflater.from(context).inflate(R.layout.layout_temp_bg, this)
        bg = rootV?.findViewById(R.id.bg)
        alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
        alphaAnimator?.duration = 500
        alphaAnimator?.interpolator =
            BreatheInterpolator() //使用自定义的插值器
        alphaAnimator?.addUpdateListener {
            animatorAlpha = it.getAnimatedValue("alpha") as Float
//            Log.w("透明值进度","$animatorAlpha")
        }
        alphaAnimator?.repeatCount = ValueAnimator.INFINITE
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )



    fun startAnimation(type : Int){
        this.visibility = View.VISIBLE
        if (this.type != type){
            alphaAnimator?.cancel()
            alphaAnimator?.removeAllListeners()
            when (type) {
                TYPE_HOT -> {
                    isHot = true
                    alphaAnimator?.repeatCount = ValueAnimator.INFINITE
                    bg?.setBackgroundResource(R.drawable.ic_ir_read_bg)
                }
                TYPE_A -> {
                    alphaAnimator?.repeatCount = 0
                    alphaAnimator?.addListener(animatorListener)
                }
                else -> {
                    alphaAnimator?.repeatCount = ValueAnimator.INFINITE
                    isHot = false
                    bg?.setBackgroundResource(R.drawable.ic_ir_blue_bg)
                }
            }
            alphaAnimator?.start()
            this.type = type
        }

    }

    var animatorListener: Animator.AnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
        }
        override fun onAnimationEnd(animation: Animator) {
            if (this@TempLayout.visibility == View.VISIBLE){
                isHot = !isHot
                if (isHot){
                    bg?.setBackgroundResource(R.drawable.ic_ir_read_bg)
                }else{
                    bg?.setBackgroundResource(R.drawable.ic_ir_blue_bg)
                }
                alphaAnimator?.start()
            }
        }
        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
    }

    fun stopAnimation(){
        this.type = -1
        alphaAnimator?.removeAllListeners()
        this.visibility = View.GONE
        alphaAnimator?.cancel()
    }

    fun startAlphaBreathAnimation() {
        alphaAnimator?.start()
    }

}