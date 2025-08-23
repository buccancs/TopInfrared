
package com.github.mikephil.charting.data;

import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.List;

public class BarData extends BarLineScatterCandleBubbleData<IBarDataSet> {

    private float mBarWidth = 0.85f;

    public BarData() {
        super();
    }

    public BarData(IBarDataSet... dataSets) {
        super(dataSets);
    }

    public BarData(List<IBarDataSet> dataSets) {
        super(dataSets);
    }

    public void setBarWidth(float mBarWidth) {
        this.mBarWidth = mBarWidth;
    }

    public float getBarWidth() {
        return mBarWidth;
    }

    public void groupBars(float fromX, float groupSpace, float barSpace) {

        int setCount = mDataSets.size();
        if (setCount <= 1) {
            throw new RuntimeException("BarData needs to hold at least 2 BarDataSets to allow grouping.");
        }

        IBarDataSet max = getMaxEntryCountSet();
        int maxEntryCount = max.getEntryCount();

        float groupSpaceWidthHalf = groupSpace / 2f;
        float barSpaceHalf = barSpace / 2f;
        float barWidthHalf = mBarWidth / 2f;

        float interval = getGroupWidth(groupSpace, barSpace);

        for (int i = 0; i < maxEntryCount; i++) {

            float start = fromX;
            fromX += groupSpaceWidthHalf;

            for (IBarDataSet set : mDataSets) {

                fromX += barSpaceHalf;
                fromX += barWidthHalf;

                if (i < set.getEntryCount()) {

                    BarEntry entry = set.getEntryForIndex(i);

                    if (entry != null) {
                        entry.setX(fromX);
                    }
                }

                fromX += barWidthHalf;
                fromX += barSpaceHalf;
            }

            fromX += groupSpaceWidthHalf;
            float end = fromX;
            float innerInterval = end - start;
            float diff = interval - innerInterval;

            if (diff > 0 || diff < 0) {
                fromX += diff;
            }
        }

        notifyDataChanged();
    }

    public float getGroupWidth(float groupSpace, float barSpace) {
        return mDataSets.size() * (mBarWidth + barSpace) + groupSpace;
    }
}
