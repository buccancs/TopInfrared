

package com.github.gzuliyujiang.wheelpicker.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface TimeMode {
    
    int NONE = -1;
    
    int HOUR_24_NO_SECOND = 0;
    
    int HOUR_24_HAS_SECOND = 1;
    
    int HOUR_12_NO_SECOND = 2;
    
    int HOUR_12_HAS_SECOND = 3;
}
