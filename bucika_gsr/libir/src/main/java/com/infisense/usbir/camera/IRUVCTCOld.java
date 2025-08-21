//package com.infisense.usbir.camera;
//
//import android.content.Context;
//import android.hardware.usb.UsbDevice;
//import android.os.SystemClock;
//import android.util.Log;
//
//import com.elvishew.xlog.XLog;
//import com.infisense.iruvc.sdkisp.LibIRProcess;
//import com.infisense.iruvc.sdkisp.Libircmd;
//import com.infisense.iruvc.sdkisp.Libirprocess;
//import com.infisense.iruvc.usb.DeviceFilter;
//import com.infisense.iruvc.usb.IFrameCallback;
//import com.infisense.iruvc.usb.USBMonitor;
//import com.infisense.iruvc.usb.UVCCamera;
//import com.infisense.iruvc.utils.CommonParams;
//import com.infisense.iruvc.utils.SynchronizedBitmap;
//import com.infisense.iruvc.uvc.ConnectCallback;
//import com.infisense.usbir.R;
//import com.infisense.usbir.config.MsgCode;
//import com.infisense.usbir.event.IRMsgEvent;
//import com.infisense.usbir.utils.USBMonitorCallback;
//import com.topdon.lib.core.bean.event.device.DeviceCameraEvent;
//import com.topdon.lib.core.bean.event.device.ResetConnectEvent;
//
//import org.greenrobot.eventbus.EventBus;
//
//import java.util.List;
//
///**
// * device -> bytes
// * 红外出图核心工具类
// */
//public class IRUVCTC {
//
//    private static final String TAG = "IRUVC";
//    private final int TinyB = 0x3901;
//    private final IFrameCallback iFrameCallback;
//    private final Context context;
//    public UVCCamera uvcCamera;
//    private USBMonitor mUSBMonitor;
//    private int cameraWidth;
//    private int cameraHeight;
//    private byte[] image;
//    private byte[] temperature;
//    private SynchronizedBitmap syncimage;
//    // 设备PID白名单
//    private int pids[] = {0x5840, 0x3901, 0x5830, 0x5838};
//    public boolean auto_gain_switch = false;
//    private boolean auto_over_portect = false;
//    /**
//     * 自动增益切换
//     */
//    private LibIRProcess.AutoGainSwitchInfo_t auto_gain_switch_info = new LibIRProcess.AutoGainSwitchInfo_t();
//    private LibIRProcess.GainSwitchParam_t gain_switch_param = new LibIRProcess.GainSwitchParam_t();
//    private int count = 0;
//    private int rotate = 0;
//    long timeLog = 0L;//记录时间
//
//    private byte[] imageTemp = null;
//    private byte[] temperatureTemp = null;
//    private int countTemp = 0;
//    public byte[] imageEditTemp = null;
//    Long updateTime = 0L;
//
//    /**
//     * @param cameraHeight
//     * @param cameraWidth
//     * @param context
//     * @param syncimage
//     */
//    public IRUVCTC(int cameraHeight, int cameraWidth, Context context, SynchronizedBitmap syncimage,
//                   CommonParams.DataFlowMode dataFlowMode, boolean isUseIRISP, boolean isUseGPU,
//                   ConnectCallback connectCallback, USBMonitorCallback usbMonitorCallback) {
//        this.mContext = context;
//        this.syncimage = syncimage;
//        this.isUseIRISP = isUseIRISP;
//        this.isUseGPU = isUseGPU;
//        this.mConnectCallback = connectCallback;
//        this.defaultDataFlowMode = dataFlowMode;
//        init(cameraHeight, cameraWidth, context);
//
//
//        // 注意：USBMonitor的所有回调函数都是运行在线程中的
//        mUSBMonitor = new USBMonitor(context, new USBMonitor.OnDeviceConnectListener() {
//
//            // called by checking usb device
//            // do request device permission
//            @Override
//            public void onAttach(UsbDevice device) {
//                XLog.tag(TAG).w("onAttach");
//                if (isIRpid(device.getProductId())) {
//                    if (uvcCamera == null || !uvcCamera.getOpenStatus()) {
//                        mUSBMonitor.requestPermission(device);
//                    }
//                }
//            }
//
//            @Override
//            public void onGranted(UsbDevice usbDevice, boolean b) {
//
//            }
//
//            // called by connect to usb camera
//            // do open camera,start previewing
//            @Override
//            public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
//                XLog.tag(TAG).w("onConnect");
//                if (isIRpid(device.getProductId())) {
//                    if (createNew) {
//                        open(ctrlBlock);
//                        start();
//                    }
//                }
//                EventBus.getDefault().post(new ResetConnectEvent(1));
//            }
//
//            // called by disconnect to usb camera
//            // do nothing
//            @Override
//            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
//                XLog.tag(TAG).w("onDisconnect");
//            }
//
//            // called by taking out usb device
//            // do close camera
//            @Override
//            public void onDettach(UsbDevice device) {
//                XLog.tag(TAG).w("onDetach");
//                if (isIRpid(device.getProductId())) {
//                    if (uvcCamera != null && uvcCamera.getOpenStatus()) {
//                        stop();
//                    }
//                }
//            }
//
//            @Override
//            public void onCancel(UsbDevice device) {
//                //在usb permission获取无效时触发
//                XLog.tag(TAG).w("onCancel");
//
//            }
//        });
//        // auto gain switch parameter
//        gain_switch_param.above_pixel_prop = 0.1f;    //用于high -> low gain,设备像素总面积的百分比
//        gain_switch_param.above_temp_data = (int) ((130 + 273.15) * 16 * 4); //用于high -> low gain,高增益向低增益切换的触发温度
//        gain_switch_param.below_pixel_prop = 0.95f;   //用于low -> high gain,设备像素总面积的百分比
//        gain_switch_param.below_temp_data = (int) ((110 + 273.15) * 16 * 4);//用于low -> high gain,低增益向高增益切换的触发温度
//        auto_gain_switch_info.switch_frame_cnt = 5 * 15; //连续满足触发条件帧数超过该阈值会触发自动增益切换(假设出图速度为15帧每秒，则5 * 15大概为5秒)
//        auto_gain_switch_info.waiting_frame_cnt = 7 * 15;//触发自动增益切换之后，会间隔该阈值的帧数不进行增益切换监测(假设出图速度为15帧每秒，则7 * 15大概为7秒)
//        //over_portect parameter
//        int low_gain_over_temp_data = (int) ((550 + 273.15) * 16 * 4);
//        int high_gain_over_temp_data = (int) ((100 + 273.15) * 16 * 4);
//        float pixel_above_prop = 0.02f;         //0-1
//
//        // 监听读取设备红外数据
//        iFrameCallback = frame -> {
//            Log.d(TAG, "frame: " + "刷新："+(System.currentTimeMillis()-updateTime));
//            updateTime = System.currentTimeMillis();
//            // 测试帧率，可以根据实际需要决定是否保留
//            if (count++ >= 25) {
//                count = 1;
//                Log.d(TAG, "frame: " + frame.length);
//            }
//            if (syncimage == null) return;
//            syncimage.start = true;
//            synchronized (syncimage.dataLock) {
//                // 判断坏帧，出现坏帧则重启sensor
//                int length = frame.length - 1;
//                if (frame[length] == 1) {
//                    EventBus.getDefault().post(new IRMsgEvent(MsgCode.RESTART_USB));
//                    XLog.tag(TAG).i("RESTART_USB");
//                    return;
//                }
//                /**
//                 * copy红外数据到image数组中
//                 * 出图的frame数组中前半部分是红外数据，后半部分是温度数据，
//                 * 例如256*384分辨率的设备，前面的256*192是红外数据，后面的256*192是温度数据，
//                 * 其中的数据是旋转90度的，需要旋转回来。
//                 */
//                if (imageEditTemp != null && imageEditTemp.length >= length) {
//                    //部分场景不需要保存帧数据
//                    System.arraycopy(frame, 0, imageEditTemp, 0, length);
//                }
//                System.arraycopy(frame, 0, image, 0, length / 2);
//                Libirprocess.ImageRes_t imageRes = new Libirprocess.ImageRes_t();
//                imageRes.height = (char) (cameraHeight / 2);
//                imageRes.width = (char) cameraWidth;
////                Libirprocess.rotate_right_90(frame, imageRes, Libirprocess.IRPROC_SRC_FMT_Y14, imageEditTemp);
////                //获取原始温度数据
////                System.arraycopy(frame, length / 2, temperatureSrc, 0, length / 2);
//
////                //保存测试数据
////                countTemp++;
////                if (countTemp == 100) {
////                    imageTemp = new byte[length / 2];
////                    temperatureTemp = new byte[length / 2];
////
////                    System.arraycopy(frame, 0, imageTemp, 0, length / 2);
////                    XLog.tag("ahh").i("imageTemp: " + ByteUtils.INSTANCE.toHexString(imageTemp, " "));
////
////                    System.arraycopy(frame, length / 2, temperatureTemp, 0, length / 2);
////                    XLog.tag("ahh").i("temperatureTemp: " + ByteUtils.INSTANCE.toHexString(temperatureTemp, " "));
////                }
//
//                if (rotate == 270) {
//                    // 270
//                    byte[] temp = new byte[length / 2];
//                    System.arraycopy(frame, length / 2, temp, 0, length / 2);
//                    Libirprocess.rotate_right_90(temp, imageRes, Libirprocess.IRPROC_SRC_FMT_Y14, temperature);
//                } else if (rotate == 90) {
//                    // 90
//                    byte[] temp = new byte[length / 2];
//                    System.arraycopy(frame, length / 2, temp, 0, length / 2);
//                    Libirprocess.rotate_left_90(temp, imageRes, Libirprocess.IRPROC_SRC_FMT_Y14, temperature);
//                } else if (rotate == 180) {
//                    // 180
//                    byte[] temp = new byte[length / 2];
//                    System.arraycopy(frame, length / 2, temp, 0, length / 2);
//                    Libirprocess.rotate_180(temp, imageRes, Libirprocess.IRPROC_SRC_FMT_Y14, temperature);
//                } else {
//                    // 0
//                    System.arraycopy(frame, length / 2, temperature, 0, length / 2);
//                }
//                // 自动增益切换，不生效的话请您的设备是否支持自动增益切换
//                if (auto_gain_switch) {
//                    Libircmd.auto_gain_switch(temperature, imageRes, auto_gain_switch_info, gain_switch_param, uvcCamera.nativePtr);
//                }
//                // 防灼烧保护
//                if (auto_over_portect) {
//                    Libircmd.avoid_overexposure(temperature, imageRes, low_gain_over_temp_data,
//                            high_gain_over_temp_data, pixel_above_prop, 15 * 25, uvcCamera.nativePtr);
//                }
//            }
//        };
//    }
//
//    /**
//     * @param rotate
//     */
//    public void setRotate(int rotate) {
//        this.rotate = rotate;
//    }
//
//    /**
//     * @param image
//     */
//    public void setImage(byte[] image) {
//        this.image = image;
//    }
//
//    /**
//     * @param temperature
//     */
//    public void setTemperature(byte[] temperature) {
//        this.temperature = temperature;
//    }
//
//    public void setImageEditSrc(byte[] imageEditTemp) {
//        this.imageEditTemp = imageEditTemp;
//    }
//
//    /**
//     * 判断是否是红外设备，请把您的设备的PID添加进设备PID白名单
//     *
//     * @param devpid
//     * @return
//     */
//    private boolean isIRpid(int devpid) {
//        for (int x : pids) {
//            if (x == devpid) return true;
//        }
//        return false;
//    }
//
//    /**
//     * @param cameraHeight
//     * @param cameraWidth
//     * @param context
//     */
//    public void init(int cameraHeight, int cameraWidth, Context context) {
//        XLog.tag(TAG).w("init");
//        uvcCamera = new UVCCamera(cameraWidth, cameraHeight, context);
//        uvcCamera.create();
//        EventBus.getDefault().post(new DeviceCameraEvent(100));
//    }
//
//    /**
//     *
//     */
//    public void registerUSB() {
//        if (mUSBMonitor != null) {
//            mUSBMonitor.register();
//        }
//    }
//
//    /**
//     *
//     */
//    public void unregisterUSB() {
//        if (mUSBMonitor != null) {
//            mUSBMonitor.unregister();
//        }
//    }
//
//    /**
//     * @return
//     */
//    public List<UsbDevice> getUsbDeviceList() {
////        List<DeviceFilter> deviceFiltersTemp = DeviceFilter.getDeviceFilters(context, R.xml.device_filter);
//        List<DeviceFilter> deviceFilters = DeviceFilter.getDeviceFilters(context, R.xml.ir_device_filter);
//        if (mUSBMonitor == null || deviceFilters == null)
////            throw new NullPointerException("mUSBMonitor ="+mUSBMonitor+"deviceFilters=;"+deviceFilters);
//            return null;
//        // matching all of filter devices
//        return mUSBMonitor.getDeviceList(deviceFilters);
//    }
//
//    /**
//     * @param index
//     */
//    public void requestPermission(int index) {
//        List<UsbDevice> devList = getUsbDeviceList();
//        if (devList == null || devList.size() == 0) {
//            return;
//        }
//        int count = devList.size();
//        if (index >= count)
//            new IllegalArgumentException("index illegal,should be < devList.size()");
//        if (mUSBMonitor != null) {
//            mUSBMonitor.requestPermission(getUsbDeviceList().get(index));
//        }
//    }
//
//    /**
//     * @param ctrlBlock
//     */
//    public void open(USBMonitor.UsbControlBlock ctrlBlock) {
//        if (ctrlBlock.getProductId() == TinyB) {
//            if (syncimage != null) {
//                syncimage.type = 1;
//            }
//        }
//        if (uvcCamera == null) {
//            init(cameraHeight, cameraWidth, context);
//        }
//        uvcCamera.open(ctrlBlock);
//    }
//
//    /**
//     *
//     */
//    public void start() {
//        try {
//            XLog.tag(TAG).w("start");
//            uvcCamera.setOpenStatus(true);
//            uvcCamera.setFrameCallback(iFrameCallback); //注册监听事件
//            //uvcCamera.setgetframemode(uvcCamera.GET_FRAME_ASYNC);
//            //default sync mode for some devices  Lost-Packet
//            //uvcCamera.DEFAULT_BANDWIDTH=0.3f;//hub
//            uvcCamera.startPreview(); //开始读取数据
//            new Thread(() -> {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                EventBus.getDefault().post(new DeviceCameraEvent(101));
//                //打快门
//                if (uvcCamera != null) {
//                    if (syncimage.type == 1) {
//                        Libircmd.tiny1b_shutter_manual(uvcCamera.nativePtr);
//                    } else {
//                        //源码设置快门
//                        Libircmd.ooc_b_update(Libircmd.B_UPDATE, uvcCamera.nativePtr);
//                    }
//                }
//            }).start();
//        }catch (Exception e){
//            Log.w("红外sdk异常", e.getMessage());
//        }
//
//    }
//
//    /**
//     *
//     */
//    public void stop() {
//        XLog.tag(TAG).w("stop");
////        if (uvcCamera != null) {
////            if (uvcCamera.getOpenStatus()) {
////                uvcCamera.stopPreview();
////            }
////            final UVCCamera camera;
////            camera = uvcCamera;
////            uvcCamera = null;
////            SystemClock.sleep(200);
////            camera.destroy();
////            EventBus.getDefault().post(new ResetConnectEvent(3));
////        }
//    }
//
////    Disposable disposable = null;
////    private boolean isRun = false;
////
////    private void monitor() {
////        if (disposable != null) {
////            disposable.dispose();
////        }
////        disposable = Observable.interval(1L, TimeUnit.SECONDS).take(1000)
////                .subscribeOn(Schedulers.io())
////                .subscribe(aLong -> {
////                    Log.w("123", "aLong" + aLong);
//////                    if (isRun) {
//////                        if (timeLog != 0 && System.currentTimeMillis() - timeLog > 1000) {
//////                            //通知超时
//////                            EventBus.getDefault().post(new DeviceConnectEvent(false, null));
//////                            XLog.w("超过1s没数据采集,退出界面");
////////                ToastTools.INSTANCE.showShort("超过1s没数据采集,退出界面");
//////                        }
//////                        timeLog = System.currentTimeMillis();
//////                    }
////                });
////        Log.w("123", "Observable.timer");
////    }
////
////    private void cancelMonitor() {
////        isRun = false;
////        if (disposable != null) {
////            disposable.dispose();
////        }
////    }
//
//}
