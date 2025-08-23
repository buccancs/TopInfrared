package com.topdon.module.thermal.ir.view

import android.content.Context
import android.graphics.Canvas
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.animation.*
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.SizeUtils
import java.util.*

public class TimeDownView : AppCompatTextView {
    private var timer: Timer? = null
    private var downTimerTask: DownTimerTask? = null
    private var downCount = 0
    private var lastDown = 0
    private var intervalMills: Long = 0
    private var delayMills: Long = 0
    private var animationSet: AnimationSet? = null
    var isRunning = false
    private fun init() {
        if (animationSet == null) {
            animationSet = AnimationSet(true)
        }
        if (downHandler == null) {
            downHandler = DownHandler()
        }
        gravity = Gravity.CENTER
        textSize = SizeUtils.sp2px(30f).toFloat()
    }
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    fun downSecond(seconds: Int) {
        downSecond(seconds,true)
    }

    fun downSecond(seconds: Int,openAnimation: Boolean) {
        if (seconds == 0){
            isRunning = false
            visibility = GONE
            downTimeWatcher?.onLastTimeFinish(seconds)
            onFinishListener?.invoke()
        }else{
            visibility = VISIBLE
            isRunning = true
            downTime(seconds, 1, 0, 1000,openAnimation)
        }
    }

    fun downTime(downCount: Int, lastDown: Int, delayMills: Long, intervalMills: Long,startAnimate : Boolean) {
        timer = Timer()
        this.downCount = downCount
        this.lastDown = lastDown
        this.delayMills = delayMills
        this.intervalMills = intervalMills
        if (startAnimate){
            initDefaultAnimate()
        }
        downTimerTask = DownTimerTask()
        timer?.schedule(downTimerTask, delayMills, intervalMills)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (GONE == visibility) {
            downTimerTask = null
            timer?.cancel()
            timer = null
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (drawTextFlag == DRAW_TEXT_NO) {
            return
        }
        super.onDraw(canvas)
    }

    fun cancel() {
        animationSet?.cancel()
        downTimerTask?.cancel()
        timer?.cancel()
        drawTextFlag = DRAW_TEXT_NO
        invalidate()
        visibility = GONE
        downTimerTask = null
        timer = null
        isRunning = false
    }

    private inner class DownTimerTask : TimerTask() {
        override fun run() {
            if (downCount >= lastDown - 1) {
                val msg = Message.obtain()
                msg.what = 1
                downHandler!!.sendMessage(msg)
            }
        }
    }

    interface DownTimeWatcher {
        fun onTime(num: Int)
        fun onLastTime(num: Int)
        fun onLastTimeFinish(num: Int)
    }

    var onTimeListener: ((time: Int) -> Unit)? = null
    
    var onFinishListener: (() -> Unit)? = null

    var downTimeWatcher: DownTimeWatcher? = null

    fun setOnTimeDownListener(downTimeWatcher: DownTimeWatcher?) {
        this.downTimeWatcher = downTimeWatcher
    }

    private var downHandler: DownHandler? = null

    private inner class DownHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 1) {
                if (downTimeWatcher != null) {
                    downTimeWatcher!!.onTime(downCount)
                }
                onTimeListener?.invoke(downCount)
                if (downCount >= lastDown - 1) {
                    drawTextFlag = DRAW_TEXT_YES
                    if (downCount >= lastDown) {
                        text = downCount.toString() + ""
                        startDefaultAnimate()
                        if (downCount == lastDown && downTimeWatcher != null) {
                            downTimeWatcher!!.onLastTime(downCount)
                        }
                    } else if (downCount == lastDown - 1) {
                        if (afterDownDimissFlag == AFTER_LAST_TIME_DIMISS) {
                            drawTextFlag = DRAW_TEXT_NO
                        }
                        invalidate()
                        isRunning = false
                        downTimerTask == null
                        timer?.cancel()
                        timer = null
                        if (downTimeWatcher != null) {
                            downTimeWatcher!!.onLastTimeFinish(downCount)
                        }
                        onFinishListener?.invoke()
                    }
                    downCount--
                }
            }
        }
    }

    private val DRAW_TEXT_YES = 1
    private val DRAW_TEXT_NO = 0

    private var drawTextFlag = DRAW_TEXT_YES
    private val AFTER_LAST_TIME_DIMISS = 1
    private val AFTER_LAST_TIME_NODIMISS = 0

    private var afterDownDimissFlag = AFTER_LAST_TIME_DIMISS

    fun setAfterDownNoDimiss() {
        afterDownDimissFlag = AFTER_LAST_TIME_NODIMISS
    }

    fun setAferDownDimiss() {
        afterDownDimissFlag = AFTER_LAST_TIME_DIMISS
    }

    var startDefaultAnimFlag = true

    fun closeDefaultAnimate() {
        animationSet?.reset()
        startDefaultAnimFlag = false
    }

    private fun startDefaultAnimate() {
        if (startDefaultAnimFlag) {
            animation?.start()
        }
    }

    private fun initDefaultAnimate() {
        if (animationSet == null) {
            animationSet = AnimationSet(true)
        }
        val scaleAnimation = ScaleAnimation(
            1f,
            0.5f,
            1f,
            0.5f,
            ScaleAnimation.ABSOLUTE,
            measuredWidth / 2f,
            ScaleAnimation.ABSOLUTE,
            measuredHeight / 2f
        )
        scaleAnimation.duration = intervalMills
        val alphaAnimation = AlphaAnimation(1f, 0.3f)
        alphaAnimation.duration = intervalMills
        animationSet!!.addAnimation(scaleAnimation)
        animationSet!!.addAnimation(alphaAnimation)
        animationSet!!.interpolator = AccelerateInterpolator()
        animation = animationSet
    }
}