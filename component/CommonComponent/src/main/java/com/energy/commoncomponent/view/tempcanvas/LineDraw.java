package com.energy.commoncomponent.view.tempcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.Log;
import com.energy.commoncomponent.R;
import com.energy.commoncomponent.utils.ScreenUtils;
import java.util.LinkedList;
import java.util.UUID;

public class LineDraw extends BaseDraw {
    private static final String TAG = "BaseTemperatureView LineDraw";
    public static final int OPERATE_STATUS_LINE_IN_TOUCH_START = 0;
    public static final int OPERATE_STATUS_LINE_IN_TOUCH_CENTER = 1;
    public static final int OPERATE_STATUS_LINE_IN_TOUCH_END = 2;
    public static final int OPERATE_STATUS_LINE_ADD = 3;
    public static final int OPERATE_STATUS_LINE_REMOVE = 4;
    private static final int MAX_LINE_COUNT = 3;

    private LinkedList<LineView> mLineList;
    private Paint mLinePaint;
    private int LINE_STROKE_WIDTH;

    private Paint mBgPaint;
    private Paint.FontMetrics mFontMetrics;
    private Paint mTextPaint;

    private int mBgStrokeColor = Color.parseColor("#99000000");
    private int mBgColor = Color.parseColor("#CC1A1A1A");

    private final int STROKE_WIDTH = 8;
    private final int TEXT_SIZE = 14;
    private final int TOUCH_TOLERANCE = 48;

    private LineView mTempLine;

    private int mOperateStatus = -1;

    public LineDraw(Context context) {
        super(context);
        LINE_STROKE_WIDTH = ScreenUtils.dp2px(1);
        mLineList = new LinkedList<>();

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStrokeWidth(LINE_STROKE_WIDTH);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStyle(Paint.Style.STROKE);

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

    public void addLine(int startX, int startY, int endX, int endY) {
        if (Math.abs(endX - startX) > TOUCH_TOLERANCE || Math.abs(endY - startY) > TOUCH_TOLERANCE) {
            LineView lineView = new LineView(mContext, startX, startY, endX, endY);
            int size = mLineList.size();
            if (mLineList.size() < MAX_LINE_COUNT) {

                String newLabel = "L" + (size + 1);
                Log.d(TAG, "addLine newLabel : " + newLabel);
                boolean hasSame = false;
                for (int i = 0; i < mLineList.size(); i ++) {
                    if (mLineList.get(i).getLabel().equals(newLabel)) {
                        hasSame = true;
                        Log.d(TAG, "addLine is same");
                        break;
                    }
                }

                if (hasSame) {
                    mLineList.add(lineView);
                    for (int i = 0; i < mLineList.size(); i ++) {
                        mLineList.get(i).setLabel("L" + (i + 1));
                    }
                } else {
                    lineView.setLabel(newLabel);
                    mLineList.add(lineView);
                }

                mTouchIndex = size;
            } else {
                Log.d(TAG, "line remove and add");
                mLineList.remove();
                mLineList.add(lineView);
                for (int i = 0; i < mLineList.size(); i ++) {
                    mLineList.get(i).setLabel("L" + (i + 1));
                }
                mTouchIndex = MAX_LINE_COUNT - 1;
            }
        }
    }

    public void removeLine(int index) {
        if (mLineList.size() > index) {
            mLineList.remove(index);
        }
    }

    public void removeLine() {
        mLineList.clear();
    }

    @Override
    public void onDraw(Canvas canvas, boolean isScroll) {
        for (int i = 0; i < mLineList.size(); i ++) {
            LineView lineView = mLineList.get(i);
            drawLabel(canvas, lineView);
            canvas.drawLine(lineView.mStartMovingLineX, lineView.mStartMovingLineY, lineView.mEndMovingLineX, lineView.mEndMovingLineY, mLinePaint);
            if (!isScroll) {
                if (lineView.getHighTempPoint() != null) {
                    canvas.drawBitmap(lineView.mHighPointBitmap, lineView.getHighTempPoint().x, lineView.getHighTempPoint().y, null);
                }
                if (lineView.getLowTempPoint() != null) {
                    canvas.drawBitmap(lineView.mLowPointBitmap, lineView.getLowTempPoint().x, lineView.getLowTempPoint().y, null);
                }
            }
        }
    }

    public void onTempDraw(Canvas canvas, int startX, int startY, int endX, int endY) {
        if (mTempLine == null) {
            mTempLine = new LineView(mContext, startX, startY, endX, endY);
            mTempLine.setLabel("L");
        } else {
            mTempLine.changeLocation(startX, startY, endX, endY);
        }
        drawLabel(canvas, mTempLine);
        canvas.drawLine(mTempLine.mStartMovingLineX, mTempLine.mStartMovingLineY, mTempLine.mEndMovingLineX, mTempLine.mEndMovingLineY, mLinePaint);
    }

    private void drawLabel(Canvas canvas, LineView lineView) {
        canvas.save();
        canvas.rotate(mScreenDegree, lineView.mStartMovingLineX + (lineView.mEndMovingLineX - lineView.mStartMovingLineX) / 2,
                lineView.mStartMovingLineY + (lineView.mEndMovingLineY - lineView.mStartMovingLineY) / 2);
        RectF tempRectF = new RectF();

        tempRectF.top = lineView.mStartMovingLineY + (float) (lineView.mEndMovingLineY - lineView.mStartMovingLineY) / 2;
        tempRectF.bottom = lineView.mStartMovingLineY + (float) (lineView.mEndMovingLineY - lineView.mStartMovingLineY) / 2;;
        tempRectF.left = lineView.mStartMovingLineX + (float) (lineView.mEndMovingLineX - lineView.mStartMovingLineX) / 2;
        tempRectF.right = lineView.mStartMovingLineX + (float) (lineView.mEndMovingLineX - lineView.mStartMovingLineX) / 2;

        drawCustomTextBg(canvas, lineView.getLabel(), tempRectF);
        canvas.restore();
    }

    private RectF drawCustomTextBg(Canvas canvas, String text, RectF rectF) {
        int rectWidth = (int) mTextPaint.measureText(text) * 2;
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

    public void changeTouchLineOperateStatus(float startX, float startY) {
        if (mTouchIndex < 0 || mTouchIndex >= mLineList.size()) {
            return;
        }
        LineView lineView = mLineList.get(mTouchIndex);
        if (startX > lineView.mStartMovingLineX - TOUCH_TOLERANCE && startX < lineView.mStartMovingLineX + TOUCH_TOLERANCE && startY > lineView.mStartMovingLineY - TOUCH_TOLERANCE && startY < lineView.mStartMovingLineY + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_LINE_IN_TOUCH_START);
        } else if (startX > lineView.mEndMovingLineX - TOUCH_TOLERANCE && startX < lineView.mEndMovingLineX + TOUCH_TOLERANCE && startY > lineView.mEndMovingLineY - TOUCH_TOLERANCE && startY < lineView.mEndMovingLineY + TOUCH_TOLERANCE) {
            setOperateStatus(OPERATE_STATUS_LINE_IN_TOUCH_END);
        } else {
            setOperateStatus(OPERATE_STATUS_LINE_IN_TOUCH_CENTER);
        }

    }

    public void changeTouchLineLocationByIndex(float moveX, float moveY) {
        if (mTouchIndex < 0 || mTouchIndex >= mLineList.size()) {
            return;
        }
        Log.d(TAG, "mOperateStatus : " + mOperateStatus);

        LineView lineView = mLineList.get(mTouchIndex);
        if (mOperateStatus == OPERATE_STATUS_LINE_IN_TOUCH_START) {
            int startMovingLineX = (int)(lineView.mStartPoint.x + moveX);
            int startMovingLineY = (int)(lineView.mStartPoint.y + moveY);
            int endMovingLineX = (int)(lineView.mEndPoint.x);
            int endMovingLineY = (int)(lineView.mEndPoint.y);
            if (startMovingLineX < MIN_SIZE_PIX_COUNT) {
                startMovingLineX = MIN_SIZE_PIX_COUNT;
            } else if (startMovingLineX >= mViewWidth) {
                startMovingLineX = mViewWidth - MIN_SIZE_PIX_COUNT;
            }
            if (startMovingLineY < MIN_SIZE_PIX_COUNT) {
                startMovingLineY = MIN_SIZE_PIX_COUNT;
            } else if (startMovingLineY >= mViewHeight) {
                startMovingLineY = mViewHeight - MIN_SIZE_PIX_COUNT;
            }

            if (endMovingLineX < MIN_SIZE_PIX_COUNT) {
                endMovingLineX = MIN_SIZE_PIX_COUNT;
            } else if (endMovingLineX >= mViewWidth) {
                endMovingLineX = mViewWidth - MIN_SIZE_PIX_COUNT;
            }
            if (endMovingLineY < MIN_SIZE_PIX_COUNT) {
                endMovingLineY = MIN_SIZE_PIX_COUNT;
            } else if (endMovingLineY >= mViewHeight) {
                endMovingLineY = mViewHeight - MIN_SIZE_PIX_COUNT;
            }

            mLineList.get(mTouchIndex).changeLocation(startMovingLineX, startMovingLineY, endMovingLineX, endMovingLineY);
        } else if (mOperateStatus == OPERATE_STATUS_LINE_IN_TOUCH_END){
            int startMovingLineX = (int)(lineView.mStartPoint.x);
            int startMovingLineY = (int)(lineView.mStartPoint.y);
            int endMovingLineX = (int)(lineView.mEndPoint.x + moveX);
            int endMovingLineY = (int)(lineView.mEndPoint.y + moveY);

            if (startMovingLineX < MIN_SIZE_PIX_COUNT) {
                startMovingLineX = MIN_SIZE_PIX_COUNT;
            } else if (startMovingLineX >= mViewWidth) {
                startMovingLineX = mViewWidth - MIN_SIZE_PIX_COUNT;
            }
            if (startMovingLineY < MIN_SIZE_PIX_COUNT) {
                startMovingLineY = MIN_SIZE_PIX_COUNT;
            } else if (startMovingLineY >= mViewHeight) {
                startMovingLineY = mViewHeight - MIN_SIZE_PIX_COUNT;
            }

            if (endMovingLineX < MIN_SIZE_PIX_COUNT) {
                endMovingLineX = MIN_SIZE_PIX_COUNT;
            } else if (endMovingLineX >= mViewWidth) {
                endMovingLineX = mViewWidth - MIN_SIZE_PIX_COUNT;
            }
            if (endMovingLineY < MIN_SIZE_PIX_COUNT) {
                endMovingLineY = MIN_SIZE_PIX_COUNT;
            } else if (endMovingLineY >= mViewHeight) {
                endMovingLineY = mViewHeight - MIN_SIZE_PIX_COUNT;
            }

            mLineList.get(mTouchIndex).changeLocation(startMovingLineX, startMovingLineY, endMovingLineX, endMovingLineY);
        } else if (mOperateStatus == OPERATE_STATUS_LINE_IN_TOUCH_CENTER) {
            int startMovingLineX = (int)(lineView.mStartPoint.x + moveX);
            int startMovingLineY = (int)(lineView.mStartPoint.y + moveY);
            int endMovingLineX = (int)(lineView.mEndPoint.x + moveX);
            int endMovingLineY = (int)(lineView.mEndPoint.y + moveY);
            if (startMovingLineX < MIN_SIZE_PIX_COUNT) {
                startMovingLineX = MIN_SIZE_PIX_COUNT;
            } else if (startMovingLineX >= mViewWidth) {
                startMovingLineX = mViewWidth - MIN_SIZE_PIX_COUNT;
            }
            if (startMovingLineY < MIN_SIZE_PIX_COUNT) {
                startMovingLineY = MIN_SIZE_PIX_COUNT;
            } else if (startMovingLineY >= mViewHeight) {
                startMovingLineY = mViewHeight - MIN_SIZE_PIX_COUNT;
            }

            if (endMovingLineX < MIN_SIZE_PIX_COUNT) {
                endMovingLineX = MIN_SIZE_PIX_COUNT;
            } else if (endMovingLineX >= mViewWidth) {
                endMovingLineX = mViewWidth - MIN_SIZE_PIX_COUNT;
            }
            if (endMovingLineY < MIN_SIZE_PIX_COUNT) {
                endMovingLineY = MIN_SIZE_PIX_COUNT;
            } else if (endMovingLineY >= mViewHeight) {
                endMovingLineY = mViewHeight - MIN_SIZE_PIX_COUNT;
            }

            mLineList.get(mTouchIndex).changeLocation(startMovingLineX, startMovingLineY, endMovingLineX, endMovingLineY);
        }
    }

    public void changeTouchPointLocation() {
        if (mTouchIndex < 0 || mTouchIndex >= mLineList.size()) {
            return;
        }
        mLineList.get(mTouchIndex).changePointLocation();
    }

    public int checkTouchLineInclude(int x, int y) {
        mTouchIndex = -1;
        for (int i = 0; i < mLineList.size(); i ++) {
            LineView lineView = mLineList.get(i);

            int tempDistance = ((lineView.mEndMovingLineY - lineView.mStartMovingLineY) * x - (lineView.mEndMovingLineX - lineView.mStartMovingLineX) * y + lineView.mEndMovingLineX * lineView.mStartMovingLineY - lineView.mStartMovingLineX * lineView.mEndMovingLineY);
            tempDistance = (int) (tempDistance / Math.sqrt(Math.pow(lineView.mEndMovingLineY - lineView.mStartMovingLineY, 2) + Math.pow(lineView.mEndMovingLineX - lineView.mStartMovingLineX, 2)));
            if (Math.abs(tempDistance) < TOUCH_TOLERANCE && x > Math.min(lineView.mStartMovingLineX, lineView.mEndMovingLineX) - TOUCH_TOLERANCE && x < Math.max(lineView.mStartMovingLineX, lineView.mEndMovingLineX) + TOUCH_TOLERANCE) {
                mTouchIndex = i;
                Log.d(TAG, "checkTouchLineInclude true mTouchIndex = " + mTouchIndex);
                return i;
            }
        }
        return mTouchIndex;
    }

    public static class LineView extends BaseView {
        private Point mStartPoint;
        private Point mEndPoint;
        private static final float TOUCH_EXTRA = 10;
        private Bitmap mHighPointBitmap;
        private Bitmap mLowPointBitmap;
        private Point mHighTempPoint;
        private Point mLowTempPoint;

        private int mStartMovingLineX;
        private int mStartMovingLineY;
        private int mEndMovingLineX;
        private int mEndMovingLineY;

        public LineView(Context context, int startX, int startY, int endX, int endY) {
            mPointSize = ScreenUtils.dp2px(20f);
            mId = UUID.randomUUID().toString();
            mStartPoint = new Point(startX, startY);
            mEndPoint = new Point(endX, endY);
            mStartMovingLineX = startX;
            mStartMovingLineY = startY;
            mEndMovingLineX = endX;
            mEndMovingLineY = endY;
            mHighPointBitmap = getCustomSizeImg(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_point_red), mPointSize, mPointSize);
            mLowPointBitmap = getCustomSizeImg(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon_point_green), mPointSize, mPointSize);
        }

        public void changeLocation(int startX, int startY, int endX, int endY) {
            mStartMovingLineX = startX;
            mStartMovingLineY = startY;
            mEndMovingLineX = endX;
            mEndMovingLineY = endY;
        }

        public void changePointLocation() {
            mStartPoint.x = mStartMovingLineX;
            mStartPoint.y = mStartMovingLineY;
            mEndPoint.x = mEndMovingLineX;
            mEndPoint.y = mEndMovingLineY;
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

        public int getStartMovingLineX() {
            return mStartMovingLineX;
        }

        public void setStartMovingLineX(int mStartMovingLineX) {
            this.mStartMovingLineX = mStartMovingLineX;
        }

        public int getStartMovingLineY() {
            return mStartMovingLineY;
        }

        public void setStartMovingLineY(int mStartMovingLineY) {
            this.mStartMovingLineY = mStartMovingLineY;
        }

        public int getEndMovingLineX() {
            return mEndMovingLineX;
        }

        public void setEndMovingLineX(int mEndMovingLineX) {
            this.mEndMovingLineX = mEndMovingLineX;
        }

        public int getEndMovingLineY() {
            return mEndMovingLineY;
        }

        public void setEndMovingLineY(int mEndMovingLineY) {
            this.mEndMovingLineY = mEndMovingLineY;
        }
    }

    public LinkedList<LineView> getLineViewList() {
        return mLineList;
    }
}
