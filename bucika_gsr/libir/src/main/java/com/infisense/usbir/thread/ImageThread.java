//package com.infisense.usbir.thread;
//
//import android.graphics.Bitmap;
//import android.os.SystemClock;
//import android.util.Log;
//
//import com.infisense.iruvc.sdkisp.Libirparse;
//import com.infisense.iruvc.sdkisp.Libirprocess;
//import com.infisense.iruvc.utils.SynchronizedBitmap;
//
//import java.nio.ByteBuffer;
//
//public class ImageThread extends Thread {
//
//    private static final int TYPE_TINY1B = 1;
//    private static final int TYPE_TINY1C = 0;
//    private String TAG = "ImageThread";
//    private Bitmap bitmap;
//    private SynchronizedBitmap syncImage;
//    private int imageWidth;
//    private int imageHeight;
//    private byte[] imageSrc;
//    private boolean rotate = false;
//
//    public void setSyncimage(SynchronizedBitmap syncimage) {
//        this.syncImage = syncimage;
//    }
//
//    public void setImageSrc(byte[] imageSrc) {
//        this.imageSrc = imageSrc;
//    }
//
//    public void setRotate(boolean rotate) {
//        this.rotate = rotate;
//    }
//
//
//    public int pseudocolorMode = Libirprocess.IRPROC_COLOR_MODE_0;
//
//    public ImageThread(int imageWidth, int imageHeight) {
//        this.imageWidth = imageWidth;
//        this.imageHeight = imageHeight;
//    }
//
//    public void setPseudocolorMode(int pseudocolorMode) {
//        this.pseudocolorMode = pseudocolorMode;
//    }
//
//    public void setBitmap(Bitmap bitmap) {
//        this.bitmap = bitmap;
//    }
//
//    @Override
//    public void run() {
//        byte[] imagertemp1 = new byte[imageWidth * imageHeight * 2];
//        byte[] imagertemp2 = new byte[imageWidth * imageHeight * 4];
//        byte[] imagedst = new byte[imageWidth * imageHeight * 4];
//        while (!isInterrupted()) {
//
//            synchronized (syncImage.dataLock) {
//                if (syncImage.start) {
//
//                    //uvc Width,Height
//
//                /*
//                imageprocess(imagertemp1, imagertemp2, imageRes);
//
//                if(pseudocolorMode!=0) {
//                    Libirprocess.yuyv_map_to_argb_pseudocolor(imageSrc, imageHeight * imageWidth, pseudocolorMode, imagedst);
//                }else {
//                    Libirparse.yuv422_to_argb(imageSrc,imageHeight*imageWidth,imagedst);
//                }
//                 */
//                    if (pseudocolorMode != 0) {
//                        Libirprocess.yuyv_map_to_argb_pseudocolor(imageSrc, imageHeight * imageWidth, pseudocolorMode, imagedst);
//                    } else {
//                        Libirparse.yuv422_to_argb(imageSrc, imageHeight * imageWidth, imagedst);
//                    }
//                    //Libirprocess.rotate_180(image,imageRes,Libirprocess.IRPROC_SRC_FMT_Y14,imager180);
//                    //Libirprocess.y14_map_to_yuyv_pseudocolor(imageSrc,imageHeight*imageWidth,Libirprocess.IRPROC_COLOR_MODE_3,imagertemp2);
//
//                    //Libirparse.yuv422_to_argb(imager180,imageHeight*imageWidth,imagergb);
//
//                    if (syncImage.type == TYPE_TINY1B) {
//                        Libirparse.y14_to_yuv422(imageSrc, imageHeight * imageWidth, imagertemp1);
//                        //Libirparse.yuv422_to_argb(imagertemp2, imageHeight * imageWidth, imagertemp1);
//                        //Libirprocess.y14_map_to_yuyv_pseudocolor(imageSrc,imageHeight*imageWidth,Libirprocess.IRPROC_COLOR_MODE_1,imagertemp2);
//                        //Libirparse.yuv422_to_argb(imagertemp2,imageHeight*imageWidth,imagertemp1);
//                        //Libirparse.y14_to_argb(imageSrc, imageHeight * imageWidth, imagertemp1);
//
//                    } else {
//                        imagertemp1 = imageSrc;
//                    }
//
//                    if (pseudocolorMode != 0) {
//                        Libirprocess.yuyv_map_to_argb_pseudocolor(imagertemp1, imageHeight * imageWidth, pseudocolorMode, imagertemp2);
//                    } else {
//                        Libirparse.yuv422_to_argb(imagertemp1, imageHeight * imageWidth, imagertemp2);
//                    }
//
//                    if (rotate) {
//                        Libirprocess.ImageRes_t imageRes = new Libirprocess.ImageRes_t();
//                        imageRes.height = (char) imageWidth;
//                        imageRes.width = (char) imageHeight;
//                        Libirprocess.rotate_right_90(imagertemp2, imageRes, Libirprocess.IRPROC_SRC_FMT_ARGB8888, imagedst);
//                    } else imagedst = imagertemp2;
//                }
//            }
//
//            //jpegBytes = PixelFormatConverter.yuv422ToJpeg(pseudoImage, imageWidth, imageHeight);
//
//            synchronized (syncImage.viewLock) {
//                if (syncImage.valid == false) {
//                    // bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
//                    //bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pseudoImage));
//                    bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imagedst));
//                    syncImage.valid = true;
//                    syncImage.viewLock.notify();
//                }
//            }
//            SystemClock.sleep(20);
//        }
//        Log.w(TAG, "ImageThread exit:");
//    }
//
//    private void imageprocess(byte[] src, byte[] dst, Libirprocess.ImageRes_t imageRes) {
//        imageRes.height = (char) imageHeight;
//        imageRes.width = (char) imageWidth;
//        Libirprocess.rotate_right_90(imageSrc, imageRes, Libirprocess.IPROC_SRC_FMT_YUV422, src);
//        imageRes.height = (char) imageWidth;
//        imageRes.width = (char) imageHeight;
//        Libirprocess.mirror(src, imageRes, Libirprocess.IRPROC_SRC_FMT_Y14, dst);
//    }
//}