
package com.github.mikephil.charting.utils;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

import com.elvishew.xlog.XLog;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBubbleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ICandleDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;

import java.util.List;

public class Transformer {

    protected Matrix mMatrixValueToPx = new Matrix();

    protected Matrix mMatrixOffset = new Matrix();

    protected ViewPortHandler mViewPortHandler;

    public Transformer(ViewPortHandler viewPortHandler) {
        this.mViewPortHandler = viewPortHandler;
    }

    public void prepareMatrixValuePx(float xChartMin, float deltaX, float deltaY, float yChartMin) {

        float scaleX = (float) ((mViewPortHandler.contentWidth()) / deltaX);
        float scaleY = (float) ((mViewPortHandler.contentHeight()) / deltaY);

        if (Float.isInfinite(scaleX)) {
            scaleX = 0;
        }
        if (Float.isInfinite(scaleY)) {
            scaleY = 0;
        }

        mMatrixValueToPx.reset();
        mMatrixValueToPx.postTranslate(-xChartMin, -yChartMin);
        mMatrixValueToPx.postScale(scaleX, -scaleY);
    }

    public void prepareMatrixOffset(boolean inverted) {

        mMatrixOffset.reset();

        if (!inverted)
            mMatrixOffset.postTranslate(mViewPortHandler.offsetLeft(),
                    mViewPortHandler.getChartHeight() - mViewPortHandler.offsetBottom());
        else {
            mMatrixOffset
                    .setTranslate(mViewPortHandler.offsetLeft(), -mViewPortHandler.offsetTop());
            mMatrixOffset.postScale(1.0f, -1.0f);
        }
    }

    protected float[] valuePointsForGenerateTransformedValuesScatter = new float[1];

    public float[] generateTransformedValuesScatter(IScatterDataSet data, float phaseX,
                                                    float phaseY, int from, int to) {

        final int count = (int) ((to - from) * phaseX + 1) * 2;

        if (valuePointsForGenerateTransformedValuesScatter.length != count) {
            valuePointsForGenerateTransformedValuesScatter = new float[count];
        }
        float[] valuePoints = valuePointsForGenerateTransformedValuesScatter;

        for (int j = 0; j < count; j += 2) {

            Entry e = data.getEntryForIndex(j / 2 + from);

            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getY() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    protected float[] valuePointsForGenerateTransformedValuesBubble = new float[1];

    public float[] generateTransformedValuesBubble(IBubbleDataSet data, float phaseY, int from, int to) {

        final int count = (to - from + 1) * 2;

        if (valuePointsForGenerateTransformedValuesBubble.length != count) {
            valuePointsForGenerateTransformedValuesBubble = new float[count];
        }
        float[] valuePoints = valuePointsForGenerateTransformedValuesBubble;

        for (int j = 0; j < count; j += 2) {

            Entry e = data.getEntryForIndex(j / 2 + from);

            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getY() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    protected float[] valuePointsForGenerateTransformedValuesLine = new float[1];

    public float[] generateTransformedValuesLine(ILineDataSet data,
                                                 float phaseX, float phaseY,
                                                 int min, int max) {
        if (max < min) {
            XLog.w("generateTransformedValuesLine error: max:" + max + ", min:" + min + ", phaseX:" + phaseX);
            return new float[0];
        }
        final int count = ((int) ((max - min) * phaseX) + 1) * 2;

        if (valuePointsForGenerateTransformedValuesLine.length != count) {
            valuePointsForGenerateTransformedValuesLine = new float[count];
        }
        float[] valuePoints = valuePointsForGenerateTransformedValuesLine;

        for (int j = 0; j < count; j += 2) {

            Entry e = data.getEntryForIndex(j / 2 + min);

            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getY() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    protected float[] valuePointsForGenerateTransformedValuesCandle = new float[1];

    public float[] generateTransformedValuesCandle(ICandleDataSet data,
                                                   float phaseX, float phaseY, int from, int to) {

        final int count = (int) ((to - from) * phaseX + 1) * 2;

        if (valuePointsForGenerateTransformedValuesCandle.length != count) {
            valuePointsForGenerateTransformedValuesCandle = new float[count];
        }
        float[] valuePoints = valuePointsForGenerateTransformedValuesCandle;

        for (int j = 0; j < count; j += 2) {

            CandleEntry e = data.getEntryForIndex(j / 2 + from);

            if (e != null) {
                valuePoints[j] = e.getX();
                valuePoints[j + 1] = e.getHigh() * phaseY;
            } else {
                valuePoints[j] = 0;
                valuePoints[j + 1] = 0;
            }
        }

        getValueToPixelMatrix().mapPoints(valuePoints);

        return valuePoints;
    }

    public void pathValueToPixel(Path path) {

        path.transform(mMatrixValueToPx);
        path.transform(mViewPortHandler.getMatrixTouch());
        path.transform(mMatrixOffset);
    }

    public void pathValuesToPixel(List<Path> paths) {

        for (int i = 0; i < paths.size(); i++) {
            pathValueToPixel(paths.get(i));
        }
    }

    public void pointValuesToPixel(float[] pts) {

        mMatrixValueToPx.mapPoints(pts);
        mViewPortHandler.getMatrixTouch().mapPoints(pts);
        mMatrixOffset.mapPoints(pts);
    }

    public void rectValueToPixel(RectF r) {

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    public void rectToPixelPhase(RectF r, float phaseY) {

        r.top *= phaseY;
        r.bottom *= phaseY;

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    public void rectToPixelPhaseHorizontal(RectF r, float phaseY) {

        r.left *= phaseY;
        r.right *= phaseY;

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    public void rectValueToPixelHorizontal(RectF r) {

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    public void rectValueToPixelHorizontal(RectF r, float phaseY) {

        r.left *= phaseY;
        r.right *= phaseY;

        mMatrixValueToPx.mapRect(r);
        mViewPortHandler.getMatrixTouch().mapRect(r);
        mMatrixOffset.mapRect(r);
    }

    public void rectValuesToPixel(List<RectF> rects) {

        Matrix m = getValueToPixelMatrix();

        for (int i = 0; i < rects.size(); i++)
            m.mapRect(rects.get(i));
    }

    protected Matrix mPixelToValueMatrixBuffer = new Matrix();

    public void pixelsToValue(float[] pixels) {

        Matrix tmp = mPixelToValueMatrixBuffer;
        tmp.reset();

        mMatrixOffset.invert(tmp);
        tmp.mapPoints(pixels);

        mViewPortHandler.getMatrixTouch().invert(tmp);
        tmp.mapPoints(pixels);

        mMatrixValueToPx.invert(tmp);
        tmp.mapPoints(pixels);
    }

    float[] ptsBuffer = new float[2];

    public MPPointD getValuesByTouchPoint(float x, float y) {

        MPPointD result = MPPointD.getInstance(0, 0);
        getValuesByTouchPoint(x, y, result);
        return result;
    }

    public void getValuesByTouchPoint(float x, float y, MPPointD outputPoint) {

        ptsBuffer[0] = x;
        ptsBuffer[1] = y;

        pixelsToValue(ptsBuffer);

        outputPoint.x = ptsBuffer[0];
        outputPoint.y = ptsBuffer[1];
    }

    public MPPointD getPixelForValues(float x, float y) {

        ptsBuffer[0] = x;
        ptsBuffer[1] = y;

        pointValuesToPixel(ptsBuffer);

        double xPx = ptsBuffer[0];
        double yPx = ptsBuffer[1];

        return MPPointD.getInstance(xPx, yPx);
    }

    public Matrix getValueMatrix() {
        return mMatrixValueToPx;
    }

    public Matrix getOffsetMatrix() {
        return mMatrixOffset;
    }

    private Matrix mMBuffer1 = new Matrix();

    public Matrix getValueToPixelMatrix() {
        mMBuffer1.set(mMatrixValueToPx);
        mMBuffer1.postConcat(mViewPortHandler.mMatrixTouch);
        mMBuffer1.postConcat(mMatrixOffset);
        return mMBuffer1;
    }

    private Matrix mMBuffer2 = new Matrix();

    public Matrix getPixelToValueMatrix() {
        getValueToPixelMatrix().invert(mMBuffer2);
        return mMBuffer2;
    }
}
