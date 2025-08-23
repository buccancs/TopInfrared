

package com.github.gzuliyujiang.wheelview.widget;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

public class NumberWheelView extends WheelView {

    public NumberWheelView(Context context) {
        super(context);
    }

    public NumberWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberWheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected List<?> generatePreviewData() {
        List<Integer> data = new ArrayList<>();
        for (int i = 1; i <= 10; i = i + 1) {
            data.add(i);
        }
        return data;
    }

    @Deprecated
    @Override
    public void setData(List<?> data) {
        if (isInEditMode()) {
            super.setData(generatePreviewData());
        } else {
            throw new UnsupportedOperationException("Use setRange instead");
        }
    }

    public void setRange(int min, int max, int step) {
        int minValue = Math.min(min, max);
        int maxValue = Math.max(min, max);
        int capacity = (maxValue - minValue) / step;
        List<Integer> data = new ArrayList<>(capacity);
        for (int i = minValue; i <= maxValue; i = i + step) {
            data.add(i);
        }
        super.setData(data);
    }

    public void setRange(float min, float max, float step) {
        float minValue = Math.min(min, max);
        float maxValue = Math.max(min, max);
        int capacity = (int) ((maxValue - minValue) / step);
        List<Float> data = new ArrayList<>(capacity);
        for (float i = minValue; i <= maxValue; i = i + step) {
            data.add(i);
        }
        super.setData(data);
    }

}
