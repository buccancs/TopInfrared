package com.topdon.ble.callback;



import com.topdon.ble.Request;

/**
 * date: 2021/8/12 17:43
 * author: bichuanfeng
 */
public interface NotificationChangeCallback extends RequestFailedCallback {
    /**
     * 通知开关变化 / Indication开关变化
     *
     * @param request   请求
     * @param isEnabled 开启或关闭
     */
    void onNotificationChanged(Request request, boolean isEnabled);
}
