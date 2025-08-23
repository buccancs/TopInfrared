package com.github.mikephil.charting.components;

import android.graphics.Canvas;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

public interface IMarker {

    MPPointF getOffset();

    MPPointF getOffsetForDrawingAtPoint(float posX, float posY);

    void refreshContent(Entry e, Highlight highlight);

    void draw(Canvas canvas, float posX, float posY);
}
