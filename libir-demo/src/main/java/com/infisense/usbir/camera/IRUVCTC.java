package com.infisense.usbir.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.hardware.usb.UsbDevice;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.infisense.iruvc.ircmd.ConcreteIRCMDBuilder;
import com.infisense.iruvc.ircmd.IRCMD;
import com.infisense.iruvc.ircmd.IRCMDType;
import com.infisense.iruvc.sdkisp.LibIRProcess;
import com.infisense.iruvc.usb.DeviceFilter;
import com.infisense.iruvc.usb.USBMonitor;
import com.infisense.iruvc.utils.CommonParams;
import com.infisense.iruvc.utils.IFrameCallback;
import com.infisense.iruvc.utils.SynchronizedBitmap;
import com.infisense.iruvc.uvc.ConcreateUVCBuilder;
import com.infisense.iruvc.uvc.UVCCamera;
import com.infisense.iruvc.uvc.UVCType;
import com.infisense.usbir.R;
import com.infisense.usbir.config.MsgCode;
import com.infisense.usbir.utils.BitmapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class IRUVCTC {

    private static final String TAG = "IRUVC";
    private final int TinyB = 0x3901;
    private IFrameCallback iFrameCallback;
    private Context mContext;
    private UVCCamera uvcCamera;
    private IRCMD ircmd;
    private int[] curVtemp = new int[1];
    private USBMonitor mUSBMonitor;
    private int cameraWidth;
    private int cameraHeight;
    private byte[] imageSrc;
    private byte[] temperatureSrc;
    private SynchronizedBitmap syncimage;
    private int pids[] = {0x5840, 0x3901, 0x5830, 0x5838};
    private boolean auto_gain_switch = false;
    private boolean auto_over_portect = false;
    private LibIRProcess.AutoGainSwitchInfo_t auto_gain_switch_info = new LibIRProcess.AutoGainSwitchInfo_t();
    private LibIRProcess.GainSwitchParam_t gain_switch_param = new LibIRProcess.GainSwitchParam_t();
    private int count = 0;
    private long timestart = 0;
    private double fps = 0;
    private Handler mHandler;
    private int rotate = 0;
    private boolean isUseIRISP;
    private boolean isFrameReady = true;
    private CommonParams.GainStatus gainStatus = CommonParams.GainStatus.HIGH_GAIN;
    private CommonParams.GainMode gainMode = CommonParams.GainMode.GAIN_MODE_HIGH;
    private short[] nuc_table_high = new short[8192];
    private short[] nuc_table_low = new short[8192];
    private byte[] priv_high = new byte[1201];
    private byte[] priv_low = new byte[1201];
    private short[] kt_high = new short[1201];
    private short[] kt_low = new short[1201];
    private short[] bt_high = new short[1201];
    private short[] bt_low = new short[1201];

    public IRUVCTC(int cameraWidth, int cameraHeight, Context context, SynchronizedBitmap syncimage,
                   CommonParams.DataFlowMode dataFlowMode, boolean isUseIRISP) {
        this.cameraHeight = cameraHeight;
        this.cameraWidth = cameraWidth;
        this.mContext = context;
        this.syncimage = syncimage;
        this.isUseIRISP = isUseIRISP;
        init(cameraWidth, cameraHeight);
        mUSBMonitor = new USBMonitor(context, new USBMonitor.OnDeviceConnectListener() {

            @Override
            public void onAttach(UsbDevice device) {
                Log.w(TAG, "onAttach");
                if (isIRpid(device.getProductId())) {
                    if (uvcCamera == null || !uvcCamera.getOpenStatus()) {
                        mUSBMonitor.requestPermission(device);
                    }
                }
            }

            @Override
            public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                Log.w(TAG, "onConnect");
                if (isIRpid(device.getProductId())) {
                    if (createNew) {
                        openUVCCamera(ctrlBlock, dataFlowMode);
                        startPreview();
                    }
                }
            }

            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                Log.w(TAG, "onDisconnect");
            }

            @Override
            public void onDettach(UsbDevice device) {
                Log.w(TAG, "onDettach");
                if (isIRpid(device.getProductId())) {
                    if (uvcCamera != null && uvcCamera.getOpenStatus()) {
                        stopPreview();
                    }
                }
            }

            @Override
            public void onCancel(UsbDevice device) {
                Log.w(TAG, "onCancel");
            }
        });
        
        gain_switch_param.above_pixel_prop = 0.1f;
        gain_switch_param.above_temp_data = (int) ((130 + 273.15) * 16 * 4);
        gain_switch_param.below_pixel_prop = 0.95f;
        gain_switch_param.below_temp_data = (int) ((110 + 273.15) * 16 * 4);
        auto_gain_switch_info.switch_frame_cnt = 5 * 15;
        auto_gain_switch_info.waiting_frame_cnt = 7 * 15;
        int low_gain_over_temp_data = (int) ((550 + 273.15) * 16 * 4);
        int high_gain_over_temp_data = (int) ((100 + 273.15) * 16 * 4);
        float pixel_above_prop = 0.02f;
        int switch_frame_cnt = 7 * 15;
        int close_frame_cnt = 10 * 15;
        iFrameCallback = frame -> {
            if (!isFrameReady) {
                return;
            }
            count++;
            if (count == 100) {
                count = 0;
                long currentTimeMillis = System.currentTimeMillis();
                if (timestart != 0) {
                    long timeuse = currentTimeMillis - timestart;
                    fps = 100 * 1000 / (timeuse + 0.0);
                }
                timestart = currentTimeMillis;
                Log.d(TAG, "frame.length = " + frame.length + " fps=" + String.format("%.1f", fps) +
                        " dataFlowMode = " + dataFlowMode + " rotate = " + rotate);
            }
            if (syncimage == null) return;
            syncimage.start = true;
            synchronized (syncimage.dataLock) {
                int length = frame.length - 1;
                if (frame[length] == 1) {
                    if (mHandler != null)
                        mHandler.sendEmptyMessage(MsgCode.RESTART_USB);
                    Log.d(TAG, "RESTART_USB");
                    return;
                }
                if ((dataFlowMode == CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT) || isUseIRISP && (dataFlowMode == CommonParams.DataFlowMode.TEMP_OUTPUT)) {
                    
                    System.arraycopy(frame, 0, imageSrc, 0, length / 2);
                    LibIRProcess.ImageRes_t imageRes = new LibIRProcess.ImageRes_t();
                    imageRes.height = (char) cameraHeight;
                    imageRes.width = (char) cameraWidth;

                    if (rotate == 270) {
                        byte[] temp = new byte[length / 2];
                        System.arraycopy(frame, length / 2, temp, 0, length / 2);
                        LibIRProcess.rotateRight90(temp, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, temperatureSrc);
                    } else if (rotate == 90) {
                        byte[] temp = new byte[length / 2];
                        System.arraycopy(frame, length / 2, temp, 0, length / 2);
                        LibIRProcess.rotateLeft90(temp, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, temperatureSrc);
                    } else if (rotate == 180) {
                        byte[] temp = new byte[length / 2];
                        System.arraycopy(frame, length / 2, temp, 0, length / 2);
                        LibIRProcess.rotate180(temp, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, temperatureSrc);
                    } else {
                        System.arraycopy(frame, length / 2, temperatureSrc, 0, length / 2);
                    }

                    if (auto_gain_switch) {
                        ircmd.autoGainSwitch(temperatureSrc, imageRes, auto_gain_switch_info, gain_switch_param, null);
                    }
                    if (auto_over_portect) {
                        ircmd.avoidOverexposure(temperatureSrc, imageRes, low_gain_over_temp_data, high_gain_over_temp_data, pixel_above_prop, switch_frame_cnt, close_frame_cnt, null);
                    }
                } else if (!isUseIRISP && (dataFlowMode == CommonParams.DataFlowMode.IMAGE_OUTPUT)) {
                    System.arraycopy(frame, 0, imageSrc, 0, length);
                } else if (!isUseIRISP && (dataFlowMode == CommonParams.DataFlowMode.TEMP_OUTPUT)) {
                    System.arraycopy(frame, 0, imageSrc, 0, length);
                }
            }
        };
    }

    public void setHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public void setImageSrc(byte[] image) {
        this.imageSrc = image;
    }

    public void setTemperatureSrc(byte[] temperatureSrc) {
        this.temperatureSrc = temperatureSrc;
    }

    public void setFrameReady(boolean frameReady) {
        isFrameReady = frameReady;
    }

    private boolean isIRpid(int devpid) {
        for (int x : pids) {
            if (x == devpid) return true;
        }
        return false;
    }

    public void init(int cameraWidth, int cameraHeight) {
        Log.w(TAG, "init");
        
        ConcreateUVCBuilder concreateUVCBuilder = new ConcreateUVCBuilder();
        uvcCamera = concreateUVCBuilder
                .setUVCType(UVCType.USB_UVC)
                .setOutputWidth(cameraWidth)
                .setOutputHeight(cameraHeight)
                .build();
        ConcreteIRCMDBuilder concreteIRCMDBuilder = new ConcreteIRCMDBuilder();
        ircmd = concreteIRCMDBuilder
                .setIrcmdType(IRCMDType.USB_IR_256_384)
                .setIdCamera(uvcCamera.getNativePtr())
                .build();
    }

    public UVCCamera getUvcCamera() {
        return uvcCamera;
    }

    public IRCMD getIrcmd() {
        return ircmd;
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

    public List<UsbDevice> getUsbDeviceList() {
        List<DeviceFilter> deviceFilters = DeviceFilter.getDeviceFilters(mContext, R.xml.device_filter);
        if (mUSBMonitor == null || deviceFilters == null)
            return null;
        return mUSBMonitor.getDeviceList(deviceFilters);
    }

    public void requestPermission(int index) {
        List<UsbDevice> devList = getUsbDeviceList();
        if (devList == null || devList.size() == 0) {
            return;
        }
        int count = devList.size();
        if (index >= count)
            new IllegalArgumentException("index illegal,should be < devList.size()");
        if (mUSBMonitor != null) {
            mUSBMonitor.requestPermission(getUsbDeviceList().get(index));
        }
    }

    public void openUVCCamera(USBMonitor.UsbControlBlock ctrlBlock, CommonParams.DataFlowMode dataFlowMode) {
        Log.i(TAG, "openUVCCamera");
        if (ctrlBlock.getProductId() == TinyB) {
            if (syncimage != null) {
                syncimage.type = 1;
            }
        }
        if (uvcCamera == null) {
            init(cameraWidth, cameraHeight);
        }
        int DEFAULT_PREVIEW_MIN_FPS = 1;
        int DEFAULT_PREVIEW_MAX_FPS = 50;
        uvcCamera.openUVCCamera(ctrlBlock, DEFAULT_PREVIEW_MIN_FPS, DEFAULT_PREVIEW_MAX_FPS);
    }

    public void startPreview() {
        Log.i(TAG, "startPreview");
       if (uvcCamera!=null){
           uvcCamera.setOpenStatus(true);
           uvcCamera.setFrameCallback(iFrameCallback);
           if (isUseIRISP) {
               initIRISP();
           } else {
               uvcCamera.onStartPreview();
           }
       }
    }

    public void stopPreview() {
        Log.i(TAG, "stopPreview");
        if (uvcCamera != null) {
            if (uvcCamera.getOpenStatus()) {
                uvcCamera.onStopPreview();
            }
            uvcCamera.setFrameCallback(null);
            final UVCCamera camera;
            camera = uvcCamera;
            uvcCamera = null;
            SystemClock.sleep(200);
            camera.onDestroyPreview();
        }
    }

    private void initIRISP() {
        ircmd.getCurrentVTemperature(curVtemp);
        uvcCamera.setCurVTemp(curVtemp[0]);
        Log.i(TAG, "ktbt_init->CurVTemp=" + curVtemp[0]);

        uvcCamera.initIRISPModule();
        uvcCamera.setEnvCorrectParams(16384, 16384, 300 * 16, 300 * 16);

        uvcCamera.setGainStatus(gainStatus);

        uvcCamera.onStartPreview();
    }

    public void requestPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(activity, "申请权限", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    public void getIRISPfParamData() {
        boolean isUseSaveData = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                AssetManager am = mContext.getAssets();
                InputStream is = null;
                try {
                    if (isUseSaveData) {
                        is = am.open("priv_high.bin");
                        int lenth_priv = is.available();
                        priv_high = new byte[lenth_priv];
                        if (is.read(priv_high) != lenth_priv) {
                            Log.d(TAG, "read priv file fail ");
                        }
                        Log.d(TAG, "read priv file lenth " + lenth_priv);
                    } else {
                        ircmd.readPrivData(gainMode, priv_high, priv_low);
                        BitmapUtils.saveByteFile(priv_high, "priv_high");
                        BitmapUtils.saveByteFile(priv_low, "priv_low");
                    }
                    for (int i = 0; i < 6; i++) {
                        Log.i(TAG, "ktbt_init->priv_data[" + i + "]=" + priv_high[i]);
                    }

                    if (isUseSaveData) {
                        is = am.open("kt_high.bin");
                        int lenthKt = is.available();
                        byte kt_high_byte[] = new byte[lenthKt];
                        if (is.read(kt_high_byte) != lenthKt) {
                            Log.d(TAG, "read kt file fail ");
                        }
                        Log.d(TAG, "read kt file lenth " + lenthKt);
                        kt_high = BitmapUtils.toShortArray(kt_high_byte);
                    } else {
                        ircmd.readKTData(gainMode, kt_high, kt_low);
                        BitmapUtils.saveShortFile(kt_high, "kt_high");
                        BitmapUtils.saveShortFile(kt_low, "kt_low");
                    }
                    for (int i = 0; i < 100; i++) {
                        Log.i(TAG, "ktbt_init->kt_data[" + i + "]=" + kt_high[i]);
                    }

                    if (isUseSaveData) {
                        is = am.open("bt_high.bin");
                        int lenthBt = is.available();
                        byte bt_high_byte[] = new byte[lenthBt];
                        if (is.read(bt_high_byte) != lenthBt) {
                            Log.d(TAG, "read file fail ");
                        }
                        Log.d(TAG, "read bt file lenth " + lenthBt);
                        bt_high = BitmapUtils.toShortArray(bt_high_byte);

                        for (int i = 0; i < 100; i++) {
                            Log.i(TAG, "ktbt_init->bt_data[" + i + "]=" + bt_high_byte[i]);
                        }
                    } else {
                        ircmd.readBTData(gainMode, bt_high, bt_low);
                        BitmapUtils.saveShortFile(bt_high, "bt_high");
                        BitmapUtils.saveShortFile(bt_low, "bt_low");
                    }

                    if (isUseSaveData) {
                        is = am.open("nuc_table_high.bin");
                        int lenthNuc = is.available();
                        byte nuc_table_high_byte[] = new byte[lenthNuc];
                        if (is.read(nuc_table_high_byte) != lenthNuc) {
                            Log.d(TAG, "read nuc_table file fail ");
                        }
                        Log.d(TAG, "read nuc_table file lenth " + lenthNuc);
                        nuc_table_high = BitmapUtils.toShortArray(nuc_table_high_byte);
                    } else {
                        int[] valueGain = new int[1];
                        ircmd.getPropTPDParams(CommonParams.PropTPDParams.TPD_PROP_GAIN_SEL, valueGain);
                        Log.i(TAG, "TPD_PROP_GAIN_SEL=" + valueGain[0]);

                        if (valueGain[0] == 1) {
                            gainStatus = CommonParams.GainStatus.HIGH_GAIN;
                        } else {
                            gainStatus = CommonParams.GainStatus.LOW_GAIN;
                        }
                        ircmd.readNucTableFromFlash(gainMode, nuc_table_high, nuc_table_low);
                        for (int i = 4000; i < 5000; i += 100) {
                            Log.i(TAG, "ktbt_init->nuc_table_high[" + i + "]=" + nuc_table_high[i]);
                        }
                        for (int i = 4000; i < 5000; i += 100) {
                            Log.i(TAG, "ktbt_init->nuc_table_low[" + i + "]=" + nuc_table_low[i]);
                        }
                        BitmapUtils.saveShortFile(nuc_table_high, "nuc_table_high");
                        BitmapUtils.saveShortFile(nuc_table_low, "nuc_table_low");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (uvcCamera != null) {
                    uvcCamera.setTempCorrectParams(priv_high, priv_low, kt_high, kt_low, bt_high, bt_low, nuc_table_high, nuc_table_low);
                }
            }
        }).start();
    }

}
