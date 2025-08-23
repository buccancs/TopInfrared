package com.example.thermal_lite.camera;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.Utils;
import com.elvishew.xlog.XLog;
import com.energy.ac020library.IrcamEngine;
import com.energy.ac020library.IrcmdEngine;
import com.energy.ac020library.bean.AutoGainImageRes;
import com.energy.ac020library.bean.AutoGainSwitchCallback;
import com.energy.ac020library.bean.AutoGainSwitchInfo;
import com.energy.ac020library.bean.AutoGainSwitchParam;
import com.energy.ac020library.bean.CommonParams;
import com.energy.ac020library.bean.DevHandleParam;
import com.energy.ac020library.bean.ErrorCode;
import com.energy.ac020library.bean.HandleInitCallback;
import com.energy.ac020library.bean.IIrFrameCallback;
import com.energy.ac020library.bean.InfoLineBean;
import com.energy.ac020library.bean.IrcmdError;
import com.energy.ac020library.bean.UvcHandleParam;

import com.energy.commoncomponent.Const;
import com.energy.commoncomponent.bean.DeviceType;
import com.energy.commoncomponent.bean.RotateDegree;
import com.energy.commonlibrary.util.FileUtil;
import com.energy.commonlibrary.util.SharedPreferencesUtils;
import com.energy.commonlibrary.view.SurfaceNativeWindow;
import com.energy.irutilslibrary.LibIRParse;
import com.energy.irutilslibrary.LibIRProcess;
import com.energy.irutilslibrary.LibIRTemp;
import com.energy.irutilslibrary.bean.IRPROCSRCFMTType;
import com.energy.irutilslibrary.bean.LogLevel;
import com.energy.iruvccamera.bean.CameraSize;
import com.energy.iruvccamera.bean.UvcParams;
import com.energy.iruvccamera.usb.USBMonitor;
import com.example.thermal_lite.BuildConfig;
import com.example.thermal_lite.IrConst;
import com.example.thermal_lite.ui.activity.IrDisplayActivity;
import com.example.thermal_lite.util.CommonUtil;
import com.infisense.usbir.utils.IRImageHelp;
import com.infisense.usbir.utils.PseudocodeUtils;
import com.topdon.lib.core.bean.AlarmBean;
import com.topdon.lib.ui.widget.LiteSurfaceView;

import java.nio.ByteBuffer;
import java.util.List;

public class CameraPreviewManager {

    private final String TAG = "CameraPreviewManager";
    private IIrFrameCallback mIIrFrameCallback;
    public LiteSurfaceView mSurfaceView;

    private CommonParams.FrameOutputFormat FRAME_OUT_PUT_FORMAT = CommonParams.FrameOutputFormat.YUYV_AND_TEMP_OUTPUT;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mStreamWidth;
    private int mStreamHeight;
    private int mFinalImageWidth = 0;
    private int mFinalImageHeight = 0;
    private UvcParams.FrameFormatType mFrameFormatType = UvcParams.FrameFormatType.FRAME_FORMAT_YUYV;

    private IrcamEngine mIrcamEngine;

    private LibIRTemp mLibIRTemp;

    private RotateDegree mImageRotate = RotateDegree.DEGREE_270;
    private LibIRProcess.ImageRes_t mImageRes;

    private boolean mFramePause = false;

    private boolean mShowDoubleImage = false;
    private IRImageHelp irImageHelp;

    private OnTempDataChangeCallback mOnTempDataChangeCallback;

    private CameraPreviewManager() {
        irImageHelp = new IRImageHelp();
    }

    private static CameraPreviewManager mInstance;

    public static synchronized CameraPreviewManager getInstance() {
        if (mInstance == null) {
            mInstance = new CameraPreviewManager();
        }
        return mInstance;
    }

    private Handler mMainHandler;

    private byte[] mIrData;
    private int mIrLength;
    private int mInfoDataHeight = 0;
    private byte[] mInfoData;
    private int mInfoLength;
    private byte[] mIrARGBData;
    private int mIrARGBLength;
    private byte[] mIrYuvData;
    private byte[] mIrRotateData;
    private byte[] mTempData;
    private byte[] mTempRotateData;
    private int mTempLength;
    private byte[] mResultARBGDataForZetaZoom;
    public byte[] frameIrAndTempData = new byte[192 * 256 * 4];
    public byte[] takePhotoIrAndTempData = new byte[192 * 256 * 4];

    private boolean mIsShowFPS = true;
    private boolean mSaveData = false;
    private boolean mTakePhoto = false;

    private SurfaceNativeWindow mSurfaceNativeWindow;
    private Surface mSurface;
    private Bitmap mPhotoBitmap;

    private boolean mSunProtectEnable = false;

    private float max = Float.MAX_VALUE;
    private float min = Float.MIN_VALUE;
    private int pseudocolorMode = 3;
    private AlarmBean alarmBean;
    private int maxColor;
    private int minColor;

    private boolean mAutoSwitchGainEnable = false;
    private AutoGainImageRes mAutoGainImageRes = new AutoGainImageRes();
    private AutoGainSwitchInfo mAutoGainSwitchInfo = new AutoGainSwitchInfo();
    private AutoGainSwitchParam mGainSwitchParam = new AutoGainSwitchParam();

    public int getPreviewWidth() {
        return mPreviewWidth;
    }

    public int getPreviewHeight() {
        return mPreviewHeight;
    }

    public boolean isSunProtectEnable() {
        return mSunProtectEnable;
    }

    public void setSunProtectEnable(boolean mSunProtectEnable) {
        this.mSunProtectEnable = mSunProtectEnable;
    }

    public void init(LiteSurfaceView surfaceView, Handler mainHandler) {
        this.mSurfaceView = surfaceView;
        this.mMainHandler = mainHandler;
        initData();

        mSurfaceNativeWindow = new SurfaceNativeWindow();
        mIIrFrameCallback = new IIrFrameCallback() {
            
            @Override
            public void onFrame(byte[] frame, int length) {
                try {
                    if (mFramePause) {
                        return;
                    }

                    if (mIsShowFPS) {
                        double fps = CommonUtil.showFps();
                        Log.d(TAG, "onFrame frame.length = " + length + " onFrame fps=" + String.format("%.1f", fps));
                        Message message = Message.obtain(mMainHandler, IrDisplayActivity.HANDLE_SHOW_FPS, fps);
                        mMainHandler.sendMessage(message);
                    }

                    System.arraycopy(frame, 0, mIrData, 0, mIrLength);
                    System.arraycopy(mIrData, 0, frameIrAndTempData, 0, mIrLength);

                    if (!mShowDoubleImage) {
                        if (mInfoLength != 0 && mSunProtectEnable) {
                            System.arraycopy(frame, mIrLength, mInfoData, 0, mInfoLength);
                            InfoLineBean infoLineBean = mIrcamEngine.getInfoLineBean(mInfoData);
                            if (infoLineBean.getSunProtectFlag() == 1 || infoLineBean.getHardwareSunProtectFlag() == 1) {
                                mMainHandler.sendEmptyMessage(IrDisplayActivity.HANDLE_SHOW_SUN_PROTECT_FLAG);
                            }
                        }
                    }

                    if (FRAME_OUT_PUT_FORMAT == CommonParams.FrameOutputFormat.YUYV_AND_TEMP_OUTPUT) {
                        if (!mShowDoubleImage) {
                            System.arraycopy(frame, mIrLength + mInfoLength, mTempData, 0, mTempLength);
                            System.arraycopy(frame, mIrLength + mInfoLength, frameIrAndTempData, mIrLength, mTempLength);

                        }
                        if (mOnTempDataChangeCallback != null) {
                            mOnTempDataChangeCallback.onTempDataChange(mTempData);
                        }
                    } else if (FRAME_OUT_PUT_FORMAT == CommonParams.FrameOutputFormat.NV12_AND_TEMP_OUTPUT) {
                        System.arraycopy(frame, mIrLength + mInfoLength, mTempData, 0, mTempLength);

                    }

                    switch (FRAME_OUT_PUT_FORMAT) {
                        case YUYV_IMAGE_OUTPUT:
                        case YUYV_AND_TEMP_OUTPUT:
                            if (Const.DEVICE_TYPE == DeviceType.DEVICE_TYPE_GL1280) {
                                CommonUtil.convertArrayY16ToY14(mIrData, 2 * mPreviewWidth * mPreviewHeight, mIrYuvData);
                                LibIRParse.convertArrayY14ToARGB(mIrYuvData, mPreviewWidth * mPreviewHeight, mIrARGBData);
                            } else {
                                LibIRParse.converyArrayYuv422ToARGB(mIrData, mPreviewWidth * mPreviewHeight, mIrARGBData);
                                if (irImageHelp.getColorList() == null) {
                                    com.energy.iruvc.sdkisp.LibIRProcess.convertYuyvMapToARGBPseudocolor(mIrData,
                                            mPreviewWidth * mPreviewHeight,
                                            PseudocodeUtils.INSTANCE.changePseudocodeModeByOld(pseudocolorMode), mIrARGBData);
                                }else {
                                    com.energy.iruvc.sdkisp.LibIRProcess.convertYuyvMapToARGBPseudocolor(mIrData,
                                            mPreviewWidth * mPreviewHeight,
                                            PseudocodeUtils.INSTANCE.changePseudocodeModeByOld(1), mIrARGBData);
                                }
                                irImageHelp.customPseudoColor(mIrARGBData,mTempData,mPreviewWidth,mPreviewHeight);
                                
                                irImageHelp.setPseudoColorMaxMin(mIrARGBData,mTempData,max,min,mPreviewWidth,mPreviewHeight);
                                mIrARGBData = irImageHelp.contourDetection(alarmBean,
                                        mIrARGBData, mTempData, mPreviewWidth, mPreviewHeight);
                            }
                            break;
                        case NV12_IMAGE_OUTPUT:
                        case NV12_AND_TEMP_OUTPUT:
                            Log.d(TAG, "NV12_AND_TEMP_OUTPUT");
                            LibIRParse.NV12ToRGBA(mIrData, mPreviewWidth, mPreviewHeight, mIrARGBData);

                            break;
                        default:
                            break;
                    }
                    mFinalImageWidth = 0;
                    mFinalImageHeight = 0;

                    handleSurfaceDisplay();

                    if (mAutoSwitchGainEnable && FRAME_OUT_PUT_FORMAT == CommonParams.FrameOutputFormat.YUYV_AND_TEMP_OUTPUT) {
                        Log.d(TAG, "onAutoGainSwitchState switch");
                        mIrcamEngine.advAutoGainSwitch(mTempData, mAutoGainImageRes, mAutoGainSwitchInfo, mGainSwitchParam, new AutoGainSwitchCallback() {
                            @Override
                            public void onAutoGainSwitchState(int gainselStatus) {
                                Log.d(TAG, "onAutoGainSwitchState : " + gainselStatus);
                            }

                            @Override
                            public void onAutoGainSwitchResult(int gainselStatus, int result) {
                                Log.d(TAG, "onAutoGainSwitchResult : " + gainselStatus);
                                Log.d(TAG, "onAutoGainSwitchResult : " + result);
                            }
                        });
                    }
                }catch (Exception e){
                    XLog.e(TAG,"Lite图像处理异常"+e.getMessage());
                }
            }
        };
    }

    public void initData() {

        mStreamWidth = IrConst.DEFAULT_STREAM_WIDTH;
        mStreamHeight = IrConst.DEFAULT_STREAM_HEIGHT;

        boolean isDoubleImage = IrConst.DEFAULT_DOUBLE_IMAGE;
        if (isDoubleImage) {
            setFrameOutPutFormat(CommonParams.FrameOutputFormat.YUYV_AND_TEMP_OUTPUT);
        } else {
            setFrameOutPutFormat(CommonParams.FrameOutputFormat.YUYV_IMAGE_OUTPUT);
        }
        if (Const.DEVICE_TYPE == DeviceType.DEVICE_TYPE_X3
                || Const.DEVICE_TYPE == DeviceType.DEVICE_TYPE_P2L
                || Const.DEVICE_TYPE == DeviceType.DEVICE_TYPE_X2PRO
                || Const.DEVICE_TYPE == DeviceType.DEVICE_TYPE_TC2C) {
            mInfoDataHeight = 2;
        } else {
            mInfoDataHeight = 0;
        }
        switch (FRAME_OUT_PUT_FORMAT) {
            case YUYV_IMAGE_OUTPUT:
                
                mFrameFormatType = UvcParams.FrameFormatType.FRAME_FORMAT_YUYV;

                mPreviewWidth = mStreamWidth;
                mPreviewHeight = mStreamHeight - mInfoDataHeight;
                mIrLength = mPreviewWidth * mPreviewHeight * 2;
                mIrData = new byte[mIrLength];
                mInfoLength = mPreviewWidth * mInfoDataHeight * 2;
                mInfoData = new byte[mInfoLength];
                mIrARGBLength = mPreviewWidth * mPreviewHeight * 2 * 2;
                mIrARGBData = new byte[mIrARGBLength];
                mIrYuvData = new byte[mIrLength / 2];
                mIrRotateData = new byte[mIrARGBLength];

                initZetaZoomData();

                break;
            case YUYV_AND_TEMP_OUTPUT:
                
                mFrameFormatType = UvcParams.FrameFormatType.FRAME_FORMAT_YUYV;

                if (mShowDoubleImage) {
                    mPreviewWidth = mStreamWidth;
                    mPreviewHeight = mStreamHeight;
                } else {
                    mPreviewWidth = mStreamWidth;
                    mPreviewHeight = (mStreamHeight - mInfoDataHeight) / 2;
                }

                mIrLength = mPreviewWidth * mPreviewHeight * 2;
                mIrData = new byte[mIrLength];
                if (!mShowDoubleImage) {
                    mInfoLength = mPreviewWidth * mInfoDataHeight * 2;
                    mInfoData = new byte[mInfoLength];
                }

                mIrARGBLength = mPreviewWidth * mPreviewHeight * 2 * 2;
                mIrARGBData = new byte[mIrARGBLength];
                mTempLength = mPreviewWidth * mPreviewHeight * 2;
                mTempData = new byte[mTempLength];
                mIrRotateData = new byte[mIrARGBLength];
                mTempRotateData = new byte[mIrARGBLength];
                break;
            case NV12_IMAGE_OUTPUT:
                
                mFrameFormatType = UvcParams.FrameFormatType.FRAME_FORMAT_NV12;

                mPreviewWidth = 640;
                mPreviewHeight = 512;

                mStreamWidth = 640;
                mStreamHeight = 512;

                mIrLength = (int) (mPreviewWidth * mPreviewHeight * 1.5);
                mIrData = new byte[mIrLength];
                mIrARGBLength = mPreviewWidth * mPreviewHeight * 2 * 2;
                mIrARGBData = new byte[mIrARGBLength];

                break;
            case NV12_AND_TEMP_OUTPUT:
                
                mFrameFormatType = UvcParams.FrameFormatType.FRAME_FORMAT_NV12;

                mPreviewWidth = 640;
                mPreviewHeight = 512;

                mStreamWidth = 640;
                mStreamHeight = 1200;

                mIrLength = (int) (mPreviewWidth * mPreviewHeight * 1.5);
                mIrData = new byte[mIrLength];
                mIrARGBLength = mPreviewWidth * mPreviewHeight * 2 * 2;
                mIrARGBData = new byte[mIrARGBLength];
                mTempLength = mPreviewWidth * mPreviewHeight * 2;
                mTempData = new byte[mTempLength];
                break;
            default:
                break;
        }

        Log.i(TAG, "mPreviewWidth = " + mPreviewWidth + " mPreviewHeight = " + mPreviewHeight);
        mLibIRTemp = new LibIRTemp(mPreviewWidth, mPreviewHeight);
        mImageRes = new LibIRProcess.ImageRes_t();
        mImageRes.width = (char) mPreviewWidth;
        mImageRes.height = (char) mPreviewHeight;

        mAutoGainImageRes.width = 256;
        mAutoGainImageRes.height = 192;

        mGainSwitchParam.above_pixel_prop = 0.1f;
        mGainSwitchParam.above_temp_data = (int) ((130 + 273.15) * 16 * 4);
        mGainSwitchParam.below_pixel_prop = 0.95f;
        mGainSwitchParam.below_temp_data = (int) ((150 + 273.15) * 16 * 4);
        mAutoGainSwitchInfo.switch_frame_cnt = 5 * 15;
        mAutoGainSwitchInfo.waiting_frame_cnt = 7 * 15;

    }

    public void handleUSBConnect(USBMonitor.UsbControlBlock ctrlBlock) {
        initHandleEngine(ctrlBlock, true);
    }

    public void handleUSBConnectNoPreview(USBMonitor.UsbControlBlock ctrlBlock) {
        initHandleEngine(ctrlBlock, false);
    }

    private void handleStartPreview() {
        startPreview();
        if (Const.DEVICE_TYPE == DeviceType.DEVICE_TYPE_WN2640) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    IrcmdError basicVideoStreamContinueResult = DeviceIrcmdControlManager.getInstance()
                            .getIrcmdEngine().basicVideoStreamContinue();
                    Log.d(TAG, "basicVideoStreamContinueResult=" + basicVideoStreamContinueResult);
                    mMainHandler.sendEmptyMessage(IrDisplayActivity.HIDE_LOADING);
                }
            }, 10000);
        } else {
            mMainHandler.sendEmptyMessage(IrDisplayActivity.HIDE_LOADING);
        }
    }

    public Bitmap scaledBitmap(){
        return scaledBitmap(false);
    }
    byte[] tmpData = null;
    public Bitmap scaledBitmap(Boolean isTakePhoto){
        if (tmpData == null){
            tmpData = new byte[mIrARGBLength];
        }
        System.arraycopy(mIrRotateData,0,tmpData,0,mIrARGBLength);
        mPhotoBitmap = Bitmap.createBitmap(mFinalImageWidth, mFinalImageHeight, Bitmap.Config.ARGB_8888);
        if (isTakePhoto){
            System.arraycopy(frameIrAndTempData, 0, takePhotoIrAndTempData, 0, takePhotoIrAndTempData.length);
        }
        mPhotoBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(tmpData));
        return mPhotoBitmap;
    }

    public List<CameraSize> getAllSupportedSize() {
        return mIrcamEngine.getUsbSupportInfo();
    }

    private void initHandleEngine(USBMonitor.UsbControlBlock ctrlBlock, boolean isStartPreview) {
        UvcHandleParam uvcHandleParam = new UvcHandleParam();
        
        uvcHandleParam.setCtrlBlock(ctrlBlock);

        int fps = IrConst.DEFAULT_STREAM_FPS;
        
        uvcHandleParam.setFps(fps);

        float bandwidth = SharedPreferencesUtils.getFloat(Utils.getApp(),
                IrConst.KEY_DEFAULT_STREAM_BANDWIDTH, IrConst.DEFAULT_STREAM_BANDWIDTH);

        uvcHandleParam.setBandwidth(bandwidth);

        Log.d(TAG, "initHandleEngine UvcHandleParam = " + uvcHandleParam.toString());

        LibIRProcess.irprocessLogRegister(LogLevel.SDK_LOG_NO_PRINT);
        LibIRProcess.getIRProcessVersion();
        LibIRParse.irparseLogRegister(LogLevel.SDK_LOG_NO_PRINT);
        LibIRParse.getIRParseVersion();
        LibIRTemp.irtempLogRegister(LogLevel.SDK_LOG_NO_PRINT);
        LibIRTemp.getIRTempVersion();

        mIrcamEngine = IrcamEngine.Builder()
                .setLogLevel(CommonParams.LogLevel.SDK_LOG_DEBUG)
                .setStreamWidth(mStreamWidth)
                .setStreamHeight(mStreamHeight)
                .setDriverType(CommonParams.DriverType.USB)
                
                .setFrameOutputFormat(FRAME_OUT_PUT_FORMAT)
                .setUvcHandleParam(uvcHandleParam)
                .build();
        Log.d(TAG, "stopPreview onSuccess initHandle : ");
        mIrcamEngine.initHandle(new HandleInitCallback() {
            @Override
            public void onSuccess(IrcmdEngine ircmdEngine) {
                DeviceIrcmdControlManager.getInstance().setIrcamEngine(mIrcamEngine);
                DeviceIrcmdControlManager.getInstance().setIrcmdEngine(ircmdEngine);
                Log.d(TAG, "IrcamVersion : " + mIrcamEngine.ircamVersion());
                Log.d(TAG, "IrcmdVersion : " + ircmdEngine.ircmdVersion());
                Log.d(TAG, "IrcamVersion number: " + mIrcamEngine.ircamVersionNumber());
                Log.d(TAG, "IrcmdVersion number: " + ircmdEngine.ircmdVersionNumber());
                if (isStartPreview) {
                    handleStartPreview();
                }
            }

            @Override
            public void onFail(ErrorCode errorCode) {
                mMainHandler.sendEmptyMessage(IrDisplayActivity.HANDLE_INIT_FAIL);
            }
        });
    }

    public void startPreview() {
        Log.d(TAG, "startPreview");
        if (mIrcamEngine != null) {
            mIrcamEngine.setIrFrameCallback(mIIrFrameCallback);
            int result = mIrcamEngine.startVideoStream();
            if (result != 0) {
                mMainHandler.sendEmptyMessage(IrDisplayActivity.PREVIEW_FAIL);
            }
            if (Const.DEVICE_TYPE == DeviceType.DEVICE_TYPE_X3) {
                DeviceIrcmdControlManager.getInstance().sendFPGAParam();
                DeviceIrcmdControlManager.getInstance().sendISPParam();
            }
        }
        TempCompensation.getInstance().startTempCompensation();
    }

    public void pausePreview() {
        if (mIrcamEngine != null) {
            mIrcamEngine.pauseVideoStream();
        }
    }

    public void resumePreview() {
        if (mIrcamEngine != null) {
            mIrcamEngine.resumeVideoStream();
        }
    }

    public void closePreview() {
        if (mIrcamEngine != null) {
            mIrcamEngine.closeVideoStream();
            mIrcamEngine.releaseVideoStream();
            mIrcamEngine.destroyHandle();
            mIrcamEngine = null;
        }
    }

    public void stopPreview() {
        Log.i(TAG, "stopPreview");
        if (Const.DEVICE_TYPE == DeviceType.DEVICE_TYPE_WN2640) {
            IrcmdError ircmdError = DeviceIrcmdControlManager.getInstance().getIrcmdEngine()
                    .basicVideoStreamPause();
            Log.d(TAG, "basicVideoStreamPause=" + ircmdError);
        }
        if (mIrcamEngine != null) {
            mIrcamEngine.setIrFrameCallback(null);
            mIrcamEngine.stopVideoStream();
        }
    }

    public void releaseSource() {
        mIIrFrameCallback = null;
        mIrARGBData = null;
        mIrData = null;
        mOnTempDataChangeCallback = null;
        setAutoSwitchGainEnable(false);
        DeviceIrcmdControlManager.getInstance().setIrcmdEngine(null);
        DeviceIrcmdControlManager.getInstance().setIrcamEngine(null);
    }

    public void updateDevHandleParam(DevHandleParam devHandleParam) {
        if (mIrcamEngine != null) {
            mIrcamEngine.updateDevHandleParam(devHandleParam);
        }
    }

    public void setSaveData(boolean mSaveData) {
        this.mSaveData = mSaveData;
    }

    public void setTakePhoto(boolean takePhoto) {
        this.mTakePhoto = takePhoto;
    }

    public IrcamEngine getIrcamEngine() {
        return mIrcamEngine;
    }

    public void setImageRotate(RotateDegree imageRotate) {
        this.mImageRotate = imageRotate;
        mIrRotateData = null;
        mIrRotateData = new byte[mIrARGBLength];
        Log.d(TAG, "setImageRotate : " + imageRotate.getValue());
    }

    public RotateDegree getImageRotate() {
        return mImageRotate;
    }

    public void setFramePause(boolean framePause) {
        this.mFramePause = framePause;
    }

    public void setFrameOutPutFormat(CommonParams.FrameOutputFormat frameOutPutFormat) {
        FRAME_OUT_PUT_FORMAT = frameOutPutFormat;
    }

    public void setShowDoubleImage(boolean showDoubleImage) {
        this.mShowDoubleImage = showDoubleImage;
    }

    public void setAutoSwitchGainEnable(boolean mAutoSwitchGainEnable) {
        this.mAutoSwitchGainEnable = mAutoSwitchGainEnable;
    }

    public boolean getAutoSwitchGainEnable() {
        return mAutoSwitchGainEnable;
    }

    public void setOnTempDataChangeCallback(OnTempDataChangeCallback onTempDataChangeCallback) {
        this.mOnTempDataChangeCallback = onTempDataChangeCallback;
    }

    public interface OnTempDataChangeCallback {
        void onTempDataChange(byte[] data);
    }

    private void handleSurfaceDisplay() {

        switch (mImageRotate) {
            case DEGREE_0:
                mFinalImageWidth = mPreviewWidth;
                mFinalImageHeight = mPreviewHeight;
                System.arraycopy(mIrARGBData,0,mIrRotateData,0,mIrARGBData.length);
                break;
            case DEGREE_90:
                mFinalImageWidth = mPreviewHeight;
                mFinalImageHeight = mPreviewWidth;
                LibIRProcess.rotateRight90(mIrARGBData, mImageRes,
                        IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, mIrRotateData);
                break;
            case DEGREE_180:
                mFinalImageWidth = mPreviewWidth;
                mFinalImageHeight = mPreviewHeight;
                LibIRProcess.rotate180(mIrARGBData, mImageRes,
                        IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, mIrRotateData);
                break;
            case DEGREE_270:
                mFinalImageWidth = mPreviewHeight;
                mFinalImageHeight = mPreviewWidth;
                LibIRProcess.rotateLeft90(mIrARGBData, mImageRes,
                        IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, mIrRotateData);
                break;
            default:
                break;
        }
        try {
            mSurfaceView.setMIrRotateData(mIrRotateData.clone());
            mSurfaceView.setMFinalImageWidth(mFinalImageWidth);
            mSurfaceView.setMFinalImageHeight(mFinalImageHeight);
            mSurface = mSurfaceView.getHolder().getSurface();
            if (mSurface != null) {
                mSurfaceNativeWindow.onDrawFrame(mSurface, mIrRotateData, mFinalImageWidth, mFinalImageHeight);
            }
        }catch (Exception e){
            XLog.e(TAG+":lite的图像渲染异常："+e.getMessage());
        }
    }

    private void initZetaZoomData() {
    }

    private void handleSurfaceDisplayForZetaZoom() {
    }

    public AlarmBean getAlarmBean() {
        return alarmBean;
    }

    public void setAlarmBean(AlarmBean alarmBean) {
        this.alarmBean = alarmBean;
    }

    public void setLimit(float max, float min, int maxColor, int minColor) {
        this.max = max;
        this.min = min;
        this.maxColor = maxColor;
        this.minColor = minColor;
    }

    public void setColorList(int[] colorList, @Nullable float[] places, boolean isUseGray, float customMaxTemp, float customMinTemp) {
        irImageHelp.setColorList(colorList, places, isUseGray,customMaxTemp,customMinTemp);
    }
    public void setPseudocolorMode(int pseudocolorMode) {
        this.pseudocolorMode = pseudocolorMode;
    }
}
