package com.infisense.usbir.thread;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import com.infisense.iruvc.sdkisp.LibIRParse;
import com.infisense.iruvc.sdkisp.LibIRProcess;
import com.infisense.iruvc.utils.CommonParams;
import com.infisense.iruvc.utils.SynchronizedBitmap;

import java.nio.ByteBuffer;

public class ImageThreadTC extends Thread {
    private String TAG = "ImageThread";
    private Bitmap bitmap;
    private SynchronizedBitmap syncimage;
    private int imageWidth;
    private int imageHeight;
    private byte[] imagesrc;
    private int rotate = 0;
    private CommonParams.PseudoColorType pseudocolorMode = CommonParams.PseudoColorType.PSEUDO_WHITE_HOT;
    private CommonParams.DataFlowMode dataFlowMode = CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT;
    private byte[] imageYUV422;
    private byte[] imageARGB;
    private byte[] imageDst;

    public void setSyncimage(SynchronizedBitmap syncimage) {
        this.syncimage = syncimage;
    }

    public void setImagesrc(byte[] imagesrc) {
        this.imagesrc = imagesrc;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public ImageThreadTC(int imageWidth, int imageHeight) {
        Log.i(TAG, "ImageThread create->imageWidth = " + imageWidth + " imageHeight = " + imageHeight);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        imageYUV422 = new byte[imageWidth * imageHeight * 2];
        imageARGB = new byte[imageWidth * imageHeight * 4];
        imageDst = new byte[imageWidth * imageHeight * 4];
    }

    public void setDataFlowMode(CommonParams.DataFlowMode dataFlowMode) {
        this.dataFlowMode = dataFlowMode;
    }

    public void setPseudocolorMode(CommonParams.PseudoColorType pseudocolorMode) {
        this.pseudocolorMode = pseudocolorMode;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            synchronized (syncimage.dataLock) {
                if (syncimage.start) {
                    Log.d(TAG, "run->dataFlowMode = " + dataFlowMode + " pseudocolorMode = " +
                            pseudocolorMode + " rotate = " + rotate + " syncimage.valid = " + syncimage.valid
                            + " imagesrc.length = " + imagesrc.length + " imagesrc[100] = " + imagesrc[100]);
                    if (dataFlowMode == CommonParams.DataFlowMode.IMAGE_AND_TEMP_OUTPUT ||
                            dataFlowMode == CommonParams.DataFlowMode.IMAGE_OUTPUT) {
                        if (pseudocolorMode != null) {
                            LibIRProcess.convertYuyvMapToARGBPseudocolor(imagesrc, (long) imageHeight * imageWidth, pseudocolorMode, imageARGB);
                        } else {
                            LibIRParse.converyArrayYuv422ToARGB(imagesrc, imageHeight * imageWidth, imageARGB);
                        }
                    } else {
                        LibIRParse.convertArrayY14ToYuv422(imagesrc, imageHeight * imageWidth, imageYUV422);
                        if (pseudocolorMode != null) {
                            LibIRProcess.convertYuyvMapToARGBPseudocolor(imageYUV422, (long) imageHeight * imageWidth, pseudocolorMode, imageARGB);
                        } else {
                            LibIRParse.converyArrayYuv422ToARGB(imageYUV422, imageHeight * imageWidth, imageARGB);
                        }
                    }
                    if (rotate == 270) {
                        LibIRProcess.ImageRes_t imageRes = new LibIRProcess.ImageRes_t();
                        imageRes.height = (char) imageWidth;
                        imageRes.width = (char) imageHeight;
                        LibIRProcess.rotateRight90(imageARGB, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, imageDst);
                    } else if (rotate == 90) {
                        LibIRProcess.ImageRes_t imageRes = new LibIRProcess.ImageRes_t();
                        imageRes.height = (char) imageWidth;
                        imageRes.width = (char) imageHeight;
                        LibIRProcess.rotateLeft90(imageARGB, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, imageDst);
                    } else if (rotate == 180) {
                        LibIRProcess.ImageRes_t imageRes = new LibIRProcess.ImageRes_t();
                        imageRes.height = (char) imageHeight;
                        imageRes.width = (char) imageWidth;
                        LibIRProcess.rotate180(imageARGB, imageRes, CommonParams.IRPROCSRCFMTType.IRPROC_SRC_FMT_ARGB8888, imageDst);
                    } else {
                        imageDst = imageARGB;
                    }
                }
            }

            synchronized (syncimage.viewLock) {
                if (!syncimage.valid) {
                    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageDst));
                    syncimage.valid = true;
                    syncimage.viewLock.notify();
                }
            }
            SystemClock.sleep(20);
        }
        Log.i(TAG, "ImageThread exit");
    }

}