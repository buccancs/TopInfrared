package android.yt.jni;

import android.util.Log;

public class Usbjni {

    private static final String TAG = "Usbjni";

    static {
        try {
            System.loadLibrary("usb3803_hub");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            Log.e(TAG, "Couldn't load lib:   - " + e.getMessage());
        }
    }

    public static int setUSB3803Mode(boolean isPowerOn) {
        if (isPowerOn) {
            return usb3803_mode_setting(1);
        } else {
            return usb3803_mode_setting(0);
        }
    }

    public static int readUSB3803Parameter(int i) {
        return usb3803_read_parameter(i);
    }

    static native int usb3803_mode_setting(int i);

    static native int usb3803_read_parameter(int i);
}
