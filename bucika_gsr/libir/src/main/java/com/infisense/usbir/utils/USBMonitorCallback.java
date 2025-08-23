package com.infisense.usbir.utils;

public interface USBMonitorCallback {

    void onAttach();

    void onGranted();

    void onConnect();

    void onDisconnect();

    void onDettach();

    void onCancel();

}
