package com.app.personalization.core.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;

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
        super.onDraw(canvas);
        if (userImageBitmap != null) {
            canvas.drawBitmap(userImageBitmap, 0, 0, paint);
        }
    }
}