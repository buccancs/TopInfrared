package com.topdon.ble.util;

import android.util.Log;

/**
 * ByteUtil
 * 数组操作工具
 *
 * @author chuanfeng.bi
 * @date 2021/12/8 10:49
 */
public class ByteUtil {
    public static byte[] byteMerger(byte[] byte1, int byte2, int byte3, int byte4) {
        return byteMerger(byte1, intToByteArray(byte2), intToByteArray(byte3), intToByteArray2(byte4));
    }
    public static byte[] byteMerger(byte[] byte1, String byte2,String byte3) {
        return byteMerger(byte1, byte2.getBytes(),byte3.getBytes());
    }
    public static byte[] byteMerger(byte[] byte1, String byte2,String byte3,String byte4) {
        return byteMerger(byte1, byte2.getBytes(),byte3.getBytes(),byte4.getBytes());
    }

    public static byte[] byteMerger(String byte1, int byte2) {
        return byteMerger(byte1.getBytes(), intToByteArray(byte2));
    }

    public static byte[] byteMerger(byte[] byte1, int byte2) {
        return byteMerger(byte1, intToByteArray(byte2));
    }

    public static byte[] byteMerger(String byte1, String byte2) {
        return byteMerger(byte1.getBytes(), byte2.getBytes());
    }

    public static byte[] byteMerger(String byte1, byte[] byte2) {
        return byteMerger(byte1.getBytes(), byte2);
    }

    public static byte[] byteMerger(byte[] byte1, String byte2) {
        return byteMerger(byte1, byte2.getBytes());
    }

//    public static byte[] byteMerger(byte[] byte1, byte[] byte2) {
//        byte[] result = new byte[byte1.length + byte2.length];
//        System.arraycopy(byte1, 0, result, 0, byte1.length);
//        System.arraycopy(byte2, 0, result, byte1.length, byte2.length);
//        return result;
//    }

    public static byte[] byteMerger(byte[]... bytes) {
        int length = 0;
        for (byte[] tmp : bytes) {
            length += tmp.length;
        }
        byte[] result = new byte[length];
        int lastTypeLength = 0;
        for (byte[] tmp : bytes) {
            System.arraycopy(tmp, 0, result, lastTypeLength, tmp.length);
            lastTypeLength += tmp.length;
        }
        return result;
    }

    /**
     * int到byte[] 由高位到低位
     *
     * @param i 需要转换为byte数组的整行值。
     * @return byte数组
     */
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[1];
        result[0] = (byte) (i & 0xFF);
        return result;
    }

    public static byte[] intToByteArray2(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }


    public static byte[] LongToBytes(long values) {
        byte[] buffer = new byte[4];
        for (int i = 0; i < 4; i++) {
//            int offset = 64 - (i + 1) * 8;
            int offset = (4 - i - 1)* 8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        return buffer;
    }



    public static float bytesToFloat(byte[] bytes) {
        float value = Integer.valueOf(HexUtil.bytesToHexString(bytes), 16);
        return value;
    }

    public static float byteToFloat(byte... bytes) {
        byte[] resultByte = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            resultByte[i] = bytes[i];
        }
        float value = Integer.valueOf(HexUtil.bytesToHexString(resultByte), 16);
        Log.e("bcf", "bytesToFloat bytes: " + HexUtil.bytesToHexString(resultByte) + "   float:" + value);
        return value;
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    public static byte[] short2byte(short s){
        byte[] b = new byte[2];
        for(int i = 0; i < 2; i++){
            int offset = 16 - (i+1)*8; //因为byte占4个字节，所以要计算偏移量
            b[i] = (byte)((s >> offset)&0xff); //把16位分为2个8位进行分别存储
        }
        return b;
    }

    /**
     * byte[]转int
     * @param bytes 需要转换成int的数组
     * @return int值
     */
    public static int byteArrayToInt(byte[] bytes) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value +=(bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    public static String getCmdType(byte[] bytes) {
        String hex = HexUtil.bytesToHexString(bytes);
        String cmd = "";
        if (hex.length() >= 16) {
            cmd = hex.substring(12, 16);
        }
        return cmd;
    }

    public static String getCmd(byte[] bytes){
        String hex = HexUtil.bytesToHexString(bytes);
        String cmd = "";
        if (hex.length() >= 16) {
            cmd = hex.substring(12, 14);
        }
        return cmd;
    }
}
