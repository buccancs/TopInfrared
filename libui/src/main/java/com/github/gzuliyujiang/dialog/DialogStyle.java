

package com.github.gzuliyujiang.dialog;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface DialogStyle {
    int Default = 0;
    int One = 1;
    int Two = 2;
    int Three = 3;
}
