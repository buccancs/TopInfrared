package com.topdon.libhik.util

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.annotation.IntRange
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.elvishew.xlog.XLog
import com.hcusbsdk.Interface.FStreamCallBack
import com.hcusbsdk.Interface.JavaInterface
import com.hcusbsdk.Interface.USB_FRAME_INFO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Arrays

object HikHelper {
    
    private var userId: Int = JavaInterface.USB_INVALID_USER_ID
    
    private var callbackId: Int = JavaInterface.USB_INVALID_CHANNEL

    private var timeoutJob: Job? = null
    
    private val streamCallBack = MyFStreamCallBack()

    fun bind(activity: ComponentActivity) {
        init(activity)
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                owner.lifecycleScope.launch {
                    startStream()
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                stopStream()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                release()
            }
        })
    }

    fun bind(fragment: Fragment) {
        init(fragment.requireContext())
        fragment.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                owner.lifecycleScope.launch {
                    startStream()
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                stopStream()
            }

            override fun onDestroy(owner: LifecycleOwner) {
                release()
            }
        })
    }

    private var hasInit = false
    
    fun init(context: Context): Boolean {
        if (userId != JavaInterface.USB_INVALID_USER_ID) {
            return true
        }

        if (!hasInit) {
            if (!HikCmdUtil.init()) {
                return false
            }
            hasInit = true
        }

        HikCmdUtil.setLogPath(context.getExternalFilesDir("hkLog"))

        userId = HikCmdUtil.login(context)
        return userId != JavaInterface.USB_INVALID_USER_ID
    }

    @Volatile
    private var wantCheckTimeout = false
    
    suspend fun startStream() {
        if (callbackId == JavaInterface.USB_INVALID_CHANNEL) {
            callbackId = HikCmdUtil.startStream(userId, streamCallBack)
            wantCheckTimeout = true
            timeoutJob = CoroutineScope(Dispatchers.Main).launch {
                while (wantCheckTimeout) {
                    delay(5000)
                    if (System.currentTimeMillis() - streamCallBack.beforeFrameTime > 5000) {
                        wantCheckTimeout = false
                        onTimeoutListener?.invoke()
                    }
                }
            }
            onReadyListener?.invoke()
        }
    }

    fun stopStream() {
        if (callbackId != JavaInterface.USB_INVALID_CHANNEL) {
            timeoutJob?.cancel()
            HikCmdUtil.removeStreamCallback(userId, callbackId)
            callbackId = JavaInterface.USB_INVALID_CHANNEL
            wantCheckTimeout = false
        }
    }

    fun release() {
        if (userId != JavaInterface.USB_INVALID_USER_ID) {
            JavaInterface.getInstance().USB_Logout(userId)
            userId = JavaInterface.USB_INVALID_USER_ID
        }
        if (hasInit) {
            JavaInterface.getInstance().USB_Cleanup()
            hasInit = false
        }
        streamCallBack.onTempListener = null
        streamCallBack.onFrameListener = null
        onTimeoutListener = null
    }

    fun setFrameListener(listener : ((ByteArray, ByteArray) -> Unit)) {
        streamCallBack.onFrameListener = listener
    }
    
    fun setTempListener(listener : ((ByteArray) -> Unit)) {
        streamCallBack.onTempListener = listener
    }

    var onTimeoutListener: (() -> Unit)? = null
    
    var onReadyListener: (() -> Unit)? = null

    fun copyFrameData(): ByteArray = streamCallBack.copyFrameArray()

    private class MyFStreamCallBack : FStreamCallBack {
        
        private val yuvArray = ByteArray(256 * 192 * 2)
        
        private val tempArray = ByteArray(256 * 192 * 2)
        
        @Volatile
        var onTempListener: ((ByteArray) -> Unit)? = null
        
        @Volatile
        var onFrameListener: ((ByteArray, ByteArray) -> Unit)? = null

        @Volatile
        var beforeFrameTime: Long = 0

        fun copyFrameArray(): ByteArray = synchronized(this) {
            val frameArray = ByteArray(256 * 192 * 4)
            System.arraycopy(yuvArray, 0, frameArray, 0, yuvArray.size)
            System.arraycopy(tempArray, 0, frameArray, yuvArray.size, tempArray.size)
            frameArray
        }

        override fun invoke(handle: Int, frameInfo: USB_FRAME_INFO) {
            beforeFrameTime = System.currentTimeMillis()
            if (frameInfo.dwBufSize != 4640 + 256 * 192 * 4) {
                XLog.w("数据长度不对！${frameInfo.dwBufSize}")
                return
            }
            val dataArray: ByteArray = Arrays.copyOf(frameInfo.pBuf, frameInfo.dwBufSize)

            synchronized(this) {
                System.arraycopy(dataArray, dataArray.size - yuvArray.size, yuvArray, 0, yuvArray.size)
                for (i in tempArray.indices step 2) {
                    val newValue = ((dataArray[4640 + i].toInt() and 0xff) or (dataArray[4640 + i + 1].toInt() and 0xff shl 8)) + 14272
                    tempArray[i] = (newValue and 0xff).toByte()
                    tempArray[i + 1] = (newValue shr 8 and 0xff).toByte()
                }
            }
            onTempListener?.invoke(tempArray)
            onFrameListener?.invoke(yuvArray, tempArray)
        }
    }

    suspend fun initConfig() = withContext(Dispatchers.IO) {
        HikCmdUtil.initConfig(userId)
    }

    suspend fun shutter() = withContext(Dispatchers.IO) {
        HikCmdUtil.shutter(userId)
    }

    suspend fun setAutoShutter(isAutoShutter: Boolean) = withContext(Dispatchers.IO) {
        HikCmdUtil.setAutoShutter(userId, isAutoShutter)
    }

    suspend fun setContrast(value: Int) = withContext(Dispatchers.IO) {
        HikCmdUtil.setContrast(userId, value)
    }

    suspend fun setEnhanceLevel(value: Int) = withContext(Dispatchers.IO) {
        HikCmdUtil.setEnhanceLevel(userId, value)
    }

    suspend fun setMirror(rotateAngle: Int, isMirror: Boolean) = withContext(Dispatchers.IO) {
        HikCmdUtil.setMirror(userId, rotateAngle, isMirror)
    }

    suspend fun setEmissivity(emissivity: Int) = withContext(Dispatchers.IO) {
        HikCmdUtil.setEmissivity(userId, emissivity)
    }

    suspend fun setDistance(distance: Int) = withContext(Dispatchers.IO) {
        HikCmdUtil.setDistance(userId, distance)
    }

    suspend fun setTemperatureMode(@IntRange(-1, 1) mode: Int) = withContext(Dispatchers.IO) {
        HikCmdUtil.setTemperatureMode(userId, mode)
    }

    suspend fun startCorrect() = withContext(Dispatchers.IO) {
        HikCmdUtil.startCorrect(userId)
    }
}