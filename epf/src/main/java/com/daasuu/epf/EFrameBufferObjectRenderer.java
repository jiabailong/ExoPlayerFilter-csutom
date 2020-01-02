package com.daasuu.epf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;

import com.daasuu.epf.filter.GlColorOverlaySample;
import com.daasuu.epf.filter.GlFilter;
import com.daasuu.epf.filter.GlFilterZoom;
import com.daasuu.epf.filter.GlGaussianBlurFilter;
import com.daasuu.epf.filter.GlGrayScaleFilter;

import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

abstract class EFrameBufferObjectRenderer implements GLSurfaceView.Renderer {

    private EFramebufferObject framebufferObject;
    private GlFilter normalShader;
    private GlFilterZoom glFilterZoom;
//    private GlGaussianBlurFilter glGaussianBlurFilter;
    private GlColorOverlaySample glGrayScaleFilter;
//                return new GlBitmapOverlaySample(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round));

    private final Queue<Runnable> runOnDraw;


    EFrameBufferObjectRenderer() {
        runOnDraw = new LinkedList<Runnable>();
    }


    @Override
    public final void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
        framebufferObject = new EFramebufferObject();

        normalShader = new GlFilter();
        glFilterZoom = new GlFilterZoom();
//        glGaussianBlurFilter=new GlGaussianBlurFilter();

        glGrayScaleFilter = new GlColorOverlaySample();;
        normalShader.setup();
//        glGaussianBlurFilter.setup();
        glFilterZoom.setup();
        glGrayScaleFilter.setup();
        onSurfaceCreated(config);
    }

    @Override
    public final void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        framebufferObject.setup(width, height);
        normalShader.setFrameSize(width, height);
//        glGaussianBlurFilter.setFrameSize(width, height);
        glFilterZoom.setFrameSize(width, height);
        Bitmap bmp=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Bitmap bmp2=null;
        Canvas canvas=new Canvas(bmp);
        canvas.drawColor(Color.parseColor("#000000"));
        // bmp.recycle();
        glGrayScaleFilter.setBitmap(bmp);
        glGrayScaleFilter.setFrameSize(width, height);
        onSurfaceChanged(width, height);
    }

    @Override
    public final void onDrawFrame(final GL10 gl) {
        synchronized (runOnDraw) {
            while (!runOnDraw.isEmpty()) {
                runOnDraw.poll().run();
            }
        }
        framebufferObject.enable();
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());
        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        onDrawFrame(framebufferObject);





        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, framebufferObject.getWidth(), framebufferObject.getHeight());

        GLES20.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glGrayScaleFilter.draw(framebufferObject.getTexName(), framebufferObject);
//        glGaussianBlurFilter.draw(framebufferObject.getTexName(), framebufferObject);
        glFilterZoom.draw(framebufferObject.getTexName(), null);
//        normalShader.draw(framebufferObject.getTexName(), null); //



    }


    @Override
    protected void finalize() throws Throwable {

    }

    public abstract void onSurfaceCreated(EGLConfig config);

    public abstract void onSurfaceChanged(int width, int height);

    public abstract void onDrawFrame(EFramebufferObject fbo);
    public abstract Context getAppContext();
}
