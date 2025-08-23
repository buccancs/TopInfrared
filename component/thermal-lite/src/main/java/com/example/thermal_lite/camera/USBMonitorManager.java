package com.example.thermal_lite.camera;

import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.energy.iruvccamera.usb.DeviceFilter;
import com.energy.iruvccamera.usb.USBMonitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class USBMonitorManager {
    public static final String TAG = "USBMonitorManager";
    private USBMonitor mUSBMonitor;
    private boolean mDeviceConnected = false;

    private HashMap<String, OnUSBConnectListener> mOnUSBConnectListeners = new HashMap<>();

    private USBMonitorManager() {

    }

    private static USBMonitorManager mInstance;

    public static synchronized USBMonitorManager getInstance() {
        if (mInstance == null) {
            mInstance = new USBMonitorManager();
        }
        return mInstance;
    }

    public void addOnUSBConnectListener(String key, OnUSBConnectListener onUSBConnectListener) {
        mOnUSBConnectListeners.put(key, onUSBConnectListener);
    }

    public void removeOnUSBConnectListener(String key) {
        mOnUSBConnectListeners.remove(key);
    }

    public void init() {
        if (mUSBMonitor == null) {
            mUSBMonitor = new USBMonitor(Utils.getApp().getApplicationContext(),
                    new USBMonitor.OnDeviceConnectListener() {

                @Override
                public void onAttach(UsbDevice device) {
                    Log.w(TAG, "onAttach" + device.getProductId());
                    mUSBMonitor.requestPermission(device);
                    for (Map.Entry<String, OnUSBConnectListener> entry: mOnUSBConnectListeners.entrySet()) {
                        entry.getValue().onAttach(device);
                    }
                }

                @Override
                public void onGranted(UsbDevice usbDevice, boolean granted) {
                    Log.d(TAG, "onGranted");
                    for (Map.Entry<String, OnUSBConnectListener> entry: mOnUSBConnectListeners.entrySet()) {
                        entry.getValue().onGranted(usbDevice, granted);
                    }
                }

                @Override
                public void onDetach(UsbDevice device) {
                    Log.d(TAG, "onDetach");
                    mDeviceConnected = false;
                    for (Map.Entry<String, OnUSBConnectListener> entry: mOnUSBConnectListeners.entrySet()) {
                        entry.getValue().onDetach(device);
                    }
                }

                @Override
                public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                    if (createNew) {
                        mDeviceConnected = true;
                        Log.w(TAG, "onConnect");
                        for (Map.Entry<String, OnUSBConnectListener> entry: mOnUSBConnectListeners.entrySet()) {
                            entry.getValue().onConnect(device, ctrlBlock, createNew);
                        }
                    }
                }

                @Override
                public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                    Log.w(TAG, "onDisconnect");
                    mDeviceConnected = false;
                    for (Map.Entry<String, OnUSBConnectListener> entry: mOnUSBConnectListeners.entrySet()) {
                        entry.getValue().onDisconnect(device, ctrlBlock);
                    }
                }

                @Override
                public void onCancel(UsbDevice device) {
                    Log.d(TAG, "onCancel");
                    mDeviceConnected = false;
                    for (Map.Entry<String, OnUSBConnectListener> entry: mOnUSBConnectListeners.entrySet()) {
                        entry.getValue().onCancel(device);
                    }
                }
            });
        }

    }

    public void registerMonitor() {
        if (mUSBMonitor != null) {
            Log.d(TAG, "registerMonitor");
            mUSBMonitor.register();
        }
    }

    public void unregisterMonitor() {
        if (mUSBMonitor != null) {
            Log.d(TAG, "unregisterMonitor");
            mUSBMonitor.unregister();
        }
    }

    public void destroyMonitor() {
        if (mUSBMonitor != null) {
            Log.d(TAG, "destroyMonitor");
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
    }

    public boolean isDeviceConnected() {
        return mDeviceConnected;
    }

    public List<DeviceFilter> getDeviceFilter() {
        if (mUSBMonitor != null) {
            mUSBMonitor.getDeviceFilter();
        }
        return null;
    }
}
