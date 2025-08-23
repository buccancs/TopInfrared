
package com.github.mikephil.charting.data;

import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class LineData extends BarLineScatterCandleBubbleData<ILineDataSet> {

    public LineData() {
        super();
    }

    public LineData(ILineDataSet... dataSets) {
        super(dataSets);
    }

    public LineData(List<ILineDataSet> dataSets) {
        super(dataSets);
    }
}
