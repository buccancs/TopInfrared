package com.topinfrared.tc001.standalone

import android.Manifest
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.permissionx.guolindev.PermissionX
import com.topinfrared.tc001.standalone.databinding.ActivityMainBinding
import com.topinfrared.tc001.standalone.usb.TC001UsbManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var usbManager: TC001UsbManager
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeUI()
        initializeUSB()
        requestPermissions()
    }
    
    private fun initializeUI() {
        binding.apply {
            btnConnectTC001.setOnClickListener {
                connectToTC001()
            }
            
            btnLocalFiles.setOnClickListener {
                val intent = Intent(this@MainActivity, LocalFilesActivity::class.java)
                startActivity(intent)
            }
            
            updateConnectionStatus(false)
        }
    }
    
    private fun initializeUSB() {
        usbManager = TC001UsbManager(
            activity = this,
            connectionCallback = { isConnected, device ->
                runOnUiThread {
                    updateConnectionStatus(isConnected, device)
                    if (isConnected) {
                        binding.btnConnectTC001.text = "Open Thermal View"
                        binding.btnConnectTC001.setOnClickListener {
                            openThermalActivity()
                        }
                    } else {
                        binding.btnConnectTC001.text = "Connect to TC001"
                        binding.btnConnectTC001.setOnClickListener {
                            connectToTC001()
                        }
                    }
                }
            },
            statusCallback = { status ->
                runOnUiThread {
                    updateStatusMessage(status)
                }
            }
        )
    }
    
    private fun connectToTC001() {
        lifecycleScope.launch {
            try {
                val connected = usbManager.connectToTC001()
                if (!connected) {
                    Toast.makeText(
                        this@MainActivity,
                        "TC001 device not found. Please connect the device.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to TC001", e)
                Toast.makeText(
                    this@MainActivity,
                    "Failed to connect: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun updateConnectionStatus(isConnected: Boolean, device: android.hardware.usb.UsbDevice? = null) {
        binding.apply {
            val statusText = if (isConnected && device != null) {
                "TC001 Connected: ${device.deviceName}"
            } else if (isConnected) {
                "TC001 Connected"
            } else {
                "TC001 Not Connected"
            }
            
            tvConnectionStatus.text = statusText
            tvConnectionStatus.setTextColor(
                getColor(
                    if (isConnected) android.R.color.holo_green_dark 
                    else android.R.color.holo_red_dark
                )
            )
            
            if (isConnected && device != null) {
                val deviceInfo = "VID: ${String.format("0x%04X", device.vendorId)}, " +
                              "PID: ${String.format("0x%04X", device.productId)}"
                tvDeviceInfo.text = deviceInfo
                tvDeviceInfo.visibility = android.view.View.VISIBLE
            } else {
                tvDeviceInfo.visibility = android.view.View.GONE
            }
            
            btnConnectTC001.isEnabled = true
        }
    }
    
    private fun updateStatusMessage(status: String) {
        binding.apply {
            tvStatusMessage.text = status
            tvStatusMessage.visibility = android.view.View.VISIBLE
            
            tvStatusMessage.postDelayed({
                if (tvStatusMessage.text == status) {
                    tvStatusMessage.visibility = android.view.View.GONE
                }
            }, 3000)
        }
    }
    
    private fun openThermalActivity() {
        val intent = Intent(this, ThermalActivity::class.java)
        startActivity(intent)
    }
    
    private fun requestPermissions() {
        PermissionX.init(this)
            .permissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .request { allGranted, _, deniedList ->
                if (!allGranted) {
                    Log.w(TAG, "Some permissions denied: $deniedList")
                    Toast.makeText(
                        this,
                        "Permissions required for TC001 functionality",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
    
    override fun onResume() {
        super.onResume()
        usbManager.checkTC001Connection()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        usbManager.cleanup()
    }
}