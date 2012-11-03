package com.skaffl.paperless.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;

public abstract class PaperDrawingViewPart extends View {
    public PaperDrawingViewPart(Context context) {
        super(context);
    }

    public PaperDrawingViewPart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaperDrawingViewPart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    abstract public Bitmap getBitmap();
}