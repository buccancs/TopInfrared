package com.topdon.ble.callback;

import com.topdon.ble.Request;

public interface RequestFailedCallback extends RequestCallback {
    
    void onRequestFailed(Request request, int failType, Object value);
}
