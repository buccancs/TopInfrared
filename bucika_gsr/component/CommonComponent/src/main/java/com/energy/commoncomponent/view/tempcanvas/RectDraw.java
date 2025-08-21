package com.energy.commoncomponent.view.tempcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;

import com.energy.commoncomponent.R;
import com.energy.commoncomponent.utils.ScreenUtils;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Created by fengjibo on 2024/2/1.
 */
public class RectDraw extends BaseDraw {
    private static final String TAG = "BaseTemperatureView RectDraw";

    private static final int MAX_RECT_COUNT = 3;
    public static final int OPERATE_STATUS_RECTANGLE_LEFT_TOP_CORNER = 0;
    public static final int OPERATE_STATUS_RECTANGLE_RIGHT_TOP_CORNER = 1;
    public static final int OPERATE_STATUS_RECTANGLE_RIGHT_BOTTOM_CORNER = 2;
    public static final int OPERATE_STATUS_RECTANGLE_LEFT_BOTTOM_CORNER = 3;

    public static final int OPERATE_STATUS_RECTANGLE_LEFT_EDGE = 4;
    public static final int OPERATE_STATUS_RECTANGLE_TOP_EDGE = 5;
    public static final int OPERATE_STATUS_RECTANGLE_RIGHT_EDGE = 6;
    public static final int OPERATE_STATUS_RECTANGLE_BOTTOM_EDGE = 7;

    public static final int OPERATE_STATUS_RECTANGLE_MOVE_ENTIRE = 8;
    public static final int OPERATE_STATUS_RECTANGLE_STATUS_ADD = 9;
    public static final int OPERATE_STATUS_RECTANGLE_STATUS_REMOVE = 10;

    private LinkedList<RectView> mRectList;
    private Paint mRectPaint;
    private int LINE_STROKE_WIDTH;

    private Paint mBgPaint;
    private Paint.FontMetrics mFontMetrics;
    private Paint mTextPaint;
    private int mBgStrokeColor = Color.parseColor("#99000000");
    private int mBgColor = Color.parseColor("#CC1A1A1A");

    private final int STROKE_WIDTH = 8;
    private final int TEXT_SIZE = 14; // 文字大小
    private final int TOUCH_TOLERANCE = 48;
    private RectView mTempRect;

    private int mOperateStatus = -1;

    private final static int PIXCOUNT = 8;

    public RectDraw(Context context) {
        super(context);
        LINE_STROKE_WIDTH = ScreenUtils.dp2px(1);
        mRectList = new LinkedList<>();

        mRectPaint = new Paint();
        mRectPaint.setColor(Color.WHITE);
        mRectPaint.setStrokeWidth(LINE_STROKE_WIDTH);
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStrokeJoin(Paint.Join.ROUND);
        mRectPaint.setStrokeCap(Paint.Cap.ROUND);
        mRectPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setStrokeWidth(ScreenUtils.dp2px(STROKE_WIDTH));
        mTextPaint.setTextSize(ScreenUtils.sp2px(TEXT_SIZE));
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mBgPaint = new TextPaint();
        mBgPaint.setStrokeWidth(ScreenUtils.dp2px(1));
    }

    public int getOperateStatus() {
        return mOperateStatus;
    }

    public void setOperateStatus(int mOperateStatus) {
        this.mOperateStatus = mOperateStatus;
        Log.d(TAG, "setOperateStatus = " + mOperateStatus);
    }

    /**
     * 添加一个矩形数据
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     */
    public void addRect(int startX, int startY, int endX, int endY) {
        if (Math.abs(endX - startX) > TOUCH_TOLERANCE || Math.abs(endY - startY) > TOUCH_TOLERANCE) {
            RectView rectView = new RectView(mContext, startX, startY, endX, endY);
            int size = mRectList.size();
            if (mRectList.size() < MAX_RECT_COUNT) {

                String newLabel = "R" + (size + 1);
                Log.d(TAG, "addRect newLabel : " + newLabel);
                boolean hasSame = false;
                for (int i = 0; i < mRectList.size(); i ++) {
                    if (mRectList.get(i).getLabel().equals(newLabel)) {
                        //存在一样的
                        hasSame = true;
                        Log.d(TAG, "addRect is same");
                        break;
                    }
                }

                if (hasSame) {
                    mRectList.add(rectView);
                    for (int i = 0; i < mRectList.size(); i ++) {
                        mRectList.get(i).setLabel("R" + (i + 1));
                    }
                } else {
                    rectView.setLabel(newLabel);
                    mRectList.add(rectView);
                }

                mTouchIndex = size;
            } else {
                Log.d(TAG, "Rect remove and add");
                mRectList.remove();
                mRectList.add(rectView);
                for (int i = 0; i < mRectList.size(); i ++) {
                    mRectList.get(i).setLabel("R" + (i + 1));
                }
                mTouchIndex = MAX_RECT_COUNT - 1;
            }
        }
    }

    /**
     * 删除一个矩形数据
     * @param index
     */
    public void removeRect(int index) {
        if (mRectList.size() > index) {
            mRectList.remove(index);
        }
    }

    /**
     * 删除所有框数据
     */
    public void removeRect() {
        mRectList.clear();
    }


    /**
     * 更新选中框的手势位置状态
     * @param startX
     * @param startY
     */
    public void changeTouchRectOperateStatus(float startX, float startY) {
        if (mTouchIndex < 0 || mTouchIndex >= mRectList.size()) {
            return;
        }
        RectView rectView = mRectList.get(mTouchIndex);
        if (startX > rectView.mMovingLeft - TOUCH_TOLERANCE && startX < rectView.mMovingLeft + TOUCH_TOLERANCE && startY > rectView.mMovingTop - TOUCH_TOLERANCE && startY < rectView.mMovingTop + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_RECTANGLE_LEFT_TOP_CORNER);
        } else if (startX > rectView.mMovingRight - TOUCH_TOLERANCE && startX < rectView.mMovingRight + TOUCH_TOLERANCE && startY > rectView.mMovingTop - TOUCH_TOLERANCE && startY < rectView.mMovingTop + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_RECTANGLE_RIGHT_TOP_CORNER);
        } else if (startX > rectView.mMovingRight - TOUCH_TOLERANCE && startX < rectView.mMovingRight + TOUCH_TOLERANCE && startY > rectView.mMovingBottom - TOUCH_TOLERANCE && startY < rectView.mMovingBottom + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_RECTANGLE_RIGHT_BOTTOM_CORNER);
        } else if (startX > rectView.mMovingLeft - TOUCH_TOLERANCE && startX < rectView.mMovingLeft + TOUCH_TOLERANCE && startY > rectView.mMovingBottom - TOUCH_TOLERANCE && startY < rectView.mMovingBottom + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_RECTANGLE_LEFT_BOTTOM_CORNER);
        } else if (startX > rectView.mMovingLeft - TOUCH_TOLERANCE && startX < rectView.mMovingLeft + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_RECTANGLE_LEFT_EDGE);
        } else if (startY > rectView.mMovingTop - TOUCH_TOLERANCE && startY < rectView.mMovingTop + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_RECTANGLE_TOP_EDGE);
        } else if (startX > rectView.mMovingRight - TOUCH_TOLERANCE && startX < rectView.mMovingRight + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_RECTANGLE_RIGHT_EDGE);
        } else if (startY > rectView.mMovingBottom - TOUCH_TOLERANCE && startY < rectView.mMovingBottom + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_RECTANGLE_BOTTOM_EDGE);
        } else {
            setOperateStatus(OPERATE_STATUS_RECTANGLE_MOVE_ENTIRE);
        }
    }

    /**
     * 修改选中的框坐标
     * @param touchIndex
     * @param moveX
     * @param moveY
     */
    public void changeTouchLineLocationByIndex(int touchIndex, float moveX, float moveY) {
        if (touchIndex < 0 || touchIndex >= mRectList.size()) {
            return;
        }
        int tmpLeft, tmpTop, tmpRight, tmpBottom;
        int tmp;
        RectView rectView = mRectList.get(touchIndex);
        float rectLeft = rectView.mRect.left + moveX < 0 ? 0 : rectView.mRect.left + moveX;
        float rectTop = rectView.mRect.top + moveY < 0 ? 0 : rectView.mRect.top + moveY;
        float rectRight = rectView.mRect.right + moveX > mViewWidth ? mViewWidth : rectView.mRect.right + moveX;
        float rectBottom = rectView.mRect.bottom + moveY > mViewHeight ? mViewHeight : rectView.mRect.bottom + moveY;

        if (mOperateStatus == OPERATE_STATUS_RECTANGLE_MOVE_ENTIRE) {
            mRectList.get(touchIndex).changeLocation((int)rectLeft, (int)rectTop, (int)rectRight, (int)rectBottom);
        }
        if (mOperateStatus == OPERATE_STATUS_RECTANGLE_LEFT_EDGE) {
            if (rectLeft == rectRight) {
                rectLeft -= PIXCOUNT;
            }
            if (rectView.mMovingRight < rectLeft) {
                mRectList.get(touchIndex).changeLocation(rectView.mMovingRight, rectView.mMovingTop, (int)rectLeft, rectView.mMovingBottom);
            } else {
                mRectList.get(touchIndex).changeLocation((int)rectLeft, rectView.mMovingTop, rectView.mMovingRight, rectView.mMovingBottom);
            }
        }
        if (mOperateStatus == OPERATE_STATUS_RECTANGLE_TOP_EDGE) {
            if (rectTop == rectBottom) {
                rectTop -= PIXCOUNT;
            }

            if (rectView.mMovingBottom < rectView.mMovingLeft) {
                mRectList.get(touchIndex).changeLocation(rectView.mMovingLeft, rectView.mMovingBottom, rectView.mMovingRight, (int)rectTop);
            } else {
                mRectList.get(touchIndex).changeLocation(rectView.mMovingLeft, (int)rectTop, rectView.mMovingRight, rectView.mMovingBottom);
            }
        }
        if (mOperateStatus == OPERATE_STATUS_RECTANGLE_RIGHT_EDGE) {
            if (rectLeft == rectRight) {
                rectRight += PIXCOUNT;
            }
            if (rectRight < rectView.mMovingLeft) {
                mRectList.get(touchIndex).changeLocation((int)rectRight, rectView.mMovingTop, rectView.mMovingLeft, rectView.mMovingBottom);
            } else {
                mRectList.get(touchIndex).changeLocation(rectView.mMovingLeft, rectView.mMovingTop, (int)rectRight, rectView.mMovingBottom);
            }
        }
        if (mOperateStatus == OPERATE_STATUS_RECTANGLE_BOTTOM_EDGE) {
            if (rectTop == rectBottom) {
                rectBottom += PIXCOUNT;
            }
            if (rectBottom < rectView.mMovingTop) {
                mRectList.get(touchIndex).changeLocation(rectView.mMovingLeft, (int)rectBottom, rectView.mMovingRight, rectView.mRect.top);
            } else {
                mRectList.get(touchIndex).changeLocation(rectView.mMovingLeft, rectView.mRect.top, rectView.mMovingRight, (int)rectBottom);
            }
        }
        if (mOperateStatus == OPERATE_STATUS_RECTANGLE_LEFT_TOP_CORNER) {
            tmpLeft = (int)rectLeft;
            tmpRight = rectView.mMovingRight;
            if (rectView.mMovingRight < rectLeft) {
                tmp = tmpLeft;
                tmpLeft = tmpRight;
                tmpRight = tmp;
            }
            tmpTop = (int) rectTop;
            tmpBottom = rectView.mMovingBottom;
            if (rectView.mMovingBottom < rectTop) {
                tmp = tmpBottom;
                tmpBottom = tmpTop;
                tmpTop = tmp;
            }
            mRectList.get(touchIndex).changeLocation(tmpLeft, tmpTop, tmpRight, tmpBottom);
        }
        if (mOperateStatus == OPERATE_STATUS_RECTANGLE_RIGHT_TOP_CORNER) {
            tmpLeft = rectView.mMovingLeft;
            tmpRight = (int)rectRight;
            if (rectRight < rectView.mMovingLeft) {
                tmp = tmpLeft;
                tmpLeft = tmpRight;
                tmpRight = tmp;
            }
            tmpTop = (int) rectTop;
            tmpBottom = rectView.mMovingBottom;
            if (rectView.mMovingBottom < rectTop) {
                tmp = tmpBottom;
                tmpBottom = tmpTop;
                tmpTop = tmp;
            }

            mRectList.get(touchIndex).changeLocation(tmpLeft, tmpTop, tmpRight, tmpBottom);
        }
        if (mOperateStatus == OPERATE_STATUS_RECTANGLE_RIGHT_BOTTOM_CORNER) {
            tmpLeft = rectView.mMovingLeft;
            tmpRight = (int)rectRight;

            if (rectRight < rectView.mMovingLeft) {
                tmp = tmpLeft;
                tmpLeft = tmpRight;
                tmpRight = tmp;
            }
            tmpTop = rectView.mMovingTop;
            tmpBottom = (int) rectBottom;
            if (rectBottom < rectView.mMovingTop) {
                tmp = tmpBottom;
                tmpBottom = tmpTop;
                tmpTop = tmp;
            }
            mRectList.get(touchIndex).changeLocation(tmpLeft, tmpTop, tmpRight, tmpBottom);
        }
        if (mOperateStatus == OPERATE_STATUS_RECTANGLE_LEFT_BOTTOM_CORNER) {

            tmpLeft = (int) rectLeft;
            tmpRight = rectView.mMovingRight;
            if (rectView.mMovingRight < rectLeft) {
                tmp = tmpLeft;
                tmpLeft = tmpRight;
                tmpRight = tmp;
            }
            tmpTop = rectView.mMovingTop;
            tmpBottom = (int) rectBottom;
            if (rectView.mMovingBottom < rectTop) {
                tmp = tmpBottom;
                tmpBottom = tmpTop;
                tmpTop = tmp;
            }
            mRectList.get(touchIndex).changeLocation(tmpLeft, tmpTop, tmpRight, tmpBottom);
        }
    }

    @Override
    void onDraw(Canvas canvas, boolean isScroll) {
        for (int i = 0; i < mRectList.size(); i ++) {
            RectView rectView = mRectList.get(i);

            drawLabel(canvas, rectView);
            canvas.drawRect(rectView.mMovingLeft, rectView.mMovingTop, rectView.mMovingRight, rectView.mMovingBottom, mRectPaint);

            if (!isScroll) {
                if (rectView.getHighTempPoint() != null) {
                    canvas.drawBitmap(rectView.mHighPointBitmap, rectView.getHighTempPoint().x, rectView.getHighTempPoint().y, null);
                }
                if (rectView.getLowTempPoint() != null) {
                    canvas.drawBitmap(rectView.mLowPointBitmap, rectView.getLowTempPoint().x, rectView.getLowTempPoint().y, null);
                }
            }
        }
    }

    /**
     * 绘制临时点
     * @param canvas
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     */
    public void onTempDraw(Canvas canvas, int startX, int startY, int endX, int endY) {
        if (mTempRect == null) {
            mTempRect = new RectView(mContext, startX, startY, endX, endY);
            mTempRect.setLabel("R");
        } else {
            mTempRect.changeLocation(startX, startY, endX, endY);
            mTempRect.changeRectLocation();
        }
        drawLabel(canvas, mTempRect);
        canvas.drawRect(mTempRect.mMovingLeft, mTempRect.mMovingTop, mTempRect.mMovingRight, mTempRect.mMovingBottom, mRectPaint);
    }

    /**
     * 检查当前是否存在手势选中的框
     * @param x
     * @param y
     * @return
     */
    public int checkTouchRectInclude(int x, int y) {
        mTouchIndex = -1;
        for (int i = 0; i < mRectList.size(); i ++) {
            RectView rectView = mRectList.get(i);

            if (rectView.mRect.contains(x, y)) {
                mTouchIndex = i;
                return i;
            }
        }
        return mTouchIndex;

    }

    public void changeTouchRectLocation() {
        if (mTouchIndex < 0 || mTouchIndex >= mRectList.size()) {
            return;
        }
        mRectList.get(mTouchIndex).changeRectLocation();
    }

    private void drawLabel(Canvas canvas, RectView rectView) {
        canvas.save();
        canvas.rotate(mScreenDegree, rectView.mMovingLeft + (rectView.mMovingRight - rectView.mMovingLeft) / 2,
                rectView.mMovingTop + (rectView.mMovingBottom - rectView.mMovingTop) / 2);

        //label中心点
        RectF tempRectF = new RectF();

        tempRectF.top = rectView.mMovingTop + (float) (rectView.mMovingBottom - rectView.mMovingTop) / 2;
        tempRectF.bottom = rectView.mMovingTop + (float) (rectView.mMovingBottom - rectView.mMovingTop) / 2;;
        tempRectF.left = rectView.mMovingLeft + (float) (rectView.mMovingRight - rectView.mMovingLeft) / 2;
        tempRectF.right = rectView.mMovingLeft + (float) (rectView.mMovingRight - rectView.mMovingLeft) / 2;

        drawCustomTextBg(canvas, rectView.getLabel(), tempRectF);
        canvas.restore();
    }

    private RectF drawCustomTextBg(Canvas canvas, String text, RectF rectF) {
        int rectWidth = (int) mTextPaint.measureText(text) * 2;
        float left = rectF.left - rectWidth / 2;
        float right = rectF.right + rectWidth / 2;
        float top = rectF.top;
        float bottom = rectF.bottom;
        //左侧超出
        if (left < 0) {
            left = 0;
            right = rectWidth;
        }
        //右侧超出
        if (right > mViewWidth) {
            left = mViewWidth - rectWidth;
            right = mViewWidth;
        }
        if (top < 0) {
            top = 0;
            bottom = 0;
        }
        rectF.left = left;
        rectF.right = right;
        rectF.top = top;
        rectF.bottom = bottom;
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(mBgStrokeColor);
        canvas.drawRect(rectF, mBgPaint);
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setColor(mBgColor);
        canvas.drawRect(rectF, mBgPaint);

        mFontMetrics = mTextPaint.getFontMetrics();
        float topMetrics = mFontMetrics.top;
        float bottomMetrics = mFontMetrics.bottom;
        int baseLineY = (int) (rectF.centerY() - topMetrics / 2 - bottomMetrics / 2);
        canvas.drawText(text, rectF.centerX(), baseLineY, mTextPaint);
        return rectF;
    }

    public static class RectView extends BaseView {
        private Rect mRect;
        private static final float TOUCH_EXTRA = 10;//额外的触摸范围
        private Bitmap mHighPointBitmap;
        private Bitmap mLowPointBitmap;
        private Point mHighTempPoint;
        private Point mLowTempPoint;

        private int mMovingTop;
        private int mMovingBottom;
        private int mMovingLeft;
        private int mMovingRight;

        public RectView(Context context, int startX, int startY, int endX, int endY) {
            mPointSize = ScreenUtils.dp2px(20f);
            mId = UUID.randomUUID().toString();
            mRect = new Rect();
            mRect.left = startX;
            mRect.right = endX;
            mRect.top = startY;
            mRect.bottom = endY;
            mMovingLeft = startX;
            mMovingTop = startY;
            mMovingRight = endX;
            mMovingBottom = endY;
            mHighPointBitmap = getCustomSizeImg(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_point_red), mPointSize, mPointSize);
            mLowPointBitmap = getCustomSizeImg(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_point_green), mPointSize, mPointSize);
        }

        public void changeLocation(int left, int top, int right, int bottom) {
            mMovingTop = top;
            mMovingLeft = left;
            mMovingRight = right;
            mMovingBottom = bottom;
        }

        public void changeRectLocation() {
            mRect.top = mMovingTop;
            mRect.left = mMovingLeft;
            mRect.bottom = mMovingBottom;
            mRect.right = mMovingRight;
        }

        public Point getHighTempPoint() {
            return mHighTempPoint;
        }

        public void setHighTempPoint(Point mHighTempPoint) {
            this.mHighTempPoint = mHighTempPoint;
        }

        public Point getLowTempPoint() {
            return mLowTempPoint;
        }

        public void setLowTempPoint(Point mLowTempPoint) {
            this.mLowTempPoint = mLowTempPoint;
        }

        public int getMovingTop() {
            return mMovingTop;
        }

        public void setMovingTop(int mMovingTop) {
            this.mMovingTop = mMovingTop;
        }

        public int getMovingBottom() {
            return mMovingBottom;
        }

        public void setMovingBottom(int mMovingBottom) {
            this.mMovingBottom = mMovingBottom;
        }

        public int getMovingLeft() {
            return mMovingLeft;
        }

        public void setMovingLeft(int mMovingLeft) {
            this.mMovingLeft = mMovingLeft;
        }

        public int getMovingRight() {
            return mMovingRight;
        }

        public void setMovingRight(int mMovingRight) {
            this.mMovingRight = mMovingRight;
        }
    }

    public LinkedList<RectView> getRectViewList() {
        return mRectList;
    }
}
