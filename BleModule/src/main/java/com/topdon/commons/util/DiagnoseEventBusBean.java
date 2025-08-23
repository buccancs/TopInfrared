package com.topdon.commons.util;

public class DiagnoseEventBusBean {
    private int what;
    private String language;
    private boolean snConnection;
    private boolean isDiagnose;
    private long mDiagEntryType;
    private long mDiagMenuMask;
    private String snPath;

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
