package com.topdon.ble.callback;

import com.topdon.ble.Request;

public interface MtuChangeCallback extends RequestFailedCallback {
    
    void onMtuChanged(Request request, int mtu);
}
