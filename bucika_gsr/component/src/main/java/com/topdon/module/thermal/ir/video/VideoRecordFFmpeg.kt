package com.topdon.module.thermal.ir.video

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.RECORDSTATE_RECORDING
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SDCardUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.StringUtils.getString
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.elvishew.xlog.XLog
import com.infisense.usbdual.camera.DualViewWithExternalCameraCommonApi
import com.infisense.usbir.view.CameraView
import com.infisense.usbir.view.TemperatureView
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.FileConfig
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.tools.TimeTool
import com.topdon.lib.core.utils.BitmapUtils
import com.topdon.lib.ui.camera.CameraPreView
import com.topdon.lib.ui.widget.BitmapConstraintLayout
import com.topdon.lib.ui.widget.LiteSurfaceView
import com.topdon.libcom.view.TempLayout
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.view.HikSurfaceView
import com.topdon.module.thermal.ir.view.TemperatureHikView
import com.topdon.module.thermal.ir.view.compass.LinearCompassView
import io.reactivex.FlowableEmitter
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.FrameFilter
import org.bytedeco.opencv.opencv_core.IplImage
import java.io.File
import java.nio.ByteBuffer
import java.nio.ShortBuffer
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantReadWriteLock


/**
 * 软编吗
 * bitmap -> mp4
 *
 * avcodec.AV_CODEC_ID_MPEG4 //播放正常
 * avcodec.AV_CODEC_ID_H264 //不能拖拽进度条
 */
@SuppressLint("MissingPermission")
class VideoRecordFFmpeg(
    private val cameraView: View,
    private val cameraPreview: CameraPreView?,
    private val temperatureView: View?,
    private val isRecordTemp: Boolean,
    private val thermalPseudoBarView: BitmapConstraintLayout?,
    private val tempBg: TempLayout?,
    private val compassView: LinearCompassView? = null, //指南针
    private val dualView: DualViewWithExternalCameraCommonApi? = null,  // 双光
    private val isTC007 : Boolean = false,
    private val carView : View ?= null
) : VideoRecord() {

    companion object {
        const val TAG = "VideoRecordFFmpeg"
        const val FORMAT = "mp4"
        const val RATE = 25
        const val VIDEO_BITRATE = 1500000
        var VIDEO_CODEC = avcodec.AV_CODEC_ID_MPEG4
        const val SAMPLE_AUDIO_RETE_INHZ = 44100
        const val AUDIO_CHANNELS = 1



        /**
         * 内存检测
         */
        fun canStartVideoRecord(context: Context, videoFile: File? = null): Boolean {
            val canStart = (SDCardUtils.getExternalAvailableSize() - (videoFile?.length()
                ?: 0)) > (500L * 1000 * 1000)
            if (!canStart) {
                ThreadUtils.runOnUiThread {
                    TipDialog.Builder(context)
                        .setTitleMessage(getString(R.string.app_tip))
                        .setMessage(R.string.album_report_aleart)
                        .setPositiveListener(R.string.app_confirm) {

                        }
                        .setCanceled(true)
                        .create().show()
                }
            }
            return canStart
        }
    }

    private var alphaPaint: Paint? = null

    @Volatile
    private var isBitmapChangeTime: Long = 0L
    private var audioDisposable: Disposable? = null
    private var bitmapDisposable: Disposable? = null
    private var recorder: FFmpegFrameRecorder? = null
    private var exportDisposable: Disposable? = null

    @Volatile
    private var isRunning = false
    private var exportedFile: File? = null

    private var width = 640
    private var height = 480

    @Volatile
    private var openAudioRecord = true
    private var bufferSize = 0

    private var audioRecord: AudioRecord? = null
    private var audioData: ShortBuffer? = null
    private var tmpAudioData: ShortBuffer? = null
    private var bufferReadResult: Int = 0
    var stopVideoRecordListener: ((shoVideoTip: Boolean) -> Unit)? = null
    val bitmapExecutor = Executors.newScheduledThreadPool(1);
    val recordExecutor = Executors.newScheduledThreadPool(1)
    val audioExecutor = Executors.newScheduledThreadPool(1)
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var rectText = Rect() //得到text占用宽高， 单位：像素
    private val pix20 = SizeUtils.dp2px(20f)
    private val pix10 = SizeUtils.dp2px(10f)
    private val pix6 = SizeUtils.dp2px(6f)
    private val pixArray = ByteArray(width * height * 4)
    private val bufferRef: AtomicReference<ByteBuffer> =
        AtomicReference(ByteBuffer.allocate(pixArray.size))


    //    fun readByteBuffer(): ByteBuffer? {
//        synchronized(lock) {
//            return pixels?.duplicate() as ByteBuffer?
//        }
//    }
//
//    fun setBitmap(bitmap: Bitmap) {
//        synchronized(lock) {
//            if (pixels == null || pixels?.capacity() != bitmap.byteCount) {
//                pixels = ByteBuffer.allocate(bitmap.byteCount)
//            }
//            pixels?.position(0)
//            bitmap.copyPixelsToBuffer(pixels)
//            bitmap.recycle()
//        }
//    }
    private fun readByteBuffer(): ByteBuffer? {
        return bufferRef.get()?.duplicate()
    }

    private fun setBitmap(bitmap: Bitmap) {
        val byteCount = bitmap.byteCount
        val newPixels = ByteBuffer.allocate(byteCount)
        bitmap.copyPixelsToBuffer(newPixels)
        newPixels.flip()
        bitmap.recycle()
        bufferRef.set(newPixels)
    }

//    fun setBitmap(bitmap: Bitmap) {
//        lockWriteLock.writeLock().lock()
//        try {
//            if (pixels == null || pixels?.capacity() != bitmap.byteCount) {
//                pixels = ByteBuffer.allocate(bitmap.byteCount)
//            }
//            pixels?.position(0)
//            bitmap.copyPixelsToBuffer(pixels)
//            bitmap.recycle()
//        } finally {
//            lockWriteLock.writeLock().unlock()
//        }
//    }
//
//    fun readByteBuffer(): ByteBuffer? {
//        lockWriteLock.readLock().lock()
//        try {
//            return pixels?.duplicate()
//        } finally {
//            lockWriteLock.readLock().unlock()
//        }
//    }

    /**
     *
     * avcodec.AV_CODEC_ID_MPEG4 播放正常
     * avcodec.AV_CODEC_ID_H264 不能拖拽进度条
     *
     * 个别机型使用H264编码无法打开视频,优先使用AV_CODEC_ID_MPEG4
     */
    private fun getVideoCodec(): Int {
        return if (Build.BRAND == "motorola" && Build.MODEL == "XT2201-2") {
            XLog.i("使用视频编码AV_CODEC_ID_H264")
            avcodec.AV_CODEC_ID_H264
        } else {
            //默认类型
            XLog.i("使用视频编码AV_CODEC_ID_MPEG4")
            avcodec.AV_CODEC_ID_MPEG4
        }
    }

    init {
        if ((cameraView.parent as ViewGroup).height > (cameraView.parent as ViewGroup).width) {
            // 竖屏
            width = 480
            height =
                width * (cameraView.parent as ViewGroup).height / (cameraView.parent as ViewGroup).width
        } else {
            // 横屏
            width = 640
            height =
                width * (cameraView.parent as ViewGroup).height / (cameraView.parent as ViewGroup).width
        }
        //宽高不能出现奇数
        if (height % 2 == 1) {
            height -= 1
        }
        VIDEO_CODEC = getVideoCodec()
        bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_AUDIO_RETE_INHZ,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_AUDIO_RETE_INHZ,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
        )
        paint.color = Color.WHITE //白色半透明
        paint.textSize = SizeUtils.sp2px(6f).toFloat()
        paint.isDither = true
        paint.isFilterBitmap = true
        paint.getTextBounds("占位高度文本", 0, "占位高度文本".length, rectText)
    }


    var startTime: Long = 0L
    override fun startRecord() {
        startRecord(FileConfig.lineGalleryDir)
    }

    override fun startRecord(downloadDir: String) {
        try {
            exportedFile = File(downloadDir, "${Date().time}.mp4")
            if (exportedFile!!.exists()) {
                exportedFile!!.delete()
            }
            recorder = FFmpegFrameRecorder(
                exportedFile!!.absolutePath, width, height,
                AUDIO_CHANNELS
            )
            recorder!!.format = FORMAT
            recorder!!.frameRate = RATE.toDouble()
            recorder!!.videoBitrate = VIDEO_BITRATE
//            recorder!!.audioBitrate = VIDEO_BITRATE
            recorder!!.videoCodec = VIDEO_CODEC
//            recorder!!.setAudioOption("itsoffset",(1000L * 200L).toString())
            recorder!!.sampleRate = SAMPLE_AUDIO_RETE_INHZ
//            recorder!!.pixelFormat = avutil.AV_PIX_FMT_YUV420P
//            recorder!!.audioChannels = 1
//            recorder!!.setVideoOption("preset", "ultrafast")
            recorder!!.timestamp = 0L
            recorder!!.start()
            isRunning = true
            isBitmapChangeTime = System.currentTimeMillis()
            if (openAudioRecord &&
                ActivityCompat.checkSelfPermission(
                    cameraView.context,
                    Manifest.permission.RECORD_AUDIO
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                startAudioRecording()
            }

            if (audioData == null) {
                audioData = ShortBuffer.allocate(bufferSize / 2)
            }
            if (tmpAudioData == null) {
                tmpAudioData = ShortBuffer.allocate((bufferSize / 2))
            }
            val recordSchedulers = Schedulers.from(recordExecutor)
            val bitmapSchedulers = Schedulers.from(bitmapExecutor)
            setBitmap(createBitmapFromView())
            val fTime = 1000L / RATE
            bitmapDisposable = Observable.interval(fTime, TimeUnit.MILLISECONDS)
                .observeOn(bitmapSchedulers)
                .subscribe(
                    Consumer {
                        val tmp = createBitmapFromView()
                        tmp?.let {
                            setBitmap(it)
                        }
                    }, Consumer {
                        Log.e("图像对象录制异常", "${it.message}")
                    }
                )
            if (audioRecord == null) {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC, SAMPLE_AUDIO_RETE_INHZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
                )
            }
            startTime = System.currentTimeMillis()
            val i = 0;
            exportDisposable = Observable.interval(fTime, TimeUnit.MILLISECONDS)
                .observeOn(recordSchedulers)
                .subscribe(Consumer {
                    try {
                        val currentTimestamp = 1000L * (System.currentTimeMillis() - startTime)
                        val frame = Frame(width, height, Frame.DEPTH_BYTE, 4)
                        frame.image[0] = readByteBuffer()
                        val t = 1000L * (System.currentTimeMillis() - startTime)
                        if (t > (recorder?.timestamp ?: 0)) {
                            recorder!!.timestamp = t
                        }
                        recorder!!.record(frame)
                        frame.close()
                        if (System.currentTimeMillis() - queTime > 60 * 1000) {
                            //间隔1分钟，校验下剩余空间
                            if (!canStartVideoRecord(cameraView.context, exportedFile)) {
                                exportDisposable?.dispose()
                                stopVideoRecordListener?.invoke(false)
                                //录制的视频超出大小容量限制
                                return@Consumer
                            }
                            queTime = System.currentTimeMillis()
                        }
                        recorder?.timestamp?.let {
                            if (it / 1000 > 60 * 60 * 1000) {
                                //热成像录像限制60分钟
                                exportDisposable?.dispose()
                                stopVideoRecordListener?.invoke(true)
                                return@Consumer
                            }
                        }
                        if (audioRecord == null) {
                            return@Consumer
                        }
                        val audioTime = System.currentTimeMillis()
                        if (openAudioRecord) {
                            bufferReadResult =
                                audioRecord?.read(audioData!!.array(), 0, audioData!!.capacity())
                                    ?: 0
                            if (bufferReadResult > 0) {
                                audioData?.limit(bufferReadResult)
                                if (currentTimestamp > (recorder?.timestamp ?: 0)) {
                                    recorder!!.timestamp = currentTimestamp
                                }
                                recorder?.recordSamples(
                                    SAMPLE_AUDIO_RETE_INHZ,
                                    AUDIO_CHANNELS, audioData
                                )
                            }
                        } else {
                            for (i in 0 until tmpAudioData!!.capacity()) {
                                tmpAudioData!!.put(i, 1.toShort())
                            }
                            // 使用当前时间戳
                            if (currentTimestamp > (recorder?.timestamp ?: 0)) {
                                recorder!!.timestamp = currentTimestamp
                            }
                            recorder?.recordSamples(
                                SAMPLE_AUDIO_RETE_INHZ,
                                AUDIO_CHANNELS, tmpAudioData
                            )
                        }
//                        Log.w(
//                            "图像大小",
//                            "${System.currentTimeMillis() - time}======${frame.image.size}//${bufferSize}//${(recorder?.timestamp!! / 1000000L)}"
//                        )

                    } catch (e: Exception) {
                        Log.e("图像录制", "Caught an exception: " + e.message);
                    }
                }, Consumer {
                    Log.e("图像对象录制异常", "${it.message}")
                })

        } catch (e: Exception) {
//            stopRecord()
            exportDisposable?.dispose()
            stopVideoRecordListener?.invoke(false)
            XLog.e("录制异常")
            e.printStackTrace()
        }
    }

    private class FrameInterpolationFilter(private val interpolationFactor: Int) :
        FrameFilter() {
        private var previousFrame: Frame? = null
        override fun start() {
            previousFrame = null
        }

        override fun stop() {
            previousFrame = null
        }

        override fun push(frame: Frame) {
            previousFrame = frame.clone()
        }

        override fun pull(): Frame? {
            if (previousFrame == null) {
                return null
            }
            val interpolatedFrame = previousFrame!!.clone()
            interpolatedFrame.timestamp += (1.0 / interpolationFactor).toLong()
            return interpolatedFrame
        }

        override fun release() {

        }

        fun filter(image: IplImage?, image2: IplImage?): IplImage? {
            // 未使用
            return null
        }
    }

    fun startAudioRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_AUDIO_RETE_INHZ,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
        )
        audioRecord!!.startRecording()
    }

    fun stopAudioRecording() {
        try {
            if (RECORDSTATE_RECORDING == audioRecord?.recordingState) {
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC, SAMPLE_AUDIO_RETE_INHZ,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
                )
            }
        } catch (e: Exception) {
            Log.e("图像对象处理异常", "${e.message}")
        }
    }

    /**
     * 内存检测
     */
    fun canStartVideoRecord(videoFile: File?): Boolean {
        val canStart = (SDCardUtils.getExternalAvailableSize() - (videoFile?.length()
            ?: 0)) > (500L * 1000 * 1000)
//        Log.w("本地可用空间","" + SDCardUtils.getExternalAvailableSize() / 1000 / 1000)
        if (!canStart) {
            ThreadUtils.runOnUiThread {
                TipDialog.Builder(cameraView.context)
                    .setTitleMessage(getString(R.string.app_tip))
                    .setMessage(R.string.album_report_aleart)
                    .setPositiveListener(R.string.app_confirm) {

                    }
                    .setCanceled(true)
                    .create().show()
            }
        }
        return canStart
    }


    var queTime = 0L

    override fun stopRecord() {
        CoroutineScope(Dispatchers.IO).launch {
            if (isRunning) {
                try {
                    launch(Dispatchers.Main) {
                        exportDisposable?.let {
                            if (!it.isDisposed) {
                                it.dispose()
                            }
                        }
                        bitmapDisposable?.let {
                            if (!it.isDisposed) {
                                it.dispose()
                            }
                        }
                        if (RECORDSTATE_RECORDING == audioRecord?.recordingState) {
                            audioRecord?.stop()
                            audioRecord?.release()
                            audioRecord = null
                        }
                        bitmapRecycle()
                        audioDisposable?.let {
                            if (!it.isDisposed) {
                                it.dispose()
                            }
                        }
//                        AudioRecordHelp.getInstance().stopAudioRecording()
                    }
                    bitmapExecutor.shutdown()
                    recordExecutor.shutdown()
                    audioExecutor.shutdown()
                    delay(500)
                    recorder?.stop()
                    delay(300)
                    refreshAlbum()
                } catch (e: Exception) {
                    XLog.e("捕获停止录制视频" + e.message)
                }
            }
            isRunning = false
        }
    }

    private fun bitmapRecycle() {

        tempBitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
            tempBitmap = null
        }
        cameraBitmap?.let {
            if (!it.isRecycled) {
                it.recycle()
            }
            cameraBitmap = null
        }
    }

    override fun updateAudioState(openAudioRecord: Boolean) {
        if (this@VideoRecordFFmpeg.openAudioRecord == openAudioRecord) {
            return
        }
        try {
            if (openAudioRecord && isRunning) {
                startAudioRecording()
            } else {
                stopAudioRecording()
            }
            this@VideoRecordFFmpeg.openAudioRecord = openAudioRecord
        } catch (_: Exception) {

        }
    }


    /**
     * cameraViewBitmap是屏幕控件的实际宽高
     * dstBitmap转成视频输出的
     */
    private fun createBitmapFromView(): Bitmap {
        var cameraViewBitmap: Bitmap

        when (cameraView) {
            is CameraView -> cameraViewBitmap = if (dualView == null) cameraView.scaledBitmap else dualView.scaledBitmap
            is TextureView -> {
                cameraViewBitmap = Bitmap.createBitmap(cameraView.width, cameraView.height, Bitmap.Config.ARGB_8888)
                cameraView.getBitmap(cameraViewBitmap)
            }
            is LiteSurfaceView -> cameraViewBitmap = cameraView.scaleBitmap()
            is HikSurfaceView -> cameraViewBitmap = cameraView.getScaleBitmap()
            else -> cameraViewBitmap = Bitmap.createBitmap(cameraView.width, cameraView.height, Bitmap.Config.ARGB_8888)
        }

        when (temperatureView) {
            is TemperatureView -> {
                if (isRecordTemp) {
                    if (temperatureView.temperatureRegionMode != TemperatureView.REGION_MODE_CLEAN) {
                        cameraViewBitmap = BitmapUtils.mergeBitmap(cameraViewBitmap, temperatureView.regionBitmap, 0, 0)
                    }
                } else {
                    if (temperatureView.temperatureRegionMode == TemperatureView.REGION_MODE_RESET) {
                        cameraViewBitmap = BitmapUtils.mergeBitmap(cameraViewBitmap, temperatureView.regionBitmap, 0, 0)
                    }
                }
            }
            is TemperatureHikView -> {
                temperatureView.draw(Canvas(cameraViewBitmap))
            }
        }

        //伪彩条
        if (thermalPseudoBarView?.visibility == VISIBLE) {
            try {
                thermalPseudoBarView?.viewBitmap?.let {
//                    Log.w("图像对象处理耗时-彩条大小",it.byteCount.toString())
                    cameraViewBitmap = BitmapUtils.mergeBitmap(
                        cameraViewBitmap,
                        it,
                        cameraViewBitmap!!.width - it.width,
                        (cameraViewBitmap!!.height - it.height) / 2
                    )
                }
//                Log.w("图像对象处理耗时-彩条",""+(System.currentTimeMillis() - startTime))
            } catch (e: Exception) {
//                Log.e("图像对象处理耗时-彩条",""+(System.currentTimeMillis() - startTime))
            }
        }
        if (true == tempBg?.isVisible) {
            if (alphaPaint == null) {
                alphaPaint = Paint()
            }
            alphaPaint?.alpha = (tempBg!!.animatorAlpha * 255).toInt()
            cameraViewBitmap = BitmapUtils.mergeBitmapAlpha(
                cameraViewBitmap,
                tempBg!!.drawToBitmap(), alphaPaint,
                0,
                0,
            )
        }
        if (carView?.isVisible == true){
            cameraViewBitmap = BitmapUtils.mergeBitmap(
                cameraViewBitmap,
                carView?.drawToBitmap(), 0, 0)
        }
        //指南针
        compassView?.let {
            if (it.isVisible) {
                try {
                    val bitmap = it.curBitmap
                    cameraViewBitmap = BitmapUtils.mergeBitmap(
                        cameraViewBitmap,
                        bitmap,
                        ((cameraView.parent as ViewGroup).width - it.width) / 2,
                        SizeUtils.dp2px(20f)
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "图像对象处理异常 exception:${e.message}")
                }
//                Log.w("图像对象处理耗时-指南针", "${System.currentTimeMillis() - startTime}")
            }
        }

        //画中画
        cameraPreview?.let {
            if (it.isVisible) {
                val newBitmap: Bitmap? = BitmapUtils.mergeBitmapByView(
                    cameraViewBitmap,
                    it.getBitmap(),
                    it
                )
                if (newBitmap != null) {
                    cameraViewBitmap = newBitmap
                }
            }
        }

        var dstBitmap = if (cameraViewBitmap != null) {
            Bitmap.createScaledBitmap(cameraViewBitmap!!, width, height, true)
        } else {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }

        //添加水印
        val watermarkBean = if (isTC007){SharedManager.wifiWatermarkBean} else {SharedManager.watermarkBean}
        if (watermarkBean.isOpen) {
            dstBitmap = drawCenterLable(
                dstBitmap!!,
                watermarkBean.title,
                watermarkBean.address,
                if (watermarkBean.isAddTime) TimeTool.getNowTime() else ""
            )!!
        }
        return dstBitmap
    }

    private var cameraBitmap: Bitmap? = null
    private var tempBitmap: Bitmap? = null


    fun drawCenterLable(bmp: Bitmap, title: String, address: String, time: String?): Bitmap {
        //创建一样大小的图片
        val newBmp = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
        //创建画布
        val canvas = Canvas(newBmp)
        canvas.drawBitmap(bmp, 0f, 0f, null) //绘制原始图片
        canvas.save()
        val beginX = pix10.toDouble() //45度角度值是1.414
        var beginY = (bmp.height - pix10).toDouble()
        paint.getTextBounds("占位高度文本", 0, "占位高度文本".length, rectText)
        if (!TextUtils.isEmpty(time)) {
            beginY = beginY - (rectText.bottom - rectText.top)
            canvas.drawText(time!!, beginX.toInt().toFloat(), beginY.toInt().toFloat(), paint)
            beginY -= pix6.toDouble()
        }
        if (!TextUtils.isEmpty(address)) {
            val textHeight = (rectText.bottom - rectText.top)
            paint.getTextBounds(address, 0, address.length, rectText)
            if (rectText.width() > bmp.width - pix20) {
                //字符太长，进行换行处理
                val staticLayout = StaticLayout(
                    address,
                    paint, bmp.width - pix20,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false
                )
                beginY = beginY - (textHeight + SizeUtils.dp2px(1f)) * staticLayout.lineCount
                canvas.save()
                canvas.translate(beginX.toInt().toFloat(), (beginY.toInt() - textHeight).toFloat())
                staticLayout.draw(canvas)
                canvas.restore()
            } else {
                beginY = beginY - textHeight
                canvas.drawText(address, beginX.toInt().toFloat(), beginY.toInt().toFloat(), paint)
            }
            beginY -= pix6.toDouble()
        }
        if (!TextUtils.isEmpty(title)) {
            val textHeight = rectText.bottom - rectText.top
            paint.getTextBounds(title, 0, title.length, rectText)
            if (rectText.width() > bmp.width - pix20) {
                //字符太长，进行换行处理
                val staticLayout = StaticLayout(
                    title,
                    paint, bmp.width - pix20,
                    Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false
                )
                beginY = beginY - textHeight * staticLayout.lineCount
                canvas.save()
                canvas.translate(beginX.toInt().toFloat(), (beginY.toInt() - textHeight).toFloat())
                staticLayout.draw(canvas)
                canvas.restore()
            } else {
                beginY = beginY - textHeight
                canvas.drawText(title, beginX.toInt().toFloat(), beginY.toInt().toFloat(), paint)
            }
            beginY -= pix6.toDouble()
        }
        canvas.restore()
        if (!bmp.isRecycled) {
            bmp.recycle()
        }
        return newBmp
    }


    private fun refreshAlbum() {
        exportedFile?.let {
            MediaScannerConnection.scanFile(Utils.getApp(), arrayOf(it.toString()), null, null)
        }
    }

}