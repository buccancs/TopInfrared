package com.infisense.usbir.utils;

import static org.bytedeco.opencv.global.opencv_imgproc.MORPH_ELLIPSE;
import static org.opencv.core.Core.BORDER_CONSTANT;
import static org.opencv.core.Core.DFT_SCALE;
import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.Core.absdiff;
import static org.opencv.core.Core.getOptimalDFTSize;
import static org.opencv.core.Core.magnitude;
import static org.opencv.core.Core.normalize;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.core.CvType.CV_32FC1;
import static org.opencv.core.CvType.CV_32FC2;
import static org.opencv.core.CvType.CV_64FC1;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC2;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2BGR;
import static org.opencv.imgproc.Imgproc.COLOR_YUV2GRAY_YUYV;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.RETR_EXTERNAL;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;
import static org.opencv.imgproc.Imgproc.applyColorMap;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.threshold;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.suplib.wrapper.SupHelp;
import com.topdon.lib.core.BaseApplication;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class OpencvTools {

    static {
        System.loadLibrary("opencv_java4");
    }

    private static Mat resultMat = new Mat();
    public static byte[] supImageMix(byte[] imageARGB, int width, int height, byte[] resulARGB) {
        Mat argbMat = new Mat(width, height, CvType.CV_8UC4);
        argbMat.put(0, 0, imageARGB);
        Mat downscaledMat = new Mat();
        Imgproc.resize(argbMat, downscaledMat, new Size(height / 2, width / 2));
        Mat bgrMat = new Mat();
        Imgproc.cvtColor(downscaledMat, bgrMat, Imgproc.COLOR_RGBA2BGR);
        try {
            SupHelp.getInstance().runImage(bgrMat, resultMat);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Mat resulArgbMat = new Mat();
        Imgproc.cvtColor(resultMat, resulArgbMat, Imgproc.COLOR_BGR2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(resulArgbMat.width(), resulArgbMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resulArgbMat, dstBitmap);
        ByteBuffer byteBuffer = ByteBuffer.wrap(resulARGB);
        dstBitmap.copyPixelsToBuffer(byteBuffer);
        return resulARGB;
    }

    public static Bitmap supImageFour(Bitmap inBitmap){
        long startTime = System.currentTimeMillis();
        ByteBuffer rawData = ByteBuffer.wrap(SupRUtils.INSTANCE.bitmapToByteArray(inBitmap));
        ByteBuffer dataIn = ByteBuffer.allocateDirect(rawData.array().length);
        dataIn.put(rawData);
        ByteBuffer dataOut = ByteBuffer.allocateDirect(rawData.array().length * 4);
        SupHelp.getInstance().imgUpScalerFour(BaseApplication.instance,dataIn,dataOut);
        byte[] byteArray = new byte[dataOut.capacity()];
        dataOut.get(byteArray);
        Log.e("4倍超分模型：", String.valueOf((System.currentTimeMillis() - startTime)));
        return SupRUtils.INSTANCE.byteArrayToBitmap(byteArray);
    }

    public static byte[] supImageFourExToByte(byte[] imgByte) {
        long startTime = System.currentTimeMillis();
        ByteBuffer dataIn = ByteBuffer.wrap(imgByte);
        ByteBuffer dataOut = ByteBuffer.allocateDirect(imgByte.length * 4);
        SupHelp.getInstance().imgUpScalerFour(BaseApplication.instance, dataIn, dataOut);
        Log.e("AI_UPSCALE 4倍超分模型2：", String.valueOf((System.currentTimeMillis() - startTime)));
        byte[] outputData = new byte[dataOut.capacity()];
        dataOut.get(outputData);
        Log.e("4倍超分模型：", String.valueOf((System.currentTimeMillis() - startTime)));
        Bitmap bitmap = SupRUtils.INSTANCE.byteArrayToBitmap(outputData);
        return outputData;
    }
    public static Bitmap supImageFourExToBitmap(byte[] dstArgbBytes, int width, int height) {
        long startTime = System.currentTimeMillis();

        ByteBuffer dataIn = ByteBuffer.allocateDirect(dstArgbBytes.length);
        dataIn.put(dstArgbBytes);

        ByteBuffer dataOut = ByteBuffer.allocateDirect(dstArgbBytes.length * 4);

        SupHelp.getInstance().imgUpScalerFour(BaseApplication.instance, dataIn, dataOut);
        Log.e("AI_UPSCALE 4倍超分模型2：", String.valueOf((System.currentTimeMillis() - startTime)) + "////" + dstArgbBytes.length);

        byte[] outputData = new byte[dataOut.capacity()];
        dataOut.get(outputData);

        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        outputBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(outputData));

        Mat srcMat = new Mat();
        Utils.bitmapToMat(outputBitmap, srcMat);

        Mat dstMat = new Mat();
        Imgproc.resize(srcMat, dstMat, new org.opencv.core.Size(srcMat.cols() * 4, srcMat.rows() * 4));

        Bitmap finalBitmap = Bitmap.createBitmap(dstMat.cols(), dstMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dstMat, finalBitmap);

        srcMat.release();
        dstMat.release();
        Log.e("4倍超分模型：", String.valueOf((System.currentTimeMillis() - startTime)));

        return finalBitmap;
    }

    public static Bitmap supImageFourExToBitmap(Bitmap inBitmap) {
        long startTime = System.currentTimeMillis();
        byte[] rawData = SupRUtils.INSTANCE.bitmapToByteArray(inBitmap);
        ByteBuffer dataIn = ByteBuffer.allocateDirect(rawData.length);
        dataIn.put(rawData);
        ByteBuffer dataOut = ByteBuffer.allocateDirect(256 * 192 * 4 * 4);
        SupHelp.getInstance().imgUpScalerFour(BaseApplication.instance, dataIn, dataOut);
        Log.e("AI_UPSCALE 4倍超分模型2：", String.valueOf((System.currentTimeMillis() - startTime))+"////"+rawData.length);
        byte[] outputData = new byte[dataOut.capacity()];
        dataOut.get(outputData);
        Bitmap outputBitmap = SupRUtils.INSTANCE.byteArrayToBitmap(outputData);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(outputBitmap, srcMat);
        Mat dstMat = new Mat();
        Imgproc.resize(srcMat, dstMat, new org.opencv.core.Size(srcMat.cols() * 4, srcMat.rows() * 4));
        Bitmap finalBitmap = Bitmap.createBitmap(dstMat.cols(), dstMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dstMat, finalBitmap);
        srcMat.release();
        dstMat.release();
        Log.e("4倍超分模型：", String.valueOf((System.currentTimeMillis() - startTime)));
        return finalBitmap;
    }

    public static byte[] supImage(byte[] imageARGB, int width, int height, byte[] resulARGB){
        Mat argbMat = new Mat(width, height, CvType.CV_8UC4);
        argbMat.put(0, 0, imageARGB);
        Mat bgrMat = new Mat();
        Imgproc.cvtColor(argbMat, bgrMat, Imgproc.COLOR_RGBA2BGR);
        try {
            SupHelp.getInstance().runImage(bgrMat,resultMat);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Mat resulArgbMat = new Mat();
        Imgproc.cvtColor(resultMat, resulArgbMat, Imgproc.COLOR_BGR2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(resulArgbMat.width(), resulArgbMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resulArgbMat, dstBitmap);
        ByteBuffer byteBuffer = ByteBuffer.wrap(resulARGB);
        dstBitmap.copyPixelsToBuffer(byteBuffer);
        return resulARGB;
    }

    public static byte[] convertSingleByteToDoubleByte(byte[] singleByteImage) {
        if (singleByteImage == null) {
            throw new IllegalArgumentException("输入的byte数组不能为null");
        }
        int singleLength = singleByteImage.length;
        int doubleLength = singleLength * 2;
        byte[] doubleByteImage = new byte[doubleLength];

        for (int i = 0; i < singleLength; i++) {
            doubleByteImage[2 * i] = singleByteImage[i];
        }
        return doubleByteImage;
    }

    public static byte[] convertCelsiusToOriginalBytes(float[] temp) {
        if (temp == null) {
            return new byte[0];
        }
        float maxValue = 0f;
        byte[] temperature = new byte[temp.length * 2];
        for (int i = 0, j = 0; i < temp.length; i++, j += 2) {
            if (maxValue < temp[i]){
                maxValue = temp[i];
            }
            float tempInKelvin = temp[i] + 273.15f;
            float originalValue = tempInKelvin * 64;
            int intValue = (int) originalValue;
            byte low = (byte) (intValue & 0xFF);
            byte high = (byte) ((intValue >> 8) & 0xFF);
            temperature[j] = low;
            temperature[j + 1] = high;
        }
        return temperature;
    }

    public static LinkedHashMap<Integer, int[]> getColorByTemp(float customMaxTemp, float customMinTemp, int[] colorList){
        float temp = 0.1f;
        float tempValue = customMaxTemp - customMinTemp;
        LinkedHashMap<Integer, int[]> map = new LinkedHashMap<>();
        int r;
        int g;
        int b;
        for (float i = customMinTemp;i <= customMaxTemp; i+=temp){
            long time = System.currentTimeMillis();
            float ratio = (i - customMinTemp) / tempValue;
            int colorNumber = colorList.length - 1;
            float avg = 1.f / colorNumber;
            int colorIndex = colorNumber;
            for (int index = 1; index <= colorNumber;index++){
                if (ratio == 0){
                    colorIndex = 0;
                    break;
                }
                if (ratio < (avg * index)){
                    colorIndex = index;
                    break;
                }
            }
            ratio = (ratio - (avg * (colorIndex - 1))) / avg;
            r = interpolateR(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
            g = interpolateG(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
            b = interpolateB(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
            int intKey = (int) (i * 10);
            int[] rgb = new int[]{r,g,b};
            map.put(intKey, rgb);
        }
        return map;
    }

    public static byte[] matToByteArray(Mat mat) {
        int rows = mat.rows();
        int cols = mat.cols();
        int type = mat.type();
        byte[] byteArray = new byte[rows * cols * 4];
        mat.get(0, 0, byteArray);
        return byteArray;
    }
    public static Mat pseudoColorViewThree(byte[] image, int cols, int rows,
                                      int customMinColor, int customMiddleColor, int customMaxColor,
                                      float maxTemp,float minTemp,float customMaxTemp,float customMinTemp,
                                      boolean isGrayUse){
        Mat im;
        im = new Mat(rows, cols, CvType.CV_8UC4);
        im.put(0, 0, image);
        cvtColor(im, im, Imgproc.COLOR_RGBA2GRAY);
        Mat colorMat = generateColorBarThree(customMinColor,customMiddleColor,customMaxColor,
                maxTemp,minTemp,customMaxTemp,customMinTemp,isGrayUse);
        applyColorMap(im, im, colorMat);
        Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2RGBA);
        return im;
    }

    public static Mat pseudoColorView(byte[] image, int cols, int rows, int[] colorList,
                                      float maxTemp,float minTemp,float customMaxTemp,float customMinTemp,
                                      boolean isGrayUse){
        Mat im;
        im = new Mat(rows, cols, CV_8UC2);
        im.put(0, 0, image);
        cvtColor(im, im, COLOR_YUV2GRAY_YUYV);
        normalize(im, im, 0, 255, NORM_MINMAX);
        Mat colorMat = generateColorBar(colorList, maxTemp,minTemp,customMaxTemp,customMinTemp,isGrayUse);
        if (colorMat!=null){
            applyColorMap(im, im, colorMat);
            Imgproc.cvtColor(im, im, Imgproc.COLOR_BGR2RGBA);
        }
        return im;
    }

    private static Mat draw_high_temp_edge_argb_pse(byte[] image, byte[] temperature, Bitmap lut, int cols, int rows, double high_t, int color_h, int type) throws IOException {
        double[] temp = new double[cols * rows];
        int t = 0;
        for (int i = 0; i < temperature.length; i++) {
            if (i % 2 == 0) {
                float temperature0 = (temperature[i] & 0xff) + (temperature[i + 1] & 0xff) * 256;
                temperature0 = (float) (temperature0 / 64 - 273.15);
                temp[t] = temperature0;
                t++;
            }
        }
        Mat im;
        im = new Mat(rows, cols, CvType.CV_8UC4);
        im.put(0, 0, image);
        cvtColor(im, im, Imgproc.COLOR_RGBA2GRAY);
        normalize(im, im, 0, 255, NORM_MINMAX);
        im.convertTo(im, CV_8UC1);
        Mat colorMat = new Mat();
        Utils.bitmapToMat(lut, colorMat);
        Imgproc.cvtColor(colorMat, colorMat, Imgproc.COLOR_RGBA2BGR);
        Size colorSize = new Size(1.0, 256.0);
        Imgproc.resize(colorMat, colorMat, colorSize);
        applyColorMap(im, im, colorMat);
        Mat tem;
        tem = new Mat(rows, cols, CV_64FC1);
        tem.put(0, 0, temp);
        tem.convertTo(tem, CV_8UC1);
        Mat thres_gray = new Mat();
        threshold(tem, thres_gray, high_t, 255, THRESH_BINARY);
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(thres_gray, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<Rect> rects = new ArrayList<Rect>();
        String B = Integer.toString(color_h & 255, 2);
        int b = Integer.parseInt(B, 2);
        int gc = color_h >> 8;
        String G = Integer.toString(gc & 255, 2);
        int g = Integer.parseInt(G, 2);
        int rc = color_h >> 16;
        String R = Integer.toString(rc & 255, 2);
        int r = Integer.parseInt(R, 2);
        Scalar color = new Scalar(b, g, r);
        for (int i = 0; i < cnts.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);
            double area = contourArea(points);
            if (area > 50) {
                if (type == 1) {
                    drawContours(im, cnts, i, color, 1, 8);
                } else {
                    rectangle(im, rect.tl(), rect.br(), color, 1, 8, 0);
                }
            }
        }
        return im;
    }

    private static Mat draw_high_temp_edge_argb_pse(byte[] image, byte[] temperature, int cols, int rows, double high_t, int color_h, int type) throws IOException {
        double[] temp = new double[cols * rows];
        int t = 0;
        for (int i = 0; i < temperature.length; i++) {
            if (i % 2 == 0) {
                float temperature0 = (temperature[i] & 0xff) + (temperature[i + 1] & 0xff) * 256;
                temperature0 = (float) (temperature0 / 64 - 273.15);
                temp[t] = temperature0;
                t++;
            }
        }
        Mat im;
        im = new Mat(rows, cols, CvType.CV_8UC4);
        im.put(0, 0, image);
        cvtColor(im, im, Imgproc.COLOR_RGBA2BGR);
        Mat tem;
        tem = new Mat(rows, cols, CV_64FC1);
        tem.put(0, 0, temp);
        Mat thres_gray = new Mat();
        threshold(tem, thres_gray, high_t, 255, THRESH_BINARY);
        thres_gray.convertTo(thres_gray, CV_8UC1);
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(thres_gray, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<Rect> rects = new ArrayList<Rect>();
        String B = Integer.toString(color_h & 255, 2);
        int b = Integer.parseInt(B, 2);
        int gc = color_h >> 8;
        String G = Integer.toString(gc & 255, 2);
        int g = Integer.parseInt(G, 2);
        int rc = color_h >> 16;
        String R = Integer.toString(rc & 255, 2);
        int r = Integer.parseInt(R, 2);
        Scalar color = new Scalar(b, g, r);
        for (int i = 0; i < cnts.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);
            double area = contourArea(points);
            if (area > 50) {
                if (type == 1) {
                    drawContours(im, cnts, i, color, 1, Imgproc.LINE_8);
                } else {
                    rectangle(im, rect.tl(), rect.br(), color, 1, Imgproc.LINE_8, 0);
                }
            }
        }
        return im;
    }
    public static Bitmap cropBitmap(Bitmap src, int x, int y, int width, int height, boolean isRecycle) {
        if (x == 0 && y == 0 && width == src.getWidth() && height == src.getHeight()) {
            return src;
        }
        Bitmap dst = Bitmap.createBitmap(src, x, y, width, height);
        if (isRecycle && dst != src) {
            src.recycle();
        }
        return dst;
    }

    private static Mat draw_high_temp_edge_argb(byte[] image, byte[] temperature, int cols, int rows, double high_t, int color_h, int type) throws IOException {
        double[] temp = new double[cols * rows];
        int t = 0;
        for (int i = 0; i < temperature.length; i++) {
            if (i % 2 == 0) {
                float temperature0 = (temperature[i] & 0xff) + (temperature[i + 1] & 0xff) * 256;
                temperature0 = (float) (temperature0 / 64 - 273.15);
                temp[t] = temperature0;
                t++;
            }
        }
        Mat im;
        im = new Mat(rows, cols, CvType.CV_8UC4);
        im.put(0, 0, image);
        cvtColor(im, im, Imgproc.COLOR_RGBA2GRAY);
        normalize(im, im, 0, 255, NORM_MINMAX);
        im.convertTo(im, CV_8UC1);
        applyColorMap(im, im, 15);
        Mat tem;
        tem = new Mat(rows, cols, CV_64FC1);
        tem.put(0, 0, temp);
        tem.convertTo(tem, CV_8UC1);
        Mat thres_gray = new Mat();
        threshold(tem, thres_gray, high_t, 255, THRESH_BINARY);
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(thres_gray, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<Rect> rects = new ArrayList<Rect>();
        String B = Integer.toString(color_h & 255, 2);
        int b = Integer.parseInt(B, 2);
        int gc = color_h >> 8;
        String G = Integer.toString(gc & 255, 2);
        int g = Integer.parseInt(G, 2);
        int rc = color_h >> 16;
        String R = Integer.toString(rc & 255, 2);
        int r = Integer.parseInt(R, 2);
        Scalar color = new Scalar(b, g, r);
        for (int i = 0; i < cnts.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);
            double area = contourArea(points);
            if (area > 50) {
                if (type == 1) {
                    drawContours(im, cnts, i, color, 1, 8);
                } else {
                    rectangle(im, rect.tl(), rect.br(), color, 1, 8, 0);
                }
            }
        }
        return im;
    }

    private static Mat draw_high_temp_edge(byte[] image, byte[] temperature, int cols, int rows, double high_t, int color_h, int type) throws IOException {
        double[] temp = new double[cols * rows];
        int t = 0;
        for (int i = 0; i < temperature.length; i++) {
            if (i % 2 == 0) {
                float temperature0 = (temperature[i] & 0xff) + (temperature[i + 1] & 0xff) * 256;
                temperature0 = (float) (temperature0 / 64 - 273.15);
                temp[t] = temperature0;
                t++;
            }
        }
        Mat im;
        im = new Mat(rows, cols, CV_8UC2);
        im.put(0, 0, image);
        cvtColor(im, im, COLOR_YUV2GRAY_YUYV);
        normalize(im, im, 0, 255, NORM_MINMAX);
        im.convertTo(im, CV_8UC1);
        applyColorMap(im, im, 15);
        Mat tem;
        tem = new Mat(rows, cols, CV_64FC1);
        tem.put(0, 0, temp);
        tem.convertTo(tem, CV_8UC1);
        Mat thres_gray = new Mat();
        threshold(tem, thres_gray, high_t, 255, THRESH_BINARY);
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(thres_gray, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<Rect> rects = new ArrayList<Rect>();
        String B = Integer.toString(color_h & 255, 2);
        int b = Integer.parseInt(B, 2);
        int gc = color_h >> 8;
        String G = Integer.toString(gc & 255, 2);
        int g = Integer.parseInt(G, 2);
        int rc = color_h >> 16;
        String R = Integer.toString(rc & 255, 2);
        int r = Integer.parseInt(R, 2);
        Scalar color = new Scalar(b, g, r);
        for (int i = 0; i < cnts.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);
            double area = contourArea(points);
            if (area > 50) {
                if (type == 1) {
                    drawContours(im, cnts, i, color, 1, 8);
                } else {
                    rectangle(im, rect.tl(), rect.br(), color, 1, 8, 0);
                }
            }
        }
        return im;
    }

    private static Mat draw_temp_edge(Mat src, byte[] temperature, double low_t, int color_l, int type) throws IOException {
        double[] temp = new double[src.rows() * src.cols()];
        int t = 0;
        for (int i = 0; i < temperature.length; i++) {
            if (i % 2 == 0) {
                float temperature0 = (temperature[i] & 0xff) + (temperature[i + 1] & 0xff) * 256;
                temperature0 = (float) (temperature0 / 64 - 273.15);
                temp[t] = temperature0;
                t++;
            }
        }
        Mat tem;
        tem = new Mat(src.rows(), src.cols(), CV_64FC1);
        tem.put(0, 0, temp);
        Mat thres_gray = new Mat();
        threshold(tem, thres_gray, low_t, 255, 4);
        thres_gray.convertTo(thres_gray, CV_8UC1);
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(thres_gray, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<Rect> rects = new ArrayList<Rect>();
        String B = Integer.toString(color_l & 255, 2);
        int b = Integer.parseInt(B, 2);
        int gc = color_l >> 8;
        String G = Integer.toString(gc & 255, 2);
        int g = Integer.parseInt(G, 2);
        int rc = color_l >> 16;
        String R = Integer.toString(rc & 255, 2);
        int r = Integer.parseInt(R, 2);
        Scalar color = new Scalar(b, g, r);
        for (int i = 0; i < cnts.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);
            double area = contourArea(points);
            if (area > 50) {
                if (type == 1) {
                    drawContours(src, cnts, i, color, 1, 8);
                } else {
                    rectangle(src, rect.tl(), rect.br(), color, 1, 8, 0);
                }
            }
        }
        MatOfByte matOfByte = new MatOfByte();
        return src;
    }

    public static byte[] draw_edge_from_temp_reigon_byte(byte[] image, byte[] temperature, int row, int col, double high_t, double low_t, int color_h, int color_l, int type) throws IOException {
        Mat src = draw_high_temp_edge(image, temperature, row, col, high_t, color_h, type);
        Mat mat = draw_temp_edge(src, temperature, low_t, color_l, type);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, dstBitmap);
        byte[] bytes = new byte[192 * 256 * 4];
        return bytes;

    }

    public static Mat draw_edge_from_temp_reigon(byte[] image, byte[] temperature, int row, int col, double high_t, double low_t, int color_h, int color_l, int type) throws IOException {
        Mat src = draw_high_temp_edge(image, temperature, row, col, high_t, color_h, type);
        Mat mat = draw_temp_edge(src, temperature, low_t, color_l, type);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, dstBitmap);
        return draw_temp_edge(src, temperature, low_t, color_l, type);

    }

    public static Bitmap draw_edge_from_temp_reigon_bitmap(byte[] image, byte[] temperature, int image_h, int image_w, double high_t, double low_t, int color_h, int color_l, int type) throws IOException {
        Mat src = draw_high_temp_edge(image, temperature, image_h, image_w, high_t, color_h, type);
        Mat mat = draw_temp_edge(src, temperature, low_t, color_l, type);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, dstBitmap);
        return dstBitmap;
    }

    public static Bitmap draw_edge_from_temp_reigon_bitmap_argb(byte[] image, byte[] temperature, int image_h, int image_w, double high_t, double low_t, int color_h, int color_l, int type) throws IOException {
        Mat src = draw_high_temp_edge_argb(image, temperature, image_h, image_w, high_t, color_h, type);
        Mat mat = draw_temp_edge(src, temperature, low_t, color_l, type);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, dstBitmap);
        return dstBitmap;
    }
    
    public static Bitmap draw_edge_from_temp_reigon_bitmap_argb_psd(byte[] image, byte[] temperature, Bitmap lut, int image_h, int image_w, double high_t, double low_t, int color_h, int color_l, int type) throws IOException {
        Mat src = draw_high_temp_edge_argb_pse(image, temperature, image_h, image_w, high_t, color_h, type);
        Mat mat = draw_temp_edge(src, temperature, low_t, color_l, type);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, dstBitmap);
        return dstBitmap;
    }
    
    public static Bitmap draw_edge_from_temp_reigon_bitmap_argb_psd(byte[] image, byte[] temperature,
                                                                    int image_h, int image_w, float high_t,
                                                                    float low_t, int color_h, int color_l, int type) throws IOException {
        Log.w("预警值","最高温："+high_t+"//最低温："+low_t);
        Mat src = draw_high_temp_edge_argb_pse(image, temperature, image_h, image_w, high_t == Float.MAX_VALUE ? 128f : high_t, color_h, type);
        Mat mat = low_t == Float.MIN_VALUE ? src : draw_temp_edge(src, temperature, low_t, color_l, type);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, dstBitmap);
        return dstBitmap;
    }

    public static Mat calcHU(Size size,double t2){
        Mat hu = new Mat(size,CV_32FC1);
        int row = hu.rows();
        int col = hu.cols();
        int cx = row / 2;
        int cy = row / 2;
        for(int i=0; i < row; i ++){
            for(int j = 0; j < col; j++){
                double value = 1 / (1 + Math.pow(Math.sqrt(Math.pow(cx-i,2) + Math.pow(cy-j,2)),-t2));
                hu.put(i,j,value);
            }
        }
        List<Mat> homo = new ArrayList<Mat>();
        homo.add(hu.clone());
        homo.add(new Mat(hu.size(),CV_32FC1,new Scalar(0)));
        Mat hu2c = new Mat(size,CV_32FC2);
        Core.merge(homo,hu2c);
        return hu2c;
    }

    public static Mat iftCenter(Mat src){
        Mat dst = new Mat(src.size(),CV_32F,new Scalar(0));
        int dx = src.rows() / 2;
        int dy = src.cols() / 2;
        float[] data = new float[dy];

        if(src.rows() % 2 == 0) {
            if (src.cols() % 2 == 0) {
                for(int i = 0; i < dx; i++){
                    src.get(i,0,data);
                    dst.put((dx+i),dy,data);
                }
                for(int i = 0; i < dx; i++){
                    src.get(i,dy,data);
                    dst.put((dx+i),0,data);
                }
                for(int i = 0; i < dx; i++){
                    src.get((dx+i),dy,data);
                    dst.put(i,0,data);
                }
                for(int i = 0; i < dx; i++){
                    src.get((dx+i),0,data);
                    dst.put(i,dy,data);
                }

            }else{
                System.out.println("copy failed");
            }
        }
        return dst;
    }

    public static Mat homoMethod(byte[] im, int r, int c){
        int t = 1;
        double t2 = (double)(t-10) / 110;
        Mat image;
        image = new Mat(r, c, CV_8UC2);
        image.put(0, 0, im);
        cvtColor(image, image, COLOR_YUV2GRAY_YUYV);
        normalize(image, image, 0, 255, NORM_MINMAX);
        image.convertTo(image, CV_8UC1);
        CLAHE clahe = Imgproc.createCLAHE();
        clahe.setClipLimit(1.0);
        clahe.setTilesGridSize(new Size(3,3));
        clahe.apply(image,image);
        Mat image_padd = new Mat();
        int row = image.rows();
        int col = image.cols();
        int m = getOptimalDFTSize(row);
        int n = getOptimalDFTSize(col);
        image.convertTo(image_padd, CV_32FC1);
        Core.add(image_padd,new Scalar(1),image_padd);
        Core.log(image_padd,image_padd);
        Core.copyMakeBorder(image_padd,image_padd,0,m - row,0,n - col,BORDER_CONSTANT,new Scalar(0));

        image_padd = iftCenter(image_padd);
        List<Mat> tmp_merge = new ArrayList<Mat>();
        tmp_merge.add(image_padd.clone());
        tmp_merge.add(new Mat(image_padd.size(),CV_32FC1,new Scalar(0)));
        Core.merge(tmp_merge,image_padd);
        Core.dft(image_padd,image_padd);

        Mat image_padd_2c = new Mat(image_padd.size(),CV_32FC2);

        Mat hu2c = calcHU(image_padd.size(),t2);
        Core.mulSpectrums(image_padd, hu2c, image_padd_2c, 0);
        Core.idft(image_padd_2c,image_padd_2c,DFT_SCALE);
        System.out.println(image_padd_2c.channels());

        Core.exp(image_padd_2c,image_padd_2c);
        Core.subtract(image_padd_2c,new Scalar(1),image_padd_2c);
        List<Mat> image_padd_s = new ArrayList<Mat>();
        Core.split(image_padd_2c,image_padd_s);
        Mat reinforce_src = new Mat();
        magnitude(image_padd_s.get(0), image_padd_s.get(1), reinforce_src);

        Mat temp = new Mat();
        normalize(reinforce_src, temp, 0, 255, NORM_MINMAX);
        temp = iftCenter(temp);
        Mat result = new Mat();
        Log.w("123",temp.toString());
        temp.convertTo(result,CV_8UC1);
        Log.w("1234",result.toString());
        applyColorMap(result,result,15);
        cvtColor(result,result,COLOR_RGB2BGR);

        Log.w("1234",result.toString());
        return result;

    }

    public static Mat generateColorBar(int[] colorList, float maxTemp,float minTemp,float customMaxTemp,
                                       float customMinTemp, boolean isGrayUse) {
        if (colorList == null){
            return null;
        }
        Mat colorBar = new Mat(256, 1, CvType.CV_8UC3);
        float maxGrey =  maxTemp > customMaxTemp ? (customMaxTemp - minTemp)/ (maxTemp - minTemp) : -1;
        float minGrey = minTemp < customMinTemp ? (customMinTemp - minTemp) / (maxTemp - minTemp) : -1;
        int[] colors = new int[3];
        for (int i = 0; i < 256; i++) {
            double ratio = (double) i / 255.0;
            int r = 0;
            int g = 0;
            int b = 0;
            if (minGrey != -1 && minGrey > 0 && ratio < minGrey){
                if (isGrayUse){
                    ratio =  ratio / minGrey;
                    r = interpolateR(0x858585, 0x000000, ratio);
                    g = interpolateR(0x858585, 0x000000, ratio);
                    b = interpolateR(0x858585, 0x000000, ratio);
                }else {
                    r = (colorList[0] >> 16) & 0xFF;
                    g = (colorList[0] >> 8) & 0xFF;
                    b = colorList[0] & 0xFF;
                }
                int grey = (int) ((r * 0.3f) + (g * 0.59f) + (b * 0.11f));
                colors[0] = r;
                colors[1] = g;
                colors[2] = b;
                Log.w("测试","低于最小值");
            }else if (maxGrey != -1 && ratio > maxGrey){
                if (isGrayUse){
                    ratio =  (1 - ratio) / (1 - maxGrey);
                    r = interpolateR(0xFFFFFF, 0x858585, ratio);
                    g = interpolateR(0xFFFFFF, 0x858585, ratio);
                    b = interpolateR(0xFFFFFF, 0x858585, ratio);
                }else {
                    r = (colorList[colorList.length-1] >> 16) & 0xFF;
                    g = (colorList[colorList.length-1] >> 8) & 0xFF;
                    b = colorList[colorList.length-1] & 0xFF;
                }
                int grey = (int) ((r * 0.3f) + (g * 0.59f) + (b * 0.11f));
                colors[0] = r;
                colors[1] = g;
                colors[2] = b;
                Log.w("测试","大于于最大值");
            }else if (maxTemp >= customMaxTemp && minTemp <= customMinTemp){
                Log.w("测试","实际温度大于并且小于自定义的最高低温");
               colors = capColor(colorList,maxTemp,minTemp,customMaxTemp,customMinTemp,isGrayUse,ratio);
            }else if (customMinTemp > maxTemp){
                if (isGrayUse){
                    r = interpolateR(0xFFFFFF, 0x000000, ratio);
                    g = interpolateR(0xFFFFFF, 0x000000, ratio);
                    b = interpolateR(0xFFFFFF, 0x000000, ratio);
                }else {
                    r = (colorList[0] >> 16) & 0xFF;
                    g = (colorList[0] >> 8) & 0xFF;
                    b = colorList[0] & 0xFF;
                }
                int grey = (int) ((r * 0.3f) + (g * 0.59f) + (b * 0.11f));
                colors[0] = grey;
                colors[1] = grey;
                colors[2] = grey;
            }else if (maxTemp < customMaxTemp && minTemp < customMinTemp){
                colors = capColor(getStartColor(colorList,customMaxTemp,customMinTemp,maxTemp),
                        maxTemp,minTemp,maxTemp,customMinTemp,isGrayUse,ratio);
            }else if (maxTemp > customMaxTemp && minTemp > customMinTemp){
                colors = capColor(getEndColor(colorList,customMaxTemp,customMinTemp,minTemp),
                        maxTemp,minTemp,customMaxTemp,minTemp,isGrayUse,ratio);
            }else if (maxTemp < customMaxTemp && minTemp > customMinTemp){
                int[] tmpColor = getStartOrEndColor(colorList,customMaxTemp,customMinTemp,maxTemp,minTemp);
                colors = capColor(tmpColor,
                        maxTemp,minTemp,maxTemp,minTemp,isGrayUse,ratio);
            }
            Log.w("测试","编号值"+i+":"+colors[0]+"--"+ colors[1]+"--"+colors[2]+"//"+maxTemp+"--"+minTemp+"-"+customMaxTemp);
            colorBar.put(i, 0, colors[2], colors[1], colors[0]);
        }
        return colorBar;
    }

    static int[] getStartColor(int[] colorList, float customMaxTemp,float customMinTemp,float nowTemp){
        double ratio = (nowTemp - customMinTemp) / (customMaxTemp - customMinTemp);
        int colorNumber = colorList.length - 1;
        float avg = 1.f / colorNumber;
        int colorIndex = colorNumber;
        int r = 0;
        int g = 0;
        int b = 0;
        for (int index = 1; index <= colorNumber;index++){
            if (ratio == 0){
                colorIndex = 0;
                break;
            }
            if (ratio < (avg * index)){
                colorIndex = index;
                break;
            }
        }
        ratio = (ratio - (avg * (colorIndex - 1))) / avg;
        r = interpolateR(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
        g = interpolateG(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
        b = interpolateB(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
        int nowColor = convertTo16Bit(r,g,b);
        int[] nowColorList = Arrays.copyOfRange(colorList,0,colorIndex+1);
       return nowColorList;
    }
    
    static int[] getEndColor(int[] colorList, float customMaxTemp,float customMinTemp,float nowTemp){
        double ratio = (nowTemp - customMinTemp) / (customMaxTemp - customMinTemp);
        int colorNumber = colorList.length - 1;
        float avg = 1.f / colorNumber;
        int colorIndex = colorNumber;
        int r = 0;
        int g = 0;
        int b = 0;
        for (int index = 1; index <= colorNumber;index++){
            if (ratio == 0){
                colorIndex = 0;
                break;
            }
            if (ratio < (avg * index)){
                colorIndex = index;
                break;
            }
        }
        ratio = (ratio - (avg * (colorIndex - 1))) / avg;
        r = interpolateR(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
        g = interpolateG(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
        b = interpolateB(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
        int nowColor = convertTo16Bit(r,g,b);
        int nowColorLenght = colorList.length - colorIndex + 1;
        if (nowColorLenght < 1){
            nowColorLenght = 2;
        }
        int[] nowColorList = new int[nowColorLenght];
        nowColorList[0] = nowColor;
        for (int i = 1;i < nowColorList.length;i++){
            nowColorList[i]= colorList[colorIndex-1 + i];
        }
        return nowColorList;
    }
    static int[] getStartOrEndColor(int[] colorList, float customMaxTemp,float customMinTemp,float nowMaxTemp,float nowMinTemp){
        double maxRatio = (nowMaxTemp - customMinTemp) / (customMaxTemp - customMinTemp);
        double minRatio = (nowMinTemp - customMinTemp) / (customMaxTemp - customMinTemp);
        int colorNumber = colorList.length - 1;
        float avg = 1.f / colorNumber;
        int maxColorIndex = colorNumber;
        int r = 0;
        int g = 0;
        int b = 0;
        for (int index = 1; index <= colorNumber;index++){
            if (maxRatio == 0){
                maxColorIndex = 0;
                break;
            }
            if (maxRatio < (avg * index)){
                maxColorIndex = index;
                break;
            }
        }
        maxRatio = (maxRatio - (avg * (maxColorIndex - 1))) / avg;
        r = interpolateR(colorList[maxColorIndex-1], colorList[maxColorIndex], maxRatio);
        g = interpolateG(colorList[maxColorIndex-1], colorList[maxColorIndex], maxRatio);
        b = interpolateB(colorList[maxColorIndex-1], colorList[maxColorIndex], maxRatio);
        int nowMaxColor = convertTo16Bit(r,g,b);

        int minColorIndex = colorNumber;
        for (int index = 1; index <= colorNumber;index++){
            if (minRatio == 0){
                minColorIndex = 0;
                break;
            }
            if (minRatio < (avg * index)){
                minColorIndex = index;
                break;
            }
        }
        minRatio = (minRatio - (avg * (minColorIndex - 1))) / avg;
        r = interpolateR(colorList[minColorIndex-1], colorList[minColorIndex], minRatio);
        g = interpolateG(colorList[minColorIndex-1], colorList[minColorIndex], minRatio);
        b = interpolateB(colorList[minColorIndex-1], colorList[minColorIndex], minRatio);
        int nowMinColor = convertTo16Bit(r,g,b);
        int[] nowColorList;
        if (minColorIndex == maxColorIndex){
            nowColorList = new int[2];
            nowColorList[nowColorList.length - 1] = nowMaxColor;
            nowColorList[0] = nowMinColor;
        }else {
            nowColorList = new int[maxColorIndex - minColorIndex+2];
            nowColorList[nowColorList.length - 1] = nowMaxColor;
            nowColorList[0] = nowMinColor;
            for (int i = minColorIndex;i < maxColorIndex;i++){
                nowColorList[i]= colorList[i];
            }
        }
        return nowColorList;
    }

    public static int convertTo16Bit(int red, int green, int blue) {
        int intValue = (red << 16) | (green << 8) | blue ;
        return intValue;
    }
    static int[] capColor(int[] colorList, float maxTemp,float minTemp,float customMaxTemp,
                          float customMinTemp, boolean isGrayUse,double ratio){
        int r = 0;
        int g = 0;
        int b = 0;
        float tempValue = (maxTemp - minTemp);
        float minGrayRatio = (customMinTemp - minTemp) / tempValue;
        float maxGrayRatio = (customMaxTemp - minTemp) / tempValue;
        if (minGrayRatio > 0 && ratio < minGrayRatio){
            if (isGrayUse){
                ratio =  ratio / minGrayRatio;
                r = interpolateR(0x858585, 0x000000, ratio);
                g = interpolateR(0x858585, 0x000000, ratio);
                b = interpolateR(0x858585, 0x000000, ratio);
            }else {
                r = (colorList[0] >> 16) & 0xFF;
                g = (colorList[0] >> 8) & 0xFF;
                b = colorList[0] & 0xFF;
            }
        }else if (ratio > maxGrayRatio){
            if (isGrayUse){
                ratio =  (1 - ratio) / (1 - maxGrayRatio);
                r = interpolateR(0xFFFFFF, 0x858585, ratio);
                g = interpolateR(0xFFFFFF, 0x858585, ratio);
                b = interpolateR(0xFFFFFF, 0x858585, ratio);
            }else {
                r = (colorList[colorList.length-1] >> 16) & 0xFF;
                g = (colorList[colorList.length-1] >> 8) & 0xFF;
                b = colorList[colorList.length-1] & 0xFF;
            }
        }else if (ratio >= minGrayRatio && ratio <= maxGrayRatio){
            if (minGrayRatio >= 0 && maxGrayRatio >= 0){
                ratio =  (ratio - minGrayRatio) / (maxGrayRatio - minGrayRatio);
            }
            int colorNumber = colorList.length - 1;
            float avg = 1.f / colorNumber;
            int colorIndex = colorNumber;
            for (int index = 1; index <= colorNumber;index++){
                if (ratio == 0){
                    colorIndex = 0;
                    break;
                }
                if (ratio < (avg * index)){
                    colorIndex = index;
                    break;
                }
            }
            ratio = (ratio - (avg * (colorIndex - 1))) / avg;
            r = interpolateR(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
            g = interpolateG(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
            b = interpolateB(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
        }
        return new int[]{r,g,b};
    }

    public static int lastColor(int[] colorList,int index){
        if (index == 0){
            return colorList[0];
        }
        return colorList[index-1];
    }

    public static Mat generateColorBarThree(int customMinColor, int customMiddleColor, int customMaxColor,
                                       float maxTemp,float minTemp,float customMaxTemp,float customMinTemp,
                                       boolean isGrayUse) {
        Mat colorBar = new Mat(256, 1, CvType.CV_8UC3);
        float tempValue = (maxTemp - minTemp);
        float maxGrayRatio = (maxTemp - customMaxTemp) / tempValue;
        float minGrayRatio = (maxTemp - customMinTemp) / tempValue;
        for (int i = 0; i < 256; i++) {
            double ratio = (double) i / 255.0;
            int r = 0;
            int g = 0;
            int b = 0;
            if (maxGrayRatio > 0 && ratio < maxGrayRatio){
                if (isGrayUse){
                    ratio =  ratio / maxGrayRatio;
                    r = interpolateR(0xC2C2C2, 0xADADAD, ratio);
                    g = interpolateR(0xC2C2C2, 0xADADAD, ratio);
                    b = interpolateR(0xC2C2C2, 0xADADAD, ratio);
                }else {
                    r = (customMaxColor >> 16) & 0xFF;
                    g = (customMaxColor >> 8) & 0xFF;
                    b = customMaxColor & 0xFF;
                }
            }else if (ratio > minGrayRatio){
                if (isGrayUse){
                    ratio =  (1 - ratio) / (1 - minGrayRatio);
                    r = interpolateR(0xADADAD, 0x707070, ratio);
                    g = interpolateR(0xADADAD, 0x707070, ratio);
                    b = interpolateR(0xADADAD, 0x707070, ratio);
                }else {
                    r = (customMinColor >> 16) & 0xFF;
                    g = (customMinColor >> 8) & 0xFF;
                    b = customMinColor & 0xFF;
                }
            }else if (ratio > maxGrayRatio && ratio < minGrayRatio){
                if (maxGrayRatio > 0 && minGrayRatio > 0){
                    ratio =  (ratio - maxGrayRatio) / (minGrayRatio - maxGrayRatio);
                }
                if (ratio < 0.5){
                    ratio = ratio / 0.5;
                    r = interpolateR(customMaxColor, customMiddleColor, ratio);
                    g = interpolateG(customMaxColor, customMiddleColor, ratio);
                    b = interpolateB(customMaxColor, customMiddleColor, ratio);
                }else {
                    ratio = (ratio - 0.5) / 0.5;
                    r = interpolateR(customMiddleColor, customMinColor, ratio);
                    g = interpolateG(customMiddleColor, customMinColor, ratio);
                    b = interpolateB(customMiddleColor, customMinColor, ratio);
                }
            }
            colorBar.put(i, 0, b, g, r);
        }
        return colorBar;
    }
    private static int[] getOneColorByTemp(float customMaxTemp, float customMinTemp, float nowTemp, int[] colorList){
        long time = System.nanoTime();
        int[] result = new int[3];
        float tempValue = customMaxTemp - customMinTemp;
        float ratio = (nowTemp - customMinTemp) / tempValue;
        int colorNumber = colorList.length - 1;
        float avg = 1.f / colorNumber;
        int colorIndex = colorNumber;
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
        if (ratio - 0f > 0){
            int avgColorIndex = (int) (ratio / avg);
            int addNumber = 0;
            if ((ratio % avg) > 0){
                addNumber = 1;
            }
            colorIndex = avgColorIndex + addNumber;
        }else {
            colorIndex = 0;
        }
        ratio = (ratio - (avg * (colorIndex - 1))) / avg;
        result[0] = interpolateR(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
        result[1] = interpolateG(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
        result[2] = interpolateB(lastColor(colorList,colorIndex), colorList[colorIndex], ratio);
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

    public static int[] getOneColorByTempUnif(float customMaxTemp, float customMinTemp, float nowTemp,
                                              int[] colorList, float[] positionList){
       if (positionList!=null){
          return getOneColorByTempEx(
                    customMaxTemp,
                    customMinTemp,
                    nowTemp,
                    colorList,
                    positionList
            );
        }else{
          return getOneColorByTemp(
                    customMaxTemp,
                    customMinTemp,
                    nowTemp,
                    colorList
            );
        }
    }

    private static int[] getOneColorByTempEx(float customMaxTemp, float customMinTemp, float nowTemp,
                                            int[] colorList, float[] positionList) {
        if (colorList == null || colorList.length == 0 || positionList == null || positionList.length == 0) {
            return null;
        }

        float tempRange = customMaxTemp - customMinTemp;
        float ratio = (nowTemp - customMinTemp) / tempRange;
        ratio = Math.min(Math.max(ratio, 0), 1);

        int[] result = new int[3];
        int colorCount = colorList.length;

        if (Math.abs(nowTemp - customMaxTemp) < 0.1f) {
            return new int[] {
                    (colorList[colorCount - 1] >> 16) & 0xFF,
                    (colorList[colorCount - 1] >> 8) & 0xFF,
                    colorList[colorCount - 1] & 0xFF
            };
        } else if (Math.abs(nowTemp - customMinTemp) < 0.1f) {
            return new int[] {
                    (colorList[0] >> 16) & 0xFF,
                    (colorList[0] >> 8) & 0xFF,
                    colorList[0] & 0xFF
            };
        }

        int lowerColorIndex = 0;
        for(int index = positionList.length - 1; index > 0;index--){
            if (index == 1){
                lowerColorIndex = 0;
                break;
            }
            if (ratio <= positionList[index] && ratio >= positionList[index-1]){
                lowerColorIndex = index - 1;
                break;
            }
        }
        float regionRatio = 1;
        if (Math.abs((positionList[lowerColorIndex+1] - positionList[lowerColorIndex])) > 0){
            regionRatio = (ratio - positionList[lowerColorIndex]) / Math.abs((positionList[lowerColorIndex] - positionList[lowerColorIndex+1]));
        }
        int startColor = colorList[lowerColorIndex];
        int endColor = colorList[lowerColorIndex + 1];

        result[0] = interpolateR(startColor, endColor, regionRatio);
        result[1] = interpolateG(startColor, endColor, regionRatio);
        result[2] = interpolateB(startColor, endColor, regionRatio);

        return result;
    }

    static class CustomComparator implements Comparator<Float> {
        @Override
        public int compare(Float key1, Float key2) {
            if ((key1 - key2) <= 0.01) {
                return 0;
            } else if (key1 < key2) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static double calculateHistogram(Mat image1, Mat image2) {
        Mat hist1 = calculateHistogram(image1);
        Mat hist2 = calculateHistogram(image2);

        final double similarity = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL);
        return similarity;
    }

    private static double calculateMSE(Mat image1, Mat image2) {
        Mat diff = new Mat();
        Core.absdiff(image1, image2, diff);
        Mat squaredDiff = new Mat();
        Core.multiply(diff, diff, squaredDiff);
        Scalar mseScalar = Core.mean(squaredDiff);
        return mseScalar.val[0];
    }

    private static double calculateSSIM(Mat image1, Mat image2) {
        Mat image1Gray = new Mat();
        Mat image2Gray = new Mat();
        Imgproc.cvtColor(image1, image1Gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(image2, image2Gray, Imgproc.COLOR_BGR2GRAY);
        MatOfFloat ssimMat = new MatOfFloat();
        Imgproc.matchTemplate(image1Gray, image2Gray, ssimMat, Imgproc.CV_COMP_CORREL);
        Scalar ssimScalar = Core.mean(ssimMat);
        return ssimScalar.val[0];
    }

    private static double calculatePSNR(Mat image1, Mat image2) {
        Mat diff = new Mat();
        Core.absdiff(image1, image2, diff);
        Mat squaredDiff = new Mat();
        Core.multiply(diff, diff, squaredDiff);
        Scalar mseScalar = Core.mean(squaredDiff);
        double mse = mseScalar.val[0];
        double psnr = 10.0 * Math.log10(255.0 * 255.0 / mse);
        return psnr;
    }

    private static Mat calculateHistogram(Mat image) {
        Mat hist = new Mat();
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0, 256);
        MatOfInt channels = new MatOfInt(0);
        List<Mat> images = new ArrayList<Mat>();
        images.add(image);

        Imgproc.calcHist(images, channels, new Mat(), hist, histSize, ranges);
        return hist;
    }

    public static Mat getImageData(byte[] image){
        Mat im;
        im = new Mat(256, 192, CvType.CV_8UC4);
        im.put(0, 0, image);
        cvtColor(im, im, Imgproc.COLOR_RGBA2BGR);
        return im;
    }
    public static Mat getTempData(byte[] temperature){
        double[] temp = new double[256*192];
        int t = 0;
        for (int i = 0; i < temperature.length; i++) {
            if (i % 2 == 0) {
                double value = (temperature[i] & 0xff) + (temperature[i + 1] & 0xff) * 256;
                double g = value / 64.0 - 273.15;
                temp[t] = g;
                t++;
            }
        }
        Mat src;
        src = new Mat(256, 192, CV_64FC1);
        src.put(0,0,temp);
        return src;
    }

    public static boolean getStatus(byte[] image1, byte[] image2){
        long time = System.currentTimeMillis();
        Mat mat1 = getImageData(image1);
        Mat mat2 = getImageData(image2);
        cvtColor(mat1, mat1, Imgproc.COLOR_BGR2GRAY);
        cvtColor(mat2, mat2, Imgproc.COLOR_BGR2GRAY);
        boolean isSame =  getStatus(mat1,mat2);
        return isSame;
    }

    public static Mat highTemTrack(byte[] image, byte[] temperature) throws IOException{
        Mat im = getImageData(image);
        Mat tempMat = getTempData(temperature);
        tempMat.convertTo(tempMat, CV_8UC1);
        Mat thresMat = new Mat();
        threshold(tempMat, thresMat, 40.0, 255.0, THRESH_BINARY);
        thresMat.convertTo(thresMat, CV_8UC1);
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(thresMat, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<Rect> rects = new ArrayList<Rect>();
        for (int i = 0; i < cnts.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);
            double area = contourArea(points);
            if ((area > 50) && (area < 256 * 192 * 0.2)) {
                int topX = (int)rect.tl().x;
                int topY = (int)rect.tl().y;
                int bottomX = (int)rect.br().x;
                int bottomY = (int)rect.br().y;
                for (int k = topY; k < bottomY; k++) {
                    for (int j = topX; j < bottomX; j++) {
                        double[] rgb = new double[3];
                        rgb[0] = 0.6 * im.get(k, j)[0];
                        rgb[1] = 0.6 * im.get(k, j)[1] + 0.4 * 255.0;
                        rgb[2] = 0.6 * im.get(k, j)[2] + 0.4 * 255.0;
                        im.put(k, j, rgb);
                    }
                }
            }
        }
        return im;

    }
    public static Mat lowTemTrack(byte[] image, byte[] temperature) throws IOException{
        Mat im = getImageData(image);
        Mat tempMat = getTempData(temperature);
        tempMat.convertTo(tempMat, CV_8UC1);
        Mat thresMat = new Mat();
        threshold(tempMat, thresMat, 30.0, 255.0, THRESH_BINARY_INV);
        thresMat.convertTo(thresMat, CV_8UC1);
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(thresMat, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<Rect> rects = new ArrayList<Rect>();
        for (int i = 0; i < cnts.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);
            double area = contourArea(points);
            if ((area > 50) && (area < 256 * 192 * 0.2)) {
                int topX = (int)rect.tl().x;
                int topY = (int)rect.tl().y;
                int bottomX = (int)rect.br().x;
                int bottomY = (int)rect.br().y;
                for (int k = topY; k < bottomY; k++) {
                    for (int j = topX; j < bottomX; j++) {
                        double[] rgb = new double[3];
                        rgb[0] = 0.6 * im.get(k, j)[2];
                        rgb[1] = 0.6 * im.get(k, j)[1] + 0.4 * 255.0;
                        rgb[2] = 0.6 * im.get(k, j)[0] + 0.4 * 255.0;
                        im.put(k, j, rgb);
                    }
                }
            }
        }
        return im;
    }
    public static boolean getStatus(Mat image1, Mat image2){

        final double similarity = calculateHistogram(image1, image2);
        return similarity > 0.9;
    }

    public static Mat diff2firstFrame(byte[] base, byte[] nextFrame){
        Mat background = getImageData(base);
        Mat add_target_gray = getImageData(nextFrame);
        Mat background_gray = new Mat();
        background.convertTo(background_gray, CV_8UC1);
        Mat es = getStructuringElement(MORPH_ELLIPSE, new Size(9, 4));
        Mat diff = new Mat();
        absdiff(background_gray, add_target_gray, diff);
        Mat thres_diff = new Mat();
        threshold(diff, thres_diff, 25, 255, THRESH_BINARY);

        Mat thres_dilate = new Mat();
        dilate(thres_diff, thres_dilate, es, new Point(-1, -1), 2);
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        cvtColor(thres_dilate, thres_dilate, Imgproc.COLOR_BGR2GRAY);
        findContours(thres_dilate, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        for (int i = 0; i < cnts.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rec = boundingRect(points);
            double area = contourArea(points);
            if (area < 1500) {
                continue;
            }
            else {
                rectangle(background, rec.tl(), rec.br(), new Scalar(0, 255, 0), 1);
            }
        }
        return background;
    }

}
