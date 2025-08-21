package com.topdon.commons.util;

import android.text.TextUtils;

/**
 * @Desc 版本对比
 * @ClassName VersionUtils
 * @Email 616862466@qq.com
 * @Author 子墨
 * @Date 2022/10/11 17:56
 */

public class VersionUtils {

    /**
     * 如果版本1 大于 版本2 返回true 否则返回fasle 支持 2.2 2.2.1 比较
     * 支持不同位数的比较  2.0.0.0.0.1  2.0 对比
     *
     * @param v1 版本服务器版本 " 1.1.2 "
     * @param v2 版本 当前版本 " 1.2.1 "
     * @return ture ：需要更新 false ： 不需要更新
     */
    public static boolean compareVersions(String v1, String v2) {
        //判断是否为空数据
        if (TextUtils.equals(v1, "") || TextUtils.equals(v2, "")) {
            return false;
        }
        String[] str1 = v1.split("\\.");
        String[] str2 = v2.split("\\.");

        if (str1.length == str2.length) {
            for (int i = 0; i < str1.length; i++) {
                if (Integer.parseInt(str1[i]) > Integer.parseInt(str2[i])) {
                    return true;
                } else if (Integer.parseInt(str1[i]) < Integer.parseInt(str2[i])) {
                    return false;
                } else if (Integer.parseInt(str1[i]) == Integer.parseInt(str2[i])) {

                }
            }
        } else {
            if (str1.length > str2.length) {
                for (int i = 0; i < str2.length; i++) {
                    if (Integer.parseInt(str1[i]) > Integer.parseInt(str2[i])) {
                        return true;
                    } else if (Integer.parseInt(str1[i]) < Integer.parseInt(str2[i])) {
                        return false;

                    } else if (Integer.parseInt(str1[i]) == Integer.parseInt(str2[i])) {
                        if (str2.length == 1) {
                            continue;
                        }
                        if (i == str2.length - 1) {

                            for (int j = i; j < str1.length; j++) {
                                if (Integer.parseInt(str1[j]) != 0) {
                                    return true;
                                }
                                if (j == str1.length - 1) {
                                    return false;
                                }

                            }
                            return true;
                        }
                    }
                }
            } else {
                for (int i = 0; i < str1.length; i++) {
                    if (Integer.parseInt(str1[i]) > Integer.parseInt(str2[i])) {
                        return true;
                    } else if (Integer.parseInt(str1[i]) < Integer.parseInt(str2[i])) {
                        return false;

                    } else if (Integer.parseInt(str1[i]) == Integer.parseInt(str2[i])) {
                        if (str1.length == 1) {
                            continue;
                        }
                        if (i == str1.length - 1) {
                            return false;

                        }
                    }

                }
            }
        }
        return false;
    }
}
