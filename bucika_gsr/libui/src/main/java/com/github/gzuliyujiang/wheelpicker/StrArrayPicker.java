

package com.github.gzuliyujiang.wheelpicker;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.github.gzuliyujiang.dialog.DialogLog;
import com.github.gzuliyujiang.wheelpicker.entity.SexEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrArrayPicker extends OptionPicker {

    @NonNull
    private final List<String> optionList;

    public StrArrayPicker(Activity activity, @NonNull String[] optionArray, int defaultPosition) {
        super(activity);
        this.optionList = Arrays.asList(optionArray);
        this.defaultPosition = defaultPosition;
    }

    @Override
    protected List<?> provideData() {
        return optionList;
    }

}
