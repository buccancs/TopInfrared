package com.topinfrared.tc001.standalone

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.topinfrared.tc001.standalone.databinding.ActivityThermalBinding
import com.topinfrared.tc001.standalone.thermal.TC001ThermalManager
import kotlinx.coroutines.launch

class ThermalActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityThermalBinding
    private lateinit var thermalManager: TC001ThermalManager
    
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
            
            // Thermal controls
            btnTempModePoint.setOnClickListener {
                thermalManager.setTemperatureMeasureMode(TC001ThermalManager.TempMode.POINT)
                updateTempModeUI(TC001ThermalManager.TempMode.POINT)
            }
            
            btnTempModeLine.setOnClickListener {
                thermalManager.setTemperatureMeasureMode(TC001ThermalManager.TempMode.LINE)
                updateTempModeUI(TC001ThermalManager.TempMode.LINE)
            }
            
            btnTempModeArea.setOnClickListener {
                thermalManager.setTemperatureMeasureMode(TC001ThermalManager.TempMode.AREA)
                updateTempModeUI(TC001ThermalManager.TempMode.AREA)
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
                    Toast.makeText(
                        this@ThermalActivity,
                        "Recording saved: $filename",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val started = thermalManager.startRecording()
                    if (started) {
                        binding.btnRecord.text = "Stop Recording"
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
        thermalManager.cleanup()
    }
}