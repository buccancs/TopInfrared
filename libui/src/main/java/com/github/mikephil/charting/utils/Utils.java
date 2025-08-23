package com.github.mikephil.charting.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.List;

public abstract class Utils {

    private static DisplayMetrics mMetrics;
    private static int mMinimumFlingVelocity = 50;
    private static int mMaximumFlingVelocity = 8000;
    public final static double DEG2RAD = (Math.PI / 180.0);
    public final static float FDEG2RAD = ((float) Math.PI / 180.f);

    @SuppressWarnings("unused")
    public final static double DOUBLE_EPSILON = Double.longBitsToDouble(1);

    @SuppressWarnings("unused")
    public final static float FLOAT_EPSILON = Float.intBitsToFloat(1);

    @SuppressWarnings("deprecation")
    public static void init(Context context) {

        if (context == null) {
            mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();

            Log.e("MPChartLib-Utils"
                    , "Utils.init(...) PROVIDED CONTEXT OBJECT IS NULL");

        } else {
            ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
            mMinimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
            mMaximumFlingVelocity = viewConfiguration.getScaledMaximumFlingVelocity();

            Resources res = context.getResources();
            mMetrics = res.getDisplayMetrics();
        }
    }

    @Deprecated
    public static void init(Resources res) {

        mMetrics = res.getDisplayMetrics();

        mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
        mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
    }

    public static float convertDpToPixel(float dp) {

        if (mMetrics == null) {

            Log.e("MPChartLib-Utils",
                    "Utils NOT INITIALIZED. You need to call Utils.init(...) at least once before" +
                            " calling Utils.convertDpToPixel(...). Otherwise conversion does not " +
                            "take place.");
            return dp;
        }

        return dp * mMetrics.density;
    }

    public static float convertPixelsToDp(float px) {

        if (mMetrics == null) {

            Log.e("MPChartLib-Utils",
                    "Utils NOT INITIALIZED. You need to call Utils.init(...) at least once before" +
                            " calling Utils.convertPixelsToDp(...). Otherwise conversion does not" +
                            " take place.");
            return px;
        }

        return px / mMetrics.density;
    }

    public static int calcTextWidth(Paint paint, String demoText) {
        return (int) paint.measureText(demoText);
    }

    private static Rect mCalcTextHeightRect = new Rect();
    
    public static int calcTextHeight(Paint paint, String demoText) {

        Rect r = mCalcTextHeightRect;
        r.set(0,0,0,0);
        paint.getTextBounds(demoText, 0, demoText.length(), r);
        return r.height();
    }

    private static Paint.FontMetrics mFontMetrics = new Paint.FontMetrics();

    public static float getLineHeight(Paint paint) {
        return getLineHeight(paint, mFontMetrics);
    }

    public static float getLineHeight(Paint paint, Paint.FontMetrics fontMetrics){
        paint.getFontMetrics(fontMetrics);
        return fontMetrics.descent - fontMetrics.ascent;
    }

    public static float getLineSpacing(Paint paint) {
        return getLineSpacing(paint, mFontMetrics);
    }

    public static float getLineSpacing(Paint paint, Paint.FontMetrics fontMetrics){
        paint.getFontMetrics(fontMetrics);
        return fontMetrics.ascent - fontMetrics.top + fontMetrics.bottom;
    }

    public static FSize calcTextSize(Paint paint, String demoText) {

        FSize result = FSize.getInstance(0,0);
        calcTextSize(paint, demoText, result);
        return result;
    }

    private static Rect mCalcTextSizeRect = new Rect();
    
    public static void calcTextSize(Paint paint, String demoText, FSize outputFSize) {

        Rect r = mCalcTextSizeRect;
        r.set(0,0,0,0);
        paint.getTextBounds(demoText, 0, demoText.length(), r);
        outputFSize.width = r.width();
        outputFSize.height = r.height();

    }

    private static final int POW_10[] = {
            1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000
    };

    private static ValueFormatter mDefaultValueFormatter = generateDefaultValueFormatter();

    private static ValueFormatter generateDefaultValueFormatter() {
        return new DefaultValueFormatter(1);
    }

    public static ValueFormatter getDefaultValueFormatter()
    {
        return mDefaultValueFormatter;
    }

    public static String formatNumber(float number, int digitCount, boolean separateThousands) {
        return formatNumber(number, digitCount, separateThousands, '.');
    }

    public static String formatNumber(float number, int digitCount, boolean separateThousands,
                                      char separateChar) {

        char[] out = new char[35];

        boolean neg = false;
        if (number == 0) {
            return "0";
        }

        boolean zero = false;
        if (number < 1 && number > -1) {
            zero = true;
        }

        if (number < 0) {
            neg = true;
            number = -number;
        }

        if (digitCount > POW_10.length) {
            digitCount = POW_10.length - 1;
        }

        number *= POW_10[digitCount];
        long lval = Math.round(number);
        int ind = out.length - 1;
        int charCount = 0;
        boolean decimalPointAdded = false;

        while (lval != 0 || charCount < (digitCount + 1)) {
            int digit = (int) (lval % 10);
            lval = lval / 10;
            out[ind--] = (char) (digit + '0');
            charCount++;

            if (charCount == digitCount) {
                out[ind--] = ',';
                charCount++;
                decimalPointAdded = true;

            } else if (separateThousands && lval != 0 && charCount > digitCount) {

                if (decimalPointAdded) {

                    if ((charCount - digitCount) % 4 == 0) {
                        out[ind--] = separateChar;
                        charCount++;
                    }

                } else {

                    if ((charCount - digitCount) % 4 == 3) {
                        out[ind--] = separateChar;
                        charCount++;
                    }
                }
            }
        }

        if (zero) {
            out[ind--] = '0';
            charCount += 1;
        }

        if (neg) {
            out[ind--] = '-';
            charCount += 1;
        }

        int start = out.length - charCount;

        return String.valueOf(out, start, out.length - start);
    }

    public static float roundToNextSignificant(double number) {
        if (Double.isInfinite(number) ||
            Double.isNaN(number) ||
            number == 0.0)
            return 0;

        final float d = (float) Math.ceil((float) Math.log10(number < 0 ? -number : number));
        final int pw = 1 - (int) d;
        final float magnitude = (float) Math.pow(10, pw);
        final long shifted = Math.round(number * magnitude);
        return shifted / magnitude;
    }

    public static int getDecimals(float number) {

        float i = roundToNextSignificant(number);

        if (Float.isInfinite(i))
            return 0;

        return (int) Math.ceil(-Math.log10(i)) + 2;
    }

    public static int[] convertIntegers(List<Integer> integers) {

        int[] ret = new int[integers.size()];

        copyIntegers(integers, ret);

        return ret;
    }

    public static void copyIntegers(List<Integer> from, int[] to){
        int count = to.length < from.size() ? to.length : from.size();
        for(int i = 0 ; i < count ; i++){
            to[i] = from.get(i);
        }
    }

    public static String[] convertStrings(List<String> strings) {

        String[] ret = new String[strings.size()];

        for (int i = 0; i < ret.length; i++) {
            ret[i] = strings.get(i);
        }

        return ret;
    }

    public static void copyStrings(List<String> from, String[] to){
        int count = to.length < from.size() ? to.length : from.size();
        for(int i = 0 ; i < count ; i++){
            to[i] = from.get(i);
        }
    }

    public static double nextUp(double d) {
        if (d == Double.POSITIVE_INFINITY)
            return d;
        else {
            d += 0.0d;
            return Double.longBitsToDouble(Double.doubleToRawLongBits(d) +
                    ((d >= 0.0d) ? +1L : -1L));
        }
    }

    public static MPPointF getPosition(MPPointF center, float dist, float angle) {

        MPPointF p = MPPointF.getInstance(0,0);
        getPosition(center, dist, angle, p);
        return p;
    }

    public static void getPosition(MPPointF center, float dist, float angle, MPPointF outputPoint){
        outputPoint.x = (float) (center.x + dist * Math.cos(Math.toRadians(angle)));
        outputPoint.y = (float) (center.y + dist * Math.sin(Math.toRadians(angle)));
    }

    public static void velocityTrackerPointerUpCleanUpIfNecessary(MotionEvent ev,
                                                                  VelocityTracker tracker) {

        tracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
        final int upIndex = ev.getActionIndex();
        final int id1 = ev.getPointerId(upIndex);
        final float x1 = tracker.getXVelocity(id1);
        final float y1 = tracker.getYVelocity(id1);
        for (int i = 0, count = ev.getPointerCount(); i < count; i++) {
            if (i == upIndex)
                continue;

            final int id2 = ev.getPointerId(i);
            final float x = x1 * tracker.getXVelocity(id2);
            final float y = y1 * tracker.getYVelocity(id2);

            final float dot = x + y;
            if (dot < 0) {
                tracker.clear();
                break;
            }
        }
    }

    @SuppressLint("NewApi")
    public static void postInvalidateOnAnimation(View view) {
        if (Build.VERSION.SDK_INT >= 16)
            view.postInvalidateOnAnimation();
        else
            view.postInvalidateDelayed(10);
    }

    public static int getMinimumFlingVelocity() {
        return mMinimumFlingVelocity;
    }

    public static int getMaximumFlingVelocity() {
        return mMaximumFlingVelocity;
    }

    public static float getNormalizedAngle(float angle) {
        while (angle < 0.f)
            angle += 360.f;

        return angle % 360.f;
    }

    private static Rect mDrawableBoundsCache = new Rect();

    public static void drawImage(Canvas canvas,
                                 Drawable drawable,
                                 int x, int y,
                                 int width, int height) {

        MPPointF drawOffset = MPPointF.getInstance();
        drawOffset.x = x - (width / 2);
        drawOffset.y = y - (height / 2);

        drawable.copyBounds(mDrawableBoundsCache);
        drawable.setBounds(
                mDrawableBoundsCache.left,
                mDrawableBoundsCache.top,
                mDrawableBoundsCache.left + width,
                mDrawableBoundsCache.top + width);

        int saveId = canvas.save();
        canvas.translate(drawOffset.x, drawOffset.y);
        drawable.draw(canvas);
        canvas.restoreToCount(saveId);
    }

    private static Rect mDrawTextRectBuffer = new Rect();
    private static Paint.FontMetrics mFontMetricsBuffer = new Paint.FontMetrics();

    public static void drawXAxisValue(Canvas c, String text, float x, float y,
                                      Paint paint,
                                      MPPointF anchor, float angleDegrees) {

        float drawOffsetX = 0.f;
        float drawOffsetY = 0.f;

        final float lineHeight = paint.getFontMetrics(mFontMetricsBuffer);
        paint.getTextBounds(text, 0, text.length(), mDrawTextRectBuffer);

        drawOffsetX -= mDrawTextRectBuffer.left;

        drawOffsetY += -mFontMetricsBuffer.ascent;

        Paint.Align originalTextAlign = paint.getTextAlign();
        paint.setTextAlign(Paint.Align.LEFT);

        if (angleDegrees != 0.f) {

            drawOffsetX -= mDrawTextRectBuffer.width() * 0.5f;
            drawOffsetY -= lineHeight * 0.5f;

            float translateX = x;
            float translateY = y;

            if (anchor.x != 0.5f || anchor.y != 0.5f) {
                final FSize rotatedSize = getSizeOfRotatedRectangleByDegrees(
                        mDrawTextRectBuffer.width(),
                        lineHeight,
                        angleDegrees);

                translateX -= rotatedSize.width * (anchor.x - 0.5f);
                translateY -= rotatedSize.height * (anchor.y - 0.5f);
                FSize.recycleInstance(rotatedSize);
            }

            c.save();
            c.translate(translateX, translateY);
            c.rotate(angleDegrees);

            c.drawText(text, drawOffsetX, drawOffsetY, paint);

            c.restore();
        } else {
            if (anchor.x != 0.f || anchor.y != 0.f) {

                drawOffsetX -= mDrawTextRectBuffer.width() * anchor.x;
                drawOffsetY -= lineHeight * anchor.y;
            }

            drawOffsetX += x;
            drawOffsetY += y;

            c.drawText(text, drawOffsetX, drawOffsetY, paint);
        }

        paint.setTextAlign(originalTextAlign);
    }

    public static void drawMultilineText(Canvas c, StaticLayout textLayout,
                                         float x, float y,
                                         TextPaint paint,
                                         MPPointF anchor, float angleDegrees) {

        float drawOffsetX = 0.f;
        float drawOffsetY = 0.f;
        float drawWidth;
        float drawHeight;

        final float lineHeight = paint.getFontMetrics(mFontMetricsBuffer);

        drawWidth = textLayout.getWidth();
        drawHeight = textLayout.getLineCount() * lineHeight;

        drawOffsetX -= mDrawTextRectBuffer.left;

        drawOffsetY += drawHeight;

        Paint.Align originalTextAlign = paint.getTextAlign();
        paint.setTextAlign(Paint.Align.LEFT);

        if (angleDegrees != 0.f) {

            drawOffsetX -= drawWidth * 0.5f;
            drawOffsetY -= drawHeight * 0.5f;

            float translateX = x;
            float translateY = y;

            if (anchor.x != 0.5f || anchor.y != 0.5f) {
                final FSize rotatedSize = getSizeOfRotatedRectangleByDegrees(
                        drawWidth,
                        drawHeight,
                        angleDegrees);

                translateX -= rotatedSize.width * (anchor.x - 0.5f);
                translateY -= rotatedSize.height * (anchor.y - 0.5f);
                FSize.recycleInstance(rotatedSize);
            }

            c.save();
            c.translate(translateX, translateY);
            c.rotate(angleDegrees);

            c.translate(drawOffsetX, drawOffsetY);
            textLayout.draw(c);

            c.restore();
        } else {
            if (anchor.x != 0.f || anchor.y != 0.f) {

                drawOffsetX -= drawWidth * anchor.x;
                drawOffsetY -= drawHeight * anchor.y;
            }

            drawOffsetX += x;
            drawOffsetY += y;

            c.save();

            c.translate(drawOffsetX, drawOffsetY);
            textLayout.draw(c);

            c.restore();
        }

        paint.setTextAlign(originalTextAlign);
    }

    public static void drawMultilineText(Canvas c, String text,
                                         float x, float y,
                                         TextPaint paint,
                                         FSize constrainedToSize,
                                         MPPointF anchor, float angleDegrees) {

        StaticLayout textLayout = new StaticLayout(
                text, 0, text.length(),
                paint,
                (int) Math.max(Math.ceil(constrainedToSize.width), 1.f),
                Layout.Alignment.ALIGN_NORMAL, 1.f, 0.f, false);

        drawMultilineText(c, textLayout, x, y, paint, anchor, angleDegrees);
    }

    public static FSize getSizeOfRotatedRectangleByDegrees(FSize rectangleSize, float degrees) {
        final float radians = degrees * FDEG2RAD;
        return getSizeOfRotatedRectangleByRadians(rectangleSize.width, rectangleSize.height,
                radians);
    }

    public static FSize getSizeOfRotatedRectangleByRadians(FSize rectangleSize, float radians) {
        return getSizeOfRotatedRectangleByRadians(rectangleSize.width, rectangleSize.height,
                radians);
    }

    public static FSize getSizeOfRotatedRectangleByDegrees(float rectangleWidth, float
            rectangleHeight, float degrees) {
        final float radians = degrees * FDEG2RAD;
        return getSizeOfRotatedRectangleByRadians(rectangleWidth, rectangleHeight, radians);
    }

    public static FSize getSizeOfRotatedRectangleByRadians(float rectangleWidth, float
            rectangleHeight, float radians) {
        return FSize.getInstance(
                Math.abs(rectangleWidth * (float) Math.cos(radians)) + Math.abs(rectangleHeight *
                        (float) Math.sin(radians)),
                Math.abs(rectangleWidth * (float) Math.sin(radians)) + Math.abs(rectangleHeight *
                        (float) Math.cos(radians))
        );
    }

    public static int getSDKInt() {
        return Build.VERSION.SDK_INT;
    }
}
