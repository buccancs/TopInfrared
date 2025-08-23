
package com.github.mikephil.charting.data;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.renderer.scatter.ChevronDownShapeRenderer;
import com.github.mikephil.charting.renderer.scatter.ChevronUpShapeRenderer;
import com.github.mikephil.charting.renderer.scatter.CircleShapeRenderer;
import com.github.mikephil.charting.renderer.scatter.CrossShapeRenderer;
import com.github.mikephil.charting.renderer.scatter.IShapeRenderer;
import com.github.mikephil.charting.renderer.scatter.SquareShapeRenderer;
import com.github.mikephil.charting.renderer.scatter.TriangleShapeRenderer;
import com.github.mikephil.charting.renderer.scatter.XShapeRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class ScatterDataSet extends LineScatterCandleRadarDataSet<Entry> implements IScatterDataSet {

    private float mShapeSize = 15f;

    protected IShapeRenderer mShapeRenderer = new SquareShapeRenderer();

    private float mScatterShapeHoleRadius = 0f;

    private int mScatterShapeHoleColor = ColorTemplate.COLOR_NONE;

    public ScatterDataSet(List<Entry> yVals, String label) {
        super(yVals, label);
    }

    @Override
    public DataSet<Entry> copy() {
        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < mValues.size(); i++) {
            entries.add(mValues.get(i).copy());
        }
        ScatterDataSet copied = new ScatterDataSet(entries, getLabel());
        copy(copied);
        return copied;
    }

    protected void copy(ScatterDataSet scatterDataSet) {
        super.copy(scatterDataSet);
        scatterDataSet.mShapeSize = mShapeSize;
        scatterDataSet.mShapeRenderer = mShapeRenderer;
        scatterDataSet.mScatterShapeHoleRadius = mScatterShapeHoleRadius;
        scatterDataSet.mScatterShapeHoleColor = mScatterShapeHoleColor;
    }

    public void setScatterShapeSize(float size) {
        mShapeSize = size;
    }

    @Override
    public float getScatterShapeSize() {
        return mShapeSize;
    }

    public void setScatterShape(ScatterChart.ScatterShape shape) {
        mShapeRenderer = getRendererForShape(shape);
    }

    public void setShapeRenderer(IShapeRenderer shapeRenderer) {
        mShapeRenderer = shapeRenderer;
    }

    @Override
    public IShapeRenderer getShapeRenderer() {
        return mShapeRenderer;
    }

    public void setScatterShapeHoleRadius(float holeRadius) {
        mScatterShapeHoleRadius = holeRadius;
    }

    @Override
    public float getScatterShapeHoleRadius() {
        return mScatterShapeHoleRadius;
    }

    public void setScatterShapeHoleColor(int holeColor) {
        mScatterShapeHoleColor = holeColor;
    }

    @Override
    public int getScatterShapeHoleColor() {
        return mScatterShapeHoleColor;
    }

    public static IShapeRenderer getRendererForShape(ScatterChart.ScatterShape shape) {

        switch (shape) {
            case SQUARE:
                return new SquareShapeRenderer();
            case CIRCLE:
                return new CircleShapeRenderer();
            case TRIANGLE:
                return new TriangleShapeRenderer();
            case CROSS:
                return new CrossShapeRenderer();
            case X:
                return new XShapeRenderer();
            case CHEVRON_UP:
                return new ChevronUpShapeRenderer();
            case CHEVRON_DOWN:
                return new ChevronDownShapeRenderer();
        }

        return null;
    }
}
