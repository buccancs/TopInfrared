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

/**
 *
 */
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

    /**
     * 获取车型名字
     *
     * @param path 车型路径
     * @return String
     */
    public static String getVehicleName(String path) {
        File file = new File(path + "/Diag.ini");
        if (!file.exists()) {
            return "INI_LOST";
        }
//        return UTF8StringUtils.readByUtf8WithOutBom(path + "/Diag.ini");
        return readFileInfo(path + "/Diag.ini");
    }


    /**
     * 读取文件
     *
     * @param path 路径
     */
    private static String readFileInfo(String path) {
        String name = "";
        File file = new File(path);
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory()) {
            LLog.d("TestFile", "The File doesn't not exist.");
        } else {
            try {
                InputStream instream = new FileInputStream(file);
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line;
                //分行读取
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
//            e.printStackTrace();
            LLog.e("bcf", "INI: error: " + e.getMessage());
        }
        return "";
    }


    /**
     * 获取保养类型
     *
     * @param path
     * @param name
     * @return
     */
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
            if (!TextUtils.isEmpty(versionSection.get("base_ver"))) {
                hashMap.put("base_ver", versionSection.get("base_ver"));
            } else hashMap.put("base_ver", "0");
            if (!TextUtils.isEmpty(versionSection.get("base_rdtc"))) {
                hashMap.put("base_rdtc", versionSection.get("base_rdtc"));
            } else hashMap.put("base_rdtc", "0");
            if (!TextUtils.isEmpty(versionSection.get("base_cdtc"))) {
                hashMap.put("base_cdtc", versionSection.get("base_cdtc"));
            } else hashMap.put("base_cdtc", "0");
            if (!TextUtils.isEmpty(versionSection.get("base_rds"))) {
                hashMap.put("base_rds", versionSection.get("base_rds"));
            } else hashMap.put("base_rds", "0");
            if (!TextUtils.isEmpty(versionSection.get("base_act"))) {
                hashMap.put("base_act", versionSection.get("base_act"));
            } else hashMap.put("base_act", "0");
            if (!TextUtils.isEmpty(versionSection.get("base_fframe"))) {
                hashMap.put("base_fframe", versionSection.get("base_fframe"));
            } else hashMap.put("base_fframe", "0");
            if (!TextUtils.isEmpty(versionSection.get("oilreset"))) {
                hashMap.put("oilreset", versionSection.get("oilreset"));
            } else hashMap.put("oilreset", "0");
            if (!TextUtils.isEmpty(versionSection.get("throttle"))) {
                hashMap.put("throttle", versionSection.get("throttle"));
            } else hashMap.put("throttle", "0");
            if (!TextUtils.isEmpty(versionSection.get("epb"))) {
                hashMap.put("epb", versionSection.get("epb"));
            } else hashMap.put("epb", "0");
            if (!TextUtils.isEmpty(versionSection.get("abs"))) {
                hashMap.put("abs", versionSection.get("abs"));
            } else hashMap.put("abs", "0");
            if (!TextUtils.isEmpty(versionSection.get("steering"))) {
                hashMap.put("steering", versionSection.get("steering"));
            } else hashMap.put("steering", "0");
            if (!TextUtils.isEmpty(versionSection.get("dpf"))) {
                hashMap.put("dpf", versionSection.get("dpf"));
            } else hashMap.put("dpf", "0");
            if (!TextUtils.isEmpty(versionSection.get("airbag"))) {
                hashMap.put("airbag", versionSection.get("airbag"));
            } else hashMap.put("airbag", "0");
            if (!TextUtils.isEmpty(versionSection.get("bms"))) {
                hashMap.put("bms", versionSection.get("bms"));
            } else hashMap.put("bms", "0");
            if (!TextUtils.isEmpty(versionSection.get("adas"))) {
                hashMap.put("adas", versionSection.get("adas"));
            } else hashMap.put("adas", "0");
            if (!TextUtils.isEmpty(versionSection.get("immo"))) {
                hashMap.put("immo", versionSection.get("immo"));
            } else hashMap.put("immo", "0");
            if (!TextUtils.isEmpty(versionSection.get("smart_key"))) {
                hashMap.put("smart_key", versionSection.get("smart_key"));
            } else hashMap.put("smart_key", "0");
            if (!TextUtils.isEmpty(versionSection.get("password_reading"))) {
                hashMap.put("password_reading", versionSection.get("password_reading"));
            } else hashMap.put("password_reading", "0");
            if (!TextUtils.isEmpty(versionSection.get("brake_replace"))) {
                hashMap.put("brake_replace", versionSection.get("brake_replace"));
            } else hashMap.put("brake_replace", "0");
            if (!TextUtils.isEmpty(versionSection.get("injector_code"))) {
                hashMap.put("injector_code", versionSection.get("injector_code"));
            } else hashMap.put("injector_code", "0");
            if (!TextUtils.isEmpty(versionSection.get("suspension"))) {
                hashMap.put("suspension", versionSection.get("suspension"));
            } else hashMap.put("suspension", "0");
            if (!TextUtils.isEmpty(versionSection.get("tire_pressure"))) {
                hashMap.put("tire_pressure", versionSection.get("tire_pressure"));
            } else hashMap.put("tire_pressure", "0");
            if (!TextUtils.isEmpty(versionSection.get("ransmission"))) {
                hashMap.put("ransmission", versionSection.get("ransmission"));
            } else hashMap.put("ransmission", "0");
            if (!TextUtils.isEmpty(versionSection.get("gearbox_learning"))) {
                hashMap.put("gearbox_learning", versionSection.get("gearbox_learning"));
            } else hashMap.put("gearbox_learning", "0");
            if (!TextUtils.isEmpty(versionSection.get("transport_mode"))) {
                hashMap.put("transport_mode", versionSection.get("transport_mode"));
            } else hashMap.put("transport_mode", "0");
            if (!TextUtils.isEmpty(versionSection.get("head_light"))) {
                hashMap.put("head_light", versionSection.get("head_light"));
            } else hashMap.put("head_light", "0");
            if (!TextUtils.isEmpty(versionSection.get("sunroof_init"))) {
                hashMap.put("sunroof_init", versionSection.get("sunroof_init"));
            } else hashMap.put("sunroof_init", "0");
            if (!TextUtils.isEmpty(versionSection.get("seat_cali"))) {
                hashMap.put("seat_cali", versionSection.get("seat_cali"));
            } else hashMap.put("seat_cali", "0");
            if (!TextUtils.isEmpty(versionSection.get("window_cali"))) {
                hashMap.put("window_cali", versionSection.get("window_cali"));
            } else hashMap.put("window_cali", "0");
            if (!TextUtils.isEmpty(versionSection.get("start_stop"))) {
                hashMap.put("start_stop", versionSection.get("start_stop"));
            } else hashMap.put("start_stop", "0");
            if (!TextUtils.isEmpty(versionSection.get("egr"))) {
                hashMap.put("egr", versionSection.get("egr"));
            } else hashMap.put("egr", "0");
            if (!TextUtils.isEmpty(versionSection.get("odometer"))) {
                hashMap.put("odometer", versionSection.get("odometer"));
            } else hashMap.put("odometer", "0");
            if (!TextUtils.isEmpty(versionSection.get("language"))) {
                hashMap.put("language", versionSection.get("language"));
            } else hashMap.put("language", "0");
            if (!TextUtils.isEmpty(versionSection.get("tire_modified"))) {
                hashMap.put("tire_modified", versionSection.get("tire_modified"));
            } else hashMap.put("tire_modified", "0");
            if (!TextUtils.isEmpty(versionSection.get("a_f_adj"))) {
                hashMap.put("a_f_adj", versionSection.get("a_f_adj"));
            } else hashMap.put("a_f_adj", "0");
            if (!TextUtils.isEmpty(versionSection.get("electronic_pump"))) {
                hashMap.put("electronic_pump", versionSection.get("electronic_pump"));
            } else hashMap.put("electronic_pump", "0");
            if (!TextUtils.isEmpty(versionSection.get("nox_reset"))) {
                hashMap.put("nox_reset", versionSection.get("nox_reset"));
            } else hashMap.put("nox_reset", "0");
            if (!TextUtils.isEmpty(versionSection.get("urea_reset"))) {
                hashMap.put("urea_reset", versionSection.get("urea_reset"));
            } else hashMap.put("urea_reset", "0");
            if (!TextUtils.isEmpty(versionSection.get("turbine_learning"))) {
                hashMap.put("turbine_learning", versionSection.get("turbine_learning"));
            } else hashMap.put("turbine_learning", "0");
            if (!TextUtils.isEmpty(versionSection.get("cylinder"))) {
                hashMap.put("cylinder", versionSection.get("cylinder"));
            } else hashMap.put("cylinder", "0");
            if (!TextUtils.isEmpty(versionSection.get("eeprom"))) {
                hashMap.put("eeprom", versionSection.get("eeprom"));
            } else hashMap.put("eeprom", "0");
            if (!TextUtils.isEmpty(versionSection.get("exhaust_processing"))) {
                hashMap.put("exhaust_processing", versionSection.get("exhaust_processing"));
            } else hashMap.put("exhaust_processing", "0");
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            return hashMap;
        }
    }


    /**
     * 获取保养类型
     *
     * @param path
     * @param name
     * @return
     */
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

            if (!TextUtils.isEmpty(versionSection.get("ecm"))) {
                hashMap.put("ecm", versionSection.get("ecm"));
            } else hashMap.put("ecm", "0");
            if (!TextUtils.isEmpty(versionSection.get("tcm"))) {
                hashMap.put("tcm", versionSection.get("tcm"));
            } else hashMap.put("tcm", "0");
            if (!TextUtils.isEmpty(versionSection.get("abs"))) {
                hashMap.put("abs", versionSection.get("abs"));
            } else hashMap.put("abs", "0");
            if (!TextUtils.isEmpty(versionSection.get("srs"))) {
                hashMap.put("srs", versionSection.get("srs"));
            } else hashMap.put("srs", "0");
            if (!TextUtils.isEmpty(versionSection.get("hvac"))) {
                hashMap.put("hvac", versionSection.get("hvac"));
            } else hashMap.put("hvac", "0");
            if (!TextUtils.isEmpty(versionSection.get("adas"))) {
                hashMap.put("adas", versionSection.get("adas"));
            } else hashMap.put("adas", "0");
            if (!TextUtils.isEmpty(versionSection.get("immo"))) {
                hashMap.put("immo", versionSection.get("immo"));
            } else hashMap.put("immo", "0");
            if (!TextUtils.isEmpty(versionSection.get("bms"))) {
                hashMap.put("bms", versionSection.get("bms"));
            } else hashMap.put("bms", "0");
            if (!TextUtils.isEmpty(versionSection.get("eps"))) {
                hashMap.put("eps", versionSection.get("eps"));
            } else hashMap.put("eps", "0");
            if (!TextUtils.isEmpty(versionSection.get("led"))) {
                hashMap.put("led", versionSection.get("led"));
            } else hashMap.put("led", "0");
            if (!TextUtils.isEmpty(versionSection.get("ic"))) {
                hashMap.put("ic", versionSection.get("ic"));
            } else hashMap.put("ic", "0");
            if (!TextUtils.isEmpty(versionSection.get("informa"))) {
                hashMap.put("informa", versionSection.get("informa"));
            } else hashMap.put("informa", "0");
            if (!TextUtils.isEmpty(versionSection.get("bcm"))) {
                hashMap.put("bcm", versionSection.get("bcm"));
            } else hashMap.put("bcm", "0");

            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            return hashMap;
        }
    }
}
