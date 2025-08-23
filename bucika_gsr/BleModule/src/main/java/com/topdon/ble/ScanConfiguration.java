package com.topdon.ble;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.List;

public class ScanConfiguration {
    int scanPeriodMillis = 10000;
    boolean acceptSysConnectedDevice;
    ScanSettings scanSettings;
    boolean onlyAcceptBleDevice;
    int rssiLowLimit = -120;
    List<ScanFilter> filters;

    public int getScanPeriodMillis() {
        return scanPeriodMillis;
    }

    public boolean isAcceptSysConnectedDevice() {
        return acceptSysConnectedDevice;
    }

    public ScanSettings getScanSettings() {
        return scanSettings;
    }

    public boolean isOnlyAcceptBleDevice() {
        return onlyAcceptBleDevice;
    }

    public int getRssiLowLimit() {
        return rssiLowLimit;
    }

    public List<ScanFilter> getFilters() {
        return filters;
    }

    public ScanConfiguration setScanPeriodMillis(int scanPeriodMillis) {
        if (scanPeriodMillis >= 1000) {
            this.scanPeriodMillis = scanPeriodMillis;
        }
        return this;
    }

    public ScanConfiguration setAcceptSysConnectedDevice(boolean acceptSysConnectedDevice) {
        this.acceptSysConnectedDevice = acceptSysConnectedDevice;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanConfiguration setScanSettings(ScanSettings scanSettings) {
        Inspector.requireNonNull(scanSettings, "scanSettings can't be null");
        this.scanSettings = scanSettings;
        return this;
    }

    public ScanConfiguration setOnlyAcceptBleDevice(boolean onlyAcceptBleDevice) {
        this.onlyAcceptBleDevice = onlyAcceptBleDevice;
        return this;
    }

    public ScanConfiguration setRssiLowLimit(int rssiLowLimit) {
        this.rssiLowLimit = rssiLowLimit;
        return this;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanConfiguration setFilters(List<ScanFilter> filters) {
        this.filters = filters;
        return this;
    }
}
