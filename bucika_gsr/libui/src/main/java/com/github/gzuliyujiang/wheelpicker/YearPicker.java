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

package com.github.gzuliyujiang.wheelpicker;

import android.app.Activity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.gzuliyujiang.dialog.ModalDialog;
import com.github.gzuliyujiang.wheelpicker.contract.OnYearPickedListener;
import com.github.gzuliyujiang.wheelpicker.entity.DateEntity;
import com.github.gzuliyujiang.wheelpicker.entity.DatimeEntity;
import com.github.gzuliyujiang.wheelpicker.widget.YearWheelLayout;
import com.topdon.lib.ui.R;

import java.util.Calendar;

/**
 * 日期时间选择器
 *
 * @author 贵州山野羡民（1032694760@qq.com）
 * @since 2021/6/5 18:21
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class YearPicker extends ModalDialog {
    protected YearWheelLayout wheelLayout;
    private OnYearPickedListener onYearPickedListener;

    public YearPicker(@NonNull Activity activity, @Nullable Integer year) {
        super(activity);

        //初始化年份范围及当前选中年份
        int nowYear = Calendar.getInstance().get(Calendar.YEAR);
        DatimeEntity startTimeEntity = new DatimeEntity();
        startTimeEntity.setDate(DateEntity.target(nowYear - 1000, 1, 1));
        DatimeEntity defaultEntity = new DatimeEntity();
        defaultEntity.setDate(DateEntity.target(year == null ? nowYear : year, 1, 1));
        wheelLayout.setRange(startTimeEntity, DatimeEntity.now(), defaultEntity);

        wheelLayout.setCurtainEnabled(true);//选中栏是否有背景颜色
        wheelLayout.setCurtainColor(ContextCompat.getColor(getContext(), R.color.wheel_select_bg));       //选中栏背景颜色
        wheelLayout.setSelectedTextColor(ContextCompat.getColor(getContext(), R.color.wheel_select_text));//选中文字颜色
        wheelLayout.setTextColor(ContextCompat.getColor(getContext(), R.color.wheel_unselect_text));      //未选中文字颜色

        wheelLayout.setResetWhenLinkage(false, false);//他娘的不知道什么鬼，连个注释都没有

    }

    @NonNull
    @Override
    protected View createBodyView() {
        wheelLayout = new YearWheelLayout(activity);
        return wheelLayout;
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void onOk() {
        if (onYearPickedListener != null) {
            onYearPickedListener.onYearPicked(wheelLayout.getSelectedYear());
        }
    }

    public YearPicker setOnYearPickedListener(OnYearPickedListener onYearPickedListener) {
        this.onYearPickedListener = onYearPickedListener;
        return this;
    }

    public final YearWheelLayout getWheelLayout() {
        return wheelLayout;
    }

}
