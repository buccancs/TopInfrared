package com.topdon.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import androidx.annotation.Nullable;


/**
 * date: 2021/8/12 13:03
 * author: bichuanfeng
 */
class DefaultDeviceCreator implements DeviceCreator {
    @Nullable
    @Override
    public Device create(BluetoothDevice device, ScanResult scanResult) {
        return new Device(device);
    }
}
