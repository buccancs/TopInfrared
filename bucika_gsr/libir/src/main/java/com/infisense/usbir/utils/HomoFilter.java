package com.infisense.usbir.utils;

import org.opencv.core.*;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.core.Mat.zeros;
import static org.opencv.highgui.HighGui.imshow;
import static org.opencv.highgui.HighGui.waitKey;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgproc.Imgproc.*;
import static org.opencv.imgproc.Imgproc.COLOR_YUV2GRAY_YUYV;

public class HomoFilter {

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
        //System.out.println(hu.dump());
        return hu2c;
    }

    public static Mat iftCenter(Mat src){
        Mat dst = new Mat(src.size(),CV_32F,new Scalar(0));
        int dx = src.rows() / 2;
        int dy = src.cols() / 2;
        float[] data = new float[dy];

        //System.out.println(src.dump());
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
        //System.out.println(dst.dump());
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
        //imshow("src", image);
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
        temp.convertTo(result,CV_8UC1);
        //applyColorMap(image,image,15);
        //imshow("image", image);
        //equalizeHist(result,result);
//        CLAHE clahe = Imgproc.createCLAHE();
//        clahe.setClipLimit(2);
//        clahe.setTilesGridSize(new Size(3,3));
//        clahe.apply(result,result);
//        applyColorMap(result,result,15);
        //imshow("result", result);
        //waitKey(0);
        return result;

    }

}