package com.energy.commoncomponent.bean;

/**
 * Created by fengjibo on 2023/8/10.
 */
public enum DeviceType {
    DEVICE_TYPE_TC2C("TC2C"),
    DEVICE_TYPE_WN2256("WN2256"),
    DEVICE_TYPE_WN2384("WN2384"),
    DEVICE_TYPE_WN2640("WN2640"),
    DEVICE_TYPE_X3("X3"),
    DEVICE_TYPE_P2L("P2L"),
    DEVICE_TYPE_X2PRO("X2PRO"),
    DEVICE_TYPE_GL1280("GL1280");

    private String type;
    DeviceType(String type) {
        this.type = type;
    }
}
