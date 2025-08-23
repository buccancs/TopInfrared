

package com.github.gzuliyujiang.wheelpicker.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface DateMode {
    
    int NONE = -1;
    
    int YEAR_MONTH_DAY = 0;
    
    int YEAR_MONTH = 1;
    
    int MONTH_DAY = 2;
    
    int YEAR = 3;
}
