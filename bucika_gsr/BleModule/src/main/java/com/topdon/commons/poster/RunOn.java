package com.topdon.commons.poster;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RunOn {
    
    ThreadMode value() default ThreadMode.UNSPECIFIED;
}
