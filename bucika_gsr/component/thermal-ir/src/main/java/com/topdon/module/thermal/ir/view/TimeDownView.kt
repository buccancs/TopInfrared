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

/**
 * @author: CaiSongL
 * @date: 2023/4/7 23:43
 */
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

    /**
     * 开始计时
     *
     * @param seconds
     */
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

    /**
     * 倒计时开启方法
     *
     * @param downCount     倒计时总数
     * @param lastDown      显示的倒计时的最后一个数
     * @param delayMills    延迟启动倒计时（毫秒数）
     * @param intervalMills 倒计时间隔时间（毫秒数）
     */
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

    /**
     * 取消
     */
    fun cancel() {
        animationSet?.cancel()
        downTimerTask?.cancel()
        timer?.cancel()
        drawTextFlag = DRAW_TEXT_NO
        invalidate() //刷新一下
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

    /**
     * 每个倒计时事件监听.
     */
    var onTimeListener: ((time: Int) -> Unit)? = null
    /**
     * 倒计时结束事件监听.
     */
    var onFinishListener: (() -> Unit)? = null



    var downTimeWatcher: DownTimeWatcher? = null

    /**
     * 监听倒计时的变化
     * @param downTimeWatcher
     */
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
//                Log.e("测试","//handleMessage"+downCount+"//"+lastDown);
                if (downCount >= lastDown - 1) {
                    drawTextFlag = DRAW_TEXT_YES //默认绘制
                    //未到结束时
                    if (downCount >= lastDown) {
                        text = downCount.toString() + ""
                        startDefaultAnimate()
                        if (downCount == lastDown && downTimeWatcher != null) {
                            downTimeWatcher!!.onLastTime(downCount)
                        }
                    } else if (downCount == lastDown - 1) { // 若lastDown为0，downCount == -1时是倒计时真正结束之时。
                        //倒计时结束，虽然setText()方法触发onDraw，但重写使之不进行绘制
                        //设置不绘制标记
                        if (afterDownDimissFlag == AFTER_LAST_TIME_DIMISS) {
                            drawTextFlag = DRAW_TEXT_NO
                        }
                        invalidate() //刷新一下
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
                //
            }
        }
    }

    private val DRAW_TEXT_YES = 1
    private val DRAW_TEXT_NO = 0

    /**
     * 是否执行onDraw的标识，默认绘制
     */
    private var drawTextFlag = DRAW_TEXT_YES
    private val AFTER_LAST_TIME_DIMISS = 1
    private val AFTER_LAST_TIME_NODIMISS = 0

    /**
     * 在倒计时结束之后文字是否消失的标志，默认消失
     */
    private var afterDownDimissFlag = AFTER_LAST_TIME_DIMISS

    /**
     * 设置倒计时结束后文字不消失
     */
    fun setAfterDownNoDimiss() {
        afterDownDimissFlag = AFTER_LAST_TIME_NODIMISS
    }

    /**
     * 设置倒计时结束后文字消失
     */
    fun setAferDownDimiss() {
        afterDownDimissFlag = AFTER_LAST_TIME_DIMISS
    }

    var startDefaultAnimFlag = true

    //关闭默认动画
    fun closeDefaultAnimate() {
        animationSet?.reset()
        startDefaultAnimFlag = false
    }

    //开启默认动画
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
        //将AlphaAnimation这个已经设置好的动画添加到 AnimationSet中
        animationSet!!.addAnimation(scaleAnimation)
        animationSet!!.addAnimation(alphaAnimation)
        animationSet!!.interpolator = AccelerateInterpolator()
        animation = animationSet
    }
}