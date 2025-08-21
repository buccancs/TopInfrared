package com.topdon.lib.ui.motion

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout

/**
 * 闪烁效果
 */
class RepeatMotionLayout : MotionLayout, MotionLayout.TransitionListener {

    private var motionStartId = 0
    private var motionEndId = 0

    @Volatile
    private var isAdd = false


    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * 开始闪烁
     */
    fun startTransition() {
//        Log.w("123", "开始闪烁")
        if (!isAdd) {
            addTransitionListener(this)
            isAdd = true
        }
        transitionToEnd()
    }

    /**
     * 恢复状态
     */
    fun cancelTransition() {
        removeTransitionListener(this)
        isAdd = false
        transitionToStart()
    }

    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
        motionStartId = startId
        motionEndId = endId
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout?,
        startId: Int,
        endId: Int,
        progress: Float
    ) {

    }

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
//        Log.w("123", "onTransitionCompleted currentId:$currentId")
        if (currentId == motionEndId) {
            transitionToStart()
        } else {
            transitionToEnd()
        }
    }

    override fun onTransitionTrigger(
        motionLayout: MotionLayout?,
        triggerId: Int,
        positive: Boolean,
        progress: Float
    ) {

    }


}