

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
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class SexPicker extends OptionPicker {
    public static String JSON = "[{\"id\":0,\"name\":\"保密\",\"english\":\"Secrecy\"},\n" +
            "{\"id\":1,\"name\":\"男\",\"english\":\"Male\"},\n" +
            "{\"id\":2,\"name\":\"女\",\"english\":\"Female\"}]";
    private boolean includeSecrecy;

    public SexPicker(Activity activity) {
        super(activity);
    }

    public SexPicker(@NonNull Activity activity, @StyleRes int themeResId) {
        super(activity, themeResId);
    }

    public void setIncludeSecrecy(boolean includeSecrecy) {
        this.includeSecrecy = includeSecrecy;
        setData(provideData());
    }

    @Override
    public void setDefaultValue(Object item) {
        if (item instanceof String) {
            setDefaultValueByName(item.toString());
        } else {
            super.setDefaultValue(item);
        }
    }

    public void setDefaultValueByName(String name) {
        SexEntity entity = new SexEntity();
        entity.setName(name);
        super.setDefaultValue(entity);
    }

    public void setDefaultValueByEnglish(String english) {
        SexEntity entity = new SexEntity();
        entity.setEnglish(english);
        super.setDefaultValue(entity);
    }

    @Override
    protected List<?> provideData() {
        ArrayList<SexEntity> data = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(JSON);
            for (int i = 0, n = jsonArray.length(); i < n; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                SexEntity entity = new SexEntity();
                entity.setId(jsonObject.getString("id"));
                entity.setName(jsonObject.getString("name"));
                entity.setEnglish(jsonObject.getString("english"));
                if (!includeSecrecy && "0".equals(entity.getId())) {
                    continue;
                }
                data.add(entity);
            }
        } catch (JSONException e) {
            DialogLog.print(e);
        }
        return data;
    }

}
