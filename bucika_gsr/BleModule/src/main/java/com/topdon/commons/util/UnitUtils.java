package com.topdon.commons.util;

import android.text.TextUtils;

import com.blankj.utilcode.util.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.topdon.commons.base.entity.UnitDBBean;
import com.topdon.lms.sdk.utils.SPUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UnitUtils {

    public static List<UnitDBBean> getUnitDBBeanList(int unitType) {
        try {
            String jsonStr;
            if (unitType == 0) {
                jsonStr = PreUtil.getInstance(Topdon.getApp()).get(SPKeyUtils.UNIT_METRIC);
            } else {
                jsonStr = PreUtil.getInstance(Topdon.getApp()).get(SPKeyUtils.UNIT_BRITISH);
            }
            LLog.w("bcf--jsonStr", jsonStr);
            if (TextUtils.isEmpty(jsonStr)) {
                return new ArrayList<>();
            }
            List<UnitDBBean> unitDBBeanList = GsonUtils.fromJson(jsonStr, new TypeToken<List<UnitDBBean>>() {
            }.getType());
            return unitDBBeanList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static HashMap<String, UnitDBBean> getUnitDBBeanHashMap() {
        String unit = (String) SPUtils.getInstance(Topdon.getApp()).get("unit", "0");
        int unitType = "0".equals(unit) ? 0 : 1;
        return getUnitDBBeanHashMap(unitType);
    }

    public static HashMap<String, UnitDBBean> getUnitDBBeanHashMap(int unitType) {
        HashMap<String, UnitDBBean> hashMap = new HashMap<>();
        try {
            List<UnitDBBean> unitDBBeanList = getUnitDBBeanList(unitType);
            for (UnitDBBean unitDBBean : unitDBBeanList) {
                hashMap.put(unitDBBean.getPreUnit().toLowerCase(), unitDBBean);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hashMap;
    }

    public static String[] getCalcResult(HashMap<String, UnitDBBean> hashMap, String preUnit, String numericalValue) {
        String unit = (String) SPUtils.getInstance(Topdon.getApp()).get("unit", "0");
        int unitType = "0".equals(unit) ? 0 : 1;
        return getCalcResult(unitType, hashMap, preUnit, numericalValue);
    }

    public static String[] getCalcResult(int unitType, HashMap<String, UnitDBBean> hashMap, String preUnit, String numericalValue) {
        UnitDBBean unitDBBean = null;
        try {
            if (TextUtils.isEmpty(preUnit)) {
                return new String[]{numericalValue, preUnit};
            }
            unitDBBean = hashMap.get(preUnit.toLowerCase());
            if (unitDBBean == null) {
                return new String[]{numericalValue, preUnit};
            }
            if (unitType == 0) {
                if (preUnit.equalsIgnoreCase(unitDBBean.getAfterUnit())) {
                    return new String[]{numericalValue, unitDBBean.getAfterUnit()};
                }
                if (preUnit.equalsIgnoreCase("K")) {
                    try {
                        return new String[]{String.valueOf(getResult(Double.parseDouble(numericalValue) - 273.15)), unitDBBean.getAfterUnit()};
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return new String[]{numericalValue, unitDBBean.getAfterUnit()};
                    }
                } else if (preUnit.equals("deg.F")) {
                    try {
                        return new String[]{String.valueOf(getResult((Double.parseDouble(numericalValue) - 32) / 1.8)), "°C"};
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new String[]{numericalValue, unitDBBean.getAfterUnit()};
                    }
                }
                return new String[]{String.valueOf(getResult(Double.parseDouble(numericalValue) * Double.parseDouble(unitDBBean.getCalcFactor()))), unitDBBean.getAfterUnit()};
            } else {
                if (preUnit.equalsIgnoreCase(unitDBBean.getAfterUnit())) {
                    return new String[]{numericalValue, unitDBBean.getAfterUnit()};
                }
                if (preUnit.equalsIgnoreCase("K")) {
                    try {
                        return new String[]{String.valueOf(getResult(32 + (Double.parseDouble(numericalValue) - 273.15) * 1.8)), unitDBBean.getAfterUnit()};
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new String[]{numericalValue, unitDBBean.getAfterUnit()};
                    }
                } else if (preUnit.equalsIgnoreCase("deg.C")) {
                    try {
                        return new String[]{String.valueOf(getResult(32 + Double.parseDouble(numericalValue) * 1.8)), "°F"};
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new String[]{numericalValue, unitDBBean.getAfterUnit()};
                    }
                }
                return new String[]{String.valueOf(getResult(Double.parseDouble(numericalValue) * Double.parseDouble(unitDBBean.getCalcFactor()))), unitDBBean.getAfterUnit()};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (unitDBBean == null) {
            return new String[]{numericalValue, preUnit};
        } else {
            return new String[]{numericalValue, unitDBBean.getAfterUnit()};
        }

    }

    public static double getResult(double dou) {
        BigDecimal bigDecimal = new BigDecimal(dou).setScale(2, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();

    }

    public static String getDecimalFormatByDouble(double score) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00#");
        return decimalFormat.format(score);
    }

}
