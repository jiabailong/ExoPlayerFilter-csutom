package com.daasuu.epf.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.daasuu.epf.Resolution;

import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;

/**
 * Created by sudamasayuki on 2017/05/18.
 */

public abstract class GlOverlayFilter extends GlFilterBg2 {

    private int[] textures = new int[1];

    private Bitmap bitmap = null;

    protected Resolution inputResolution = new Resolution(1280, 720);

    public GlOverlayFilter() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
    }

//    private final static String FRAGMENT_SHADER =
//            "precision mediump float;\n" +
//                    "varying vec2 vTextureCoord;\n" +
//                    "uniform lowp sampler2D sTexture;\n" +
//                    "uniform lowp sampler2D oTexture;\n" +
//                    "void main() {\n" +
//                    "   lowp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
//                    "   lowp vec4 textureColor2 = texture2D(oTexture, vTextureCoord);\n" +
//                    "   \n" +
//                    "   gl_FragColor = mix(textureColor, textureColor2, textureColor2.a);\n" +
//                    "}\n";

    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;" +
                    "attribute vec4 aTextureCoord;" +

                    "const lowp int GAUSSIAN_SAMPLES = 9;" +

                    "uniform highp float texelWidthOffset;" +
                    "uniform highp float texelHeightOffset;" +
                    "uniform highp float blurSize;" +

                    "varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];" +

                    "void main() {" +
                    "gl_Position = aPosition;" +
                    "highp vec2 vTextureCoord = aTextureCoord.xy;" +

                    // Calculate the positions for the blur
                    "int multiplier = 0;" +
                    "highp vec2 blurStep;" +
                    "highp vec2 singleStepOffset = vec2(texelHeightOffset, texelWidthOffset) * blurSize;" +

                    "for (lowp int i = 0; i < GAUSSIAN_SAMPLES; i++) {" +
                    "multiplier = (i - ((GAUSSIAN_SAMPLES - 1) / 2));" +
                    // Blur in x (horizontal)
                    "blurStep = float(multiplier) * singleStepOffset;" +
                    "blurCoordinates[i] = vTextureCoord.xy + blurStep;" +
                    "}" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "uniform lowp sampler2D oTexture;\n" +
                    "const lowp int GAUSSIAN_SAMPLES = 9;" +
                    "varying highp vec2 blurCoordinates[GAUSSIAN_SAMPLES];" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform lowp sampler2D ssTexture;\n" +
                    "uniform lowp sampler2D sTexture;" +
                    "uniform lowp sampler2D ddTexture;" +

                    "void main() {" +
                    "   lowp vec4 textureColor = texture2D(ssTexture, vTextureCoord);\n" +
//                    "   \n" +

                    "lowp vec4 sum = vec4(0.0);" +

                    "sum += texture2D(sTexture, blurCoordinates[0]) * 0.05;" +
                    "sum += texture2D(sTexture, blurCoordinates[1]) * 0.09;" +
                    "sum += texture2D(sTexture, blurCoordinates[2]) * 0.12;" +
                    "sum += texture2D(sTexture, blurCoordinates[3]) * 0.15;" +
                    "sum += texture2D(sTexture, blurCoordinates[4]) * 0.18;" +
                    "sum += texture2D(sTexture, blurCoordinates[5]) * 0.15;" +
                    "sum += texture2D(sTexture, blurCoordinates[6]) * 0.12;" +
                    "sum += texture2D(sTexture, blurCoordinates[7]) * 0.09;" +
                    "sum += texture2D(sTexture, blurCoordinates[8]) * 0.05;" +
//                    "   lowp vec4 textureColor3 = mix(textureColor2, sum, sum.a);\n" +
//                    "   lowp vec4 textureColor2 = texture2D(ddTexture, sum);\n" +
                    "   gl_FragColor = mix(sum, textureColor, 0.7f);\n" +
//
//                    "gl_FragColor = sum;" +
                    "}";

    public void setResolution(Resolution resolution) {
        this.inputResolution = resolution;
    }

    @Override
    public void setFrameSize(int width, int height) {
        super.setFrameSize(width, height);
        setResolution(new Resolution(width, height));
    }

    private void createBitmap() {
        releaseBitmap(bitmap);
        bitmap = Bitmap.createBitmap(inputResolution.width(), inputResolution.height(), Bitmap.Config.ARGB_8888);
    }

    @Override
    public void setup() {
        super.setup();// 1
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        createBitmap();
    }
    private float texelWidthOffset = 0.01f;
    private float texelHeightOffset = 0.01f;
    private float blurSize = 0.3f;
    @Override
    public void onDraw() {
        GLES20.glUniform1f(getHandle("texelWidthOffset"), texelWidthOffset);
        GLES20.glUniform1f(getHandle("texelHeightOffset"), texelHeightOffset);
        GLES20.glUniform1f(getHandle("blurSize"), blurSize);
        if (bitmap == null) {
            createBitmap();
        }
        if (bitmap.getWidth() != inputResolution.width() || bitmap.getHeight() != inputResolution.height()) {
            createBitmap();
        }
//
        bitmap.eraseColor(Color.argb(90, 80, 80, 80));
        Canvas bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.scale(1, -1, bitmapCanvas.getWidth() / 2, bitmapCanvas.getHeight() / 2);
        drawCanvas(bitmapCanvas);
//
        int offsetDepthMapTextureUniform = getHandle("ssTexture");// 3
//
        GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
//
        if (bitmap != null && !bitmap.isRecycled()) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0);
        }
//
        GLES20.glUniform1i(offsetDepthMapTextureUniform, 4);


    }

    protected abstract void drawCanvas(Canvas canvas);

    public static void releaseBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}
