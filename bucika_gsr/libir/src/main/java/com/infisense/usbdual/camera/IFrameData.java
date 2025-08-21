package com.infisense.usbdual.camera;

import com.infisense.usbdual.Const;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class IFrameData {
    /**
     * 融合图像数据长度，ARGB，故值为：
     * 融合图像输出宽度 x 融合图像输出高度 x 4.
     */
    public static int FUSION_LEN = Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 4;
    /**
     * 原始红外数据长度、原始温度数据长度，YUV-Y16，故值为：
     * 原始红外宽度 x 原始红外高度 x 2.
     */
    public static int ORIGINAL_LEN = Const.IR_WIDTH * Const.IR_HEIGHT * 2;
    /**
     * 缩放温度数据长度，YUV-422，故值为：
     * 融合图像输出宽度 x 融合图像输出高度 x 2.
     */
    public static int REMAP_TEMP_LEN = Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 2;
    /**
     * 原始可见光数据长度，RGB24，故值为：
     * 原始可见光宽度 x 原始可见光高度 x 3.
     */
    public static int LIGHT_LEN = Const.VL_WIDTH * Const.VL_HEIGHT * 3;
    /**
     * 缩放可见光数据长度，ARGB，故值为：
     * 原始可见光宽度 x 原始可见光高度 x 4.
     */
    public static int P_IN_P_LEN = Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 4;
    /**
     * 一帧除画中画缩放缩放可见光数据之外的所有数据长度，
     * 包含 融合图像、原始红外、原始温度、缩放温度、原始可见光、画中画缩放可见光 数据.
     * 值为上述数据长度之和.
     */
    public static int FRAME_LEN = FUSION_LEN + ORIGINAL_LEN + ORIGINAL_LEN + REMAP_TEMP_LEN + LIGHT_LEN + P_IN_P_LEN;



    /**
     * 将指定帧数据中 ARGB <b>融合图像数据</b> 复制到指定数组中.
     */
    public static byte[] readFusionData(@NonNull byte[] frame, @Nullable byte[] fusionData) {
        if (fusionData == null) {
            fusionData = new byte[FUSION_LEN];
        }
        System.arraycopy(frame, 0, fusionData, 0, fusionData.length);   //融合图像数据，ARGB
        return fusionData;
    }

    /**
     * 将指定帧数据中 YUV-16 <b>原始红外数据</b> 复制到指定数组中.
     */
    public static byte[] readNorIRData(@NonNull byte[] frame, @Nullable byte[] irData) {
        if (irData == null) {
            irData = new byte[ORIGINAL_LEN];
        }
        System.arraycopy(frame, FUSION_LEN, irData, 0, irData.length); //原始红外数据，YUV-Y16
        return irData;
    }

    /**
     * 将指定帧数据中 YUV-16 <b>原始温度数据</b> 复制到指定数组中.
     */
    public static byte[] readNorTempData(@NonNull byte[] frame, @Nullable byte[] norTempData) {
        if (norTempData == null) {
            norTempData = new byte[ORIGINAL_LEN];
        }
        System.arraycopy(frame, FUSION_LEN + ORIGINAL_LEN, norTempData, 0, norTempData.length); //原始温度数据，YUV-Y16
        return norTempData;
    }

    /**
     * 将指定帧数据中 YUV-422 <b>缩放温度数据</b> 复制到指定数组中.
     */
    public static byte[] readRemapTempData(@NonNull byte[] frame, @Nullable byte[] remapTempData) {
        if (remapTempData == null) {
            remapTempData = new byte[REMAP_TEMP_LEN];
        }
        System.arraycopy(frame, FUSION_LEN + ORIGINAL_LEN + ORIGINAL_LEN, remapTempData, 0, remapTempData.length); //缩放温度数据，YUV-422
        return remapTempData;
    }
}
