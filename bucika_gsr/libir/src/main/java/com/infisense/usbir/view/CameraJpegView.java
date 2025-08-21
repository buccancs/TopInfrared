package com.infisense.usbir.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import com.energy.iruvc.utils.SynchronizedBitmap;


public class CameraJpegView extends TextureView {

    private String TAG = "CameraView";
    private Bitmap bitmap;
    private SynchronizedBitmap syncimage;
    private Runnable runnable;
    private Thread cameraThread;

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setSyncimage(SynchronizedBitmap syncimage) {
        this.syncimage = syncimage;
    }

    public CameraJpegView(Context context) {
        this(context, null, 0);
    }

    public CameraJpegView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraJpegView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        runnable = new Runnable() {
            @Override
            public void run() {
                Canvas canvas = null;
                while (!cameraThread.isInterrupted()) {
                    synchronized (syncimage.viewLock) {
                        if (syncimage.valid == false) {
                            try {
                                syncimage.viewLock.wait();
                            } catch (InterruptedException e) {
                                cameraThread.interrupt();
                                Log.e(TAG, "lock.wait(): catch an interrupted exception");
                            }
                        }
                        if (syncimage.valid == true) {
                            canvas = lockCanvas();
                            if (canvas == null)
                                continue;

                            //p2
                            /*Matrix matrix = new Matrix();
                            matrix.setRotate(90);
                            Bitmap newBM = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                            */
                            Bitmap mScaledBitmap = Bitmap.createScaledBitmap(bitmap, getWidth(), getHeight(), true);
                            canvas.drawBitmap(mScaledBitmap, 0, 0, null);

                            Paint paint = new Paint();  //画笔
                            paint.setStrokeWidth(2);  //设置线宽。单位为像素
                            paint.setAntiAlias(true); //抗锯齿
                            paint.setColor(Color.WHITE);  //画笔颜色

                            int cross_len = 20;
                            canvas.drawLine(getWidth() / 2f - cross_len, getHeight() / 2f,
                                    getWidth() / 2f + cross_len, getHeight() / 2f, paint);
                            canvas.drawLine(getWidth() / 2f, getHeight() / 2f - cross_len,
                                    getWidth() / 2f, getHeight() / 2f + cross_len, paint);
                            unlockCanvasAndPost(canvas);
                            syncimage.valid = false;
                        }
                    }
                    try {
                        cameraThread.sleep(1);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "sleep crash");
                        e.printStackTrace();
                        cameraThread.interrupt();
                    }
                }
                Log.w(TAG, "DisplayThread exit:");
            }
        };

    }

    public void start() {
        cameraThread = new Thread(runnable);
        cameraThread.start();
    }

    public void stop() {
        cameraThread.interrupt();
        try {
            cameraThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
