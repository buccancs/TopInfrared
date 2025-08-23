package com.energy.commoncomponent.view.tempcanvas;

import android.content.Context;
import android.graphics.Canvas;

public abstract class BaseDraw {
    protected Context mContext;
    
    protected final static int MIN_SIZE_PIX_COUNT = 20;
    protected int mScreenDegree = 0;
    protected int mTouchIndex = -1;
    protected int mViewWidth;
    protected int mViewHeight;

    public BaseDraw(Context context) {
        mContext = context;
    }

    public void setViewWidth(int viewWidth) {
        this.mViewWidth = viewWidth;
    }

    public void setViewHeight(int viewHeight) {
        this.mViewHeight = viewHeight;
    }

    abstract void onDraw(Canvas canvas, boolean isScroll);

    public int getTouchInclude() {
        return mTouchIndex;
    }

    public boolean isTouch() {
        return mTouchIndex != -1;
    }

}
