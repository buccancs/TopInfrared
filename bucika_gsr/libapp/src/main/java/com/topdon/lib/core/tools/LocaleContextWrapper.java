package com.topdon.lib.core.tools;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleContextWrapper extends ContextWrapper {

    public LocaleContextWrapper(Context base) {
        super(base);
    }

    public static ContextWrapper wrap(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        return new ContextWrapper(context.createConfigurationContext(config));
    }
}

