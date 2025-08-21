package com.topdon.lib.core.bean;

import java.util.List;

/**
 * @author qiang.lv
 */
public class CarDetectBean {
    public String title;
    public List<CarDetectChildBean> detectChildBeans;

    public CarDetectBean(String title, List<CarDetectChildBean> detectChildBeans) {
        this.title = title;
        this.detectChildBeans = detectChildBeans;
    }
}
