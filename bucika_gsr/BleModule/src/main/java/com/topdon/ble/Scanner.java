package com.topdon.ble;

import android.content.Context;

import com.topdon.ble.callback.ScanListener;

interface Scanner {
    
    void addScanListener(ScanListener listener);

    void removeScanListener(ScanListener listener);

    void startScan(Context context);

    void stopScan(boolean quietly);

    boolean isScanning();

    void onBluetoothOff();

    void release();

    ScannerType getType();
}
