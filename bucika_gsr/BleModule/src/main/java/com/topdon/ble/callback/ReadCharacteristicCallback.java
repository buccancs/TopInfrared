package com.topdon.ble.callback;

import com.topdon.ble.Request;

public interface ReadCharacteristicCallback extends RequestFailedCallback {
    
    void onCharacteristicRead(Request request, byte[] value);
}
