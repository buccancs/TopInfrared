package com.topdon.lib.core.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.elvishew.xlog.XLog
import com.topdon.lib.core.config.DeviceConfig
import com.topdon.lib.core.tools.PermissionTool

object BluetoothUtil {

    /**
     * 在给定 activity 生命周期内添加 蓝牙 开关状态监听.
     */
    fun addBtStateListener(activity: ComponentActivity, listener: ((isEnable: Boolean) -> Unit)) {
        activity.lifecycle.addObserver(BtStateObserver(activity, listener))
    }

    private class BtStateObserver(val context: Context, val listener: ((isEnable: Boolean) -> Unit)) : DefaultLifecycleObserver {
        private val receiver = BtStateReceiver()

        override fun onCreate(owner: LifecycleOwner) {
            context.registerReceiver(receiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        }

        override fun onDestroy(owner: LifecycleOwner) {
            context.unregisterReceiver(receiver)
            owner.lifecycle.removeObserver(this)
        }

        private inner class BtStateReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)) {
                    BluetoothAdapter.STATE_OFF -> listener.invoke(false)
                    BluetoothAdapter.STATE_ON -> listener.invoke(true)
                }
            }
        }
    }




    private val scanCallback = MyScanCallback()

    /**
     * 设置低功耗蓝牙搜索回调.
     */
    fun setLeScanListener(isTS004: Boolean, listener: (name: String) -> Unit) {
        scanCallback.isTS004 = isTS004
        scanCallback.listener = listener
    }

    /**
     * 开启低功耗蓝牙搜索，调用前需确保拥有相应权限且开启蓝牙.
     * @return true-调用成功 false-缺少权限或蓝牙未开启
     */
    @SuppressLint("MissingPermission")
    fun startLeScan(context: Context): Boolean {
        XLog.i("startLeScan()")

        if (!PermissionTool.hasBtPermission(context)) {
            XLog.e("开始蓝牙扫描-没有相应定位或蓝牙权限!")
            return false
        }

        val btAdapter: BluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val btLeScanner: BluetoothLeScanner? = btAdapter.bluetoothLeScanner
        if (btLeScanner == null) {
            XLog.e("开始蓝牙扫描-蓝牙未开启")
            return false
        }

        val settings = ScanSettings.Builder()
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        btLeScanner.startScan(null, settings, scanCallback)
        return true
    }

    /**
     * 停止低功耗蓝牙搜索，调用前需确保拥有相应权限且开启蓝牙.
     * @return true-调用成功 false-缺少权限或蓝牙未开启
     */
    @SuppressLint("MissingPermission")
    fun stopLeScan(context: Context): Boolean {
        XLog.i("stopBtScan()")

        if (!PermissionTool.hasBtPermission(context)) {
            XLog.w("停止蓝牙扫描-没有相应定位或蓝牙权限!")
            return false
        }

        val btAdapter: BluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val btLeScanner: BluetoothLeScanner? = btAdapter.bluetoothLeScanner
        if (btLeScanner == null) {
            XLog.w("停止蓝牙扫描-蓝牙未开启")
            return false
        }

        btLeScanner.stopScan(scanCallback)
        return true
    }

    private class MyScanCallback : ScanCallback() {
        var isTS004: Boolean = false
        var listener: ((name: String) -> Unit)? = null

        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val name: String = result?.device?.name ?: return
            if (name.startsWith(if (isTS004) DeviceConfig.TS004_NAME_START else DeviceConfig.TC007_NAME_START)) {
                XLog.v("蓝牙扫描出一个目标设备：$name")
                listener?.invoke(name)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            XLog.e("蓝牙扫描失败！$errorCode")
        }
    }
}