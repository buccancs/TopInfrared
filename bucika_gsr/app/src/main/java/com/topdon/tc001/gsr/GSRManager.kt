package com.topdon.tc001.gsr

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.shimmerresearch.android.Shimmer
import com.shimmerresearch.android.guiUtilities.ShimmerBluetoothDialog
import com.shimmerresearch.android.manager.ShimmerBluetoothManagerAndroid
import com.shimmerresearch.driver.Configuration
import com.shimmerresearch.driver.ObjectCluster

/**
 * GSR (Galvanic Skin Response) Manager for bucika_gsr version
 * Integrates ShimmerAndroidAPI functionality for physiological monitoring
 */
class GSRManager private constructor(private val context: Context) {
    
    private var shimmerManager: ShimmerBluetoothManagerAndroid? = null
    private var connectedShimmer: Shimmer? = null
    private var isRecording = false
    
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
        shimmerManager = ShimmerBluetoothManagerAndroid(context, mHandler)
        shimmerManager?.let { manager ->
            // Enable GSR and related sensors
            val sensorsToEnable = (Configuration.Shimmer3.SENSOR_A_ACCEL or 
                                 Configuration.Shimmer3.SENSOR_MPU9X50_GYRO or 
                                 Configuration.Shimmer3.SENSOR_GSR or 
                                 Configuration.Shimmer3.SENSOR_INT_EXP_ADC_A15)
            manager.enableSensors(sensorsToEnable)
            
            // Set sampling rate to 128 Hz as per requirements
            manager.setSamplingRate(128.0)
            
            // Configure GSR range (auto-ranging)
            manager.setGSRRange(0) // Auto range
        }
    }
    
    fun startRecording(): Boolean {
        return if (connectedShimmer != null && !isRecording) {
            shimmerManager?.startStreaming()
            isRecording = true
            true
        } else {
            false
        }
    }
    
    fun stopRecording(): Boolean {
        return if (isRecording) {
            shimmerManager?.stopStreaming()
            isRecording = false
            true
        } else {
            false
        }
    }
    
    fun connectToShimmer(bluetoothAddress: String) {
        shimmerManager?.connectShimmerDevice(bluetoothAddress, true)
    }
    
    fun disconnectShimmer() {
        connectedShimmer?.let {
            shimmerManager?.disconnectShimmerDevice(it.bluetoothAddress)
        }
    }
    
    fun isConnected(): Boolean = connectedShimmer?.isConnected() == true
    
    fun isRecording(): Boolean = isRecording
    
    fun getConnectedDeviceName(): String? = connectedShimmer?.deviceName
    
    // Message handler for Shimmer callbacks
    private val mHandler = object : android.os.Handler() {
        override fun handleMessage(msg: android.os.Message) {
            when (msg.what) {
                ShimmerBluetoothManagerAndroid.MSG_IDENTIFIER_DATA_PACKET -> {
                    val data = msg.obj as ObjectCluster
                    processGSRData(data)
                }
                ShimmerBluetoothManagerAndroid.MSG_IDENTIFIER_STATE_CHANGE -> {
                    val shimmer = msg.obj as Shimmer
                    when (msg.arg1) {
                        Shimmer.STATE_CONNECTED -> {
                            connectedShimmer = shimmer
                            dataListener?.onConnectionStatusChanged(true, shimmer.deviceName)
                        }
                        Shimmer.STATE_CONNECTING -> {
                            // Connection in progress
                        }
                        Shimmer.STATE_NONE -> {
                            connectedShimmer = null
                            isRecording = false
                            dataListener?.onConnectionStatusChanged(false, null)
                        }
                    }
                }
            }
        }
    }
    
    private fun processGSRData(objectCluster: ObjectCluster) {
        try {
            val timestamp = System.currentTimeMillis()
            
            // Extract GSR value
            val gsrData = objectCluster.getFormatClusterValue(Configuration.Shimmer3.ObjectClusterSensorName.GSR_CONDUCTANCE, "CAL")
            val gsrValue = gsrData?.data ?: 0.0
            
            // Extract skin temperature if available
            val tempData = objectCluster.getFormatClusterValue(Configuration.Shimmer3.ObjectClusterSensorName.TEMPERATURE, "CAL")
            val skinTemp = tempData?.data ?: 0.0
            
            // Notify listener
            dataListener?.onGSRDataReceived(timestamp, gsrValue, skinTemp)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun cleanup() {
        stopRecording()
        disconnectShimmer()
        shimmerManager = null
        connectedShimmer = null
    }
}