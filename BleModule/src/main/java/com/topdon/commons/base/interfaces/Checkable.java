package com.topdon.commons.base.interfaces;

public interface Checkable<T> {
    boolean isChecked();
    
    T setChecked(boolean isChecked);
}
