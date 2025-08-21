package android.yt.jni;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Usbjni是framework提供的jni接口，必须保持类名不变，由继承类实现so加载判断。
 */
public class Usbcontorl extends Usbjni {

    public static boolean isload = false;

    static {
        File file = new File("/proc/self/maps");
        if (file.exists() && file.isFile()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String tempString = null;
                // 一次读入一行，直到读入null为文件结束
                while ((tempString = reader.readLine()) != null) {
                    // 显示行号
                    if (tempString.contains("libusb3803_hub.so")) {
                        isload = true;
                        break;
                    }
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e1) {
                    }
                }
            }
        }
    }
}
