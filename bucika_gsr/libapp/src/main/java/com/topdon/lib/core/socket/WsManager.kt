package com.topdon.lib.core.socket

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.locks.ReentrantLock

class WsManager(private val wsUrl: String, private val okHttpClient: OkHttpClient, private val statusListener: IWebSocketListener) {
    companion object {
        private const val NORMAL_CLOSE_CODE = 1000
        private const val ABNORMAL_CLOSE_CODE = 1001

        private const val NORMAL_CLOSE_TIPS = "APP call close() and return true"
        private const val ABNORMAL_CLOSE_TIPS = "APP call close() and return false"
    }

    private var mWebSocket: WebSocket? = null
    private var status: State = State.DISCONNECTED //websocket连接状态
    private var heartBeatTimer: HeartBeatTimer? = null

    private val mWebSocketListener: WebSocketListener = object : WebSocketListener() {
        @Override
        override fun onOpen(webSocket: WebSocket, response: Response) {
            mWebSocket = webSocket
            status = State.CONNECTED

            //开始定时发送心跳
            heartBeatTimer?.cancel()
            heartBeatTimer = HeartBeatTimer(this@WsManager)
            heartBeatTimer?.timeoutListener = {
                statusListener.runMain {
                    it.onHeartBeatTimeout()
                }
            }
            heartBeatTimer?.start()

            statusListener.runMain {
                it.onOpen(webSocket, response)
            }
        }

        @Override
        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            heartBeatTimer?.lastHeartBeatTime = System.currentTimeMillis()
            statusListener.runMain {
                it.onMessage(webSocket, bytes)
            }
        }

        @Override
        override fun onMessage(webSocket: WebSocket, text: String) {
            heartBeatTimer?.lastHeartBeatTime = System.currentTimeMillis()
            statusListener.runMain {
                it.onMessage(webSocket, text)
            }
        }

        @Override
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            status = State.DISCONNECTED
            statusListener.runMain {
                it.onClosing(webSocket, code, reason)
            }
        }

        @Override
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            status = State.DISCONNECTED

            heartBeatTimer?.cancel()
            heartBeatTimer = null

            statusListener.runMain {
                it.onClosed(webSocket, code, reason)
            }
        }

        @Override
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            status = State.DISCONNECTED
            statusListener.runMain {
                it.onFailure(webSocket, t, response)
            }
        }
    }


    fun isConnect(): Boolean = status == State.CONNECTING || status == State.CONNECTED


    private var mLock = ReentrantLock()
    @Synchronized
    fun startConnect() {
        if (status == State.CONNECTING || status == State.CONNECTED) {//连接中或已连接
            Log.w("WebSocket", "${if (status == State.CONNECTING) "连接中" else "已连接"} startConnect() 重复调用")
            return
        }
        status = State.CONNECTING

        okHttpClient.dispatcher.cancelAll()
        val mRequest: Request = Request.Builder()
            .url(wsUrl)
            .build()
        try {
            mLock.lockInterruptibly()
            try {
                okHttpClient.newWebSocket(mRequest, mWebSocketListener)
            } finally {
                mLock.unlock()
            }
        } catch (_: InterruptedException) {
        }
    }

    fun stopConnect() {
        heartBeatTimer?.cancel()
        heartBeatTimer = null

        if (status == State.DISCONNECTED) {
            return
        }
        status = State.DISCONNECTED

        okHttpClient.dispatcher.cancelAll()
        if (mWebSocket != null) {
            val isClosed = mWebSocket!!.close(NORMAL_CLOSE_CODE, NORMAL_CLOSE_TIPS)
            if (isClosed) {
                statusListener.runMain {
                    it.onClosed(mWebSocket!!, NORMAL_CLOSE_CODE, NORMAL_CLOSE_TIPS)
                }
            } else {
                statusListener.runMain {
                    it.onClosed(mWebSocket!!, ABNORMAL_CLOSE_CODE, ABNORMAL_CLOSE_TIPS)
                }
            }
        }
    }

    fun sendMessage(msg: String?): Boolean {
        return send(msg)
    }

    fun sendMessage(byteString: ByteString?): Boolean {
        return send(byteString)
    }

    private fun send(msg: Any?): Boolean {
        var isSend = false
        if (mWebSocket != null && status == State.CONNECTED) {
            if (msg is String) {
                isSend = mWebSocket!!.send(msg)
            } else if (msg is ByteString) {
                isSend = mWebSocket!!.send(msg)
            }
        }
        return isSend
    }


    private val wsMainHandler = Handler(Looper.getMainLooper())
    private fun IWebSocketListener?.runMain(block: (IWebSocketListener) -> Unit) {
        if (this != null) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                wsMainHandler.post {
                    block(this)
                }
            } else {
                block(this)
            }
        }
    }


    private class HeartBeatTimer(val wsManager: WsManager) : Timer() {
        var timeoutListener: (() -> Unit)? = null

        @Volatile
        var lastHeartBeatTime: Long = 0

        fun start() {
            schedule(object : TimerTask() {
                override fun run() {
                    val currentTime = System.currentTimeMillis()
                    if (lastHeartBeatTime == 0L) {
                        lastHeartBeatTime = currentTime
                    }
                    if (currentTime - lastHeartBeatTime > 15 * 1000) {//3秒一个心跳包，连续丢失 5 个包视为断开
                        Log.d("WebSocket", "连续5个心跳包无响应，视为连接断开")
                        timeoutListener?.invoke()
                        lastHeartBeatTime = currentTime
                    } else {
                        val heartBeatMsg: String? = wsManager.statusListener.onHeartBeat()
                        if (heartBeatMsg == null) {
                            lastHeartBeatTime = currentTime
                        } else {
                            val isSuccess = wsManager.sendMessage(heartBeatMsg)
                            Log.v("WebSocket", "--> 发送心跳消息 ${if (isSuccess) "成功" else "失败"}")
                        }
                    }
                }
            }, 3000, 3000)
        }
    }

    abstract class IWebSocketListener : WebSocketListener() {
        /**
         * 返回要发送的心跳消息，null 则不发送.
         */
        abstract fun onHeartBeat(): String?

        /**
         * 心跳超时处理.
         */
        abstract fun onHeartBeatTimeout()
    }

    enum class State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
    }

    class Builder {
        private var wsUrl: String? = null
        private var okHttpClient: OkHttpClient? = null
        private var statusListener: IWebSocketListener? = null

        fun wsUrl(url: String?): Builder {
            wsUrl = url
            return this
        }

        fun client(client: OkHttpClient?): Builder {
            okHttpClient = client
            return this
        }

        fun setWsStatusListener(wsStatusListener: IWebSocketListener?): Builder {
            statusListener = wsStatusListener
            return this
        }

        fun build(): WsManager = WsManager(wsUrl!!, okHttpClient!!, statusListener!!)
    }
}