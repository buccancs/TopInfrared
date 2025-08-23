package com.topdon.ble.callback;

import com.topdon.ble.Request;

public interface WriteCharacteristicCallback extends RequestFailedCallback {
    
    void onCharacteristicWrite(Request request, byte[] value);
}
