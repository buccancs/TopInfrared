package com.infisense.usbir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.SizeUtils;
import com.energy.iruvc.dual.DualUVCCamera;
import com.energy.iruvc.sdkisp.LibIRTemp;
import com.energy.iruvc.utils.DualCameraParams;
import com.energy.iruvc.utils.Line;
import com.energy.iruvc.utils.SynchronizedBitmap;
import com.infisense.usbdual.Const;
import com.infisense.usbdual.camera.BaseDualView;
import com.infisense.usbir.R;
import com.infisense.usbir.inf.ILiteListener;
import com.infisense.usbir.utils.TempDrawHelper;
import com.infisense.usbir.utils.TempUtil;
import com.topdon.lib.core.common.SharedManager;
import com.topdon.lib.core.tools.UnitTools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.List;

/*
 * @Description:
 * @Author:         brilliantzhao
 * @CreateDate:     2022.7.19 17:20
 * @UpdateUser:
 * @UpdateDate:     2022.7.19 17:20
 * @UpdateRemark:
 */
public class TemperatureView extends SurfaceView implements SurfaceHolder.Callback,
        View.OnTouchListener, BaseDualView.OnFrameCallback {

    private static final String TAG = "TemperatureView";


    /**
     * 选中操作灵敏度，当 Touch Down 坐标与点线面坐标偏差在该值范围内，视为选中，单位 px.<br>
     * 删除操作灵敏度，当 Touch UP 与 Touch Down 坐标偏差在该值范围内，视为删除，单位 px.
     */
    private static final int TOUCH_TOLERANCE = SizeUtils.sp2px(7f);


    private int drawCount = 3;

    private final int POINT_MAX_COUNT;
    private final int LINE_MAX_COUNT;
    private final int RECTANGLE_MAX_COUNT;


    /**
     * 对温度数据的解析和处理，以及温度的二次修正等计算.
     */
    @Nullable
    private LibIRTemp irtemp;

    /**
     * {@link #viewWidth} / {@link #temperatureWidth} 的比值.
     */
    private float xScale = 0;
    /**
     * {@link #viewHeight} / {@link #temperatureHeight} 的比值.
     */
    private float yScale = 0;
    /**
     * 当前 View 去除 padding 后剩余的可用宽度，单位 px.
     */
    private int viewWidth = 0;
    /**
     * 当前 View 去除 padding 后剩余的可用高度，单位 px.
     */
    private int viewHeight = 0;
    /**
     * 温度数据宽度，单位 px.
     */
    private int temperatureWidth;
    /**
     * 温度数据高度，单位 px.
     */
    private int temperatureHeight;


    private final TempDrawHelper helper = new TempDrawHelper();



    /**
     * 温度区域模式 - 高低温点重置.
     */
    public static final int REGION_MODE_RESET = -1;
    /**
     * 温度区域模式 - 点.
     */
    public static final int REGION_MODE_POINT = 0;
    /**
     * 温度区域模式 - 线.
     */
    public static final int REGION_MODE_LINE = 1;
    /**
     * 温度区域模式 - 面.
     */
    public static final int REGION_MODE_RECTANGLE = 2;
    /**
     * 温度区域模式 - 全图.
     */
    public static final int REGION_MODE_CENTER = 3;
    /**
     * 温度区域模式 - 趋势图，也就是只一条线.
     */
    public static final int REGION_NODE_TREND = 4;
    /**
     * 温度区域模式 - 清除.
     */
    public static final int REGION_MODE_CLEAN = 5;

    @IntDef({REGION_MODE_RESET, REGION_MODE_POINT, REGION_MODE_LINE, REGION_MODE_RECTANGLE, REGION_MODE_CENTER, REGION_NODE_TREND, REGION_MODE_CLEAN})
    @Retention(RetentionPolicy.SOURCE)
    private @interface RegionMode {
    }

    /**
     * 温度区域模式，由 REGION_MODE_** 定义，默认清除.
     */
    @RegionMode
    private int temperatureRegionMode = REGION_MODE_CLEAN;
    @RegionMode
    public int getTemperatureRegionMode() {
        return this.temperatureRegionMode;
    }
    public void setTemperatureRegionMode(@RegionMode int temperatureRegionMode) {
        this.temperatureRegionMode = temperatureRegionMode;
        if (temperatureRegionMode == REGION_MODE_CENTER) {
            isShowFull = true;
        } else if (temperatureRegionMode == REGION_MODE_CLEAN) {
            isShowFull = false;
        }
    }

    /**
     * 当前是否显示了全图.
     */
    private boolean isShowFull;
    public boolean isShowFull() {
        return isShowFull;
    }
    public void setShowFull(boolean showFull) {
        isShowFull = showFull;
        if (temperatureRegionMode == REGION_MODE_CLEAN) {
            temperatureRegionMode = REGION_MODE_CENTER;
        }
    }


    public void setTextSize(int textSize){
        helper.setTextSize(textSize);
        refreshRegion();
    }

    public void setLinePaintColor(@ColorInt int color) {
        helper.setTextColor(color);
        refreshRegion();
    }

    private void refreshRegion() {
        Canvas surfaceViewCanvas = getHolder().lockCanvas();
        if (surfaceViewCanvas != null) {
            surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            setBitmap();
            surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
            getHolder().unlockCanvasAndPost(surfaceViewCanvas);
        }
    }


    @Nullable
    private OnTrendChangeListener onTrendChangeListener = null;
    /**
     * 设置趋势图温度变化时监听，注意，回调不在主线程！！
     */
    public void setOnTrendChangeListener(@Nullable OnTrendChangeListener onTrendChangeListener) {
        this.onTrendChangeListener = onTrendChangeListener;
    }

    @Nullable
    private Runnable onTrendAddListener = null;
    /**
     * 设置趋势图添加事件监听，放心，回调在主线程.
     */
    public void setOnTrendAddListener(@Nullable Runnable onTrendAddListener) {
        this.onTrendAddListener = onTrendAddListener;
    }

    @Nullable
    private Runnable onTrendRemoveListener = null;
    /**
     * 设置趋势图移除事件监听，放心，回调在主线程.
     */
    public void setOnTrendRemoveListener(@Nullable Runnable onTrendRemoveListener) {
        this.onTrendRemoveListener = onTrendRemoveListener;
    }

    private ILiteListener iLiteListener = null;
    public void setiLiteListener(ILiteListener iLiteListener) {
        this.iLiteListener = iLiteListener;
    }


    /**
     * 单位摄氏度
     */
    private TempListener listener;
    public TempListener getListener() {
        return listener;
    }
    public void setListener(TempListener listener) {
        this.listener = listener;
    }


    private boolean isMonitor = false;//如果是温度监控，则进行实时校验点线面的比例
    public void setMonitor(boolean monitor) {
        isMonitor = monitor;
    }


    /**
     * 观测模式时高温点是否开启
     */
    private boolean isUserHighTemp = false;
    public boolean isUserHighTemp() {
        return isUserHighTemp;
    }
    public void setUserHighTemp(boolean isUserHighTemp) {
        this.isUserHighTemp = isUserHighTemp;
    }

    /**
     * 观测模式时低温点是否开启
     */
    private boolean isUserLowTemp = false;
    public boolean isUserLowTemp() {
        return isUserLowTemp;
    }
    public void setUserLowTemp(boolean isUserLowTemp) {
        this.isUserLowTemp = isUserLowTemp;
    }


    private SynchronizedBitmap syncimage;
    public void setSyncimage(SynchronizedBitmap syncimage) {
        this.syncimage = syncimage;
    }


    private  byte[] temperature;
    public void setTemperature(byte[] temperature) {
        this.temperature = temperature;
    }


    private void setDefPoint(Point point) {
        if (point.x > temperatureWidth && point.x > 0) {
            point.x = temperatureWidth;
        }
        if (point.x <= 0) {
            point.x = 0;
        }
        if (point.y > temperatureHeight) {
            point.y = temperatureHeight;
        }
        if (point.y < 0) {
            point.y = 0;
        }
    }
    public LibIRTemp.TemperatureSampleResult getPointTemp(Point point) {
        if (irtemp == null) {
            return null;
        } else {
            setDefPoint(point);
            return irtemp.getTemperatureOfPoint(point);
        }
    }
    public LibIRTemp.TemperatureSampleResult getLineTemp(Line line) {
        if (irtemp == null) {
            return null;
        } else {
            setDefPoint(line.start);
            setDefPoint(line.end);
            return irtemp.getTemperatureOfLine(line);
        }
    }
    public LibIRTemp.TemperatureSampleResult getRectTemp(Rect rect) {
        if (irtemp == null) {
            return null;
        } else {
            if (rect.top < 0) {
                rect.top = 0;
            }
            if (rect.bottom > temperatureHeight) {
                rect.bottom = temperatureHeight;
            }
            if (rect.left < 0) {
                rect.left = 0;
            }
            if (rect.right > temperatureWidth) {
                rect.right = temperatureWidth;
            }
            return irtemp.getTemperatureOfRect(rect);
        }
    }



    public int productType = Const.TYPE_IR;


    /**
     * 以 View 尺寸为坐标系，当前已添加的趋势图对应直线，坐标为修正过后的坐标，null 表示未绘制.
     */
    @Nullable
    private Line trendLine;
    /**
     * 以 View 尺寸为坐标系，当前已添加的点列表，坐标为修正过后的坐标.
     */
    private final ArrayList<Point> pointList = new ArrayList<>();
    /**
     * 以 View 尺寸为坐标系，当前已添加的点列表，坐标为修正过后的坐标.
     */
    private final ArrayList<Line> lineList = new ArrayList<>();
    /**
     * 当前绘制的面列表，坐标采用 view 的宽高坐标.
     */
    private final ArrayList<Rect> rectList = new ArrayList<>();

    private final ArrayList<LibIRTemp.TemperatureSampleResult> pointResultList = new ArrayList<>(3);
    private final ArrayList<LibIRTemp.TemperatureSampleResult> lineResultList = new ArrayList<>(3);
    private final ArrayList<LibIRTemp.TemperatureSampleResult> rectangleResultList = new ArrayList<>(3);




    private Bitmap regionBitmap;

    private Bitmap regionAndValueBitmap;
    public Bitmap getRegionBitmap() {
        return regionAndValueBitmap;
    }
    public Bitmap getRegionAndValueBitmap() {
        synchronized (regionLock) {
            return regionAndValueBitmap;
        }
    }


    private final Runnable runnable;
    private Thread temperatureThread;
    private final Object regionLock = new Object();
    private volatile boolean runflag = false;

    /**
     * true-使用摄氏度 flase-使用华氏度
     */
    private final boolean isShowC = SharedManager.INSTANCE.getTemperature() == 1;

    private WeakReference<ITsTempListener> iTsTempListenerWeakReference;

    public void setImageSize(int imageWidth, int imageHeight, ITsTempListener iTsTempListener) {
        if (iTsTempListener != null) {
            iTsTempListenerWeakReference = new WeakReference<>(iTsTempListener);
        }
        this.temperatureWidth = imageWidth;
        this.temperatureHeight = imageHeight;
        if (viewWidth == 0) {
            viewWidth = getMeasuredWidth();
        }
        if (viewHeight == 0) {
            viewHeight = getMeasuredHeight();
        }
        xScale = (float) viewWidth / (float) imageWidth;
        yScale = (float) viewHeight / (float) imageHeight;
        irtemp = new LibIRTemp(imageWidth, imageHeight);
        llTempData = new byte[imageHeight * imageWidth * 2];
        for (int i = 0; i < drawCount; i++) {
            pointResultList.add(irtemp.new TemperatureSampleResult());
            lineResultList.add(irtemp.new TemperatureSampleResult());
            rectangleResultList.add(irtemp.new TemperatureSampleResult());
        }
    }

    public void restView(){
        viewWidth = 0;
        viewHeight = 0;
        viewWidth = getMeasuredWidth();
        xScale = (float) viewWidth / (float) temperatureWidth;
        viewHeight = getMeasuredHeight();
        yScale = (float) viewHeight / (float) temperatureHeight;
    }

    private boolean isShow = false;

    public void start() {
        if (!runflag){
            runflag = true;
            temperatureThread = new Thread(runnable);
            if (isShow) {
                setVisibility(VISIBLE);
            } else {
                setVisibility(INVISIBLE);
            }
            temperatureThread.start();
        }
    }

    public void stop() {
        runflag = false;
        isShow = getVisibility() == View.VISIBLE;
        try {
            if (temperatureThread != null) {
                temperatureThread.interrupt();
                temperatureThread.join();
                temperatureThread = null;
            }
        } catch (InterruptedException ignored) {

        }
    }

    public void clear() {
        if (onTrendRemoveListener != null) {
            onTrendRemoveListener.run();
        }
        trendLine = null;
        pointList.clear();
        lineList.clear();
        rectList.clear();
        if (regionBitmap != null) {
            regionBitmap.eraseColor(0);
        }
        Canvas surfaceViewCanvas = getHolder().lockCanvas();
        if (surfaceViewCanvas != null) {
            surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
            getHolder().unlockCanvasAndPost(surfaceViewCanvas);
        }
        for (int i = 0; i < pointResultList.size(); i++) {
            pointResultList.get(i).index = 0;
        }
        for (int i = 0; i < lineResultList.size(); i++) {
            lineResultList.get(i).index = 0;
        }
        for (int i = 0; i < rectangleResultList.size(); i++) {
            rectangleResultList.get(i).index = 0;
        }
    }

    public void addScalePoint(Point point) {
        float sx = getMeasuredWidth() / (float) temperatureWidth;
        float sy = getMeasuredHeight() / (float) temperatureHeight;
        int viewX = TempDrawHelper.Companion.correctPoint(point.x * sx, getMeasuredWidth());
        int viewY = TempDrawHelper.Companion.correctPoint(point.y * sy, getMeasuredHeight());
        if (pointList.size() == POINT_MAX_COUNT) {
            pointList.remove(0);
        }
        pointList.add(new Point(viewX, viewY));
    }

    public void addScaleLine(Line l) {
        float sx = getMeasuredWidth() / (float) temperatureWidth;
        float sy = getMeasuredHeight() / (float) temperatureHeight;
        Line line = new Line(new Point(), new Point());
        line.start.x = TempDrawHelper.Companion.correct(l.start.x * sx, getMeasuredWidth());
        line.start.y = TempDrawHelper.Companion.correct(l.start.y * sy, getMeasuredHeight());
        line.end.x = TempDrawHelper.Companion.correct(l.end.x * sx, getMeasuredWidth());
        line.end.y = TempDrawHelper.Companion.correct(l.end.y * sy, getMeasuredHeight());
        if (pointList.size() == POINT_MAX_COUNT) {
            pointList.remove(0);
        }
        lineList.add(line);
    }

    public void addScaleRectangle(Rect r) {
        float sx = getMeasuredWidth() / (float) temperatureWidth;
        float sy = getMeasuredHeight() / (float) temperatureHeight;
        Rect rectangle = new Rect();
        rectangle.left = (int) (r.left * sx);
        rectangle.top = (int) (r.top * sy);
        rectangle.right = (int) (r.right * sx);
        rectangle.bottom = (int) (r.bottom * sy);
        if (rectList.size() < RECTANGLE_MAX_COUNT) {
            rectList.add(rectangle);
        } else {
            for (int index = 0; index < rectList.size() - 1; index++) {
                Rect tempRectangle = rectList.get(index + 1);
                rectList.set(index, tempRectangle);
            }
            rectList.set(rectList.size() - 1, rectangle);
        }
    }

    public Point getPoint() {
        if (pointList.isEmpty()) {
            return null;
        }
        return new Point((int) (pointList.get(0).x / xScale), (int) (pointList.get(0).y / yScale));
    }

    public Line getLine() {
        if (!lineList.isEmpty()) {
            Line line = new Line(new Point(), new Point());
            line.start.x = (int) (lineList.get(0).start.x / xScale);
            line.start.y = (int) (lineList.get(0).start.y / yScale);
            line.end.x = (int) (lineList.get(0).end.x / xScale);
            line.end.y = (int) (lineList.get(0).end.y / yScale);
            return line;
        } else {
            return null;
        }
    }

    public Rect getRectangle() {
        if (!rectList.isEmpty()) {
            Rect rect = new Rect();
            rect.left = (int) (rectList.get(0).left / xScale);
            rect.top = (int) (rectList.get(0).top / yScale);
            rect.right = (int) (rectList.get(0).right / xScale);
            rect.bottom = (int) (rectList.get(0).bottom / yScale);
            return rect;
        } else {
            return null;
        }
    }

    public void drawLine() {
        setBitmap();
    }




    public TemperatureView(final Context context) {
        this(context, null, 0);
    }

    public TemperatureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TemperatureView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        // 注意这个方法尽早执行(可以在构造方法里面执行)，解决在小米mix2(Android7.0)上出现的surfaceView内容不展示问题
        setZOrderOnTop(true);

        getHolder().addCallback(this);
        setOnTouchListener(this);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TemperatureView);
        try {
            drawCount = ta.getInteger(R.styleable.TemperatureView_temperature_count, 3);
        } catch (Exception e) {
            // ignored
        } finally {
            ta.recycle();
        }

        POINT_MAX_COUNT = drawCount;
        LINE_MAX_COUNT = drawCount;
        RECTANGLE_MAX_COUNT = drawCount;

        runnable = () -> {
            while (!temperatureThread.isInterrupted() && runflag) {
                byte[] tempArray;
                if (productType == Const.TYPE_IR_DUAL){
                    try {
                        if (remapTempData == null) {
                            Log.d(TAG, "remapTempData == NULL");
                            if (dualUVCCamera != null && llTempData != null
                                    && dualUVCCamera.getTempData(llTempData) != 0) {
                                //获取映射后的温度数据失败
                                Log.d(TAG, "--------error----------");
                                SystemClock.sleep(1000);
                                continue;
                            }
                        } else {
                            Log.d(TAG, "remapTempData != NULL");
                            System.arraycopy(remapTempData, 0, llTempData, 0,
                                    temperatureHeight * temperatureWidth * 2);
                        }
                        if (llTempData == null){
                            continue;
                        }else {
                            tempArray = llTempData;
                            irtemp.setTempData(llTempData);
                        }
                    }catch (Exception e){
                        Log.d(TAG, "remapTempData != NULL"+e.getMessage());
                        continue;
                    }
                }else {
                    try {
                        synchronized (syncimage.dataLock) {
                            // 用来关联温度数据和TemperatureView,方便后面的点线框测温
                            irtemp.setTempData(temperature);
                            if (syncimage.type == 1) irtemp.setScale(16);
                        }
                    }catch (Exception e){
                        Log.d(TAG, "syncimage != NULL"+e.getMessage());
                    }
                    tempArray = temperature;
                }
                try {
                    if (iLiteListener != null){
                        iLiteListener.getDeltaNucAndVTemp();
                    }
                    if (isMonitor && (viewWidth != getMeasuredWidth() || viewHeight != getMeasuredHeight())){
                        viewWidth = getMeasuredWidth();
                        xScale = (float) viewWidth / (float) temperatureWidth;
                        viewHeight = getMeasuredHeight();
                        yScale = (float) viewHeight / (float) temperatureHeight;
                    }
                    LibIRTemp.TemperatureSampleResult temperatureSampleResult = irtemp.getTemperatureOfRect(new Rect(0, 0, temperatureWidth / 2, temperatureHeight - 1));
                    // 点线框
                    if (regionAndValueBitmap != null) {
                        synchronized (regionLock) {
                            Canvas canvas = new Canvas(regionAndValueBitmap);
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            canvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                            // 获取最高温和最低温的数据
                            float fullMaxTemp;
                            float fullMinTemp;
                            LibIRTemp.TemperatureSampleResult fullResult = irtemp.getTemperatureOfRect(new Rect(0, 0, temperatureWidth - 1, temperatureHeight - 1));
                            fullMaxTemp = getTSTemp(fullResult.maxTemperature);
                            fullMinTemp = getTSTemp(fullResult.minTemperature);
                            if (listener != null) {
                                listener.getTemp((int) (fullMaxTemp * 100) / 100f, (int) (fullMinTemp * 100) / 100f, temperature);
                            }

                            // 最低温
                            if (isShowFull) {
                                String minTem = UnitTools.showC(fullMinTemp, isShowC);
                                int x = TempDrawHelper.Companion.correct(fullResult.minTemperaturePixel.x * xScale, getWidth());
                                int y = TempDrawHelper.Companion.correct(fullResult.minTemperaturePixel.y * yScale, getHeight());
                                drawCircle(canvas, x, y, false);
                                drawTempText(canvas, minTem, x, y);
                            }
                            if (isUserLowTemp) {
                                int x = TempDrawHelper.Companion.correctPoint(fullResult.minTemperaturePixel.x * xScale, getWidth());
                                int y = TempDrawHelper.Companion.correctPoint(fullResult.minTemperaturePixel.y * yScale, getHeight());
                                drawPoint(canvas, x, y);
                                drawCircle(canvas, x, y, false);
                            }

                            // 最高温
                            if (isShowFull) {
                                String maxTem = UnitTools.showC(fullMaxTemp, isShowC);
                                int x = TempDrawHelper.Companion.correct(fullResult.maxTemperaturePixel.x * xScale, getWidth());
                                int y = TempDrawHelper.Companion.correct(fullResult.maxTemperaturePixel.y * yScale, getHeight());
                                drawCircle(canvas, x, y, true);
                                drawTempText(canvas, maxTem, x, y);
                            }
                            if (isUserHighTemp) {
                                int x = TempDrawHelper.Companion.correctPoint(fullResult.maxTemperaturePixel.x * xScale, getWidth());
                                int y = TempDrawHelper.Companion.correctPoint(fullResult.maxTemperaturePixel.y * yScale, getHeight());
                                drawPoint(canvas, x, y);
                                drawCircle(canvas, x, y, true);
                            }

                            //趋势图
                            Line trendLine = this.trendLine;
                            if (trendLine != null) {
                                int startX = (int) (trendLine.start.x / xScale);
                                int startY = (int) (trendLine.start.y / yScale);
                                int endX = (int) (trendLine.end.x / xScale);
                                int endY = (int) (trendLine.end.y / yScale);
                                int minX = Math.min(startX, endX);
                                int maxX = Math.max(startX, endX);
                                int minY = Math.min(startY, endY);
                                int maxY = Math.max(startY, endY);
                                if (maxX < temperatureWidth && minX > 0 && maxY < temperatureHeight && minY > 0) {
                                    temperatureSampleResult = irtemp.getTemperatureOfLine(new Line(new Point(startX, startY), new Point(endX, endY)));
                                    String min = UnitTools.showC(getTSTemp(temperatureSampleResult.minTemperature), isShowC);
                                    String max = UnitTools.showC(getTSTemp(temperatureSampleResult.maxTemperature), isShowC);
                                    drawDot(canvas, temperatureSampleResult.minTemperaturePixel, false);
                                    drawTempText(canvas, min, temperatureSampleResult.minTemperaturePixel);
                                    drawDot(canvas, temperatureSampleResult.maxTemperaturePixel, true);
                                    drawTempText(canvas, max, temperatureSampleResult.maxTemperaturePixel);
                                    if (onTrendChangeListener != null) {
                                        List<Float> tempList = TempUtil.INSTANCE.getLineTemps(new Point(startX, startY), new Point(endX, endY), tempArray, temperatureWidth);
                                        onTrendChangeListener.onChange(tempList);
                                    }
                                }
                            }
                            for (int index = 0; index < rectList.size(); index++) {
                                Rect tempRectangle = rectList.get(index);
                                int left = (int) (tempRectangle.left / xScale);
                                int top = (int) (tempRectangle.top / yScale);
                                int right = (int) (tempRectangle.right / xScale);
                                int bottom = (int) (tempRectangle.bottom / yScale);
                                if (right > left && bottom > top && left < temperatureWidth && top < temperatureHeight && right > 0 && bottom > 0) {
                                    int tempLeft = Math.max(left, 0);
                                    int tempTop = Math.max(top, 0);
                                    int tempRight = Math.min(right, temperatureWidth);
                                    int tempBottom = Math.min(bottom, temperatureHeight);
                                    temperatureSampleResult = irtemp.getTemperatureOfRect(new Rect(tempLeft, tempTop, tempRight, tempBottom));
                                    String min = UnitTools.showC(getTSTemp(temperatureSampleResult.minTemperature), isShowC);
                                    String max = UnitTools.showC(getTSTemp(temperatureSampleResult.maxTemperature), isShowC);
                                    drawDot(canvas, temperatureSampleResult.minTemperaturePixel, false);
                                    drawTempText(canvas, min, temperatureSampleResult.minTemperaturePixel);
                                    drawDot(canvas, temperatureSampleResult.maxTemperaturePixel, true);
                                    drawTempText(canvas, max, temperatureSampleResult.maxTemperaturePixel);
                                }
                            }
                            for (Line line : lineList) {
                                int startX = (int) (line.start.x / xScale);
                                int startY = (int) (line.start.y / yScale);
                                int endX = (int) (line.end.x / xScale);
                                int endY = (int) (line.end.y / yScale);
                                int minX = Math.min(startX, endX);
                                int maxX = Math.max(startX, endX);
                                int minY = Math.min(startY, endY);
                                int maxY = Math.max(startY, endY);
                                if (maxX < temperatureWidth && minX > 0 && maxY < temperatureHeight && minY > 0) {
                                    temperatureSampleResult = irtemp.getTemperatureOfLine(new Line(new Point(startX, startY), new Point(endX, endY)));
                                    String min = UnitTools.showC(getTSTemp(temperatureSampleResult.minTemperature), isShowC);
                                    String max = UnitTools.showC(getTSTemp(temperatureSampleResult.maxTemperature), isShowC);
                                    drawDot(canvas, temperatureSampleResult.minTemperaturePixel, false);
                                    drawTempText(canvas, min, temperatureSampleResult.minTemperaturePixel);
                                    drawDot(canvas, temperatureSampleResult.maxTemperaturePixel, true);
                                    drawTempText(canvas, max, temperatureSampleResult.maxTemperaturePixel);
                                }
                            }
                            for (Point point : pointList) {
                                int x = (int) (point.x / xScale);
                                int y = (int) (point.y / yScale);
                                if (x < temperatureWidth && x > 0 && y < temperatureHeight && y > 0) {
                                    temperatureSampleResult = irtemp.getTemperatureOfPoint(new Point(x, y));
                                    String max = UnitTools.showC(getTSTemp(temperatureSampleResult.maxTemperature), isShowC);
                                    drawCircle(canvas, point.x, point.y, true);
                                    drawTempText(canvas, max, point.x, point.y);
                                }
                            }
                            //中心温度
                            if (isShowFull || (!lineList.isEmpty() || !pointList.isEmpty() || !rectList.isEmpty())) {
                                drawPoint(canvas, getWidth() / 2, getHeight() / 2);
                                temperatureSampleResult = irtemp.getTemperatureOfPoint(new Point(temperatureWidth / 2, temperatureHeight / 2));
                                String max = UnitTools.showC(getTSTemp(temperatureSampleResult.maxTemperature), isShowC);
                                drawTempText(canvas, max, temperatureSampleResult.maxTemperaturePixel);
                            }
                        }
                        Canvas surfaceViewCanvas = getHolder().lockCanvas();
                        if (surfaceViewCanvas == null) {
                            SystemClock.sleep(1000);
                            continue;
                        }
                        try {
                            surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                            surfaceViewCanvas.drawBitmap(regionAndValueBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                            getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                        } catch (Exception e) {
                            Log.e(TAG, "temperatureThread:" + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "temperatureError:" + e.getMessage());
                }
                SystemClock.sleep(1000);
            }
            Log.d(TAG, "temperatureThread exit");
        };
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        viewHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();

        xScale = (float) viewWidth / (float) temperatureWidth;
        yScale = (float) viewHeight / (float) temperatureHeight;

        if (regionBitmap == null || regionBitmap.getWidth() != viewWidth || regionBitmap.getHeight() != viewHeight) {
            regionBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        }
        regionAndValueBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
    }





    /* **************************************** Touch **************************************** */
    /**
     * 是否为添加 点线面 模式。<br>
     * true-添加一个新点线面 false-移动一个已有点线面
     */
    private boolean isAddAction = true;


    private int downX = 0;
    private int downY = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (temperatureRegionMode) {
            case REGION_MODE_POINT:
                return handleTouchPoint(event);
            case REGION_MODE_LINE:
                return handleTouchLine(event, false);
            case REGION_MODE_RECTANGLE:
                return handleTouchRect(event);
            case REGION_NODE_TREND:
                return handleTouchLine(event, true);
            default:
                return false;
        }
    }

    /* **************************************** 点 **************************************** */

    private boolean handleTouchPoint(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = TempDrawHelper.Companion.correctPoint(event.getX(), getWidth());
                downY = TempDrawHelper.Companion.correctPoint(event.getY(), getHeight());
                Point point = getPoint(downX, downY);
                if (point == null) {//新增
                    isAddAction = true;
                    if (pointList.size() == POINT_MAX_COUNT) {
                        synchronized (regionLock) {
                            pointList.remove(0);
                        }
                        setBitmap();
                    }
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawPoint(surfaceViewCanvas, downX, downY);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else {//移动或删除
                    isAddAction = false;
                    synchronized (regionLock) {
                        pointList.remove(point);
                    }
                    setBitmap();
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawPoint(surfaceViewCanvas, point.x, point.y);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                int x = TempDrawHelper.Companion.correctPoint(event.getX(), getWidth());
                int y = TempDrawHelper.Companion.correctPoint(event.getY(), getHeight());
                Canvas surfaceViewCanvas = getHolder().lockCanvas();
                surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                drawPoint(surfaceViewCanvas, x, y);
                getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                int x = TempDrawHelper.Companion.correctPoint(event.getX(), getWidth());
                int y = TempDrawHelper.Companion.correctPoint(event.getY(), getHeight());
                if (isAddAction) {
                    synchronized (regionLock) {
                        if (pointList.size() == POINT_MAX_COUNT) {
                            pointList.remove(0);
                        }
                        pointList.add(new Point(x, y));
                    }
                } else {
                    if (Math.abs(x - downX) > TOUCH_TOLERANCE || Math.abs(y - downY) > TOUCH_TOLERANCE) {
                        synchronized (regionLock) {
                            if (pointList.size() == POINT_MAX_COUNT) {
                                pointList.remove(0);
                            }
                            pointList.add(new Point(x, y));
                        }
                    }
                }
                setBitmap();
                Canvas surfaceViewCanvas = getHolder().lockCanvas();
                surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                return true;
            }
            default:
                return false;
        }
    }

    @Nullable
    private Point getPoint(int x, int y) {
        for (int i = pointList.size() - 1; i >= 0; i--) {
            Point point = pointList.get(i);
            if (point.x > x - TOUCH_TOLERANCE && point.x < x + TOUCH_TOLERANCE && point.y > y - TOUCH_TOLERANCE && point.y < y + TOUCH_TOLERANCE) {
                return point;
            }
        }
        return null;
    }


    /* **************************************** 线 **************************************** */

    private Line movingLine;

    private enum LineMoveType { ALL, START, END }
    /**
     * 线移动方式：整体移动、仅变更头、仅变更尾。
     */
    private LineMoveType lineMoveType = LineMoveType.ALL;

    private boolean handleTouchLine(MotionEvent event, boolean isTrend) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = TempDrawHelper.Companion.correct(event.getX(), getWidth());
                downY = TempDrawHelper.Companion.correct(event.getY(), getHeight());
                Line line = getLine(downX, downY, isTrend);
                if (line == null) {
                    isAddAction = true;
                } else {
                    isAddAction = false;
                    movingLine = line;
                    if (downX > line.start.x - TOUCH_TOLERANCE && downX < line.start.x + TOUCH_TOLERANCE && downY > line.start.y - TOUCH_TOLERANCE && downY < line.start.y + TOUCH_TOLERANCE) {
                        lineMoveType = LineMoveType.START;
                    } else if (downX > line.end.x - TOUCH_TOLERANCE && downX < line.end.x + TOUCH_TOLERANCE && downY > line.end.y - TOUCH_TOLERANCE && downY < line.end.y + TOUCH_TOLERANCE) {
                        lineMoveType = LineMoveType.END;
                    } else {
                        lineMoveType = LineMoveType.ALL;
                    }
                    if (isTrend) {
                        synchronized (regionLock) {
                            trendLine = null; //手势操作过程中不需要绘制温度，置为 null
                        }
                        if (onTrendRemoveListener != null) {
                            onTrendRemoveListener.run();
                        }
                    } else {
                        synchronized (regionLock) {
                            // 真是醉了，Line 没有重写 equals 方法，不过好在这个 line 本来就是从 lineList 里取出来的，所以 remove 没问题
                            lineList.remove(line);
                        }
                    }
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    setBitmap();
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawLine(surfaceViewCanvas, line.start.x, line.start.y, line.end.x, line.end.y, isTrend);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                int x = TempDrawHelper.Companion.correct(event.getX(), getWidth());
                int y = TempDrawHelper.Companion.correct(event.getY(), getHeight());
                if (isAddAction) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawLine(surfaceViewCanvas, downX, downY, x, y, isTrend);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);

                    Point start = new Point();
                    Point end = new Point();
                    switch (lineMoveType) {
                        case ALL:
                            Rect rect = TempDrawHelper.Companion.getRect(getWidth(), getHeight());
                            int minX = Math.min(movingLine.start.x, movingLine.end.x);
                            int maxX = Math.max(movingLine.start.x, movingLine.end.x);
                            int minY = Math.min(movingLine.start.y, movingLine.end.y);
                            int maxY = Math.max(movingLine.start.y, movingLine.end.y);
                            int biasX = x < downX ? Math.max(x - downX, rect.left - minX) : Math.min(x - downX, rect.right - maxX);
                            int biasY = y < downY ? Math.max(y - downY, rect.top - minY) : Math.min(y - downY, rect.bottom - maxY);
                            start = new Point(movingLine.start.x + biasX, movingLine.start.y + biasY);
                            end = new Point(movingLine.end.x + biasX, movingLine.end.y + biasY);
                            break;
                        case START:
                            start = new Point(x, y);
                            end = movingLine.end;
                            break;
                        case END:
                            start = movingLine.start;
                            end = new Point(x, y);
                            break;
                    }
                    drawLine(surfaceViewCanvas, start.x, start.y, end.x, end.y, isTrend);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                int x = TempDrawHelper.Companion.correct(event.getX(), getWidth());
                int y = TempDrawHelper.Companion.correct(event.getY(), getHeight());
                if (isAddAction) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    if (Math.abs(x - downX) > TOUCH_TOLERANCE || Math.abs(y - downY) > TOUCH_TOLERANCE) {
                        if (isTrend) {
                            synchronized (regionLock) {
                                trendLine = new Line(new Point(downX, downY), new Point(x, y));
                            }
                            if (onTrendAddListener != null) {
                                onTrendAddListener.run();
                            }
                        } else {
                            synchronized (regionLock) {
                                if (lineList.size() == LINE_MAX_COUNT) {
                                    lineList.remove(0);
                                }
                                lineList.add(new Line(new Point(downX, downY), new Point(x, y)));
                            }
                        }
                        setBitmap();
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    Canvas bitmapCanvas = new Canvas(regionBitmap);

                    // TODO: 2024/12/13 这里有历史遗留问题，拖动的时候可以把直线拖成点
                    if (Math.abs(x - downX) > TOUCH_TOLERANCE || Math.abs(y - downY) > TOUCH_TOLERANCE) {
                        Point start = new Point();
                        Point end = new Point();
                        switch (lineMoveType) {
                            case ALL:
                                Rect rect = TempDrawHelper.Companion.getRect(getWidth(), getHeight());
                                int minX = Math.min(movingLine.start.x, movingLine.end.x);
                                int maxX = Math.max(movingLine.start.x, movingLine.end.x);
                                int minY = Math.min(movingLine.start.y, movingLine.end.y);
                                int maxY = Math.max(movingLine.start.y, movingLine.end.y);
                                int biasX = x < downX ? Math.max(x - downX, rect.left - minX) : Math.min(x - downX, rect.right - maxX);
                                int biasY = y < downY ? Math.max(y - downY, rect.top - minY) : Math.min(y - downY, rect.bottom - maxY);
                                start = new Point(movingLine.start.x + biasX, movingLine.start.y + biasY);
                                end = new Point(movingLine.end.x + biasX, movingLine.end.y + biasY);
                                break;
                            case START:
                                start = new Point(x, y);
                                end = movingLine.end;
                                break;
                            case END:
                                start = movingLine.start;
                                end = new Point(x, y);
                                break;
                        }
                        drawLine(bitmapCanvas, start.x, start.y, end.x, end.y, isTrend);

                        if (isTrend) {
                            synchronized (regionLock) {
                                trendLine = new Line(start, end);
                            }
                            if (onTrendAddListener != null) {
                                onTrendAddListener.run();
                            }
                        } else {
                            synchronized (regionLock) {
                                if (lineList.size() == LINE_MAX_COUNT) {
                                    lineList.remove(0);
                                }
                                lineList.add(new Line(start, end));
                            }
                        }
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            }
            default:
                return false;
        }
    }

    /**
     * 指定坐标 (x, y) 是否视为指定 Line 的选中.
     */
    private static boolean isLineConcat(@NonNull Line line, int x, int y) {
        int tempDistance = ((line.end.y - line.start.y) * x - (line.end.x - line.start.x) * y + line.end.x * line.start.y - line.start.x * line.end.y);
        tempDistance = (int) (tempDistance / Math.sqrt(Math.pow(line.end.y - line.start.y, 2) + Math.pow(line.end.x - line.start.x, 2)));
        return Math.abs(tempDistance) < TOUCH_TOLERANCE && x > Math.min(line.start.x, line.end.x) - TOUCH_TOLERANCE && x < Math.max(line.start.x, line.end.x) + TOUCH_TOLERANCE;
    }

    @Nullable
    private Line getLine(int x, int y, boolean isTrend) {
        if (isTrend) {
            if (trendLine != null && isLineConcat(trendLine, x, y)) {
                return trendLine;
            }
        } else {
            for (int i = lineList.size() - 1; i >= 0; i--) {
                Line line = lineList.get(i);
                if (isLineConcat(line, x, y)) {
                    return line;
                }
            }
        }
        return null;
    }


    /* **************************************** 面 **************************************** */
    private Rect movingRect;

    
    private enum RectMoveType { ALL, EDGE, CORNER }
    /**
     * 面移动方式：点击面内部-整体移动、点击面4条边-边移动、点击面4个角-角移动。
     */
    private RectMoveType rectMoveType = RectMoveType.ALL;


    private enum RectMoveEdge { LEFT, TOP, RIGHT, BOTTOM }
    /**
     * 仅边移动模式时，移动的是哪条边.
     */
    private RectMoveEdge rectMoveEdge = RectMoveEdge.LEFT;
    
    
    private enum RectMoveCorner { LT, RT, RB, LB }
    /**
     * 仅角移动模式时，移动的是哪个角.
     */
    private RectMoveCorner rectMoveCorner = RectMoveCorner.LT;


    private boolean handleTouchRect(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = TempDrawHelper.Companion.correct(event.getX(), getWidth());
                downY = TempDrawHelper.Companion.correct(event.getY(), getHeight());
                Rect rect = getRect(downX, downY);
                if (rect == null) {
                    isAddAction = true;
                } else {
                    isAddAction = false;
                    movingRect = rect;

                    if (isIn(downX, rect.left)) {//选中最左那条边
                        if (isIn(downY, rect.top)) {
                            rectMoveType = RectMoveType.CORNER;
                            rectMoveCorner = RectMoveCorner.LT;
                        } else if (isIn(downY, rect.bottom)) {
                            rectMoveType = RectMoveType.CORNER;
                            rectMoveCorner = RectMoveCorner.LB;
                        } else {
                            rectMoveType = RectMoveType.EDGE;
                            rectMoveEdge = RectMoveEdge.LEFT;
                        }
                    } else if (isIn(downX, rect.right)) {//选中最右那条边
                        if (isIn(downY, rect.top)) {
                            rectMoveType = RectMoveType.CORNER;
                            rectMoveCorner = RectMoveCorner.RT;
                        } else if (isIn(downY, rect.bottom)) {
                            rectMoveType = RectMoveType.CORNER;
                            rectMoveCorner = RectMoveCorner.RB;
                        } else {
                            rectMoveType = RectMoveType.EDGE;
                            rectMoveEdge = RectMoveEdge.RIGHT;
                        }
                    } else if (isIn(downY, rect.top)) {//选中顶边
                        rectMoveType = RectMoveType.EDGE;
                        rectMoveEdge = RectMoveEdge.TOP;
                    } else if (isIn(downY, rect.bottom)) {//选中底边
                        rectMoveType = RectMoveType.EDGE;
                        rectMoveEdge = RectMoveEdge.BOTTOM;
                    } else {
                        rectMoveType = RectMoveType.ALL;
                    }
                    synchronized (regionLock) {
                        rectList.remove(rect);
                    }
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    setBitmap();
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawRect(surfaceViewCanvas, rect.left, rect.top, rect.right, rect.bottom);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                int x = TempDrawHelper.Companion.correct(event.getX(), getWidth());
                int y = TempDrawHelper.Companion.correct(event.getY(), getHeight());
                if (isAddAction) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawRect(surfaceViewCanvas, downX, downY, x, y);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    switch (rectMoveType) {
                        case ALL:
                            Rect rect = TempDrawHelper.Companion.getRect(getWidth(), getHeight());
                            int biasX = x < downX ? Math.max(x - downX, rect.left - movingRect.left) : Math.min(x - downX, rect.right - movingRect.right);
                            int biasY = y < downY ? Math.max(y - downY, rect.top - movingRect.top) : Math.min(y - downY, rect.bottom - movingRect.bottom);
                            drawRect(surfaceViewCanvas, movingRect.left + biasX, movingRect.top + biasY, movingRect.right + biasX, movingRect.bottom + biasY);
                            break;
                        case EDGE:
                            switch (rectMoveEdge) {
                                case LEFT:
                                    drawRect(surfaceViewCanvas, x, movingRect.top, movingRect.right, movingRect.bottom);
                                    break;
                                case TOP:
                                    drawRect(surfaceViewCanvas, movingRect.left, y, movingRect.right, movingRect.bottom);
                                    break;
                                case RIGHT:
                                    drawRect(surfaceViewCanvas, movingRect.left, movingRect.top, x, movingRect.bottom);
                                    break;
                                case BOTTOM:
                                    drawRect(surfaceViewCanvas, movingRect.left, movingRect.top, movingRect.right, y);
                                    break;
                            }
                            break;
                        case CORNER:
                            switch (rectMoveCorner) {
                                case LT:
                                    drawRect(surfaceViewCanvas, x, y, movingRect.right, movingRect.bottom);
                                    break;
                                case LB:
                                    drawRect(surfaceViewCanvas, x, movingRect.top, movingRect.right, y);
                                    break;
                                case RT:
                                    drawRect(surfaceViewCanvas, movingRect.left, y, x, movingRect.bottom);
                                    break;
                                case RB:
                                    drawRect(surfaceViewCanvas, movingRect.left, movingRect.top, x, y);
                                    break;
                            }
                            break;
                    }
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                int x = TempDrawHelper.Companion.correct(event.getX(), getWidth());
                int y = TempDrawHelper.Companion.correct(event.getY(), getHeight());
                if (isAddAction) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    if (Math.abs(x - downX) > TOUCH_TOLERANCE || Math.abs(y - downY) > TOUCH_TOLERANCE) {
                        synchronized (regionLock) {
                            if (rectList.size() == RECTANGLE_MAX_COUNT) {
                                rectList.remove(0);
                            }
                            rectList.add(new Rect(Math.min(downX, x), Math.min(downY, y), Math.max(downX, x), Math.max(downY, y)));
                        }
                        setBitmap();
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    Canvas bitmapCanvas = new Canvas(regionBitmap);
                    // TODO: 2024/12/13 这里有历史遗留问题，拖动的时候可以把矩形拖成直线
                    if (Math.abs(x - downX) > TOUCH_TOLERANCE || Math.abs(y - downY) > TOUCH_TOLERANCE) {
                        switch (rectMoveType) {
                            case ALL:
                                Rect rect = TempDrawHelper.Companion.getRect(getWidth(), getHeight());
                                int biasX = x < downX ? Math.max(x - downX, rect.left - movingRect.left) : Math.min(x - downX, rect.right - movingRect.right);
                                int biasY = y < downY ? Math.max(y - downY, rect.top - movingRect.top) : Math.min(y - downY, rect.bottom - movingRect.bottom);
                                movingRect.offset(biasX, biasY);
                                break;
                            case EDGE:
                                switch (rectMoveEdge) {
                                    case LEFT:
                                        movingRect.left = Math.min(x, movingRect.right);
                                        movingRect.right = Math.max(x, movingRect.right);
                                        break;
                                    case TOP:
                                        movingRect.top = Math.min(y, movingRect.bottom);
                                        movingRect.bottom = Math.max(y, movingRect.bottom);
                                        break;
                                    case RIGHT:
                                        movingRect.right = Math.max(x, movingRect.left);
                                        movingRect.left = Math.min(x, movingRect.left);
                                        break;
                                    case BOTTOM:
                                        movingRect.bottom = Math.max(y, movingRect.top);
                                        movingRect.top = Math.min(y, movingRect.top);
                                        break;
                                }
                                break;
                            case CORNER:
                                switch (rectMoveCorner) {
                                    case LT:
                                        movingRect.left = Math.min(x, movingRect.right);
                                        movingRect.right = Math.max(x, movingRect.right);
                                        movingRect.top = Math.min(y, movingRect.bottom);
                                        movingRect.bottom = Math.max(y, movingRect.bottom);
                                        break;
                                    case RT:
                                        movingRect.right = Math.max(x, movingRect.left);
                                        movingRect.left = Math.min(x, movingRect.left);
                                        movingRect.top = Math.min(y, movingRect.bottom);
                                        movingRect.bottom = Math.max(y, movingRect.bottom);
                                        break;
                                    case RB:
                                        movingRect.right = Math.max(x, movingRect.left);
                                        movingRect.left = Math.min(x, movingRect.left);
                                        movingRect.bottom = Math.max(y, movingRect.top);
                                        movingRect.top = Math.min(y, movingRect.top);
                                        break;
                                    case LB:
                                        movingRect.left = Math.min(x, movingRect.right);
                                        movingRect.right = Math.max(x, movingRect.right);
                                        movingRect.bottom = Math.max(y, movingRect.top);
                                        movingRect.top = Math.min(y, movingRect.top);
                                        break;
                                }
                                break;
                        }

                        drawRect(bitmapCanvas, movingRect.left, movingRect.top, movingRect.right, movingRect.bottom);
                        synchronized (regionLock) {
                            if (rectList.size() == RECTANGLE_MAX_COUNT) {
                                rectList.remove(0);
                            }
                            rectList.add(movingRect);
                        }
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            }
            default:
                return false;
        }
    }

    @Nullable
    private Rect getRect(int x, int y) {
        for (int i = rectList.size() - 1; i >= 0; i--) {
            Rect rect = rectList.get(i);
            if (x > rect.left - TOUCH_TOLERANCE && x < rect.right + TOUCH_TOLERANCE
                    && y > rect.top - TOUCH_TOLERANCE && y < rect.bottom + TOUCH_TOLERANCE) {
                return rect;
            }
        }
        return null;
    }

    private boolean isIn(int a, int b) {
        return a > b - TOUCH_TOLERANCE && a < b + TOUCH_TOLERANCE;
    }




    /* **************************************** Draw **************************************** */

    /**
     * 以 View 尺寸为坐标系，在 (x,y) 画一个十字.<br>
     * 注意，不对 x、y 进行处理，传进来是哪就在哪绘制。
     */
    private void drawPoint(Canvas canvas, int x, int y) {
        helper.drawPoint(canvas, x, y);
    }

    /**
     * 绘制以 View 尺寸为坐标的一根线段，这里的 x,y 为 View 坐标原始值
     */
    private void drawLine(Canvas canvas, int x1, int y1, int x2, int y2, boolean isTrend) {
        // 由于线段与实心点的的绘制是分开的，线段使用当前 View 坐标，而实心点使用温度(192x256)坐标转换为 View 坐标
        // 故而这里需要把当前的坐标，尽量贴近温度坐标的整数倍，否则会出现实心圆偏离直线太远的情况
        int startX = (int) ((int) (x1 / xScale) * xScale);
        int startY = (int) ((int) (y1 / yScale) * yScale);
        int stopX = (int) ((int) (x2 / xScale) * xScale);
        int stopY = (int) ((int) (y2 / yScale) * yScale);
        helper.drawLine(canvas, startX, startY, stopX, stopY);

        if (isTrend) {
            helper.drawTrendText(canvas, getWidth(), getHeight(), startX, startY, stopX, stopY);
        }
    }

    /**
     * 绘制以 View 尺寸为坐标的一根线段，这里的 x,y 为 View 坐标原始值
     */
    private void drawRect(Canvas canvas, float x1, float y1, float x2, float y2) {
        int left = (int) ((int) (x1 / xScale) * xScale);
        int top = (int) ((int) (y1 / yScale) * yScale);
        int right = (int) ((int) (x2 / xScale) * xScale);
        int bottom = (int) ((int) (y2 / yScale) * yScale);
        helper.drawRect(canvas, left, top, right, bottom);
    }

    /**
     * 以 View 尺寸为坐标系，在 (x,y) 画一个实心圆.
     * @param isMax true-最高温红色 false-最低温蓝色
     */
    private void drawCircle(Canvas canvas, int x, int y, boolean isMax) {
        helper.drawCircle(canvas, x, y, isMax);
    }

    /**
     * 在指定 canvas 上，以指定 point 坐标为中心，绘制一个实心圆.
     * @param point 以温度尺寸(192x256)为坐标系的点
     * @param isMax true-最高温红色 false-最低温蓝色
     */
    private void drawDot(Canvas canvas, Point point, boolean isMax) {
        //这里的 (x,y) 是通过温度坐标转换来的，所以已经是温度坐标的整数倍
        int x = TempDrawHelper.Companion.correct(point.x * xScale, getWidth());
        int y = TempDrawHelper.Companion.correct(point.y * yScale, getHeight());
        helper.drawCircle(canvas, x, y, isMax);
    }


    /**
     * 以 View 尺寸为坐标系，以 (x,y) 为基准，绘制温度值文字.
     */
    private void drawTempText(Canvas canvas, String text, int x, int y) {
        helper.drawTempText(canvas, text, getWidth(), x, y);
    }
    /**
     * 在指定 canvas 上，以指定 point 坐标为中心，绘制指定的文字.
     * @param point 以温度尺寸(192x256)为坐标系的点
     */
    private void drawTempText(Canvas canvas, String text, Point point) {
        int x = TempDrawHelper.Companion.correct(point.x * xScale, getWidth());
        int y = TempDrawHelper.Companion.correct(point.y * yScale, getHeight());
        helper.drawTempText(canvas, text, getWidth(), x, y);
    }



    private void setBitmap() {
        regionBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(regionBitmap);
        for (Point point : pointList) {
            drawPoint(canvas, point.x, point.y);
        }
        for (Line line : lineList) {
            drawLine(canvas, line.start.x, line.start.y, line.end.x, line.end.y, false);
        }
        for (Rect rect : rectList) {
            drawRect(canvas, rect.left, rect.top, rect.right, rect.bottom);
        }
        if (trendLine != null) {
            drawLine(canvas, trendLine.start.x, trendLine.start.y, trendLine.end.x, trendLine.end.y, true);
        }
    }


    /**
     * 趋势图对应的温度数据变更监听。
     * 注意！回调不在主线程！！
     */
    public interface OnTrendChangeListener {
        void onChange(List<Float> temps);
    }

    public interface TempListener {
        void getTemp(float max, float min, byte[] tempData);
    }

    public float getCompensateTemp(float temp){
        if (iLiteListener != null){
            return iLiteListener.compensateTemp(temp);
        }else {
            return temp;
        }
    }

    public float getTSTemp(float temp) {
        if (iTsTempListenerWeakReference != null && iTsTempListenerWeakReference.get() != null) {
            return iTsTempListenerWeakReference.get().tempCorrectByTs(getCompensateTemp(temp));
        } else {
            return getCompensateTemp(temp);
        }
    }

    /**
     *  ----------------------双光设备--------------------------------
     */

    public void setUseIRISP(boolean useIRISP) {
        if (irtemp != null) {
            irtemp.setScale(useIRISP ? 16 : 64);
        }
    }

    public void setCurrentFusionType(@NonNull DualCameraParams.FusionType currentFusionType) {
        this.mCurrentFusionType = currentFusionType;
    }

    public void setDualUVCCamera(@NonNull DualUVCCamera dualUVCCamera) {
        this.dualUVCCamera = dualUVCCamera;

    }
    private DualCameraParams.FusionType mCurrentFusionType;
    private byte[] remapTempData;
    private DualUVCCamera dualUVCCamera;
    private byte[] llTempData;

    @Override
    public void onFame(byte[] mixData, byte[] tempData, double fpsText) {
        if (Const.TYPE_IR_DUAL == productType){
            if (mCurrentFusionType == DualCameraParams.FusionType.IROnlyNoFusion) {
                if (this.remapTempData == null) {
                    this.remapTempData = new byte[Const.IR_WIDTH * Const.IR_HEIGHT * 2];
                }
                System.arraycopy(tempData, 0, this.remapTempData, 0, Const.IR_WIDTH * Const.IR_HEIGHT * 2);
            } else {
                if (this.remapTempData == null) {
                    this.remapTempData = new byte[Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 2];
                }
                System.arraycopy(tempData, 0, this.remapTempData, 0, Const.DUAL_WIDTH * Const.DUAL_HEIGHT * 2);
            }
        }
    }
}