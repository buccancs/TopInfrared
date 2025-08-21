package com.topdon.commons.util;

/**
 * @Desc 诊断语言实体类
 * @ClassName DiagnoseLanguageBean
 * @Email 616862466@qq.com
 * @Author 子墨
 * @Date 2022/9/13 15:40
 */

public class DiagnoseEventBusBean {
    private int what;//1 语言  2 sn连接  3进入诊断或者百公里加速 4 诊断或者保养  5 Folder sn号路径   6 diagMenuMask
    private String language;
    private boolean snConnection;// true sn不相等  false 相等
    private boolean isDiagnose;// true 进入诊断或者百公里加速  false  未进入诊断或者百公里加速
    private long mDiagEntryType;// 进入诊断的方式
    private long mDiagMenuMask;//车型实际开发内容配置
    private String snPath;//sn文件夹路径

    public String getSnPath() {
        return snPath;
    }

    public void setSnPath(String snPath) {
        this.snPath = snPath;
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isSnConnection() {
        return snConnection;
    }

    public void setSnConnection(boolean snConnection) {
        this.snConnection = snConnection;
    }

    public boolean isDiagnose() {
        return isDiagnose;
    }

    public void setDiagnose(boolean diagnose) {
        isDiagnose = diagnose;
    }

    public long getmDiagEntryType() {
        return mDiagEntryType;
    }

    public void setmDiagEntryType(long mDiagEntryType) {
        this.mDiagEntryType = mDiagEntryType;
    }

    public long getDiagMenuMask() {
        return mDiagMenuMask;
    }

    public void setDiagMenuMask(long diagMenuMask) {
        mDiagMenuMask = diagMenuMask;
    }
}
