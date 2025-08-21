package com.topdon.commons.base;

public class SaveBean {

    public String type;
    public String mac;
    public String name;

    public SaveBean(String type, String mac,String name) {
        this.type = type;
        this.mac = mac;
        this.name = name;
    }

    public SaveBean() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
