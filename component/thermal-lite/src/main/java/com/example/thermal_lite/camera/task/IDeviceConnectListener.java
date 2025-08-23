package com.example.thermal_lite.camera.task;

public interface IDeviceConnectListener {
    void onPrepareConnect();
    void onConnected();
    void onDisconnected();
    void onPaused();
    void onResumed();
}
