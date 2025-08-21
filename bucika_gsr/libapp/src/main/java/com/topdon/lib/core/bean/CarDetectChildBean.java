package com.topdon.lib.core.bean;

import com.topdon.lib.core.utils.TemperatureUtil;

/**
 * @author qiang.lv
 */
public class CarDetectChildBean {
    public int type;
    public int pos;
    public String description;
    public String item;
    public String temperature;
    public boolean isSelected;

    public CarDetectChildBean(int type,int pos,String description, String item, String temperature) {
        this.type = type;
        this.pos = pos;
        this.description = description;
        this.item = item;
        this.temperature = temperature;
    }

    public String buildString() {
        String[] temperatures = temperature.split("~");
        return item + TemperatureUtil.INSTANCE.getTempStr(Integer.parseInt(temperatures[0]), Integer.parseInt(temperatures[1]));
    }
}
