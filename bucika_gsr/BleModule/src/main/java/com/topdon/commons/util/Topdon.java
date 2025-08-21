package com.topdon.commons.util;

import android.content.Context;

public class Topdon {
    private static Context app;

    public static void init(Context context) {
        app = context;
    }

    public static Context getApp() {
        return app;
    }
}
