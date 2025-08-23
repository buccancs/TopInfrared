package com.topdon.ble.util;

public interface Logger {
    
    int TYPE_GENERAL = 0;
    
    int TYPE_SCAN_STATE = 1;
    
    int TYPE_CONNECTION_STATE = 2;
    
    int TYPE_CHARACTERISTIC_READ = 3;
    
    int TYPE_CHARACTERISTIC_CHANGED = 4;
    
    int TYPE_READ_REMOTE_RSSI = 5;
    
    int TYPE_MTU_CHANGED = 6;
    
    int TYPE_REQUEST_FAILED = 7;
    int TYPE_DESCRIPTOR_READ = 8;
    int TYPE_NOTIFICATION_CHANGED = 9;
    int TYPE_INDICATION_CHANGED = 10;
    int TYPE_CHARACTERISTIC_WRITE = 11;
    int TYPE_PHY_CHANGE = 12;

    void log(int priority, int type, String msg);

    void log(int priority, int type, String msg, Throwable th);

    void setEnabled(boolean isEnabled);

    boolean isEnabled();
}
