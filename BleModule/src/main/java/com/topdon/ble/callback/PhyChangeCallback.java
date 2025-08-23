package com.topdon.ble.callback;

import android.bluetooth.BluetoothDevice;

import com.topdon.ble.Request;

public interface PhyChangeCallback extends RequestFailedCallback {
    
    void onPhyChange(Request request, int txPhy, int rxPhy);
}
