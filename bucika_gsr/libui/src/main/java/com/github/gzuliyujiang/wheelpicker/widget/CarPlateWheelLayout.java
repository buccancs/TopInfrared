

package com.github.gzuliyujiang.wheelpicker.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;

import com.github.gzuliyujiang.wheelpicker.impl.CarPlateProvider;

public class CarPlateWheelLayout extends LinkageWheelLayout {
    private CarPlateProvider provider;

    public CarPlateWheelLayout(Context context) {
        super(context);
    }

    public CarPlateWheelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CarPlateWheelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CarPlateWheelLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onInit(@NonNull Context context) {
        super.onInit(context);
        provider = new CarPlateProvider();
        setData(provider);
    }

    @Override
    protected void onAttributeSet(@NonNull Context context, @NonNull TypedArray typedArray) {
        super.onAttributeSet(context, typedArray);
        setFirstVisible(provider.firstLevelVisible());
        setThirdVisible(provider.thirdLevelVisible());
    }

}
