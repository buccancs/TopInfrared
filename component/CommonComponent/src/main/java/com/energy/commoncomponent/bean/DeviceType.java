package com.energy.commoncomponent.bean;

/**
 * Created by fengjibo on 2023/8/10.
 * Modified to support TC001 only.
 */
public enum DeviceType {
    DEVICE_TYPE_TC001("TC001");

    private String type;
    DeviceType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
}
