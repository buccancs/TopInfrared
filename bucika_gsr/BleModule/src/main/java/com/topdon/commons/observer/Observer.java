package com.topdon.commons.observer;

/**
 * 观察者
 * <p>
 * date: 2019/8/3 13:15
 * author: chuanfeng.bi
 */
public interface Observer {
    /**
     * 数据变化
     */
    @Observe
    default void onChanged(Object o) {}
}
