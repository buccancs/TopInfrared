package com.topdon.ble;



/**
 * 配对控制器
 * <p>
 * date: 2021/8/12 12:59
 * author: bichuanfeng
 */
public interface BondController {
    /**
     * 配对控制
     * 
     * @param device 设备
     */
    boolean accept(Device device);
}
