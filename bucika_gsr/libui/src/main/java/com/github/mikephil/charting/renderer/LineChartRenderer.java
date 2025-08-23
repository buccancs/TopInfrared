package com.github.mikephil.charting.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;

import com.github.mikephil.charting.animation.ChartAnimator;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointD;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

public class LineChartRenderer extends LineRadarRenderer {

    protected LineDataProvider mChart;

    protected Paint mCirclePaintInner;

    protected WeakReference<Bitmap> mDrawBitmap;

    protected Canvas mBitmapCanvas;

    protected Bitmap.Config mBitmapConfig = Bitmap.Config.ARGB_8888;

    protected Path cubicPath = new Path();
    protected Path cubicFillPath = new Path();

    public LineChartRenderer(LineDataProvider chart, ChartAnimator animator,
                             ViewPortHandler viewPortHandler) {
        super(animator, viewPortHandler);
        mChart = chart;

        mCirclePaintInner = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaintInner.setStyle(Paint.Style.FILL);
        mCirclePaintInner.setColor(Color.WHITE);
    }

    @Override
    public void initBuffers() {
    }

    @Override
    public void drawData(Canvas c) {

        int width = (int) mViewPortHandler.getChartWidth();
        int height = (int) mViewPortHandler.getChartHeight();

        Bitmap drawBitmap = mDrawBitmap == null ? null : mDrawBitmap.get();

        if (drawBitmap == null
                || (drawBitmap.getWidth() != width)
                || (drawBitmap.getHeight() != height)) {

            if (width > 0 && height > 0) {
                drawBitmap = Bitmap.createBitmap(width, height, mBitmapConfig);
                mDrawBitmap = new WeakReference<>(drawBitmap);
                mBitmapCanvas = new Canvas(drawBitmap);
            } else
                return;
        }

        drawBitmap.eraseColor(Color.TRANSPARENT);

        LineData lineData = mChart.getLineData();

        if (lineData != null) {
            for (ILineDataSet set : lineData.getDataSets()) {

                if (set.isVisible())
                    drawDataSet(c, set);
            }
        }

        c.drawBitmap(drawBitmap, 0, 0, mRenderPaint);
    }

    protected void drawDataSet(Canvas c, ILineDataSet dataSet) {

        if (dataSet.getEntryCount() < 1)
            return;

        mRenderPaint.setStrokeWidth(dataSet.getLineWidth());
        mRenderPaint.setPathEffect(dataSet.getDashPathEffect());

        switch (dataSet.getMode()) {
            default:
            case LINEAR:
            case STEPPED:
                drawLinear(c, dataSet);
                break;

            case CUBIC_BEZIER:
                drawCubicBezier(dataSet);
                break;

            case HORIZONTAL_BEZIER:
                drawHorizontalBezier(dataSet);
                break;
        }

        mRenderPaint.setPathEffect(null);
    }

    protected void drawHorizontalBezier(ILineDataSet dataSet) {

        float phaseY = mAnimator.getPhaseY();

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mXBounds.set(mChart, dataSet);

        cubicPath.reset();

        if (mXBounds.range >= 1) {

            Entry prev = dataSet.getEntryForIndex(mXBounds.min);
            Entry cur = prev;

            cubicPath.moveTo(cur.getX(), cur.getY() * phaseY);

            for (int j = mXBounds.min + 1; j <= mXBounds.range + mXBounds.min; j++) {

                prev = cur;
                cur = dataSet.getEntryForIndex(j);

                final float cpx = (prev.getX())
                        + (cur.getX() - prev.getX()) / 2.0f;

                cubicPath.cubicTo(
                        cpx, prev.getY() * phaseY,
                        cpx, cur.getY() * phaseY,
                        cur.getX(), cur.getY() * phaseY);
            }
        }

        if (dataSet.isDrawFilledEnabled()) {

            cubicFillPath.reset();
            cubicFillPath.addPath(cubicPath);
            drawCubicFill(mBitmapCanvas, dataSet, cubicFillPath, trans, mXBounds);
        }

        mRenderPaint.setColor(dataSet.getColor());

        mRenderPaint.setStyle(Paint.Style.STROKE);

        trans.pathValueToPixel(cubicPath);

        mBitmapCanvas.drawPath(cubicPath, mRenderPaint);

        mRenderPaint.setPathEffect(null);
    }

    protected void drawCubicBezier(ILineDataSet dataSet) {

        float phaseY = mAnimator.getPhaseY();

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        mXBounds.set(mChart, dataSet);

        float intensity = dataSet.getCubicIntensity();

        cubicPath.reset();

        if (mXBounds.range >= 1) {

            float prevDx = 0f;
            float prevDy = 0f;
            float curDx = 0f;
            float curDy = 0f;

            final int firstIndex = mXBounds.min + 1;
            final int lastIndex = mXBounds.min + mXBounds.range;

            Entry prevPrev;
            Entry prev = dataSet.getEntryForIndex(Math.max(firstIndex - 2, 0));
            Entry cur = dataSet.getEntryForIndex(Math.max(firstIndex - 1, 0));
            Entry next = cur;
            int nextIndex = -1;

            if (cur == null) return;

            cubicPath.moveTo(cur.getX(), cur.getY() * phaseY);

            for (int j = mXBounds.min + 1; j <= mXBounds.range + mXBounds.min; j++) {

                prevPrev = prev;
                prev = cur;
                cur = nextIndex == j ? next : dataSet.getEntryForIndex(j);

                nextIndex = j + 1 < dataSet.getEntryCount() ? j + 1 : j;
                next = dataSet.getEntryForIndex(nextIndex);

                prevDx = (cur.getX() - prevPrev.getX()) * intensity;
                prevDy = (cur.getY() - prevPrev.getY()) * intensity;
                curDx = (next.getX() - prev.getX()) * intensity;
                curDy = (next.getY() - prev.getY()) * intensity;

                cubicPath.cubicTo(prev.getX() + prevDx, (prev.getY() + prevDy) * phaseY,
                        cur.getX() - curDx,
                        (cur.getY() - curDy) * phaseY, cur.getX(), cur.getY() * phaseY);
            }
        }

        if (dataSet.isDrawFilledEnabled()) {

            cubicFillPath.reset();
            cubicFillPath.addPath(cubicPath);

            drawCubicFill(mBitmapCanvas, dataSet, cubicFillPath, trans, mXBounds);
        }

        mRenderPaint.setColor(dataSet.getColor());

        mRenderPaint.setStyle(Paint.Style.STROKE);

        trans.pathValueToPixel(cubicPath);

        mBitmapCanvas.drawPath(cubicPath, mRenderPaint);

        mRenderPaint.setPathEffect(null);
    }

    protected void drawCubicFill(Canvas c, ILineDataSet dataSet, Path spline, Transformer trans, XBounds bounds) {

        float fillMin = dataSet.getFillFormatter()
                .getFillLinePosition(dataSet, mChart);

        spline.lineTo(dataSet.getEntryForIndex(bounds.min + bounds.range).getX(), fillMin);
        spline.lineTo(dataSet.getEntryForIndex(bounds.min).getX(), fillMin);
        spline.close();

        trans.pathValueToPixel(spline);

        final Drawable drawable = dataSet.getFillDrawable();
        if (drawable != null) {

            drawFilledPath(c, spline, drawable);
        } else {

            drawFilledPath(c, spline, dataSet.getFillColor(), dataSet.getFillAlpha());
        }
    }

    private float[] mLineBuffer = new float[4];

    protected void drawLinear(Canvas c, ILineDataSet dataSet) {

        int entryCount = dataSet.getEntryCount();

        final boolean isDrawSteppedEnabled = dataSet.getMode() == LineDataSet.Mode.STEPPED;
        final int pointsPerEntryPair = isDrawSteppedEnabled ? 4 : 2;

        Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

        float phaseY = mAnimator.getPhaseY();

        mRenderPaint.setStyle(Paint.Style.STROKE);

        Canvas canvas = null;

        if (dataSet.isDashedLineEnabled()) {
            canvas = mBitmapCanvas;
        } else {
            canvas = c;
        }

        mXBounds.set(mChart, dataSet);

        if (dataSet.isDrawFilledEnabled() && entryCount > 0) {
            drawLinearFill(c, dataSet, trans, mXBounds);
        }

        if (dataSet.getColors().size() > 1) {

            if (mLineBuffer.length <= pointsPerEntryPair * 2)
                mLineBuffer = new float[pointsPerEntryPair * 4];

            for (int j = mXBounds.min; j <= mXBounds.range + mXBounds.min; j++) {

                Entry e = dataSet.getEntryForIndex(j);
                if (e == null) continue;

                mLineBuffer[0] = e.getX();
                mLineBuffer[1] = e.getY() * phaseY;

                if (j < mXBounds.max) {

                    e = dataSet.getEntryForIndex(j + 1);

                    if (e == null) break;

                    if (isDrawSteppedEnabled) {
                        mLineBuffer[2] = e.getX();
                        mLineBuffer[3] = mLineBuffer[1];
                        mLineBuffer[4] = mLineBuffer[2];
                        mLineBuffer[5] = mLineBuffer[3];
                        mLineBuffer[6] = e.getX();
                        mLineBuffer[7] = e.getY() * phaseY;
                    } else {
                        mLineBuffer[2] = e.getX();
                        mLineBuffer[3] = e.getY() * phaseY;
                    }

                } else {
                    mLineBuffer[2] = mLineBuffer[0];
                    mLineBuffer[3] = mLineBuffer[1];
                }

                trans.pointValuesToPixel(mLineBuffer);

                if (!mViewPortHandler.isInBoundsRight(mLineBuffer[0]))
                    break;

                if (!mViewPortHandler.isInBoundsLeft(mLineBuffer[2])
                        || (!mViewPortHandler.isInBoundsTop(mLineBuffer[1]) && !mViewPortHandler
                        .isInBoundsBottom(mLineBuffer[3])))
                    continue;

                mRenderPaint.setColor(dataSet.getColor(j));

                canvas.drawLines(mLineBuffer, 0, pointsPerEntryPair * 2, mRenderPaint);
            }

        } else {

            if (mLineBuffer.length < Math.max((entryCount) * pointsPerEntryPair, pointsPerEntryPair) * 2)
                mLineBuffer = new float[Math.max((entryCount) * pointsPerEntryPair, pointsPerEntryPair) * 4];

            Entry e1, e2;

            e1 = dataSet.getEntryForIndex(mXBounds.min);

            if (e1 != null) {

                int j = 0;
                for (int x = mXBounds.min; x <= mXBounds.range + mXBounds.min; x++) {

                    e1 = dataSet.getEntryForIndex(x == 0 ? 0 : (x - 1));
                    e2 = dataSet.getEntryForIndex(x);

                    if (e1 == null || e2 == null) continue;

                    mLineBuffer[j++] = e1.getX();
                    mLineBuffer[j++] = e1.getY() * phaseY;

                    if (isDrawSteppedEnabled) {
                        mLineBuffer[j++] = e2.getX();
                        mLineBuffer[j++] = e1.getY() * phaseY;
                        mLineBuffer[j++] = e2.getX();
                        mLineBuffer[j++] = e1.getY() * phaseY;
                    }

                    mLineBuffer[j++] = e2.getX();
                    mLineBuffer[j++] = e2.getY() * phaseY;
                }

                if (j > 0) {
                    trans.pointValuesToPixel(mLineBuffer);

                    final int size = Math.max((mXBounds.range + 1) * pointsPerEntryPair, pointsPerEntryPair) * 2;

                    mRenderPaint.setColor(dataSet.getColor());

                    canvas.drawLines(mLineBuffer, 0, size, mRenderPaint);
                }
            }
        }

        mRenderPaint.setPathEffect(null);
    }

    protected Path mGenerateFilledPathBuffer = new Path();

    protected void drawLinearFill(Canvas c, ILineDataSet dataSet, Transformer trans, XBounds bounds) {

        final Path filled = mGenerateFilledPathBuffer;

        final int startingIndex = bounds.min;
        final int endingIndex = bounds.range + bounds.min;
        final int indexInterval = 128;

        int currentStartIndex = 0;
        int currentEndIndex = indexInterval;
        int iterations = 0;

        do {
            currentStartIndex = startingIndex + (iterations * indexInterval);
            currentEndIndex = currentStartIndex + indexInterval;
            currentEndIndex = currentEndIndex > endingIndex ? endingIndex : currentEndIndex;

            if (currentStartIndex <= currentEndIndex) {
                generateFilledPath(dataSet, currentStartIndex, currentEndIndex, filled);

                trans.pathValueToPixel(filled);

                final Drawable drawable = dataSet.getFillDrawable();
                if (drawable != null) {

                    drawFilledPath(c, filled, drawable);
                } else {

                    drawFilledPath(c, filled, dataSet.getFillColor(), dataSet.getFillAlpha());
                }
            }

            iterations++;

        } while (currentStartIndex <= currentEndIndex);

    }

    private void generateFilledPath(final ILineDataSet dataSet, final int startIndex, final int endIndex, final Path outputPath) {

        final float fillMin = dataSet.getFillFormatter().getFillLinePosition(dataSet, mChart);
        final float phaseY = mAnimator.getPhaseY();
        final boolean isDrawSteppedEnabled = dataSet.getMode() == LineDataSet.Mode.STEPPED;

        final Path filled = outputPath;
        filled.reset();

        final Entry entry = dataSet.getEntryForIndex(startIndex);

        filled.moveTo(entry.getX(), fillMin);
        filled.lineTo(entry.getX(), entry.getY() * phaseY);

        Entry currentEntry = null;
        Entry previousEntry = entry;
        for (int x = startIndex + 1; x <= endIndex; x++) {

            currentEntry = dataSet.getEntryForIndex(x);

            if (isDrawSteppedEnabled) {
                filled.lineTo(currentEntry.getX(), previousEntry.getY() * phaseY);
            }

            filled.lineTo(currentEntry.getX(), currentEntry.getY() * phaseY);

            previousEntry = currentEntry;
        }

        if (currentEntry != null) {
            filled.lineTo(currentEntry.getX(), fillMin);
        }

        filled.close();
    }

    @Override
    public void drawValues(Canvas c) {

        if (isDrawingValuesAllowed(mChart)) {

            List<ILineDataSet> dataSets = mChart.getLineData().getDataSets();

            for (int i = 0; i < dataSets.size(); i++) {

                ILineDataSet dataSet = dataSets.get(i);

                if (!shouldDrawValues(dataSet) || dataSet.getEntryCount() < 1)
                    continue;

                applyValueTextStyle(dataSet);

                Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

                int valOffset = (int) (dataSet.getCircleRadius() * 1.75f);

                if (!dataSet.isDrawCirclesEnabled())
                    valOffset = valOffset / 2;

                mXBounds.set(mChart, dataSet);

                float[] positions = trans.generateTransformedValuesLine(dataSet, mAnimator.getPhaseX(), mAnimator
                        .getPhaseY(), mXBounds.min, mXBounds.max);
                ValueFormatter formatter = dataSet.getValueFormatter();

                MPPointF iconsOffset = MPPointF.getInstance(dataSet.getIconsOffset());
                iconsOffset.x = Utils.convertDpToPixel(iconsOffset.x);
                iconsOffset.y = Utils.convertDpToPixel(iconsOffset.y);

                for (int j = 0; j < positions.length; j += 2) {

                    float x = positions[j];
                    float y = positions[j + 1];

                    if (!mViewPortHandler.isInBoundsRight(x))
                        break;

                    if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y))
                        continue;

                    Entry entry = dataSet.getEntryForIndex(j / 2 + mXBounds.min);

                    if (dataSet.isDrawValuesEnabled()) {
                        drawValue(c, formatter.getPointLabel(entry), x, y - valOffset, dataSet.getValueTextColor(j / 2));
                    }

                    if (entry.getIcon() != null && dataSet.isDrawIconsEnabled()) {

                        Drawable icon = entry.getIcon();

                        Utils.drawImage(
                                c,
                                icon,
                                (int) (x + iconsOffset.x),
                                (int) (y + iconsOffset.y),
                                icon.getIntrinsicWidth(),
                                icon.getIntrinsicHeight());
                    }
                }

                MPPointF.recycleInstance(iconsOffset);
            }
        }
    }

    @Override
    public void drawValue(Canvas c, String valueText, float x, float y, int color) {
        mValuePaint.setColor(color);
        c.drawText(valueText, x, y, mValuePaint);
    }

    @Override
    public void drawExtras(Canvas c) {
        drawCircles(c);
    }

    private HashMap<IDataSet, DataSetImageCache> mImageCaches = new HashMap<>();

    private float[] mCirclesBuffer = new float[2];

    protected void drawCircles(Canvas c) {

        mRenderPaint.setStyle(Paint.Style.FILL);

        float phaseY = mAnimator.getPhaseY();

        mCirclesBuffer[0] = 0;
        mCirclesBuffer[1] = 0;

        List<ILineDataSet> dataSets = mChart.getLineData().getDataSets();

        for (int i = 0; i < dataSets.size(); i++) {

            ILineDataSet dataSet = dataSets.get(i);

            if (!dataSet.isVisible() || !dataSet.isDrawCirclesEnabled() ||
                    dataSet.getEntryCount() == 0)
                continue;

            mCirclePaintInner.setColor(dataSet.getCircleHoleColor());

            Transformer trans = mChart.getTransformer(dataSet.getAxisDependency());

            mXBounds.set(mChart, dataSet);

            float circleRadius = dataSet.getCircleRadius();
            float circleHoleRadius = dataSet.getCircleHoleRadius();
            boolean drawCircleHole = dataSet.isDrawCircleHoleEnabled() &&
                    circleHoleRadius < circleRadius &&
                    circleHoleRadius > 0.f;
            boolean drawTransparentCircleHole = drawCircleHole &&
                    dataSet.getCircleHoleColor() == ColorTemplate.COLOR_NONE;

            DataSetImageCache imageCache;

            if (mImageCaches.containsKey(dataSet)) {
                imageCache = mImageCaches.get(dataSet);
            } else {
                imageCache = new DataSetImageCache();
                mImageCaches.put(dataSet, imageCache);
            }

            boolean changeRequired = imageCache.init(dataSet);

            if (changeRequired) {
                imageCache.fill(dataSet, drawCircleHole, drawTransparentCircleHole);
            }

            int boundsRangeCount = mXBounds.range + mXBounds.min;

            for (int j = mXBounds.min; j <= boundsRangeCount; j++) {

                Entry e = dataSet.getEntryForIndex(j);

                if (e == null) break;

                mCirclesBuffer[0] = e.getX();
                mCirclesBuffer[1] = e.getY() * phaseY;

                trans.pointValuesToPixel(mCirclesBuffer);

                if (!mViewPortHandler.isInBoundsRight(mCirclesBuffer[0]))
                    break;

                if (!mViewPortHandler.isInBoundsLeft(mCirclesBuffer[0]) ||
                        !mViewPortHandler.isInBoundsY(mCirclesBuffer[1]))
                    continue;

                Bitmap circleBitmap = imageCache.getBitmap(j);

                if (circleBitmap != null) {
                    c.drawBitmap(circleBitmap, mCirclesBuffer[0] - circleRadius, mCirclesBuffer[1] - circleRadius, null);
                }
            }
        }
    }

    @Override
    public void drawHighlighted(Canvas c, Highlight[] indices) {

        LineData lineData = mChart.getLineData();

        for (Highlight high : indices) {

            ILineDataSet set = lineData.getDataSetByIndex(high.getDataSetIndex());

            if (set == null || !set.isHighlightEnabled())
                continue;

            Entry e = set.getEntryForXValue(high.getX(), high.getY());

            if (!isInBoundsX(e, set))
                continue;

            MPPointD pix = mChart.getTransformer(set.getAxisDependency()).getPixelForValues(e.getX(), e.getY() * mAnimator
                    .getPhaseY());

            high.setDraw((float) pix.x, (float) pix.y);

            drawHighlightLines(c, (float) pix.x, (float) pix.y, set);
        }
    }

    public void setBitmapConfig(Bitmap.Config config) {
        mBitmapConfig = config;
        releaseBitmap();
    }

    public Bitmap.Config getBitmapConfig() {
        return mBitmapConfig;
    }

    public void releaseBitmap() {
        if (mBitmapCanvas != null) {
            mBitmapCanvas.setBitmap(null);
            mBitmapCanvas = null;
        }
        if (mDrawBitmap != null) {
            Bitmap drawBitmap = mDrawBitmap.get();
            if (drawBitmap != null) {
                drawBitmap.recycle();
            }
            mDrawBitmap.clear();
            mDrawBitmap = null;
        }
    }

    private class DataSetImageCache {

        private Path mCirclePathBuffer = new Path();

        private Bitmap[] circleBitmaps;

        protected boolean init(ILineDataSet set) {

            int size = set.getCircleColorCount();
            boolean changeRequired = false;

            if (circleBitmaps == null) {
                circleBitmaps = new Bitmap[size];
                changeRequired = true;
            } else if (circleBitmaps.length != size) {
                circleBitmaps = new Bitmap[size];
                changeRequired = true;
            }

            return changeRequired;
        }

        protected void fill(ILineDataSet set, boolean drawCircleHole, boolean drawTransparentCircleHole) {

            int colorCount = set.getCircleColorCount();
            float circleRadius = set.getCircleRadius();
            float circleHoleRadius = set.getCircleHoleRadius();

            for (int i = 0; i < colorCount; i++) {

                Bitmap.Config conf = Bitmap.Config.ARGB_4444;
                Bitmap circleBitmap = Bitmap.createBitmap((int) (circleRadius * 2.1), (int) (circleRadius * 2.1), conf);

                Canvas canvas = new Canvas(circleBitmap);
                circleBitmaps[i] = circleBitmap;
                mRenderPaint.setColor(set.getCircleColor(i));

                if (drawTransparentCircleHole) {
                    mCirclePathBuffer.reset();

                    mCirclePathBuffer.addCircle(
                            circleRadius,
                            circleRadius,
                            circleRadius,
                            Path.Direction.CW);

                    mCirclePathBuffer.addCircle(
                            circleRadius,
                            circleRadius,
                            circleHoleRadius,
                            Path.Direction.CCW);

                    canvas.drawPath(mCirclePathBuffer, mRenderPaint);
                } else {

                    canvas.drawCircle(
                            circleRadius,
                            circleRadius,
                            circleRadius,
                            mRenderPaint);

                    if (drawCircleHole) {
                        canvas.drawCircle(
                                circleRadius,
                                circleRadius,
                                circleHoleRadius,
                                mCirclePaintInner);
                    }
                }
            }
        }

        protected Bitmap getBitmap(int index) {
            return circleBitmaps[index % circleBitmaps.length];
        }
    }
}
