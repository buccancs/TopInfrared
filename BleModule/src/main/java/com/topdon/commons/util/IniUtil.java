package com.topdon.commons.util;

import android.text.TextUtils;

import org.ini4j.Config;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

public class IniUtil {
    private static String NAME = "Link";
    private static final String LINK = "link";
    private static final String LINK_NAME = "name";
    private static final String LANGUAGE = "language";
    private static final String VERSION = "version";
    private static final String MAINTENANCE = "maintenance";
    private static final String SYSTEM = "system";

    public static String getLink(String path) {
        File file = new File(path + "/Diag.ini");
        if (!file.exists())
            return "";
        Config cfg = new Config();
        cfg.setLowerCaseOption(true);
        cfg.setLowerCaseSection(true);
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
            ini.load(file);
            Section linkSection = ini.get(LINK);
            if (linkSection == null)
                return "";
            return linkSection.get(LINK_NAME);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getVehicleName(String path) {
        File file = new File(path + "/Diag.ini");
        if (!file.exists()) {
            return "INI_LOST";
        }
        return readFileInfo(path + "/Diag.ini");
    }

    private static String readFileInfo(String path) {
        String name = "";
        File file = new File(path);
        if (file.isDirectory()) {
            LLog.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                while ((line = buffreader.readLine()) != null) {
                    LLog.e("TestFile", "ReadTxtFile: " + line);
                    name = line;
                    break;
                }
                instream.close();
            } catch (java.io.FileNotFoundException e) {
                LLog.d("TestFile", "The File doesn't not exist.");
            } catch (IOException e) {
                LLog.d("TestFile", e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return name;
    }

    public static String getVersion(String path, String name) {
        File file = new File(path + "/Diag.ini");
        if (!file.exists()) {
            LLog.e("bcf", name + "  ini不存在：" + file.getPath());
            return "INI_LOST";
        }
        Config cfg = new Config();
        cfg.setLowerCaseOption(true);
        cfg.setLowerCaseSection(true);
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
            ini.load(file);
            Section versionSection = ini.get(name.toLowerCase());
            if (versionSection == null)
                return "";
            return versionSection.get(VERSION);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getName(String language, String path) {
        File file = new File(path + "/Diag.ini");
        Config cfg = new Config();
        cfg.setLowerCaseOption(true);
        cfg.setLowerCaseSection(true);
        cfg.setMultiSection(true);
        cfg.setFileEncoding(Charset.forName("UTF-8"));
        Ini ini = new Ini();
        ini.setConfig(cfg);
        try {
            ini.load(file);
            Section languageSection = ini.get(LANGUAGE);
            if (languageSection == null)
                return "";
            return languageSection.get(language.toLowerCase());
        } catch (Exception e) {
            LLog.e("bcf", "INI: error: " + e.getMessage());
        }
        return "";
    }

    /**
     * Helper method to add a section value to the hashMap, using "0" as default if empty
     */
    private static void addSectionValue(HashMap<String, String> hashMap, Section section, String key) {
        String value = section.get(key);
        hashMap.put(key, !TextUtils.isEmpty(value) ? value : "0");
    }

    public static HashMap<String, String> getMaintenance(String path, String name) {
        HashMap<String, String> hashMap = new HashMap<>();
        File file = new File(path + "/Diag.ini");
        if (!file.exists()) {
            LLog.e("bcf", name + "  ini不存在：" + file.getPath());
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
            Section versionSection = ini.get(MAINTENANCE.toLowerCase());
            if (versionSection == null) {
                return hashMap;
            }
            // Use helper method to eliminate repetitive if-else patterns
            addSectionValue(hashMap, versionSection, "base_ver");
            addSectionValue(hashMap, versionSection, "base_rdtc");
            addSectionValue(hashMap, versionSection, "base_cdtc");
            addSectionValue(hashMap, versionSection, "base_rds");
            addSectionValue(hashMap, versionSection, "base_act");
            addSectionValue(hashMap, versionSection, "base_fframe");
            addSectionValue(hashMap, versionSection, "oilreset");
            addSectionValue(hashMap, versionSection, "throttle");
            addSectionValue(hashMap, versionSection, "epb");
            addSectionValue(hashMap, versionSection, "abs");
            addSectionValue(hashMap, versionSection, "steering");
            addSectionValue(hashMap, versionSection, "dpf");
            addSectionValue(hashMap, versionSection, "airbag");
            addSectionValue(hashMap, versionSection, "bms");
            addSectionValue(hashMap, versionSection, "adas");
            addSectionValue(hashMap, versionSection, "immo");
            addSectionValue(hashMap, versionSection, "smart_key");
            addSectionValue(hashMap, versionSection, "password_reading");
            addSectionValue(hashMap, versionSection, "brake_replace");
            addSectionValue(hashMap, versionSection, "injector_code");
            addSectionValue(hashMap, versionSection, "suspension");
            addSectionValue(hashMap, versionSection, "tire_pressure");
            addSectionValue(hashMap, versionSection, "ransmission");
            addSectionValue(hashMap, versionSection, "gearbox_learning");
            addSectionValue(hashMap, versionSection, "transport_mode");
            addSectionValue(hashMap, versionSection, "head_light");
            addSectionValue(hashMap, versionSection, "sunroof_init");
            addSectionValue(hashMap, versionSection, "seat_cali");
            addSectionValue(hashMap, versionSection, "window_cali");
            addSectionValue(hashMap, versionSection, "start_stop");
            addSectionValue(hashMap, versionSection, "egr");
            addSectionValue(hashMap, versionSection, "odometer");
            addSectionValue(hashMap, versionSection, "language");
            addSectionValue(hashMap, versionSection, "tire_modified");
            addSectionValue(hashMap, versionSection, "a_f_adj");
            addSectionValue(hashMap, versionSection, "electronic_pump");
            addSectionValue(hashMap, versionSection, "nox_reset");
            addSectionValue(hashMap, versionSection, "urea_reset");
            addSectionValue(hashMap, versionSection, "turbine_learning");
            addSectionValue(hashMap, versionSection, "cylinder");
            addSectionValue(hashMap, versionSection, "eeprom");
            addSectionValue(hashMap, versionSection, "exhaust_processing");
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            return hashMap;
        }
    }

    public static HashMap<String, String> getIniSysTem(String path, String name) {
        HashMap<String, String> hashMap = new HashMap<>();
        File file = new File(path + "/Diag.ini");
        if (!file.exists()) {
            LLog.e("bcf", name + "  ini不存在：" + file.getPath());
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
            Section versionSection = ini.get(SYSTEM.toLowerCase());
            if (versionSection == null) {
                return hashMap;
            }

            // Use helper method to eliminate repetitive if-else patterns
            addSectionValue(hashMap, versionSection, "ecm");
            addSectionValue(hashMap, versionSection, "tcm");
            addSectionValue(hashMap, versionSection, "abs");
            addSectionValue(hashMap, versionSection, "srs");
            addSectionValue(hashMap, versionSection, "hvac");
            addSectionValue(hashMap, versionSection, "adas");
            addSectionValue(hashMap, versionSection, "immo");
            addSectionValue(hashMap, versionSection, "bms");
            addSectionValue(hashMap, versionSection, "eps");
            addSectionValue(hashMap, versionSection, "led");
            addSectionValue(hashMap, versionSection, "ic");
            addSectionValue(hashMap, versionSection, "informa");
            addSectionValue(hashMap, versionSection, "bcm");

            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            return hashMap;
        }
    }
}
