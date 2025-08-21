package com.topdon.commons.base.interfaces;

/**
 * date: 2019/8/6 10:05
 * author: chuanfeng.bi
 */
public interface Checkable<T> {
    boolean isChecked();
    
    T setChecked(boolean isChecked);
}
