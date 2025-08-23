package com.topdon.ble.callback;

import com.topdon.ble.Request;

public interface ReadRssiCallback extends RequestFailedCallback {
    
    void onRssiRead(Request request, int rssi);
}
