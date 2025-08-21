/*
 * Copyright (c) 2016-present 贵州纳雍穿青人李裕江<1032694760@qq.com>
 *
 * The software is licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *     http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR
 * PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.github.gzuliyujiang.wheelpicker.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.gzuliyujiang.wheelpicker.annotation.DateMode;
import com.github.gzuliyujiang.wheelpicker.annotation.TimeMode;
import com.github.gzuliyujiang.wheelpicker.contract.DateFormatter;
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity;
import com.github.gzuliyujiang.wheelpicker.impl.SimpleDateFormatter;
import com.github.gzuliyujiang.wheelview.annotation.CurtainCorner;
import com.github.gzuliyujiang.wheelview.annotation.ItemTextAlign;
import com.github.gzuliyujiang.wheelview.annotation.ScrollState;
import com.github.gzuliyujiang.wheelview.widget.NumberWheelView;
import com.github.gzuliyujiang.wheelview.widget.WheelView;
import com.topdon.lib.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 日期时间滚轮控件
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2019/5/14 15:26
 */
@SuppressWarnings("unused")
public class YearWheelLayout extends BaseWheelLayout {
    private DateWheelLayout dateWheelLayout;
    private DatimeEntity startValue;
    private DatimeEntity endValue;

    public YearWheelLayout(Context context) {
        super(context);
    }

    public YearWheelLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public YearWheelLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public YearWheelLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int provideLayoutRes() {
        return R.layout.wheel_picker_year;
    }

    @Override
    protected int[] provideStyleableRes() {
        return R.styleable.DatimeWheelLayout;
    }

    @Override
    protected List<WheelView> provideWheelViews() {
        List<WheelView> list = new ArrayList<>();
        list.addAll(dateWheelLayout.provideWheelViews());
        return list;
    }

    @Override
    protected void onInit(@NonNull Context context) {
        dateWheelLayout = findViewById(R.id.wheel_picker_date_wheel);

        //初始颜色
        setCurtainEnabled(true);
        getMonthLabelView().setBackgroundColor(0x1A2B79D7);
        getYearLabelView().setBackgroundColor(0x1A2B79D7);
        getDayLabelView().setBackgroundColor(0x1A2B79D7);

        post(() -> {
            View view_select_bg = findViewById(R.id.view_select_bg);
            ViewGroup.LayoutParams params = view_select_bg.getLayoutParams();
            params.height = dateWheelLayout.getYearWheelView().itemHeight;
            view_select_bg.setLayoutParams(params);
        });
    }

    @Override
    protected void onAttributeSet(@NonNull Context context, @NonNull TypedArray typedArray) {
        float density = context.getResources().getDisplayMetrics().density;
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        setVisibleItemCount(typedArray.getInt(R.styleable.DatimeWheelLayout_wheel_visibleItemCount, 5));
        setSameWidthEnabled(typedArray.getBoolean(R.styleable.DatimeWheelLayout_wheel_sameWidthEnabled, false));
        setMaxWidthText(typedArray.getString(R.styleable.DatimeWheelLayout_wheel_maxWidthText));
        setSelectedTextColor(typedArray.getColor(R.styleable.DatimeWheelLayout_wheel_itemTextColorSelected, 0xFF000000));
        setTextColor(typedArray.getColor(R.styleable.DatimeWheelLayout_wheel_itemTextColor, 0xFF888888));
        setTextSize(typedArray.getDimension(R.styleable.DatimeWheelLayout_wheel_itemTextSize, 15 * scaledDensity));
        setSelectedTextSize(typedArray.getDimension(R.styleable.DatimeWheelLayout_wheel_itemTextSizeSelected, 15 * scaledDensity));
        setSelectedTextBold(typedArray.getBoolean(R.styleable.DatimeWheelLayout_wheel_itemTextBoldSelected, false));
        setTextAlign(typedArray.getInt(R.styleable.DatimeWheelLayout_wheel_itemTextAlign, ItemTextAlign.CENTER));
        setItemSpace(typedArray.getDimensionPixelSize(R.styleable.DatimeWheelLayout_wheel_itemSpace,
                (int) (20 * density)));
        setCyclicEnabled(typedArray.getBoolean(R.styleable.DatimeWheelLayout_wheel_cyclicEnabled, false));
        setIndicatorEnabled(typedArray.getBoolean(R.styleable.DatimeWheelLayout_wheel_indicatorEnabled, false));
        setIndicatorColor(typedArray.getColor(R.styleable.DatimeWheelLayout_wheel_indicatorColor, 0xFFC9C9C9));
        setIndicatorSize(typedArray.getDimension(R.styleable.DatimeWheelLayout_wheel_indicatorSize, 1 * density));
        setCurvedIndicatorSpace(typedArray.getDimensionPixelSize(R.styleable.DatimeWheelLayout_wheel_curvedIndicatorSpace, (int) (1 * density)));
        setCurtainEnabled(typedArray.getBoolean(R.styleable.DatimeWheelLayout_wheel_curtainEnabled, false));
        setCurtainColor(typedArray.getColor(R.styleable.DatimeWheelLayout_wheel_curtainColor, 0x88FFFFFF));
        setCurtainCorner(typedArray.getInt(R.styleable.DatimeWheelLayout_wheel_curtainCorner, CurtainCorner.NONE));
        setCurtainRadius(typedArray.getDimension(R.styleable.DatimeWheelLayout_wheel_curtainRadius, 0));
        setAtmosphericEnabled(typedArray.getBoolean(R.styleable.DatimeWheelLayout_wheel_atmosphericEnabled, false));
        setCurvedEnabled(typedArray.getBoolean(R.styleable.DatimeWheelLayout_wheel_curvedEnabled, false));
        setCurvedMaxAngle(typedArray.getInteger(R.styleable.DatimeWheelLayout_wheel_curvedMaxAngle, 90));
        setDateMode(typedArray.getInt(R.styleable.DatimeWheelLayout_wheel_dateMode, DateMode.YEAR));
        String yearLabel = typedArray.getString(R.styleable.DatimeWheelLayout_wheel_yearLabel);
        String monthLabel = typedArray.getString(R.styleable.DatimeWheelLayout_wheel_monthLabel);
        String dayLabel = typedArray.getString(R.styleable.DatimeWheelLayout_wheel_dayLabel);
        setDateLabel(yearLabel, monthLabel, dayLabel);
        setDateFormatter(new SimpleDateFormatter());
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE && startValue == null && endValue == null) {
            setRange(DatimeEntity.now(), DatimeEntity.yearOnFuture(30), DatimeEntity.now());
        }
    }

    @Override
    public void onWheelSelected(WheelView view, int position) {
        dateWheelLayout.onWheelSelected(view, position);
    }

    @Override
    public void onWheelScrolled(WheelView view, int offset) {
        dateWheelLayout.onWheelScrolled(view, offset);
    }

    @Override
    public void onWheelScrollStateChanged(WheelView view, @ScrollState int state) {
        dateWheelLayout.onWheelScrollStateChanged(view, state);
    }

    @Override
    public void onWheelLoopFinished(WheelView view) {
        dateWheelLayout.onWheelLoopFinished(view);
    }

    public void setDateMode(@DateMode int dateMode) {
        dateWheelLayout.setDateMode(dateMode);
    }

    /**
     * 设置日期时间范围
     */
    public void setRange(DatimeEntity startValue, DatimeEntity endValue) {
        setRange(startValue, endValue, null);
    }

    /**
     * 设置日期时间范围
     */
    public void setRange(DatimeEntity startValue, DatimeEntity endValue, DatimeEntity defaultValue) {
        if (startValue == null) {
            startValue = DatimeEntity.now();
        }
        if (endValue == null) {
            endValue = DatimeEntity.yearOnFuture(10);
        }
        if (defaultValue == null) {
            defaultValue = startValue;
        }
        dateWheelLayout.setRange(startValue.getDate(), endValue.getDate(), defaultValue.getDate());
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public void setDefaultValue(DatimeEntity defaultValue) {
        if (defaultValue == null) {
            defaultValue = DatimeEntity.now();
        }
        dateWheelLayout.setDefaultValue(defaultValue.getDate());
    }

    public void setDateFormatter(DateFormatter dateFormatter) {
        dateWheelLayout.setDateFormatter(dateFormatter);
    }

    public void setDateLabel(CharSequence year, CharSequence month, CharSequence day) {
        dateWheelLayout.setDateLabel(year, month, day);
    }

    public void setResetWhenLinkage(boolean dateResetWhenLinkage, boolean timeResetWhenLinkage) {
        dateWheelLayout.setResetWhenLinkage(dateResetWhenLinkage);
    }

    public final DatimeEntity getStartValue() {
        return startValue;
    }

    public final DatimeEntity getEndValue() {
        return endValue;
    }

    public final DateWheelLayout getDateWheelLayout() {
        return dateWheelLayout;
    }

    public final NumberWheelView getYearWheelView() {
        return dateWheelLayout.getYearWheelView();
    }

    public final NumberWheelView getMonthWheelView() {
        return dateWheelLayout.getMonthWheelView();
    }

    public final NumberWheelView getDayWheelView() {
        return dateWheelLayout.getDayWheelView();
    }

    public final TextView getYearLabelView() {
        return dateWheelLayout.getYearLabelView();
    }

    public final TextView getMonthLabelView() {
        return dateWheelLayout.getMonthLabelView();
    }

    public final TextView getDayLabelView() {
        return dateWheelLayout.getDayLabelView();
    }

    public final TextView getSpaceStartView() {
        return dateWheelLayout.getSpaceStartView();
    }

    public final TextView getSpaceEndView() {
        return dateWheelLayout.getSpaceEndView();
    }

    public final int getSelectedYear() {
        return dateWheelLayout.getSelectedYear();
    }

    public final int getSelectedMonth() {
        return dateWheelLayout.getSelectedMonth();
    }

    public final int getSelectedDay() {
        return dateWheelLayout.getSelectedDay();
    }

}
