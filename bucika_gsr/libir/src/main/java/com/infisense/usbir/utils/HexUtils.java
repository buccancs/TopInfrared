package com.infisense.usbir.utils;

/**
 * Created by fengjibo on 2022/12/9.
 */
public class HexUtils {

    /**
     * 将字节数组转换成十六进制的字符串
     * @return
     */
    public static String binaryToHexString(byte[] bytes) {
        String hexStr = "0123456789ABCDEF";
        String result = "";
        String hex = "";
        for (byte b : bytes) {
            hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
            hex += String.valueOf(hexStr.charAt(b & 0x0F));
            result += hex + " ";
        }
        return result;
    }
}
