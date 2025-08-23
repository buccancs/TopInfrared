

package com.github.gzuliyujiang.dialog;

import android.util.Log;

import androidx.annotation.NonNull;

public final class DialogLog {
    private static final String TAG = "AndroidPicker";
    private static boolean enable = false;

    private DialogLog() {
        super();
    }

    public static void enable() {
        enable = true;
    }

    public static void print(@NonNull Object log) {
        if (!enable) {
            return;
        }
        Log.d(TAG, log.toString());
    }

}
