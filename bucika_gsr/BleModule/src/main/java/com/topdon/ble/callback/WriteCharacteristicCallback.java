package com.topdon.ble.callback;



import com.topdon.ble.Request;

/**
 * date: 2021/8/12 17:40
 * author: bichuanfeng
 */
public interface WriteCharacteristicCallback extends RequestFailedCallback {
    /**
     * 成功写入特征值
     *
     * @param request 请求
     * @param value   写入的数据
     */
    void onCharacteristicWrite(Request request, byte[] value);
}
