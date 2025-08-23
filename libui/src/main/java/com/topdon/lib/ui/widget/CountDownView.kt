package com.topdon.lib.ui.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.ui.R

class CountDownView : View {

    private var mRingColor = 0

    private var mRingWidth = 0

    private var mRingProgressTextSize = 0

    private var mWidth = 0

    private var mHeight = 0

    private var mRingText: String? = null
    private lateinit var mPaint: Paint
    private lateinit var mTextPaint: Paint

    private var mRectF: RectF? = null

    private var mProgressTextColor = 0
    private var mCountdownTime = 0
    private var mCurrentProgress = 0f

    private var valueAnimator: ValueAnimator? = null

    private var mListener: OnCountDownListener? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CountDownView)
        for (i in 0 until ta.indexCount) {
            when (ta.getIndex(i)) {
                R.styleable.CountDownView_ringColor -> mRingColor =
                    ta.getColor(
                        R.styleable.CountDownView_ringColor,
                        ContextCompat.getColor(context, R.color.colorAccent)
                    )
                R.styleable.CountDownView_ringWidth -> mRingWidth =
                    ta.getDimensionPixelSize(
                        R.styleable.CountDownView_ringWidth,
                        40
                    )
                R.styleable.CountDownView_progressTextSize -> mRingProgressTextSize =
                    ta.getDimensionPixelSize(
                        R.styleable.CountDownView_progressTextSize,
                        20
                    )
                R.styleable.CountDownView_progressTextColor -> mProgressTextColor =
                    ta.getColor(
                        R.styleable.CountDownView_progressTextColor,
                        context.resources.getColor(R.color.colorAccent)
                    )
                R.styleable.CountDownView_countdownTime -> mCountdownTime =
                    ta.getInteger(
                        R.styleable.CountDownView_countdownTime,
                        60
                    )
                R.styleable.CountDownView_progressText -> mRingText =
                    ta.getString(R.styleable.CountDownView_progressText)
            }
        }
        ta.recycle()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.isAntiAlias = true
        mTextPaint = Paint()
        this.setWillNotDraw(false)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mWidth = measuredWidth
        mHeight = measuredHeight
        mRectF = RectF(
            0 + mRingWidth / 2f,
            0 + mRingWidth / 2f,
            mWidth - mRingWidth / 2f,
            mHeight - mRingWidth / 2f
        )
    }

    fun setCountdownTime(mCountdownTime: Int) {
        this.mCountdownTime = mCountdownTime
        mRingText = mCountdownTime.toString()
        invalidate()
    }

    private fun getValueAnimator(countdownTime: Long): ValueAnimator? {
        val valueAnimator = ValueAnimator.ofFloat(0f, 100f)
        valueAnimator.duration = countdownTime
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.repeatCount = 0
        return valueAnimator
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint.color = mRingColor
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = mRingWidth.toFloat()
        canvas.drawArc(mRectF!!, -90f, mCurrentProgress - 360, false, mPaint)
        val font = ResourcesCompat.getFont(context, R.font.pingfang_bold)
        mTextPaint.isAntiAlias = true
        mTextPaint.textAlign = Paint.Align.CENTER
        mTextPaint.typeface = font

        mTextPaint.textSize = mRingProgressTextSize.toFloat()
        mTextPaint.color = mProgressTextColor

        val fontMetrics = mTextPaint.fontMetricsInt
        val baseline =
            ((mRectF!!.bottom + mRectF!!.top - fontMetrics.bottom - fontMetrics.top) / 2).toInt()
        canvas.drawText(mRingText!!, mRectF!!.centerX(), baseline.toFloat(), mTextPaint)
    }

    fun startCountDown() {
        valueAnimator = getValueAnimator((mCountdownTime * 1000).toLong())
        valueAnimator!!.addUpdateListener { animation ->
            val i = animation.animatedValue.toString().toFloat()
            mCurrentProgress = (360 * (i / 100f))
            invalidate()
        }
        valueAnimator!!.start()
        valueAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (mListener != null) {
                    mListener!!.countDownFinished()
                }
            }
        })
    }

    fun stopCountDown() {
        if (valueAnimator!!.isRunning) {
            valueAnimator!!.cancel()
        }
    }

    fun setOnCountDownListener(mListener: OnCountDownListener) {
        this.mListener = mListener
    }

    interface OnCountDownListener {
        fun countDownFinished()
    }
}