package com.example.connectlisten;


import com.topdon.lib.core.so.algorithm;

public class JNITest {
    static {
        System.loadLibrary("opencv_java4");
//        System.loadLibrary("SRImage");
//        System.loadLibrary("minMaxTemperatureDetect");
    }

    public static byte[] maxTempL(byte[] imgBytes,byte[] tempByte,int width,int height) {
        return  algorithm.maxTempL(imgBytes, tempByte,width,height);
    }
    public static byte[] lowTemTrack(byte[] imgBytes,byte[] tempByte,int width,int height) {
        return  algorithm.lowTemTrack(imgBytes, tempByte,width,height);
    }
}
