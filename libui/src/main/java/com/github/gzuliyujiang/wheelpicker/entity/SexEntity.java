

package com.github.gzuliyujiang.wheelpicker.entity;

import androidx.annotation.NonNull;

import com.github.gzuliyujiang.wheelview.contract.TextProvider;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class SexEntity implements TextProvider, Serializable {
    private static final boolean IS_CHINESE;
    private String id;
    private String name;
    private String english;

    static {
        IS_CHINESE = Locale.getDefault().getDisplayLanguage().contains("中文");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    @Override
    public String provideText() {
        if (IS_CHINESE) {
            return name;
        }
        return english;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SexEntity that = (SexEntity) o;
        return Objects.equals(id, that.id) ||
                Objects.equals(name, that.name) ||
                Objects.equals(english, that.english);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, english);
    }

    @NonNull
    @Override
    public String toString() {
        return "SexEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", english" + english + '\'' +
                '}';
    }

}
