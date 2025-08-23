

package com.github.gzuliyujiang.wheelpicker.contract;

import androidx.annotation.NonNull;

import java.util.List;

public interface LinkageProvider {
    int INDEX_NO_FOUND = -1;

    boolean firstLevelVisible();

    boolean thirdLevelVisible();

    @NonNull
    List<?> provideFirstData();

    @NonNull
    List<?> linkageSecondData(int firstIndex);

    @NonNull
    List<?> linkageThirdData(int firstIndex, int secondIndex);

    int findFirstIndex(Object firstValue);

    int findSecondIndex(int firstIndex, Object secondValue);

    int findThirdIndex(int firstIndex, int secondIndex, Object thirdValue);

}