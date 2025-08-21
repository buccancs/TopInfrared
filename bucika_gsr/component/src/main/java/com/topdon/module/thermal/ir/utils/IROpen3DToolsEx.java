package com.topdon.module.thermal.ir.utils;

import android.util.Log;

import com.example.opengl.render.IROpen3DTools;

import org.opencv.core.Mat;

import static org.opencv.core.Core.NORM_MINMAX;
import static org.opencv.core.Core.normalize;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC2;
import static org.opencv.imgproc.Imgproc.COLOR_YUV2GRAY_YUYV;
import static org.opencv.imgproc.Imgproc.applyColorMap;
import static org.opencv.imgproc.Imgproc.cvtColor;

/**
 * @author: CaiSongL
 * @date: 2023/10/26 19:58
 */
public class IROpen3DToolsEx  extends IROpen3DTools {

    private Mat img ;

    @Override
    public void init(byte[] image_, int type) {
        long time = System.currentTimeMillis();
        rws = 192;
        cls = 256;
        if (gray_image == null){
            gray_image = new Mat();
        }

        img = new Mat(rws, cls, CV_8UC2);
        img.put(0, 0, image_);
        cvtColor(img, img, COLOR_YUV2GRAY_YUYV);
        normalize(img, img, 0, 255, NORM_MINMAX);
        img.convertTo(gray_image, CV_8UC1);
        image = new Mat();
        applyColorMap(gray_image,image,15);
//        image = psuColor(gray_image,type);
        halfx = (float)rws / 2;
        halfy = (float)cls / 2;
    }
}
