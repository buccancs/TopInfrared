package com.topdon.commons.util;

import android.util.Log;

import com.elvishew.xlog.XLog;
import com.topdon.ble.BuildConfig;

public class LLog {
    private static boolean isDebug = BuildConfig.DEBUG;

    public static void d(String tag, String value) {
        XLog.tag(tag).d(value);
    }

    public static void i(String tag, String value) {
        XLog.tag(tag).i(value);
    }

    public static void w(String tag, String value) {
        XLog.tag(tag).w(value);
    }

    public static void e(String tag, String value) {
        XLog.tag(tag).e(value);
    }

    public final static int MAX_LENGTH = 2000;

    public static void LogMaxPrint(String tag, String msg) {
        if (msg.length() > MAX_LENGTH) {
            int length = MAX_LENGTH + 1;
            String remain = msg;
            int index = 0;
            while (length > MAX_LENGTH) {
                index++;
                Log.v(tag + "[" + index + "]", " \n" + remain.substring(0, MAX_LENGTH));
                remain = remain.substring(MAX_LENGTH);
                length = remain.length();
            }
            if (length <= MAX_LENGTH) {
                index++;
                Log.v(tag + "[" + index + "]", " \n" + remain);
            }
        } else {
            Log.v(tag, msg);
        }
    }

}
