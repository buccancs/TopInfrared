package com.topdon.commons.poster;

import androidx.annotation.NonNull;

/**
 * date: 2019/8/7 09:44
 * author: chuanfeng.bi
 */
interface Poster {
    /**
     * 将要执行的任务加入队列
     * 
     * @param runnable 要执行的任务
     */
    void enqueue(@NonNull Runnable runnable);

    /**
     * 清除队列任务
     */
    void clear();
}
