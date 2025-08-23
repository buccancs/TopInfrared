package com.infisense.usbdual.inf;

import android.hardware.usb.UsbDevice;

import com.energy.iruvc.ircmd.IRCMD;
import com.energy.iruvc.usb.USBMonitor;

public interface OnUSBConnectListener {

    void onAttach(UsbDevice device);

    void onGranted(UsbDevice usbDevice, boolean granted);

    void onDettach(UsbDevice device);

    void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew);

    void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock);

    void onCancel(UsbDevice device);

    void onIRCMDInit(IRCMD ircmd);

    void onCompleteInit();

    void onSetPreviewSizeFail();

}
