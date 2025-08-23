package com.example.thermal_lite.util;

import android.content.Context;
import android.content.res.AssetManager;

import com.energy.irutilslibrary.LibIRParse;

import java.io.IOException;
import java.io.InputStream;

public class CommonUtil {
    public static final String TAG = "CommonUtil";

    public static byte[] getAssetData(Context mContext, String assetTauName) {
        byte[] tau_data = null;
        AssetManager am = mContext.getAssets();
        InputStream is = null;
        try {
            is = am.open(assetTauName);
            int lenth = is.available();
            tau_data = new byte[lenth];
            if (is.read((tau_data)) != lenth) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tau_data;
    }

    public static int convertArrayY16ToY14(byte[] y16_data, int pixel_num, byte[] y14_data) {
        if (y16_data != null && y14_data != null) {
            if (pixel_num <= 0) {
                return LibIRParse.IrparseResult.IRPARSE_ERROR_PARAM.getValue();
            } else {
                for (int i = 0; i < pixel_num; ++i) {
                    y14_data[i] = (byte) (y16_data[i] >> 2);
                }

                return LibIRParse.IrparseResult.IRPARSE_SUCCESS.getValue();
            }
        } else {
            return LibIRParse.IrparseResult.IRPARSE_ERROR_PARAM.getValue();
        }
    }

    public static int bytesToInt(byte[] src, int offset) {
        int value;
        value = (int) ((src[offset] & 0xFF)
                | ((src[offset + 1] & 0xFF) << 8)
                | ((src[offset + 2] & 0xFF) << 16)
                | ((src[offset + 3] & 0xFF) << 24));
        return value;
    }

    public static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        final float factor = (float) Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    public static int mCount = 0;
    private static long mTimeStart = 0;
    private static double mFps = 0;
    public static double showFps() {
        mCount++;
        if (mCount == 100) {
            mCount = 0;
            long currentTimeMillis = System.currentTimeMillis();
            if (mTimeStart != 0) {
                long timeuse = currentTimeMillis - mTimeStart;
                mFps = 100 * 1000 / (timeuse + 0.0);
            }
            mTimeStart = currentTimeMillis;
        }

        return mFps;
    }

    public static String byteToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }

        String hexString = sb.toString().trim();
        return hexString;
    }

}
