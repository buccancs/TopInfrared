package com.github.mikephil.charting.data;

import android.graphics.Typeface;
import android.util.Log;

import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;

import java.util.ArrayList;
import java.util.List;

public abstract class ChartData<T extends IDataSet<? extends Entry>> {

    protected float mYMax = -Float.MAX_VALUE;

    protected float mYMin = Float.MAX_VALUE;

    protected float mXMax = -Float.MAX_VALUE;

    protected float mXMin = Float.MAX_VALUE;

    protected float mLeftAxisMax = -Float.MAX_VALUE;

    protected float mLeftAxisMin = Float.MAX_VALUE;

    protected float mRightAxisMax = -Float.MAX_VALUE;

    protected float mRightAxisMin = Float.MAX_VALUE;

    protected List<T> mDataSets;

    public ChartData() {
        mDataSets = new ArrayList<T>();
    }

    public ChartData(T... dataSets) {
        mDataSets = arrayToList(dataSets);
        notifyDataChanged();
    }

    private List<T> arrayToList(T[] array) {

        List<T> list = new ArrayList<>();

        for (T set : array) {
            list.add(set);
        }

        return list;
    }

    public ChartData(List<T> sets) {
        this.mDataSets = sets;
        notifyDataChanged();
    }

    public void notifyDataChanged() {
        calcMinMax();
    }

    public void calcMinMaxY(float fromX, float toX) {

        for (T set : mDataSets) {
            set.calcMinMaxY(fromX, toX);
        }

        calcMinMax();
    }

    protected void calcMinMax() {

        if (mDataSets == null)
            return;

        mYMax = -Float.MAX_VALUE;
        mYMin = Float.MAX_VALUE;
        mXMax = -Float.MAX_VALUE;
        mXMin = Float.MAX_VALUE;

        for (T set : mDataSets) {
            calcMinMax(set);
        }

        mLeftAxisMax = -Float.MAX_VALUE;
        mLeftAxisMin = Float.MAX_VALUE;
        mRightAxisMax = -Float.MAX_VALUE;
        mRightAxisMin = Float.MAX_VALUE;

        T firstLeft = getFirstLeft(mDataSets);

        if (firstLeft != null) {

            mLeftAxisMax = firstLeft.getYMax();
            mLeftAxisMin = firstLeft.getYMin();

            for (T dataSet : mDataSets) {
                if (dataSet.getAxisDependency() == AxisDependency.LEFT) {
                    if (dataSet.getYMin() < mLeftAxisMin)
                        mLeftAxisMin = dataSet.getYMin();

                    if (dataSet.getYMax() > mLeftAxisMax)
                        mLeftAxisMax = dataSet.getYMax();
                }
            }
        }

        T firstRight = getFirstRight(mDataSets);

        if (firstRight != null) {

            mRightAxisMax = firstRight.getYMax();
            mRightAxisMin = firstRight.getYMin();

            for (T dataSet : mDataSets) {
                if (dataSet.getAxisDependency() == AxisDependency.RIGHT) {
                    if (dataSet.getYMin() < mRightAxisMin)
                        mRightAxisMin = dataSet.getYMin();

                    if (dataSet.getYMax() > mRightAxisMax)
                        mRightAxisMax = dataSet.getYMax();
                }
            }
        }
    }

    public int getDataSetCount() {
        if (mDataSets == null)
            return 0;
        return mDataSets.size();
    }

    public float getYMin() {
        return mYMin;
    }

    public float getYMin(AxisDependency axis) {
        if (axis == AxisDependency.LEFT) {

            if (mLeftAxisMin == Float.MAX_VALUE) {
                return mRightAxisMin;
            } else
                return mLeftAxisMin;
        } else {
            if (mRightAxisMin == Float.MAX_VALUE) {
                return mLeftAxisMin;
            } else
                return mRightAxisMin;
        }
    }

    public float getYMax() {
        return mYMax;
    }

    public float getYMax(AxisDependency axis) {
        if (axis == AxisDependency.LEFT) {

            if (mLeftAxisMax == -Float.MAX_VALUE) {
                return mRightAxisMax;
            } else
                return mLeftAxisMax;
        } else {
            if (mRightAxisMax == -Float.MAX_VALUE) {
                return mLeftAxisMax;
            } else
                return mRightAxisMax;
        }
    }

    public float getXMin() {
        return mXMin;
    }

    public float getXMax() {
        return mXMax;
    }

    public List<T> getDataSets() {
        return mDataSets;
    }

    protected int getDataSetIndexByLabel(List<T> dataSets, String label,
                                         boolean ignorecase) {

        if (ignorecase) {
            for (int i = 0; i < dataSets.size(); i++)
                if (label.equalsIgnoreCase(dataSets.get(i).getLabel()))
                    return i;
        } else {
            for (int i = 0; i < dataSets.size(); i++)
                if (label.equals(dataSets.get(i).getLabel()))
                    return i;
        }

        return -1;
    }

    public String[] getDataSetLabels() {

        String[] types = new String[mDataSets.size()];

        for (int i = 0; i < mDataSets.size(); i++) {
            types[i] = mDataSets.get(i).getLabel();
        }

        return types;
    }

    public Entry getEntryForHighlight(Highlight highlight) {
        if (highlight.getDataSetIndex() >= mDataSets.size())
            return null;
        else {
            return mDataSets.get(highlight.getDataSetIndex()).getEntryForXValue(highlight.getX(), highlight.getY());
        }
    }

    public T getDataSetByLabel(String label, boolean ignorecase) {

        int index = getDataSetIndexByLabel(mDataSets, label, ignorecase);

        if (index < 0 || index >= mDataSets.size())
            return null;
        else
            return mDataSets.get(index);
    }

    public T getDataSetByIndex(int index) {

        if (mDataSets == null || index < 0 || index >= mDataSets.size())
            return null;

        return mDataSets.get(index);
    }

    public void addDataSet(T d) {

        if (d == null)
            return;

        calcMinMax(d);

        mDataSets.add(d);
    }

    public boolean removeDataSet(T d) {

        if (d == null)
            return false;

        boolean removed = mDataSets.remove(d);

        if (removed) {
            calcMinMax();
        }

        return removed;
    }

    public boolean removeDataSet(int index) {

        if (index >= mDataSets.size() || index < 0)
            return false;

        T set = mDataSets.get(index);
        return removeDataSet(set);
    }

    public void addEntry(Entry e, int dataSetIndex) {

        if (mDataSets.size() > dataSetIndex && dataSetIndex >= 0) {

            IDataSet set = mDataSets.get(dataSetIndex);
            if (!set.addEntry(e))
                return;

            calcMinMax(e, set.getAxisDependency());

        } else {
            Log.e("addEntry", "Cannot add Entry because dataSetIndex too high or too low.");
        }
    }

    protected void calcMinMax(Entry e, AxisDependency axis) {

        if (mYMax < e.getY())
            mYMax = e.getY();
        if (mYMin > e.getY())
            mYMin = e.getY();

        if (mXMax < e.getX())
            mXMax = e.getX();
        if (mXMin > e.getX())
            mXMin = e.getX();

        if (axis == AxisDependency.LEFT) {

            if (mLeftAxisMax < e.getY())
                mLeftAxisMax = e.getY();
            if (mLeftAxisMin > e.getY())
                mLeftAxisMin = e.getY();
        } else {
            if (mRightAxisMax < e.getY())
                mRightAxisMax = e.getY();
            if (mRightAxisMin > e.getY())
                mRightAxisMin = e.getY();
        }
    }

    protected void calcMinMax(T d) {

        if (mYMax < d.getYMax())
            mYMax = d.getYMax();
        if (mYMin > d.getYMin())
            mYMin = d.getYMin();

        if (mXMax < d.getXMax())
            mXMax = d.getXMax();
        if (mXMin > d.getXMin())
            mXMin = d.getXMin();

        if (d.getAxisDependency() == AxisDependency.LEFT) {

            if (mLeftAxisMax < d.getYMax())
                mLeftAxisMax = d.getYMax();
            if (mLeftAxisMin > d.getYMin())
                mLeftAxisMin = d.getYMin();
        } else {
            if (mRightAxisMax < d.getYMax())
                mRightAxisMax = d.getYMax();
            if (mRightAxisMin > d.getYMin())
                mRightAxisMin = d.getYMin();
        }
    }

    public boolean removeEntry(Entry e, int dataSetIndex) {

        if (e == null || dataSetIndex >= mDataSets.size())
            return false;

        IDataSet set = mDataSets.get(dataSetIndex);

        if (set != null) {
            boolean removed = set.removeEntry(e);

            if (removed) {
                calcMinMax();
            }

            return removed;
        } else
            return false;
    }

    public boolean removeEntry(float xValue, int dataSetIndex) {

        if (dataSetIndex >= mDataSets.size())
            return false;

        IDataSet dataSet = mDataSets.get(dataSetIndex);
        Entry e = dataSet.getEntryForXValue(xValue, Float.NaN);

        if (e == null)
            return false;

        return removeEntry(e, dataSetIndex);
    }

    public T getDataSetForEntry(Entry e) {

        if (e == null)
            return null;

        for (int i = 0; i < mDataSets.size(); i++) {

            T set = mDataSets.get(i);

            for (int j = 0; j < set.getEntryCount(); j++) {
                if (e.equalTo(set.getEntryForXValue(e.getX(), e.getY())))
                    return set;
            }
        }

        return null;
    }

    public int[] getColors() {

        if (mDataSets == null)
            return null;

        int clrcnt = 0;

        for (int i = 0; i < mDataSets.size(); i++) {
            clrcnt += mDataSets.get(i).getColors().size();
        }

        int[] colors = new int[clrcnt];
        int cnt = 0;

        for (int i = 0; i < mDataSets.size(); i++) {

            List<Integer> clrs = mDataSets.get(i).getColors();

            for (Integer clr : clrs) {
                colors[cnt] = clr;
                cnt++;
            }
        }

        return colors;
    }

    public int getIndexOfDataSet(T dataSet) {
        return mDataSets.indexOf(dataSet);
    }

    protected T getFirstLeft(List<T> sets) {
        for (T dataSet : sets) {
            if (dataSet.getAxisDependency() == AxisDependency.LEFT)
                return dataSet;
        }
        return null;
    }

    public T getFirstRight(List<T> sets) {
        for (T dataSet : sets) {
            if (dataSet.getAxisDependency() == AxisDependency.RIGHT)
                return dataSet;
        }
        return null;
    }

    public void setValueFormatter(ValueFormatter f) {
        if (f == null)
            return;
        else {
            for (IDataSet set : mDataSets) {
                set.setValueFormatter(f);
            }
        }
    }

    public void setValueTextColor(int color) {
        for (IDataSet set : mDataSets) {
            set.setValueTextColor(color);
        }
    }

    public void setValueTextColors(List<Integer> colors) {
        for (IDataSet set : mDataSets) {
            set.setValueTextColors(colors);
        }
    }

    public void setValueTypeface(Typeface tf) {
        for (IDataSet set : mDataSets) {
            set.setValueTypeface(tf);
        }
    }

    public void setValueTextSize(float size) {
        for (IDataSet set : mDataSets) {
            set.setValueTextSize(size);
        }
    }

    public void setDrawValues(boolean enabled) {
        for (IDataSet set : mDataSets) {
            set.setDrawValues(enabled);
        }
    }

    public void setHighlightEnabled(boolean enabled) {
        for (IDataSet set : mDataSets) {
            set.setHighlightEnabled(enabled);
        }
    }

    public boolean isHighlightEnabled() {
        for (IDataSet set : mDataSets) {
            if (!set.isHighlightEnabled())
                return false;
        }
        return true;
    }

    public void clearValues() {
        if (mDataSets != null) {
            mDataSets.clear();
        }
        notifyDataChanged();
    }

    public boolean contains(T dataSet) {

        for (T set : mDataSets) {
            if (set.equals(dataSet))
                return true;
        }

        return false;
    }

    public int getEntryCount() {

        int count = 0;

        for (T set : mDataSets) {
            count += set.getEntryCount();
        }

        return count;
    }

    public T getMaxEntryCountSet() {

        if (mDataSets == null || mDataSets.isEmpty())
            return null;

        T max = mDataSets.get(0);

        for (T set : mDataSets) {

            if (set.getEntryCount() > max.getEntryCount())
                max = set;
        }

        return max;
    }
}
