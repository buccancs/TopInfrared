package com.topdon.ble.util;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class HexUtil {
    private static FileInputStream in;

    public static String bytesToHexString(byte[] bArray) {
        if (bArray == null || bArray.length <= 0)
            return "BYTE IS NULL";
        int length = bArray.length;
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static String byteToHex(byte byte1) {
        StringBuffer sb = new StringBuffer(1);
        String sTemp;
        for (int i = 0; i < 1; i++) {
            sTemp = Integer.toHexString(0xFF & byte1);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    public static byte[] toByteArray(String hexStr) {
        String s = hexStr.replaceAll("", "");
        if (s.length() % 2 != 0) {
            s = "0" + s;
        }
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }

    public static byte[] toByteArray1(String hexStr) {
        String s = hexStr.replaceAll("", "");
        if (s.length() % 2 != 0) {
            s = "0" + s;
        }
        byte[] bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        if (bytes.length != 2) {
            byte v = bytes[0];
            bytes = new byte[2];
            bytes[0] = 0;
            bytes[1] = v;
        }
        return bytes;
    }

    public static byte[] getString2HexBytes(String src) {
        byte[] ret = new byte[src.length() / 2];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < src.length() / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    public static byte[] HexString2Bytes(String src) {
        int len = src.length() / 2;
        byte[] ret = new byte[len];
        byte[] tmp = src.getBytes();
        for (int i = 0; i < len; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    public static byte[] hexToByte(String hex) {
        int m = 0, n = 0;
        int byteLen = hex.length() / 2;
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = Byte.valueOf((byte) intVal);
        }
        return ret;
    }

    public static String hexToString(String bytes) {
        bytes = bytes.toUpperCase();
        String hexString = "0123456789ABCDEFabcdef";
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    public static byte[] readFileToByteArray(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Log.d("bcf", "File doesn't exist!");
            return null;
        }
        try {
            in = new FileInputStream(file);
            long inSize = in.getChannel().size();
            if (inSize == 0) {
                Log.d("bcf", "The FileInputStream has no content!");
                return null;
            }

            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            return buffer;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                return null;
            }
        }
    }

    public static byte[] byteSub(byte[] data, int start, int length) {
        byte[] bt = new byte[length];
        if (start + length > data.length) {
            bt = new byte[data.length - start];
        }
        for (int i = 0; i < length && (i + start) < data.length; i++) {
            bt[i] = data[i + start];
        }
        return bt;
    }
}
