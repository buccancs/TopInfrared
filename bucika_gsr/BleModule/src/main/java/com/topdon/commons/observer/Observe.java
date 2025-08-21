package com.topdon.commons.observer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 是否开启观察（接收通知）
 * 
 * date: 2019/8/9 12:46
 * author: chuanfeng.bi
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Observe {
}
