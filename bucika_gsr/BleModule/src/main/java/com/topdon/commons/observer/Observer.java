package com.topdon.commons.observer;

public interface Observer {
    
    @Observe
    default void onChanged(Object o) {}
}
