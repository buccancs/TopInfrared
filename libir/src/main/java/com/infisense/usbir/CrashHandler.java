package com.infisense.usbir;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static final String TAG = "CrashHandler";
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler crashHandler = new CrashHandler();

    private Context mContext;
    
    private File logFile ;

    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        if (crashHandler == null) {
            synchronized (CrashHandler.class) {
                if (crashHandler == null) {
                    crashHandler = new CrashHandler();
                }
            }
        }
        return crashHandler;
    }

    public void init(Context context) {
        mContext = context;
        logFile = new File(mContext.getCacheDir(),"crashLog.trace");
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();
        if (!handlelException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                upLoadErrorFileToServer(logFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private boolean handlelException(Throwable ex) {
        if (ex == null) {
            return false;
        }

        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "程序发生异常，即将重启", Toast.LENGTH_LONG)
                        .show();
                Looper.loop();
            }
        }.start();

        PrintWriter pw = null;
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            pw = new PrintWriter(logFile);
            logFile = collectInfoToSDCard(pw, ex);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private void upLoadErrorFileToServer(File errorFile) {

    }

    private File collectInfoToSDCard(PrintWriter pw, Throwable ex)
            throws PackageManager.NameNotFoundException {

        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),PackageManager.GET_ACTIVITIES);
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        pw.print("time : ");
        pw.println(time);
        pw.print("versionCode : ");
        pw.println(pi.versionCode);
        pw.print("versionName : ");
        pw.println(pi.versionName);
        try {
            
            Field[] Fields = Build.class.getDeclaredFields();
            for (Field field : Fields) {
                field.setAccessible(true);
                pw.print(field.getName() + " : ");
                pw.println(field.get(null).toString());
            }
        } catch (Exception e) {
            Log.i(TAG, "an error occured when collect crash info" + e);
        }

        ex.printStackTrace(pw);
        return logFile;
    }
}