package com.daasuu.epf.filter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by sudamasayuki on 2017/05/19.
 */

public class GlColorOverlaySample extends GlOverlayFilter {

    private Bitmap bitmap;

    public GlColorOverlaySample(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
    public GlColorOverlaySample() {
    }
    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }

    @Override
    protected void drawCanvas(Canvas canvas) {

        canvas.drawBitmap(bitmap, 0, 0, null);

    }
}