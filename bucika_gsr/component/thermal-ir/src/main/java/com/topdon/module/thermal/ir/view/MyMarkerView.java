package com.topdon.module.thermal.ir.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.topdon.lib.core.db.entity.ThermalEntity;
import com.topdon.lib.core.tools.TimeTool;
import com.topdon.lib.core.tools.UnitTools;
import com.topdon.module.thermal.ir.R;

import java.util.Locale;


/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
public class MyMarkerView extends MarkerView {

    private final TextView tvContent;
    private final TextView timeText;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tvContent = findViewById(R.id.tvContent);
        timeText = findViewById(R.id.time_text);
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        try {
            if (e instanceof CandleEntry) {
                CandleEntry ce = (CandleEntry) e;
                tvContent.setText(Utils.formatNumber(ce.getHigh(), 0, true));
            } else {
                if (e.getData() instanceof ThermalEntity) {
                    ThermalEntity data = (ThermalEntity) e.getData();
                    int index = highlight.getDataIndex();//曲线序号
                    StringBuilder str = new StringBuilder();
                    if (index == 0) {
                        str.append(com.blankj.utilcode.util.Utils.getApp().getString(R.string.chart_temperature) + ": ").append(UnitTools.showC(data.getThermal()));
                    } else if (index == 1) {
                        str.append(com.blankj.utilcode.util.Utils.getApp().getString(R.string.chart_temperature_high) + ": ").append(UnitTools.showC(data.getThermalMax()));
                        str.append(System.getProperty("line.separator")).append(com.blankj.utilcode.util.Utils.getApp().getString(R.string.chart_temperature_low) + ": ").append(UnitTools.showC(data.getThermalMin()));
                    } else {
                        str.append(com.blankj.utilcode.util.Utils.getApp().getString(R.string.chart_temperature_high) + ": ").append(UnitTools.showC(data.getThermalMax()));
                        str.append(System.getProperty("line.separator")).append(com.blankj.utilcode.util.Utils.getApp().getString(R.string.chart_temperature_low) + ": ").append(UnitTools.showC(data.getThermalMin()));
                    }
                    tvContent.setText(str.toString());
                    timeText.setText(TimeTool.INSTANCE.showTimeSecond(data.getCreateTime()));
                } else {
                    tvContent.setText(com.blankj.utilcode.util.Utils.getApp().getString(R.string.chart_temperature) + ": " + String.format(Locale.ENGLISH, "%.1f", e.getY()) + UnitTools.showUnit());
                    timeText.setVisibility(View.GONE);
                }
            }
        } catch (Exception ex) {
            XLog.e("MarkerView error: " + ex.getMessage());
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}
