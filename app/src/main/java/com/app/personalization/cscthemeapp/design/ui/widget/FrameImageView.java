package com.app.personalization.cscthemeapp.design.ui.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import com.caverock.androidsvg.SVG;

public class FrameImageView extends AppCompatImageView {
    private String bgMaskImageUrl = null;
    private Bitmap maskBitmap = null;
    private Bitmap userImageBitmap = null;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FrameImageView(Context context) {
        super(context);
    }
    public FrameImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public FrameImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMaskSvgPath(String svgPath) {
        this.bgMaskImageUrl = svgPath;
        if (this.maskBitmap != null) {
            this.maskBitmap.recycle();
            this.maskBitmap = null;
        }
        invalidate();
    }

    public void setUserImage(Bitmap bitmap) {
        this.userImageBitmap = bitmap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bgMaskImageUrl == null) {
            super.onDraw(canvas);
            return;
        }

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        try {
            if (maskBitmap == null) {
                maskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                Canvas maskCanvas = new Canvas(maskBitmap);
                SVG svg = SVG.getFromAsset(getContext().getAssets(), bgMaskImageUrl);
                RectF rect = new RectF(0, 0, w, h);
                svg.renderToCanvas(maskCanvas, rect);
            }

            Bitmap compositeBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas compositeCanvas = new Canvas(compositeBmp);

            paint.setXfermode(null);
            compositeCanvas.drawBitmap(maskBitmap, 0, 0, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            Bitmap photoBmp = userImageBitmap;
            if (photoBmp != null) {
                android.graphics.Rect srcRect = new android.graphics.Rect(0, 0, photoBmp.getWidth(), photoBmp.getHeight());
                android.graphics.Rect destRect = new android.graphics.Rect(0, 0, w, h);
                compositeCanvas.drawBitmap(photoBmp, srcRect, destRect, paint);
            }
            paint.setXfermode(null);

            canvas.drawBitmap(compositeBmp, 0, 0, null);
            compositeBmp.recycle();
        } catch (Exception e) {
            e.printStackTrace();
            super.onDraw(canvas);
        }
    }
}
