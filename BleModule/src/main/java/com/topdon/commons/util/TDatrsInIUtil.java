package com.topdon.commons.util;

import static com.topdon.lms.sdk.LMS.SUCCESS;

import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.topdon.lms.sdk.LMS;
import com.topdon.lms.sdk.network.IResponseCallback;
import com.topdon.lms.sdk.utils.StringUtils;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class TDatrsInIUtil {

    public static String getTdartsVersion(String path) {
        File file = new File(path + "T-darts.ini");
        if (!file.exists()) {
            LLog.e("bcf", "  ini不存在：" + file.getPath());
            return "";
        }
        Config cfg = new Config();
        cfg.setLowerCaseOption(true);
        cfg.setLowerCaseSection(true);
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
            ini.load(file);
            Profile.Section tDartSWSection = ini.get("TDartSW".toLowerCase());
            if (tDartSWSection == null) {
                return "";
            }
            if (!TextUtils.isEmpty(tDartSWSection.get("version"))) {
                return tDartSWSection.get("Version".toLowerCase());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    public static HashMap<String, String> getTdarts(String path) {
        HashMap<String, String> hashMap = new HashMap<>();
        File file = new File(path + "T-darts.ini");
        if (!file.exists()) {
            LLog.e("bcf", "  ini不存在：" + file.getPath());
            return hashMap;
        }
        Config cfg = new Config();
        cfg.setLowerCaseOption(true);
        cfg.setLowerCaseSection(true);
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
            ini.load(file);
            Profile.Section tDartSWSection = ini.get("TDartSW".toLowerCase());
            if (tDartSWSection == null) {
                return hashMap;
            }
            if (!TextUtils.isEmpty(tDartSWSection.get("version"))) {
                hashMap.put("Version", tDartSWSection.get("Version".toLowerCase()));
            }

            Profile.Section libsSection = ini.get("libs");
            if (libsSection == null) {
                return hashMap;
            }

            if (!TextUtils.isEmpty(libsSection.get("T-dartsApp".toLowerCase()))) {
                hashMap.put("T-dartsApp", libsSection.get("T-dartsApp".toLowerCase()));
            }
            if (!TextUtils.isEmpty(libsSection.get("825x_module".toLowerCase()))) {
                hashMap.put("825x_module", libsSection.get("825x_module".toLowerCase()));
            }
            if (!TextUtils.isEmpty(libsSection.get("N32S032-app".toLowerCase()))) {
                hashMap.put("N32S032-app", libsSection.get("N32S032-app".toLowerCase()));
            }
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            return hashMap;
        }
    }

    public static String getBinPath(int data) {
        String path = FolderUtil.getTdartsUpgradePath();
        if (data == 0) {
            return path + "T-dartsApp.bin";
        } else if (data == 1) {
            return path + "825x_module.bin";
        } else if (data == 2) {
            return path + "N32S032-app.bin";
        }
        return "";
    }
}
