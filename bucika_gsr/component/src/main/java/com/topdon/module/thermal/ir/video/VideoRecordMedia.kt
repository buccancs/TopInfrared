package com.topdon.module.thermal.ir.video

import android.graphics.Bitmap
import com.topdon.lib.core.utils.BitmapUtils
import com.infisense.usbir.view.CameraView
import com.infisense.usbir.view.TemperatureView
import com.topdon.lib.core.config.FileConfig
import com.topdon.module.thermal.ir.video.media.Encoder
import com.topdon.module.thermal.ir.video.media.MP4Encoder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 硬编码
 * bitmap -> mp4
 */
class VideoRecordMedia(
    private var cameraView: CameraView,
    private var temperatureView: TemperatureView
) : VideoRecord() {

    private lateinit var exportDisposable: Disposable
    private var encoder: Encoder = MP4Encoder()
    private var isRunning = false

    var width = 480
    var height = 640

    init {
        encoder.setFrameDelay(25)
        width = 480
        height = width * cameraView.height / cameraView.width
        //宽高不能出现奇数
        if (height % 2 == 1) {
            height -= 1
        }
    }


    override fun startRecord() {

        val downloadDir = FileConfig.lineGalleryDir
        val exportedFile = File(downloadDir, "${Date().time}.mp4")
        if (exportedFile.exists()) {
            exportedFile.delete()
        }
        encoder.setOutputFilePath(exportedFile.path)
//        if (bitmap == null) {
//            Log.w("123", "录制准备失败")
//            return
//        }
        encoder.setOutputSize(width, height)
        encoder.startEncode()
        isRunning = true
        //默认帧率20,间隔50ms一帧
        exportDisposable = Observable.interval(50, TimeUnit.MILLISECONDS)
            .map {
                createBitmapFromView()
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                encoder.addFrame(it)
            }
    }

    override fun startRecord(fileDir: String) {
    }

    override fun stopRecord() {
        if (isRunning) {
            encoder.stopEncode()
            exportDisposable.dispose()
        }
        isRunning = false
    }

    override fun updateAudioState(audioRecord: Boolean) {
        TODO("Not yet implemented")
    }

    private fun createBitmapFromView(): Bitmap {
        var cameraViewBitmap = cameraView.bitmap
        if (temperatureView.temperatureRegionMode != TemperatureView.REGION_MODE_CLEAN) {
            // 获取温度图层的数据，包括点线框，温度值等，重新合成bitmap
            cameraViewBitmap = BitmapUtils.mergeBitmap(
                cameraViewBitmap,
                temperatureView.regionAndValueBitmap,
                0,
                0
            )
        }
        val dstBitmap = if (cameraViewBitmap != null) {
            Bitmap.createScaledBitmap(cameraViewBitmap, width, height, true)
        } else {
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )
        }
        return dstBitmap
    }

}