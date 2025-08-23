package com.guide.zm04c.matrix

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import com.guide.zm04c.matrix.Logger.d
import com.guide.zm04c.matrix.utils.FileUtils.Companion.saveFile
import com.guide.zm04c.matrix.utils.HexDump
import com.topdon.lib.core.BaseApplication
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import kotlin.experimental.and

class GuideInterface {

    private val TAG = "guidecore"
    private val IR_WIDTH = 256
    private val IR_HEIGHT = 192
    private val HEAD_SIZE = 64
    private val IR_SIZE = IR_WIDTH * IR_HEIGHT
    private val YUV_SIZE = IR_SIZE * 2
    private val PARAM_SIZE = 512
    private val TEMP_MATRIX_SIZE = IR_SIZE * 4
    private val FRAME_SIZE = HEAD_SIZE + YUV_SIZE + PARAM_SIZE + TEMP_MATRIX_SIZE
    private val MAX_BULK_TRANSFER_SIZE = 16384
    private var mGuideUsbManager: GuideUsbManager? = null
    private var mUsbBuffer: UsbBuffer? = null
    private var mNativeGuideCore: NativeGuideCore? = null
    private val mUsbReadbuffer = ByteArray(MAX_BULK_TRANSFER_SIZE)
    private val mFrame = ByteArray(FRAME_SIZE)

    private val mYuv = ByteArray(YUV_SIZE)

    private val mParam = ByteArray(PARAM_SIZE)

    private val mTempMatrixByte = ByteArray(TEMP_MATRIX_SIZE)
    private val mTempMatrixFloat = FloatArray(IR_SIZE)
    private var mIrDataCallback: IrDataCallback? = null
    private var mUsbBufferWriteThread: Thread? = null
    private var mUsbBufferReadThread: Thread? = null

    @Volatile
    private var mWriteThreadFlag = false

    @Volatile
    private var mReadThreadFlag = false
    private val mLock = Any()

    interface IrDataCallback {
        fun processIrData(yuv: ByteArray, temp: FloatArray)
    }

    private fun startUsbBufferWriteThread() {
        mWriteThreadFlag = true
        mUsbBufferWriteThread = Thread {
            d(TAG, "write thread start")
            while (mWriteThreadFlag) {
                val length: Int = mGuideUsbManager!!.read(mUsbReadbuffer)
                if (length > 0) {
                    mUsbBuffer!!.write(mUsbReadbuffer, 0, length)
                } else {
                    try {
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
            d(TAG, "write thread exit")
        }
        mUsbBufferWriteThread!!.start()
    }

    var startTime = 0L

    private fun startUsbBufferReadThread() {
        mReadThreadFlag = true
        mUsbBufferReadThread = Thread {
            d(TAG, "read thread start")
            while (mReadThreadFlag) {
                val ret = mUsbBuffer!!.readFrame(mFrame)
                if (ret) {
                    System.arraycopy(mFrame, HEAD_SIZE, mYuv, 0, mYuv.size)
                    synchronized(mLock) {
                        System.arraycopy(
                            mFrame,
                            HEAD_SIZE + YUV_SIZE,
                            mParam,
                            0,
                            mParam.size
                        )
                        System.arraycopy(
                            mFrame,
                            HEAD_SIZE + YUV_SIZE + PARAM_SIZE,
                            mTempMatrixByte,
                            0,
                            mTempMatrixByte.size
                        )
                    }
                    mNativeGuideCore!!.toFloatTempMatrix(mTempMatrixFloat, mTempMatrixByte)
                    if (mIrDataCallback != null) {
                        mIrDataCallback!!.processIrData(mYuv, mTempMatrixFloat)
                    }
                } else {
                }
            }
            d(TAG, "read thread exit")
        }
        mUsbBufferReadThread!!.start()
    }

    private fun stopUsbBufferWriteThread() {
        if (mUsbBufferWriteThread != null) {
            mWriteThreadFlag = false
            try {
                mUsbBufferWriteThread!!.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            mUsbBufferWriteThread = null
        }
    }

    private fun stopUsbBufferReadThread() {
        if (mUsbBufferReadThread != null) {
            mReadThreadFlag = false
            try {
                mUsbBufferReadThread!!.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            mUsbBufferReadThread = null
        }
    }

    private fun getParam(offset: Int, len: Int, index: Int): Byte {
        val param = ByteArray(len)
        synchronized(mLock) { System.arraycopy(mParam, offset, param, 0, len) }
        return param[index]
    }

    private fun getParam(offset: Int, len: Int): ByteArray {
        val param = ByteArray(len)
        synchronized(mLock) { System.arraycopy(mParam, offset, param, 0, len) }
        return param
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun init(context: Context?, irDataCallback: IrDataCallback?): Int {
        mNativeGuideCore = NativeGuideCore()
        mGuideUsbManager = GuideUsbManager(context, mNativeGuideCore)
        mIrDataCallback = irDataCallback
        val ret: Int = mGuideUsbManager!!.connectUsbDevice()
        if (ret != 5) {
            return ret
        }
        d(TAG, "connectUsbDevice ret = $ret")
        mUsbBuffer = UsbBuffer(FRAME_SIZE, HEAD_SIZE, 8)
        mUsbBuffer!!.setFrameMark(0xBB66)
        startUsbBufferReadThread()
        startUsbBufferWriteThread()
        return ret
    }

    fun exit() {
        stopUsbBufferWriteThread()
        stopUsbBufferReadThread()
        if (mGuideUsbManager != null) {
            mGuideUsbManager!!.disconnectUsbDevice()
            mGuideUsbManager = null
        }
        if (mNativeGuideCore != null) {
            mNativeGuideCore = null
        }
    }

    fun shutter() {
        if (mGuideUsbManager == null) {
            return
        }
        mGuideUsbManager!!.shutter()
    }

    fun nuc() {
        if (mGuideUsbManager == null) {
            return
        }
        mGuideUsbManager!!.nuc()
    }

    fun changePalette(i: Int) {
        Log.d(TAG, "changePalette() called with: i = [$i]")
        if (mGuideUsbManager == null) {
            return
        }
        if (i < 0 || i > 9) {
            return
        }
        mGuideUsbManager!!.changePalette(i)
    }

    fun setDistance(distance: Float) {
        if (mGuideUsbManager == null) {
            return
        }
        mGuideUsbManager!!.setDistance(distance)
    }

    fun getDistance(): Float {
        if (mNativeGuideCore == null) {
            return (-1).toFloat()
        }
        val PARAM_INDEX_DISTANCE = 163
        return getParam(PARAM_INDEX_DISTANCE * 2, 1, 0) * 1.0f / 10
    }

    fun setBright(bright: Int) {
        if (mGuideUsbManager == null) {
            return
        }
        if (bright < 0 || bright > 100) {
            return
        }
        mGuideUsbManager!!.setBright(bright)
    }

    fun getBright(): Int {
        if (mNativeGuideCore == null) {
            return -1
        }
        val PARAM_INDEX_BRIGHT = 164
        return getParam(PARAM_INDEX_BRIGHT * 2, 1, 0).toInt()
    }

    fun setContrast(contrast: Int) {
        if (mGuideUsbManager == null) {
            return
        }
        if (contrast < 0 || contrast > 100) {
            return
        }
        mGuideUsbManager!!.setContrast(contrast)
    }

    fun getContrast(): Int {
        if (mNativeGuideCore == null) {
            return -1
        }
        val PARAM_INDEX_CONTRAST = 164
        return getParam(PARAM_INDEX_CONTRAST * 2, 2, 1).toInt()
    }

    fun yuv2Bitmap(bitmap: Bitmap?, yuv: ByteArray?) {
        if (mNativeGuideCore == null) {
            return
        }
        mNativeGuideCore!!.yuv2Bitmap(bitmap!!, yuv!!)

    }

    fun saveTempMatrix(path: String?) {
        synchronized(mLock) {
            saveFile(
                mTempMatrixByte,
                path!!, false
            )
        }
    }

    fun setRange(range: Int) {
        if (mGuideUsbManager == null) {
            return
        }
        mGuideUsbManager!!.setRange(range)
    }

    fun setEmiss(emiss: Int) {
        if (mGuideUsbManager == null) {
            return
        }
        if (emiss < 1 || emiss > 99) {
            return
        }
        mGuideUsbManager!!.setEmiss(emiss)
    }

    fun getEmiss(): Int {
        if (mNativeGuideCore == null) {
            return -1
        }
        val PARAM_INDEX_EMISS = 162
        return getParam(PARAM_INDEX_EMISS * 2, 1, 0).toInt()
    }

    fun getFirmwareVersion(): String? {
        val PARAM_INDEX_ASIC_MAIN_VERSION = 32
        val DOT = "."
        val bytes = getParam(PARAM_INDEX_ASIC_MAIN_VERSION * 2, 6)
        val mainVersion =
            (bytes[1] and 0xFF.toByte()).toInt().shl(8) or ((bytes[0] and 0xFF.toByte()).toInt())
        val mainVersion1: Int = (mainVersion and 0xFFFF).shr(12)
        val mainVersion2: Int = (mainVersion and 0x0FC0).shr(6)
        val mainVersion3: Int = mainVersion and 0x003F
        val asicVersion = StringBuilder()
        1.shr(2)
        asicVersion.append(mainVersion1)
            .append(DOT)
            .append(mainVersion2)
            .append(DOT)
            .append(mainVersion3)
            .append(DOT)
            .append(HexDump.toHexString(bytes[3]))
            .append(HexDump.toHexString(bytes[2]))
            .append(HexDump.toHexString(bytes[5]))
            .append(HexDump.toHexString(bytes[4]))
        return asicVersion.toString()
    }

    fun getSN(): String {
        val PARAM_INDEX_SN = 39
        val bytes = getParam(PARAM_INDEX_SN * 2, 15)
        return String(bytes, StandardCharsets.US_ASCII)
    }

    fun getId(): String {
        val PARAM_INDEX_ID = 192
        val bytes = getParam(PARAM_INDEX_ID * 2, 17)
        return String(bytes, StandardCharsets.US_ASCII)
    }

    fun getShutterStatus(): Int {
        val PARAM_INDEX_SHUTTER_STATUS = 12
        return getParam(PARAM_INDEX_SHUTTER_STATUS * 2, 1, 0).toInt()
    }

    fun getImageStatus(): Int {
        val PARAM_INDEX_IMAGE_STATUS = 13
        return getParam(PARAM_INDEX_IMAGE_STATUS * 2, 1, 0).toInt()
    }

    fun upgrade(path: String?): FirmwareUpgradeResultCode? {
        if (mGuideUsbManager == null) {
            return FirmwareUpgradeResultCode.USB_DEVICE_ERROR
        }
        if (TextUtils.isEmpty(path)) {
            return FirmwareUpgradeResultCode.FILE_ERROR
        }
        val file = File(path)
        if (!file.exists()) {
            return FirmwareUpgradeResultCode.FILE_NOT_EXISTS
        }
        if (!mGuideUsbManager!!.isUsbValid()) {
            return FirmwareUpgradeResultCode.USB_DEVICE_ERROR
        }
        val bos = ByteArrayOutputStream(file.length().toInt())
        var `in`: BufferedInputStream? = null
        try {
            `in` = BufferedInputStream(FileInputStream(file))
            val bufSize = 1024
            val buffer = ByteArray(bufSize)
            var len = 0
            while (-1 != `in`.read(buffer, 0, bufSize).also { len = it }) {
                bos.write(buffer, 0, len)
            }
        } catch (e: Exception) {
            try {
                `in`!!.close()
                bos.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return FirmwareUpgradeResultCode.FILE_READ_ERROR
        }
        val allData = bos.toByteArray()
        return if (mGuideUsbManager!!.upgrade(allData)) {
            FirmwareUpgradeResultCode.SUCCESS
        } else {
            FirmwareUpgradeResultCode.FILE_WRITE_ERROR
        }
    }
}