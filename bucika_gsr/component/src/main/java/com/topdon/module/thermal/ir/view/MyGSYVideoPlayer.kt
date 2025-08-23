package com.topdon.module.thermal.ir.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.topdon.module.thermal.ir.R

class MyGSYVideoPlayer : StandardGSYVideoPlayer {

    override fun getLayoutId(): Int = R.layout.view_my_gsy_video_player

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun updateStartImage() {
        if (mStartButton is ImageView) {
            val imageView = mStartButton as ImageView
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                imageView.setImageResource(R.drawable.svg_pause_icon)
            } else {
                imageView.setImageResource(R.drawable.svg_play_icon)
            }
        }
    }
}