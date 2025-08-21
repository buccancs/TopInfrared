package com.topdon.ble.callback;


import com.topdon.ble.Request;

/**
 * date: 2021/8/12 17:42
 * author: bichuanfeng
 */
public interface MtuChangeCallback extends RequestFailedCallback {
    /**
     * 最大传输单元变化
     *
     * @param request 请求
     * @param mtu     最大传输单元新的值
     */
    void onMtuChanged(Request request, int mtu);
}
