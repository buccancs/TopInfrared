package com.topdon.ble.callback;



import com.topdon.ble.Request;

/**
 * date: 2021/8/12 17:41
 * author: bichuanfeng
 */
public interface ReadDescriptorCallback extends RequestFailedCallback {
    /**
     * 读取到描述符值
     *
     * @param request 请求
     * @param value   读取到的数据
     */
    void onDescriptorRead(Request request, byte[] value);
}
