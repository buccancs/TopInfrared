package com.infisense.usbdual.camera;

import android.hardware.usb.UsbDevice;
import android.os.SystemClock;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.energy.iruvc.ircmd.ConcreteIRCMDBuilder;
import com.energy.iruvc.ircmd.IRCMD;
import com.energy.iruvc.ircmd.IRCMDType;
import com.energy.iruvc.usb.USBMonitor;
import com.energy.iruvc.utils.CommonParams;
import com.energy.iruvc.utils.DeviceType;
import com.energy.iruvc.uvc.CameraSize;
import com.energy.iruvc.uvc.ConcreateUVCBuilder;
import com.energy.iruvc.uvc.UVCCamera;
import com.energy.iruvc.uvc.UVCType;
import com.infisense.usbdual.Const;
import com.infisense.usbdual.inf.OnUSBConnectListener;

import java.util.ArrayList;
import java.util.List;

public class USBMonitorManager {
    public static final String TAG = "USBMonitorManager";
    private USBMonitor mUSBMonitor;
    private UVCCamera mUvcCamera;
    private IRCMD mIrcmd;
    private CommonParams.DataFlowMode mDefaultDataFlowMode = CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT;
    private CommonParams.GainMode gainMode = CommonParams.GainMode.GAIN_MODE_HIGH_LOW;
    private boolean isUseIRISP;
    private boolean isUseGPU = true;
    private int cameraWidth;
    private int cameraHeight;
    private byte[] tau_data_H;
    private byte[] tau_data_L;
    private long tempinfo = 0;
    private short[] nuc_table_high = new short[8192];
    private short[] nuc_table_low = new short[8192];

    private byte[] priv_high = new byte[1201];
    private byte[] priv_low = new byte[1201];
    private short[] kt_high = new short[1201];
    private short[] kt_low = new short[1201];
    private short[] bt_high = new short[1201];
    private short[] bt_low = new short[1201];
    private boolean isGetNucFromFlash;
    private CommonParams.GainStatus gainStatus = CommonParams.GainStatus.HIGH_GAIN;
    private int[] curVtemp = new int[1];

    private List<OnUSBConnectListener> mOnUSBConnectListeners = new ArrayList<>();

    private boolean isTempReplacedWithTNREnabled;

    private boolean isReStart = false;
    private int mPid = 0;

    private USBMonitorManager() {
    }

    private static USBMonitorManager mInstance;

    public static synchronized USBMonitorManager getInstance() {
        if (mInstance == null) {
            mInstance = new USBMonitorManager();
        }
        return mInstance;
    }

    public void addOnUSBConnectListener(OnUSBConnectListener onUSBConnectListener) {
        mOnUSBConnectListeners.add(onUSBConnectListener);
    }

    public void removeOnUSBConnectListener(OnUSBConnectListener onUSBConnectListener) {
        mOnUSBConnectListeners.remove(onUSBConnectListener);
    }

    public boolean isReStart() {
        return isReStart;
    }

    public void setReStart(boolean reStart) {
        isReStart = reStart;
    }

    public void init(int pid, boolean isUseIRISP, CommonParams.DataFlowMode defaultDataFlowMode) {
        this.mPid = pid;
        this.isUseIRISP = isUseIRISP;
        this.mDefaultDataFlowMode = defaultDataFlowMode;
        if (defaultDataFlowMode == CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT) {
            
            cameraWidth = 256;
            cameraHeight = 384;
        } else {
            
            cameraWidth = 256;
            cameraHeight = 192;
        }
        if (mUSBMonitor == null) {
            mUSBMonitor = new USBMonitor(Utils.getApp(),
                    new USBMonitor.OnDeviceConnectListener() {
                        @Override
                        public void onAttach(UsbDevice device) {
                            Log.w(TAG, "USBMonitorManager-onAttach-getProductId = " + device.getProductId());
                            Log.w(TAG, "USBMonitorManager-onAttach-mPid = " + mPid);
                            
                            if (device.getProductId() != mPid) {
                                return;
                            }
                            mUSBMonitor.requestPermission(device);
                            for (OnUSBConnectListener onUSBConnectListener : mOnUSBConnectListeners) {
                                onUSBConnectListener.onAttach(device);
                            }
                        }

                        @Override
                        public void onGranted(UsbDevice usbDevice, boolean granted) {
                            Log.d(TAG, "USBMonitorManager-onGranted");
                            for (OnUSBConnectListener onUSBConnectListener : mOnUSBConnectListeners) {
                                onUSBConnectListener.onGranted(usbDevice, granted);
                            }
                        }

                        @Override
                        public void onDettach(UsbDevice device) {
                            Log.d(TAG, "USBMonitorManager-onDettach");
                            Const.isDeviceConnected = false;
                            for (OnUSBConnectListener onUSBConnectListener : mOnUSBConnectListeners) {
                                onUSBConnectListener.onDettach(device);
                            }
                        }
                        @Override
                        public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock,
                                              boolean createNew) {
                            Log.w(TAG, "USBMonitorManager-onConnect");
                            if (createNew) {
                                Const.isDeviceConnected = true;
                                if (isReStart()) {
                                    SystemClock.sleep(2000);
                                }
                                handleUSBConnect(ctrlBlock);
                                for (OnUSBConnectListener onUSBConnectListener : mOnUSBConnectListeners) {
                                    onUSBConnectListener.onConnect(device, ctrlBlock, createNew);
                                }
                            }
                        }
                        @Override
                        public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                            Log.w(TAG, "USBMonitorManager-onDisconnect");
                            Const.isDeviceConnected = false;
                            for (OnUSBConnectListener onUSBConnectListener : mOnUSBConnectListeners) {
                                onUSBConnectListener.onDisconnect(device, ctrlBlock);
                            }
                        }

                        @Override
                        public void onCancel(UsbDevice device) {
                            Log.d(TAG, "USBMonitorManager-onCancel");
                            Const.isDeviceConnected = false;
                            for (OnUSBConnectListener onUSBConnectListener : mOnUSBConnectListeners) {
                                onUSBConnectListener.onCancel(device);
                            }
                        }
                    });
        }
    }

    public void registerUSB() {
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    public void unregisterUSB() {
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
        }
    }

    private void initUVCCamera() {
        Log.d(TAG, "initUVCCamera");

        ConcreateUVCBuilder concreateUVCBuilder = new ConcreateUVCBuilder();
        mUvcCamera = concreateUVCBuilder
                .setUVCType(UVCType.USB_UVC)
                .build();
        
        mUvcCamera.setDefaultBandwidth(1f);
    }

    public void openUVCCamera(USBMonitor.UsbControlBlock ctrlBlock) {
        if (mUvcCamera == null) {
            initUVCCamera();
        }
        mUvcCamera.openUVCCamera(ctrlBlock);
    }

    public UVCCamera getUvcCamera() {
        return mUvcCamera;
    }

    public IRCMD getIrcmd() {
        return mIrcmd;
    }

    public void handleUSBConnect(USBMonitor.UsbControlBlock ctrlBlock) {
        openUVCCamera(ctrlBlock);
        List<CameraSize> previewList = getAllSupportedSize();
        initIRCMD(previewList);
        if (mDefaultDataFlowMode == CommonParams.DataFlowMode.TNR_OUTPUT) {
            isTempReplacedWithTNREnabled = mIrcmd.isTempReplacedWithTNREnabled(DeviceType.P2);
            Log.i(TAG, "startPreview->isTempReplacedWithTNREnabled = " + isTempReplacedWithTNREnabled);
            if (isTempReplacedWithTNREnabled) {
                cameraWidth = 256;
                cameraHeight = 384;
            } else {
                cameraWidth = 256;
                cameraHeight = 192;
            }
        }
        int result = setPreviewSize(cameraWidth, cameraHeight);
        if (result == 0) {
            startPreview();
        }
    }

    private List<CameraSize> getAllSupportedSize() {
        Log.w(TAG, "getSupportedSize = " + mUvcCamera.getSupportedSize());
        List<CameraSize> previewList = new ArrayList<>();
        if (mUvcCamera != null) {
            previewList = mUvcCamera.getSupportedSizeList();
        }
        for (CameraSize size : previewList) {
            Log.i(TAG, "SupportedSize : " + size.width + " * " + size.height);
        }
        return previewList;
    }

    public void initIRCMD(List<CameraSize> previewList) {
        for (CameraSize size : previewList) {
            Log.i(TAG, "SupportedSize : " + size.width + " * " + size.height);
        }
        if (mUvcCamera != null) {
            ConcreteIRCMDBuilder concreteIRCMDBuilder = new ConcreteIRCMDBuilder();
            mIrcmd = concreteIRCMDBuilder
                    .setIrcmdType(IRCMDType.USB_IR_256_384)
                    .setIdCamera(mUvcCamera.getNativePtr())
                    .build();
            for (OnUSBConnectListener onUSBConnectListener : mOnUSBConnectListeners) {
                onUSBConnectListener.onIRCMDInit(mIrcmd);
            }
        }
    }

    private int setPreviewSize(int cameraWidth, int cameraHeight) {
        int result = -1;
        try {
            if (mUvcCamera != null) {
                result = mUvcCamera.setUSBPreviewSize(cameraWidth, cameraHeight);
            }
        } catch (Exception e) {
            for (OnUSBConnectListener onUSBConnectListener : mOnUSBConnectListeners) {
                onUSBConnectListener.onSetPreviewSizeFail();
            }
        }
        return result;
    }

    private void startPreview() {
        Log.d(TAG, "startPreview");

        if (mUvcCamera == null) {
            return;
        }

        Const.isReadFlashData = true;
        mUvcCamera.setOpenStatus(true);
        mUvcCamera.onStartPreview();
        if (isTempReplacedWithTNREnabled) {
            if (mIrcmd.startPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                    CommonParams.StartPreviewSource.SOURCE_SENSOR,
                    25, CommonParams.StartPreviewMode.VOC_DVP_MODE,
                    CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT) == 0) {
                if (mIrcmd.startY16ModePreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                        CommonParams.Y16ModePreviewSrcType.Y16_MODE_TEMPERATURE) == 0) {
                    mIrcmd.setPropImageParams(CommonParams.PropImageParams.IMAGE_PROP_SEL_MIRROR_FLIP,
                            Const.IR_MIRROR_FLIP_TYPE);
                }
            }
        } else {
            if (mIrcmd.startPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                    CommonParams.StartPreviewSource.SOURCE_SENSOR,
                    25, CommonParams.StartPreviewMode.VOC_DVP_MODE,
                    CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT) == 0) {
                mIrcmd.setPropImageParams(CommonParams.PropImageParams.IMAGE_PROP_SEL_MIRROR_FLIP,
                        Const.IR_MIRROR_FLIP_TYPE);
            }
        }
    }

    public void stopPreview() {
        Log.i(TAG, "stopPreview");
        if (mUvcCamera != null) {
            if (mUvcCamera.getOpenStatus()) {
                mUvcCamera.onStopPreview();
            }
            SystemClock.sleep(200);

            mUvcCamera.onDestroyPreview();
            mUvcCamera = null;
        }
    }

    public void onPauseUvcPreview() {
        if (mUvcCamera != null) {
            mUvcCamera.onPausePreview();
        }
    }

    public void onResumeUvcPreview() {
        if (mUvcCamera != null) {
            mUvcCamera.onResumePreview();
        }
    }

}
