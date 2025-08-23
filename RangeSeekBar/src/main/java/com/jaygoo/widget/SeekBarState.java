package com.jaygoo.widget;

public class SeekBarState {
    public String indicatorText;
    public float value;
    public boolean isMin;
    public boolean isMax;

    @Override
    public String toString() {
        return "indicatorText: " + indicatorText + " ,isMin: " + isMin + " ,isMax: " + isMax;
    }
}
