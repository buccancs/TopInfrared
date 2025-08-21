package com.topdon.commons.util;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;


/**
 * @Desc 友盟埋点工具类
 * @ClassName UMUtils
 * @Email 616862466@qq.com
 * @Author 子墨
 * @Date 2023/3/28 13:53
 */

public class UMUtils {

    public static void onEvent(Context mContext, String var1, String var2) {
        MobclickAgent.onEvent(mContext, var1, var2);
    }

    public static void onEvent(Context mContext, String var1) {
        MobclickAgent.onEvent(mContext, var1);
    }

}
