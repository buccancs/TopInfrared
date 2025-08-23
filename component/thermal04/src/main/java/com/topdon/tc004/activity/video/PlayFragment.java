package com.topdon.tc004.activity.video;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.elvishew.xlog.XLog;
import com.topdon.tc004.R;
import com.topdon.tc004.util.FileUtil;
import com.topdon.tc004.util.SPUtil;

import org.easydarwin.video.Client;
import org.easydarwin.video.EasyPlayerClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.UUID;

public class PlayFragment extends Fragment implements TextureView.SurfaceTextureListener{
    protected static final String TAG = "PlayFragment";

    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_TRANSPORT_MODE = "ARG_TRANSPORT_MODE";
    public static final String ARG_SEND_OPTION = "ARG_SEND_OPTION";
    public static final String ARG_PARAM3 = "param3";
    public static final String P_TC007 = "IS_TC007";

    public static final int RESULT_REND_START = 1;
    public static final int RESULT_REND_VIDEO_DISPLAY = 2;
    public static final int RESULT_REND_STOP = -1;

    public static final int ASPECT_RATIO_INSIDE = 1;
    public static final int ASPECT_RATIO_CROPS_MATRIX = 2;
    public static final int ASPECT_RATIO_CENTER_CROPS = 3;
    public static final int FILL_WINDOW = 4;

    private int mRatioType = ASPECT_RATIO_INSIDE;

    protected String mUrl;
    protected int mType;
    protected int sendOption;

    private ResultReceiver mRR;

    protected EasyPlayerClient mStreamRender;
    protected ResultReceiver mResultReceiver;

    protected int mWidth;
    protected int mHeight;

    protected View.OnLayoutChangeListener listener;

    private ImageView mRenderCover;
    private ImageView mTakePictureThumb;
    protected TextureView mSurfaceView;
    private SurfaceTexture mSurfaceTexture;
    protected ImageView cover;
    private ImageView ivCover;

    private MediaScannerConnection mScanner;

    private AsyncTask<Void, Void, Bitmap> mLoadingPictureThumbTask;

    private OnDoubleTapListener doubleTapListener;

    private final Runnable mAnimationHiddenTakePictureThumbTask = new Runnable() {
        @Override
        public void run() {
            ViewCompat.animate(mTakePictureThumb).scaleX(0.0f).scaleY(0.0f).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    super.onAnimationEnd(view);
                    view.setVisibility(View.INVISIBLE);
                }
            });
        }
    };

    public static PlayFragment newInstance(String url, int transportMode, int sendOption, ResultReceiver rr) {
       return newInstance(url,transportMode,sendOption,rr,false);
    }

    public static PlayFragment newInstance(String url, int transportMode, int sendOption, ResultReceiver rr,boolean isTs007) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, url);
        args.putInt(ARG_TRANSPORT_MODE, transportMode);
        args.putInt(ARG_SEND_OPTION, sendOption);
        args.putParcelable(ARG_PARAM3, rr);
        args.putBoolean(P_TC007,isTs007);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_PARAM1);
            mType = getArguments().getInt(ARG_TRANSPORT_MODE);
            sendOption = getArguments().getInt(ARG_SEND_OPTION);
            mRR = getArguments().getParcelable(ARG_PARAM3);
            boolean isTC007 = getArguments().getBoolean(P_TC007);
            if (!isTC007){
                setRetainInstance(true);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_play, container, false);
        cover = (ImageView) view.findViewById(R.id.surface_cover);
        ivCover = view.findViewById(R.id.iv_not_connect_space);

        if (!TextUtils.isEmpty(mUrl)) {
        }
        return view;
    }

    private boolean isVideoDisplayed;

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isVideoDisplayed) {
            view.findViewById(R.id.iv_not_connect_space).setVisibility(View.GONE);
        }

        mSurfaceView = (TextureView) view.findViewById(R.id.surface_view);
        mSurfaceView.setOpaque(false);
        mSurfaceView.setSurfaceTextureListener(this);

        mRenderCover = (ImageView) getView().findViewById(R.id.surface_cover);
        mTakePictureThumb = (ImageView) getView().findViewById(R.id.live_video_snap_thumb);
        mResultReceiver = new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);

                Activity activity = getActivity();

                if (activity == null)
                    return;

                if (resultCode == EasyPlayerClient.RESULT_VIDEO_DISPLAYED) {
                    if (resultData != null) {
                        int videoDecodeType = resultData.getInt(EasyPlayerClient.KEY_VIDEO_DECODE_TYPE, 0);
                        Log.i(TAG, "视频解码方式:" + (videoDecodeType == 0 ? "软解码" : "硬解码"));
                    }
                    isVideoDisplayed = true;
                    onVideoDisplayed();
                } else if (resultCode == EasyPlayerClient.RESULT_VIDEO_SIZE) {
                    mWidth = resultData.getInt(EasyPlayerClient.EXTRA_VIDEO_WIDTH);
                    mHeight = resultData.getInt(EasyPlayerClient.EXTRA_VIDEO_HEIGHT);

                    onVideoSizeChange();
                } else if (resultCode == EasyPlayerClient.RESULT_TIMEOUT) {
                    ivCover.setVisibility(View.VISIBLE);
                    new AlertDialog.Builder(getActivity()).setMessage("试播时间到").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
                } else if (resultCode == EasyPlayerClient.RESULT_UNSUPPORTED_AUDIO) {
                    new AlertDialog.Builder(getActivity()).setMessage("音频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
                } else if (resultCode == EasyPlayerClient.RESULT_UNSUPPORTED_VIDEO) {
                    new AlertDialog.Builder(getActivity()).setMessage("视频格式不支持").setTitle("SORRY").setPositiveButton(android.R.string.ok, null).show();
                } else if (resultCode == EasyPlayerClient.RESULT_EVENT) {
                    int errorCode = resultData.getInt("errorcode");

                    Log.i(TAG, "errorCode:" + errorCode);
                } else if (resultCode == EasyPlayerClient.RESULT_RECORD_BEGIN) {
                } else if (resultCode == EasyPlayerClient.RESULT_RECORD_END) {
                }
            }
        };

        listener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.d(TAG, String.format("onLayoutChange left:%d,top:%d,right:%d,bottom:%d->oldLeft:%d,oldTop:%d,oldRight:%d,oldBottom:%d", left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom));

                if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
                    onVideoSizeChange();
                }
            }
        };

        ViewGroup parent = (ViewGroup) view.getParent();
        parent.addOnLayoutChangeListener(listener);

        GestureDetector.SimpleOnGestureListener sgl = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (doubleTapListener != null)
                    doubleTapListener.onDoubleTab(PlayFragment.this);

                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (doubleTapListener != null)
                    doubleTapListener.onSingleTab(PlayFragment.this);

                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        };

        final GestureDetector gd = new GestureDetector(getContext(), sgl);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gd.onTouchEvent(event);
            }
        });
    }

    public TextureView getTextureView(){
        return mSurfaceView;
    }

    @Override
    public void onPause() {
        super.onPause();

        onVideoDisplayed();
    }

    @Override
    public void onDestroyView() {
        ViewGroup parent = (ViewGroup) getView().getParent();
        if (parent!=null){
            parent.removeOnLayoutChangeListener(listener);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        stopRending();
        super.onDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (hidden) {
            if (mStreamRender != null) {
                mStreamRender.pause();
            }
        } else {
            if (mStreamRender != null) {
                mStreamRender.resume();
            }
        }
    }

    private void onVideoDisplayed() {
        View view = getView();
        Log.i(TAG, String.format("VIDEO DISPLAYED!!!!%d*%d", mWidth, mHeight));
        view.findViewById(android.R.id.progress).setVisibility(View.GONE);
        view.findViewById(R.id.iv_not_connect_space).setVisibility(View.GONE);
        mSurfaceView.post(new Runnable() {
            @Override
            public void run() {
                if (mWidth != 0 && mHeight != 0) {
                }
            }
        });
        ivCover.setVisibility(View.GONE);
        cover.setVisibility(View.GONE);
        sendResult(RESULT_REND_VIDEO_DISPLAY, null);
    }

    public boolean onRecordOrStop() {
        if (!mStreamRender.isRecording()) {
            mStreamRender.startRecord(FileUtil.getMovieName(mUrl).getPath());
            return true;
        } else {
            mStreamRender.stopRecord();
            return false;
        }
    }

    public boolean toggleAudioEnable() {
        if (mStreamRender == null) {
            return false;
        }

        mStreamRender.setAudioEnable(!mStreamRender.isAudioEnable());
        return mStreamRender.isAudioEnable();
    }

    protected void startRending(SurfaceTexture surface) {
        mStreamRender = new EasyPlayerClient(getContext(), new Surface(surface), mResultReceiver, new EasyPlayerClient.I420DataCallback() {
            @Override
            public void onI420Data(ByteBuffer byteBuffer) {
            }
        });

        boolean autoRecord = SPUtil.getAutoRecord(getContext());

        File f = new File(FileUtil.getMoviePath(mUrl));
        f.mkdirs();

        try {
            mStreamRender.start(mUrl,
                    Client.TRANSTYPE_TCP,
                    sendOption,
                    Client.EASY_SDK_VIDEO_FRAME_FLAG | Client.EASY_SDK_AUDIO_FRAME_FLAG,
                    "",
                    "",
                    autoRecord ? FileUtil.getMovieName(mUrl).getPath() : null);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        sendResult(RESULT_REND_START, null);
    }

    private void stopRending() {
        if (mStreamRender != null) {
            sendResult(RESULT_REND_STOP, null);
            mStreamRender.stop();
            mStreamRender = null;
        }
    }

    public void takePicture(final String path) {
        try {
            if (mWidth <= 0 || mHeight <= 0) {
                return;
            }

            Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            mSurfaceView.getBitmap(bitmap);
            saveBitmapInFile(path, bitmap);
            bitmap.recycle();

            mRenderCover.setImageDrawable(new ColorDrawable(getResources().getColor(android.R.color.white)));
            mRenderCover.setVisibility(View.VISIBLE);
            mRenderCover.setAlpha(1.0f);

            ViewCompat.animate(mRenderCover).cancel();
            ViewCompat.animate(mRenderCover).alpha(0.3f).setListener(new ViewPropertyAnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(View view) {
                    super.onAnimationEnd(view);
                    mRenderCover.setVisibility(View.GONE);
                }
            });

            if (mLoadingPictureThumbTask != null)
                mLoadingPictureThumbTask.cancel(true);

            final int w = mTakePictureThumb.getWidth();
            final int h = mTakePictureThumb.getHeight();

            mLoadingPictureThumbTask = new AsyncTask<Void, Void, Bitmap>() {
                final WeakReference<ImageView> mImageViewRef = new WeakReference<>(mTakePictureThumb);
                final String mPath = path;

                @Override
                protected Bitmap doInBackground(Void... params) {
                    return decodeSampledBitmapFromResource(mPath, w, h);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);

                    if (isCancelled()) {
                        bitmap.recycle();
                        return;
                    }

                    ImageView iv = mImageViewRef.get();

                    if (iv == null)
                        return;

                    iv.setImageBitmap(bitmap);
                    iv.setVisibility(View.VISIBLE);
                    iv.removeCallbacks(mAnimationHiddenTakePictureThumbTask);
                    iv.clearAnimation();

                    ViewCompat.animate(iv).scaleX(1.0f).scaleY(1.0f).setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            super.onAnimationEnd(view);
                            view.postOnAnimationDelayed(mAnimationHiddenTakePictureThumbTask, 4000);
                        }
                    });

                    iv.setTag(mPath);
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void saveBitmapInFile(final String path, Bitmap bitmap) {
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);

            if (mScanner == null) {
                MediaScannerConnection connection = new MediaScannerConnection(getContext(), new MediaScannerConnection.MediaScannerConnectionClient() {
                    public void onMediaScannerConnected() {
                        mScanner.scanFile(path, null );
                    }

                    public void onScanCompleted(String path1, Uri uri) {
                        if (path1.equals(path)) {
                            mScanner.disconnect();
                            mScanner = null;
                        }
                    }
                });

                try {
                    connection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mScanner = connection;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void enterFullscreen() {
        setScaleType(FILL_WINDOW);
    }

    public void quiteFullscreen() {
        setScaleType(ASPECT_RATIO_CROPS_MATRIX);
    }

    private void onVideoSizeChange() {
        try {
            Log.i(TAG, String.format("RESULT_VIDEO_SIZE RECEIVED :%d*%d", mWidth, mHeight));

            if (mWidth == 0 || mHeight == 0)
                return;
            if (mRatioType == ASPECT_RATIO_CROPS_MATRIX) {
                ViewGroup parent = (ViewGroup) getView().getParent();
                parent.addOnLayoutChangeListener(listener);
                fixPlayerRatio(getView(), parent.getWidth(), parent.getHeight());

                mSurfaceView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                mSurfaceView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;

            } else {
                mSurfaceView.setTransform(new Matrix());
                float ratioView = getView().getWidth() * 1.0f / getView().getHeight();
                float ratio = mWidth * 1.0f / mHeight;

                switch (mRatioType) {
                    case ASPECT_RATIO_INSIDE: {
                        if (ratioView - ratio < 0) {
                            mSurfaceView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                            mSurfaceView.getLayoutParams().height = (int) (getView().getWidth() / ratio + 0.5f);
                        } else {
                            mSurfaceView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                            mSurfaceView.getLayoutParams().width = (int) (getView().getHeight() * ratio + 0.5f);
                        }
                    }
                    break;
                    case ASPECT_RATIO_CENTER_CROPS: {
                        if (ratioView - ratio < 0) {
                            mSurfaceView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                            mSurfaceView.getLayoutParams().width = (int) (getView().getHeight() * ratio + 0.5f);
                        } else {
                            mSurfaceView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                            mSurfaceView.getLayoutParams().height = (int) (getView().getWidth() / ratio + 0.5f);
                        }
                    }
                    break;
                    case FILL_WINDOW: {
                        mSurfaceView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                        mSurfaceView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                    break;
                }
            }

            mSurfaceView.requestLayout();
        }catch (Exception e){
            XLog.d(e.getMessage());
        }
    }

    protected void sendResult(int resultCode, Bundle resultData) {
        if (mRR != null)
            mRR.send(resultCode, resultData);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mSurfaceTexture != null) {
            mSurfaceView.setSurfaceTexture(mSurfaceTexture);
        } else {
            startRending(surface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurfaceTexture = surface;
        return false;

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public interface OnDoubleTapListener {
        void onDoubleTab(PlayFragment f);

        void onSingleTab(PlayFragment f);
    }

    public boolean isAudioEnable() {
        return mStreamRender != null && mStreamRender.isAudioEnable();
    }

    public void setScaleType(@IntRange(from = ASPECT_RATIO_INSIDE, to = FILL_WINDOW) int type) {
        mRatioType = type;

        if (mWidth != 0 && mHeight != 0) {
            onVideoSizeChange();
        }
    }

    public void setOnDoubleTapListener(OnDoubleTapListener listener) {
        this.doubleTapListener = listener;
    }

    public long getReceivedStreamLength() {
        if (mStreamRender != null) {
            return mStreamRender.receivedDataLength();
        }

        return 0;
    }

    public void setUrl(String url) {
        this.mUrl = url;

    }

    public void setTransType(int transType) {
        this.mType = transType;
    }

    public void setResultReceiver(ResultReceiver rr) {
        mRR = rr;
    }

    public void setSelected(boolean selected) {
        mSurfaceView.animate().scaleX(selected ? 0.9f : 1.0f);
        mSurfaceView.animate().scaleY(selected ? 0.9f : 1.0f);
        mSurfaceView.animate().alpha(selected ? 0.7f : 1.0f);
    }

    protected void fixPlayerRatio(View renderView, int maxWidth, int maxHeight) {
    }

    protected void fixPlayerRatio(View renderView, int widthSize, int heightSize, int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }

        float aspectRatio = width * 1.0f / height;

        if (widthSize > heightSize * aspectRatio) {
            height = heightSize;
            width = (int) (height * aspectRatio);
        } else {
            width = widthSize;
            height = (int) (width / aspectRatio);
        }

        renderView.getLayoutParams().width = width;
        renderView.getLayoutParams().height = height;
        renderView.requestLayout();
    }

    public static class ReverseInterpolator extends AccelerateDecelerateInterpolator {
        @Override
        public float getInterpolation(float paramFloat) {
            return super.getInterpolation(1.0f - paramFloat);
        }
    }

    protected boolean isLandscape() {
        return getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || getActivity().getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
    }

    public int getSurfaceViewWidth(){
        return mSurfaceView.getMeasuredWidth();
    }
    public int getSurfaceViewHeight(){
        return mSurfaceView.getMeasuredHeight();
    }
}
