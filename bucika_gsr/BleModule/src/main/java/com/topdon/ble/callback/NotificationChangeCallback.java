package com.topdon.ble.callback;

import com.topdon.ble.Request;

public interface NotificationChangeCallback extends RequestFailedCallback {
    
    void onNotificationChanged(Request request, boolean isEnabled);
}
