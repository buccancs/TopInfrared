package com.topdon.module.thermal.ir.video.media;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public abstract class Encoder {
    private static final String TAG = Encoder.class.getSimpleName();
    protected static final int STATE_IDLE = 0;
    protected static final int STATE_RECORDING = 1;
    protected static final int STATE_RECORDING_UNTIL_LAST_FRAME = 2;
    private List<Bitmap> bitmapQueue;
    private EncodeFinishListener encodeFinishListener;
    private EncodingOptions encodingOptions;
    private Thread encodingThread;
    private int frameDelay = 50;
    private int height;
    protected String outputFilePath = null;
    private int state = STATE_IDLE;
    private int width;

    private Runnable mRunnableEncoder = new Runnable() {
        public void run() {
            while (true) {
                if (state != STATE_RECORDING && bitmapQueue.size() <= 0) {
                    break;
                } else if (bitmapQueue.size() > 0) {
                    Bitmap bitmap = null;
                    try {
                        bitmap = bitmapQueue.remove(0);
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    if (bitmap != null) {
                        try {
                            onAddFrame(bitmap);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            Log.e(TAG, e.getMessage());
                        }
                        bitmap.recycle();
                    }
                    if (state == STATE_RECORDING_UNTIL_LAST_FRAME && bitmapQueue.size() == 0) {
                        Log.d(TAG, "Last frame added");
                        break;
                    }
                }
            }
            Log.d(TAG, "add Frame finished");
            onStop();
            notifyEncodeFinish();
        }
    };


    public interface EncodeFinishListener {
        void onEncodeFinished();
    }


    public Encoder() {
        setDefaultEncodingOptions();
        init();
    }

    public Encoder(EncodingOptions options) {
        encodingOptions = options;
        init();
    }

    private void init() {
        onInit();
        initBitmapQueue();
    }

    private void setDefaultEncodingOptions() {
        encodingOptions = new EncodingOptions();
        encodingOptions.compressLevel = 0;
    }

    private void initBitmapQueue() {
        bitmapQueue = Collections.synchronizedList(new ArrayList<Bitmap>());
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public void setOutputSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * delay in ms
     */
    public void setFrameDelay(int delay) {
        frameDelay = delay;
    }

    public void startEncode() {
        bitmapQueue.clear();
        onStart();
        setState(STATE_RECORDING);
        encodingThread = new Thread(this.mRunnableEncoder);
        encodingThread.setName("EncodeThread");
        encodingThread.start();
    }

    private void notifyEncodeFinish() {
        if (encodeFinishListener != null) {
            encodeFinishListener.onEncodeFinished();
        }
    }

    public void stopEncode() {
        if (encodingThread != null && encodingThread.isAlive()) {
            encodingThread.interrupt();
        }
        setState(STATE_IDLE);
    }

    public void addFrame(Bitmap bitmap) {
        if (state != STATE_RECORDING) {

        } else {
            bitmapQueue.add(bitmap);
        }
    }

    public void setEncodeFinishListener(EncodeFinishListener listener) {
        encodeFinishListener = listener;
    }

    /**
     * Reserved for gif encoder
     */
    public void notifyLastFrameAdded() {
        setState(STATE_RECORDING_UNTIL_LAST_FRAME);
    }


    private void setState(int state) {
        this.state = state;
    }

    protected abstract void onAddFrame(Bitmap bitmap);

    protected abstract void onInit();

    protected abstract void onStart();

    protected abstract void onStop();

    protected int getFrameDelay() {
        return frameDelay;
    }

    protected int getHeight() {
        return height;
    }

    protected int getWidth() {
        return width;
    }

    protected EncodingOptions getEncodingOptions() {
        return encodingOptions;
    }
}