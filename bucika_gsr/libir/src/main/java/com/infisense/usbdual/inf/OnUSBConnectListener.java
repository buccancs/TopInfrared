package com.infisense.usbdual.inf;

import android.hardware.usb.UsbDevice;

import com.energy.iruvc.ircmd.IRCMD;
import com.energy.iruvc.usb.USBMonitor;


/**
 * @ProjectName: ANDROID_IRUVC_SDK
 * @Package: com.infisense.usbdual.utils
 * @ClassName: OnUSBConnectListener
 * @Description:
 * @Author: brilliantzhao
 * @CreateDate: 4/24/2023 1:37 PM
 * @UpdateUser:
 * @UpdateDate: 4/24/2023 1:37 PM
 * @UpdateRemark:
 * @Version: 1.0.0
 */
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
