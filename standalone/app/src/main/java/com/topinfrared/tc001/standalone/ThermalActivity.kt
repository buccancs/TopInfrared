package com.topinfrared.tc001.standalone

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.topinfrared.tc001.standalone.databinding.ActivityThermalBinding
import com.topinfrared.tc001.standalone.thermal.TC001ThermalManager
import com.topinfrared.tc001.standalone.thermal.EnhancedRecordingManager
import com.topinfrared.tc001.standalone.ui.ThermalOverlayView
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ThermalActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityThermalBinding
    private lateinit var thermalManager: TC001ThermalManager
    private var isStandardRecording = false
    private var enhancedRecordingUpdateJob: kotlinx.coroutines.Job? = null
    
    companion object {
        private const val TAG = "ThermalActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThermalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeThermalManager()
        setupUI()
        startThermalView()
    }
    
    private fun initializeThermalManager() {
        thermalManager = TC001ThermalManager(this) { bitmap ->
            runOnUiThread {
                // Update thermal image display
                binding.ivThermalView.setImageBitmap(bitmap)
            }
        }
    }
    
    private fun setupUI() {
        binding.apply {
            // Back button
            btnBack.setOnClickListener {
                finish()
            }
            
            // Capture thermal image
            btnCapture.setOnClickListener {
                captureThermalImage()
            }
            
            // Toggle recording
            btnRecord.setOnClickListener {
                toggleRecording()
            }
            
            // Enhanced thermal controls with overlay configuration
            btnTempModePoint.setOnClickListener {
                thermalManager.setTemperatureMeasureMode(TC001ThermalManager.TempMode.POINT)
                binding.thermalOverlay.setTemperatureMeasureMode(TC001ThermalManager.TempMode.POINT)
                updateTempModeUI(TC001ThermalManager.TempMode.POINT)
            }
            
            btnTempModeLine.setOnClickListener {
                thermalManager.setTemperatureMeasureMode(TC001ThermalManager.TempMode.LINE)
                binding.thermalOverlay.setTemperatureMeasureMode(TC001ThermalManager.TempMode.LINE)
                updateTempModeUI(TC001ThermalManager.TempMode.LINE)
            }
            
            btnTempModeArea.setOnClickListener {
                thermalManager.setTemperatureMeasureMode(TC001ThermalManager.TempMode.AREA)
                binding.thermalOverlay.setTemperatureMeasureMode(TC001ThermalManager.TempMode.AREA)
                updateTempModeUI(TC001ThermalManager.TempMode.AREA)
            }
            
            // Additional overlay controls
            btnToggleGradient?.setOnClickListener {
                val currentState = (it.tag as? Boolean) ?: true
                val newState = !currentState
                it.tag = newState
                binding.thermalOverlay.setShowTemperatureGradient(newState)
                it.alpha = if (newState) 1.0f else 0.5f
            }
            
            btnToggleHistory?.setOnClickListener {
                val currentState = (it.tag as? Boolean) ?: false
                val newState = !currentState
                it.tag = newState
                binding.thermalOverlay.setShowMeasurementHistory(newState)
                it.alpha = if (newState) 1.0f else 0.5f
            }
            
            btnClearHistory?.setOnClickListener {
                binding.thermalOverlay.clearMeasurementHistory()
                Toast.makeText(this@ThermalActivity, "Measurement history cleared", Toast.LENGTH_SHORT).show()
            }
            
            // Enhanced recording controls
            btnSamsung4K?.setOnClickListener {
                toggleSamsung4KRecording()
            }
            
            btnRadWndL3?.setOnClickListener {
                toggleRadWndLevel3Recording()
            }
            
            btnParallelRec?.setOnClickListener {
                toggleParallelRecording()
            }
        }
    }
    
    private fun startThermalView() {
        lifecycleScope.launch {
            try {
                binding.progressLoading.visibility = View.VISIBLE
                
                val started = thermalManager.startThermalCapture()
                if (started) {
                    binding.progressLoading.visibility = View.GONE
                    binding.thermalControls.visibility = View.VISIBLE
                } else {
                    Toast.makeText(
                        this@ThermalActivity,
                        "Failed to start thermal capture",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start thermal view", e)
                Toast.makeText(
                    this@ThermalActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }
    
    private fun captureThermalImage() {
        lifecycleScope.launch {
            try {
                val filename = thermalManager.captureImage()
                if (filename != null) {
                    Toast.makeText(
                        this@ThermalActivity,
                        "Image saved: $filename",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@ThermalActivity,
                        "Failed to capture image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to capture thermal image", e)
                Toast.makeText(
                    this@ThermalActivity,
                    "Capture failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun toggleRecording() {
        lifecycleScope.launch {
            try {
                val isRecording = thermalManager.isRecording()
                if (isRecording) {
                    val filename = thermalManager.stopRecording()
                    binding.btnRecord.text = "Start Recording"
                    binding.tvRecordingDuration.visibility = View.GONE
                    stopRecordingTimer()
                    
                    Toast.makeText(
                        this@ThermalActivity,
                        "Recording saved: $filename",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val started = thermalManager.startRecording()
                    if (started) {
                        binding.btnRecord.text = "Stop Recording"
                        binding.tvRecordingDuration.visibility = View.VISIBLE
                        startRecordingTimer()
                        
                        Toast.makeText(
                            this@ThermalActivity,
                            "Recording started",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Recording toggle failed", e)
                Toast.makeText(
                    this@ThermalActivity,
                    "Recording error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private var recordingTimer: Runnable? = null
    
    private fun startRecordingTimer() {
        recordingTimer = object : Runnable {
            override fun run() {
                val duration = thermalManager.getRecordingDuration()
                val minutes = duration / 60
                val seconds = duration % 60
                binding.tvRecordingDuration.text = String.format("Recording: %02d:%02d", minutes, seconds)
                
                binding.tvRecordingDuration.postDelayed(this, 1000)
            }
        }
        recordingTimer?.let { binding.tvRecordingDuration.post(it) }
    }
    
    private fun stopRecordingTimer() {
        recordingTimer?.let { binding.tvRecordingDuration.removeCallbacks(it) }
        recordingTimer = null
    }
    
    private fun updateTempModeUI(mode: TC001ThermalManager.TempMode) {
        binding.apply {
            // Reset all button states
            btnTempModePoint.isSelected = false
            btnTempModeLine.isSelected = false
            btnTempModeArea.isSelected = false
            
            // Set active button
            when (mode) {
                TC001ThermalManager.TempMode.POINT -> btnTempModePoint.isSelected = true
                TC001ThermalManager.TempMode.LINE -> btnTempModeLine.isSelected = true
                TC001ThermalManager.TempMode.AREA -> btnTempModeArea.isSelected = true
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        thermalManager.pauseCapture()
    }
    
    override fun onResume() {
        super.onResume()
        thermalManager.resumeCapture()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopRecordingTimer()
        enhancedRecordingUpdateJob?.cancel()
        thermalManager.cleanup()
    }
    
    private fun toggleSamsung4KRecording() {
        lifecycleScope.launch {
            try {
                val type = EnhancedRecordingManager.Companion.RecordingType.SAMSUNG_4K_30FPS
                val isRecording = thermalManager.isEnhancedRecordingActive(type)
                
                if (isRecording) {
                    val filename = thermalManager.stopEnhancedRecording(type)
                    binding.btnSamsung4K.text = "Samsung 4K"
                    binding.btnSamsung4K.backgroundTintList = getColorStateList(R.color.purple_700)
                    binding.tvSamsung4KStatus.visibility = View.GONE
                    updateEnhancedRecordingVisibility()
                    
                    Toast.makeText(
                        this@ThermalActivity,
                        "Samsung 4K recording saved: $filename",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val started = thermalManager.startSamsung4KRecording()
                    if (started) {
                        binding.btnSamsung4K.text = "Stop 4K"
                        binding.btnSamsung4K.backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
                        binding.tvSamsung4KStatus.visibility = View.VISIBLE
                        updateEnhancedRecordingVisibility()
                        startEnhancedRecordingTimer()
                        
                        Toast.makeText(
                            this@ThermalActivity,
                            "Samsung 4K 30FPS recording started",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ThermalActivity,
                            "Samsung 4K recording not available on this device",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Samsung 4K recording toggle failed", e)
                Toast.makeText(
                    this@ThermalActivity,
                    "Samsung 4K error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun toggleRadWndLevel3Recording() {
        lifecycleScope.launch {
            try {
                val type = EnhancedRecordingManager.Companion.RecordingType.RAD_WND_LEVEL3_30FPS
                val isRecording = thermalManager.isEnhancedRecordingActive(type)
                
                if (isRecording) {
                    val filename = thermalManager.stopEnhancedRecording(type)
                    binding.btnRadWndL3.text = "RAD WND L3"
                    binding.btnRadWndL3.backgroundTintList = getColorStateList(R.color.purple_700)
                    binding.tvRadWndStatus.visibility = View.GONE
                    updateEnhancedRecordingVisibility()
                    
                    Toast.makeText(
                        this@ThermalActivity,
                        "RAD WND Level 3 recording saved: $filename",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val started = thermalManager.startRadWndLevel3Recording()
                    if (started) {
                        binding.btnRadWndL3.text = "Stop RAD"
                        binding.btnRadWndL3.backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
                        binding.tvRadWndStatus.visibility = View.VISIBLE
                        updateEnhancedRecordingVisibility()
                        startEnhancedRecordingTimer()
                        
                        Toast.makeText(
                            this@ThermalActivity,
                            "RAD WND Level 3 30FPS recording started",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ThermalActivity,
                            "RAD WND Level 3 recording failed to start",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "RAD WND Level 3 recording toggle failed", e)
                Toast.makeText(
                    this@ThermalActivity,
                    "RAD WND error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun toggleParallelRecording() {
        lifecycleScope.launch {
            try {
                val isAnyRecording = thermalManager.isAnyEnhancedRecordingActive()
                
                if (isAnyRecording) {
                    val filenames = thermalManager.stopAllEnhancedRecordings()
                    binding.btnParallelRec.text = "Parallel"
                    binding.btnParallelRec.backgroundTintList = getColorStateList(R.color.design_default_color_primary)
                    
                    // Reset individual buttons
                    binding.btnSamsung4K.text = "Samsung 4K"
                    binding.btnSamsung4K.backgroundTintList = getColorStateList(R.color.purple_700)
                    binding.btnRadWndL3.text = "RAD WND L3"
                    binding.btnRadWndL3.backgroundTintList = getColorStateList(R.color.purple_700)
                    
                    // Hide status indicators
                    binding.tvSamsung4KStatus.visibility = View.GONE
                    binding.tvRadWndStatus.visibility = View.GONE
                    updateEnhancedRecordingVisibility()
                    
                    val message = if (filenames.isNotEmpty()) {
                        "Parallel recording saved: ${filenames.size} files"
                    } else {
                        "Parallel recording stopped"
                    }
                    
                    Toast.makeText(this@ThermalActivity, message, Toast.LENGTH_SHORT).show()
                    
                } else {
                    val (samsung4KStarted, radWndStarted) = thermalManager.startParallelRecording()
                    
                    if (samsung4KStarted || radWndStarted) {
                        binding.btnParallelRec.text = "Stop All"
                        binding.btnParallelRec.backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
                        
                        if (samsung4KStarted) {
                            binding.btnSamsung4K.text = "Stop 4K"
                            binding.btnSamsung4K.backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
                            binding.tvSamsung4KStatus.visibility = View.VISIBLE
                        }
                        
                        if (radWndStarted) {
                            binding.btnRadWndL3.text = "Stop RAD"
                            binding.btnRadWndL3.backgroundTintList = getColorStateList(android.R.color.holo_red_dark)
                            binding.tvRadWndStatus.visibility = View.VISIBLE
                        }
                        
                        updateEnhancedRecordingVisibility()
                        startEnhancedRecordingTimer()
                        
                        val message = when {
                            samsung4KStarted && radWndStarted -> "Parallel recording: Samsung 4K + RAD WND L3 at 30FPS"
                            samsung4KStarted -> "Samsung 4K recording started at 30FPS"
                            radWndStarted -> "RAD WND Level 3 recording started at 30FPS"
                            else -> "No recordings started"
                        }
                        
                        Toast.makeText(this@ThermalActivity, message, Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(
                            this@ThermalActivity,
                            "Parallel recording failed to start",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Parallel recording toggle failed", e)
                Toast.makeText(
                    this@ThermalActivity,
                    "Parallel recording error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun updateEnhancedRecordingVisibility() {
        val hasActiveRecording = thermalManager.isAnyEnhancedRecordingActive()
        binding.enhancedRecordingStatus.visibility = if (hasActiveRecording) View.VISIBLE else View.GONE
    }
    
    private fun startEnhancedRecordingTimer() {
        enhancedRecordingUpdateJob?.cancel()
        enhancedRecordingUpdateJob = lifecycleScope.launch {
            while (thermalManager.isAnyEnhancedRecordingActive()) {
                try {
                    // Update Samsung 4K status
                    val samsung4KType = EnhancedRecordingManager.Companion.RecordingType.SAMSUNG_4K_30FPS
                    if (thermalManager.isEnhancedRecordingActive(samsung4KType)) {
                        val duration = thermalManager.getEnhancedRecordingDuration(samsung4KType)
                        val minutes = duration / 60
                        val seconds = duration % 60
                        binding.tvSamsung4KStatus.text = String.format("Samsung 4K: %02d:%02d", minutes, seconds)
                    }
                    
                    // Update RAD WND status
                    val radWndType = EnhancedRecordingManager.Companion.RecordingType.RAD_WND_LEVEL3_30FPS
                    if (thermalManager.isEnhancedRecordingActive(radWndType)) {
                        val duration = thermalManager.getEnhancedRecordingDuration(radWndType)
                        val minutes = duration / 60
                        val seconds = duration % 60
                        binding.tvRadWndStatus.text = String.format("RAD WND L3: %02d:%02d", minutes, seconds)
                    }
                    
                    delay(1000) // Update every second
                    
                } catch (e: Exception) {
                    Log.w(TAG, "Enhanced recording timer update failed", e)
                    break
                }
            }
        }
    }
}