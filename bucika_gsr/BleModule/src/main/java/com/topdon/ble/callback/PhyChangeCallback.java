package com.topdon.ble.callback;

import android.bluetooth.BluetoothDevice;



import com.topdon.ble.Request;

/**
 * date: 2021/8/12 17:43
 * author: bichuanfeng
 */
public interface PhyChangeCallback extends RequestFailedCallback {
    /**
     * @param request 请求
     * @param txPhy   物理层发送器偏好。{@link BluetoothDevice#PHY_LE_1M_MASK}等
     * @param rxPhy   物理层接收器偏好。{@link BluetoothDevice#PHY_LE_1M_MASK}等
     */
    void onPhyChange(Request request, int txPhy, int rxPhy);
}
