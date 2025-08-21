package com.energy.commoncomponent.view.tempcanvas;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.energy.commoncomponent.R;
import com.energy.commoncomponent.utils.ScreenUtils;

import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by fengjibo on 2023/6/21.
 */
public abstract class BaseTemperatureView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "BaseTemperatureView";

    /**
     * 绘制的中心点距离边界最小距离
     * 用于判断手势移动超出边界
     */
    private final static int BORDER_PX = 8;

    private Context mContext;
    private SurfaceHolder mSurfaceHolder;

    private TempThread mTempThread;

    private DrawThread mDrawThread;

    private boolean mCanDraw = false;

    private GestureDetector mGestureDetector;

    protected PointDraw mPointDraw;
    protected LineDraw mLineDraw;
    protected RectDraw mRectDraw;

    private DrawModel mDrawModel = DrawModel.NONE;

    private Object mCanvasLock = new Object();

    private float mFirstX;//手指按下x坐标，相对父布局
    private float mFirstY;//手指按下y坐标，相对父布局

    private float mCurX;//当前手指x坐标，相对父布局
    private float mCurY;//当前手指y坐标，相对父布局

    private float mRawX;//当前手指x坐标，相对屏幕
    private float mRawY;//当前手指y坐标，相对屏幕

    private float mDistanceX;//当前手指距离上个点滑动的x轴距离
    private float mDistanceY;//当前手指距离上个点滑动的y轴距离

    protected int mViewWidth;
    protected int mViewHeight;

    /**
     * 温度数据宽高
     */
    protected int mTempWidth;
    protected int mTempHeight;

    protected float xScale = 0;//实际渲染与原始图像宽高比
    protected float yScale = 0;//实际渲染与原始图像宽高比

    /**
     * 温度文字绘制相关
     */
    private int mTextWidth = 110;
    private TextPaint mTextPaint;

    public BaseTemperatureView(Context context) {
        this(context, null);
    }

    public BaseTemperatureView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public BaseTemperatureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    //------------------------ public method ------------------------------//
    /**
     * 开始绘制
     */
    public void start() {
        mDrawThread = new DrawThread();//创建一个绘图线程
        mDrawThread.start();
    }

    /**
     * 停止绘制
     */
    public void stop () {
        mDrawThread.isRun = false;
        if (mDrawThread != null) {
            mDrawThread.interrupt();
            mDrawThread = null;
        }
    }

    /**
     * 恢复绘制
     */
    public void resume() {
        if (mDrawThread != null) {
            mDrawThread.isRun = true;
        }
    }

    /**
     * 暂停绘制
     */
    public void pause() {
        if (mDrawThread != null) {
            mDrawThread.isRun = false;
        }
    }

    /**
     * 当前设置绘制类型, 如点，线，框，圆
     */
    public void setDrawModel(DrawModel drawModel) {
        this.mDrawModel = drawModel;
    }

    /**
     * 清空画布所有
     */
    public void clearCanvas() {
        pause();
        if (mPointDraw != null) {
            mPointDraw.removePoint();
        }
        if (mLineDraw != null) {
            mLineDraw.removeLine();
        }
        if (mRectDraw != null) {
            mRectDraw.removeRect();
        }
        resume();
    }

    public abstract int getTempWidth();

    public abstract int getTempHeight();

    //------------------------ private method ------------------------------//
    private void initView(Context context) {
        Log.d(TAG, "initView");
        mContext = context;

        mSurfaceHolder = this.getHolder();

        mSurfaceHolder.addCallback(this);

        setFocusableInTouchMode(true);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(ScreenUtils.sp2px(14));

        mPointDraw = new PointDraw(mContext);
        mLineDraw = new LineDraw(mContext);
        mRectDraw = new RectDraw(mContext);

        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        setZOrderOnTop(true);
        setZOrderMediaOverlay(true);

        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onContextClick(MotionEvent e) {
                Log.d(TAG, "onContextClick");
                return super.onContextClick(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d(TAG, "onDoubleTap");


                return super.onDoubleTap(e);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {

                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "onDoubleTapEvent ACTION_DOWN");

                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onDoubleTapEvent ACTION_UP");
                        //双击
                        mCurX = e.getX();
                        mCurY = e.getY();
                        mRawX = e.getRawX();
                        mRawY = e.getRawY();
                        Log.d(TAG, "onDoubleTap mCurX : " + mCurX);
                        Log.d(TAG, "onDoubleTap mCurY : " + mCurY);
                        // 防止数据越界到图像外部
                        if (mCurX < BORDER_PX) {
                            mCurX = BORDER_PX;
                        } else if (mCurX >= mViewWidth) {
                            mCurX = mViewWidth - BORDER_PX;
                        }
                        if (mCurY < BORDER_PX) {
                            mCurY = BORDER_PX;
                        } else if (mCurY >= mViewHeight) {
                            mCurY = mViewHeight - BORDER_PX;
                        }
                        switch (mDrawModel) {
                            case DRAW_POINT:
                                if (mPointDraw.getOperateStatus() == PointDraw.OPERATE_STATUS_POINT_IN_TOUCH) {
                                    mPointDraw.setOperateStatus(PointDraw.OPERATE_STATUS_POINT_REMOVE);
                                }

                                break;
                            case DRAW_LINE:
                                if (mLineDraw.getOperateStatus() == LineDraw.OPERATE_STATUS_LINE_IN_TOUCH_START
                                        || mLineDraw.getOperateStatus() == LineDraw.OPERATE_STATUS_LINE_IN_TOUCH_END
                                        || mLineDraw.getOperateStatus() == LineDraw.OPERATE_STATUS_LINE_IN_TOUCH_CENTER) {

                                    mLineDraw.setOperateStatus(LineDraw.OPERATE_STATUS_LINE_REMOVE);
                                }

                                break;
                            case DRAW_RECT:
                                if (mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_LEFT_TOP_CORNER
                                        || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_RIGHT_TOP_CORNER
                                        || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_RIGHT_BOTTOM_CORNER
                                        || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_LEFT_BOTTOM_CORNER
                                        || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_LEFT_EDGE
                                        || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_TOP_EDGE
                                        || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_RIGHT_EDGE
                                        || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_BOTTOM_EDGE
                                        || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_MOVE_ENTIRE) {
                                    mRectDraw.setOperateStatus(RectDraw.OPERATE_STATUS_RECTANGLE_STATUS_REMOVE);
                                }

                                break;
                        }
                        break;
                }

                return super.onDoubleTapEvent(e);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                //屏幕点下
                Log.d(TAG, "onDown");
                pause();

                mFirstX = e.getX();
                mFirstY = e.getY();
                mRawX = e.getRawX();
                mRawY = e.getRawY();
                if (mFirstX < BORDER_PX) {
                    mFirstX = BORDER_PX;
                } else if (mFirstX >= mViewWidth) {
                    mFirstX = mViewWidth - BORDER_PX;
                }
                if (mFirstY < BORDER_PX) {
                    mFirstY = BORDER_PX;
                } else if (mFirstY >= mViewHeight) {
                    mFirstY = mViewHeight - BORDER_PX;
                }
                Log.d(TAG, "onDown mFirstX : " + mFirstX);
                Log.d(TAG, "onDown mFirstY : " + mFirstY);
                switch (mDrawModel) {
                    case DRAW_POINT:
                        //判断当时是否触碰已有的点
                        int indexPointTouch = mPointDraw.checkTouchPointInclude(mFirstX, mFirstY);
                        Log.d(TAG, "indexPointTouch : " + indexPointTouch);
                        if (indexPointTouch != -1) {
                            mPointDraw.setOperateStatus(PointDraw.OPERATE_STATUS_POINT_IN_TOUCH);
                        } else {
                            mPointDraw.setOperateStatus(PointDraw.OPERATE_STATUS_POINT_ADD);
                        }
                        break;
                    case DRAW_LINE:
                        int indexLineTouch = mLineDraw.checkTouchLineInclude((int)mFirstX, (int)mFirstY);
                        Log.d(TAG, "indexLineTouch : " + indexLineTouch);
                        if (indexLineTouch != -1) {
                            //判断触碰线的面积
                            mLineDraw.changeTouchLineOperateStatus(mFirstX, mFirstY);
                        } else {
                            mLineDraw.setOperateStatus(LineDraw.OPERATE_STATUS_LINE_ADD);
                        }
                        break;
                    case DRAW_RECT:
                        int indexRectTouch = mRectDraw.checkTouchRectInclude((int)mFirstX, (int)mFirstY);
                        Log.d(TAG, "indexRectTouch : " + indexRectTouch);
                        if (indexRectTouch != -1) {
                            mRectDraw.changeTouchRectOperateStatus(mFirstX, mFirstY);
                        } else {
                            mRectDraw.setOperateStatus(RectDraw.OPERATE_STATUS_RECTANGLE_STATUS_ADD);
                        }
                        break;
                }

                return super.onDown(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //屏幕拖动
                mCurX = e2.getX();
                mCurY = e2.getY();
                mRawX = e2.getRawX();
                mRawY = e2.getRawY();

                mDistanceX = distanceX;
                mDistanceY = distanceY;

                // 防止数据越界到图像外部
                if (mCurX < BORDER_PX) {
                    mCurX = BORDER_PX;
                } else if (mCurX >= mViewWidth) {
                    mCurX = mViewWidth - BORDER_PX;
                }
                if (mCurY < BORDER_PX) {
                    mCurY = BORDER_PX;
                } else if (mCurY >= mViewHeight) {
                    mCurY = mViewHeight - BORDER_PX;
                }
                Log.d(TAG, "onScroll mCurX : " + mCurX);
                Log.d(TAG, "onScroll mCurY : " + mCurY);
                Log.d(TAG, "onScroll distanceX : " + mDistanceX);
                Log.d(TAG, "onScroll distanceY : " + mDistanceY);
                float moveX = mCurX - mFirstX;
                float moveY = mCurY - mFirstY;

                switch (mDrawModel) {
                    case DRAW_POINT:
                        if (mPointDraw.getOperateStatus() == PointDraw.OPERATE_STATUS_POINT_IN_TOUCH) {
                            mPointDraw.changeTouchPointLocationByIndex(mPointDraw.getTouchInclude(), mCurX, mCurY);
                        }

                        doTouchDraw();
                        break;
                    case DRAW_LINE:
                        if (mLineDraw.getOperateStatus() == LineDraw.OPERATE_STATUS_LINE_IN_TOUCH_START
                                || mLineDraw.getOperateStatus() == LineDraw.OPERATE_STATUS_LINE_IN_TOUCH_END
                                || mLineDraw.getOperateStatus() == LineDraw.OPERATE_STATUS_LINE_IN_TOUCH_CENTER) {
                            mLineDraw.changeTouchLineLocationByIndex(moveX, moveY);
                        }

                        doTouchDraw();
                        break;
                    case DRAW_RECT:
                        if (mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_LEFT_TOP_CORNER
                                || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_RIGHT_TOP_CORNER
                                || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_RIGHT_BOTTOM_CORNER
                                || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_LEFT_BOTTOM_CORNER
                                || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_LEFT_EDGE
                                || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_TOP_EDGE
                                || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_RIGHT_EDGE
                                || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_BOTTOM_EDGE
                                || mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_MOVE_ENTIRE) {
                            mRectDraw.changeTouchLineLocationByIndex(mRectDraw.getTouchInclude(), moveX, moveY);
                        }
                        doTouchDraw();
                        break;
                }

                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //屏幕点下
                Log.d(TAG, "onFling");

                return super.onFling(e1, e2, velocityX, velocityY);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                //屏幕点击后弹起
                Log.d(TAG, "onSingleTapUp");

                mCurX = e.getX();
                mCurY = e.getY();
                mRawX = e.getRawX();
                mRawY = e.getRawY();
                if (mCurX < BORDER_PX) {
                    mCurX = BORDER_PX;
                } else if (mCurX >= mViewWidth) {
                    mCurX = mViewWidth - BORDER_PX;
                }
                if (mCurY < BORDER_PX) {
                    mCurY = BORDER_PX;
                } else if (mCurY >= mViewHeight) {
                    mCurY = mViewHeight - BORDER_PX;
                }
                switch (mDrawModel) {
                    case DRAW_POINT:
                        break;
                    case DRAW_LINE:
                        break;
                    case DRAW_RECT:
                        break;
                }
                resume();
                return super.onSingleTapUp(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                //屏幕点下 并长按时触发
                Log.d(TAG, "onLongPress");
                super.onLongPress(e);
            }

            @Override
            public void onShowPress(MotionEvent e) {
                //屏幕长按
                Log.d(TAG, "onShowPress");
                super.onShowPress(e);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event); //通知手势识别方法
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                break;

            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "ACTION_UP OR ACTION_POINTER_UP");
                switch (mDrawModel) {
                    case DRAW_POINT:
                        if (mPointDraw.getOperateStatus() == PointDraw.OPERATE_STATUS_POINT_ADD) {
                            mPointDraw.addPoint(1, mCurX, mCurY);
                        } else if (mPointDraw.getOperateStatus() == PointDraw.OPERATE_STATUS_POINT_REMOVE) {
                            mPointDraw.removePoint(mPointDraw.getTouchInclude());
                        }
                        doShapeDraw();
                        break;
                    case DRAW_LINE:
                        if (mLineDraw.getOperateStatus() == LineDraw.OPERATE_STATUS_LINE_ADD) {
                            mLineDraw.addLine((int)mFirstX, (int)mFirstY, (int)mCurX, (int)mCurY);
                        } else if (mLineDraw.getOperateStatus() == LineDraw.OPERATE_STATUS_LINE_REMOVE) {
                            mLineDraw.removeLine(mLineDraw.getTouchInclude());
                        } else {
                            mLineDraw.changeTouchPointLocation();
                        }

                        break;
                    case DRAW_RECT:
                        if (mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_STATUS_ADD) {
                            int left = (int) Math.min(mFirstX, mCurX);
                            int right = (int) Math.max(mFirstX, mCurX);
                            int top = (int) Math.min(mFirstY, mCurY);
                            int bottom = (int) Math.max(mFirstY, mCurY);

                            mRectDraw.addRect(left, top, right, bottom);
                        } else if (mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_STATUS_REMOVE) {
                            mRectDraw.removeRect(mRectDraw.getTouchInclude());
                        } else {
                            mRectDraw.changeTouchRectLocation();
                        }
                        doShapeDraw();
                        break;
                }
                resume();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;

        }
        return true;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        mCanDraw = true;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        mCanDraw = false;
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

        mViewWidth = initialWidth;
        mViewHeight = initialHeight;

        mPointDraw.setViewWidth(mViewWidth);
        mPointDraw.setViewHeight(mViewHeight);
        mLineDraw.setViewWidth(mViewWidth);
        mLineDraw.setViewHeight(mViewHeight);
        mRectDraw.setViewWidth(mViewWidth);
        mRectDraw.setViewHeight(mViewHeight);

        mTempWidth = getTempWidth();
        mTempHeight = getTempHeight();

        xScale = (float) initialWidth / (float) mTempWidth;
        yScale = (float) initialHeight / (float) mTempHeight;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    /**
     * 温度数据处理线程
     * 通过libirtemp库获取点，线，框的对应最大最小温度，更新数据
     * 不做绘制
     */
    private class TempThread extends Thread {
        public boolean isRun;

        public TempThread () {
            isRun = true;
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    if (!isRun) {
                        Thread.sleep(1000);
                        continue;
                    }

                    Log.d(TAG, "TempThread running");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG, "TempThread InterruptedException :" + e.getMessage());
                    if (mTempThread != null) {
                        mTempThread.interrupt();
                        Log.d(TAG, "TempThread interrupt");
                    }
                }
            }
        }
    }

    /**
     * 绘制处理线程
     */
    private class DrawThread extends Thread {

        public boolean isRun;

        public DrawThread() {
            isRun = true;
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {

                try {
                    if (!isRun) {
//                        Thread.sleep(384);
                        continue;
                    }

                    doShapeDraw();
                }catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "DrawThread InterruptedException :" + e.getMessage());
                }
            }
        }
    }


    /**
     * 图形绘制
     */
    private void doShapeDraw() {
        if (mSurfaceHolder == null || !mCanDraw) {
            return;
        }
        Canvas canvas = null;

        try {
            synchronized (mCanvasLock) {
                canvas = mSurfaceHolder.lockCanvas();

                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                if (mPointDraw.getPointViewList().size() != 0
                        || mLineDraw.getLineViewList().size() != 0
                        || mRectDraw.getRectViewList().size() != 0) {
                    CopyOnWriteArrayList<TempResultBean> tempResultBeans = generateViewData(mPointDraw.getPointViewList(), mLineDraw.getLineViewList(), mRectDraw.getRectViewList());

                    for (int i = 0; i < tempResultBeans.size(); i ++) {
                        TempResultBean tempResultBean = tempResultBeans.get(i);
                        for (int x = 0; x < mPointDraw.getPointViewList().size(); x ++) {
                            if (mPointDraw.getPointViewList().get(x).getId().equals(tempResultBean.getId())) {
                                mPointDraw.getPointViewList().get(x).setTempPoint(new Point(tempResultBean.getMax_temp_x(), tempResultBean.getMax_temp_y()));
                            }
                        }
                        for (int y = 0; y < mLineDraw.getLineViewList().size(); y ++) {
                            if (mLineDraw.getLineViewList().get(y).getId().equals(tempResultBean.getId())) {
                                mLineDraw.getLineViewList().get(y).setHighTempPoint(new Point(tempResultBean.getMax_temp_x(), tempResultBean.getMax_temp_y()));
                                mLineDraw.getLineViewList().get(y).setLowTempPoint(new Point(tempResultBean.getMin_temp_x(), tempResultBean.getMin_temp_y()));
                            }
                        }
                        for (int z = 0; z < mRectDraw.getRectViewList().size(); z ++) {
                            if (mRectDraw.getRectViewList().get(z).getId().equals(tempResultBean.getId())) {
                                mRectDraw.getRectViewList().get(z).setHighTempPoint(new Point(tempResultBean.getMax_temp_x(), tempResultBean.getMax_temp_y()));
                                mRectDraw.getRectViewList().get(z).setLowTempPoint(new Point(tempResultBean.getMin_temp_x(), tempResultBean.getMin_temp_y()));
                            }
                        }
                    }
                    mPointDraw.onDraw(canvas, false);
                    mLineDraw.onDraw(canvas, false);
                    mRectDraw.onDraw(canvas, false);
                    drawTempData(mContext, mLineDraw.mScreenDegree, canvas, tempResultBeans);
                } else {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "DrawThread InterruptedException :" + e.getMessage());
        } finally {
            if (canvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /**
     * 绘制手势操作过程
     */
    private void doTouchDraw() {
        if (mSurfaceHolder == null || !mCanDraw) {
            return;
        }
        Canvas canvas = null;

        try {
            synchronized (mCanvasLock) {
                canvas = mSurfaceHolder.lockCanvas();

                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                switch (mDrawModel) {
                    case DRAW_POINT:
                        if (mPointDraw.getOperateStatus() == PointDraw.OPERATE_STATUS_POINT_ADD) {
                            mPointDraw.onTempDraw(canvas, 1, mCurX, mCurY);
                        }

                        break;
                    case DRAW_LINE:
                        if (mLineDraw.getOperateStatus() == LineDraw.OPERATE_STATUS_LINE_ADD) {
                            mLineDraw.onTempDraw(canvas, (int)mFirstX, (int)mFirstY, (int)mCurX, (int)mCurY);
                        }

                        break;
                    case DRAW_RECT:
                        if (mRectDraw.getOperateStatus() == RectDraw.OPERATE_STATUS_RECTANGLE_STATUS_ADD) {
                            mRectDraw.onTempDraw(canvas, (int)mFirstX, (int)mFirstY, (int)mCurX, (int)mCurY);
                        }

                        break;
                }
                mPointDraw.onDraw(canvas, true);
                mLineDraw.onDraw(canvas, true);
                mRectDraw.onDraw(canvas, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "DrawThread InterruptedException :" + e.getMessage());
        } finally {
            if (canvas != null) {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }


    public abstract CopyOnWriteArrayList<TempResultBean> generateViewData(LinkedList<PointDraw.PointView> pointViews,
                                          LinkedList<LineDraw.LineView> lineViews,
                                          LinkedList<RectDraw.RectView> rectViews);


    /**
     * 绘制温度数据
     * @param context
     * @param screenDegree
     * @param canvas
     * @param tempResultBean
     */
    private void drawTempData(Context context, int screenDegree, Canvas canvas, CopyOnWriteArrayList<TempResultBean> tempResultBean) {
        if (tempResultBean.size() <= 0) {
            return;
        }
//        Log.d(TAG, "drawTempResultBean size : " + tempResultBean.size());
        int y = 10;
        int x = 10;

        if (screenDegree == 90) {
            x = mViewWidth - 10;
            y = 10;
        } else if (screenDegree == 270) {
            x = 10;
            y = mViewHeight - 10;
        } else if (screenDegree == 180) {
            x = mViewWidth - 10;
            y = mViewHeight - 10;
        }

        int interval = ScreenUtils.dp2px(mTextWidth + 10);
        int count = 0;
        int startIndex = 0;
        int pointCount = 0;
        for (int i = 0; i < tempResultBean.size(); i++) {

            TempResultBean result = tempResultBean.get(i);
            StringBuffer stringBuffer = new StringBuffer();
            if (result.getLabel().contains("P")) {
                pointCount ++;
                stringBuffer.append(result.getLabel()).append(result.getContent()).append("\n").append(context.getString(R.string.temp_label)).append(result.getMaxTemperature()).append("\n");
            } else {
                stringBuffer.append(result.getLabel()).append(result.getContent()).append("\n").append(context.getString(R.string.temp_max)).append(result.getMaxTemperature()).append("\n").append(context.getString(R.string.temp_avg)).append(result.getAverageTemperature()).append("\n").append(context.getString(R.string.temp_min)).append(result.getMinTemperature()).append("\n");
            }

            StaticLayout layout = new StaticLayout(stringBuffer.toString(), mTextPaint, interval, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false);
            canvas.save();
            if (count != 0 && count % 3 == 0) {
                startIndex = 0;
                if (pointCount == 3 && count == 3) {
                    y += ScreenUtils.dp2px(40);
                } else {
                    y += ScreenUtils.dp2px(80);
                }
            }
            canvas.translate(x + interval * startIndex, y);
            canvas.rotate(screenDegree);
            layout.draw(canvas);
            canvas.restore();
            count ++;
            startIndex ++;
        }
    }
}
