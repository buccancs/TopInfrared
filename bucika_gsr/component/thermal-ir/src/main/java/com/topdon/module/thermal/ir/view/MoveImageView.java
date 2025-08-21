package com.topdon.module.thermal.ir.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class MoveImageView extends ImageView {

    private static final String TAG = "MoveImageView";
    private float mPreX;
    private float mPreY;

    public MoveImageView(Context context) {
        this(context, null);
    }

    public MoveImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public MoveImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "ACTION_DOWN");
                mPreX = event.getX();
                mPreY = event.getY();
                lastClickTime = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "ACTION_MOVE");
                float preX = mPreX;
                float preY = mPreY;
                float curX = event.getX();
                float curY = event.getY();

                if (onMoveListener != null && delayMoveTime()) {

                    Log.d(TAG, "ACTION_MOVE isFastClick");
                    onMoveListener.onMove(preX, preY, curX, curY);
                    mPreX = curX;
                    mPreY = curY;
                }
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP");
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.d(TAG, "ACTION_CANCEL");
                break;

        }
        return true;
    }
    private static final int MIN_CLICK_DELAY_TIME = 100;
    private static long lastClickTime;

    //最多70毫秒执行一次move
    public static boolean delayMoveTime() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) < MIN_CLICK_DELAY_TIME) {
            flag = false;
        } else {
            flag = true;
            lastClickTime = System.currentTimeMillis();
        }
        Log.d(TAG, "ACTION_MOVE isFastClick flag : " + flag);
        return flag;
    }

    public interface OnMoveListener {
        void onMove(float preX, float preY, float curX, float curY);
    }

    public OnMoveListener onMoveListener;

    public void setOnMoveListener(OnMoveListener onMoveListener) {
        this.onMoveListener = onMoveListener;
    }
}
