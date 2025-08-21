package com.topdon.libcom

import android.content.Context
import android.media.MediaPlayer
import com.topdon.lib.core.bean.AlarmBean
import com.topdon.libcom.util.SingletonHolder
import com.topdon.libcom.view.TempLayout

/**
 * 预警逻辑统一处理
 * @author: CaiSongL
 * @date: 2023/5/5 15:13
 */
class AlarmHelp private constructor(val context: Context) {
    companion object : SingletonHolder<AlarmHelp, Context>(::AlarmHelp)

    private var mediaPlayer: MediaPlayer? = null
    private var ringtoneResPosition = -1
    private var isOpenLowTemp = false
    private var isOpenHighTemp = false
    private var isTempAlarmRingtoneOpen = false
    private var maxTemp: Float = 0f
    private var minTemp: Float = 0f
    private var isPause = false
    private var alarmBean: AlarmBean? = null



    fun updateData(alarmBean : AlarmBean) {
        this.alarmBean = alarmBean
        isTempAlarmRingtoneOpen = alarmBean?.isRingtoneOpen ?: false
        isOpenLowTemp = alarmBean?.isLowOpen ?: false
        isOpenHighTemp = alarmBean?.isHighOpen ?: false
        ringtoneResPosition = alarmBean?.ringtoneType ?: -1
        maxTemp = alarmBean?.highTemp ?: Float.MAX_VALUE
        minTemp = alarmBean?.lowTemp ?: Float.MIN_VALUE
        if (isTempAlarmRingtoneOpen) {
            when (ringtoneResPosition) {
                0 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone1)
                1 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone2)
                2 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone3)
                3 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone4)
                4 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone5)
            }
            mediaPlayer?.isLooping = true
        } else {
            mediaPlayer = null
        }
    }

    fun updateData(low: Float?, high: Float?, ringtone: Int?) {
        if (low == null) {
            isOpenLowTemp = false
        } else {
            isOpenLowTemp = true
            minTemp = low
        }
        if (high == null) {
            isOpenHighTemp = false
        } else {
            isOpenHighTemp = true
            maxTemp = high
        }
        if (ringtone == null) {
            isTempAlarmRingtoneOpen = false
            ringtoneResPosition = -1
            try {
                stopPlayer()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {
            }
        } else {
            isTempAlarmRingtoneOpen = true
            try {
                stopPlayer()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (_: Exception) {
            }
            when (ringtone) {
                0 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone1)
                1 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone2)
                2 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone3)
                3 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone4)
                4 -> mediaPlayer = MediaPlayer.create(context, R.raw.ringtone5)
            }
            mediaPlayer?.isLooping = true
            ringtoneResPosition = ringtone
        }
    }


    /**
     *
     */
    fun alarmData(realMax: Float, realMin: Float, tempLayout: TempLayout?) {
        if (isOpenHighTemp && isOpenLowTemp) {
            //高低温预警
            if (realMax > maxTemp && realMin < minTemp) {
                tempLayout?.startAnimation(TempLayout.TYPE_A)
                startMediaPlayer()
            } else if (realMax > maxTemp) {
                tempLayout?.startAnimation(TempLayout.TYPE_HOT)
                startMediaPlayer()
            } else if (realMin < minTemp) {
                tempLayout?.startAnimation(TempLayout.TYPE_LT)
                startMediaPlayer()
            } else {
                tempLayout?.stopAnimation()
                stopPlayer()
            }
        } else if (isOpenHighTemp) {
            //高温预警
            if (realMax > maxTemp) {
                tempLayout?.startAnimation(TempLayout.TYPE_HOT)
                startMediaPlayer()
            } else {
                tempLayout?.stopAnimation()
                stopPlayer()
            }
        } else if (isOpenLowTemp) {
            //低温预警
            if (realMin < minTemp) {
                tempLayout?.startAnimation(TempLayout.TYPE_LT)
                startMediaPlayer()
            } else {
                tempLayout?.stopAnimation()
                stopPlayer()
            }
        } else {
            tempLayout?.stopAnimation()
            stopPlayer()
        }
    }

    private fun stopPlayer() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    private fun startMediaPlayer() {
        if (mediaPlayer?.isPlaying != true && !isPause) {
            mediaPlayer?.seekTo(0)
            mediaPlayer?.start()
        }
    }

    fun onDestroy(isSaveSetting : Boolean) {
        if (!isSaveSetting) {
            isTempAlarmRingtoneOpen = false
            isOpenHighTemp = false
            isOpenLowTemp = false
        }
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }


    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPause = true
            }
        }
    }

    fun onResume() {
        isPause = false
    }


}