package com.topdon.commons.util;

/**
 * @Desc PDF
 * @ClassName PDFUtils
 * @Email 616862466@qq.com
 * @Author 子墨
 * @Date 2023/3/1 9:46
 */

public class PDFUtils {

    /**
     * 处理PDF特殊符号打不开的问题
     * //+，空格，/，?，%，#，&，=
     *
     * @param name pdfname
     * @return String
     */
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
