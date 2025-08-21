package com.topdon.ble.callback;




import com.topdon.ble.Request;

/**
 * date: 2021/8/12 17:39
 * author: bichuanfeng
 */
public interface RequestFailedCallback extends RequestCallback {
    /**
     * 请求失败
     *
     * @param request  请求
     * @param failType 失败类型。{@link Connection#REQUEST_FAIL_TYPE_GATT_IS_NULL}等
     * @param value    请求时带的数据，可能为null
     */
    void onRequestFailed(Request request, int failType, Object value);
}
