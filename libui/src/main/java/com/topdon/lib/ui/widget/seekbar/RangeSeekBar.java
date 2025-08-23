package com.topdon.lib.ui.widget.seekbar;

import static com.topdon.lib.ui.widget.seekbar.SeekBar.INDICATOR_ALWAYS_HIDE;
import static com.topdon.lib.ui.widget.seekbar.SeekBar.INDICATOR_ALWAYS_SHOW;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.topdon.lib.ui.R;
import com.topdon.menu.util.PseudoColorConfig;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class RangeSeekBar extends View {

    private int pseudocode = 3;

    private final static int MIN_INTERCEPT_DISTANCE = 100;

    public final static int SEEKBAR_MODE_SINGLE = 1;
    public final static int SEEKBAR_MODE_RANGE = 2;

    private boolean noNegativeNumber = false;

    public final static int TEMP_MODE_CLOSE = 0;
    public final static int TEMP_MODE_MAX = 2;
    public final static int TEMP_MODE_MIN = 1;
    public final static int TEMP_MODE_INTERVAL = 3;
    private int tempMode = TEMP_MODE_CLOSE;

    public void setTempMode(int tempMode) {
        this.tempMode = tempMode;
    }
    public int getTempMode(){
        return tempMode;
    }

    private void updateTempModeState(){
        if (tempMode == TEMP_MODE_CLOSE){
            if (currTouchSB == leftSB){
                tempMode = TEMP_MODE_MIN;
            }else if (currTouchSB == rightSB){
                tempMode = TEMP_MODE_MAX;
            }
        }else if (tempMode == TEMP_MODE_MIN){
            if (currTouchSB == rightSB){
                tempMode = TEMP_MODE_INTERVAL;
            }
        }else if (tempMode == TEMP_MODE_MAX){
            if (currTouchSB == leftSB){
                tempMode = TEMP_MODE_INTERVAL;
            }
        }
    }

    @IntDef({SEEKBAR_MODE_SINGLE, SEEKBAR_MODE_RANGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SeekBarModeDef {
    }

    public final static int TRICK_MARK_MODE_NUMBER = 0;
    public final static int TRICK_MARK_MODE_OTHER = 1;

    @IntDef({TRICK_MARK_MODE_NUMBER, TRICK_MARK_MODE_OTHER})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TickMarkModeDef {
    }

    public final static int TICK_MARK_GRAVITY_LEFT = 0;
    public final static int TICK_MARK_GRAVITY_CENTER = 1;
    public final static int TICK_MARK_GRAVITY_RIGHT = 2;

    @IntDef({TICK_MARK_GRAVITY_LEFT, TICK_MARK_GRAVITY_CENTER, TICK_MARK_GRAVITY_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TickMarkGravityDef {
    }

    @IntDef({Gravity.TOP, Gravity.BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TickMarkLayoutGravityDef {
    }

    @IntDef({Gravity.TOP, Gravity.CENTER, Gravity.BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GravityDef {
    }

    public static class Gravity {
        public final static int TOP = 0;
        public final static int BOTTOM = 1;
        public final static int CENTER = 2;
    }

    private int progressTop, progressBottom, progressLeft, progressRight;
    private int seekBarMode;
    private int tickMarkMode;
    private int tickMarkTextMargin;
    private int tickMarkTextSize;
    private int tickMarkGravity;
    private int tickMarkLayoutGravity;
    private int tickMarkTextColor;
    private int tickMarkInRangeTextColor;
    private CharSequence[] tickMarkTextArray;
    private float progressRadius;
    private int progressColor;
    private int progressDefaultColor;

    private int progressDrawableId;
    private int progressDefaultDrawableId;

    private int progressHeight;
    private int progressWidth;
    private float minInterval;

    private int gravity;
    private boolean enableThumbOverlap;

    private int stepsColor;
    private float stepsWidth;
    private float stepsHeight;
    private float stepsRadius;
    private int steps;
    private boolean stepsAutoBonding;
    private int stepsDrawableId;
    private float minProgress, maxProgress;

    private boolean isEnable = true;
    float touchDownX, touchDownY;
    float reservePercent;
    boolean isScaleThumb = false;
    Paint paint = new Paint();
    RectF progressDefaultDstRect = new RectF();
    RectF progressDstRect = new RectF();
    Rect progressSrcRect = new Rect();
    RectF stepDivRect = new RectF();
    Rect tickMarkTextRect = new Rect();
    SeekBar leftSB;
    SeekBar rightSB;
    SeekBar currTouchSB;
    Bitmap progressBitmap;
    Bitmap progressDefaultBitmap;
    List<Bitmap> stepsBitmaps = new ArrayList<>();
    private int progressPaddingRight;
    private OnRangeChangedListener callback;

    @Nullable
    private int[] colorList;

    @Nullable
    private float[] places;

    public RangeSeekBar(Context context) {
        this(context, null);
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initPaint();
        initSeekBar(attrs);
        initStepsBitmap();
    }

    private void initProgressBitmap() {
        if (progressBitmap == null) {
            progressBitmap = Utils.drawableToBitmap(getContext(), progressWidth, progressHeight, progressDrawableId);
        }
        if (progressDefaultBitmap == null) {
            progressDefaultBitmap = Utils.drawableToBitmap(getContext(), progressWidth, progressHeight, progressDefaultDrawableId);
        }
    }

    private boolean verifyStepsMode() {
        if (steps < 1 || stepsHeight <= 0 || stepsWidth <= 0) return false;
        return true;
    }

    private void initStepsBitmap() {
        if (!verifyStepsMode() || stepsDrawableId == 0) return;
        if (stepsBitmaps.isEmpty()) {
            Bitmap bitmap = Utils.drawableToBitmap(getContext(), (int) stepsWidth, (int) stepsHeight, stepsDrawableId);
            for (int i = 0; i <= steps; i++) {
                stepsBitmaps.add(bitmap);
            }
        }
    }

    private void initSeekBar(AttributeSet attrs) {
        leftSB = new SeekBar(this, attrs, true);
        rightSB = new SeekBar(this, attrs, false);
        rightSB.setVisible(seekBarMode != SEEKBAR_MODE_SINGLE);
    }

    private void initAttrs(AttributeSet attrs) {
        try {
            TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);
            seekBarMode = t.getInt(R.styleable.RangeSeekBar_rsb_mode, SEEKBAR_MODE_RANGE);
            minProgress = t.getFloat(R.styleable.RangeSeekBar_rsb_min, 0);
            maxProgress = t.getFloat(R.styleable.RangeSeekBar_rsb_max, 100);
            minInterval = t.getFloat(R.styleable.RangeSeekBar_rsb_min_interval, 0);
            gravity = t.getInt(R.styleable.RangeSeekBar_rsb_gravity, Gravity.TOP);
            progressColor = t.getColor(R.styleable.RangeSeekBar_rsb_progress_color, -1);
            progressRadius = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_progress_radius, -1);
            progressDefaultColor = t.getColor(R.styleable.RangeSeekBar_rsb_progress_default_color, -1);
            progressDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_progress_drawable, 0);
            progressDefaultDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_progress_drawable_default, 0);
            progressHeight = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_progress_height, Utils.dp2px(getContext(), 2));
            tickMarkMode = t.getInt(R.styleable.RangeSeekBar_rsb_tick_mark_mode, TRICK_MARK_MODE_NUMBER);
            tickMarkGravity = t.getInt(R.styleable.RangeSeekBar_rsb_tick_mark_gravity, TICK_MARK_GRAVITY_CENTER);
            tickMarkLayoutGravity = t.getInt(R.styleable.RangeSeekBar_rsb_tick_mark_layout_gravity, Gravity.TOP);
            tickMarkTextArray = t.getTextArray(R.styleable.RangeSeekBar_rsb_tick_mark_text_array);
            tickMarkTextMargin = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_tick_mark_text_margin, Utils.dp2px(getContext(), 7));
            tickMarkTextSize = (int) t.getDimension(R.styleable.RangeSeekBar_rsb_tick_mark_text_size, Utils.dp2px(getContext(), 12));
            tickMarkTextColor = t.getColor(R.styleable.RangeSeekBar_rsb_tick_mark_text_color, progressDefaultColor);
            tickMarkInRangeTextColor = t.getColor(R.styleable.RangeSeekBar_rsb_tick_mark_in_range_text_color, progressColor);
            steps = t.getInt(R.styleable.RangeSeekBar_rsb_steps, 0);
            stepsColor = t.getColor(R.styleable.RangeSeekBar_rsb_step_color, 0xFF9d9d9d);
            stepsRadius = t.getDimension(R.styleable.RangeSeekBar_rsb_step_radius, 0);
            stepsWidth = t.getDimension(R.styleable.RangeSeekBar_rsb_step_width, 0);
            stepsHeight = t.getDimension(R.styleable.RangeSeekBar_rsb_step_height, 0);
            stepsDrawableId = t.getResourceId(R.styleable.RangeSeekBar_rsb_step_drawable, 0);
            stepsAutoBonding = t.getBoolean(R.styleable.RangeSeekBar_rsb_step_auto_bonding, true);
            t.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void onMeasureProgress(int w, int h) {
        int viewHeight = h - getPaddingBottom() - getPaddingTop();
        if (h <= 0) return;

        if (gravity == Gravity.TOP) {
            float maxIndicatorHeight = 0;
            if (leftSB.getIndicatorShowMode() != INDICATOR_ALWAYS_HIDE
                    || rightSB.getIndicatorShowMode() != INDICATOR_ALWAYS_HIDE) {
                maxIndicatorHeight = Math.max(leftSB.getIndicatorRawHeight(), rightSB.getIndicatorRawHeight());
            }
            float thumbHeight = Math.max(leftSB.getThumbScaleHeight(), rightSB.getThumbScaleHeight());
            thumbHeight -= progressHeight / 2f;

            progressTop = (int) (maxIndicatorHeight + (thumbHeight - progressHeight) / 2f);
            if (tickMarkTextArray != null && tickMarkLayoutGravity == Gravity.TOP) {
                progressTop = (int) Math.max(getTickMarkRawHeight(), maxIndicatorHeight + (thumbHeight - progressHeight) / 2f);
            }
            progressBottom = progressTop + progressHeight;
        } else if (gravity == Gravity.BOTTOM) {
            if (tickMarkTextArray != null && tickMarkLayoutGravity == Gravity.BOTTOM) {
                progressBottom = viewHeight - getTickMarkRawHeight();
            } else {
                progressBottom = (int) (viewHeight - Math.max(leftSB.getThumbScaleHeight(), rightSB.getThumbScaleHeight()) / 2f
                        + progressHeight / 2f);
            }
            progressTop = progressBottom - progressHeight;
        } else {
            progressTop = (viewHeight - progressHeight) / 2;
            progressBottom = progressTop + progressHeight;
        }

        int maxThumbWidth = (int) Math.max(leftSB.getThumbScaleWidth(), rightSB.getThumbScaleWidth());
        progressLeft = maxThumbWidth / 2 + getPaddingLeft();
        progressRight = w - maxThumbWidth / 2 - getPaddingRight();
        progressWidth = progressRight - progressLeft;
        progressDefaultDstRect.set(getProgressLeft(), getProgressTop(), getProgressRight(), getProgressBottom());
        progressPaddingRight = w - progressRight;
        if (progressRadius <= 0) {
            progressRadius = (int) ((getProgressBottom() - getProgressTop()) * 0.15f);
        }
        initProgressBitmap();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
            heightSize = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        } else if (heightMode == MeasureSpec.AT_MOST && getParent() instanceof ViewGroup
                && heightSize == ViewGroup.LayoutParams.MATCH_PARENT) {
            heightSize = MeasureSpec.makeMeasureSpec(((ViewGroup) getParent()).getMeasuredHeight(), MeasureSpec.AT_MOST);
        } else {
            int heightNeeded;
            if (gravity == Gravity.CENTER) {
                if (tickMarkTextArray != null && tickMarkLayoutGravity == Gravity.BOTTOM) {
                    heightNeeded = (int) (2 * (getRawHeight() - getTickMarkRawHeight()));
                } else {
                    heightNeeded = (int) (2 * (getRawHeight() - Math.max(leftSB.getThumbScaleHeight(), rightSB.getThumbScaleHeight()) / 2));
                }
            } else {
                heightNeeded = (int) getRawHeight();
            }
            heightSize = MeasureSpec.makeMeasureSpec(heightNeeded, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightSize);
    }

    protected int getTickMarkRawHeight() {
        if (tickMarkTextArray != null && tickMarkTextArray.length > 0) {
            return tickMarkTextMargin + Utils.measureText(String.valueOf(tickMarkTextArray[0]), tickMarkTextSize).height() + 3;
        }
        return 0;
    }

    protected float getRawHeight() {
        float rawHeight;
        if (seekBarMode == SEEKBAR_MODE_SINGLE) {
            rawHeight = leftSB.getRawHeight();
            if (tickMarkLayoutGravity == Gravity.BOTTOM && tickMarkTextArray != null) {
                float h = Math.max((leftSB.getThumbScaleHeight() - progressHeight) / 2, getTickMarkRawHeight());
                rawHeight = rawHeight - leftSB.getThumbScaleHeight() / 2 + progressHeight / 2f + h;
            }
        } else {
            rawHeight = Math.max(leftSB.getRawHeight(), rightSB.getRawHeight());
            if (tickMarkLayoutGravity == Gravity.BOTTOM && tickMarkTextArray != null) {
                float thumbHeight = Math.max(leftSB.getThumbScaleHeight(), rightSB.getThumbScaleHeight());
                float h = Math.max((thumbHeight - progressHeight) / 2, getTickMarkRawHeight());
                rawHeight = rawHeight - thumbHeight / 2 + progressHeight / 2f + h;
            }
        }
        return rawHeight;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onMeasureProgress(w, h);
        setRange(minProgress, maxProgress, minInterval);
        int lineCenterY = (getProgressBottom() + getProgressTop()) / 2;
        leftSB.onSizeChanged(getProgressLeft(), lineCenterY);
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSB.onSizeChanged(getProgressLeft(), lineCenterY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        onDrawTickMark(canvas, paint);
        onDrawProgressBar(canvas, paint);
        onDrawSteps(canvas, paint);
        onDrawSeekBar(canvas);
    }

    protected void onDrawTickMark(Canvas canvas, Paint paint) {
        if (tickMarkTextArray != null) {
            int trickPartWidth = progressWidth / (tickMarkTextArray.length - 1);
            for (int i = 0; i < tickMarkTextArray.length; i++) {
                final String text2Draw = tickMarkTextArray[i].toString();
                if (TextUtils.isEmpty(text2Draw)) continue;
                paint.getTextBounds(text2Draw, 0, text2Draw.length(), tickMarkTextRect);
                paint.setColor(tickMarkTextColor);
                float x;
                if (tickMarkMode == TRICK_MARK_MODE_OTHER) {
                    if (tickMarkGravity == TICK_MARK_GRAVITY_RIGHT) {
                        x = getProgressLeft() + i * trickPartWidth - tickMarkTextRect.width();
                    } else if (tickMarkGravity == TICK_MARK_GRAVITY_CENTER) {
                        x = getProgressLeft() + i * trickPartWidth - tickMarkTextRect.width() / 2f;
                    } else {
                        x = getProgressLeft() + i * trickPartWidth;
                    }
                } else {
                    float num = Utils.parseFloat(text2Draw);
                    SeekBarState[] states = getRangeSeekBarState();
                    if (Utils.compareFloat(num, states[0].value) != -1 && Utils.compareFloat(num, states[1].value) != 1 && (seekBarMode == SEEKBAR_MODE_RANGE)) {
                        paint.setColor(tickMarkInRangeTextColor);
                    }
                    x = getProgressLeft() + progressWidth * (num - minProgress) / (maxProgress - minProgress)
                            - tickMarkTextRect.width() / 2f;
                }
                float y;
                if (tickMarkLayoutGravity == Gravity.TOP) {
                    y = getProgressTop() - tickMarkTextMargin;
                } else {
                    y = getProgressBottom() + tickMarkTextMargin + tickMarkTextRect.height();
                }
                canvas.drawText(text2Draw, x, y, paint);
            }
        }
    }

    protected void onDrawProgressBar(Canvas canvas, Paint paint) {

        paint.setShader(null);
        if (Utils.verifyBitmap(progressDefaultBitmap)) {
            canvas.drawBitmap(progressDefaultBitmap, null, progressDefaultDstRect, paint);
        } else {
            if (progressDefaultColor == -1){
                int[] colors = PseudoColorConfig.getSeekBarColors();
                float[] positions = PseudoColorConfig.getSeekBarAlpha();
                paint.setShader(new LinearGradient(progressWidth, 0f, 0f, 0f, colors, positions, Shader.TileMode.CLAMP));
            }else {
                paint.setColor(progressDefaultColor);
            }
            canvas.drawRoundRect(progressDefaultDstRect, progressRadius, progressRadius, paint);
        }

        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            progressDstRect.top = getProgressTop();
            progressDstRect.left = leftSB.left + leftSB.getThumbScaleWidth() / 2f + progressWidth * leftSB.currPercent;
            progressDstRect.right = rightSB.left + rightSB.getThumbScaleWidth() / 2f + progressWidth * rightSB.currPercent;
            progressDstRect.bottom = getProgressBottom();
        } else {
            progressDstRect.top = getProgressTop();
            progressDstRect.left = leftSB.left + leftSB.getThumbScaleWidth() / 2f;
            progressDstRect.right = leftSB.left + leftSB.getThumbScaleWidth() / 2f + progressWidth * leftSB.currPercent;
            progressDstRect.bottom = getProgressBottom();
        }
        if (colorList != null){
            paint.setShader(new LinearGradient(progressWidth, 0f, 0f, 0f, colorList, places, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(progressDstRect, progressRadius, progressRadius, paint);
        }else if (progressColor == -1) {
            int[] colors = PseudoColorConfig.getColors(pseudocode);
            float[] positions = PseudoColorConfig.getPositions(pseudocode);
            paint.setShader(new LinearGradient(progressWidth, 0f, 0f, 0f, colors, positions, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(progressDstRect, progressRadius, progressRadius, paint);
        } else {
            if (Utils.verifyBitmap(progressBitmap)) {
                progressSrcRect.top = 0;
                progressSrcRect.bottom = progressBitmap.getHeight();
                int bitmapWidth = progressBitmap.getWidth();
                if (seekBarMode == SEEKBAR_MODE_RANGE) {
                    progressSrcRect.left = (int) (bitmapWidth * leftSB.currPercent);
                    progressSrcRect.right = (int) (bitmapWidth * rightSB.currPercent);
                } else {
                    progressSrcRect.left = 0;
                    progressSrcRect.right = (int) (bitmapWidth * leftSB.currPercent);
                }
                canvas.drawBitmap(progressBitmap, progressSrcRect, progressDstRect, null);
            } else {
                paint.setColor(progressColor);
                canvas.drawRoundRect(progressDstRect, progressRadius, progressRadius, paint);
            }
        }

    }

    protected void onDrawSteps(Canvas canvas, Paint paint) {
        if (!verifyStepsMode()) return;
        int stepMarks = getProgressWidth() / (steps);
        float extHeight = (stepsHeight - getProgressHeight()) / 2f;
        for (int k = 0; k <= steps; k++) {
            float x = getProgressLeft() + k * stepMarks - stepsWidth / 2f;
            stepDivRect.set(x, getProgressTop() - extHeight, x + stepsWidth, getProgressBottom() + extHeight);
            if (stepsBitmaps.isEmpty() || stepsBitmaps.size() <= k) {
                paint.setColor(stepsColor);
                canvas.drawRoundRect(stepDivRect, stepsRadius, stepsRadius, paint);
            } else {
                canvas.drawBitmap(stepsBitmaps.get(k), null, stepDivRect, paint);
            }
        }
    }

    protected void onDrawSeekBar(Canvas canvas) {
        if (leftSB.getIndicatorShowMode() == INDICATOR_ALWAYS_SHOW) {
            leftSB.setShowIndicatorEnable(true);
        }
        leftSB.draw(canvas, true);
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            if (rightSB.getIndicatorShowMode() == INDICATOR_ALWAYS_SHOW) {
                rightSB.setShowIndicatorEnable(true);
            }
            rightSB.draw(canvas, false);
        }
    }

    private void initPaint() {
        paint.setStyle(Paint.Style.FILL);

        if (progressDefaultColor == -1){
            int[] colors = PseudoColorConfig.getSeekBarColors();
            float[] positions = PseudoColorConfig.getSeekBarAlpha();
            paint.setShader(new LinearGradient(progressWidth, 0f, 0f, 0f, colors, positions, Shader.TileMode.CLAMP));
        }else {
            paint.setColor(progressDefaultColor);
        }
        paint.setTextSize(tickMarkTextSize);
    }

    private void changeThumbActivateState(boolean hasActivate) {
        if (hasActivate && currTouchSB != null) {
            boolean state = currTouchSB == leftSB;
            leftSB.setActivate(state);
            if (seekBarMode == SEEKBAR_MODE_RANGE)
                rightSB.setActivate(!state);
        } else {
            leftSB.setActivate(false);
            if (seekBarMode == SEEKBAR_MODE_RANGE)
                rightSB.setActivate(false);
        }
    }

    protected float getEventX(MotionEvent event) {
        return event.getX();
    }

    protected float getEventY(MotionEvent event) {
        return event.getY();
    }

    private void scaleCurrentSeekBarThumb() {
        if (currTouchSB != null && currTouchSB.getThumbScaleRatio() > 1f && !isScaleThumb) {
            isScaleThumb = true;
            currTouchSB.scaleThumb();
        }
    }

    private void resetCurrentSeekBarThumb() {
        if (currTouchSB != null && currTouchSB.getThumbScaleRatio() > 1f && isScaleThumb) {
            isScaleThumb = false;
            currTouchSB.resetThumb();
        }
    }

    protected float calculateCurrentSeekBarPercent(float touchDownX) {
        if (currTouchSB == null) return 0;
        float percent = (touchDownX - getProgressLeft()) * 1f / (progressWidth);
        if (touchDownX < getProgressLeft()) {
            percent = 0;
        } else if (touchDownX > getProgressRight()) {
            percent = 1;
        }
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            if (currTouchSB == leftSB) {
                if (percent > rightSB.currPercent - reservePercent) {
                    percent = rightSB.currPercent - reservePercent;
                }
            } else if (currTouchSB == rightSB) {
                if (percent < leftSB.currPercent + reservePercent) {
                    percent = leftSB.currPercent + reservePercent;
                }
            }
        }
        return percent;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnable) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchDownX = getEventX(event);
                touchDownY = getEventY(event);
                if (seekBarMode == SEEKBAR_MODE_RANGE) {
                    if (rightSB.currPercent >= 1 && leftSB.collide(getEventX(event), getEventY(event))) {
                        currTouchSB = leftSB;
                        scaleCurrentSeekBarThumb();
                    } else if (rightSB.collide(getEventX(event), getEventY(event))) {
                        currTouchSB = rightSB;
                        scaleCurrentSeekBarThumb();
                    } else {
                        float performClick = (touchDownX - getProgressLeft()) * 1f / (progressWidth);
                        float distanceLeft = Math.abs(leftSB.currPercent - performClick);
                        float distanceRight = Math.abs(rightSB.currPercent - performClick);
                        if (distanceLeft < distanceRight) {
                            currTouchSB = leftSB;
                        } else {
                            currTouchSB = rightSB;
                        }
                        performClick = calculateCurrentSeekBarPercent(touchDownX);
                        currTouchSB.slide(performClick);
                    }
                } else {
                    currTouchSB = leftSB;
                    scaleCurrentSeekBarThumb();
                }

                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (callback != null) {
                    callback.onStartTrackingTouch(this, currTouchSB == leftSB);
                }
                changeThumbActivateState(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                float x = getEventX(event);
                if ((seekBarMode == SEEKBAR_MODE_RANGE) && leftSB.currPercent == rightSB.currPercent) {
                    currTouchSB.materialRestore();
                    if (callback != null) {
                        callback.onStopTrackingTouch(this, currTouchSB == leftSB);
                    }
                    if (x - touchDownX > 0) {
                        if (currTouchSB != rightSB) {
                            currTouchSB.setShowIndicatorEnable(false);
                            resetCurrentSeekBarThumb();
                            currTouchSB = rightSB;
                        }
                    } else {
                        if (currTouchSB != leftSB) {
                            currTouchSB.setShowIndicatorEnable(false);
                            resetCurrentSeekBarThumb();
                            currTouchSB = leftSB;
                        }
                    }
                    if (callback != null) {
                        callback.onStartTrackingTouch(this, currTouchSB == leftSB);
                    }
                }
                scaleCurrentSeekBarThumb();
                currTouchSB.material = currTouchSB.material >= 1 ? 1 : currTouchSB.material + 0.1f;
                touchDownX = x;
                currTouchSB.slide(calculateCurrentSeekBarPercent(touchDownX));
                currTouchSB.setShowIndicatorEnable(true);

                if (callback != null) {
                    SeekBarState[] states = getRangeSeekBarState();
                    updateTempModeState();
                    callback.onRangeChanged(this, states[0].value, states[1].value, true,tempMode);
                }
                invalidate();
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                changeThumbActivateState(true);
                break;
            case MotionEvent.ACTION_CANCEL:
                if (seekBarMode == SEEKBAR_MODE_RANGE) {
                    rightSB.setShowIndicatorEnable(false);
                }
                if (currTouchSB == leftSB) {
                    resetCurrentSeekBarThumb();
                } else if (currTouchSB == rightSB) {
                    resetCurrentSeekBarThumb();
                }
                leftSB.setShowIndicatorEnable(false);
                if (callback != null) {
                    SeekBarState[] states = getRangeSeekBarState();
                    updateTempModeState();
                    callback.onRangeChanged(this, states[0].value, states[1].value, false,tempMode);
                }
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                changeThumbActivateState(false);
                break;
            case MotionEvent.ACTION_UP:
                if (verifyStepsMode() && stepsAutoBonding) {
                    float percent = calculateCurrentSeekBarPercent(getEventX(event));
                    float stepPercent = 1.0f / steps;
                    int stepSelected = new BigDecimal(percent / stepPercent).setScale(0, RoundingMode.HALF_UP).intValue();
                    currTouchSB.slide(stepSelected * stepPercent);
                }

                if (seekBarMode == SEEKBAR_MODE_RANGE) {
                    rightSB.setShowIndicatorEnable(false);
                }
                leftSB.setShowIndicatorEnable(false);
                currTouchSB.materialRestore();
                resetCurrentSeekBarThumb();
                if (callback != null) {
                    SeekBarState[] states = getRangeSeekBarState();
                    updateTempModeState();
                    callback.onRangeChanged(this, states[0].value, states[1].value, false,tempMode);
                }
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (callback != null) {
                    callback.onStopTrackingTouch(this, currTouchSB == leftSB);
                }
                changeThumbActivateState(false);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.minValue = minProgress;
        ss.maxValue = maxProgress;
        ss.rangeInterval = minInterval;
        SeekBarState[] results = getRangeSeekBarState();
        ss.currSelectedMin = results[0].value;
        ss.currSelectedMax = results[1].value;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        try {
            SavedState ss = (SavedState) state;
            super.onRestoreInstanceState(ss.getSuperState());
            float min = ss.minValue;
            float max = ss.maxValue;
            float rangeInterval = ss.rangeInterval;
            setRange(min, max, rangeInterval);
            float currSelectedMin = ss.currSelectedMin;
            float currSelectedMax = ss.currSelectedMax;
            setProgress(currSelectedMin, currSelectedMax);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setNoNegativeNumber(Boolean noNegativeNumber){
        this.noNegativeNumber = noNegativeNumber;
        if (leftSB!=null){
            leftSB.setNoNegativeNumber(noNegativeNumber);
        }
        if (rightSB!=null){
            rightSB.setNoNegativeNumber(noNegativeNumber);
        }
    }

    public void setOnRangeChangedListener(OnRangeChangedListener listener) {
        callback = listener;
    }

    public void setProgress(float value) {
        setProgress(value, maxProgress);
    }
    public void setProgressNoCallBack(float leftValue, float rightValue) {
        leftValue = Math.min(leftValue, rightValue);
        rightValue = Math.max(leftValue, rightValue);
        if (rightValue - leftValue < minInterval) {
            if (leftValue - minProgress > maxProgress - rightValue) {
                leftValue = rightValue - minInterval;
            } else {
                rightValue = leftValue + minInterval;
            }
        }
        if (leftValue < minProgress) {
            leftValue = minProgress;
        }

        if (rightValue > maxProgress) {
            rightValue = maxProgress;
        }
        float range = maxProgress - minProgress;
        leftSB.currPercent = Math.abs(leftValue - minProgress) / range;
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSB.currPercent = Math.abs(rightValue - minProgress) / range;
        }
        postInvalidate();
    }
    Long updateTime = System.currentTimeMillis();

    @Override
    public void invalidate() {
        if (System.currentTimeMillis() - updateTime < 50){
            return;
        }
        super.invalidate();
        updateTime = System.currentTimeMillis();
    }

    public void setProgress(float leftValue, float rightValue) {
        leftValue = Math.min(leftValue, rightValue);
        rightValue = Math.max(leftValue, rightValue);
        if (rightValue - leftValue < minInterval) {
            if (leftValue - minProgress > maxProgress - rightValue) {
                leftValue = rightValue - minInterval;
            } else {
                rightValue = leftValue + minInterval;
            }
        }
        if (leftValue < minProgress) {
            leftValue = minProgress;
        }

        if (rightValue > maxProgress) {
            rightValue = maxProgress;
        }
        float range = maxProgress - minProgress;
        leftSB.currPercent = Math.abs(leftValue - minProgress) / range;
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSB.currPercent = Math.abs(rightValue - minProgress) / range;
        }
        if (callback != null) {
            callback.onRangeChanged(this, leftValue, rightValue, false,tempMode);
        }
        invalidate();
    }

    public void setRange(float min, float max) {
        setRange(min, max, minInterval);
        setProgress(getLeftSeekBar().left,getRightSeekBar().right);
    }

    public void setRangeAndPro(float editMin,float editMax,float realLeftValue,float realRightValue){
        if (editMin == Float.MIN_VALUE && editMax == Float.MAX_VALUE){
            setRangeNoInvalidate(realLeftValue,realRightValue,0.1f);
            setProgressNoCallBack(realLeftValue,realRightValue);
            return;
        }
        setRangeNoInvalidate(realLeftValue,realRightValue,0.1f);
        if (editMax <= realRightValue && editMin >= realLeftValue){
            setProgressNoCallBack(editMin,editMax);
        }else if (editMax > realRightValue && editMin < realLeftValue){
            setProgressNoCallBack(realLeftValue,realRightValue);
        }else if (editMax > realRightValue && editMin > realRightValue){
            setProgressNoCallBack(realRightValue,realRightValue);
        } else if (editMax < realLeftValue && editMin < realLeftValue){
            setProgressNoCallBack(realLeftValue,realLeftValue);
        }else if (editMax <= realRightValue && editMin < realLeftValue){
            setProgressNoCallBack(realLeftValue,editMax);
        }else if (editMax > realRightValue && editMin >= realLeftValue){
            setProgressNoCallBack(editMin,realRightValue);
        }
    }

    public void setRange(float min, float max, float minInterval) {
        if (maxProgress == max && min == minProgress){
            return;
        }
        maxProgress = max;
        minProgress = min;
        this.minInterval = minInterval;
        reservePercent = minInterval / (max - min);

        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            if (leftSB.currPercent + reservePercent <= 1 && leftSB.currPercent + reservePercent > rightSB.currPercent) {
                rightSB.currPercent = leftSB.currPercent + reservePercent;
            } else if (rightSB.currPercent - reservePercent >= 0 && rightSB.currPercent - reservePercent < leftSB.currPercent) {
                leftSB.currPercent = rightSB.currPercent - reservePercent;
            }
        }
        postInvalidate();
    }
    public void setRangeNoInvalidate(float min, float max, float minInterval) {
        if (maxProgress == max && min == minProgress){
            return;
        }
        maxProgress = max;
        minProgress = min;
        this.minInterval = minInterval;
        reservePercent = minInterval / (max - min);

        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            if (leftSB.currPercent + reservePercent <= 1 && leftSB.currPercent + reservePercent > rightSB.currPercent) {
                rightSB.currPercent = leftSB.currPercent + reservePercent;
            } else if (rightSB.currPercent - reservePercent >= 0 && rightSB.currPercent - reservePercent < leftSB.currPercent) {
                leftSB.currPercent = rightSB.currPercent - reservePercent;
            }
        }
    }

    public SeekBarState[] getRangeSeekBarState() {
        SeekBarState leftSeekBarState = new SeekBarState();
        leftSeekBarState.value = leftSB.getProgress();

        leftSeekBarState.indicatorText = String.valueOf(leftSeekBarState.value);
        if (Utils.compareFloat(leftSeekBarState.value, minProgress) == 0) {
            leftSeekBarState.isMin = true;
        } else if (Utils.compareFloat(leftSeekBarState.value, maxProgress) == 0) {
            leftSeekBarState.isMax = true;
        }

        SeekBarState rightSeekBarState = new SeekBarState();
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSeekBarState.value = rightSB.getProgress();
            rightSeekBarState.indicatorText = String.valueOf(rightSeekBarState.value);
            if (Utils.compareFloat(rightSB.currPercent, minProgress) == 0) {
                rightSeekBarState.isMin = true;
            } else if (Utils.compareFloat(rightSB.currPercent, maxProgress) == 0) {
                rightSeekBarState.isMax = true;
            }
        }

        return new SeekBarState[]{leftSeekBarState, rightSeekBarState};
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.isEnable = enabled;
    }

    public void setIndicatorText(String progress) {
        leftSB.setIndicatorText(progress);
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSB.setIndicatorText(progress);
        }
    }

    public void setIndicatorTextDecimalFormat(String formatPattern) {
        leftSB.setIndicatorTextDecimalFormat(formatPattern);
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSB.setIndicatorTextDecimalFormat(formatPattern);
        }
    }

    public void setIndicatorTextStringFormat(String formatPattern) {
        leftSB.setIndicatorTextStringFormat(formatPattern);
        if (seekBarMode == SEEKBAR_MODE_RANGE) {
            rightSB.setIndicatorTextStringFormat(formatPattern);
        }
    }

    public SeekBar getLeftSeekBar() {
        return leftSB;
    }

    public SeekBar getRightSeekBar() {
        return rightSB;
    }

    public int getProgressTop() {
        return progressTop;
    }

    public int getProgressBottom() {
        return progressBottom;
    }

    public int getProgressLeft() {
        return progressLeft;
    }

    public int getProgressRight() {
        return progressRight;
    }

    public int getProgressPaddingRight() {
        return progressPaddingRight;
    }

    public int getProgressHeight() {
        return progressHeight;
    }

    public void setProgressHeight(int progressHeight) {
        this.progressHeight = progressHeight;
    }

    public float getMinProgress() {
        return minProgress;
    }

    public float getMaxProgress() {
        return maxProgress;
    }

    public void setProgressColor(@ColorInt int progressDefaultColor, @ColorInt int progressColor) {
        this.progressDefaultColor = progressDefaultColor;
        this.progressColor = progressColor;
    }

    public int getTickMarkTextColor() {
        return tickMarkTextColor;
    }

    public void setTickMarkTextColor(@ColorInt int tickMarkTextColor) {
        this.tickMarkTextColor = tickMarkTextColor;
    }

    public int getTickMarkInRangeTextColor() {
        return tickMarkInRangeTextColor;
    }

    public void setTickMarkInRangeTextColor(@ColorInt int tickMarkInRangeTextColor) {
        this.tickMarkInRangeTextColor = tickMarkInRangeTextColor;
    }

    public int getSeekBarMode() {
        return seekBarMode;
    }

    public void setSeekBarMode(@SeekBarModeDef int seekBarMode) {
        this.seekBarMode = seekBarMode;
        rightSB.setVisible(seekBarMode != SEEKBAR_MODE_SINGLE);
    }

    public int getTickMarkMode() {
        return tickMarkMode;
    }

    public void setTickMarkMode(@TickMarkModeDef int tickMarkMode) {
        this.tickMarkMode = tickMarkMode;
    }

    public int getTickMarkTextMargin() {
        return tickMarkTextMargin;
    }

    public void setTickMarkTextMargin(int tickMarkTextMargin) {
        this.tickMarkTextMargin = tickMarkTextMargin;
    }

    public int getTickMarkTextSize() {
        return tickMarkTextSize;
    }

    public void setTickMarkTextSize(int tickMarkTextSize) {
        this.tickMarkTextSize = tickMarkTextSize;
    }

    public int getTickMarkGravity() {
        return tickMarkGravity;
    }

    public void setTickMarkGravity(@TickMarkGravityDef int tickMarkGravity) {
        this.tickMarkGravity = tickMarkGravity;
    }

    public CharSequence[] getTickMarkTextArray() {
        return tickMarkTextArray;
    }

    public void setTickMarkTextArray(CharSequence[] tickMarkTextArray) {
        this.tickMarkTextArray = tickMarkTextArray;
    }

    public float getMinInterval() {
        return minInterval;
    }

    public float getProgressRadius() {
        return progressRadius;
    }

    public void setProgressRadius(float progressRadius) {
        this.progressRadius = progressRadius;
    }

    public int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(@ColorInt int progressColor) {
        this.progressColor = progressColor;
    }

    public int getProgressDefaultColor() {
        return progressDefaultColor;
    }

    public void setProgressDefaultColor(@ColorInt int progressDefaultColor) {
        this.progressDefaultColor = progressDefaultColor;
    }

    public int getProgressDrawableId() {
        return progressDrawableId;
    }

    public void setProgressDrawableId(@DrawableRes int progressDrawableId) {
        this.progressDrawableId = progressDrawableId;
        progressBitmap = null;
        initProgressBitmap();
    }

    public int getProgressDefaultDrawableId() {
        return progressDefaultDrawableId;
    }

    public void setProgressDefaultDrawableId(@DrawableRes int progressDefaultDrawableId) {
        this.progressDefaultDrawableId = progressDefaultDrawableId;
        progressDefaultBitmap = null;
        initProgressBitmap();
    }

    public int getProgressWidth() {
        return progressWidth;
    }

    public void setProgressWidth(int progressWidth) {
        this.progressWidth = progressWidth;
    }

    public void setTypeface(Typeface typeFace) {
        paint.setTypeface(typeFace);
    }

    public boolean isEnableThumbOverlap() {
        return enableThumbOverlap;
    }

    public void setEnableThumbOverlap(boolean enableThumbOverlap) {
        this.enableThumbOverlap = enableThumbOverlap;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }

    public int getStepsColor() {
        return stepsColor;
    }

    public void setStepsColor(@ColorInt int stepsColor) {
        this.stepsColor = stepsColor;
    }

    public float getStepsWidth() {
        return stepsWidth;
    }

    public void setStepsWidth(float stepsWidth) {
        this.stepsWidth = stepsWidth;
    }

    public float getStepsHeight() {
        return stepsHeight;
    }

    public void setStepsHeight(float stepsHeight) {
        this.stepsHeight = stepsHeight;
    }

    public float getStepsRadius() {
        return stepsRadius;
    }

    public void setStepsRadius(float stepsRadius) {
        this.stepsRadius = stepsRadius;
    }

    public void setProgressTop(int progressTop) {
        this.progressTop = progressTop;
    }

    public void setProgressBottom(int progressBottom) {
        this.progressBottom = progressBottom;
    }

    public void setProgressLeft(int progressLeft) {
        this.progressLeft = progressLeft;
    }

    public void setProgressRight(int progressRight) {
        this.progressRight = progressRight;
    }

    public int getTickMarkLayoutGravity() {
        return tickMarkLayoutGravity;
    }

    public void setTickMarkLayoutGravity(@TickMarkLayoutGravityDef int tickMarkLayoutGravity) {
        this.tickMarkLayoutGravity = tickMarkLayoutGravity;
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(@GravityDef int gravity) {
        this.gravity = gravity;
    }

    public boolean isStepsAutoBonding() {
        return stepsAutoBonding;
    }

    public void setStepsAutoBonding(boolean stepsAutoBonding) {
        this.stepsAutoBonding = stepsAutoBonding;
    }

    public int getStepsDrawableId() {
        return stepsDrawableId;
    }

    public void setStepsDrawableId(@DrawableRes int stepsDrawableId) {
        this.stepsBitmaps.clear();
        this.stepsDrawableId = stepsDrawableId;
        initStepsBitmap();
    }

    public List<Bitmap> getStepsBitmaps() {
        return stepsBitmaps;
    }

    public void setStepsBitmaps(List<Bitmap> stepsBitmaps) {
        this.stepsBitmaps.clear();
        this.stepsBitmaps.addAll(stepsBitmaps);
    }

    public void setStepsDrawable(List<Integer> stepsDrawableIds) {
        if (!verifyStepsMode()) {
            throw new IllegalArgumentException("stepsWidth must > 0, stepsHeight must > 0,steps must > 0 First!!");
        }
        List<Bitmap> stepsBitmaps = new ArrayList<>();
        for (int i = 0; i < stepsDrawableIds.size(); i++) {
            stepsBitmaps.add(Utils.drawableToBitmap(getContext(), (int) stepsWidth, (int) stepsHeight, stepsDrawableIds.get(i)));
        }
        setStepsBitmaps(stepsBitmaps);
    }

    public void setPseudocode(int pseudocode) {
        this.pseudocode = pseudocode;
        invalidate();
    }

    public void setColorList(@Nullable int[] colorList) {
        this.colorList = colorList;
        invalidate();
    }

    public void setPlaces(@Nullable float[] newPlaces) {
        if (newPlaces == null) {
            places = null;
        } else {
            if (places == null || places.length != newPlaces.length) {
                places = new float[newPlaces.length];
            }
            for (int i = 0; i < newPlaces.length; i++) {
                places[places.length - 1 - i] = 1 - newPlaces[i];
            }
        }
        invalidate();
    }
}
