package com.skaffl.paperless.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

public class PaperDrawingView extends FrameLayout {
    public enum PersonType {
        TEACHER,
        STUDENT
    }

    private Bitmap mOriginal;
    private Bitmap mStudent;
    private List<PaperDrawingViewPart> mStudentParts = new ArrayList<PaperDrawingViewPart>();
    private Bitmap mTeacher;

    private Canvas mDrawingCanvas;
    private PersonType mDrawingPersonType;

    public PaperDrawingView(Context context) {
        super(context);
    }

    public PaperDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaperDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public Bitmap getDrawnBitmap() {
        return mDrawingPersonType == PersonType.TEACHER ? mTeacher : mStudent;
    }

    public void setDrawingPersonType(PersonType type) {
        mDrawingPersonType = type;
        mDrawingCanvas = new Canvas(getDrawnBitmap());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mOriginal, null, null);
        canvas.drawBitmap(mStudent, null, null);

        super.onDraw(canvas);
    }
}