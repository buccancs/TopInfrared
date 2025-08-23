package com.topdon.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import androidx.annotation.Nullable;

class DefaultDeviceCreator implements DeviceCreator {
    @Nullable
    @Override
    public Device create(BluetoothDevice device, ScanResult scanResult) {
        return new Device(device);
    }
}
