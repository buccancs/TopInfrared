package com.topdon.commons.util;

import android.text.TextUtils;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;

public class SystemIniUtils {
    public static int getSystemVersion(String path, String systemName, int systemVersion) {
        File file = new File(path + "/Version.ini");
        if (!file.exists()) {
            LLog.e("bcf", "  ini不存在：" + file.getPath());
            return -1;
        }
        Config cfg = new Config();
        cfg.setLowerCaseOption(true);
        cfg.setLowerCaseSection(true);
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
            ini.load(file);
            Profile.Section tDartSWSection = ini.get("OTA".toLowerCase());
            if (tDartSWSection == null) {
                return 1;
            }
            String firmwareSw = "";
            String version = "";
            if (!TextUtils.isEmpty(tDartSWSection.get("firmwaresw"))) {
                firmwareSw = tDartSWSection.get("FirmwareSW".toLowerCase());
            }

            if (!TextUtils.isEmpty(tDartSWSection.get("version"))) {
                version = tDartSWSection.get("Version".toLowerCase());
            }

            if (StringUtils.isEmpty(firmwareSw)) {
                return 1;
            }
            if (StringUtils.isEmpty(version)) {
                return 1;
            }
            String version1 = version.toUpperCase().replace("V", "");
            if (systemName.equals(firmwareSw) && version1.equals(String.valueOf(systemVersion))) {
                return 0;
            } else {
                return 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }
}
