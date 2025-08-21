package com.infisense.usbir.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import com.infisense.usbir.R;
import com.topdon.lib.core.utils.BitmapUtils;

public class ZoomableDraggableView extends View {
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private Matrix matrix = new Matrix();
    private float scaleFactor = 1.0f;
    private float minScaleFactor = 0.5f;
    private float maxScaleFactor = 2.0f;
    private float focusX, focusY;
    private float lastX, lastY;

    //原始图片
    private Bitmap originalBitmap;
    private int imageWidth;
    private int imageHeight;
    private int viewWidth;
    private int viewHeight;
    private float xscale;
    private float yscale;
    private float originalBitmapWidth;
    private float originalBitmapHeight;

    private float pxBitmapHeight = 150;

    private float showBitmapHeightWidth = 0f;
    private float showBitmapHeight = 0f;
    private Paint paint = new Paint();

    private Bitmap showBitmap;

    public ZoomableDraggableView(Context context) {
        super(context);
        init(context);
    }

    public ZoomableDraggableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());
        originalBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.svg_ic_target_horizontal_person_green)).getBitmap();
        originalBitmapWidth = originalBitmap.getWidth();
        originalBitmapHeight = originalBitmap.getHeight();
    }


    public void setImageSize(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        viewWidth = ((ViewGroup)getParent()).getMeasuredWidth();
        viewHeight = ((ViewGroup)getParent()).getMeasuredHeight();
        if (viewWidth != 0) {
            xscale = (float) viewWidth / (float) imageWidth;
        }
        if (viewHeight != 0) {
            yscale = (float) viewHeight / (float) imageHeight;
        }
        showBitmapHeight = pxBitmapHeight / yscale;
        showBitmapHeightWidth = pxBitmapHeight * originalBitmapWidth / originalBitmapHeight * xscale;
        showBitmap = BitmapUtils.scaleWithWH(originalBitmap,showBitmapHeightWidth,showBitmapHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.concat(matrix);
        if (showBitmap!=null){
            canvas.drawBitmap(showBitmap,matrix,paint);
        }
        // 在此处绘制你的内容
        super.onDraw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(minScaleFactor, Math.min(scaleFactor, maxScaleFactor));

            focusX = detector.getFocusX();
            focusY = detector.getFocusY();

            matrix.setScale(scaleFactor, scaleFactor, focusX, focusY);

            invalidate();

            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            lastX = e.getX();
            lastY = e.getY();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float deltaX = e2.getX() - lastX;
            float deltaY = e2.getY() - lastY;

            lastX = e2.getX();
            lastY = e2.getY();

            // 将滚动距离根据缩放因子进行调整
            deltaX /= scaleFactor;
            deltaY /= scaleFactor;

            matrix.postTranslate(-deltaX, -deltaY);

            invalidate();

            return true;
        }
    }
}
