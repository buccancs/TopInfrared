package com.topdon.module.thermal.ir.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import java.nio.ByteBuffer;

import static com.example.opengl.render.IROpen3DTools.IntArrayToByteArray;

/**
 * @author: CaiSongL
 * @date: 2023/8/18 11:45
 */
public class ImageColorTools {

    static {
//        new OpenCVNativeLoader().init();
        System.loadLibrary("opencv_java4");
    }

    public static Bitmap testImageTe(byte[] buffer){
        int[] temperature = new int[256 * 192 * 2];
        int[] image = new int[256 * 192 * 2];

        int img_num = 0;
        int te_num = 0;
        for (int i = 1024; i < (1024 + 256 * 192 * 2); i++) {
            image[img_num++] = buffer[i];
        }
        for (int i = 1024 + 256 * 192 * 2; i < (1024 + 2 * 256 * 192 * 2); i++) {
            temperature[te_num++] = buffer[i];
        }

        int[] r = {0, 0, 255};
        int[] g = {0, 255, 0};
        int[] b = {255, 0, 0};
        float[] tempt = {18.0f, 23.0f, 25.0f};

        float customMinTemp = 18f;
        float customMaxTemp = 25f;
        int num_of_tem = tempt.length;

        Mat src = new Mat(192, 256, CvType.CV_64F);
        double[] temp = new double[256 * 192];
        int t = 0;
        for (int i = 0; i < temperature.length; i += 2) {
            int value = (temperature[i + 1] << 8) + temperature[i];
            float divid = 16.0f;
            float gValue = (float) (value / 4.0) / divid - 273.15f;
            temp[t++] = gValue;
        }
        src.put(0, 0, temp);

        Mat imageMat = new Mat(192, 256, CvType.CV_8UC2);
        imageMat.put(0,0,IntArrayToByteArray(image));
//        for (int i = 0; i < image.length; i += 2) {
//            imageMat.put(i / 512, (i / 2) % 256, image[i]);
//            imageMat.put(i / 512, (i / 2) % 256 + 1, image[i + 1]);
//        }
        int[] colorList = new int[]{
                Color.parseColor("#ff0000"),
                Color.parseColor("#00ff00"),
                Color.parseColor("#0000ff")};
        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_YUV2GRAY_YUYV);
//        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_GRAY2BGRA);
        byte[] imageDst = matToByteArrayBy4(imageMat);
        double srcValue = 0.1f;
        long time = System.currentTimeMillis();
        Mat imageColor = new Mat(192, 256, CvType.CV_8UC3, new Scalar(255, 255, 255));
        long startTimeAll = System.currentTimeMillis();
        int j = 0;
        int imageDstLength = imageDst.length;
        // 遍历像素点，过滤温度阈值
        for (int index = 0; index < imageDstLength; ) {
            // 温度换算公式
            float temperature0 = (temperature[j] & 0xff) + (temperature[j + 1] & 0xff) * 256;
            temperature0 = (float) (temperature0 / 64 - 273.15);
            if (temperature0 >= customMinTemp && temperature0 <= customMaxTemp) {
                int[] rgb = getOneColorByTempEx(customMaxTemp,customMinTemp,temperature0,colorList);
                if (rgb!=null){
                    imageDst[index] = (byte) rgb[0];
                    imageDst[index + 1] = (byte) rgb[1];
                    imageDst[index + 2] = (byte) rgb[2];
                }
            }
            imageDst[index + 3] = (byte) 255;
            index += 4;
            j += 2;
        }
//        Log.e("执行耗时：",System.currentTimeMillis() - time+"//");
        // Convert OpenCV Mat to Android Bitmap
        Bitmap outputBitmap = Bitmap.createBitmap(256, 192, Bitmap.Config.ARGB_8888);
        outputBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageDst));
//        Bitmap outputBitmap = Bitmap.createBitmap(imageColor.cols(), imageColor.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(imageColor, outputBitmap);
        // Display or use the resulting Bitmap as needed
        // For example, you can set this Bitmap to an ImageView
        // imageView.setImageBitmap(outputBitmap);
        // Release Mats
        src.release();
        imageMat.release();
        imageColor.release();

        return outputBitmap;
    }
    public static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
        }
        return null;

    }
    public static Bitmap testImage2(byte[] buffer){
        int[] temperature = new int[256 * 192 * 2];
        int[] image = new int[256 * 192 * 2];

        int img_num = 0;
        int te_num = 0;
        for (int i = 1024; i < (1024 + 256 * 192 * 2); i++) {
            image[img_num++] = buffer[i];
        }
        for (int i = 1024 + 256 * 192 * 2; i < (1024 + 2 * 256 * 192 * 2); i++) {
            temperature[te_num++] = buffer[i];
        }

        int[] r = {0, 0, 255};
        int[] g = {0, 255, 0};
        int[] b = {255, 0, 0};
        float[] tempt = {18.0f, 23.0f, 25.0f};
        int num_of_tem = tempt.length;

        Mat src = new Mat(192, 256, CvType.CV_64F);
        double[] temp = new double[256 * 192];
        int t = 0;
        for (int i = 0; i < temperature.length; i += 2) {
            int value = (temperature[i + 1] << 8) + temperature[i];
            float divid = 16.0f;
            float gValue = (float) (value / 4.0) / divid - 273.15f;
            temp[t++] = gValue;
        }
        src.put(0, 0, temp);

        Mat imageMat = new Mat(192, 256, CvType.CV_8UC2);
        imageMat.put(0,0,IntArrayToByteArray(image));
//        for (int i = 0; i < image.length; i += 2) {
//            imageMat.put(i / 512, (i / 2) % 256, image[i]);
//            imageMat.put(i / 512, (i / 2) % 256 + 1, image[i + 1]);
//        }

        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_YUV2GRAY_YUYV);
        double srcValue = 0.1f;
        long time = System.currentTimeMillis();
        Mat imageColor = new Mat(192, 256, CvType.CV_8UC3, new Scalar(255, 255, 255));
        for (int i = 0; i < src.rows(); i++) {
            for (int j = 0; j < src.cols(); j++) {
                srcValue = src.get(i, j)[0];
                if (srcValue > tempt[num_of_tem - 1]) {
                    imageColor.put(i, j, 0, 0, 0);
                } else if (srcValue < tempt[0]) {
                    imageColor.put(i, j, 255, 255, 255);
                } else {
                    for (int m = 0; m < num_of_tem - 1; m++) {
                        if (srcValue >= tempt[m] && srcValue <= tempt[m + 1]) {
                            int rMax = Math.max(r[m], r[m + 1]);
                            int rMin = Math.min(r[m], r[m + 1]);
                            int rColor = (r[m] >= r[m + 1])
                                    ? rMax - (int) (((float) (rMax - rMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]))
                                    : (int) (((float) (rMax - rMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]) + rMin);

                            int gMax = Math.max(g[m], g[m + 1]);
                            int gMin = Math.min(g[m], g[m + 1]);
                            int gColor = (g[m] >= g[m + 1])
                                    ? gMax - (int) (((float) (gMax - gMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]))
                                    : (int) (((float) (gMax - gMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]) + gMin);

                            int bMax = Math.max(b[m], b[m + 1]);
                            int bMin = Math.min(b[m], b[m + 1]);
                            int bColor = (b[m] >= b[m + 1])
                                    ? bMax - (int) (((float) (bMax - bMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]))
                                    : (int) (((float) (bMax - bMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]) + bMin);

                            imageColor.put(i, j, bColor, gColor, rColor);
                        }
                    }
                }
            }
        }
        Log.e("执行耗时：",System.currentTimeMillis() - time+"//");
//        Imgproc.cvtColor(imageColor, imageColor, Imgproc.COLOR_BGR2RGBA);
//        byte[] imageDst = matToByteArray(imageColor);
//        Bitmap outputBitmap = Bitmap.createBitmap(imageColor.width(),
//                imageColor.height(), Bitmap.Config.ARGB_8888);
//        outputBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageDst));
        // Convert OpenCV Mat to Android Bitmap
        Bitmap outputBitmap = Bitmap.createBitmap(imageColor.cols(), imageColor.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageColor, outputBitmap);
        // Display or use the resulting Bitmap as needed
        // For example, you can set this Bitmap to an ImageView
        // imageView.setImageBitmap(outputBitmap);
        // Release Mats
        src.release();
        imageMat.release();
        imageColor.release();

        return outputBitmap;
    }
    public static Bitmap testImage(byte[] buffer){
        int[] temperature = new int[256 * 192 * 2];
        byte[] image = new byte[256 * 192 * 2];

        int img_num = 0;
        int te_num = 0;
        for (int i = 1024; i < (1024 + 256 * 192 * 2); i++) {
            image[img_num++] = buffer[i];
        }
        for (int i = 1024 + 256 * 192 * 2; i < (1024 + 2 * 256 * 192 * 2); i++) {
            temperature[te_num++] = buffer[i];
        }

        int[] r = {0, 0, 255};
        int[] g = {0, 255, 0};
        int[] b = {255, 0, 0};
        float[] tempt = {18.0f, 23.0f, 25.0f};
        int num_of_tem = tempt.length;

        Mat src = new Mat(192, 256, CvType.CV_64F);
        double[] temp = new double[256 * 192];
        int t = 0;
        for (int i = 0; i < temperature.length; i += 2) {
            int value = (temperature[i + 1] << 8) + temperature[i];
            float divid = 16.0f;
            float gValue = (float) (value / 4.0) / divid - 273.15f;
            temp[t++] = gValue;
        }
        src.put(0, 0, temp);

        Mat imageMat = new Mat(192, 256, CvType.CV_8UC2);
        imageMat.put(0,0,image);
//        for (int i = 0; i < image.length; i += 2) {
//            imageMat.put(i / 512, (i / 2) % 256, image[i]);
//            imageMat.put(i / 512, (i / 2) % 256 + 1, image[i + 1]);
//        }

        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_YUV2GRAY_YUYV);
//        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2RGB);
        double srcValue = 0.1f;
        long time = System.currentTimeMillis();
//        Mat imageColor = new Mat(192, 256, CvType.CV_8UC3, new Scalar(255, 255, 255));
//        for (int i = 0; i < src.rows(); i++) {
//            for (int j = 0; j < src.cols(); j++) {
//                srcValue = src.get(i, j)[0];
//                if (srcValue > tempt[num_of_tem - 1]) {
//                    imageColor.put(i, j, 0, 0, 0);
//                } else if (srcValue < tempt[0]) {
//                    imageColor.put(i, j, 255, 255, 255);
//                } else {
//                    for (int m = 0; m < num_of_tem - 1; m++) {
//                        if (srcValue >= tempt[m] && srcValue <= tempt[m + 1]) {
//                            int rMax = Math.max(r[m], r[m + 1]);
//                            int rMin = Math.min(r[m], r[m + 1]);
//                            int rColor = (r[m] >= r[m + 1])
//                                    ? rMax - (int) (((float) (rMax - rMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]))
//                                    : (int) (((float) (rMax - rMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]) + rMin);
//
//                            int gMax = Math.max(g[m], g[m + 1]);
//                            int gMin = Math.min(g[m], g[m + 1]);
//                            int gColor = (g[m] >= g[m + 1])
//                                    ? gMax - (int) (((float) (gMax - gMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]))
//                                    : (int) (((float) (gMax - gMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]) + gMin);
//
//                            int bMax = Math.max(b[m], b[m + 1]);
//                            int bMin = Math.min(b[m], b[m + 1]);
//                            int bColor = (b[m] >= b[m + 1])
//                                    ? bMax - (int) (((float) (bMax - bMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]))
//                                    : (int) (((float) (bMax - bMin) / (tempt[m + 1] - tempt[m])) * (srcValue - tempt[m]) + bMin);
//
//                            imageColor.put(i, j, bColor, gColor, rColor);
//                        }
//                    }
//                }
//            }
//        }
        Log.e("执行耗时：",System.currentTimeMillis() - time+"//");
//        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2RGBA);
//        byte[] imageDst = matToByteArray(imageMat);
//        Bitmap outputBitmap = Bitmap.createBitmap(imageMat.width(),
//                imageMat.height(), Bitmap.Config.ARGB_8888);
//        outputBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageDst));
        // Convert OpenCV Mat to Android Bitmap
        Bitmap outputBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imageMat, outputBitmap,true);
        // Display or use the resulting Bitmap as needed
        // For example, you can set this Bitmap to an ImageView
        // imageView.setImageBitmap(outputBitmap);
        // Release Mats
        src.release();
        imageMat.release();
//        imageColor.release();

        return outputBitmap;
    }

    public static Bitmap  matToBitmap(Mat mat){
        Bitmap outputBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, outputBitmap,true);
        mat.release();
        return outputBitmap;
    }







    public static int[] getOneColorByTempEx(float customMaxTemp, float customMinTemp, float nowTemp, int[] colorList) {
        if (colorList == null){
            return null;
        }
        float tempValue = customMaxTemp - customMinTemp;
        float ratio = (nowTemp - customMinTemp) / tempValue;

        int[] result = new int[3];
        int colorNumber = colorList.length - 1;
        float avg = 1.f / colorNumber;

        float avgColorIndex = ratio / avg;
        int addNumber = 0;
        if ((ratio % avg) > 0){
            addNumber = 1;
        }
        int lowerColorIndex = (int) avgColorIndex + addNumber;
        float ratioInRegion = avgColorIndex - lowerColorIndex;

        if (Math.abs(nowTemp -customMaxTemp)==0.1f) {
            int lastColor = colorList[colorNumber];
            result[0] = (lastColor >> 16) & 0xFF;
            result[1] = (lastColor >> 8) & 0xFF;
            result[2] = lastColor & 0xFF;
            return result;
        } else if (Math.abs(nowTemp -customMinTemp)==0.1f) {
            int firstColor = colorList[0];
            result[0] = (firstColor >> 16) & 0xFF;
            result[1] = (firstColor >> 8) & 0xFF;
            result[2] = firstColor & 0xFF;
            return result;
        }
        int startColor = colorList[lowerColorIndex - 1];
        int endColor = colorList[lowerColorIndex];
        if (colorNumber != 1){
            ratioInRegion = (ratio - (avg * (lowerColorIndex - 1))) / avg;
        }
        int r = interpolateR(startColor, endColor, ratioInRegion);
        int g = interpolateG(startColor, endColor, ratioInRegion);
        int b = interpolateB(startColor, endColor, ratioInRegion);
        result[0] = r;
        result[1] = g;
        result[2] = b;
        return result;
    }


    private static int interpolateR(int startColor, int endColor, double ratio) {
        int startR = (startColor >> 16) & 0xFF;
        int endR = (endColor >> 16) & 0xFF;
        int red = (int) ((1 - ratio) * startR + ratio * endR);
        return red;
    }
    private static int interpolateG(int startColor, int endColor, double ratio) {
        int startG = (startColor >> 8) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int interpolatedG = (int) ((1 - ratio) * startG + ratio * endG);
        return interpolatedG;
    }
    private static int interpolateB(int startColor, int endColor, double ratio) {
        int startB = startColor & 0xFF;
        int endB = endColor & 0xFF;
        int interpolatedB = (int) ((1 - ratio) * startB + ratio * endB);
        return interpolatedB;
    }
    public static byte[] matToByteArrayBy4(Mat mat) {
        int rows = mat.rows();
        int cols = mat.cols();
        int type = mat.type();
        byte[] byteArray = new byte[rows * cols * 4];
        mat.get(0, 0, byteArray);
        return byteArray;
    }
    public static byte[] matToByteArrayBy3(Mat mat) {
        int rows = mat.rows();
        int cols = mat.cols();
        int type = mat.type();
        byte[] byteArray = new byte[rows * cols * 3];
        mat.get(0, 0, byteArray);
        return byteArray;
    }
    public static Bitmap bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}
