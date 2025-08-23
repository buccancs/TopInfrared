package com.topdon.commons.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class UTF8StringUtils {

    public static String readByUtf8WithBom(String path) {
        File file = new File(path);
        FileInputStream in;
        Reader read;
        try {
            if (file.exists() && file.isFile()) {
                in = new FileInputStream(file);
                read = new InputStreamReader(in);
                BufferedReader bf = new BufferedReader(read);
                String txt;
                while ((txt = bf.readLine()) != null) {
                    
                    txt = txt.trim();
                    String flag = txt.substring(txt.lastIndexOf("|") + 1);
                    if (flag.equals("1")) {
                        return txt.substring(0, txt.lastIndexOf("|"));
                    }
                    return txt;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String readByUtf8WithOutBom(String path) {
        File file = new File(path);
        FileInputStream in;
        try {
            if (file.exists() && file.isFile()) {
                in = new FileInputStream(file);
                BufferedReader bf = new BufferedReader(new UnicodeReader(in, "utf-8"));
                String txt = "";
                while ((txt = bf.readLine()) != null) {
                    
                    txt = txt.trim();
                    String flag = txt.substring(txt.lastIndexOf("|") + 1);
                    if (flag.equals("1")) {
                        return txt.substring(0, txt.lastIndexOf("|"));
                    }
                    return txt;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
