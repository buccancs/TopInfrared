//package com.infisense.usbir.thread;
//
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.os.SystemClock;
//import android.util.Log;
//
//import com.elvishew.xlog.XLog;
//import com.infisense.iruvc.utils.SynchronizedBitmap;
//import com.infisense.usbir.tools.BitmapTools;
//import com.infisense.usbir.tools.ImageTools;
//
//import java.nio.ByteBuffer;
//
///**
// * bytes -> bitmap
// * 将源数据转出图像照片
// */
//public class ImageThreadTCOld extends Thread {
//
//    private static final int TYPE_TINY1B = 1;
//    private static final int TYPE_TINY1C = 0;
//    private final String TAG = "ImageThread";
//    private Bitmap bitmap;
//    private SynchronizedBitmap syncImage;
//    private int imageWidth;
//    private int imageHeight;
//    private byte[] imageSrc;
//    private byte[] temperatureSrc;//温度源数据
//    private int rotate = 0;
//    private float max = Float.MAX_VALUE;
//    private float min = Float.MIN_VALUE;
//    private int maxColor = 0;
//    private int minColor = 0;
//
//    public void setSyncImage(SynchronizedBitmap syncImage) {
//        this.syncImage = syncImage;
//    }
//
//    public void setImageSrc(byte[] imageSrc) {
//        this.imageSrc = imageSrc;
//    }
//
//    public void setTemperatureSrc(byte[] temperatureSrc) {
//        this.temperatureSrc = temperatureSrc;
//    }
//
//    public void setRotate(int rotate) {
//        this.rotate = rotate;
//    }
//    public byte[] imageDst = null;
//
//
//    public void setLimit(float max, float min) {
//        this.max = max;
//        this.min = min;
//    }
//
//    public void setLimit(float max, float min, int maxColor, int minColor) {
//        this.max = max;
//        this.min = min;
//        this.maxColor = maxColor;
//        this.minColor = minColor;
//    }
//
//    public int pseudoColorMode = Libirprocess.IRPROC_COLOR_MODE_0;
//
//    public ImageThreadTCOld(int imageWidth, int imageHeight) {
//        this.imageWidth = imageWidth;
//        this.imageHeight = imageHeight;
//    }
//
//    public void setPseudoColorMode(int pseudoColorMode) {
//        this.pseudoColorMode = pseudoColorMode;
//    }
//
//    public void setBitmap(Bitmap bitmap) {
//        this.bitmap = bitmap;
//    }
//
//    @Override
//    public void run() {
//        byte[] imagerTemp1 = new byte[imageWidth * imageHeight * 2];
//        byte[] imagerTemp2 = new byte[imageWidth * imageHeight * 4];
//        imageDst = new byte[imageWidth * imageHeight * 4];
//        while (!isInterrupted()) {
//
//            synchronized (syncImage.dataLock) {
//                if (syncImage.start) {
//
//                    //uvc Width,Height
//
//                /*
//                imageprocess(imagerTemp1, imagerTemp2, imageRes);
//
//                if(pseudocolorMode!=0) {
//                    Libirprocess.yuyv_map_to_argb_pseudocolor(imageSrc, imageHeight * imageWidth, pseudocolorMode, imageDst);
//                }else {
//                    Libirparse.yuv422_to_argb(imageSrc,imageHeight*imageWidth,imageDst);
//                }
//                 */
//                    if (pseudoColorMode != 0) {
//                        Libirprocess.yuyv_map_to_argb_pseudocolor(imageSrc, (long) imageHeight * imageWidth, pseudoColorMode, imageDst);
//                    } else {
//                        Libirparse.yuv422_to_argb(imageSrc, imageHeight * imageWidth, imageDst);
//                    }
//                    //Libirprocess.rotate_180(image,imageRes,Libirprocess.IRPROC_SRC_FMT_Y14,imager180);
//                    //Libirprocess.y14_map_to_yuyv_pseudocolor(imageSrc,imageHeight*imageWidth,Libirprocess.IRPROC_COLOR_MODE_3,imagerTemp2);
//
//                    //Libirparse.yuv422_to_argb(imager180,imageHeight*imageWidth,imagergb);
//
//                    if (syncImage.type == TYPE_TINY1B) {
//                        Libirparse.y14_to_yuv422(imageSrc, imageHeight * imageWidth, imagerTemp1);
//                        //Libirparse.yuv422_to_argb(imagerTemp2, imageHeight * imageWidth, imagerTemp1);
//                        //Libirprocess.y14_map_to_yuyv_pseudocolor(imageSrc,imageHeight*imageWidth,Libirprocess.IRPROC_COLOR_MODE_1,imagerTemp2);
//                        //Libirparse.yuv422_to_argb(imagerTemp2,imageHeight*imageWidth,imagerTemp1);
//                        //Libirparse.y14_to_argb(imageSrc, imageHeight * imageWidth, imagerTemp1);
//
//                    } else {
//                        imagerTemp1 = imageSrc;
//                    }
//
//                    if (pseudoColorMode != 0) {
//                        Libirprocess.yuyv_map_to_argb_pseudocolor(imagerTemp1, (long) imageHeight * imageWidth, pseudoColorMode, imagerTemp2);
//                    } else {
//                        Libirparse.yuv422_to_argb(imagerTemp1, imageHeight * imageWidth, imagerTemp2);
//                    }
//
////                    // imagerTemp2二次处理 (温度原始数据)
////                    if (max != 0 && min != 0) {
////                        ImageTools.INSTANCE.readFrame(imagerTemp2, temperatureSrc, max, min);
////                    }
//
//                    if (rotate == 270) {
//                        Libirprocess.ImageRes_t imageRes = new Libirprocess.ImageRes_t();
//                        imageRes.height = (char) imageWidth;
//                        imageRes.width = (char) imageHeight;
//                        Libirprocess.rotate_right_90(imagerTemp2, imageRes, Libirprocess.IRPROC_SRC_FMT_ARGB8888, imageDst);
//                    } else if (rotate == 90) {
//                        Libirprocess.ImageRes_t imageRes = new Libirprocess.ImageRes_t();
//                        imageRes.height = (char) imageWidth;
//                        imageRes.width = (char) imageHeight;
//                        Libirprocess.rotate_left_90(imagerTemp2, imageRes, Libirprocess.IRPROC_SRC_FMT_ARGB8888, imageDst);
//                    } else if (rotate == 180) {
//                        Libirprocess.ImageRes_t imageRes = new Libirprocess.ImageRes_t();
//                        imageRes.height = (char) imageHeight;
//                        imageRes.width = (char) imageWidth;
//                        Libirprocess.rotate_180(imagerTemp2, imageRes, Libirprocess.IRPROC_SRC_FMT_ARGB8888, imageDst);
//                    } else {
//                        imageDst = imagerTemp2;
//                    }
//                }
//            }
//
//            //jpegBytes = PixelFormatConverter.yuv422ToJpeg(pseudoImage, imageWidth, imageHeight);
//
//            // imagerTemp2二次处理 (温度旋转后数据)
//            if (max != Float.MAX_VALUE || min != Float.MIN_VALUE ) {
//                // 当不设高温，只设置低温时
//                if (max == -273) {
//                    // 替换颜色的方法里最高温不能低于最低温
//                    max = 1000000;
//                }
//                //FF808080固定触发
//                if (maxColor == Color.parseColor("#FF808080") && minColor == Color.parseColor("#FF808080")) {
//                    ImageTools.INSTANCE.readFrame(imageDst, temperatureSrc, max, min);//替换灰度处理
//                } else {
//                    Log.w("123", "max:" + max + ", min: " + min);
////                    ImageTools.INSTANCE.readFrame(imageDst, temperatureSrc, max, min,maxColor,minColor);//替换颜色处理
//                    BitmapTools.INSTANCE.replaceBitmapColor(imageDst, temperatureSrc, max, min,0,0);//替换颜色处理
//                }
//                Log.w("原始图像:", imageDst.toString());
//            }
//            synchronized (syncImage.viewLock) {
//                if (!syncImage.valid) {
//                    if (bitmap != null) {
//                        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageDst)); //bitmap图像刷新数据
//                    } else {
//                        XLog.e("ImageThreadTC copyPixelsFromBuffer(): bitmap is null");
//                    }
//                    syncImage.valid = true;
//
//                    syncImage.viewLock.notify();
//                }
//            }
//            try {
//                SystemClock.sleep(20);
//            } catch (Exception e) {
//                XLog.e("Image Thread刷新异常: " + e.getMessage());
//            }
//        }
//        Log.w(TAG, "ImageThread exit:");
//    }
//
//    public Bitmap getBitmap() {
//        return bitmap;
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