package com.infisense.usbir.utils;

/**
 * @ProjectName: ANDROID_IRUVC_SDK
 * @Package: com.infisense.usbirmini640.utils
 * @ClassName: USBMonitorCallback
 * @Description:
 * @Author: brilliantzhao
 * @CreateDate: 3/16/2023 1:20 PM
 * @UpdateUser:
 * @UpdateDate: 3/16/2023 1:20 PM
 * @UpdateRemark:
 * @Version: 1.0.0
 */
public interface USBMonitorCallback {

    void onAttach();

    void onGranted();

    void onConnect();

    void onDisconnect();

    void onDettach();

    void onCancel();

}
