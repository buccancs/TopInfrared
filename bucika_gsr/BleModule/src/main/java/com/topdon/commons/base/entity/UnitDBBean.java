package com.topdon.commons.base.entity;

import java.io.Serializable;

/**
 * @Desc 单位转换
 * @ClassName UnitDBBean
 * @Email 616862466@qq.com
 * @Author 子墨
 * @Date 2022/12/21 15:38
 */
public class UnitDBBean implements Serializable {
//    {
//        "转换关系": "公转英",
//            "转换前单位": "m",
//            "转换前中文名称": "米",
//            "转换后单位": "yd.",
//            "转换后中文名称": "码",
//            "转换公式": "1 米 = 1.094码",
//            "计算因子": "1.094"
//    },

    private static final long serialVersionUID = -1L;
    public Long dbid;
    String LoginName;//登录账号
    int unitType;//0 公制类型  1 英制类型
    String conversionRelation;//转换关系
    String preUnit;//转换前单位
    String preName;//转换前中文名称
    String afterUnit;//转换后单位
    String afterName;//转换后中文名称
    String conversionFormula;//转换公式
    String calcFactor;//计算因子


    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public String getLoginName() {
        return LoginName;
    }

    public void setLoginName(String loginName) {
        LoginName = loginName;
    }

    public int getUnitType() {
        return unitType;
    }

    public void setUnitType(int unitType) {
        this.unitType = unitType;
    }

    public String getConversionRelation() {
        return conversionRelation;
    }

    public void setConversionRelation(String conversionRelation) {
        this.conversionRelation = conversionRelation;
    }

    public String getPreUnit() {
        return preUnit;
    }

    public void setPreUnit(String preUnit) {
        this.preUnit = preUnit;
    }

    public String getPreName() {
        return preName;
    }

    public void setPreName(String preName) {
        this.preName = preName;
    }

    public String getAfterUnit() {
        return afterUnit;
    }

    public void setAfterUnit(String afterUnit) {
        this.afterUnit = afterUnit;
    }

    public String getAfterName() {
        return afterName;
    }

    public void setAfterName(String afterName) {
        this.afterName = afterName;
    }

    public String getConversionFormula() {
        return conversionFormula;
    }

    public void setConversionFormula(String conversionFormula) {
        this.conversionFormula = conversionFormula;
    }

    public String getCalcFactor() {
        return calcFactor;
    }

    public void setCalcFactor(String calcFactor) {
        this.calcFactor = calcFactor;
    }


}
