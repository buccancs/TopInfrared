package com.topdon.lib.core.so;

public class algorithm {
    public static native byte[] AdjustPhoto(String strFilePath,byte[] bytes);
    public static native byte[] maxTempL(byte[] imgBytes,byte[] tempByte,int width,int height);
    public static native byte[] lowTemTrack(byte[] imgBytes,byte[] tempByte,int width,int height);

}
