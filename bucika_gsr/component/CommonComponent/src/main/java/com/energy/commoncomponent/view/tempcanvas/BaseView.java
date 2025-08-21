package com.energy.commoncomponent.view.tempcanvas;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by fengjibo on 2023/6/28.
 */
public abstract class BaseView {
    protected String mId;
    protected String mLabel; //标记内容
    protected String mNote; //备注
    protected double mMaxTemp;//最大温度
    protected double mMinTemp;//最大温度
    protected double mAvgTemp;//最小温度
    protected int mPointSize = 0;
    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public double getMaxTemp() {
        return mMaxTemp;
    }

    public void setMaxTemp(double maxTemp) {
        this.mMaxTemp = maxTemp;
    }

    public double getMinTemp() {
        return mMinTemp;
    }

    public void setMinTemp(double minTemp) {
        this.mMinTemp = minTemp;
    }

    public double getAvgTemp() {
        return mAvgTemp;
    }

    public void setAvgTemp(double avgTemp) {
        this.mAvgTemp = avgTemp;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        this.mNote = note;
    }


    public Bitmap getCustomSizeImg(Bitmap rootImg, int goalW, int goalH) {
        int rootW = rootImg.getWidth();
        int rootH = rootImg.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(goalW * 1.0f / rootW, goalH * 1.0f / rootH);
        return Bitmap.createBitmap(rootImg, 0, 0, rootW, rootH, matrix, true);
    }
}
