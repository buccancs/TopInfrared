package com.infisense.usbir.camera;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.os.SystemClock;
import android.util.Log;

import com.energy.iruvc.ircmd.ConcreteIRCMDBuilder;
import com.energy.iruvc.ircmd.IRCMD;
import com.energy.iruvc.ircmd.IRCMDType;
import com.energy.iruvc.sdkisp.LibIRProcess;
import com.energy.iruvc.usb.USBMonitor;
import com.energy.iruvc.utils.AutoGainSwitchCallback;
import com.energy.iruvc.utils.AvoidOverexposureCallback;
import com.energy.iruvc.utils.CommonParams;
import com.energy.iruvc.utils.DeviceType;
import com.energy.iruvc.utils.IFrameCallback;
import com.energy.iruvc.utils.SynchronizedBitmap;
import com.energy.iruvc.uvc.CameraSize;
import com.energy.iruvc.uvc.ConcreateUVCBuilder;
import com.energy.iruvc.uvc.ConnectCallback;
import com.energy.iruvc.uvc.UVCCamera;
import com.energy.iruvc.uvc.UVCType;
import com.infisense.usbir.config.MsgCode;
import com.infisense.usbir.event.IRMsgEvent;
import com.infisense.usbir.event.PreviewComplete;
import com.infisense.usbir.utils.FileUtil;
import com.infisense.usbir.utils.ScreenUtils;
import com.infisense.usbir.utils.USBMonitorCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * 红外出图核心工具类
 */
public class IRUVCTC {
    private static final String TAG = "IRUVC_DATA";
    private final IFrameCallback iFrameCallback;
    public UVCCamera uvcCamera;
    private IRCMD ircmd;
    //
    private final USBMonitor mUSBMonitor;
    private final ConnectCallback mConnectCallback; // usb连接回调
    private byte[] imageSrc;
    private byte[] temperatureSrc;
    private final int imageOrTempDataLength = 256 * 192 * 2; // 红外或温度的数据长度
    private final SynchronizedBitmap syncimage;
    /**
     * 自动增益切换
     */
    private final LibIRProcess.AutoGainSwitchInfo_t auto_gain_switch_info = new LibIRProcess.AutoGainSwitchInfo_t();
    private final LibIRProcess.GainSwitchParam_t gain_switch_param = new LibIRProcess.GainSwitchParam_t();
    private int rotateInt = 0;

    // 判断数据是否准备完毕，在准备完毕之前，画面可能会出现不正常
    private boolean isFrameReady = true;
    // 当前的增益状态
    private final CommonParams.GainStatus gainStatus = CommonParams.GainStatus.HIGH_GAIN;
    private final byte[] temperatureTemp = new byte[imageOrTempDataLength];
    // 是否可以红外+TNR出图
    private boolean isTempReplacedWithTNREnabled;
    private final CommonParams.DataFlowMode defaultDataFlowMode;
    private boolean isRestart;
    public boolean auto_gain_switch = false;
    private final boolean auto_over_portect = false;
    public byte[] imageEditTemp = null;
    private int pids[] = {0x5840, 0x3901, 0x5830, 0x5838};
    private IFrameCallBackListener iFrameCallBackListener;

    private IFrameReadListener iFrameReadListener;
    public volatile boolean isFirstFrame;

    public void setIFrameCallBackListener(IFrameCallBackListener iFrameCallBackListener) {
        this.iFrameCallBackListener = iFrameCallBackListener;
    }

    public void setiFirstFrameListener(IFrameReadListener iFrameReadListener) {
        this.iFrameReadListener = iFrameReadListener;
    }

    public interface IFrameCallBackListener {
        void updateData();
    }

    public interface IFrameReadListener {
        void frameRead();
    }

    /**
     * @param cameraWidth     cameraWidth:256,cameraHeight:384,图像+温度
     *                        cameraWidth:256,cameraHeight:192,图像
     *                        cameraWidth:256,cameraHeight:192,(调用startY16ModePreview，传入Y16_MODE_TEMPERATURE)温度
     * @param connectCallback 设置usb设备连接回调
     */
    public IRUVCTC(int cameraWidth, int cameraHeight, Context context, SynchronizedBitmap syncimage,
                   CommonParams.DataFlowMode dataFlowMode,
                   ConnectCallback connectCallback, USBMonitorCallback usbMonitorCallback) {
        this.syncimage = syncimage;
        this.mConnectCallback = connectCallback;
        this.defaultDataFlowMode = dataFlowMode;
        isFirstFrame = true;

        //
        initUVCCamera();
        // 注意：USBMonitor的所有回调函数都是运行在线程中的
        mUSBMonitor = new USBMonitor(context, new USBMonitor.OnDeviceConnectListener() {

            // called by checking usb device
            // do request device permission
            @Override
            public void onAttach(UsbDevice device) {
                Log.w(TAG, "onAttach");
                if (uvcCamera == null || !uvcCamera.getOpenStatus()) {
                    mUSBMonitor.requestPermission(device);
                }
                if (usbMonitorCallback != null) {
                    usbMonitorCallback.onAttach();
                }
            }

            @Override
            public void onGranted(UsbDevice usbDevice, boolean granted) {
                Log.w(TAG, "onGranted");
                if (usbMonitorCallback != null) {
                    usbMonitorCallback.onGranted();
                }
            }

            // called by connect to usb camera
            // do open camera,start previewing
            @Override
            public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                Log.w(TAG, "onConnect");
                if (isIRpid(device.getProductId())){
                    if (createNew) {
                        openUVCCamera(ctrlBlock);

                        // 获取设备的分辨率列表
                        List<CameraSize> previewList = getAllSupportedSize();
                        for (CameraSize size : previewList) {
                            Log.i(TAG, "SupportedSize : " + size.width + " * " + size.height);
                        }

                        // 可以根据获取到的分辨率列表，来区分不同的模组，从而改变不同的cmd参数来调用不同的SDK
                        initIRCMD();

                        if (ircmd != null) {
                            Log.d(TAG, "startPreview");
                            // 根据设备的分辨率列表，这里可以动态的设置模组的宽高(这里作为示例，用的是从外部传入的方式)
                            // 之前的openUVCCamera方法中传入的都是默认值，这里需要根据实际传入对应的值
                            isTempReplacedWithTNREnabled = ircmd.isTempReplacedWithTNREnabled(DeviceType.P2);
                            if (isTempReplacedWithTNREnabled) {
                                // 使用红外+TNR数据的方式，不用进行停图重新出图的流程，方便快速出图
                                if (uvcCamera != null) {
                                    uvcCamera.setUSBPreviewSize(cameraWidth, cameraHeight * 2);
                                }
                            } else {
                                // 单TNR数据
                                if (uvcCamera != null) {
                                    uvcCamera.setUSBPreviewSize(cameraWidth, cameraHeight);
                                }
                            }
                            startPreview();
                        }

                        if (usbMonitorCallback != null) {
                            usbMonitorCallback.onConnect();
                        }
                    }
                }
            }

            // called by disconnect to usb camera
            // do nothing
            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                Log.w(TAG, "onDisconnect");
                if (usbMonitorCallback != null) {
                    usbMonitorCallback.onDisconnect();
                }
            }

            // called by taking out usb device
            // do close camera
            @Override
            public void onDettach(UsbDevice device) {
                Log.w(TAG, "onDettach");
                if (uvcCamera != null && uvcCamera.getOpenStatus()) {
                    if (usbMonitorCallback != null) {
                        usbMonitorCallback.onDettach();
                    }
                }
            }

            @Override
            public void onCancel(UsbDevice device) {
                Log.w(TAG, "onCancel");
                if (usbMonitorCallback != null) {
                    usbMonitorCallback.onCancel();
                }
            }
        });
        /*
         * 同时打开防灼烧和自动增益切换后，如果想修改防灼烧和自动增益切换的触发优先级，可以通过修改下面的触发参数实现
         */
        // 自动增益切换参数auto gain switch parameter
        gain_switch_param.above_pixel_prop = 0.1f;    //用于high -> low gain,设备像素总面积的百分比
        gain_switch_param.above_temp_data = (int) ((130 + 273.15) * 16 * 4); //用于high -> low gain,高增益向低增益切换的触发温度
        gain_switch_param.below_pixel_prop = 0.95f;   //用于low -> high gain,设备像素总面积的百分比
        gain_switch_param.below_temp_data = (int) ((110 + 273.15) * 16 * 4);//用于low -> high gain,低增益向高增益切换的触发温度
        auto_gain_switch_info.switch_frame_cnt = 5 * 15; //连续满足触发条件帧数超过该阈值会触发自动增益切换(假设出图速度为15帧每秒，则5 * 15大概为5秒)
        auto_gain_switch_info.waiting_frame_cnt = 7 * 15;//触发自动增益切换之后，会间隔该阈值的帧数不进行增益切换监测(假设出图速度为15帧每秒，则7 * 15大概为7秒)
        // 防灼烧参数over_portect parameter
        int low_gain_over_temp_data = (int) ((550 + 273.15) * 16 * 4); //低增益下触发防灼烧的温度
        int high_gain_over_temp_data = (int) ((150 + 273.15) * 16 * 4); //高增益下触发防灼烧的温度
        float pixel_above_prop = 0.02f;//设备像素总面积的百分比
        int switch_frame_cnt = 7 * 15;//连续满足触发条件超过该阈值会触发防灼烧(假设出图速度为15帧每秒，则7 * 15大概为7秒)
        int close_frame_cnt = 10 * 15;//触发防灼烧之后，经过该阈值的帧数之后会解除防灼烧(假设出图速度为15帧每秒，则10 * 15大概为10秒)


        LibIRProcess.ImageRes_t imageRes = new LibIRProcess.ImageRes_t();
        imageRes.height = (char) (dataFlowMode == CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT ? cameraHeight / 2
                : cameraHeight);
        imageRes.width = (char) cameraWidth;

        // 设备出图回调
        iFrameCallback = new IFrameCallback() {
            @Override
            public void onFrame(byte[] frame) {
                if (!isFrameReady) {
                    return;
                }
                if (syncimage == null) {
                    return;
                }
                syncimage.start = true;
                //
                synchronized (syncimage.dataLock) {
                    // 判断坏帧，出现坏帧则重启sensor
                    int length = frame.length - 1;
                    if (frame[length] == 1) {
                        // bad frame
                        EventBus.getDefault().post(new IRMsgEvent(MsgCode.RESTART_USB));
                        return;
                    }
                    if (imageEditTemp != null && imageEditTemp.length >= length) {
                        //部分场景不需要保存帧数据
                        System.arraycopy(frame, 0, imageEditTemp, 0, length);
                    }
//                    try {
//                        byte[] tmpBy = new byte[256*192*2];
//                        System.arraycopy(frame, imageOrTempDataLength, tmpBy, 0,
//                                imageOrTempDataLength);
//                        LibIRTemp tmp = new LibIRTemp(256,192);
//                        tmp.setTempData(tmpBy);
//                        LibIRTemp.TemperatureSampleResult result = tmp.getTemperatureOfRect(new Rect(0, 0, 256,192));
//                        Log.w("温度更新3",result.maxTemperature+"///"+result.minTemperature);
//                    }catch (Exception  e){
//
//                    }
                    if (dataFlowMode == CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT) {
                        /*
                         * 图像+温度
                         * copy红外数据到image数组中
                         * 出图的frame数组中前半部分是红外数据，后半部分是温度数据，
                         * 例如256*384分辨率的设备，前面的256*192是红外数据，后面的256*192是温度数据，
                         * 其中的数据是旋转90度的，需要旋转回来,红外旋转的逻辑放在后面ImageThread中处理。
                         */
                        System.arraycopy(frame, 0, imageSrc, 0, imageOrTempDataLength);
                        /*
                         * 处理温度数据
                         * 在部分的出图中，如果不需要温度数据，则不返回，需要区分对待
                         */
                        if (length >= imageOrTempDataLength * 2) {

                            if (rotateInt == 270) {
                                // 270
                                System.arraycopy(frame, imageOrTempDataLength, temperatureTemp, 0,
                                        imageOrTempDataLength);
                                LibIRProcess.rotateRight90(temperatureTemp, imageRes,
                                        CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, temperatureSrc);
                            } else if (rotateInt == 90) {
                                // 90
                                System.arraycopy(frame, imageOrTempDataLength, temperatureTemp, 0,
                                        imageOrTempDataLength);
                                LibIRProcess.rotateLeft90(temperatureTemp, imageRes,
                                        CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, temperatureSrc);
                            } else if (rotateInt == 180) {
                                // 180
                                System.arraycopy(frame, imageOrTempDataLength, temperatureTemp, 0,
                                        imageOrTempDataLength);
                                LibIRProcess.rotate180(temperatureTemp, imageRes,
                                        CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_Y14, temperatureSrc);
                            } else {
                                // 0
                                System.arraycopy(frame, imageOrTempDataLength, temperatureSrc, 0,
                                        imageOrTempDataLength);
//                                System.arraycopy(frame, length / 2, temperatureSrc, 0, length / 2);
                            }
                            if (ircmd != null) {
                                // 自动增益切换，不生效的话请您的设备是否支持自动增益切换
                                if (auto_gain_switch) {
                                    ircmd.autoGainSwitch(temperatureSrc, imageRes, auto_gain_switch_info,
                                            gain_switch_param, new AutoGainSwitchCallback() {
                                                @Override
                                                public void onAutoGainSwitchState(CommonParams.PropTPDParamsValue.GAINSELStatus gainselStatus) {
                                                    Log.i(TAG, "onAutoGainSwitchState->" + gainselStatus.getValue());
                                                }

                                                @Override
                                                public void onAutoGainSwitchResult(CommonParams.PropTPDParamsValue.GAINSELStatus gainselStatus, int result) {
                                                    Log.i(TAG,
                                                            "onAutoGainSwitchResult->" + gainselStatus.getValue() +
                                                                    " result=" + result);
                                                }
                                            });
                                }
                                // 防灼烧保护
                                if (auto_over_portect) {
                                    ircmd.avoidOverexposure(false, gainStatus, temperatureSrc, imageRes,
                                            low_gain_over_temp_data,
                                            high_gain_over_temp_data, pixel_above_prop, switch_frame_cnt,
                                            close_frame_cnt,
                                            new AvoidOverexposureCallback() {
                                                @Override
                                                public void onAvoidOverexposureState(boolean avoidOverexpol) {
                                                    Log.i(TAG,
                                                            "onAvoidOverexposureState->avoidOverexpol=" + avoidOverexpol);
                                                }
                                            });
                                }
                            }
                        }
                    } else {
                        /*
                         * 单红外数据
                         * copy红外数据到image数组中
                         * 其中的数据是旋转90度的，需要旋转回来,红外旋转的逻辑放在后面ImageThread中处理。
                         */
                        System.arraycopy(frame, 0, imageSrc, 0, imageOrTempDataLength);
                    }
                    if (iFrameCallBackListener != null) {
                        iFrameCallBackListener.updateData();
                    }
                }
                if (isFirstFrame && iFrameReadListener != null) {
                    iFrameReadListener.frameRead();
                    isFirstFrame = false;
                }
            }

        };
    }

    public void setRotate(int rotateInt) {
        this.rotateInt = rotateInt;
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

    public boolean isRestart() {
        return isRestart;
    }

    public void setRestart(boolean restart) {
        isRestart = restart;
    }

    /**
     * init UVCCamera
     */
    private void initUVCCamera() {
        Log.i(TAG, "uvcCamera create");
        uvcCamera = new ConcreateUVCBuilder()
                .setUVCType(UVCType.USB_UVC)
                .build();
        /**
         * 调整带宽
         * 部分分辨率或在部分机型上，会出现无法出图，或出图一段时间后卡顿的问题，需要配置对应的带宽
         */
        uvcCamera.setDefaultBandwidth(0.5F);
    }

    /**
     * init IRCMD
     * 可以根据获取到的分辨率列表，来区分不同的模组，从而改变不同的cmd参数来调用不同的SDK
     */
    private void initIRCMD() {
        if (uvcCamera != null) {
            ircmd = new ConcreteIRCMDBuilder()
                    .setIrcmdType(IRCMDType.USB_IR_256_384)
                    .setIdCamera(uvcCamera.getNativePtr())
                    .build();
            //这里可根据是否得到ircmd的对象，判断是否初始化成功，初始化失败，可做相应的失败错误提示
            //错误信息可以通过setCreateResultCallback的回调查看
            if (ircmd == null) {
                EventBus.getDefault().post(new PreviewComplete());
                return;
            }
            if (mConnectCallback != null) {
                mConnectCallback.onIRCMDCreate(ircmd);
            }
        }
    }

    /**
     *
     */
    public void registerUSB() {
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    /**
     *
     */
    public void unregisterUSB() {
        if (mUSBMonitor != null) {
            mUSBMonitor.unregister();
        }
    }

    private void openUVCCamera(USBMonitor.UsbControlBlock ctrlBlock) {
        Log.i(TAG, "openUVCCamera");
        if (ctrlBlock.getProductId() == 0x3901) {
            if (syncimage != null) {
                syncimage.type = 1;
            }
        }
        if (uvcCamera == null) {
            initUVCCamera();
        }
        // uvc开启
        if (uvcCamera.openUVCCamera(ctrlBlock) == 0) {
            // UVCCamera开启成功
            if (mConnectCallback != null && uvcCamera != null) {
                mConnectCallback.onCameraOpened(uvcCamera);
            }
        }
    }

    /**
     * 获取支持的分辨率列表
     */
    private List<CameraSize> getAllSupportedSize() {
        List<CameraSize> previewList = new ArrayList<>();
        if (uvcCamera != null) {
            Log.w(TAG, "getSupportedSize = " + uvcCamera.getSupportedSize());
            previewList = uvcCamera.getSupportedSizeList();
        }
        Log.w(TAG, "getSupportedSize = " + uvcCamera.getSupportedSize());
        for (CameraSize size : previewList) {
            Log.i(TAG, "SupportedSize : " + size.width + " * " + size.height);
        }
        return previewList;
    }

    /**
     * 判断是否是红外设备，请把您的设备的PID添加进设备PID白名单
     *
     * @param devpid
     * @return
     */
    private boolean isIRpid(int devpid) {
        for (int x : pids) {
            if (x == devpid) return true;
        }
        return false;
    }

    /**
     * 预览出图
     */
    private void startPreview() {
        if (ircmd == null) {
            return;
        }
        Log.i(TAG, "startPreview isRestart : " + isRestart + " defaultDataFlowMode : " + defaultDataFlowMode);
        uvcCamera.setOpenStatus(true);
        uvcCamera.setFrameCallback(iFrameCallback);
        uvcCamera.onStartPreview();

        if (CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT == defaultDataFlowMode ||
                CommonParams.DataFlowMode.IMAGE_OUTPUT == defaultDataFlowMode) {
            /*
             * 红外+温度或单红外出图
             * YUV422格式数据
             */
            Log.i(TAG, "defaultDataFlowMode = IMAGE_AND_TEMP_OUTPUT or IMAGE_OUTPUT");
            // YUV出图流程
            setFrameReady(false);
            if (isRestart) {
                // 1.停图（全部停图，不是退出y16模式的停图）
                if (ircmd.stopPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0) == 0) {
                    Log.i(TAG, "stopPreview complete");
                    // 2. 发出图命令，设置分辨率为256*384
                    if (ircmd.startPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                            CommonParams.StartPreviewSource.SOURCE_SENSOR,
                            ScreenUtils.getPreviewFPSByDataFlowMode(defaultDataFlowMode),
                            CommonParams.StartPreviewMode.VOC_DVP_MODE,
                            defaultDataFlowMode) == 0) {
                        Log.i(TAG, "startPreview complete");
                        handleStartPreviewComplete();
                    }
                } else {
                    Log.e(TAG, "stopPreview error");
                }
            } else {
                handleStartPreviewComplete();
            }
        } else {
            /*
             * 中间出图
             */
            // Y16出图流程(例如TNR出图，使用ISP算法)
            setFrameReady(false);
            if (isRestart) {
                if (ircmd.stopPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0) == 0) {
                    Log.i(TAG, "stopPreview complete 中间出图 restart");
                    if (ircmd.startPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                            CommonParams.StartPreviewSource.SOURCE_SENSOR,
                            ScreenUtils.getPreviewFPSByDataFlowMode(defaultDataFlowMode),
                            CommonParams.StartPreviewMode.VOC_DVP_MODE, defaultDataFlowMode) == 0) {
                        Log.i(TAG, "startPreview complete 中间出图 restart");
                        try {
                            /*
                             * 对于部分设备，如5840芯片的模组，两个命令之间需要延时以防止中间出图命令失效导致的黑热白热翻转情况
                             * 需要根据自己模组的实际情况判断是否添加该延时
                             */
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (ircmd.startY16ModePreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                                FileUtil.getY16SrcTypeByDataFlowMode(defaultDataFlowMode)) == 0) {
                            handleStartPreviewComplete();
                        } else {
                            Log.e(TAG, "startY16ModePreview error 中间出图 restart");
                        }
                    } else {
                        Log.e(TAG, "startPreview error 中间出图 restart");
                    }
                } else {
                    Log.e(TAG, "stopPreview error 中间出图 restart");
                }
            } else {
                /*
                 * 使用ISP算法
                 * 红外+TNR出图,只能为25Hz
                 */
                boolean isTempReplacedWithTNREnabled = ircmd.isTempReplacedWithTNREnabled(DeviceType.P2);
                Log.i(TAG,
                        "defaultDataFlowMode = others isTempReplacedWithTNREnabled = " + isTempReplacedWithTNREnabled);
                if (isTempReplacedWithTNREnabled) {
                    /*
                     * 支持 红外+TNR 方式出图
                     */
                    // 对于P2模组来说，直接发送startY16ModePreview命令可以直接出图
//                    if (ircmd.startY16ModePreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
//                            FileUtil.getY16SrcTypeByDataFlowMode(defaultDataFlowMode)) == 0) {
//                        handleStartPreviewComplete();
//                    } else {
//                        Log.e(TAG, "startY16ModePreview error");
//                    }
                    // 对于M2模组来说，需要先发送startPreview出图命令，再发送startY16ModePreview命令才可以重新出图
                    if (ircmd.stopPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0) == 0) {
                        Log.i(TAG, "stopPreview complete 红外+TNR");
                        if (ircmd.startPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                                CommonParams.StartPreviewSource.SOURCE_SENSOR,
                                ScreenUtils.getPreviewFPSByDataFlowMode(CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT),
                                CommonParams.StartPreviewMode.VOC_DVP_MODE,
                                CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT) == 0) {
                            Log.i(TAG, "startPreview complete 红外+TNR");
                            try {
                                /*
                                 * 对于部分设备，如5840芯片的模组，两个命令之间需要延时以防止中间出图命令失效导致的黑热白热翻转情况
                                 * 需要根据自己模组的实际情况判断是否添加该延时
                                 */
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (ircmd.startY16ModePreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                                    FileUtil.getY16SrcTypeByDataFlowMode(CommonParams.DataFlowMode.TNR_OUTPUT)) == 0) {
                                handleStartPreviewComplete();
                            } else {
                                Log.e(TAG, "startY16ModePreview error 红外+TNR");
                            }
                        } else {
                            Log.e(TAG, "startPreview error 红外+TNR");
                        }
                    } else {
                        Log.e(TAG, "stopPreview error 红外+TNR");
                    }
                } else {
                    /*
                     * 单TNR 出图
                     * 默认上电之后出YUV图像，如果默认模式为Y16中间出图，进入之后需要走先断电再上电，再中间出图的流程
                     * 如果没有断电，且之前的模式为Y16模式，则重新进入仍为Y16模式，不需要执行该流程
                     */
                    // 调用 startY16ModePreview 中间出图方法之后，输出的数据格式为y16
                    if (ircmd.stopPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0) == 0) {
                        Log.i(TAG, "stopPreview complete 单TNR");
                        if (ircmd.startPreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                                CommonParams.StartPreviewSource.SOURCE_SENSOR,
                                ScreenUtils.getPreviewFPSByDataFlowMode(defaultDataFlowMode),
                                CommonParams.StartPreviewMode.VOC_DVP_MODE, defaultDataFlowMode) == 0) {
                            Log.i(TAG, "startPreview complete 单TNR");
                            try {
                                /*
                                 * 对于部分设备，如5840芯片的模组，两个命令之间需要延时以防止中间出图命令失效导致的黑热白热翻转情况
                                 * 需要根据自己模组的实际情况判断是否添加该延时
                                 */
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (ircmd.startY16ModePreview(CommonParams.PreviewPathChannel.PREVIEW_PATH0,
                                    FileUtil.getY16SrcTypeByDataFlowMode(defaultDataFlowMode)) == 0) {
                                handleStartPreviewComplete();
                            } else {
                                Log.e(TAG, "startY16ModePreview error 单TNR");
                            }
                        } else {
                            Log.e(TAG, "startPreview error 单TNR");
                        }
                    } else {
                        Log.e(TAG, "stopPreview error 单TNR");
                    }
                }
            }
        }
    }

    /**
     *
     */
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
            //IRCMD在不用时一定要回收
            if (ircmd != null) {
                ircmd.onDestroy();
                ircmd = null;
            }

            SystemClock.sleep(200);

            //initIRISPModule 与 destroyIRISPModule对应使用，回收资源
            camera.onDestroyPreview();

        }
    }

    /**
     *
     */
    private void handleStartPreviewComplete() {
        // 出图之后再去获取kt,bt,nuc_t等参数来设置温度数据，避免耗时操作导致这里的停图和出图受影响
        new Thread(() -> EventBus.getDefault().post(new PreviewComplete())).start();
    }

}
