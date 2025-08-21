package com.infisense.usbir.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.energy.iruvc.utils.SynchronizedBitmap;
import com.infisense.usbdual.Const;
import com.infisense.usbir.utils.OpencvTools;

/**
 * 红外图像展示控件，可以为TextureView或SurfaceView
 */
public class CameraView extends TextureView {
    private String TAG = "CameraView";
    private Bitmap bitmap;
    private SynchronizedBitmap syncimage;
    private Runnable runnable;
    private Thread cameraThread;
    private Canvas canvas = null;
    /**
     * 画面中心的十字交叉线绘制
     */
    private Paint paint;
    private int cross_len = 20;
    /**
     * 帧率展示
     */
    private Paint greenPaint;
    private boolean drawLine = true;//是否画中心十字架
    public int productType = Const.TYPE_IR;
    private int irWidth = 192;
    private int irHeight = 256;

    private boolean isOpenAmplify = false;


    public boolean isOpenAmplify() {
        return isOpenAmplify;
    }

    public void setOpenAmplify(boolean openAmplify) {
        isOpenAmplify = openAmplify;
    }

    public void setImageSize(int irWidth, int irHeight){
        this.irWidth = irWidth;
        this.irHeight = irHeight;
    }

    public CameraView(Context context) {
        this(context, null, 0);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //
        paint = new Paint();  //画笔
        paint = new Paint(Paint.FILTER_BITMAP_FLAG);
//        paint = new Paint();  //画笔
        paint.setStrokeWidth(2);  //设置线宽。单位为像素
        paint.setAntiAlias(true); //抗锯齿
        paint.setDither(true);    //防抖动
        paint.setColor(Color.WHITE);  //画笔颜色
        //
        greenPaint = new Paint();
        greenPaint.setStrokeWidth(6);
        greenPaint.setTextSize(56);
        greenPaint.setColor(Color.GREEN);
        // 线程中绘制画面
        runnable = new Runnable() {
            @Override
            public void run() {
                while (!cameraThread.isInterrupted()) {
                    synchronized (syncimage.viewLock) {
                        //
                        if (syncimage.valid == false) {
                            try {
                                syncimage.viewLock.wait();
                            } catch (InterruptedException e) {
                                cameraThread.interrupt();
                                Log.e(TAG, "lock.wait(): catch an interrupted exception");
                            }
                        }
                        //
                        if (syncimage.valid == true) {
                            canvas = lockCanvas();
                            if (canvas == null) {
                                continue;
                            }
                            // 画面中心的十字交叉线绘制
                            paint.setStrokeWidth(2);  //设置线宽。单位为像素
                            paint.setAntiAlias(true); //抗锯齿
                            paint.setDither(true);    //防抖动
                            paint.setColor(Color.WHITE);  //画笔颜色
                            /**
                             * 图片缩放，这里简单的使用getWidth()作为宽，getHeight()作为高，可能会出现画面拉伸情况，
                             * 实际使用的时候请参考设备的宽高按照设备的图像尺寸做等比例缩放
                             */
                            Bitmap mScaledBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
                            canvas.drawBitmap(mScaledBitmap, 0, 0, null);

                            /**
                             * 画面中心的十字交叉线绘制
                             */
                            if (drawLine){
                                canvas.drawLine(getWidth() / 2 - cross_len, getHeight() / 2,
                                        getWidth() / 2 + cross_len, getHeight() / 2, paint);
                                canvas.drawLine(getWidth() / 2, getHeight() / 2 - cross_len,
                                        getWidth() / 2, getHeight() / 2 + cross_len, paint);
                            }
                            //
                            unlockCanvasAndPost(canvas);
                            syncimage.valid = false;
                        }
                    }
                    SystemClock.sleep(1);
                }
                Log.w(TAG, "DisplayThread exit:");
            }
        };
    }

    public boolean isDrawLine() {
        return drawLine;
    }

    public void setDrawLine(boolean drawLine) {
        this.drawLine = drawLine;
    }

    /**
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Nullable
    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    /**
     * @param syncimage
     */
    public void setSyncimage(SynchronizedBitmap syncimage) {
        this.syncimage = syncimage;
    }

    @NonNull
    public Bitmap getScaledBitmap() {
        synchronized (syncimage.viewLock) {
            Bitmap sBitmap = null;
            sBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
            return sBitmap;
        }
    }
    /**
     *
     */
    public void start() {
        cameraThread = new Thread(runnable);
        cameraThread.start();
    }
    public void setShowCross(boolean isShow){
        try {
            cross_len = isShow ? 20 : 0;
            canvas.drawLine(getWidth() / 2f - cross_len, getHeight() / 2f,
                    getWidth() / 2f + cross_len, getHeight() / 2f, paint);
            canvas.drawLine(getWidth() / 2f, getHeight() / 2f - cross_len,
                    getWidth() / 2f, getHeight() / 2f + cross_len, paint);
        }catch (Exception e){
            Log.e(TAG,"点异常:"+e.getMessage());
        }
    }


    /**
     *
     */
    public void stop() {
        try {
            if (cameraThread != null){
                cameraThread.interrupt();
                cameraThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}




