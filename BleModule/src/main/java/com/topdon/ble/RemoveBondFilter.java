package com.topdon.ble;

import android.bluetooth.BluetoothDevice;

public interface RemoveBondFilter {
    boolean accept(BluetoothDevice device);
}
