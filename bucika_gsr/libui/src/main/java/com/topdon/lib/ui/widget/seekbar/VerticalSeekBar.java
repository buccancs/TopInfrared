package com.topdon.lib.ui.widget.seekbar;

import static com.topdon.lib.ui.widget.seekbar.VerticalRangeSeekBar.DIRECTION_LEFT;
import static com.topdon.lib.ui.widget.seekbar.VerticalRangeSeekBar.DIRECTION_RIGHT;
import static com.topdon.lib.ui.widget.seekbar.VerticalRangeSeekBar.TEXT_DIRECTION_VERTICAL;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;

import com.topdon.lib.ui.R;


/**
 * //                       _ooOoo_
 * //                      o8888888o
 * //                      88" . "88
 * //                      (| -_- |)
 * //                       O\ = /O
 * //                   ____/`---'\____
 * //                 .   ' \\| |// `.
 * //                  / \\||| : |||// \
 * //                / _||||| -:- |||||- \
 * //                  | | \\\ - /// | |
 * //                | \_| ''\---/'' | |
 * //                 \ .-\__ `-` ___/-. /
 * //              ______`. .' /--.--\ `. . __
 * //           ."" '< `.___\_<|>_/___.' >'"".
 * //          | | : `- \`.;`\ _ /`;.`/ - ` : | |
 * //            \ \ `-. \_ __\ /__ _/ .-` / /
 * //    ======`-.____`-.___\_____/___.-`____.-'======
 * //                       `=---='
 * //
 * //    .............................................
 * //             佛祖保佑             永无BUG
 * =====================================================
 * 作    者：JayGoo
 * 创建日期：2019-06-05
 * 描    述:
 * =====================================================
 */
public class VerticalSeekBar extends SeekBar {

    private int indicatorTextOrientation;
    VerticalRangeSeekBar verticalSeekBar;

    public VerticalSeekBar(RangeSeekBar rangeSeekBar, AttributeSet attrs, boolean isLeft) {
        super(rangeSeekBar, attrs, isLeft);
        initAttrs(attrs);
        verticalSeekBar = (VerticalRangeSeekBar) rangeSeekBar;
    }

    private void initAttrs(AttributeSet attrs) {
        try {
            TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.VerticalRangeSeekBar);
            indicatorTextOrientation = t.getInt(R.styleable.VerticalRangeSeekBar_rsb_indicator_text_orientation, TEXT_DIRECTION_VERTICAL);
            t.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDrawIndicator(Canvas canvas, Paint paint, String text2Draw) {
        if (text2Draw == null) return;
        //draw indicator
        if (indicatorTextOrientation == TEXT_DIRECTION_VERTICAL) {
            drawVerticalIndicator(canvas, paint, text2Draw);
        } else {
            super.onDrawIndicator(canvas, paint, text2Draw);
        }
    }

    private boolean drawIndPathBg = true;//是否绘制背景

    public void setDrawIndPathBg(boolean draw){
        drawIndPathBg = draw;
    }
    private boolean noNegativeNumber = false;
    /**
     * 临时处理负数
     */
    public void setNoNegativeNumber(Boolean noNegativeNumber){
        this.noNegativeNumber = noNegativeNumber;
    }
    /**
     * 竖标签绘制
     *
     * @param canvas
     * @param paint
     * @param text2Draw
     */
    protected void drawVerticalIndicator(Canvas canvas, Paint paint, String text2Draw) {
        //measure indicator text
        try {
            paint.setTextSize(getIndicatorTextSize());
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getIndicatorBackgroundColor());
            if (noNegativeNumber){
                text2Draw = text2Draw.replace("-","");
            }
            paint.getTextBounds(text2Draw, 0, text2Draw.length(), indicatorTextRect);

            int realIndicatorWidth = indicatorTextRect.height() + getIndicatorPaddingLeft() + getIndicatorPaddingRight();
            if (getIndicatorWidth() > realIndicatorWidth) {
                realIndicatorWidth = getIndicatorWidth();
            }

            int realIndicatorHeight = indicatorTextRect.width() + getIndicatorPaddingTop() + getIndicatorPaddingBottom();
            if (getIndicatorHeight() > realIndicatorHeight) {
                realIndicatorHeight = getIndicatorHeight();
            }

            indicatorRect.left = scaleThumbWidth / 2 - realIndicatorWidth / 2;
            indicatorRect.top = bottom - realIndicatorHeight - scaleThumbHeight - getIndicatorMargin();
            indicatorRect.right = indicatorRect.left + realIndicatorWidth;
            indicatorRect.bottom = indicatorRect.top + realIndicatorHeight;

            //指示箭头
            //draw default indicator arrow
            if (indicatorBitmap == null && drawIndPathBg) {
                //arrow three point
                //  b   c
                //    a
                indicatorArrowPath.reset();
                int ax = scaleThumbWidth / 2;
                int ay = indicatorRect.bottom;
                int bx = ax - getIndicatorArrowSize();
                int by = ay - getIndicatorArrowSize();
                int cx = ax + getIndicatorArrowSize();
                indicatorArrowPath.moveTo(ax, ay);
                indicatorArrowPath.lineTo(bx, by);
                indicatorArrowPath.lineTo(cx, by);
                indicatorArrowPath.close();
                canvas.drawPath(indicatorArrowPath, paint);
                indicatorRect.bottom -= getIndicatorArrowSize();
                indicatorRect.top -= getIndicatorArrowSize();
                Log.w("伪彩条刷新","///");
            }

            int defaultPaddingOffset = Utils.dp2px(getContext(), 1);
            int leftOffset = indicatorRect.width() / 2 - (int) (rangeSeekBar.getProgressWidth() * currPercent) - rangeSeekBar.getProgressLeft() + defaultPaddingOffset;
            int rightOffset = indicatorRect.width() / 2 - (int) (rangeSeekBar.getProgressWidth() * (1 - currPercent)) - rangeSeekBar.getProgressPaddingRight() + defaultPaddingOffset;
            if (leftOffset > 0) {
                indicatorRect.left += leftOffset;
                indicatorRect.right += leftOffset;
            } else if (rightOffset > 0) {
                indicatorRect.left -= rightOffset;
                indicatorRect.right -= rightOffset;
            }

            //背景
            //draw indicator background
            if (drawIndPathBg){
                if (indicatorBitmap != null) {
                    Utils.drawBitmap(canvas, paint, indicatorBitmap, indicatorRect);
                } else if (getIndicatorRadius() > 0f) {
                    canvas.drawRoundRect(new RectF(indicatorRect), getIndicatorRadius(), getIndicatorRadius(), paint);
                } else {
                    canvas.drawRect(indicatorRect, paint);
                }
            }

            //draw indicator content text
            int tx = indicatorRect.left + (indicatorRect.width() - indicatorTextRect.width()) / 2 + getIndicatorPaddingLeft() - getIndicatorPaddingRight();
            int ty = indicatorRect.bottom - (indicatorRect.height() - indicatorTextRect.height()) / 2 + getIndicatorPaddingTop() - getIndicatorPaddingBottom();

            //draw indicator text
            paint.setColor(getIndicatorTextColor());

            int degrees = 0;
            float rotateX = (tx + indicatorTextRect.width() / 2f);
            float rotateY = (ty - indicatorTextRect.height() / 2f);

            if (indicatorTextOrientation == TEXT_DIRECTION_VERTICAL) {
                if (verticalSeekBar.getOrientation() == DIRECTION_LEFT) {
                    degrees = 90;
                } else if (verticalSeekBar.getOrientation() == DIRECTION_RIGHT) {
                    degrees = -90;
                }
            }
            if (degrees != 0) {
                canvas.rotate(degrees, rotateX, rotateY);
            }
            //标签文本
            canvas.drawText(text2Draw, tx, ty, paint);
            if (degrees != 0) {
                canvas.rotate(-degrees, rotateX, rotateY);
            }
        }catch (Exception e){
            Log.e("伪彩条渲染失败",e.getMessage());
        }
    }

    public int getIndicatorTextOrientation() {
        return indicatorTextOrientation;
    }

    public void setIndicatorTextOrientation(@VerticalRangeSeekBar.TextDirectionDef int orientation) {
        this.indicatorTextOrientation = orientation;
    }
}
