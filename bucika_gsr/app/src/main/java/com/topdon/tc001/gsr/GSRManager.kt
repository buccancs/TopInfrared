package com.topdon.tc001.gsr

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.*
import kotlin.random.Random

class GSRManager private constructor(private val context: Context) {
    
    private var isSimulating = false
    private var isRecording = false
    private var simulatedDeviceName = "Shimmer GSR Device"
    private var simulationTimer: Timer? = null
    private val handler = Handler(Looper.getMainLooper())
    
    companion object {
        @Volatile
        private var INSTANCE: GSRManager? = null
        
        fun getInstance(context: Context): GSRManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GSRManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    interface GSRDataListener {
        fun onGSRDataReceived(timestamp: Long, gsrValue: Double, skinTemperature: Double)
        fun onConnectionStatusChanged(isConnected: Boolean, deviceName: String?)
    }
    
    private var dataListener: GSRDataListener? = null
    
    fun setGSRDataListener(listener: GSRDataListener) {
        this.dataListener = listener
    }
    
    fun initializeShimmer() {
    }
    
    fun startRecording(): Boolean {
        return if (isSimulating && !isRecording) {
            isRecording = true
            startSimulatedDataStream()
            true
        } else {
            false
        }
    }
    
    fun stopRecording(): Boolean {
        return if (isRecording) {
            isRecording = false
            stopSimulatedDataStream()
            true
        } else {
            false
        }
    }
    
    fun connectToShimmer(bluetoothAddress: String) {
        isSimulating = true
        handler.postDelayed({
            dataListener?.onConnectionStatusChanged(true, simulatedDeviceName)
        }, 1000)
        
    }
    
    fun disconnectShimmer() {
        isSimulating = false
        isRecording = false
        stopSimulatedDataStream()
        dataListener?.onConnectionStatusChanged(false, null)
        
    }
    
    fun isConnected(): Boolean = isSimulating
    
    fun isRecording(): Boolean = isRecording
    
    fun getConnectedDeviceName(): String? = if (isSimulating) simulatedDeviceName else null
    
    private fun startSimulatedDataStream() {
        simulationTimer = Timer()
        simulationTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (isRecording) {
                    val timestamp = System.currentTimeMillis()
                    val gsrValue = 5.0 + Random.nextDouble(-2.0, 5.0)
                    val skinTemp = 32.0 + Random.nextDouble(-1.0, 3.0)
                    
                    handler.post {
                        dataListener?.onGSRDataReceived(timestamp, gsrValue, skinTemp)
                    }
                }
            }
        }, 0, (1000.0 / 128.0).toLong())
    }
    
    private fun stopSimulatedDataStream() {
        simulationTimer?.cancel()
        simulationTimer = null
    }
    
    fun cleanup() {
        stopRecording()
        disconnectShimmer()
        simulationTimer?.cancel()
        simulationTimer = null
    }
}