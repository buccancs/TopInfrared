package com.topdon.ble;

import android.content.Context;

import com.topdon.ble.callback.ScanListener;


/**
 * 蓝牙设备搜索器
 * 
 * date: 2019/10/1 14:41
 * author: bichuanfeng
 */
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
