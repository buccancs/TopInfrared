package com.topdon.commons.util;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MathUtils {
    
    public static double setDoubleAccuracy(double num, int scale) {
        return ((int) (num * Math.pow(10, scale))) / Math.pow(10, scale);
    }

    public static float[] getPercents(int scale, @NonNull float... values) {
        float total = 0;
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0) {
                list.add(i);
            }
            total += values[i];
        }

        if (total == 0) {
            return new float[values.length];
        }

        float[] fs = new float[values.length];
        int sc = (int) Math.pow(10, scale + 2);
        float sum = 0;
        for (int i = 0; i < list.size(); i++) {
            int index = list.get(i);
            if (i == list.size() - 1) {
                fs[index] = 1 - sum;
            } else {
                fs[index] = (int) (values[index] / total * sc) / (float) sc;
                sum += fs[index];
            }
        }
        return fs;
    }

    @NonNull
    public static byte[] numberToBytes(boolean bigEndian, long value, int len) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            int j = bigEndian ? 7 - i : i;
            bytes[i] = (byte) (value >> 8 * j & 0xff);
        }
        if (len > 8) {
            return bytes;
        } else {
            return Arrays.copyOfRange(bytes, bigEndian ? 8 - len : 0, bigEndian ? 8 : len);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T bytesToNumber(boolean bigEndian, Class<T> cls, @NonNull byte... src) {
        int len = Math.min(8, src.length);
        byte[] bs = new byte[8];
        System.arraycopy(src, 0, bs, bigEndian ? 8 - len : 0, len);
        long value = 0;
        for (int i = 0; i < 8; i++) {
            int shift = (bigEndian ? 7 - i : i) << 3;
            value = value | ((long) 0xff << shift & ((long) bs[i] << shift));
        }
        if (src.length == 1) {
            value = (byte) value;
        } else if (src.length == 2) {
            value = (short) value;
        } else if (src.length <= 4) {
            value = (int) value;
        }
        if (cls == short.class || cls == Short.class) {
            return (T) Short.valueOf((short) value);
        } else if (cls == int.class || cls == Integer.class) {
            return (T) Integer.valueOf((int) value);
        } else if (cls == long.class || cls == Long.class) {
            return (T) Long.valueOf(value);
        }
        throw new IllegalArgumentException("cls must be one of short, int and long");
    }

    public static byte[] reverseBitAndByte(byte[] src) {
        if (src == null || src.length == 0) {
            return null;
        }
        byte[] target = new byte[src.length];
        for (int i = 0; i < src.length; i++) {
            int value = 0;
            int tmp = src[src.length - 1 - i];
            for (int j = 7; j >= 0; j--) {
                value |= (tmp & 0x01) << j;
                tmp >>= 1;
            }
            target[i] = (byte) value;
        }
        return target;
    }

    @NonNull
    public static List<byte[]> splitPackage(@NonNull byte[] src, int size) {
        List<byte[]> list = new ArrayList<>();
        int loop = src.length / size + (src.length % size == 0 ? 0 : 1);
        for (int i = 0; i < loop; i++) {
            int from = i * size;
            int to = Math.min(src.length, from + size);
            list.add(Arrays.copyOfRange(src, i * size, to));
        }
        return list;
    }

    @NonNull
    public static byte[] joinPackage(@NonNull byte[]... src) {
        byte[] bytes = new byte[0];
        for (byte[] bs : src) {
            bytes = Arrays.copyOf(bytes, bytes.length + bs.length);
            System.arraycopy(bs, 0, bytes, bytes.length - bs.length, bs.length);
        }
        return bytes;
    }

    public static int calcCrc8(byte[] bytes) {
        int crc = 0;
        for (byte b : bytes) {
            crc ^= b;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) != 0) {
                    crc = (crc << 1) ^ 0x07;
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc & 0xff;
    }

    public static int calcCRC16_Modbus(byte[] data) {
        int crc = 0xffff;
        for (byte b : data) {
            if (b < 0) {
                crc ^= (int) b + 256;
            } else {
                crc ^= (int) b;
            }
            for (int i = 8; i != 0; i--) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= 0xA001;
                } else
                    crc >>= 1;
            }
        }
        return crc & 0xffff;
    }

    public static int calcCRC_CCITT_XModem(byte[] bytes) {
        int crc = 0;
        int polynomial = 0x1021;
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        return crc & 0xffff;
    }

    public static int calcCRC_CCITT_XModem(byte[] bytes, int offset, int len) {
        int crc = 0;
        int polynomial = 0x1021;
        for (int i = offset; i < offset + len; i++) {
            byte b = bytes[i];
            for (int j = 0; j < 8; j++) {
                boolean bit = ((b >> (7 - j) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        return crc & 0xffff;
    }

    public static int calcCRC_CCITT_0xFFFF(byte[] bytes) {
        int crc = 0xffff;
        int polynomial = 0x1021;
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        return crc & 0xffff;
    }

    public static int calcCRC_CCITT_0xFFFF(byte[] bytes, int offset, int len) {
        int crc = 0xffff;
        int polynomial = 0x1021;
        for (int i = offset; i < offset + len; i++) {
            byte b = bytes[i];
            for (int j = 0; j < 8; j++) {
                boolean bit = ((b >> (7 - j) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        return crc & 0xffff;
    }
}
