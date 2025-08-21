package com.topdon.tc001.view;

/**
 * @author: CaiSongL
 * @date: 2023/6/3 14:43
 */
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLSurfaceView extends GLSurfaceView {
    private MyRenderer renderer;

    public MyGLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        renderer = new MyRenderer();
        setRenderer(renderer);
    }

    private class MyRenderer implements GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
            // 初始化OpenGL环境，设置背景色等
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            // 其他初始化操作...
        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int width, int height) {
            // 处理窗口大小变化，设置视口和投影矩阵
            GLES20.glViewport(0, 0, width, height);
            // 其他处理...
        }

        @Override
        public void onDrawFrame(GL10 gl10) {
            // 渲染场景，绘制点云
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            // 绘制点云...
        }
    }
}

