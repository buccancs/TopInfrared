package com.topdon.tc001.recording

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.TextureView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.module.thermal.ir.video.EnhancedVideoRecorder
import com.topdon.tc001.R
import com.topdon.tc001.gsr.GSRManager
import com.topdon.tc001.LocalFileBrowserActivity
import kotlinx.android.synthetic.main.activity_enhanced_recording.*

class EnhancedRecordingActivity : BaseActivity(), GSRManager.GSRDataListener {

    private lateinit var videoRecorder: EnhancedVideoRecorder
    private lateinit var gsrManager: GSRManager
    
    private var thermalView: TextureView? = null
    private var visualView: TextureView? = null
    
    private var isGSRConnected = false
    private var currentRecordingMode = EnhancedVideoRecorder.RecordingMode.SAMSUNG_4K_30FPS
    
    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun initContentView(): Int = R.layout.activity_enhanced_recording

    override fun initView() {
        title_view.setTitle("Enhanced Recording - Bucika GSR")
        title_view.setLeftClickListener { finish() }
        
        thermalView = texture_thermal
        visualView = texture_visual
        
        videoRecorder = EnhancedVideoRecorder(this, thermalView, visualView)
        gsrManager = GSRManager.getInstance(this)
        gsrManager.setGSRDataListener(this)
        
        setupClickListeners()
        updateUI()
    }

    override fun initData() {
        checkPermissions()
    }

    private fun setupClickListeners() {
        btn_samsung_4k.setOnClickListener {
            currentRecordingMode = EnhancedVideoRecorder.RecordingMode.SAMSUNG_4K_30FPS
            updateRecordingModeUI()
        }
        
        btn_rad_dng_level3.setOnClickListener {
            currentRecordingMode = EnhancedVideoRecorder.RecordingMode.RAD_DNG_LEVEL3_30FPS
            updateRecordingModeUI()
        }
        
        btn_parallel_recording.setOnClickListener {
            currentRecordingMode = EnhancedVideoRecorder.RecordingMode.PARALLEL_DUAL_STREAM
            updateRecordingModeUI()
        }
        
        btn_start_recording.setOnClickListener {
            startEnhancedRecording()
        }
        
        btn_stop_recording.setOnClickListener {
            stopEnhancedRecording()
        }
        
        btn_connect_gsr.setOnClickListener {
            connectGSRDevice()
        }
        
        btn_disconnect_gsr.setOnClickListener {
            disconnectGSRDevice()
        }
        
        btn_view_recordings.setOnClickListener {
            openRecordingsBrowser()
        }
    }

    private fun checkPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun startEnhancedRecording() {
        if (!videoRecorder.isRecording()) {
            val success = videoRecorder.startRecording(currentRecordingMode)
            if (success) {
                val modeText = when (currentRecordingMode) {
                    EnhancedVideoRecorder.RecordingMode.SAMSUNG_4K_30FPS -> "Samsung 4K 30FPS"
                    EnhancedVideoRecorder.RecordingMode.RAD_DNG_LEVEL3_30FPS -> "RAD DNG Level 3 30FPS"
                    EnhancedVideoRecorder.RecordingMode.PARALLEL_DUAL_STREAM -> "Parallel Dual Stream"
                }
                Toast.makeText(this, "$modeText recording started", Toast.LENGTH_SHORT).show()
                
                if (isGSRConnected && !gsrManager.isRecording()) {
                    gsrManager.startRecording()
                }
            } else {
                Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
            }
            updateUI()
        }
    }

    private fun stopEnhancedRecording() {
        if (videoRecorder.isRecording()) {
            val success = videoRecorder.stopRecording()
            if (success) {
                Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
                
                if (gsrManager.isRecording()) {
                    gsrManager.stopRecording()
                }
                
                val recordedFiles = videoRecorder.getRecordedFiles()
                val fileList = recordedFiles.joinToString("\n") { it.name }
                
                if (currentRecordingMode == EnhancedVideoRecorder.RecordingMode.RAD_DNG_LEVEL3_30FPS) {
                    val dngStats = videoRecorder.getDNGCaptureStats()
                    val framesCaptured = dngStats["framesCaptured"] as? Int ?: 0
                    val actualFPS = dngStats["actualFPS"] as? Double ?: 0.0
                    Toast.makeText(this, 
                        "DNG Recording Complete!\nFrames: $framesCaptured\nActual FPS: %.2f\nFiles: $fileList".format(actualFPS), 
                        Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Saved files:\n$fileList", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Failed to stop recording", Toast.LENGTH_SHORT).show()
            }
            updateUI()
        }
    }

    private fun connectGSRDevice() {
        val shimmerAddress = "00:06:66:XX:XX:XX"
        gsrManager.connectToShimmer(shimmerAddress)
        Toast.makeText(this, "Connecting to GSR device...", Toast.LENGTH_SHORT).show()
    }

    private fun disconnectGSRDevice() {
        gsrManager.disconnectShimmer()
        isGSRConnected = false
        updateUI()
    }

    private fun openRecordingsBrowser() {
        val intent = Intent(this, LocalFileBrowserActivity::class.java)
        startActivity(intent)
    }

    private fun updateRecordingModeUI() {
        btn_samsung_4k.isSelected = (currentRecordingMode == EnhancedVideoRecorder.RecordingMode.SAMSUNG_4K_30FPS)
        btn_rad_dng_level3.isSelected = (currentRecordingMode == EnhancedVideoRecorder.RecordingMode.RAD_DNG_LEVEL3_30FPS)
        btn_parallel_recording.isSelected = (currentRecordingMode == EnhancedVideoRecorder.RecordingMode.PARALLEL_DUAL_STREAM)
        
        val modeDescription = when (currentRecordingMode) {
            EnhancedVideoRecorder.RecordingMode.SAMSUNG_4K_30FPS -> 
                "Samsung optimized 4K recording at 30FPS with 20Mbps bitrate"
            EnhancedVideoRecorder.RecordingMode.RAD_DNG_LEVEL3_30FPS -> 
                "Professional RAD DNG Level 3 capture at 30FPS with RAW sensor data"
            EnhancedVideoRecorder.RecordingMode.PARALLEL_DUAL_STREAM -> 
                "Simultaneous thermal and visual stream recording"
        }
        tv_mode_description.text = modeDescription
    }

    private fun updateUI() {
        val isRecording = videoRecorder.isRecording()
        
        btn_start_recording.isEnabled = !isRecording
        btn_stop_recording.isEnabled = isRecording
        
        btn_samsung_4k.isEnabled = !isRecording
        btn_rad_dng_level3.isEnabled = !isRecording
        btn_parallel_recording.isEnabled = !isRecording
        
        btn_connect_gsr.isEnabled = !isGSRConnected
        btn_disconnect_gsr.isEnabled = isGSRConnected
        
        tv_recording_status.text = if (isRecording) {
            "Recording: ${currentRecordingMode.name}"
        } else {
            "Ready to record"
        }
        
        tv_gsr_status.text = if (isGSRConnected) {
            "GSR: Connected (${gsrManager.getConnectedDeviceName()})"
        } else {
            "GSR: Not connected"
        }
    }

    override fun onGSRDataReceived(timestamp: Long, gsrValue: Double, skinTemperature: Double) {
        runOnUiThread {
            tv_gsr_value.text = "GSR: %.3f µS".format(gsrValue)
            tv_skin_temp.text = "Skin: %.2f °C".format(skinTemperature)
            
            if (videoRecorder.isRecording()) {
                tv_recording_overlay.text = "Recording with GSR: ${gsrValue}µS @ ${skinTemperature}°C"
            }
        }
    }

    override fun onConnectionStatusChanged(isConnected: Boolean, deviceName: String?) {
        runOnUiThread {
            isGSRConnected = isConnected
            updateUI()
            
            val message = if (isConnected) {
                "GSR Connected: $deviceName"
            } else {
                "GSR Disconnected"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (!allGranted) {
                Toast.makeText(
                    this,
                    "All permissions required for enhanced recording functionality",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoRecorder.cleanup()
        gsrManager.cleanup()
    }
}