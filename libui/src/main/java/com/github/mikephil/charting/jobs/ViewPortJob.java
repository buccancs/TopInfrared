
package com.github.mikephil.charting.jobs;

import android.view.View;

import com.github.mikephil.charting.utils.ObjectPool;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

public abstract class ViewPortJob extends ObjectPool.Poolable implements Runnable {

    protected float[] pts = new float[2];

    protected ViewPortHandler mViewPortHandler;
    protected float xValue = 0f;
    protected float yValue = 0f;
    protected Transformer mTrans;
    protected View view;

    public ViewPortJob(ViewPortHandler viewPortHandler, float xValue, float yValue,
                       Transformer trans, View v) {

        this.mViewPortHandler = viewPortHandler;
        this.xValue = xValue;
        this.yValue = yValue;
        this.mTrans = trans;
        this.view = v;

    }

    public float getXValue() {
        return xValue;
    }

    public float getYValue() {
        return yValue;
    }
}
