package com.energy.commoncomponent.view.tempcanvas;

import android.content.Context;
import android.graphics.Canvas;

/**
 * Created by fengjibo on 2023/6/25.
 */
public abstract class BaseDraw {
    protected Context mContext;
    /**
     * 用于线和框最小尺寸判断
     */
    protected final static int MIN_SIZE_PIX_COUNT = 20;
    protected int mScreenDegree = 0;
    protected int mTouchIndex = -1;//手势按住已绘制的，进行拖拽
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

    /**
     * 获取当前选中点的数组index
     * @return
     */
    public int getTouchInclude() {
        return mTouchIndex;
    }

    /**
     * 手指是否选中了其中一个点
     * @return
     */
    public boolean isTouch() {
        return mTouchIndex != -1;
    }

}
