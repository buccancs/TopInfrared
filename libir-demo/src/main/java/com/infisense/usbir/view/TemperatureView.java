package com.infisense.usbir.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

import com.blankj.utilcode.util.SizeUtils;
import com.infisense.iruvc.ircmd.IRCMD;
import com.infisense.iruvc.sdkisp.LibIRTemp;
import com.infisense.iruvc.utils.Line;
import com.infisense.iruvc.utils.SynchronizedBitmap;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class TemperatureView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private final String TAG = "TemperatureView";
    private final int LINE_STROKE_WIDTH = SizeUtils.dp2px(1f);
    private final int DOT_STROKE_WIDTH = SizeUtils.dp2px(1f);
    private final int DOT_RADIUS = SizeUtils.dp2px(3f);
    private final int POINT_SIZE = SizeUtils.sp2px(8f);
    private final int TEXT_SIZE = SizeUtils.sp2px(12f);

    private final int TOUCH_TOLERANCE = SizeUtils.sp2px(8f);

    private final int POINT_MAX_COUNT = 3;
    private final int LINE_MAX_COUNT = 3;
    private final int RECTANGLE_MAX_COUNT = 3;

    private Runnable runnable;
    public Thread temperatureThread;
    private LibIRTemp irtemp;
    private IRCMD ircmd;
    private float minTemperature;
    private float maxTemperature;
    private String RectMinTemp, RectMaxTemp;

    private float xscale = 0;
    private float yscale = 0;
    private int viewWidth = 0;
    private int viewHeight = 0;
    private Bitmap regionBitmap;
    private Bitmap regionAndValueBitmap;
    private Object regionLock = new Object();
    private Paint linePaint;
    private Paint bluePaint;
    private Paint redPaint;
    private Paint whitePaint;
    private Paint maxPaint;
    private Paint minPaint;

    private int actionMode;
    private static final int ACTION_MODE_INSERT = 0;
    private static final int ACTION_MODE_MOVE = 1;

    private float startX, startY, endX, endY;

    public static int REGION_MODE_POINT = 0;
    public static int REGION_MODE_LINE = 1;
    public static int REGION_MODE_RECTANGLE = 2;
    public static int REGION_MODE_CENTER = 3;
    public static int REGION_MODE_CLEAN = 4;
    
    private ArrayList<Point> points = new ArrayList<Point>();
    private Point movingPoint;
    
    private ArrayList<Line> lines = new ArrayList<Line>();
    private Line movingLine;
    private int lineMoveType;
    private static final int LINE_MOVE_ENTIRE = 0;
    private static final int LINE_MOVE_POINT = 1;
    private int lineMovePoint;
    private static final int LINE_START = 0;
    private static final int LINE_END = 1;

    private ArrayList<Rect> rectangles = new ArrayList<Rect>();

    private Rect movingRectangle;
    private int rectangleMoveType;
    private static final int RECTANGLE_MOVE_ENTIRE = 0;
    private static final int RECTANGLE_MOVE_EDGE = 1;
    private static final int RECTANGLE_MOVE_CORNER = 2;
    private int rectangleMoveEdge;
    private static final int RECTANGLE_LEFT_EDGE = 0;
    private static final int RECTANGLE_TOP_EDGE = 1;
    private static final int RECTANGLE_RIGHT_EDGE = 2;
    private static final int RECTANGLE_BOTTOM_EDGE = 3;
    private int rectangleMoveCorner;
    private static final int RECTANGLE_LEFT_TOP_CORNER = 0;
    private static final int RECTANGLE_RIGHT_TOP_CORNER = 1;
    private static final int RECTANGLE_RIGHT_BOTTOM_CORNER = 2;
    private static final int RECTANGLE_LEFT_BOTTOM_CORNER = 3;
    private int imageWidth;
    private int imageHeight;
    private SynchronizedBitmap syncimage;
    private int temperatureRegionMode;
    private boolean runflag = true;

    private final static int PIXCOUNT = 5;
    private boolean isUseIRISP = true;
    private byte[] temperature;

    public LibIRTemp.TemperatureSampleResult centerResultList = null;
    public ArrayList<LibIRTemp.TemperatureSampleResult> pointResultList = new ArrayList<>(3);
    public ArrayList<LibIRTemp.TemperatureSampleResult> lineResultList = new ArrayList<>(3);
    public ArrayList<LibIRTemp.TemperatureSampleResult> rectangleResultList = new ArrayList<>(3);

    public LibIRTemp.TemperatureSampleResult getV() {
        LibIRTemp.TemperatureSampleResult result = irtemp.getTemperatureOfLine(new Line(new Point(50, 100), new Point(100, 50)));
        Log.w("123", "data tempDataRes_t:" + result.maxTemperature + ", h:" + result.minTemperature);
        return result;
    }

    public void setImageSize(int imageWidth, int imageHeight) {
        Log.w("123", "imageWidth: " + imageWidth + ", imageHeight: " + imageHeight);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        if (viewWidth != 0)
            xscale = (float) viewWidth / (float) imageWidth;
        if (viewHeight != 0)
            yscale = (float) viewHeight / (float) imageHeight;
        irtemp = new LibIRTemp(imageWidth, imageHeight);

        centerResultList = irtemp.new TemperatureSampleResult();
        pointResultList.clear();
        lineResultList.clear();
        rectangleResultList.clear();
        for (int i = 0; i < 3; i++) {
            pointResultList.add(irtemp.new TemperatureSampleResult());
            lineResultList.add(irtemp.new TemperatureSampleResult());
            rectangleResultList.add(irtemp.new TemperatureSampleResult());
        }
    }

    public void setIrcmd(IRCMD ircmd) {
        this.ircmd = ircmd;
    }

    public void setSyncimage(SynchronizedBitmap syncimage) {
        this.syncimage = syncimage;
    }

    public void setTemperatureRegionMode(int temperatureRegionMode) {
        this.temperatureRegionMode = temperatureRegionMode;
    }

    public void setTemperature(byte[] temperature) {
        this.temperature = temperature;
    }

    public TemperatureView(final Context context) {
        this(context, null, 0);
    }

    public TemperatureView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setUseIRISP(boolean useIRISP) {
        isUseIRISP = useIRISP;
        if (isUseIRISP) {
            if (irtemp != null) {
                irtemp.setScale(16);
            }
        } else {
            if (irtemp != null) {
                irtemp.setScale(64);
            }
        }
    }

    public TemperatureView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        runnable = () -> {
            int length = imageWidth * imageHeight * 2;

            linePaint = new Paint();
            linePaint.setStrokeWidth(LINE_STROKE_WIDTH);
            linePaint.setColor(Color.WHITE);

            bluePaint = new Paint();
            bluePaint.setStrokeWidth(DOT_STROKE_WIDTH);
            bluePaint.setStyle(Paint.Style.STROKE);
            bluePaint.setTextSize(TEXT_SIZE);
            bluePaint.setColor(Color.BLUE);

            redPaint = new Paint();
            redPaint.setStrokeWidth(DOT_STROKE_WIDTH);
            redPaint.setStyle(Paint.Style.STROKE);
            redPaint.setTextSize(TEXT_SIZE);
            redPaint.setColor(Color.RED);

            whitePaint = new Paint();
            whitePaint.setStrokeWidth(DOT_STROKE_WIDTH);
            whitePaint.setStyle(Paint.Style.STROKE);
            whitePaint.setTextSize(TEXT_SIZE);
            whitePaint.setColor(Color.WHITE);

            maxPaint = new Paint();
            maxPaint.setTextSize(TEXT_SIZE);
            maxPaint.setColor(Color.WHITE);

            minPaint = new Paint();
            minPaint.setTextSize(TEXT_SIZE);
            minPaint.setColor(Color.WHITE);

            while (!temperatureThread.isInterrupted() && runflag) {

                synchronized (syncimage.dataLock) {
                    irtemp.setTempData(temperature);
                    if (syncimage.type == 1) irtemp.setScale(16);
                }
                LibIRTemp.TemperatureSampleResult temperatureSampleResult = irtemp.getTemperatureOfRect(new Rect(0, 0, imageWidth / 2, imageHeight - 1));
                maxTemperature = temperatureSampleResult.maxTemperature;
                minTemperature = temperatureSampleResult.minTemperature;

                if (rectangles.size() != 0 || lines.size() != 0 || points.size() != 0 || temperatureRegionMode == REGION_MODE_CENTER) {
                    synchronized (regionLock) {
                        int moveX = SizeUtils.dp2px(8);
                        Canvas canvas = new Canvas(regionAndValueBitmap);
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        canvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                        LibIRTemp.TemperatureSampleResult temperatureSampleEasyResult = irtemp.getTemperatureOfRect(new Rect(0, 0, imageWidth - 1, imageHeight - 1));
                        float maxTemperatureTem;
                        float minTemertureTem;
                        maxTemperatureTem = temperatureSampleEasyResult.maxTemperature;
                        minTemertureTem = temperatureSampleEasyResult.minTemperature;

                        float minX0 = temperatureSampleEasyResult.minTemperaturePixel.x * xscale;
                        float minY0 = temperatureSampleEasyResult.minTemperaturePixel.y * yscale;
                        String minTem = new DecimalFormat("0.0").format(minTemertureTem);
                        if (minX0 <= 0 && minY0 <= 0) {
                            minX0 = PIXCOUNT;
                            minY0 = PIXCOUNT;
                        } else if (minX0 <= 0 && (minY0 > 0 && minY0 <= viewHeight)) {
                            minX0 = PIXCOUNT;
                        } else if (minX0 <= 0 && (minY0 >= viewHeight)) {
                            minX0 = PIXCOUNT;
                            minY0 = viewHeight - PIXCOUNT;
                        } else if (minX0 >= viewWidth && minY0 <= 0) {
                            minY0 = PIXCOUNT;
                        } else if (minX0 >= viewWidth && minY0 >= viewHeight) {
                            minX0 = viewWidth - PIXCOUNT;
                            minY0 = viewHeight - PIXCOUNT;
                        } else if ((minX0 > 0 && minX0 <= viewWidth) && minY0 >= viewHeight) {
                            minY0 = viewHeight - PIXCOUNT;
                        } else if ((minX0 > 0 && minX0 <= viewWidth) && minY0 <= 0) {
                            minY0 = PIXCOUNT;
                        } else if (minX0 >= viewWidth && (minY0 > 0 && minY0 <= viewHeight)) {
                            minX0 = viewWidth - PIXCOUNT;
                        }
                        float minTemTextX = minX0;
                        float minTemTextY = minY0;
                        float minTemTextTolerate = 30;
                        if (minX0 <= minTemTextTolerate && minY0 <= minTemTextTolerate) {
                            minTemTextX = minTemTextTolerate;
                            minTemTextY = minTemTextTolerate;
                        } else if (minX0 <= minTemTextTolerate && (minY0 > minTemTextTolerate && minY0 <= viewHeight - minTemTextTolerate)) {
                            minTemTextX = minTemTextTolerate;
                        } else if (minX0 <= minTemTextTolerate && (minY0 >= viewHeight - minTemTextTolerate)) {
                            minTemTextX = minTemTextTolerate;
                            minTemTextY = viewHeight - minTemTextTolerate;
                        } else if (minX0 >= viewWidth - minTemTextTolerate && minY0 <= minTemTextTolerate) {
                            minTemTextX = (float) (viewWidth - minTemTextTolerate * 1.5);
                            minTemTextY = minTemTextTolerate;
                        } else if (minX0 >= viewWidth - minTemTextTolerate && minY0 >= viewHeight - minTemTextTolerate) {
                            minTemTextX = viewWidth - minTemTextTolerate;
                            minTemTextY = viewHeight - minTemTextTolerate;
                        } else if ((minX0 > minTemTextTolerate && minX0 <= viewWidth - minTemTextTolerate) && minY0 >= viewHeight - minTemTextTolerate) {
                            minTemTextY = viewHeight - minTemTextTolerate;
                        } else if ((minX0 > minTemTextTolerate && minX0 <= viewWidth - minTemTextTolerate) && minY0 <= minTemTextTolerate) {
                            minTemTextY = minTemTextTolerate;
                        } else if (minX0 >= viewWidth - minTemTextTolerate && (minY0 > minTemTextTolerate && minY0 <= viewHeight - minTemTextTolerate)) {
                            minTemTextX = (float) (viewWidth - minTemTextTolerate * 1.5);
                        } else {
                            minTemTextX = minX0;
                            minTemTextY = minY0;
                        }
                        canvas.drawText(minTem, 0, minTem.length(), minTemTextX + moveX, minTemTextY, maxPaint);
                        drawDot(canvas, bluePaint, minX0, minY0);
                        String maxTem = new DecimalFormat("0.0").format(maxTemperatureTem);
                        float maxTemX = temperatureSampleEasyResult.maxTemperaturePixel.x * xscale;
                        float maxTemY = temperatureSampleEasyResult.maxTemperaturePixel.y * yscale;
                        if (maxTemX <= 0 && maxTemY <= 0) {
                            maxTemX = PIXCOUNT;
                            maxTemY = PIXCOUNT;
                        } else if (maxTemX <= 0 && (maxTemY > 0 && maxTemY <= viewHeight)) {
                            maxTemX = PIXCOUNT;
                        } else if (maxTemX <= 0 && (maxTemY >= viewHeight)) {
                            maxTemX = PIXCOUNT;
                            maxTemY = viewHeight - PIXCOUNT;
                        } else if (maxTemX >= viewWidth && maxTemY <= 0) {
                            maxTemY = PIXCOUNT;
                        } else if (maxTemX >= viewWidth && maxTemY >= viewHeight) {
                            maxTemX = viewWidth - PIXCOUNT;
                            maxTemY = viewHeight - PIXCOUNT;
                        } else if ((maxTemX > 0 && maxTemX <= viewWidth) && maxTemY >= viewHeight) {
                            maxTemY = viewHeight - PIXCOUNT;
                        } else if ((maxTemX > 0 && maxTemX <= viewWidth) && maxTemY <= 0) {
                            maxTemY = PIXCOUNT;
                        } else if (maxTemX >= viewWidth && (maxTemY > 0 && maxTemY < viewHeight)) {
                            maxTemX = viewWidth - PIXCOUNT;
                        }
                        float maxTemTextX = maxTemX;
                        float maxTemTextY = maxTemY;
                        if (maxTemX <= minTemTextTolerate && maxTemY <= minTemTextTolerate) {
                            maxTemTextX = minTemTextTolerate;
                            maxTemTextY = minTemTextTolerate;
                        } else if (maxTemX <= minTemTextTolerate && (maxTemY > minTemTextTolerate && maxTemY <= viewHeight - minTemTextTolerate)) {
                            maxTemTextX = minTemTextTolerate;
                        } else if (maxTemX <= minTemTextTolerate && (maxTemY >= viewHeight - minTemTextTolerate)) {
                            maxTemTextX = minTemTextTolerate;
                            maxTemTextY = viewHeight - minTemTextTolerate;
                        } else if (maxTemX >= viewWidth - minTemTextTolerate && maxTemY <= minTemTextTolerate) {
                            maxTemTextX = (float) (viewWidth - minTemTextTolerate * 1.5);
                            maxTemTextY = minTemTextTolerate;
                        } else if (maxTemX >= viewWidth - minTemTextTolerate && maxTemY >= viewHeight - minTemTextTolerate) {
                            maxTemTextX = viewWidth - minTemTextTolerate;
                            maxTemTextY = viewHeight - minTemTextTolerate;
                        } else if ((maxTemX > minTemTextTolerate && maxTemX <= viewWidth - minTemTextTolerate) && maxTemY >= viewHeight - minTemTextTolerate) {
                            maxTemTextY = viewHeight - minTemTextTolerate;
                        } else if ((maxTemX > minTemTextTolerate && maxTemX <= viewWidth - minTemTextTolerate) && maxTemY <= minTemTextTolerate) {
                            maxTemTextY = minTemTextTolerate;
                        } else if (maxTemX >= viewWidth - minTemTextTolerate && (maxTemY > minTemTextTolerate && maxTemY <= viewHeight - minTemTextTolerate)) {
                            maxTemTextX = (float) (viewWidth - minTemTextTolerate * 1.5);
                        } else {
                            maxTemTextX = maxTemX;
                            maxTemTextY = maxTemY;
                        }

                        canvas.rotate(0, maxTemTextX, maxTemTextY);
                        canvas.drawText(maxTem, 0, maxTem.length(), maxTemTextX + moveX, maxTemTextY, maxPaint);
                        drawDot(canvas, redPaint, maxTemTextX, maxTemTextY);

                        for (int index = 0; index < rectangles.size(); index++) {
                            Rect tempRectangle = rectangles.get(index);
                            int left = (int) (tempRectangle.left / xscale);
                            int top = (int) (tempRectangle.top / yscale);
                            int right = (int) (tempRectangle.right / xscale);
                            int bottom = (int) (tempRectangle.bottom / yscale);
                            Log.d(TAG, "Rectangle right: " + right + ", bottom: " + bottom);
                            if (right > left && bottom > top && left < imageWidth && top < imageHeight && right > 0 && bottom > 0) {
                                temperatureSampleResult = irtemp.getTemperatureOfRect(new Rect(left, top, right, bottom));
                                rectangleResultList.set(index, temperatureSampleResult);
                                rectangleResultList.get(index).index = index + 1;
                                String min = new DecimalFormat("0.0").format(temperatureSampleResult.minTemperature) + "°C";
                                String max = new DecimalFormat("0.0").format(temperatureSampleResult.maxTemperature) + "°C";

                                setRectMaxTemp(max);
                                setRectMinTemp(min);

                                drawDot(canvas, bluePaint, temperatureSampleResult.minTemperaturePixel.x * xscale, temperatureSampleResult.minTemperaturePixel.y * yscale);
                                canvas.drawText(min, 0, min.length(), temperatureSampleResult.minTemperaturePixel.x * xscale + moveX, temperatureSampleResult.minTemperaturePixel.y * yscale, minPaint);
                                drawDot(canvas, redPaint, temperatureSampleResult.maxTemperaturePixel.x * xscale, temperatureSampleResult.maxTemperaturePixel.y * yscale);
                                canvas.drawText(max, 0, max.length(), temperatureSampleResult.maxTemperaturePixel.x * xscale + moveX, temperatureSampleResult.maxTemperaturePixel.y * yscale, maxPaint);
                            }
                        }
                        for (int i = rectangles.size(); i < 3; i++) {
                            rectangleResultList.get(i).index = 0;
                        }
                        for (int index = 0; index < lines.size(); index++) {
                            Line tempLine = lines.get(index);
                            int startX = (int) (tempLine.start.x / xscale);
                            int startY = (int) (tempLine.start.y / yscale);
                            int endX = (int) (tempLine.end.x / xscale);
                            int endY = (int) (tempLine.end.y / yscale);
                            int minX = Math.min(startX, endX);
                            int maxX = Math.max(startX, endX);
                            int minY = Math.min(startY, endY);
                            int maxY = Math.max(startY, endY);
                            if (maxX < imageWidth && minX > 0 && maxY < imageHeight && minY > 0) {
                                Log.d(TAG, "start point: (" + startX + ", " + startY + "), endX: (" + endX + ", " + endY + ")");
                                temperatureSampleResult = irtemp.getTemperatureOfLine(new Line(new Point(startX, startY), new Point(endX, endY)));
                                lineResultList.set(index, temperatureSampleResult);
                                lineResultList.get(index).index = index + 1;
                                Log.d(TAG, "minTemperaturePixel x: " + temperatureSampleResult.minTemperaturePixel.x);
                                String min = new DecimalFormat("0.0").format(temperatureSampleResult.minTemperature) + "°C";
                                String max = new DecimalFormat("0.0").format(temperatureSampleResult.maxTemperature) + "°C";
                                drawDot(canvas, whitePaint, temperatureSampleResult.minTemperaturePixel.x * xscale, temperatureSampleResult.minTemperaturePixel.y * yscale);
                                canvas.drawText(min, 0, min.length(), temperatureSampleResult.minTemperaturePixel.x * xscale + moveX, temperatureSampleResult.minTemperaturePixel.y * yscale, minPaint);
                                drawDot(canvas, whitePaint, temperatureSampleResult.maxTemperaturePixel.x * xscale, temperatureSampleResult.maxTemperaturePixel.y * yscale);
                                canvas.drawText(max, 0, max.length(), temperatureSampleResult.maxTemperaturePixel.x * xscale + moveX, temperatureSampleResult.maxTemperaturePixel.y * yscale, maxPaint);
                            }
                        }
                        for (int i = lines.size(); i < 3; i++) {
                            lineResultList.get(i).index = 0;
                        }
                        for (int index = 0; index < points.size(); index++) {
                            Point tempPoint = points.get(index);
                            int x = (int) (tempPoint.x / xscale);
                            int y = (int) (tempPoint.y / yscale);
                            if (x < imageWidth && x > 0 && y < imageHeight && y > 0) {
                                temperatureSampleResult = irtemp.getTemperatureOfPoint(new Point(x, y));
                                pointResultList.set(index, temperatureSampleResult);
                                pointResultList.get(index).index = index + 1;
                                String max = new DecimalFormat("0.0").format(temperatureSampleResult.maxTemperature) + "°C";
                                drawDot(canvas, whitePaint, temperatureSampleResult.maxTemperaturePixel.x * xscale, temperatureSampleResult.maxTemperaturePixel.y * yscale);
                                canvas.drawText(max, 0, max.length(), temperatureSampleResult.maxTemperaturePixel.x * xscale + moveX, temperatureSampleResult.maxTemperaturePixel.y * yscale, maxPaint);
                            }
                        }
                        for (int i = points.size(); i < 3; i++) {
                            pointResultList.get(i).index = 0;
                        }
                        if (temperatureRegionMode == REGION_MODE_CENTER) {
                            temperatureSampleResult = irtemp.getTemperatureOfPoint(new Point(imageWidth / 2, imageHeight / 2));
                            centerResultList = temperatureSampleResult;
                            String max = new DecimalFormat("0.0").format(temperatureSampleResult.maxTemperature) + "°C";
                            drawDot(canvas, whitePaint, temperatureSampleResult.maxTemperaturePixel.x * xscale, temperatureSampleResult.maxTemperaturePixel.y * yscale);
                            canvas.drawText(max, 0, max.length(), temperatureSampleResult.maxTemperaturePixel.x * xscale + moveX, temperatureSampleResult.maxTemperaturePixel.y * yscale, maxPaint);
                        }
                    }
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    if (surfaceViewCanvas == null) {
                        continue;
                    }
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionAndValueBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }

                SystemClock.sleep(1000);
            }
            Log.d(TAG, "temperatureThread exit");
        };

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
        int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        initialWidth -= paddingLeft + paddingRight;
        initialHeight -= paddingTop + paddingBottom;

        xscale = (float) initialWidth / (float) imageWidth;
        yscale = (float) initialHeight / (float) imageHeight;

        viewWidth = initialWidth;
        viewHeight = initialHeight;
        if (regionBitmap == null) {
            regionBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);
        }
        regionAndValueBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.w(TAG, "surfaceCreated");
        setZOrderOnTop(true);
        holder.setFormat(PixelFormat.TRANSLUCENT);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.w(TAG, "surfaceDestroyed");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (temperatureRegionMode == REGION_MODE_RECTANGLE) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startX = event.getX();
                startY = event.getY();
                Log.w(TAG, "ACTION_DOWN" + startX + "|" + startY);
                Rect rectangle = getRectangle(new Point((int) startX, (int) startY));
                if (rectangle.equals(new Rect())) {
                    actionMode = ACTION_MODE_INSERT;
                    Log.w(TAG, "ACTION_MODE_INSERT");
                } else {
                    actionMode = ACTION_MODE_MOVE;
                    movingRectangle = rectangle;
                    Log.w(TAG, "ACTION_MODE_MOVE");
                    if (startX > rectangle.left - TOUCH_TOLERANCE && startX < rectangle.left + TOUCH_TOLERANCE && startY > rectangle.top - TOUCH_TOLERANCE && startY < rectangle.top + TOUCH_TOLERANCE) {
                        Log.w(TAG, "move left top corner");
                        rectangleMoveType = RECTANGLE_MOVE_CORNER;
                        rectangleMoveCorner = RECTANGLE_LEFT_TOP_CORNER;
                    } else if (startX > rectangle.right - TOUCH_TOLERANCE && startX < rectangle.right + TOUCH_TOLERANCE && startY > rectangle.top - TOUCH_TOLERANCE && startY < rectangle.top + TOUCH_TOLERANCE) {
                        Log.w(TAG, "move right top corner");
                        rectangleMoveType = RECTANGLE_MOVE_CORNER;
                        rectangleMoveCorner = RECTANGLE_RIGHT_TOP_CORNER;
                    } else if (startX > rectangle.right - TOUCH_TOLERANCE && startX < rectangle.right + TOUCH_TOLERANCE && startY > rectangle.bottom - TOUCH_TOLERANCE && startY < rectangle.bottom + TOUCH_TOLERANCE) {
                        Log.w(TAG, "move right bottom corner");
                        rectangleMoveType = RECTANGLE_MOVE_CORNER;
                        rectangleMoveCorner = RECTANGLE_RIGHT_BOTTOM_CORNER;
                    } else if (startX > rectangle.left - TOUCH_TOLERANCE && startX < rectangle.left + TOUCH_TOLERANCE && startY > rectangle.bottom - TOUCH_TOLERANCE && startY < rectangle.bottom + TOUCH_TOLERANCE) {
                        Log.w(TAG, "move left bottom corner");
                        rectangleMoveType = RECTANGLE_MOVE_CORNER;
                        rectangleMoveCorner = RECTANGLE_LEFT_BOTTOM_CORNER;
                    } else if (startX > rectangle.left - TOUCH_TOLERANCE && startX < rectangle.left + TOUCH_TOLERANCE) {
                        Log.w(TAG, "move left edge");
                        rectangleMoveType = RECTANGLE_MOVE_EDGE;
                        rectangleMoveEdge = RECTANGLE_LEFT_EDGE;
                    } else if (startY > rectangle.top - TOUCH_TOLERANCE && startY < rectangle.top + TOUCH_TOLERANCE) {
                        Log.w(TAG, "move top edge");
                        rectangleMoveType = RECTANGLE_MOVE_EDGE;
                        rectangleMoveEdge = RECTANGLE_TOP_EDGE;
                    } else if (startX > rectangle.right - TOUCH_TOLERANCE && startX < rectangle.right + TOUCH_TOLERANCE) {
                        Log.w(TAG, "move right edge");
                        rectangleMoveType = RECTANGLE_MOVE_EDGE;
                        rectangleMoveEdge = RECTANGLE_RIGHT_EDGE;
                    } else if (startY > rectangle.bottom - TOUCH_TOLERANCE && startY < rectangle.bottom + TOUCH_TOLERANCE) {
                        Log.w(TAG, "move bottom edge");
                        rectangleMoveType = RECTANGLE_MOVE_EDGE;
                        rectangleMoveEdge = RECTANGLE_BOTTOM_EDGE;
                    } else {
                        Log.w(TAG, "move entire");
                        rectangleMoveType = RECTANGLE_MOVE_ENTIRE;
                    }
                    synchronized (regionLock) {
                        deleteRectangle(rectangle);
                    }
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    setBitmap();
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawRectangle(surfaceViewCanvas, linePaint, rectangle.left, rectangle.top, rectangle.right, rectangle.bottom);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                endX = event.getX();
                endY = event.getY();
                Log.w(TAG, "ACTION_DOWN " + endX + " | " + endY);
                if (actionMode == ACTION_MODE_INSERT) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawRectangle(surfaceViewCanvas, linePaint, startX, startY, endX, endY);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else if (actionMode == ACTION_MODE_MOVE) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    float biasX = endX - startX;
                    float biasY = endY - startY;
                    if (rectangleMoveType == RECTANGLE_MOVE_ENTIRE) {
                        drawRectangle(surfaceViewCanvas, linePaint, movingRectangle.left + biasX, movingRectangle.top + biasY, movingRectangle.right + biasX, movingRectangle.bottom + biasY);
                    }
                    if (rectangleMoveType == RECTANGLE_MOVE_EDGE) {
                        if (rectangleMoveEdge == RECTANGLE_LEFT_EDGE) {
                            drawRectangle(surfaceViewCanvas, linePaint, movingRectangle.left + biasX, movingRectangle.top, movingRectangle.right, movingRectangle.bottom);
                        }
                        if (rectangleMoveEdge == RECTANGLE_TOP_EDGE) {
                            drawRectangle(surfaceViewCanvas, linePaint, movingRectangle.left, movingRectangle.top + biasY, movingRectangle.right, movingRectangle.bottom);
                        }
                        if (rectangleMoveEdge == RECTANGLE_RIGHT_EDGE) {
                            drawRectangle(surfaceViewCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right + biasX, movingRectangle.bottom);
                        }
                        if (rectangleMoveEdge == RECTANGLE_BOTTOM_EDGE) {
                            drawRectangle(surfaceViewCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right, movingRectangle.bottom + biasY);
                        }
                    }
                    if (rectangleMoveType == RECTANGLE_MOVE_CORNER) {
                        if (rectangleMoveCorner == RECTANGLE_LEFT_TOP_CORNER) {
                            drawRectangle(surfaceViewCanvas, linePaint, movingRectangle.left + biasX, movingRectangle.top + biasY, movingRectangle.right, movingRectangle.bottom);
                        }
                        if (rectangleMoveCorner == RECTANGLE_RIGHT_TOP_CORNER) {
                            drawRectangle(surfaceViewCanvas, linePaint, movingRectangle.left, movingRectangle.top + biasY, movingRectangle.right + biasX, movingRectangle.bottom);
                        }
                        if (rectangleMoveCorner == RECTANGLE_RIGHT_BOTTOM_CORNER) {
                            drawRectangle(surfaceViewCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right + biasX, movingRectangle.bottom + biasY);
                        }
                        if (rectangleMoveCorner == RECTANGLE_LEFT_BOTTOM_CORNER) {
                            drawRectangle(surfaceViewCanvas, linePaint, movingRectangle.left + biasX, movingRectangle.top, movingRectangle.right, movingRectangle.bottom + biasY);
                        }
                    }
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.w(TAG, "ACTION_UP");
                endX = event.getX();
                endY = event.getY();
                if (actionMode == ACTION_MODE_INSERT) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    if (Math.abs(endX - startX) > TOUCH_TOLERANCE || Math.abs(endY - startY) > TOUCH_TOLERANCE) {
                        int left = (int) Math.min(startX, endX);
                        int right = (int) Math.max(startX, endX);
                        int top = (int) Math.min(startY, endY);
                        int bottom = (int) Math.max(startY, endY);
                        if (rectangles.size() < RECTANGLE_MAX_COUNT) {
                            synchronized (regionLock) {
                                addRectangle(new Rect(left, top, right, bottom));
                            }
                            Canvas bitmapCanvas = new Canvas(regionBitmap);
                            drawRectangle(bitmapCanvas, linePaint, startX, startY, endX, endY);
                        } else {
                            synchronized (regionLock) {
                                addRectangle(new Rect(left, top, right, bottom));
                            }
                            setBitmap();
                        }
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                if (actionMode == ACTION_MODE_MOVE) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    Canvas bitmapCanvas = new Canvas(regionBitmap);
                    float biasX = endX - startX;
                    float biasY = endY - startY;
                    Log.d(TAG, "ACTION_UP" + movingRectangle.left + " " + movingRectangle.top + "----" + movingRectangle.right + " " + movingRectangle.bottom + " ");
                    int tmp;
                    if (Math.abs(biasX) > TOUCH_TOLERANCE || Math.abs(biasY) > TOUCH_TOLERANCE) {
                        if (rectangleMoveType == RECTANGLE_MOVE_ENTIRE) {
                            drawRectangle(bitmapCanvas, linePaint, movingRectangle.left + biasX, movingRectangle.top + biasY, movingRectangle.right + biasX, movingRectangle.bottom + biasY);
                            synchronized (regionLock) {
                                addRectangle(new Rect((int) (movingRectangle.left + biasX), (int) (movingRectangle.top + biasY), (int) (movingRectangle.right + biasX), (int) (movingRectangle.bottom + biasY)));
                            }
                        }
                        if (rectangleMoveType == RECTANGLE_MOVE_EDGE) {
                            if (rectangleMoveEdge == RECTANGLE_LEFT_EDGE) {
                                movingRectangle.left += biasX;
                                if (movingRectangle.right < movingRectangle.left) {
                                    tmp = movingRectangle.left;
                                    movingRectangle.left = movingRectangle.right;
                                    movingRectangle.right = tmp;
                                }
                                drawRectangle(bitmapCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right, movingRectangle.bottom);
                                synchronized (regionLock) {
                                    addRectangle(new Rect((int) (movingRectangle.left), (int) (movingRectangle.top), (int) (movingRectangle.right), (int) (movingRectangle.bottom)));
                                }
                            }
                            if (rectangleMoveEdge == RECTANGLE_TOP_EDGE) {
                                movingRectangle.top += biasY;
                                if (movingRectangle.bottom < movingRectangle.top) {
                                    tmp = movingRectangle.bottom;
                                    movingRectangle.bottom = movingRectangle.top;
                                    movingRectangle.top = tmp;
                                }
                                drawRectangle(bitmapCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right, movingRectangle.bottom);
                                synchronized (regionLock) {
                                    addRectangle(new Rect((int) (movingRectangle.left), (int) (movingRectangle.top), (int) (movingRectangle.right), (int) (movingRectangle.bottom)));
                                }
                            }
                            if (rectangleMoveEdge == RECTANGLE_RIGHT_EDGE) {
                                movingRectangle.right += biasX;
                                if (movingRectangle.right < movingRectangle.left) {
                                    tmp = movingRectangle.left;
                                    movingRectangle.left = movingRectangle.right;
                                    movingRectangle.right = tmp;
                                }
                                drawRectangle(bitmapCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right, movingRectangle.bottom);
                                synchronized (regionLock) {
                                    addRectangle(new Rect((int) (movingRectangle.left), (int) (movingRectangle.top), (int) (movingRectangle.right), (int) (movingRectangle.bottom)));
                                }
                            }
                            if (rectangleMoveEdge == RECTANGLE_BOTTOM_EDGE) {
                                movingRectangle.bottom += biasY;
                                if (movingRectangle.bottom < movingRectangle.top) {
                                    tmp = movingRectangle.bottom;
                                    movingRectangle.bottom = movingRectangle.top;
                                    movingRectangle.top = tmp;
                                }
                                drawRectangle(bitmapCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right, movingRectangle.bottom);
                                synchronized (regionLock) {
                                    addRectangle(new Rect((int) (movingRectangle.left), (int) (movingRectangle.top), (int) (movingRectangle.right), (int) (movingRectangle.bottom)));
                                }
                            }
                        }
                        if (rectangleMoveType == RECTANGLE_MOVE_CORNER) {
                            if (rectangleMoveCorner == RECTANGLE_LEFT_TOP_CORNER) {
                                movingRectangle.left += biasX;
                                if (movingRectangle.right < movingRectangle.left) {
                                    tmp = movingRectangle.left;
                                    movingRectangle.left = movingRectangle.right;
                                    movingRectangle.right = tmp;
                                }
                                movingRectangle.top += biasY;
                                if (movingRectangle.bottom < movingRectangle.top) {
                                    tmp = movingRectangle.bottom;
                                    movingRectangle.bottom = movingRectangle.top;
                                    movingRectangle.top = tmp;
                                }

                                drawRectangle(bitmapCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right, movingRectangle.bottom);
                                synchronized (regionLock) {
                                    addRectangle(new Rect((int) (movingRectangle.left), (int) (movingRectangle.top), (int) (movingRectangle.right), (int) (movingRectangle.bottom)));
                                }
                            }
                            if (rectangleMoveCorner == RECTANGLE_RIGHT_TOP_CORNER) {
                                movingRectangle.right += biasX;
                                if (movingRectangle.right < movingRectangle.left) {
                                    tmp = movingRectangle.left;
                                    movingRectangle.left = movingRectangle.right;
                                    movingRectangle.right = tmp;
                                }
                                movingRectangle.top += biasY;
                                if (movingRectangle.bottom < movingRectangle.top) {
                                    tmp = movingRectangle.bottom;
                                    movingRectangle.bottom = movingRectangle.top;
                                    movingRectangle.top = tmp;
                                }
                                drawRectangle(bitmapCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right, movingRectangle.bottom);
                                synchronized (regionLock) {
                                    addRectangle(new Rect((int) (movingRectangle.left), (int) (movingRectangle.top), (int) (movingRectangle.right), (int) (movingRectangle.bottom)));
                                }
                            }
                            if (rectangleMoveCorner == RECTANGLE_RIGHT_BOTTOM_CORNER) {
                                movingRectangle.right += biasX;
                                if (movingRectangle.right < movingRectangle.left) {
                                    tmp = movingRectangle.left;
                                    movingRectangle.left = movingRectangle.right;
                                    movingRectangle.right = tmp;
                                }
                                movingRectangle.bottom += biasY;
                                if (movingRectangle.bottom < movingRectangle.top) {
                                    tmp = movingRectangle.bottom;
                                    movingRectangle.bottom = movingRectangle.top;
                                    movingRectangle.top = tmp;
                                }
                                drawRectangle(bitmapCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right, movingRectangle.bottom);
                                synchronized (regionLock) {
                                    addRectangle(new Rect((int) (movingRectangle.left), (int) (movingRectangle.top), (int) (movingRectangle.right), (int) (movingRectangle.bottom)));
                                }
                            }
                            if (rectangleMoveCorner == RECTANGLE_LEFT_BOTTOM_CORNER) {
                                movingRectangle.left += biasX;
                                if (movingRectangle.right < movingRectangle.left) {
                                    tmp = movingRectangle.left;
                                    movingRectangle.left = movingRectangle.right;
                                    movingRectangle.right = tmp;
                                }
                                movingRectangle.bottom += biasY;
                                if (movingRectangle.bottom < movingRectangle.top) {
                                    tmp = movingRectangle.bottom;
                                    movingRectangle.bottom = movingRectangle.top;
                                    movingRectangle.top = tmp;
                                }
                                drawRectangle(bitmapCanvas, linePaint, movingRectangle.left, movingRectangle.top, movingRectangle.right, movingRectangle.bottom);
                                synchronized (regionLock) {
                                    addRectangle(new Rect((int) (movingRectangle.left), (int) (movingRectangle.top), (int) (movingRectangle.right), (int) (movingRectangle.bottom)));
                                }
                            }
                        }
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return false;
            } else {
                return false;
            }
        } else if (temperatureRegionMode == REGION_MODE_LINE) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.w(TAG, "ACTION_DOWN");
                startX = event.getX();
                startY = event.getY();
                Line line = getLine(new Point((int) startX, (int) startY));
                if (line.start == null || line.end == null) {
                    actionMode = ACTION_MODE_INSERT;
                    Log.w(TAG, "ACTION_MODE_INSERT: startX = " + startX + "; startY = " + startY);
                } else {
                    actionMode = ACTION_MODE_MOVE;
                    movingLine = line;
                    Log.w(TAG, "ACTION_MODE_MOVE: startX = " + startX + "; startY = " + startY);
                    Log.w(TAG, "ACTION_MODE_MOVE: x0 = " + line.start.x + "; y0 = " + line.start.y + "; x1 = " + line.end.x + "; y1 = " + line.end.y);
                    if (startX > line.start.x - TOUCH_TOLERANCE && startX < line.start.x + TOUCH_TOLERANCE && startY > line.start.y - TOUCH_TOLERANCE && startY < line.start.y + TOUCH_TOLERANCE) {
                        lineMoveType = LINE_MOVE_POINT;
                        lineMovePoint = LINE_START;
                    } else if (startX > line.end.x - TOUCH_TOLERANCE && startX < line.end.x + TOUCH_TOLERANCE && startY > line.end.y - TOUCH_TOLERANCE && startY < line.end.y + TOUCH_TOLERANCE) {
                        lineMoveType = LINE_MOVE_POINT;
                        lineMovePoint = LINE_END;
                    } else {
                        lineMoveType = LINE_MOVE_ENTIRE;
                    }
                    synchronized (regionLock) {
                        deleteLine(line);
                    }
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    setBitmap();
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    if (line.start.x > 0 && line.start.x < viewWidth && line.end.x > 0 && line.end.x < viewWidth && line.start.y > 0 && line.start.y < viewHeight && line.end.y > 0 && line.end.y < viewHeight)
                        drawLine(surfaceViewCanvas, linePaint, line.start.x, line.start.y, line.end.x, line.end.y);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                endX = event.getX();
                endY = event.getY();
                if (actionMode == ACTION_MODE_INSERT) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawLine(surfaceViewCanvas, linePaint, startX, startY, endX, endY);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else if (actionMode == ACTION_MODE_MOVE) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    float biasX = endX - startX;
                    float biasY = endY - startY;
                    if (lineMoveType == LINE_MOVE_ENTIRE) {
                        drawLine(surfaceViewCanvas, linePaint, movingLine.start.x + biasX, movingLine.start.y + biasY, movingLine.end.x + biasX, movingLine.end.y + biasY);
                    } else if (lineMoveType == LINE_MOVE_POINT) {
                        if (lineMovePoint == LINE_START) {
                            drawLine(surfaceViewCanvas, linePaint, movingLine.start.x + biasX, movingLine.start.y + biasY, movingLine.end.x, movingLine.end.y);
                        } else if (lineMovePoint == LINE_END) {
                            drawLine(surfaceViewCanvas, linePaint, movingLine.start.x, movingLine.start.y, movingLine.end.x + biasX, movingLine.end.y + biasY);
                        }
                    }
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                Log.w(TAG, "ACTION_UP");
                endX = event.getX();
                endY = event.getY();
                if (actionMode == ACTION_MODE_INSERT) {
                    Log.w(TAG, "ACTION_MODE_INSERT: endX = " + endX + "; endY = " + endY);
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    if (Math.abs(endX - startX) > TOUCH_TOLERANCE || Math.abs(endY - startY) > TOUCH_TOLERANCE) {
                        Point start = new Point((int) startX, (int) startY);
                        Point end = new Point((int) endX, (int) endY);
                        if (lines.size() < LINE_MAX_COUNT) {
                            synchronized (regionLock) {
                                if (start.x > 0 && start.x < viewWidth && end.x > 0 && end.x < viewWidth && start.y > 0 && start.y < viewHeight && end.y > 0 && end.y < viewHeight)
                                    addLine(new Line(start, end));
                            }
                            Canvas bitmapCanvas = new Canvas(regionBitmap);
                            if (start.x > 0 && start.x < viewWidth && end.x > 0 && end.x < viewWidth && start.y > 0 && start.y < viewHeight && end.y > 0 && end.y < viewHeight)
                                drawLine(bitmapCanvas, linePaint, startX, startY, endX, endY);
                        } else {
                            synchronized (regionLock) {
                                if (start.x > 0 && start.x < viewWidth && end.x > 0 && end.x < viewWidth && start.y > 0 && start.y < viewHeight && end.y > 0 && end.y < viewHeight)
                                    addLine(new Line(start, end));
                            }
                            setBitmap();
                        }
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else if (actionMode == ACTION_MODE_MOVE) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    Canvas bitmapCanvas = new Canvas(regionBitmap);
                    float biasX = endX - startX;
                    float biasY = endY - startY;
                    if (movingLine.start.x + biasX > 0 && movingLine.start.x + biasX < viewWidth && movingLine.end.x + biasX > 0 && movingLine.end.x + biasX < viewWidth && movingLine.start.y + biasY > 0 && movingLine.start.y + biasY < viewHeight && movingLine.end.y + biasY > 0 && movingLine.end.y + biasY < viewHeight) {
                        if (Math.abs(biasX) > TOUCH_TOLERANCE || Math.abs(biasY) > TOUCH_TOLERANCE) {
                            if (lineMoveType == LINE_MOVE_ENTIRE) {
                                drawLine(bitmapCanvas, linePaint, movingLine.start.x + biasX, movingLine.start.y + biasY, movingLine.end.x + biasX, movingLine.end.y + biasY);
                                synchronized (regionLock) {
                                    Point start = new Point((int) (movingLine.start.x + biasX), (int) (movingLine.start.y + biasY));
                                    Point end = new Point((int) (movingLine.end.x + biasX), (int) (movingLine.end.y + biasY));
                                    addLine(new Line(start, end));
                                }
                            } else if (lineMoveType == LINE_MOVE_POINT) {
                                if (lineMovePoint == LINE_START) {
                                    drawLine(bitmapCanvas, linePaint, movingLine.start.x + biasX, movingLine.start.y + biasY, movingLine.end.x, movingLine.end.y);
                                    synchronized (regionLock) {
                                        Point start = new Point((int) (movingLine.start.x + biasX), (int) (movingLine.start.y + biasY));
                                        Point end = new Point((int) (movingLine.end.x), (int) (movingLine.end.y));
                                        addLine(new Line(start, end));
                                    }
                                } else if (lineMovePoint == LINE_END) {
                                    drawLine(bitmapCanvas, linePaint, movingLine.start.x, movingLine.start.y, movingLine.end.x + biasX, movingLine.end.y + biasY);
                                    synchronized (regionLock) {
                                        Point start = new Point((int) (movingLine.start.x), (int) (movingLine.start.y));
                                        Point end = new Point((int) (movingLine.end.x + biasX), (int) (movingLine.end.y + biasY));
                                        addLine(new Line(start, end));
                                    }
                                }
                            }
                        }
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return false;
            } else {
                return false;
            }
        } else if (temperatureRegionMode == REGION_MODE_POINT) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                startX = event.getX();
                startY = event.getY();
                Log.w(TAG, "ACTION_DOWN" + startX + "|" + startY);
                Point point = getPoint(new Point((int) startX, (int) startY));
                if (point.equals(new Point())) {
                    actionMode = ACTION_MODE_INSERT;
                    if (points.size() == POINT_MAX_COUNT) {
                        synchronized (regionLock) {
                            deletePoint();
                        }
                        setBitmap();
                    }
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawPoint(surfaceViewCanvas, linePaint, startX, startY);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else {
                    actionMode = ACTION_MODE_MOVE;
                    movingPoint = point;
                    synchronized (regionLock) {
                        deletePoint(point);
                    }
                    setBitmap();
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawPoint(surfaceViewCanvas, linePaint, movingPoint.x, movingPoint.y);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                endX = event.getX();
                endY = event.getY();
                if (actionMode == ACTION_MODE_INSERT) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    drawPoint(surfaceViewCanvas, linePaint, endX, endY);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else if (actionMode == ACTION_MODE_MOVE) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    float biasX = endX - startX;
                    float biasY = endY - startY;
                    drawPoint(surfaceViewCanvas, linePaint, movingPoint.x + biasX, movingPoint.y + biasY);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                endX = event.getX();
                endY = event.getY();
                if (actionMode == ACTION_MODE_INSERT) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    if (points.size() < POINT_MAX_COUNT) {
                        synchronized (regionLock) {
                            addPoint(new Point((int) endX, (int) endY));
                        }
                        Canvas bitmapCanvas = new Canvas(regionBitmap);
                        drawPoint(bitmapCanvas, linePaint, endX, endY);
                    } else {
                        synchronized (regionLock) {
                            addPoint(new Point((int) endX, (int) endY));
                        }
                        setBitmap();
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                } else if (actionMode == ACTION_MODE_MOVE) {
                    Canvas surfaceViewCanvas = getHolder().lockCanvas();
                    surfaceViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    Canvas bitmapCanvas = new Canvas(regionBitmap);
                    float biasX = endX - startX;
                    float biasY = endY - startY;
                    if (Math.abs(biasX) > TOUCH_TOLERANCE || Math.abs(biasY) > TOUCH_TOLERANCE) {
                        drawPoint(bitmapCanvas, linePaint, movingPoint.x + biasX, movingPoint.y + biasY);
                        synchronized (regionLock) {
                            addPoint(new Point((int) (movingPoint.x + biasX), (int) (movingPoint.y + biasY)));
                        }
                    }
                    surfaceViewCanvas.drawBitmap(regionBitmap, new Rect(0, 0, viewWidth, viewHeight), new Rect(0, 0, viewWidth, viewHeight), null);
                    getHolder().unlockCanvasAndPost(surfaceViewCanvas);
                }
                return false;
            } else {
                return false;
            }
        } else {

            return false;
        }
    }

    public void addPoint(Point point) {
        if (points.size() < POINT_MAX_COUNT) {
            points.add(point);
        } else {
            for (int index = 0; index < points.size() - 1; index++) {
                Point tempPoint = points.get(index + 1);
                points.set(index, tempPoint);
            }
            points.set(points.size() - 1, point);
        }
    }

    public Point getPoint(Point point) {
        Point point1 = new Point();
        for (int index = 0; index < points.size(); index++) {
            Point tempPoint = points.get(index);
            if (tempPoint.x > point.x - TOUCH_TOLERANCE && tempPoint.x < point.x + TOUCH_TOLERANCE && tempPoint.y > point.y - TOUCH_TOLERANCE && tempPoint.y < point.y + TOUCH_TOLERANCE) {
                point1 = tempPoint;
            }
        }
        return point1;
    }

    public void deletePoint(Point point) {
        for (int index = 0; index < points.size(); index++) {
            Point tempPoint = points.get(index);
            if (tempPoint.equals(point)) {
                points.remove(index);
                break;
            }
        }
    }

    public void deletePoint() {
        for (int index = 0; index < points.size() - 1; index++) {
            Point tempPoint = points.get(index + 1);
            points.set(index, tempPoint);
        }
        points.remove(points.size() - 1);
    }

    public void addLine(Line line) {
        if (lines.size() < LINE_MAX_COUNT) {
            lines.add(line);
        } else {
            for (int index = 0; index < lines.size() - 1; index++) {
                Line tempLine = lines.get(index + 1);
                lines.set(index, tempLine);
            }
            lines.set(lines.size() - 1, line);
        }
    }

    public Line getLine(Point point) {
        Line line = new Line();
        for (int index = 0; index < lines.size(); index++) {
            Line tempLine = lines.get(index);
            int tempDistance = ((tempLine.end.y - tempLine.start.y) * point.x - (tempLine.end.x - tempLine.start.x) * point.y + tempLine.end.x * tempLine.start.y - tempLine.start.x * tempLine.end.y);
            tempDistance = (int) (tempDistance / Math.sqrt(Math.pow(tempLine.end.y - tempLine.start.y, 2) + Math.pow(tempLine.end.x - tempLine.start.x, 2)));
            Log.w(TAG, "tempDistance = " + tempDistance);
            if (Math.abs(tempDistance) < TOUCH_TOLERANCE && point.x > Math.min(tempLine.start.x, tempLine.end.x) - TOUCH_TOLERANCE && point.x < Math.max(tempLine.start.x, tempLine.end.x) + TOUCH_TOLERANCE) {
                line = tempLine;
            }
        }
        return line;
    }

    public void deleteLine(Line line) {
        for (int index = 0; index < lines.size(); index++) {
            Line tempLine = lines.get(index);
            if (tempLine.start.equals(line.start) && tempLine.end.equals(line.end)) {
                lines.remove(index);
                break;
            }
        }
    }

    public void addRectangle(Rect rectangle) {
        if (rectangles.size() < RECTANGLE_MAX_COUNT) {
            rectangles.add(rectangle);
        } else {
            for (int index = 0; index < rectangles.size() - 1; index++) {
                Rect tempRectangle = rectangles.get(index + 1);
                rectangles.set(index, tempRectangle);
            }
            rectangles.set(rectangles.size() - 1, rectangle);
        }
    }

    public Rect getRectangle(Point point) {
        Rect rectangle = new Rect();
        for (int index = 0; index < rectangles.size(); index++) {
            Rect tempRectangle = rectangles.get(index);
            if (tempRectangle.left - TOUCH_TOLERANCE < point.x && tempRectangle.right + TOUCH_TOLERANCE > point.x
                    && tempRectangle.top - TOUCH_TOLERANCE < point.y && tempRectangle.bottom + TOUCH_TOLERANCE > point.y) {
                rectangle = tempRectangle;
            }
        }
        return rectangle;
    }

    public void deleteRectangle(Rect rect) {
        for (int index = 0; index < rectangles.size(); index++) {
            Rect tempRectangle = rectangles.get(index);
            if (tempRectangle.equals(rect)) {
                rectangles.remove(index);
                break;
            }
        }
    }

    private void drawPoint(Canvas canvas, Paint paint, float x1, float y1) {
        float x = (int) (x1 / xscale) * xscale;
        float y = (int) (y1 / yscale) * yscale;
        float[] points = new float[]{
                x - POINT_SIZE, y, x - DOT_RADIUS, y,
                x, y - POINT_SIZE, x, y - DOT_RADIUS,
                x + POINT_SIZE, y, x + DOT_RADIUS, y,
                x, y + POINT_SIZE, x, y + DOT_RADIUS,
        };
        canvas.drawLines(points, paint);
    }

    private void drawLine(Canvas canvas, Paint paint, float x1, float y1, float x2, float y2) {
        float xStart = (int) (x1 / xscale) * xscale;
        float yStart = (int) (y1 / yscale) * yscale;
        float xEnd = (int) (x2 / xscale) * xscale;
        float yEnd = (int) (y2 / yscale) * yscale;
        float[] points = new float[]{xStart, yStart, xEnd, yEnd};
        canvas.drawLines(points, paint);
    }

    private void drawRectangle(Canvas canvas, Paint paint, float x1, float y1, float x2, float y2) {
        float[] points = new float[]{x1, y1, x2, y1, x2, y1, x2, y2, x2, y2, x1, y2, x1, y2, x1, y1};
        canvas.drawLines(points, paint);
    }

    private void drawDot(Canvas canvas, Paint paint, float x1, float y1) {
        canvas.drawCircle(x1, y1, DOT_RADIUS, paint);
    }

    private void setBitmap() {
        regionBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(regionBitmap);
        for (int index = 0; index < points.size(); index++) {
            Point tempPoint = points.get(index);
            drawPoint(canvas, linePaint, tempPoint.x, tempPoint.y);
        }
        for (int index = 0; index < lines.size(); index++) {
            Line tempLine = lines.get(index);
            drawLine(canvas, linePaint, tempLine.start.x, tempLine.start.y, tempLine.end.x, tempLine.end.y);
        }
        for (int index = 0; index < rectangles.size(); index++) {
            Rect tempRectangle = rectangles.get(index);
            drawRectangle(canvas, linePaint, tempRectangle.left, tempRectangle.top, tempRectangle.right, tempRectangle.bottom);
        }
    }

    public void start() {
        runflag = true;
        temperatureThread = new Thread(runnable);
        setVisibility(INVISIBLE);
        temperatureThread.start();
    }

    public void pause() {
        runflag = false;
    }

    public void clear() {
        points.clear();
        lines.clear();
        rectangles.clear();
        regionBitmap.eraseColor(0);
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

    public void stop() {
        Log.w(TAG, "temperatureThread interrupt");
        pause();
        temperatureThread.interrupt();
        try {
            temperatureThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getRegionAndValueBitmap() {
        synchronized (regionLock) {
            return regionAndValueBitmap;
        }
    }

    public float getMaxTemperature() {
        return maxTemperature;
    }

    public float getMinTemperature() {
        return minTemperature;
    }

    public String getRectMinTemp() {
        if (rectangles.size() > 0) {
            return RectMinTemp;
        }
        return "";
    }

    public void setRectMinTemp(String rectMinTemp) {
        RectMinTemp = rectMinTemp;
    }

    public String getRectMaxTemp() {
        if (rectangles.size() > 0) {
            return RectMaxTemp;
        }
        return "";
    }

    public void setRectMaxTemp(String rectMaxTemp) {
        RectMaxTemp = rectMaxTemp;
    }
}