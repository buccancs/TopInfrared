package com.infisense.usbdual.camera;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.energy.commonlibrary.view.SurfaceNativeWindow;
import com.energy.iruvc.dual.ConcreateDualBuilder;
import com.energy.iruvc.dual.DualType;
import com.energy.iruvc.dual.DualUVCCamera;
import com.energy.iruvc.sdkisp.LibIRParse;
import com.energy.iruvc.sdkisp.LibIRProcess;
import com.energy.iruvc.utils.AutoGainSwitchCallback;
import com.energy.iruvc.utils.AvoidOverexposureCallback;
import com.energy.iruvc.utils.CommonParams;
import com.energy.iruvc.utils.DualCameraParams;
import com.energy.iruvc.utils.IFrameCallback;
import com.energy.iruvc.utils.IIRFrameCallback;
import com.energy.iruvc.uvc.UVCCamera;
import com.infisense.usbdual.Const;
import com.infisense.usbir.utils.OpencvTools;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Locale;

import static com.infisense.usbdual.camera.IFrameData.FRAME_LEN;

public class DualViewWithExternalCameraCommonApi extends BaseDualView {

    private final String TAG = "DualViewWithExternalCameraCommonApi";
    private DualUVCCamera dualUVCCamera;
    private final IFrameCallback iFrameCallback;
    private final IIRFrameCallback irFrameCallback;
    public SurfaceView cameraview;
    public boolean isRun = true;
    public int count = 0;
    private long timestart = 0;
    private double fps = 0;
    public boolean auto_gain_switch = false;
    public boolean auto_gain_switch_running = true;
    public boolean auto_over_protect = false;
    private LibIRProcess.AutoGainSwitchInfo_t auto_gain_switch_info = new LibIRProcess.AutoGainSwitchInfo_t();
    private LibIRProcess.GainSwitchParam_t gain_switch_param = new LibIRProcess.GainSwitchParam_t();
    private CommonParams.GainStatus gainStatus = CommonParams.GainStatus.HIGH_GAIN;
    public Bitmap bitmap;
    public Bitmap supIROlyNoFusionBitmap;
    public Bitmap supMixBitmap;
    public Bitmap supIROlyBitmap;

    private boolean valid = false;
    private Bitmap mScaledBitmap;
    private Handler handler;

    private boolean isUseIRISP = false;

    private SurfaceNativeWindow mSurfaceNativeWindow;
    private Surface mSurface;

    private DualCameraParams.FusionType mCurrentFusionType;
    private boolean firstFrame = false;
    private byte[] irRGBAData;
    private byte[] preIrData;
    private byte[] preTempData;
    private byte[] preIrARGBData;
    public byte[] frameData = new byte[FRAME_LEN];

    public byte[] frameIrAndTempData = new byte[192 * 256 * 4];

    public int rotate = 180;

    private volatile boolean isOpenAmplify = false;
    private final byte[] amplifyMixRotateArray;
    private final byte[] amplifyIRRotateArray;

    public static final int MULTIPLE = 2;

    public boolean isOpenAmplify() {
        return isOpenAmplify;
    }

    public void setOpenAmplify(boolean openAmplify) {
        isOpenAmplify = openAmplify;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public DualViewWithExternalCameraCommonApi(SurfaceView cameraview, UVCCamera irUVCCamera,
                                               CommonParams.DataFlowMode dataFlowMode,
                                               int irCameraWidth, int irCameraHeight, int vlCameraWidth, int vlCameraHeight,
                                               int dualCameraWidth, int dualCameraHeight,
                                               boolean isUseIRISP,int rotate,IIRFrameCallback irFrameCallback) {
        Const.CAMERA_WIDTH = vlCameraWidth;
        Const.CAMERA_HEIGHT = vlCameraHeight;
        Const.IR_WIDTH = irCameraHeight;
        Const.IR_HEIGHT = irCameraWidth;
        Const.VL_WIDTH = vlCameraHeight;
        Const.VL_HEIGHT = vlCameraWidth;
        Const.DUAL_WIDTH = dualCameraWidth;
        Const.DUAL_HEIGHT = dualCameraHeight;
        this.rotate = rotate;
        onFrameCallbacks = new ArrayList<>();
        fusionLength = Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 4;
        irSize = Const.IR_WIDTH * Const.IR_HEIGHT;
        vlSize = Const.VL_WIDTH * Const.VL_HEIGHT * 3;
        remapTempSize = Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 2;
        remapTempData = new byte[Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 2];
        mixData = new byte[fusionLength];
        normalTempData = new byte[irSize * 2];
        irData = new byte[irSize * 2];
        vlData = new byte[vlSize];
        vlARGBData = new byte[fusionLength];
        amplifyMixRotateArray = new byte[fusionLength * MULTIPLE * MULTIPLE];
        amplifyIRRotateArray = new byte[irData.length * MULTIPLE * MULTIPLE];
        this.irFrameCallback = irFrameCallback;

        this.isUseIRISP = isUseIRISP;
        this.cameraview = cameraview;
        bitmap = Bitmap.createBitmap(dualCameraWidth, dualCameraHeight, Bitmap.Config.ARGB_8888);
        supIROlyNoFusionBitmap = Bitmap.createBitmap(irCameraWidth * MULTIPLE,
                irCameraHeight * MULTIPLE, Bitmap.Config.ARGB_8888);
        supIROlyBitmap = Bitmap.createBitmap(irCameraWidth,
                irCameraHeight, Bitmap.Config.ARGB_8888);
        supMixBitmap = Bitmap.createBitmap(Const.DUAL_WIDTH * MULTIPLE,
                Const.DUAL_HEIGHT * MULTIPLE, Bitmap.Config.ARGB_8888);
        ConcreateDualBuilder concreateDualBuilder = new ConcreateDualBuilder();
        dualUVCCamera = concreateDualBuilder
                .setDualType(DualType.USB_DUAL)
                .setIRSize(Const.IR_WIDTH, Const.IR_HEIGHT)
                .setVLSize(Const.VL_WIDTH, Const.VL_HEIGHT)
                .setDualSize(Const.DUAL_HEIGHT, Const.DUAL_WIDTH)
                .setDataFlowMode(dataFlowMode)
                .setPreviewCameraStyle(CommonParams.PreviewCameraStyle.EXTERNAL_CAMERA)
                .setDeviceStyle(CommonParams.DeviceStyle.ALL_IN_ONE)
                .setUseDualGPU(false)
                
                .setMultiThreadHandleDualEnable(false)
                .build();
        DualCameraParams.TypeLoadParameters rotateT = DualCameraParams.TypeLoadParameters.ROTATE_0;
        if (rotate == 0) {
            rotateT = DualCameraParams.TypeLoadParameters.ROTATE_0;
        } else if (rotate == 90) {
            rotateT = DualCameraParams.TypeLoadParameters.ROTATE_90;
        } else if (rotate == 180) {
            rotateT = DualCameraParams.TypeLoadParameters.ROTATE_180;
        } else if (rotate == 270) {
            rotateT = DualCameraParams.TypeLoadParameters.ROTATE_270;
        }
        dualUVCCamera.setImageRotate(rotateT);
        dualUVCCamera.addIrUVCCamera(irUVCCamera);
        mSurfaceNativeWindow = new SurfaceNativeWindow();

        gain_switch_param.above_pixel_prop = 0.1f;
        gain_switch_param.above_temp_data = (int) ((130 + 273.15) * 16 * 4);
        gain_switch_param.below_pixel_prop = 0.95f;
        gain_switch_param.below_temp_data = (int) ((110 + 273.15) * 16 * 4);
        auto_gain_switch_info.switch_frame_cnt = 5 * 15;
        auto_gain_switch_info.waiting_frame_cnt = 7 * 15;
        int low_gain_over_temp_data = (int) ((550 + 273.15) * 16 * 4);
        int high_gain_over_temp_data = (int) ((110 + 273.15) * 16 * 4);
        float pixel_above_prop = 0.02f;
        int switch_frame_cnt = 7 * 15;
        int close_frame_cnt = 10 * 15;

        LibIRProcess.ImageRes_t imageRes = new LibIRProcess.ImageRes_t();
        imageRes.height = (char) (192);
        imageRes.width = (char) 256;

        irRGBAData = new byte[irSize * 4];
        preIrData = new byte[irSize * 2];
        preTempData = new byte[irSize * 2];
        preIrARGBData = new byte[irSize * 2 * 2];;
        iFrameCallback = new IFrameCallback() {

            @Override
            public void onFrame(byte[] frame) {
                if (frame.length == 1) {
                    if (handler != null) {
                        handler.sendEmptyMessage(Const.RESTART_USB);
                    }
                    Log.d(TAG, "RESTART_USB");
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
                    Log.d(TAG, "frame.length = " + frame.length + " fps=" + String.format(Locale.US, "%.1f", fps) +
                            " dataFlowMode = " + dataFlowMode);
                }
                System.arraycopy(frame, 0, mixData, 0, fusionLength);
                System.arraycopy(frame, fusionLength, irData, 0, irSize * 2);
                System.arraycopy(frame, fusionLength + irSize * 2, normalTempData, 0, irSize * 2);

                System.arraycopy(frame, fusionLength + irSize * 4, remapTempData, 0,
                        Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 2);

                System.arraycopy(frame, fusionLength + irSize * 4 + Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 2, vlData,
                        0, vlSize);
                System.arraycopy(frame, 0, frameData, 0, FRAME_LEN);
                System.arraycopy(frame, dualCameraWidth*dualCameraHeight*4, frameIrAndTempData, 0, frameIrAndTempData.length);

                if (mCurrentFusionType == DualCameraParams.FusionType.IROnlyNoFusion) {
                    for (OnFrameCallback onFrameCallback : onFrameCallbacks) {
                        onFrameCallback.onFame(mixData, normalTempData, fps);
                    }
                } else {
                    for (OnFrameCallback onFrameCallback : onFrameCallbacks) {
                        onFrameCallback.onFame(mixData, remapTempData, fps);
                    }
                }

                mSurface = cameraview.getHolder().getSurface();

                if (mCurrentFusionType == DualCameraParams.FusionType.IROnlyNoFusion) {
                    LibIRParse.converyArrayYuv422ToARGB(irData, Const.IR_WIDTH * Const.IR_HEIGHT, irRGBAData);
                    if (isOpenAmplify){
                        OpencvTools.supImage(irData,Const.IR_HEIGHT,Const.IR_WIDTH, amplifyIRRotateArray);
                        if (mSurface != null) {
                            mSurfaceNativeWindow.onDrawFrame(mSurface, amplifyIRRotateArray,
                                    Const.IR_WIDTH * MULTIPLE,
                                    Const.IR_HEIGHT * MULTIPLE);
                        }
                    }else {
                        if (mSurface != null) {
                            mSurfaceNativeWindow.onDrawFrame(mSurface, irRGBAData, Const.IR_HEIGHT, Const.IR_WIDTH);
                        }
                    }
                }else {
                    if (isOpenAmplify){
                        if (mCurrentFusionType == DualCameraParams.FusionType.IROnly){
                            OpencvTools.supImageMix(mixData,Const.DUAL_HEIGHT,Const.DUAL_WIDTH, mixData);
                            if (mSurface != null) {
                                mSurfaceNativeWindow.onDrawFrame(mSurface, mixData, Const.DUAL_WIDTH, Const.DUAL_HEIGHT);
                            }
                        }else {
                            OpencvTools.supImage(mixData,Const.DUAL_HEIGHT,Const.DUAL_WIDTH, amplifyMixRotateArray);
                            if (mSurface != null) {
                                mSurfaceNativeWindow.onDrawFrame(mSurface, amplifyMixRotateArray,
                                        Const.DUAL_WIDTH * MULTIPLE,
                                        Const.DUAL_HEIGHT * MULTIPLE);
                            }
                        }
                    }else {
                        if (mSurface != null) {
                            mSurfaceNativeWindow.onDrawFrame(mSurface, mixData, Const.DUAL_WIDTH, Const.DUAL_HEIGHT);
                        }
                    }
                }

                if (!isUseIRISP && !firstFrame) {
                    firstFrame = true;
                    if (handler != null) {
                        handler.sendEmptyMessage(Const.HIDE_LOADING);
                    }
                }

                if (dataFlowMode == CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT) {
                    System.arraycopy(frame, fusionLength + irSize * 2, normalTempData, 0, irSize * 2);
                    if (auto_gain_switch && auto_gain_switch_running) {
                        USBMonitorManager.getInstance().getIrcmd().autoGainSwitch(normalTempData, imageRes,
                                auto_gain_switch_info, gain_switch_param, new AutoGainSwitchCallback() {
                                    @Override
                                    public void onAutoGainSwitchState(CommonParams.PropTPDParamsValue.GAINSELStatus gainselStatus) {
                                        Log.d(TAG, "onAutoGainSwitchState = " + gainselStatus.getValue());
                                        auto_gain_switch_running = false;
                                        resetAutoGainInfo();
                                    }

                                    @Override
                                    public void onAutoGainSwitchResult(CommonParams.PropTPDParamsValue.GAINSELStatus gainselStatus, int result) {
                                        Log.d(TAG, "onAutoGainSwitchResult = " + gainselStatus.getValue() + "  result" +
                                                ":" + result);
                                        auto_gain_switch_running = true;
                                    }
                                });
                    }
                    if (auto_over_protect) {
                        USBMonitorManager.getInstance().getIrcmd().avoidOverexposure(false, gainStatus,
                                normalTempData, imageRes, low_gain_over_temp_data, high_gain_over_temp_data,
                                pixel_above_prop, switch_frame_cnt, close_frame_cnt, new AvoidOverexposureCallback() {
                                    @Override
                                    public void onAvoidOverexposureState(boolean avoidOverexpol) {
                                        Log.d(TAG, "onAvoidOverexposureState = " + avoidOverexpol);
                                    }
                                });
                    }
                }

            }
        };
    }

    public void resetAutoGainInfo() {
        auto_gain_switch_info.switched_flag = 0;
        auto_gain_switch_info.cur_switched_cnt = 0;
        auto_gain_switch_info.cur_detected_low_cnt = 0;
        auto_gain_switch_info.cur_detected_high_cnt = 0;
    }

    public void startPreview() {
        
        switchIrPreDataHandleEnable(true);
        dualUVCCamera.setFrameCallback(iFrameCallback);
        dualUVCCamera.onStartPreview();
        firstFrame = false;
    }

    public DualUVCCamera getDualUVCCamera() {
        return dualUVCCamera;
    }

    public void stopPreview() {
        dualUVCCamera.setFrameCallback(null);
        dualUVCCamera.onStopPreview();
        SystemClock.sleep(200);
        dualUVCCamera.onDestroy();
    }

    public void switchIrPreDataHandleEnable(boolean enable) {
        dualUVCCamera.setIrDataPreHandleEnable(enable);
        dualUVCCamera.setIrFrameCallback(enable? irFrameCallback : null);
    }

    public byte[] getRemapTempData() {
        return remapTempData;
    }

    public Bitmap getScaledBitmap() {
        if (isOpenAmplify){
            if (mCurrentFusionType == DualCameraParams.FusionType.IROnlyNoFusion){
                supIROlyNoFusionBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(amplifyIRRotateArray, 0,
                        supIROlyNoFusionBitmap.getWidth() * supIROlyNoFusionBitmap.getHeight() * 4));
                mScaledBitmap = Bitmap.createScaledBitmap(supIROlyNoFusionBitmap,
                        ((ViewGroup)cameraview.getParent()).getWidth(),
                        ((ViewGroup)cameraview.getParent()).getHeight(), true);
            }else if (mCurrentFusionType == DualCameraParams.FusionType.IROnly){
                bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mixData, 0, bitmap.getWidth() * bitmap.getHeight() * 4));
                mScaledBitmap = Bitmap.createScaledBitmap(bitmap, ((ViewGroup)cameraview.getParent()).getWidth(),
                        ((ViewGroup)cameraview.getParent()).getHeight(), true);
            } else {
                supMixBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mixData, 0, supMixBitmap.getWidth() * supMixBitmap.getHeight() * 4));
                mScaledBitmap = Bitmap.createScaledBitmap(supMixBitmap, ((ViewGroup)cameraview.getParent()).getWidth(),
                        ((ViewGroup)cameraview.getParent()).getHeight(), true);
            }
        }else {
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mixData, 0, bitmap.getWidth() * bitmap.getHeight() * 4));
            mScaledBitmap = Bitmap.createScaledBitmap(bitmap, ((ViewGroup)cameraview.getParent()).getWidth(),
                    ((ViewGroup)cameraview.getParent()).getHeight(), true);
        }
        return mScaledBitmap;
    }

    private boolean saveData = false;

    public void saveData() {
        saveData = true;
    }

    public void setGainStatus(CommonParams.GainStatus gainStatus) {
        this.gainStatus = gainStatus;
    }

    public void setCurrentFusionType(DualCameraParams.FusionType currentFusionType) {
        this.mCurrentFusionType = currentFusionType;
        if (dualUVCCamera != null) {
            dualUVCCamera.setFusion(currentFusionType);
        }
    }
}
