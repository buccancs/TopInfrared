package com.topdon.ble.callback;

import com.topdon.ble.Request;

public interface ReadDescriptorCallback extends RequestFailedCallback {
    
    void onDescriptorRead(Request request, byte[] value);
}
