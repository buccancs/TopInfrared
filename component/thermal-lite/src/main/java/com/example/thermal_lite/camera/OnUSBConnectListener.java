package com.example.thermal_lite.camera;

import android.hardware.usb.UsbDevice;

import com.energy.iruvccamera.usb.USBMonitor;

public interface OnUSBConnectListener {

    void onAttach(UsbDevice device);

    void onGranted(UsbDevice usbDevice, boolean granted);

    void onDetach(UsbDevice device);

    void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew);

    void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock);

    void onCancel(UsbDevice device);

    void onCompleteInit();

}
