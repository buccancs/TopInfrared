package com.topdon.ble;

import android.bluetooth.BluetoothDevice;



/**
 * 清空已配对设备时的过滤器
 * <p>
 * date: 2021/8/12 21:11
 * author: bichuanfeng
 */
public interface RemoveBondFilter {
    boolean accept(BluetoothDevice device);
}
