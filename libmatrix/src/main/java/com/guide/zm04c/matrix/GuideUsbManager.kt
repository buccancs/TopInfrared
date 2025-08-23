package com.guide.zm04c.matrix

import android.app.PendingIntent
import android.content.Context
import android.hardware.usb.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.guide.zm04c.matrix.ResultCode.ERROR_CONNECT_DEVICE_FAILD
import com.guide.zm04c.matrix.ResultCode.SUCC_CONNECT_INTERFACE
import com.guide.zm04c.matrix.utils.ByteUtils.toHexString
import com.guide.zm04c.matrix.utils.HexDump
import java.util.*

class GuideUsbManager {

    private var mContext: Context? = null
    private val mPermissionIntent: PendingIntent? = null
    private var mUsbManager: UsbManager? = null
    private var mUsbDevice: UsbDevice? = null
    private var mConnection: UsbDeviceConnection? = null
    private var mUsbInterface: UsbInterface? = null
    private var mEndpointDataIn: UsbEndpoint? = null
    private var mEndpointControlOut: UsbEndpoint? = null
    private var mEndpointControlIn: UsbEndpoint? = null

    companion object {
        val ADDRESS_ENDPOINT_DATA_IN = 129
        val ADDRESS_ENDPOINT_CONTROL_OUT = 2
        val ADDRESS_ENDPOINT_CONTROL_IN = 131
        val VENDOR_ID = 0x4206
        val PRODUCT_ID = 0x3702
    }

    private var mConnectCode: Int = ResultCode.READY_CONNECT_DEVICE
    private val TAG = "guidecore"
    private var mNativeGuideCore: NativeGuideCore? = null

    constructor(context: Context?, nativeGuideCore: NativeGuideCore?) {
        mContext = context
        mNativeGuideCore = nativeGuideCore
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun connectUsbDevice(): Int {
        if (mConnectCode == ResultCode.READY_CONNECT_DEVICE) {
            getUsbDevice()
            findInterface()
            val ret = openDevice()
            if (ret != SUCC_CONNECT_INTERFACE) {
                return ret
            }
            assignEndpoint()
        }
        if (mConnectCode != ResultCode.SUCC_FIND_ENDPOINT) {
            resetUsbDevice()
        }
        return mConnectCode
    }

    fun disconnectUsbDevice() {
        resetUsbDevice()
        mConnectCode = ResultCode.READY_CONNECT_DEVICE
    }

    fun isUsbValid(): Boolean {

        return true
    }

    private fun resetUsbDevice() {
        if (mConnection != null) {
            mConnection!!.releaseInterface(mUsbInterface)
            mConnection!!.close()
        }
        mUsbManager = null
        mUsbDevice = null
        mConnection = null
        mUsbInterface = null
        mEndpointDataIn = null
        mEndpointControlOut = null
        mEndpointControlIn = null
    }

    private fun getUsbDevice() {
        mUsbManager = mContext!!.getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = mUsbManager!!.deviceList
        if (!deviceList.isEmpty()) {
            for (device in deviceList.values) {
                if (device.vendorId == VENDOR_ID && device.productId == PRODUCT_ID) {
                    mUsbDevice = device
                    mConnectCode = ResultCode.SUCC_FIND_MATCHED_DEVICE
                    break
                }
            }
            if (mUsbDevice == null) {
                mConnectCode = ResultCode.ERROR_FIND_DEVICE_NOT_MATCH
            }
        } else {
            mConnectCode = ResultCode.ERROR_NOT_FIND_DEVICE
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun findInterface() {
        if (mUsbDevice != null) {
            val count = mUsbDevice!!.interfaceCount
            if (count == 1) {
                mUsbInterface = mUsbDevice!!.getInterface(0)
            } else {
                for (i in 0 until count) {
                    val usbInterface = mUsbDevice!!.getInterface(i)
                    if (usbInterface.endpointCount == 3 && usbInterface.alternateSetting == 0) {
                        mUsbInterface = usbInterface
                        mConnectCode = ResultCode.SUCC_FIND_DEVICE_INTERFACE
                        break
                    }
                }
                if (mUsbInterface == null) {
                    mConnectCode = ResultCode.ERROR_NOT_FIND_INTERFACE
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun openDevice(): Int {
        if (mUsbInterface != null) {
            mConnection = mUsbManager!!.openDevice(mUsbDevice)
            return if (mConnection != null) {
                Logger.d(TAG, "setInterface")
                mConnection!!.setInterface(mUsbInterface)
                if (mConnection!!.claimInterface(mUsbInterface, true)) {
                    Logger.d(TAG, "claimInterface true")
                    SUCC_CONNECT_INTERFACE.also { mConnectCode = it }
                } else {
                    Logger.d(TAG, "claimInterface false")
                    mConnection!!.close()
                    ERROR_CONNECT_DEVICE_FAILD.also { mConnectCode = it }
                }
            } else {
                ResultCode.ERROR_OPEN_DEVICE_FAILD.also { mConnectCode = it }
            }
        }
        return mConnectCode
    }

    private fun assignEndpoint() {
        if (mUsbInterface != null) {
            val endpointCount = mUsbInterface!!.endpointCount
            var usbEndpoint: UsbEndpoint
            for (i in 0 until endpointCount) {
                usbEndpoint = mUsbInterface!!.getEndpoint(i)
                val address = usbEndpoint.address
                when (address) {
                    ADDRESS_ENDPOINT_DATA_IN -> mEndpointDataIn = usbEndpoint
                    ADDRESS_ENDPOINT_CONTROL_OUT -> mEndpointControlOut = usbEndpoint
                    ADDRESS_ENDPOINT_CONTROL_IN -> mEndpointControlIn = usbEndpoint
                    else -> {
                    }
                }
            }
            mConnectCode = if (true) {
                ResultCode.SUCC_FIND_ENDPOINT
            } else {
                ResultCode.ERROR_FIND_ENDPOINT_FAILD
            }
        }
    }

    fun read(buffer: ByteArray): Int {
        return if (!isUsbValid()) {
            ResultCode.ERROR_USE_USB_ISVALID
        } else mConnection!!.bulkTransfer(mEndpointDataIn, buffer, buffer.size, 1000)
    }

    fun changePalette(i: Int) {
        val cmd = byteArrayOf(0x11, 0x00)
        sendUsbCmd(cmd, toByteArray(i))
    }

    fun shutter() {
        val cmd = byteArrayOf(0x15, 0x00)
        val data = byteArrayOf(0x00, 0x00, 0x00, 0x00)
        sendUsbCmd(cmd, data)
    }

    fun nuc() {
        val cmd = byteArrayOf(0x16, 0x00)
        val data = byteArrayOf(0x00, 0x00, 0x00, 0x00)
        sendUsbCmd(cmd, data)
    }

    fun upgrade(data: ByteArray): Boolean {
        val PAGE_SIZE = 3000

        val header = byteArrayOf(0x02)
        val cmd = byteArrayOf(0x07, 0x00)
        val reserve = byteArrayOf(0x00)
        val len = toByteArray(data.size)
        val check = toByteArray(mNativeGuideCore!!.crc(data))
        val upgradeHead = ByteArray(header.size + cmd.size + reserve.size + len.size + check.size)
        var destPos = 0
        System.arraycopy(header, 0, upgradeHead, destPos, header.size)
        destPos += header.size
        System.arraycopy(cmd, 0, upgradeHead, destPos, cmd.size)
        destPos += cmd.size
        System.arraycopy(reserve, 0, upgradeHead, destPos, reserve.size)
        destPos += reserve.size
        System.arraycopy(len, 0, upgradeHead, destPos, len.size)
        destPos += len.size
        System.arraycopy(check, 0, upgradeHead, destPos, check.size)
        if (!send(upgradeHead)) {
            return false
        }

        if (data.size <= PAGE_SIZE) {
            if (!send(data)) {
                return false
            }
        } else {
            var total = 0
            var sendBuf = ByteArray(PAGE_SIZE)
            while (total < data.size) {
                val sendLen = Math.min(PAGE_SIZE, data.size - total)
                if (sendLen != PAGE_SIZE) {
                    sendBuf = ByteArray(sendLen)
                }
                System.arraycopy(data, total, sendBuf, 0, sendLen)
                total += if (!send(sendBuf)) {
                    Logger.d(TAG, "upgrade senBuf failed")
                    return false
                } else {
                    sendLen
                }
            }
        }
        val tail = byteArrayOf(0x03)
        if (!send(tail)) {
            return false
        }

        val upgradeResultCmd = byteArrayOf(0x08, 0x00)
        return receive(upgradeResultCmd)
    }

    fun setRange(range: Int) {
        val cmd = byteArrayOf(0x20, 0x01)
        sendUsbCmd(cmd, toByteArray(range))
    }

    fun setEmiss(emiss: Int) {
        val cmd = byteArrayOf(0x21, 0x01)
        sendUsbCmd(cmd, toByteArray(emiss))
    }

    fun setDistance(value: Float) {
        val cmd = byteArrayOf(0x23, 0x01)
        val distance = (value * 10).toInt()
        sendUsbCmd(cmd, toByteArray(distance))
    }

    fun setBright(bright: Int) {
        val cmd = byteArrayOf(0x00, 0x02)
        sendUsbCmd(cmd, toByteArray(bright))
    }

    fun setContrast(contrast: Int) {
        val cmd = byteArrayOf(0x01, 0x02)
        sendUsbCmd(cmd, toByteArray(contrast))
    }

    private fun toByteArray(i: Int): ByteArray {
        val data = ByteArray(4)
        data[0] = (i and 0xFF).toByte()
        data[1] = (i shr 8 and 0xFF).toByte()
        data[2] = (i shr 16 and 0xFF).toByte()
        data[3] = (i shr 24 and 0xFF).toByte()
        return data
    }

    private fun sendUsbCmd(cmd: ByteArray, data: ByteArray): Int {
        val header = byteArrayOf(0x02)
        val reserve = byteArrayOf(0x00)
        val len = toByteArray(data.size)
        val check = toByteArray(mNativeGuideCore!!.crc(data))
        Log.w("123", "check: ${check.toHexString()}")
        val tail = byteArrayOf(0x03)
        val buffer =
            ByteArray(header.size + cmd.size + reserve.size + len.size + check.size + data.size + tail.size)
        var destPos = 0
        System.arraycopy(header, 0, buffer, destPos, header.size)
        destPos += header.size
        System.arraycopy(cmd, 0, buffer, destPos, cmd.size)
        destPos += cmd.size
        System.arraycopy(reserve, 0, buffer, destPos, reserve.size)
        destPos += reserve.size
        System.arraycopy(len, 0, buffer, destPos, len.size)
        destPos += len.size
        System.arraycopy(check, 0, buffer, destPos, check.size)
        destPos += check.size
        System.arraycopy(data, 0, buffer, destPos, data.size)
        destPos += data.size
        System.arraycopy(tail, 0, buffer, destPos, tail.size)
        val length = mConnection!!.bulkTransfer(mEndpointControlOut, buffer, buffer.size, 1000)
        Log.w("123", "sendUsbCmd: ${buffer.toHexString()}")
        Logger.d(TAG, "sendUsbCmd >> ${HexDump.dumpHexString(buffer)}".trimIndent())
        Logger.d(TAG, "<< end (length = $length)")
        return length
    }

    private fun send(buffer: ByteArray): Boolean {
        val length = mConnection!!.bulkTransfer(mEndpointControlOut, buffer, buffer.size, 1000)
        Logger.d(
            TAG,
            "send " + (length == buffer.size) + ": request len = " + buffer.size + " response len = " + length
        )
        return length == buffer.size
    }

    private fun receive(cmd: ByteArray): Boolean {
        val SUCCESS = byteArrayOf(0x00, 0x00, 0x00, 0x00)
        val buffer = ByteArray(17)
        var length = -1
        while (length < 0) {
            length = mConnection!!.bulkTransfer(mEndpointControlIn, buffer, buffer.size, 1000)
        }
        Logger.d(
            TAG, """receive length = $length
 data = ${HexDump.dumpHexString(buffer)}"""
        )
        val headReceive = ByteArray(1)
        val cmdReceive = ByteArray(2)
        val reserveReceive = ByteArray(1)
        val lenReceive = ByteArray(4)
        val checkReceive = ByteArray(4)
        val dataReceive = ByteArray(4)
        val tailReceive = ByteArray(1)
        var destPos = 0
        System.arraycopy(buffer, destPos, headReceive, 0, headReceive.size)
        Logger.d(TAG, "receive headReceive = " + HexDump.dumpHexString(headReceive))
        destPos += headReceive.size
        System.arraycopy(buffer, destPos, cmdReceive, 0, cmdReceive.size)
        Logger.d(TAG, "receive cmdReceive = " + HexDump.dumpHexString(cmdReceive))
        destPos += cmdReceive.size
        System.arraycopy(buffer, destPos, reserveReceive, 0, reserveReceive.size)
        Logger.d(TAG, "receive reserveReceive = " + HexDump.dumpHexString(reserveReceive))
        destPos += reserveReceive.size
        System.arraycopy(buffer, destPos, lenReceive, 0, lenReceive.size)
        Logger.d(TAG, "receive lenReceive = " + HexDump.dumpHexString(lenReceive))
        destPos += lenReceive.size
        System.arraycopy(buffer, destPos, checkReceive, 0, checkReceive.size)
        Logger.d(TAG, "receive checkReceive = " + HexDump.dumpHexString(checkReceive))
        destPos += checkReceive.size
        System.arraycopy(buffer, destPos, dataReceive, 0, dataReceive.size)
        Logger.d(TAG, "receive dataReceive = " + HexDump.dumpHexString(dataReceive))
        destPos += dataReceive.size
        System.arraycopy(buffer, destPos, tailReceive, 0, tailReceive.size)
        Logger.d(TAG, "receive tailReceive = " + HexDump.dumpHexString(tailReceive))
        return Arrays.equals(cmd, cmdReceive) && Arrays.equals(SUCCESS, dataReceive)
    }
}