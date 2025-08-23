
package com.github.mikephil.charting.utils;

public class TransformerHorizontalBarChart extends Transformer {

    public TransformerHorizontalBarChart(ViewPortHandler viewPortHandler) {
        super(viewPortHandler);
    }

    public void prepareMatrixOffset(boolean inverted) {

        mMatrixOffset.reset();

        if (!inverted)
            mMatrixOffset.postTranslate(mViewPortHandler.offsetLeft(),
                    mViewPortHandler.getChartHeight() - mViewPortHandler.offsetBottom());
        else {
            mMatrixOffset
                    .setTranslate(
                            -(mViewPortHandler.getChartWidth() - mViewPortHandler.offsetRight()),
                            mViewPortHandler.getChartHeight() - mViewPortHandler.offsetBottom());
            mMatrixOffset.postScale(-1.0f, 1.0f);
        }

    }
}
