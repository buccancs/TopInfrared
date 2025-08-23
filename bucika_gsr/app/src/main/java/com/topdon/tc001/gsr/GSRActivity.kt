package com.topdon.tc001.gsr

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.tc001.R
import kotlinx.android.synthetic.main.activity_gsr.*

class GSRActivity : BaseActivity(), GSRManager.GSRDataListener {
    
    private lateinit var gsrManager: GSRManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val BLUETOOTH_PERMISSION_REQUEST = 1001
    
    override fun initContentView(): Int = R.layout.activity_gsr
    
    override fun initView() {
        title_view.setTitle("GSR Monitoring - Bucika")
        title_view.setLeftClickListener { finish() }
        
        gsrManager = GSRManager.getInstance(this)
        gsrManager.setGSRDataListener(this)
        gsrManager.initializeShimmer()
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        
        setupClickListeners()
        updateUI()
    }
    
    override fun initData() {
        checkBluetoothPermissions()
    }
    
    private fun setupClickListeners() {
        btn_connect_shimmer.setOnClickListener {
            if (checkBluetoothPermissions()) {
                connectToShimmer()
            }
        }
        
        btn_disconnect_shimmer.setOnClickListener {
            gsrManager.disconnectShimmer()
        }
        
        btn_start_recording.setOnClickListener {
            startGSRRecording()
        }
        
        btn_stop_recording.setOnClickListener {
            stopGSRRecording()
        }
        
        btn_enhanced_recording.setOnClickListener {
            val intent = Intent(this, com.topdon.tc001.recording.EnhancedRecordingActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun checkBluetoothPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, BLUETOOTH_PERMISSION_REQUEST)
                return false
            }
        }
        return true
    }
    
    private fun connectToShimmer() {
        if (bluetoothAdapter?.isEnabled != true) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, 1000)
            return
        }
        
        val shimmerAddress = "00:06:66:XX:XX:XX"
        gsrManager.connectToShimmer(shimmerAddress)
        
        Toast.makeText(this, "Attempting to connect to Shimmer device...", Toast.LENGTH_SHORT).show()
    }
    
    private fun startGSRRecording() {
        if (gsrManager.isConnected()) {
            if (gsrManager.startRecording()) {
                Toast.makeText(this, "GSR recording started", Toast.LENGTH_SHORT).show()
                updateUI()
            } else {
                Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please connect to Shimmer device first", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun stopGSRRecording() {
        if (gsrManager.stopRecording()) {
            Toast.makeText(this, "GSR recording stopped", Toast.LENGTH_SHORT).show()
            updateUI()
        }
    }
    
    private fun updateUI() {
        val isConnected = gsrManager.isConnected()
        val isRecording = gsrManager.isRecording()
        
        btn_connect_shimmer.isEnabled = !isConnected
        btn_disconnect_shimmer.isEnabled = isConnected
        btn_start_recording.isEnabled = isConnected && !isRecording
        btn_stop_recording.isEnabled = isRecording
        
        tv_connection_status.text = if (isConnected) {
            "Connected to: ${gsrManager.getConnectedDeviceName()}"
        } else {
            "Not connected"
        }
        
        tv_recording_status.text = if (isRecording) {
            "Recording GSR data..."
        } else {
            "Ready to record"
        }
    }
    
    override fun onGSRDataReceived(timestamp: Long, gsrValue: Double, skinTemperature: Double) {
        runOnUiThread {
            tv_gsr_value.text = "GSR: %.3f µS".format(gsrValue)
            tv_skin_temp.text = "Skin Temp: %.2f °C".format(skinTemperature)
            tv_last_update.text = "Last update: ${java.text.SimpleDateFormat("HH:mm:ss.SSS").format(timestamp)}"
        }
    }
    
    override fun onConnectionStatusChanged(isConnected: Boolean, deviceName: String?) {
        runOnUiThread {
            updateUI()
            val message = if (isConnected) {
                "Connected to $deviceName"
            } else {
                "Disconnected from Shimmer device"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth permissions required for GSR functionality", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        gsrManager.cleanup()
    }
}