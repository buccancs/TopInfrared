package com.topdon.commons.util;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

public class UMUtils {

    public static void onEvent(Context mContext, String var1, String var2) {
        MobclickAgent.onEvent(mContext, var1, var2);
    }

    public static void onEvent(Context mContext, String var1) {
        MobclickAgent.onEvent(mContext, var1);
    }

}
