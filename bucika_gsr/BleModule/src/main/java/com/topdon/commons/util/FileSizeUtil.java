package com.topdon.commons.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class FileSizeUtil {
    public static final int SIZETYPE_B = 1;
    public static final int SIZETYPE_KB = 2;
    public static final int SIZETYPE_MB = 3;
    public static final int SIZETYPE_GB = 4;

    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("bcf获取文件大小", "getFileOrFilesSize-1-获取失败!");
        }
        return FormetFileSize(blockSize, sizeType);
    }

    public static String getUnit(int sizeType) {
        String memoryUnit;
        if (sizeType == SIZETYPE_B) {
            memoryUnit = "B";
        } else if (sizeType == SIZETYPE_KB) {
            memoryUnit = "KB";
        } else if (sizeType == SIZETYPE_MB) {
            memoryUnit = "MB";
        } else {
            memoryUnit = "GB";
        }
        return memoryUnit;
    }

    public static long getFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("bcf获取文件大小--getFilesSize-2-获取失败!");
        }
        return blockSize;
    }

    public static String getAutoFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("bcf获取文件大小", "getAutoFileOrFilesSize-3-获取失败!");
        }
        return FormetFileSize(blockSize, sizeType) + getUnit(sizeType);
    }

    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("bcf获取文件大小", "getAutoFileOrFilesSize-4-获取失败!");
        }
        return FormetFileSize(blockSize);
    }

    private static long getFileSize(File file) throws Exception {
        FileChannel fc = null;
        try {
            if (file.exists() && file.isFile()) {
                FileInputStream fis = new FileInputStream(file);
                fc = fis.getChannel();
                if (fc.isOpen()) {
                    return fc.size();
                }
            }
        } catch (Exception e) {
            System.out.println("bcf获取文件大小--getFilesSize-5-获取失败!");
            e.printStackTrace();
        } finally {
            if (fc != null) {
                fc.close();
            }
        }
        return 0;
    }

    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    public static String FormetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    public static double FormetFileSize(long fileS, int sizeType) {
        Locale enlocale = new Locale("en", "US");
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(enlocale);
        df.applyPattern("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.parseDouble(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.parseDouble(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.parseDouble(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.parseDouble(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }

    public static long getFileSizeByWriteLog(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists() || !file.isFile()) {
                System.out.println("bcf--getFileSize文件大小不存在");
                return -1;
            }
            return file.length();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("bcf--getFileSize获取文件大小--getFilesSize-5-获取失败!");
        }
        return 0;
    }
}
