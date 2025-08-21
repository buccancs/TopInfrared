package com.topdon.module.thermal.ir.video

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.elvishew.xlog.XLog
import org.bytedeco.javacv.FFmpegFrameRecorder
import java.lang.ref.WeakReference
import java.nio.ShortBuffer


/**
 * 音频采集并且与视频合并一起
 * @author: CaiSongL
 * @date: 2023/3/28
 */
class AudioRecordHelp private constructor() {
    private var audioRecord: AudioRecord? = null
    private var audioRecordRunnable: AudioRecordRunnable? = null
    private var audioThread: Thread? = null
    @Volatile
    private var recordingAudio = false
    private var startTime: Long = 0
    @Volatile
    var runAudioThread = true
    var audioData : ShortBuffer?=null
    var bufferReadResult: Int = 0
    val bufferSize: Int = AudioRecord.getMinBufferSize(
        VideoRecordFFmpeg.SAMPLE_AUDIO_RETE_INHZ,
        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
    )
    var type : Int = 0
    private var startRecordTime : Long = 0L

    object AudioUtilHolder {
        val INSTANCE = AudioRecordHelp()
    }

    @SuppressLint("MissingPermission")
    fun startRecording(recorder: FFmpegFrameRecorder,startRecordTime : Long) {
        this.startRecordTime = startRecordTime
        type = 1
        initRecorder(recorder)
        if (audioRecord == null){
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC, VideoRecordFFmpeg.SAMPLE_AUDIO_RETE_INHZ,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
            )
        }
        try {
            startTime = System.currentTimeMillis()
            audioThread!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun initRecorder(recorder: FFmpegFrameRecorder) {
        audioRecordRunnable = AudioRecordRunnable(recorder)
        audioThread = Thread(audioRecordRunnable)
//        audioThread?.priority = THREAD_PRIORITY_URGENT_AUDIO
        runAudioThread = true
    }

    internal inner class AudioRecordRunnable(recorder: FFmpegFrameRecorder) : Runnable {
        private val recorder : WeakReference<FFmpegFrameRecorder> = WeakReference(recorder)
        @SuppressLint("MissingPermission")
        override fun run() {
//            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            if (audioRecord == null){
                return
            }
            // Audio
            if (audioData == null){
                audioData = ShortBuffer.allocate(bufferSize)
            }
            audioRecord!!.startRecording()
            /**
             * 音频进行循环编码
             */
            try {
                while (runAudioThread) {
                    bufferReadResult = audioRecord!!.read(audioData!!.array(), 0, audioData!!.capacity())
                    if (recordingAudio) {
                        if (bufferReadResult > 0) {
                            audioData?.limit(bufferReadResult)
                            Log.w("音频采集",bufferReadResult.toString()+"//"+bufferReadResult)
                            recorder?.get()?.recordSamples(
                                VideoRecordFFmpeg.SAMPLE_AUDIO_RETE_INHZ,
                                VideoRecordFFmpeg.AUDIO_CHANNELS, audioData)
//                            Log.w("音频采集中2",""+recorder?.get()?.frameNumber)
                        }
                    }else{
                        for (i in 0 until bufferSize) {
                            audioData!!.put(i,0)
                        }
                        recorder?.get()?.recordSamples(
                            VideoRecordFFmpeg.SAMPLE_AUDIO_RETE_INHZ,
                            VideoRecordFFmpeg.AUDIO_CHANNELS, audioData)
                        Thread.sleep(1000L/VideoRecordFFmpeg.RATE)
                    }
                }
//                Log.w("停止采集",""+recorder?.get()?.frameNumber)
            }catch (e:Exception){
                XLog.e("采集容器异常")
            }
        }
    }

    public fun updateAudioRecordingState(boolean: Boolean){
        recordingAudio = boolean
    }

    fun stopAudioRecording() {
        type = 2
        if (!runAudioThread){
            return
        }
        runAudioThread = false
        try {
            audioThread?.interrupt()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        audioRecordRunnable = null
        audioThread = null
        recordingAudio = false
    }

    fun stopRecording(){
        if (!runAudioThread){
            return
        }
    }

    companion object {
        private val LOG_TAG = AudioRecordHelp::class.java.name
        fun getInstance(): AudioRecordHelp {
            return AudioUtilHolder.INSTANCE
        }
    }
}