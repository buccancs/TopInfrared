

package com.github.gzuliyujiang.wheelpicker.entity;

import androidx.annotation.NonNull;

import com.github.gzuliyujiang.wheelview.contract.TextProvider;

import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

public class EthnicEntity implements TextProvider, Serializable {
    private static final boolean IS_CHINESE;
    private String code;
    private String name;
    private String spelling;

    static {
        IS_CHINESE = Locale.getDefault().getDisplayLanguage().contains("中文");
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpelling() {
        return spelling;
    }

    public void setSpelling(String spelling) {
        this.spelling = spelling;
    }

    @Override
    public String provideText() {
        if (IS_CHINESE) {
            return name;
        }
        return spelling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EthnicEntity that = (EthnicEntity) o;
        return Objects.equals(code, that.code) ||
                Objects.equals(name, that.name) ||
                Objects.equals(spelling, that.spelling);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, spelling);
    }

    @NonNull
    @Override
    public String toString() {
        return "EthnicEntity{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", spelling='" + spelling + '\'' +
                '}';
    }

}
