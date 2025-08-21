package com.infisense.usbir.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;
import com.energy.iruvc.utils.CommonParams;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ProjectName: ANDROID_IRUVC_SDK
 * @Package: com.infisense.iruvc.utils
 * @ClassName: FileUtil
 * @Description:
 * @Author: brilliantzhao
 * @CreateDate: 2021.11.11 13:56
 * @UpdateUser:
 * @UpdateDate: 2021.11.11 13:56
 * @UpdateRemark:
 * @Version: 1.0.0
 */
public class FileUtil {

    private static final String TAG = "FileUtil";
    private static final String DATA_SAVE_DIR = "InfiRay";

    /**
     * @param context
     * @return
     */
    public static String getDiskCacheDir(Context context) {
        String cachePath = context.getCacheDir().getPath();
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            File externalCacheDir = context.getExternalCacheDir();
            if (externalCacheDir != null) {
                cachePath = externalCacheDir.getPath();
            }
        }
        return cachePath;
    }

    /**
     * @param context
     * @param srcFileName
     * @param strOutFileName
     * @throws IOException
     */
    public static void copyAssetsDataToSD(Context context, String srcFileName, String strOutFileName) throws IOException {
        File file = new File(strOutFileName);
        if (file.exists()) {
            file.delete();
        }
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = context.getAssets().open(srcFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    /**
     * @param bytes
     * @param fileTitle
     */
    public static void saveByteFile(Context mContext, byte[] bytes, String fileTitle) {
        try {
            String fileSaveDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            File path = new File(fileSaveDir);
            if (!path.exists() && path.isDirectory()) {
                path.mkdirs();
            }
            String fileName = fileTitle + new SimpleDateFormat("_HHmmss_yyMMdd").
                    format(new Date(System.currentTimeMillis())) + ".bin";
            File file = new File(fileSaveDir, fileName);
            Log.i(TAG, "fileSaveDir=" + fileSaveDir + " fileName=" + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param bytes
     * @param fileTitle
     */
    public static void saveByteFile(byte[] bytes, String fileTitle) {
//        try {
//            String fileSaveDir = TempKey.DEVICE_DATA_SAVE_DIR;
//            File path = new File(fileSaveDir);
//            if (!path.exists() && path.isDirectory()) {
//                path.mkdirs();
//            }
//            //
//            File file = new File(fileSaveDir, fileTitle);
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(bytes);
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static String getTableDirPath(){
        return Utils.getApp().getCacheDir().getAbsolutePath()+"/table";
    };

    /**
     * @param bytes
     * @param fileTitle
     */
    public static void saveShortFileForDeviceData(short[] bytes, String fileTitle) {
        try {
            String fileSaveDir = getTableDirPath();
            createOrExistsDir(fileSaveDir);
            //
            File file = new File(fileSaveDir, fileTitle);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(toByteArray(bytes));
            fos.close();
            Log.i(TAG, fileTitle + " 保存成功");
        } catch (IOException e) {
            Log.e(TAG, fileTitle + " 保存失败："+e.getMessage());
        }
    }

    /**
     * @param bytes
     * @param fileTitle
     */
    public static void saveShortFile(short[] bytes, String fileTitle) {
        try {
            File path = new File("/sdcard");
            if (!path.exists() && path.isDirectory()) {
                path.mkdirs();
            }
            File file = new File("/sdcard/", fileTitle + new SimpleDateFormat("_HHmmss_yyMMdd").
                    format(new Date(System.currentTimeMillis())) + ".bin");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(toByteArray(bytes));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据数据流获取Y16类型
     *
     * @param dataFlowMode
     * @return
     */
    public static CommonParams.Y16ModePreviewSrcType getY16SrcTypeByDataFlowMode(CommonParams.DataFlowMode dataFlowMode) {
        switch (dataFlowMode) {
            case TEMP_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_TEMPERATURE;
            }
            case IR_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_IR;
            }
            case KBC_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_KBC;
            }
            case HBC_DPC_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_HBC_DPC;
            }
            case VBC_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_VBC;
            }
            case TNR_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_TNR;
            }
            case SNR_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_SNR;
            }
            case AGC_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_AGC;
            }
            case DDE_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_DDE;
            }
            case GAMMA_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_GAMMA;
            }
            case MIRROR_OUTPUT: {
                return CommonParams.Y16ModePreviewSrcType.Y16_MODE_MIRROR;
            }
        }
        return null;
    }

    /**
     * 创建文件夹---之所以要一层层创建，是因为一次性创建多层文件夹可能会失败！
     *
     * @param dirFile
     * @return
     */
    public static boolean createFileDir(File dirFile) {
        if (dirFile == null) return true;
        if (dirFile.exists()) {
            return true;
        }
        File parentFile = dirFile.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            //父文件夹不存在，则先创建父文件夹，再创建自身文件夹
            return createFileDir(parentFile) && createFileDir(dirFile);
        } else {
            boolean mkdirs = dirFile.mkdirs();
            boolean isSuccess = mkdirs || dirFile.exists();
            if (!isSuccess) {
                Log.e("FileUtil", "createFileDir fail " + dirFile);
            }
            return isSuccess;
        }
    }

    /**
     * @param dirPath
     * @param fileName
     * @return
     */
    public static File createFile(String dirPath, String fileName) {
        try {
            File dirFile = new File(dirPath);
            if (!dirFile.exists()) {
                if (!createFileDir(dirFile)) {
                    Log.e(TAG, "createFile dirFile.mkdirs fail");
                    return null;
                }
            } else if (!dirFile.isDirectory()) {
                boolean delete = dirFile.delete();
                if (delete) {
                    return createFile(dirPath, fileName);
                } else {
                    Log.e(TAG, "createFile dirFile !isDirectory and delete fail");
                    return null;
                }
            }
            File file = new File(dirPath, fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    Log.e(TAG, "createFile createNewFile fail");
                    return null;
                }
            }
            return file;
        } catch (Exception e) {
            Log.e(TAG, "createFile fail :" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 把两个位图覆盖合成为一个位图，以底层位图的长宽为基准
     *
     * @param bytes  在底部的位图
     * @param bytes2 盖在上面的位图
     */
    public static void savaRawFile(byte[] bytes, byte[] bytes2) {
        try {
            File path = new File("/sdcard");
            if (!path.exists() && path.isDirectory()) {
                path.mkdirs();
            }
            File file = new File("/sdcard/", new SimpleDateFormat("_HHmmss_yyMMdd").
                    format(new Date(System.currentTimeMillis())) + ".bin");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.write(bytes2);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存红外数据
     *
     * @param bytes
     */
    public static void savaIRFile(byte[] bytes) {
        try {
            File path = new File("/sdcard");
            if (!path.exists() && path.isDirectory()) {
                path.mkdirs();
            }
            File file = new File("/sdcard/", "ir" + new SimpleDateFormat("_HHmmss_yyMMdd").
                    format(new Date(System.currentTimeMillis())) + ".bin");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存温度数据
     *
     * @param bytes
     */
    public static void savaTempFile(byte[] bytes) {
        try {
            File path = new File("/sdcard");
            if (!path.exists() && path.isDirectory()) {
                path.mkdirs();
            }
            File file = new File("/sdcard/", "temp" + new SimpleDateFormat("_HHmmss_yyMMdd").
                    format(new Date(System.currentTimeMillis())) + ".bin");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param context
     * @param file
     * @return
     */
    public static boolean isFileExists(Context context, final File file) {
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return isFileExists(context, file.getAbsolutePath());
    }

    /**
     * Return whether the file exists.
     *
     * @param filePath The path of file.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isFileExists(Context context, final String filePath) {
        File file = new File(filePath);
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            return true;
        }
        return isFileExistsApi29(context, filePath);
    }

    /**
     * @param context
     * @param filePath
     * @return
     */
    private static boolean isFileExistsApi29(Context context, String filePath) {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                Uri uri = Uri.parse(filePath);
                ContentResolver cr = context.getContentResolver();
                AssetFileDescriptor afd = cr.openAssetFileDescriptor(uri, "r");
                if (afd == null) return false;
                try {
                    afd.close();
                } catch (IOException ignore) {
                }
            } catch (FileNotFoundException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * short数组转byte数组
     *
     * @param src
     * @return
     */
    private static byte[] toByteArray(short[] src) {
        int count = src.length;
        byte[] dest = new byte[count << 1];
        for (int i = 0; i < count; i++) {
            dest[i * 2] = (byte) ((src[i] >> 8) & 0xFF);
            dest[i * 2 + 1] = (byte) (src[i] & 0xFF);
        }
        return dest;
    }

    /**
     * byte数组转short数组
     *
     * @param src
     * @return
     */
    public static short[] toShortArray(byte[] src) {
        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) ((src[i * 2] & 0xFF) << 8 | ((src[2 * i + 1] & 0xFF)));
        }
        return dest;
    }

    /**
     * @param bytes
     * @param fileTitle
     */
    public static void saveShortFile(String fileDir, short[] bytes, String fileTitle) {
        // 创建目录
        createOrExistsDir(fileDir);
        try {
            File file = new File(fileDir, fileTitle + ".bin");
            createOrExistsDir(file);
            Log.i("TAG", "getAbsolutePath = " + file.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(toByteArray(bytes));
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param file
     */
    private static void createOrExistsDir(File file) {
        // 文件不存在则创建文件
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 如果文件夹不存在则创建
     *
     * @param fileDir
     */
    private static void createOrExistsDir(String fileDir) {
        File file = new File(fileDir);
        //如果文件夹不存在则创建
        if (!file.exists() && !file.isDirectory()) {
            //不存在
            file.mkdir();
        } else {
            //目录存在
        }
    }

    private static int sBufferSize = 524288;

    /**
     * @param context
     * @param file
     * @return
     */
    public static byte[] readFile2BytesByStream(Context context, final File file) {
        if (!isFileExists(context, file)) {
            return null;
        }
        try {
            ByteArrayOutputStream os = null;
            InputStream is = new BufferedInputStream(new FileInputStream(file), sBufferSize);
            try {
                os = new ByteArrayOutputStream();
                byte[] b = new byte[sBufferSize];
                int len;
                while ((len = is.read(b, 0, sBufferSize)) != -1) {
                    os.write(b, 0, len);
                }
                return os.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从Assets拷贝数据到SD
     *
     * @param context
     * @param srcFileName
     * @param strOutFileName
     * @throws IOException
     */
    public static void copyAssetsBigDataToSD(Context context, String srcFileName, String strOutFileName) {
        try {
            File file = new File(strOutFileName);
            Log.i(TAG, "file.exists->getAbsolutePath = " + file.getAbsolutePath());
            if (file.exists()) {
                // 如果文件存在则删除文件，重新创建，避免修改的内容不生效
                file.delete();
            }
            //
            if (!file.createNewFile()) {
                Log.e(TAG, "创建文件 " + srcFileName + " 失败");
                return;
            }

            InputStream myInput;
            OutputStream myOutput = new FileOutputStream(strOutFileName);
            myInput = context.getAssets().open(srcFileName);
            byte[] buffer = new byte[1024];
            int length = myInput.read(buffer);
            while (length > 0) {
                myOutput.write(buffer, 0, length);
                length = myInput.read(buffer);
            }
            myOutput.flush();
            myInput.close();
            myOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据增益状态获取对应的ISP算法的配置文件
     *
     * @param gainStatus
     * @return
     */
    public static String getISPConfigByGainStatus(CommonParams.GainStatus gainStatus) {
//        Log.i(TAG, "INFISENSE_SAVE_DIR = " + MyApplication.getInstance().INFISENSE_SAVE_DIR);
        if (CommonParams.GainStatus.HIGH_GAIN == gainStatus) {
            return INFISENSE_SAVE_DIR() + File.separator + "isp_H.json";
        } else {
            return INFISENSE_SAVE_DIR() + File.separator + "isp_L.json";
        }
    }

    /**
     * @param gainStatus
     * @return 输出hex
     */
    public static String getISPConfigWithEncryptHexByGainStatus(CommonParams.GainStatus gainStatus) {
        if (CommonParams.GainStatus.HIGH_GAIN == gainStatus) {
            return INFISENSE_SAVE_DIR() + File.separator + "isp_H_encrypt_hex.json";
        } else {
            return INFISENSE_SAVE_DIR() + File.separator + "isp_L_encrypt_hex.json";
        }
    }


    static String INFISENSE_SAVE_DIR(){
       return Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    }
    //=== 设备信息存储到私有区域，app删除后一起删除
    static String  DEVICE_DATA_SAVE_DIR (){
       return Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    }

    /**
     * @param gainStatus
     * @return 输出base64
     */
    public static String getISPConfigWithEncryptBase64ByGainStatus(CommonParams.GainStatus gainStatus) {
        if (CommonParams.GainStatus.HIGH_GAIN == gainStatus) {
            return INFISENSE_SAVE_DIR() + File.separator + "isp_H_encrypt_base64.json";
        } else {
            return INFISENSE_SAVE_DIR() + File.separator + "isp_L_encrypt_base64.json";
        }
    }

    /**
     * 获取版本信息
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String name = null;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            name = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * @param string
     * @return
     */
    public static String getMD5Key(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param filePath
     */
    public static void makeDirectory(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * @param context
     * @return
     */
    public static String getSaveFilePath(Context context) {
        boolean useExternalStorage = false;
        String directoryPath = "";
        if (Environment.getExternalStorageState().equals("mounted")) {
            if (Environment.getExternalStorageDirectory().getFreeSpace() > 0) {
                useExternalStorage = true;
            }
        }
        if (useExternalStorage) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                directoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DATA_SAVE_DIR + File.separator;
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + File.separator + DATA_SAVE_DIR + File.separator;
            } else {
                directoryPath = context.getFilesDir().getAbsolutePath() + File.separator + DATA_SAVE_DIR + File.separator;
            }
        } else {
            directoryPath = context.getFilesDir().getAbsolutePath() + File.separator + DATA_SAVE_DIR + File.separator;
        }
        return directoryPath;
    }

    /**
     * @param filePath
     * @param fileName
     * @return
     * @throws IOException
     */
    private static File makeFile(String filePath, String fileName) throws IOException {
        makeDirectory(filePath);

        File file = new File(filePath + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }

        return file;
    }

    /**
     * @param bytes
     * @param filePath
     * @param fileName
     * @return
     */
    public static int writeTxtToFile(byte[] bytes, String filePath, String fileName) {
        int result = -1;

        FileChannel fc = null;
        File file = null;
        try {
            makeFile(filePath, fileName);
            file = new File(filePath + fileName);
            fc = new FileOutputStream(file, false).getChannel();
            if (fc == null) {
                Log.e("FileUtils", "fc is null.");
            }
            fc.position(fc.size());
            fc.write(ByteBuffer.wrap(bytes));
            result = 0;

        } catch (IOException e) {
            e.printStackTrace();
            result = -1;
        } finally {
            try {
                if (fc != null) {
                    fc.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
                result = -1;
            }
            return result;
        }
    }

    /**
     * 存储String到本地，覆盖原始数据
     *
     * @param str
     * @param path
     */
    public static void saveStringToFile(String str, String path) {
        File file;
        FileOutputStream stream = null;
        try {
            file = new File(path);
            stream = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] contentInBytes = str.getBytes();
            stream.write(contentInBytes); // 写入
            stream.flush();
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param path
     * @return
     */
    public static String getStringFromFile(String path) {
        StringBuffer txtContent = new StringBuffer();
        byte[] b = new byte[2048];
        InputStream in = null;
        try {
            in = new FileInputStream(path);
            int n;
            while ((n = in.read(b)) != -1) {
                txtContent.append(new String(b, 0, n, "utf-8"));
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return txtContent.toString();
    }

    /**
     * @param num
     * @param numbyte
     */
    public static void float2Byte(float num, byte[] numbyte) {
        int fbit = Float.floatToIntBits(num);
        for (int i = 0; i < 4; i++) {
            numbyte[i] = (byte) (fbit >> (i * 8)); //little-endian
            Log.i(TAG, "numbyte[=" + i + "]=" + numbyte[i]);
        }
    }

}
