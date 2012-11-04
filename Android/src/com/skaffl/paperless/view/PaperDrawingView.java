package com.skaffl.paperless.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.gesture.GestureOverlayView;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.skaffl.paperless.dummy.Worksheets;
import com.skaffl.paperless.dummy.Worksheets.Worksheet;

import java.util.ArrayList;
import java.util.List;

public class PaperDrawingView extends View {
    public enum PersonType {
        TEACHER,
        STUDENT
    }

    private static final String TAG = "PaperDrawingView";

    private static final int INVALID_POINTER_ID = -1;

    private GestureOverlayView mGestureView;

    private List<PaperDrawingViewPart> mStudentParts = new ArrayList<PaperDrawingViewPart>();

    private Bitmap mDrawingBitmap;
    private Canvas mDrawingCanvas;

    private PersonType mDrawingPersonType = null;
    private Worksheet mWorksheet;

    private Paint mBitmapPaint;

    private int mDrawingResolutionScale = 1;

    private float mOriginalWidth = -1;
    private float mOriginalHeight = -1;

    private float mCanvasWidth;
    private float mCanvasHeight;

    float mLastTouchX;
    float mLastTouchY;

    float mPosX;
    float mPosY;

    private int mActivePointerId;

    // SCALE STUFF
    private float mScaleFactor = 1;
    private ScaleGestureDetector mScaleDetector;

    private Paint mLinePaint;

    public PaperDrawingView(Context context) {
        super(context);
        init(context);
    }

    public PaperDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PaperDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    void init(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mPath = new Path();
        mLinePaint = new Paint();
        mLinePaint.setColor(Color.argb(0xff, 0x11, 0x11, 0x11));
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setStrokeWidth(10f);
        mLinePaint.setStrokeCap(Cap.ROUND);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);

        mBitmapPaint = new Paint();
        mBitmapPaint.setShadowLayer(10f, 4f, 4f, Color.BLACK);
        mBitmapPaint.setColor(Color.BLACK);
        mBitmapPaint.setStrokeWidth(1f);
        mBitmapPaint.setStyle(Style.STROKE);

        sCallback = new RefreshCallback() {
            @Override
            public void onUpdate() {
                setWorksheet(Worksheets.ITEMS.get(1), false);
            }
        };
    }

    public void setDrawingPersonType(PersonType type) {
        mDrawingPersonType = type;
        this.invalidate();
    }

    private float mX, mY;

    private Path mPath;

    private boolean cancelDraw = false;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mDrawingCanvas.drawPath(mPath, mLinePaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Log.v(TAG, "onTouchEvent: " + ev);

        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);

        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();

                float convertedX = ((x - mPosX) / mScaleFactor);
                float convertedY = ((y - mPosY) / mScaleFactor);

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                touch_start(convertedX, convertedY);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = ev.findPointerIndex(mActivePointerId);

                final boolean isMultiFinger = ev.getPointerCount() > 1;

                final float x = ev.getX(pointerIndex);
                final float y = ev.getY(pointerIndex);

                float convertedX = ((x - mPosX) / mScaleFactor);
                float convertedY = ((y - mPosY) / mScaleFactor);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    if (isMultiFinger) {
                        cancelDraw = true;
                        mPath.reset();

                        // User is moving
                        final float dx = x - mLastTouchX;
                        final float dy = y - mLastTouchY;

                        mPosX += dx;
                        mPosY += dy;

                        invalidate();
                    } else {
                        touch_move(convertedX, convertedY);
                        invalidate();
                    }
                } else {
                    cancelDraw = true;
                    mPath.reset();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                if (!cancelDraw) {
                    touch_up();
                } else {
                    cancelDraw = false;
                    mPath.reset();
                }
                mActivePointerId = INVALID_POINTER_ID;
                invalidate();
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mPath.reset();
                mActivePointerId = INVALID_POINTER_ID;
                invalidate();
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mCanvasWidth = right - left;
        mCanvasHeight = bottom - top;

        checkDimensions();
    }

    void checkDimensions() {
        mScaleFactor = mOriginalHeight > mOriginalWidth ? mCanvasHeight / mOriginalHeight : mCanvasWidth / mOriginalWidth;
    }

    public static RefreshCallback sCallback;

    public interface RefreshCallback {
        void onUpdate();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);

        RectF dst = new RectF(0, 0, mOriginalWidth, mOriginalHeight);

        canvas.drawBitmap(mWorksheet.original, null, dst, mBitmapPaint);

        // if (mWorksheet.student != null) {
        // canvas.drawBitmap(mWorksheet.student, null, null);
        // }

        if (mDrawingBitmap != null) {
            canvas.drawBitmap(mDrawingBitmap, null, dst, null);
        }

        if (mWorksheet.teacher != null) {
            canvas.drawBitmap(mWorksheet.teacher, null, dst, null);
        }

        canvas.drawPath(mPath, mLinePaint);
        canvas.restore();
    }

    public void setWorksheet(Worksheet mItem, boolean checkDimensions) {
        Log.v(TAG, "setting worksheet to " + mItem);
        mWorksheet = mItem;

        mOriginalWidth = mWorksheet.original.getWidth();
        mOriginalHeight = mWorksheet.original.getHeight();

        if (mItem.student == null) {
            mItem.student = Bitmap.createBitmap(
                    Math.round(mOriginalWidth * mDrawingResolutionScale),
                    Math.round(mOriginalHeight * mDrawingResolutionScale),
                    Config.ARGB_4444);
            mItem.student.setDensity(Bitmap.DENSITY_NONE);
        }

        mItem.student.setDensity(Bitmap.DENSITY_NONE);

        mDrawingBitmap = mItem.student;
        mDrawingCanvas = new Canvas(mDrawingBitmap);

        checkDimensions();
        invalidate();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float lastScale = mScaleFactor;
            mScaleFactor *= detector.getScaleFactor();

            // float spanDiff = detector.getCurrentSpan() / detector.getPreviousSpan();

            // Log.v(TAG, String.format("focusX: %s, focusY: %s, lastScale: %s, mScaleFactor: %s", detector.getFocusX(), detector.getFocusY(),
            // lastScale, mScaleFactor));

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            // float convertedX = ((detector.getFocusX() - mPosX) / lastScale);
            // float convertedY = ((detector.getFocusY() - mPosY) / lastScale);

            float diffX = detector.getFocusX() - mPosX;
            float diffY = detector.getFocusY() - mPosY;

            float newDiffX = diffX / lastScale * mScaleFactor;
            float newDiffY = diffY / lastScale * mScaleFactor;

            mPosX = detector.getFocusX() - newDiffX;
            mPosY = detector.getFocusY() - newDiffY;

            invalidate();
            return true;
        }
    }
}
