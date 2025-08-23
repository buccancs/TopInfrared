
package com.github.mikephil.charting.utils;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.View;

public class ViewPortHandler {

    protected final Matrix mMatrixTouch = new Matrix();

    protected RectF mContentRect = new RectF();

    protected float mChartWidth = 0f;
    protected float mChartHeight = 0f;

    private float mMinScaleY = 1f;

    private float mMaxScaleY = Float.MAX_VALUE;

    private float mMinScaleX = 1f;

    private float mMaxScaleX = Float.MAX_VALUE;

    private float mScaleX = 1f;

    private float mScaleY = 1f;

    private float mTransX = 0f;

    private float mTransY = 0f;

    private float mTransOffsetX = 0f;

    private float mTransOffsetY = 0f;

    public ViewPortHandler() {

    }

    public void setChartDimens(float width, float height) {

        float offsetLeft = this.offsetLeft();
        float offsetTop = this.offsetTop();
        float offsetRight = this.offsetRight();
        float offsetBottom = this.offsetBottom();

        mChartHeight = height;
        mChartWidth = width;

        restrainViewPort(offsetLeft, offsetTop, offsetRight, offsetBottom);
    }

    public boolean hasChartDimens() {
        if (mChartHeight > 0 && mChartWidth > 0)
            return true;
        else
            return false;
    }

    public void restrainViewPort(float offsetLeft, float offsetTop, float offsetRight,
                                 float offsetBottom) {
        mContentRect.set(offsetLeft, offsetTop, mChartWidth - offsetRight, mChartHeight
                - offsetBottom);
    }

    public float offsetLeft() {
        return mContentRect.left;
    }

    public float offsetRight() {
        return mChartWidth - mContentRect.right;
    }

    public float offsetTop() {
        return mContentRect.top;
    }

    public float offsetBottom() {
        return mChartHeight - mContentRect.bottom;
    }

    public float contentTop() {
        return mContentRect.top;
    }

    public float contentLeft() {
        return mContentRect.left;
    }

    public float contentRight() {
        return mContentRect.right;
    }

    public float contentBottom() {
        return mContentRect.bottom;
    }

    public float contentWidth() {
        return mContentRect.width();
    }

    public float contentHeight() {
        return mContentRect.height();
    }

    public RectF getContentRect() {
        return mContentRect;
    }

    public MPPointF getContentCenter() {
        return MPPointF.getInstance(mContentRect.centerX(), mContentRect.centerY());
    }

    public float getChartHeight() {
        return mChartHeight;
    }

    public float getChartWidth() {
        return mChartWidth;
    }

    public float getSmallestContentExtension() {
        return Math.min(mContentRect.width(), mContentRect.height());
    }

    public Matrix zoomIn(float x, float y) {

        Matrix save = new Matrix();
        zoomIn(x, y, save);
        return save;
    }

    public void zoomIn(float x, float y, Matrix outputMatrix) {
        outputMatrix.reset();
        outputMatrix.set(mMatrixTouch);
        outputMatrix.postScale(1.4f, 1.4f, x, y);
    }

    public Matrix zoomOut(float x, float y) {

        Matrix save = new Matrix();
        zoomOut(x, y, save);
        return save;
    }

    public void zoomOut(float x, float y, Matrix outputMatrix) {
        outputMatrix.reset();
        outputMatrix.set(mMatrixTouch);
        outputMatrix.postScale(0.7f, 0.7f, x, y);
    }

    public void resetZoom(Matrix outputMatrix) {
        outputMatrix.reset();
        outputMatrix.set(mMatrixTouch);
        outputMatrix.postScale(1.0f, 1.0f, 0.0f, 0.0f);
    }

    public Matrix zoom(float scaleX, float scaleY) {

        Matrix save = new Matrix();
        zoom(scaleX, scaleY, save);
        return save;
    }

    public void zoom(float scaleX, float scaleY, Matrix outputMatrix) {
        outputMatrix.reset();
        outputMatrix.set(mMatrixTouch);
        outputMatrix.postScale(scaleX, scaleY);
    }

    public Matrix zoom(float scaleX, float scaleY, float x, float y) {

        Matrix save = new Matrix();
        zoom(scaleX, scaleY, x, y, save);
        return save;
    }

    public void zoom(float scaleX, float scaleY, float x, float y, Matrix outputMatrix) {
        outputMatrix.reset();
        outputMatrix.set(mMatrixTouch);
        outputMatrix.postScale(scaleX, scaleY, x, y);
    }

    public Matrix setZoom(float scaleX, float scaleY) {

        Matrix save = new Matrix();
        setZoom(scaleX, scaleY, save);
        return save;
    }

    public void setZoom(float scaleX, float scaleY, Matrix outputMatrix) {
        outputMatrix.reset();
        outputMatrix.set(mMatrixTouch);
        outputMatrix.setScale(scaleX, scaleY);
    }

    public Matrix setZoom(float scaleX, float scaleY, float x, float y) {

        Matrix save = new Matrix();
        save.set(mMatrixTouch);

        save.setScale(scaleX, scaleY, x, y);

        return save;
    }

    protected float[] valsBufferForFitScreen = new float[9];

    public Matrix fitScreen() {

        Matrix save = new Matrix();
        fitScreen(save);
        return save;
    }

    public void fitScreen(Matrix outputMatrix) {
        mMinScaleX = 1f;
        mMinScaleY = 1f;

        outputMatrix.set(mMatrixTouch);

        float[] vals = valsBufferForFitScreen;
        for (int i = 0; i < 9; i++) {
            vals[i] = 0;
        }

        outputMatrix.getValues(vals);

        vals[Matrix.MTRANS_X] = 0f;
        vals[Matrix.MTRANS_Y] = 0f;
        vals[Matrix.MSCALE_X] = 1f;
        vals[Matrix.MSCALE_Y] = 1f;

        outputMatrix.setValues(vals);
    }

    public Matrix translate(final float[] transformedPts) {

        Matrix save = new Matrix();
        translate(transformedPts, save);
        return save;
    }

    public void translate(final float[] transformedPts, Matrix outputMatrix) {
        outputMatrix.reset();
        outputMatrix.set(mMatrixTouch);
        final float x = transformedPts[0] - offsetLeft();
        final float y = transformedPts[1] - offsetTop();
        outputMatrix.postTranslate(-x, -y);
    }

    protected Matrix mCenterViewPortMatrixBuffer = new Matrix();

    public void centerViewPort(final float[] transformedPts, final View view) {

        Matrix save = mCenterViewPortMatrixBuffer;
        save.reset();
        save.set(mMatrixTouch);

        final float x = transformedPts[0] - offsetLeft();
        final float y = transformedPts[1] - offsetTop();

        save.postTranslate(-x, -y);

        refresh(save, view, true);
    }

    protected final float[] matrixBuffer = new float[9];

    public Matrix refresh(Matrix newMatrix, View chart, boolean invalidate) {

        mMatrixTouch.set(newMatrix);

        limitTransAndScale(mMatrixTouch, mContentRect);

        if (invalidate)
            chart.invalidate();

        newMatrix.set(mMatrixTouch);
        return newMatrix;
    }

    public void limitTransAndScale(Matrix matrix, RectF content) {

        matrix.getValues(matrixBuffer);

        float curTransX = matrixBuffer[Matrix.MTRANS_X];
        float curScaleX = matrixBuffer[Matrix.MSCALE_X];

        float curTransY = matrixBuffer[Matrix.MTRANS_Y];
        float curScaleY = matrixBuffer[Matrix.MSCALE_Y];

        mScaleX = Math.min(Math.max(mMinScaleX, curScaleX), mMaxScaleX);

        mScaleY = Math.min(Math.max(mMinScaleY, curScaleY), mMaxScaleY);

        float width = 0f;
        float height = 0f;

        if (content != null) {
            width = content.width();
            height = content.height();
        }

        float maxTransX = -width * (mScaleX - 1f);
        mTransX = Math.min(Math.max(curTransX, maxTransX - mTransOffsetX), mTransOffsetX);

        float maxTransY = height * (mScaleY - 1f);
        mTransY = Math.max(Math.min(curTransY, maxTransY + mTransOffsetY), -mTransOffsetY);

        matrixBuffer[Matrix.MTRANS_X] = mTransX;
        matrixBuffer[Matrix.MSCALE_X] = mScaleX;

        matrixBuffer[Matrix.MTRANS_Y] = mTransY;
        matrixBuffer[Matrix.MSCALE_Y] = mScaleY;

        matrix.setValues(matrixBuffer);
    }

    public void setMinimumScaleX(float xScale) {

        if (xScale < 1f)
            xScale = 1f;

        mMinScaleX = xScale;

        limitTransAndScale(mMatrixTouch, mContentRect);
    }

    public void setMaximumScaleX(float xScale) {

        if (xScale == 0.f)
            xScale = Float.MAX_VALUE;

        mMaxScaleX = xScale;

        limitTransAndScale(mMatrixTouch, mContentRect);
    }

    public void setMinMaxScaleX(float minScaleX, float maxScaleX) {

        if (minScaleX < 1f)
            minScaleX = 1f;

        if (maxScaleX == 0.f)
            maxScaleX = Float.MAX_VALUE;

        mMinScaleX = minScaleX;
        mMaxScaleX = maxScaleX;

        limitTransAndScale(mMatrixTouch, mContentRect);
    }

    public void setMinimumScaleY(float yScale) {

        if (yScale < 1f)
            yScale = 1f;

        mMinScaleY = yScale;

        limitTransAndScale(mMatrixTouch, mContentRect);
    }

    public void setMaximumScaleY(float yScale) {

        if (yScale == 0.f)
            yScale = Float.MAX_VALUE;

        mMaxScaleY = yScale;

        limitTransAndScale(mMatrixTouch, mContentRect);
    }

    public void setMinMaxScaleY(float minScaleY, float maxScaleY) {

        if (minScaleY < 1f)
            minScaleY = 1f;

        if (maxScaleY == 0.f)
            maxScaleY = Float.MAX_VALUE;

        mMinScaleY = minScaleY;
        mMaxScaleY = maxScaleY;

        limitTransAndScale(mMatrixTouch, mContentRect);
    }

    public Matrix getMatrixTouch() {
        return mMatrixTouch;
    }

    public boolean isInBoundsX(float x) {
        return isInBoundsLeft(x) && isInBoundsRight(x);
    }

    public boolean isInBoundsY(float y) {
        return isInBoundsTop(y) && isInBoundsBottom(y);
    }

    public boolean isInBounds(float x, float y) {
        return isInBoundsX(x) && isInBoundsY(y);
    }

    public boolean isInBoundsLeft(float x) {
        return mContentRect.left <= x + 1;
    }

    public boolean isInBoundsRight(float x) {
        x = (float) ((int) (x * 100.f)) / 100.f;
        return mContentRect.right >= x - 1;
    }

    public boolean isInBoundsTop(float y) {
        return mContentRect.top <= y;
    }

    public boolean isInBoundsBottom(float y) {
        y = (float) ((int) (y * 100.f)) / 100.f;
        return mContentRect.bottom >= y;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }

    public float getMinScaleX() {
        return mMinScaleX;
    }

    public float getMaxScaleX() {
        return mMaxScaleX;
    }

    public float getMinScaleY() {
        return mMinScaleY;
    }

    public float getMaxScaleY() {
        return mMaxScaleY;
    }

    public float getTransX() {
        return mTransX;
    }

    public float getTransY() {
        return mTransY;
    }

    public boolean isFullyZoomedOut() {

        return isFullyZoomedOutX() && isFullyZoomedOutY();
    }

    public boolean isFullyZoomedOutY() {
        return !(mScaleY > mMinScaleY || mMinScaleY > 1f);
    }

    public boolean isFullyZoomedOutX() {
        return !(mScaleX > mMinScaleX || mMinScaleX > 1f);
    }

    public void setDragOffsetX(float offset) {
        mTransOffsetX = Utils.convertDpToPixel(offset);
    }

    public void setDragOffsetY(float offset) {
        mTransOffsetY = Utils.convertDpToPixel(offset);
    }

    public boolean hasNoDragOffset() {
        return mTransOffsetX <= 0 && mTransOffsetY <= 0;
    }

    public boolean canZoomOutMoreX() {
        return mScaleX > mMinScaleX;
    }

    public boolean canZoomInMoreX() {
        return mScaleX < mMaxScaleX;
    }

    public boolean canZoomOutMoreY() {
        return mScaleY > mMinScaleY;
    }

    public boolean canZoomInMoreY() {
        return mScaleY < mMaxScaleY;
    }
}
