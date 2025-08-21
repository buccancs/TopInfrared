package com.topdon.commons.util;

import android.util.Log;

import com.elvishew.xlog.XLog;
import com.topdon.ble.BuildConfig;

/**
 * LLog
 *
 * @author chuanfeng.bi
 * @date 2021/11/16 16:34
 */
public class LLog {
    private static boolean isDebug = BuildConfig.DEBUG;


    public static void d(String tag, String value) {
        XLog.tag(tag).d(value);
//        if (isDebug) {
//            Log.d(tag, value);
//        }
    }

    public static void i(String tag, String value) {
        XLog.tag(tag).i(value);
//        if (isDebug) {
//            Log.i(tag, value);
//        }
    }

    public static void w(String tag, String value) {
        XLog.tag(tag).w(value);
//        if (isDebug) {
//            Log.w(tag, value);
//        }
    }

    public static void e(String tag, String value) {
        XLog.tag(tag).e(value);
//        if (isDebug) {
//            Log.e(tag, value);
//        }
    }


    /**
     * 最大一次打印长度
     */
    public final static int MAX_LENGTH = 2000;

    /**
     * 适应最大长度打印
     *
     * @param tag 标志
     * @param msg 信息
     */
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
