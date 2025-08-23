package com.topdon.commons.util;

public class PDFUtils {

    public static String getPdfName(String name) {
        name = name.replace('+', '-');
        name = name.replace(' ', '-');
        name = name.replace('/', '-');
        name = name.replace('?', '-');
        name = name.replace('%', '-');
        name = name.replace('#', '-');
        name = name.replace('&', '-');
        name = name.replace('=', '-');
        name = name.replace('\\', '-');
        name = name.replace(':', '-');
        name = name.replace('*', '-');
        name = name.replace('|', '-');
        name = name.replace('<', '-');
        name = name.replace('>', '-');
        name = name.replace('"', '-');
        return name;
    }
}
