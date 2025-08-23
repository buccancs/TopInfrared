package com.infisense.usbdual.camera;

public class BaseParamDualView {
    protected int mIrWidth;
    protected int mIrHeight;
    protected int mVlWidth;
    protected int mVlHeight;
    protected int mDualWidth;
    protected int mDualHeight;

    public BaseParamDualView(int mIrWidth, int mIrHeight, int mVlWidth, int mVlHeight, int mDualWidth, int mDualHeight) {
        this.mIrWidth = mIrWidth;
        this.mIrHeight = mIrHeight;
        this.mVlWidth = mVlWidth;
        this.mVlHeight = mVlHeight;
        this.mDualWidth = mDualWidth;
        this.mDualHeight = mDualHeight;
    }
}
