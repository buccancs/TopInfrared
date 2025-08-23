package com.github.mikephil.charting.formatter;

import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;

public interface IFillFormatter
{

    float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider);
}
