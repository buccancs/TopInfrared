package com.infisense.usbir.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.google.gson.Gson;

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.osgi.OpenCVNativeLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.Core.normalize;
import static org.opencv.core.CvType.*;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgproc.Imgproc.*;

public class OnlineMethod {


    static {
//        new OpenCVNativeLoader().init();
        System.loadLibrary("opencv_java4");
    }

    public static Mat draw_high_temp_edge(byte[] image,byte[] temperature, double high_t,int color_h, int type) throws IOException {
        double[] temp = new double[256*192];
        int t = 0;

        for (int i = 0; i < temperature.length; i++) {
            if (i % 2 == 0) {
                int value = (int)(temperature[i + 1] << 8) + (int)(temperature[i]);
                double divid = 16.0;
                double g = (value / 4.0) / divid - 273.15;
                //cout << g << " ";
                temp[t] = g;
                //cout << temp[t] << " ";
                t++;
            }
        }
        Mat im;
        im = new Mat(192, 256, CV_8UC2);
        im.put(0, 0, image);
        cvtColor(im, im, COLOR_YUV2GRAY_YUYV);
        normalize(im, im, 0, 255, NORM_MINMAX);
        im.convertTo(im, CV_8UC1);
        applyColorMap(im,im,15);
        Mat tem;
        tem = new Mat(192, 256, CV_64FC1);
        tem.put(0,0,temp);
        tem.convertTo(tem, CV_8UC1);

        //Mat kernal = Mat.ones(5, 5, CV_8UC1);
        //Mat es = getStructuringElement(MORPH_ELLIPSE,new Size(9, 4));
        Mat thres_gray = new Mat();
        //Mat temperature = Mat::zeros(192, 256, CV_8UC1);
        //threshold(temperature, thres_gray, 50, 255, THRESH_BINARY);
        //int thres = int(high_t);
        //src = 255 - src;

        threshold(tem, thres_gray, high_t, 255, THRESH_BINARY);

        //vector<vector<Point>> cnts;
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(thres_gray, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<Rect> rects = new ArrayList<Rect>();

        String B = Integer.toString(color_h&255,2);
        int b = Integer.parseInt(B,2);
        int gc = color_h >> 8;
        String G = Integer.toString(gc&255,2);
        int g = Integer.parseInt(G,2);
        int rc = color_h >> 16;
        String R = Integer.toString(rc&255,2);
        int r = Integer.parseInt(R,2);
        Scalar color = new Scalar(b, g, r);
        for (int i = 0; i < cnts.size(); i++)
        {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);
            double area = contourArea(points);
            if(area > 300){
                if(type == 1){
                    drawContours(im, cnts, i, color, 1, 8);
                }else{
                    rectangle(im, rect.tl(), rect.br(),color, 1, 8, 0);
                }

            }

            //minEnclosingCircle(cnts[i], center[i], radius[i]);
        }
        //cv::Mat imageContours = cv::Mat::zeros(cv::Size(W, H), CV_8UC1);
        //Bezier
        //Mat drawing = Mat::zeros(image.size(), CV_8UC3);imshow("Contours", im)
        // waitKey(0);
        return im;

    }
    public static Mat draw_temp_edge(Mat src,byte[] temperature, double low_t, int color_l, int type) throws IOException {
        double[] temp = new double[256*192];
        int t = 0;

        for (int i = 0; i < temperature.length; i++) {
            if (i % 2 == 0) {
                int value = (int)(temperature[i + 1] << 8) + (int)(temperature[i]);
                double divid = 16.0;
                double g = (value / 4.0) / divid - 273.15;
                //cout << g << " ";
                temp[t] = g;
                //cout << temp[t] << " ";
                t++;
            }
        }
        Mat tem;
        tem = new Mat(192, 256, CV_64FC1);
        tem.put(0,0,temp);
        tem.convertTo(tem, CV_8UC1);


        Mat thres_gray = new Mat();
        threshold(tem, thres_gray, low_t, 255, 4);
        List<MatOfPoint> cnts = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        findContours(thres_gray, cnts, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        List<Rect> rects = new ArrayList<Rect>();
        String B = Integer.toString(color_l&255,2);
        int b = Integer.parseInt(B,2);
        int gc = color_l >> 8;
        String G = Integer.toString(gc&255,2);
        int g = Integer.parseInt(G,2);
        int rc = color_l >> 16;
        String R = Integer.toString(rc&255,2);
        int r = Integer.parseInt(R,2);
        Scalar color = new Scalar(b, g, r);
        for (int i = 0; i < cnts.size(); i++) {
            MatOfPoint2f contour2f = new MatOfPoint2f(cnts.get(i).toArray());
            approxPolyDP(contour2f, approxCurve, 0, true);
            MatOfPoint points = new MatOfPoint(approxCurve.toArray());
            Rect rect = boundingRect(points);

            double area = contourArea(points);
            if (area > 300) {
                if(type == 1){
                    drawContours(src, cnts, i, color, 1, 8);
                }else{
                    rectangle(src, rect.tl(), rect.br(),color, 1, 8, 0);
                }
            }
        }
        MatOfByte matOfByte = new MatOfByte();
        return src;
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
    public static byte[] draw_edge_from_temp_reigon_byte(byte[] image,byte[] temperature,int row, int col, double high_t, double low_t, int color_h, int color_l, int type) throws IOException {
        Mat src = draw_high_temp_edge(image,temperature,high_t,color_h,type);
        Mat mat = draw_temp_edge(src,temperature,low_t,color_l,type);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, dstBitmap);
        byte[] bytes = new  byte[192*256*4];
        return bytes;
    }
    public static Mat draw_edge_from_temp_reigon(byte[] image,byte[] temperature,int row, int col, double high_t, double low_t, int color_h, int color_l, int type) throws IOException {
        Mat src = draw_high_temp_edge(image,temperature,high_t,color_h,type);
        Mat mat = draw_temp_edge(src,temperature,low_t,color_l,type);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, dstBitmap);
        return draw_temp_edge(src,temperature,low_t,color_l,type);
    }


    public static Bitmap draw_edge_from_temp_reigon_bitmap(byte[] image,byte[] temperature,int row, int col, double high_t, double low_t, int color_h, int color_l, int type) throws IOException {
        Mat src = draw_high_temp_edge(image,temperature,high_t,color_h,type);
        Mat mat = draw_temp_edge(src,temperature,low_t,color_l,type);
        Log.e("图像",mat.toString());
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGBA);
        Bitmap dstBitmap = Bitmap.createBitmap(mat.width(),mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, dstBitmap);
        return dstBitmap;
    }

}
