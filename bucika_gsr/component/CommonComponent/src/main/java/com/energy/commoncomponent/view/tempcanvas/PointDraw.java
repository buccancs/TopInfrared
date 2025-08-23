package com.energy.commoncomponent.view.tempcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
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

public class PointDraw extends BaseDraw {

    private static final String TAG = "BaseTemperatureView PointDraw";
    public static final int OPERATE_STATUS_POINT_IN_TOUCH = 0;
    public static final int OPERATE_STATUS_POINT_ADD = 1;
    public static final int OPERATE_STATUS_POINT_REMOVE = 2;

    private static final int MAX_POINT_COUNT = 3;

    private int TEXT_POINT_MARGIN;

    private int LABEL_POINT_MARGIN;

    private LinkedList<PointView> mPointList;

    private PointView mTempPoint;

    private Paint mTextPaint;
    private Paint mBgPaint;
    private Paint.FontMetrics mFontMetrics;

    private int mBgStrokeColor = Color.parseColor("#99000000");
    private int mBgColor = Color.parseColor("#CC1A1A1A");

    private final int STROKE_WIDTH = 8;
    private final int TEXT_SIZE = 14;

    private int mOperateStatus = -1;

    public PointDraw(Context context) {
        super(context);
        mPointList = new LinkedList<>();
        TEXT_POINT_MARGIN = ScreenUtils.dp2px(4);
        LABEL_POINT_MARGIN = ScreenUtils.dp2px(24);

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

    public void addPoint(int mode, float centerX, float centerY) {
        PointView pointView = new PointView(mContext, mode, centerX, centerY);
        int size = mPointList.size();
        if (mPointList.size() < MAX_POINT_COUNT) {
            Log.d(TAG, "addPoint");

            String newLabel = "P" + (size + 1);
            boolean hasSame = false;
            for (int i = 0; i < mPointList.size(); i ++) {
                if (mPointList.get(i).getLabel().equals(newLabel)) {
                    hasSame = true;
                    Log.d(TAG, "addPoint is same");
                    break;
                }
            }

            if (hasSame) {
                mPointList.add(pointView);
                for (int i = 0; i < mPointList.size(); i ++) {
                    mPointList.get(i).setLabel("P" + (i + 1));
                }
            } else {
                pointView.setLabel(newLabel);
                mPointList.add(pointView);
            }

            mTouchIndex = size;
        } else {
            Log.d(TAG, "point remove and add");
            mPointList.remove();
            mPointList.add(pointView);
            for (int i = 0; i < mPointList.size(); i ++) {
                mPointList.get(i).setLabel("P" + (i + 1));
            }
            mTouchIndex = MAX_POINT_COUNT - 1;
        }
    }

    public void removePoint(int index) {
        if (mPointList.size() > index) {
            mPointList.remove(index);
        }
    }

    public void removePoint() {
        mPointList.clear();
    }

    @Override
    public void onDraw(Canvas canvas, boolean isScroll) {
        for (int i = 0; i < mPointList.size(); i ++) {
            PointView pointView = mPointList.get(i);
            drawLabel(canvas, pointView);
            canvas.drawBitmap(pointView.mPointBitmap, pointView.mCenterX - pointView.mPointSize / 2, pointView.mCenterY - pointView.mPointSize / 2, null);
            if (!isScroll) {
                if (pointView.getTempPoint() != null) {
                    canvas.drawBitmap(pointView.mPointBitmap, pointView.getTempPoint().x, pointView.getTempPoint().y, null);
                }
            }
        }
    }

    public void onTempDraw(Canvas canvas, int mode, float centerX, float centerY) {
        if (mTempPoint == null) {
            mTempPoint = new PointView(mContext, mode, centerX, centerY);
            mTempPoint.setLabel("P");
        } else {
            mTempPoint.changeLocation(centerX, centerY);
        }
        drawLabel(canvas, mTempPoint);
        canvas.drawBitmap(mTempPoint.mPointBitmap, mTempPoint.mCenterX - mTempPoint.mPointSize / 2, mTempPoint.mCenterY - mTempPoint.mPointSize / 2, null);
    }

    private void drawLabel(Canvas canvas, PointView pointView) {
        canvas.save();
        canvas.rotate(mScreenDegree, pointView.mCenterX + TEXT_POINT_MARGIN, pointView.mCenterY - TEXT_POINT_MARGIN);
        RectF tempRectF = new RectF();
        float top = pointView.mCenterY - TEXT_POINT_MARGIN - LABEL_POINT_MARGIN - pointView.mPointSize / 2;
        float bottom = pointView.mCenterY - TEXT_POINT_MARGIN - pointView.mPointSize / 2;
        if (top < 0) {
            top = pointView.mCenterY + TEXT_POINT_MARGIN + pointView.mPointSize / 2;
            bottom = top + LABEL_POINT_MARGIN;
        }
        tempRectF.top = top;
        tempRectF.bottom = bottom;
        tempRectF.left = pointView.mCenterX;
        tempRectF.right = pointView.mCenterX;

        drawCustomTextBg(canvas, pointView.getLabel(), tempRectF);

        canvas.restore();
    }

    private RectF drawCustomTextBg(Canvas canvas, String text, RectF rectF) {
        int rectWidth = (int) mTextPaint.measureText(text) + TEXT_POINT_MARGIN * 2;
        float left = rectF.left - rectWidth / 2;
        float right = rectF.right + rectWidth / 2;
        float top = rectF.top;
        float bottom = rectF.bottom;
        if (left < 0) {
            left = 0;
            right = rectWidth;
        }
        if (right > mViewWidth) {
            left = mViewWidth - rectWidth;
            right = mViewWidth;
        }
        if (top < 0) {
            top = 0;
            bottom = LABEL_POINT_MARGIN;
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

    public void changeTouchPointLocationByIndex(int touchIndex, float centerX, float centerY) {
        if (touchIndex < 0 || touchIndex >= mPointList.size()) {
            return;
        }
        mPointList.get(touchIndex).changeLocation(centerX, centerY);
    }

    public int checkTouchPointInclude(float rawX, float rawY) {
        mTouchIndex = -1;
        for (int i = 0; i < mPointList.size(); i ++) {
            PointView pointView = mPointList.get(i);

            if (pointView.mInRect.contains((int) rawX, (int) rawY)) {
                mTouchIndex = i;
                return i;
            }
        }
        return mTouchIndex;
    }

    public static class PointView extends BaseView {

        private static final float TOUCH_EXTRA = 20;

        private int mMode;
        private float mCenterX;
        private float mCenterY;

        private Rect mInRect;
        private Bitmap mPointBitmap;
        private Point mTempPoint;

        public PointView(Context context, int mode, float centerX, float centerY) {
            mId = UUID.randomUUID().toString();
            mPointSize = ScreenUtils.dp2px(20f);
            mCenterX = centerX;
            mCenterY = centerY;
            mMode = mode;
            switch (mMode) {
                case 1:
                    mPointBitmap = getCustomSizeImg(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_point_green), mPointSize, mPointSize);
                    break;
                case 2:
                    mPointBitmap = getCustomSizeImg(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_point_blue), mPointSize, mPointSize);
                    break;
                case 3:
                    mPointBitmap = getCustomSizeImg(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_point_red), mPointSize, mPointSize);
                    break;
            }

            mInRect = new Rect();
            mInRect.left = (int)(mCenterX - mPointSize / 2 - TOUCH_EXTRA);
            mInRect.right = (int)(mCenterX + mPointSize / 2 + TOUCH_EXTRA);
            mInRect.top = (int)(mCenterY - mPointSize / 2 - TOUCH_EXTRA);
            mInRect.bottom = (int)(mCenterY + mPointSize / 2 + TOUCH_EXTRA);
        }

        public void changeLocation(float centerX, float centerY) {
            mCenterX = centerX;
            mCenterY = centerY;
            mInRect.left = (int)(mCenterX - mPointSize / 2 - TOUCH_EXTRA);
            mInRect.right = (int)(mCenterX + mPointSize / 2 + TOUCH_EXTRA);
            mInRect.top = (int)(mCenterY - mPointSize / 2 - TOUCH_EXTRA);
            mInRect.bottom = (int)(mCenterY + mPointSize / 2 + TOUCH_EXTRA);
        }

        public float getCenterX() {
            return mCenterX;
        }

        public void setCenterX(float mCenterX) {
            this.mCenterX = mCenterX;
        }

        public float getCenterY() {
            return mCenterY;
        }

        public void setCenterY(float mCenterY) {
            this.mCenterY = mCenterY;
        }

        public Point getTempPoint() {
            return mTempPoint;
        }

        public void setTempPoint(Point mTempPoint) {
            this.mTempPoint = mTempPoint;
        }
    }

    public LinkedList<PointView> getPointViewList() {
        return mPointList;
    }

}
