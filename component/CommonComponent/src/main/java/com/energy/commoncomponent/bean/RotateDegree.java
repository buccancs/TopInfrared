package com.energy.commoncomponent.bean;

public enum RotateDegree {

    DEGREE_0(0),
    DEGREE_90(1),
    DEGREE_180(2),
    DEGREE_270(3);

    private final int value;

    RotateDegree(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static RotateDegree valueOf(int value) {
        RotateDegree[] types = RotateDegree.values();
        for(RotateDegree type: types){
            if (type.value == value) {
                return type;
            }
        }
        return DEGREE_0;
    }
}
