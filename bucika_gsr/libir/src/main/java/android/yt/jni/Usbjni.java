package android.yt.jni;

import android.util.Log;

/**
 * usb3803_hub是系统中的so库，部分定制的机型有可能会添加应用包名的白名单，也会导致不出图
 */
public class Usbjni {

    static {
        try {
            System.loadLibrary("usb3803_hub");
        } catch (UnsatisfiedLinkError e) {
            Log.e("Usbjni", "Couldn't load lib:   - " + e.getMessage());
        }
    }

    public static native int usb3803_mode_setting(int i);

    public static native int usb3803_read_parameter(int i);
}
