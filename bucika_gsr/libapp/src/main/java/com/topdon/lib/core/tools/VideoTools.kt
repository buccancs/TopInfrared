package com.topdon.lib.core.tools

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.util.Log
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FrameGrabber


object VideoTools {

    //获取视频时长
    fun getLocalVideoDuration(videoPath: String): Long {
        return if (videoPath.uppercase().endsWith(".MP4")||videoPath.uppercase().endsWith(".AVI")) {
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(videoPath)
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
            } catch (e: Exception) {
                0
            }
        } else {
            0
        }
    }
}