package com.energy.commoncomponent.view.tempcanvas;

/**
 * Created by fengjibo on 2024/2/20.
 */
public class TempResultBean {
    private String id;
    private String label;
    private String content;
    private float minTemperature;
    private float maxTemperature;
    private float averageTemperature;
    private long order;

    private int position; //相对应点线框list的position

    //类型
    private TempInfoMode tempInfoMode;
    //环境温度
    private float ambientTemp;
    //测温距离
    private float measureDistance;
    //辐射率
    private float emissivity;
    //高温报警开关
    private boolean highAlertEnable;
    //高温阀值
    private float highThreshold;
    //低温报警开关
    private boolean lowAlertEnable;
    //低温阀值
    private float lowThreshold;

    private int x1;
    private int y1;
    private int x2_or_r1;
    private int y2_or_r2;
    private int max_temp_x;
    private int max_temp_y;
    private int min_temp_x;
    private int min_temp_y;

    public TempResultBean(String id, String label, String content, float minTemperature,
                             float maxTemperature, float averageTemperature) {
        this.id = id;
        this.label = label;
        this.content = content;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.averageTemperature = averageTemperature;
    }

    public float getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(float minTemperature) {
        this.minTemperature = minTemperature;
    }

    public float getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(float maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public float getAverageTemperature() {
        return averageTemperature;
    }

    public void setAverageTemperature(float averageTemperature) {
        this.averageTemperature = averageTemperature;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public TempInfoMode getTempInfoMode() {
        return tempInfoMode;
    }

    public void setTempInfoMode(TempInfoMode tempInfoMode) {
        this.tempInfoMode = tempInfoMode;
    }

    public float getAmbientTemp() {
        return ambientTemp;
    }

    public void setAmbientTemp(float ambientTemp) {
        this.ambientTemp = ambientTemp;
    }

    public float getMeasureDistance() {
        return measureDistance;
    }

    public void setMeasureDistance(float measureDistance) {
        this.measureDistance = measureDistance;
    }

    public float getEmissivity() {
        return emissivity;
    }

    public void setEmissivity(float emissivity) {
        this.emissivity = emissivity;
    }

    public boolean isHighAlertEnable() {
        return highAlertEnable;
    }

    public void setHighAlertEnable(boolean highAlertEnable) {
        this.highAlertEnable = highAlertEnable;
    }

    public float getHighThreshold() {
        return highThreshold;
    }

    public void setHighThreshold(float highThreshold) {
        this.highThreshold = highThreshold;
    }

    public boolean isLowAlertEnable() {
        return lowAlertEnable;
    }

    public void setLowAlertEnable(boolean lowAlertEnable) {
        this.lowAlertEnable = lowAlertEnable;
    }

    public float getLowThreshold() {
        return lowThreshold;
    }

    public void setLowThreshold(float lowThreshold) {
        this.lowThreshold = lowThreshold;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getX2_or_r1() {
        return x2_or_r1;
    }

    public void setX2_or_r1(int x2_or_r1) {
        this.x2_or_r1 = x2_or_r1;
    }

    public int getY2_or_r2() {
        return y2_or_r2;
    }

    public void setY2_or_r2(int y2_or_r2) {
        this.y2_or_r2 = y2_or_r2;
    }

    public int getMax_temp_x() {
        return max_temp_x;
    }

    public void setMax_temp_x(int max_temp_x) {
        this.max_temp_x = max_temp_x;
    }

    public int getMax_temp_y() {
        return max_temp_y;
    }

    public void setMax_temp_y(int max_temp_y) {
        this.max_temp_y = max_temp_y;
    }

    public int getMin_temp_x() {
        return min_temp_x;
    }

    public void setMin_temp_x(int min_temp_x) {
        this.min_temp_x = min_temp_x;
    }

    public int getMin_temp_y() {
        return min_temp_y;
    }

    public void setMin_temp_y(int min_temp_y) {
        this.min_temp_y = min_temp_y;
    }
}
