package com.topdon.commons.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @Desc 文件大小工具类
 * @ClassName FileSizeUtil
 * @Email 616862466@qq.com
 * @Author 子墨
 * @Date 2022/12/14 18:40
 */

public class FileSizeUtil {
    public static final int SIZETYPE_B = 1;//获取文件大小单位为B的double值
    public static final int SIZETYPE_KB = 2;//获取文件大小单位为KB的double值
    public static final int SIZETYPE_MB = 3;//获取文件大小单位为MB的double值
    public static final int SIZETYPE_GB = 4;//获取文件大小单位为GB的double值


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

    /**
     * 返回内容类型
     *
     * @param sizeType 内存类型
     * @return String
     */
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

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
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
//            Log.e("获取文件大小", "getFilesSize-2-获取失败!");
        }
        return blockSize;
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
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


    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     *
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
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

    /**
     * 获取指定文件大小
     *
     * @return
     * @throws Exception
     */
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
//            Log.e("获取文件大小", "getFileSize-5-获取失败!");
            e.printStackTrace();
        } finally {
            if (fc != null) {
                fc.close();
            }
        }
        return 0;
    }

    /**
     * 获取指定文件夹
     *
     * @param f
     * @return
     * @throws Exception
     */
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

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
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

    /**
     * 转换文件大小,指定转换的类型
     *
     * @param fileS
     * @param sizeType
     * @return
     */
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


    /**
     * 获取文件大小
     * 写入日志读取
     *
     * @param filename 文件名
     * @return long
     */
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
