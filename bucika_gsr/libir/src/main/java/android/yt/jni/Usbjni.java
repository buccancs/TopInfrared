package android.yt.jni;

import android.util.Log;

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
