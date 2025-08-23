package com.topdon.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import androidx.annotation.Nullable;

public interface DeviceCreator {
    
    @Nullable
    Device create(BluetoothDevice device, ScanResult scanResult);
}
