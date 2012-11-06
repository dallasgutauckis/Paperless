package com.skaffl.paperless.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.skaffl.paperless.dummy.Worksheets;
import com.skaffl.paperless.dummy.Worksheets.Worksheet;
import com.skaffl.paperless.view.PaperDrawingView.PaperDrawingSynchronizer.RemoteTouchCallback;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class PaperDrawingView extends View {

    private static final String MQTT_URI = "tcp://173.193.71.218:1883";

    private static final String TAG = "PaperDrawingView";

    public enum PersonType {
        TEACHER,
        STUDENT
    }

    private static final int INVALID_POINTER_ID = -1;

    private RemoteTouchCallback mRemoteTouchCallback = new RemoteTouchCallbackImplementation();
    private PaperDrawingSynchronizer mSynchronizer = new PaperDrawingSynchronizer(mRemoteTouchCallback);

    private Bitmap mDrawingBitmap;
    private Canvas mDrawingCanvas;
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
    private Paint mRemoteLinePaint;

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
        mRemotePath = new Path();

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStyle(Style.STROKE);
        mLinePaint.setStrokeWidth(10f);
        mLinePaint.setStrokeCap(Cap.ROUND);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setDither(true);

        mRemoteLinePaint = new Paint();
        mRemoteLinePaint.setColor(Color.RED);
        mRemoteLinePaint.setStyle(Style.STROKE);
        mRemoteLinePaint.setStrokeWidth(20f);
        mRemoteLinePaint.setStrokeCap(Cap.ROUND);
        mRemoteLinePaint.setAntiAlias(true);
        mRemoteLinePaint.setDither(true);

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

        // Connect to MQTT
        mSynchronizer.connect(MQTT_URI, MqttClient.generateClientId());
    }

    private float mX, mY;
    private float mRemoteX, mRemoteY;

    private Path mPath;
    private Path mRemotePath;

    private boolean cancelDraw = false;
    private static final float TOUCH_TOLERANCE = 4;

    private void touchDown(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mDrawingCanvas.drawPath(mPath, mLinePaint);
        // kill this so we don't double draw
        mPath.reset();
    }

    private void touchDownRemote(float x, float y) {
        mRemotePath.reset();
        mRemotePath.moveTo(x, y);
        mRemoteX = x;
        mRemoteY = y;
        invalidate();
    }

    private void touchMoveRemote(float x, float y) {
        Log.v(TAG, "Touch Move Remote( x, y ) = ( " + x + ", " + y + " )");

        float dx = Math.abs(x - mRemoteX);
        float dy = Math.abs(y - mRemoteY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mRemotePath.quadTo(mRemoteX, mRemoteY, (x + mRemoteX) / 2, (y + mRemoteY) / 2);
            mRemoteX = x;
            mRemoteY = y;
        }
        invalidate();
    }

    private void touchUpRemote() {
        mRemotePath.lineTo(mRemoteX, mRemoteY);
        // commit the path to our offscreen
        mDrawingCanvas.drawPath(mRemotePath, mRemoteLinePaint);
        // kill this so we don't double draw
        mRemotePath.reset();
        invalidate();
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

                mSynchronizer.onTouchDown(convertedX, convertedY);

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = ev.getPointerId(0);
                touchDown(convertedX, convertedY);
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
                        touchMove(convertedX, convertedY);

                        mSynchronizer.onTouchMove(convertedX, convertedY);

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
                    mSynchronizer.onTouchUp();
                    touchUp();
                } else {
                    cancelDraw = false;
                    mPath.reset();
                }
                mActivePointerId = INVALID_POINTER_ID;
                invalidate();
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mSynchronizer.onTouchCancel();
                mPath.reset();
                mActivePointerId = INVALID_POINTER_ID;
                invalidate();
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);

        canvas.drawBitmap(mWorksheet.original, null, mDst, mBitmapPaint);

        // if (mWorksheet.student != null) {
        // canvas.drawBitmap(mWorksheet.student, null, null);
        // }

        if (mDrawingBitmap != null) {
            canvas.drawBitmap(mDrawingBitmap, null, mDst, null);
        }

        if (mWorksheet.teacher != null) {
            canvas.drawBitmap(mWorksheet.teacher, null, mDst, null);
        }

        canvas.drawPath(mRemotePath, mRemoteLinePaint);
        canvas.drawPath(mPath, mLinePaint);

        canvas.restore();
    }

    public void setWorksheet(Worksheet mItem, boolean checkDimensions) {
        mWorksheet = mItem;

        mOriginalWidth = mWorksheet.original.getWidth();
        mOriginalHeight = mWorksheet.original.getHeight();

        mDst = new RectF(0, 0, mOriginalWidth, mOriginalHeight);

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

        if (checkDimensions) {
            checkDimensions();
        }

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

    static class Messager {
        public void messageToCallback(String data, RemoteTouchCallback callback) {

        }
    }

    private enum TouchType {
        DOWN,
        MOVE,
        UP,
        CANCEL;

        public static TouchType getByOrdinal(int ordinal) {
            return values()[ordinal];
        }
    }

    static class PaperDrawingSynchronizer {
        private MqttClient mClient;
        private MqttTopic mTopic;

        private char mDrawerId = Character.toChars((int) Math.round(Math.random() * 255))[0];

        final private RemoteTouchCallback mRemoteTouchCallback;

        public PaperDrawingSynchronizer(RemoteTouchCallback callback) {
            mRemoteTouchCallback = callback;
        }

        private void connect(final String serverUrl, final String clientId) {
            (new Thread() {
                @Override
                public void run() {
                    // Without persistence, please
                    try {
                        mClient = new MqttClient(serverUrl, clientId, null);
                        mClient.connect();
                        mClient.setCallback(mCallback);
                        mTopic = mClient.getTopic("draw");
                        mClient.subscribe("draw");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        public char getDrawerId() {
            return mDrawerId;
        }

        private MqttCallback mCallback = new MqttCallback() {
            @Override
            public void messageArrived(MqttTopic topic, MqttMessage message) throws Exception {
                String msg = new String(message.getPayload());
                char clientId = msg.substring(0, 1).toCharArray()[0];

                if (clientId == getDrawerId()) {
                    // Don't do anything
                    return;
                }

                TouchType type = TouchType.getByOrdinal(Integer.parseInt(msg.substring(1, 2)));

                if (type == TouchType.DOWN || type == TouchType.MOVE) {
                    String coords = msg.substring(2);
                    int commaPos = coords.indexOf(',');

                    float x = Float.parseFloat(coords.substring(0, commaPos));
                    float y = Float.parseFloat(coords.substring(commaPos + 1));

                    switch (type) {
                        case DOWN:
                            mRemoteTouchCallback.onTouchDown(x, y);
                            break;

                        case MOVE:
                            mRemoteTouchCallback.onTouchMove(x, y);
                            break;
                    }
                } else {
                    switch (type) {
                        case UP:
                            mRemoteTouchCallback.onTouchUp();
                            break;

                        case CANCEL:
                            mRemoteTouchCallback.onTouchCancel();
                            break;
                    }
                }
            }

            @Override
            public void deliveryComplete(MqttDeliveryToken token) {
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.v(TAG, "Connection lost", cause);
            }
        };

        private void sendTouch(TouchType type) {
            String message = getDrawerId() + "" + type.ordinal();
            final byte[] payload = (message).getBytes();
            sendMessage(payload);
        }

        private void sendTouchWithCoordinates(TouchType type, float x, float y) {
            String message = String.format("%s%s%f,%f", getDrawerId(), type.ordinal(), x, y);
            final byte[] payload = (message).getBytes();
            sendMessage(payload);
        }

        private void sendMessage(final byte[] payload) {
            if (mClient.isConnected()) {
                (new Thread() {
                    public void run() {
                        try {
                            mTopic.publish(new MqttMessage(payload));
                        } catch (MqttPersistenceException e) {
                            e.printStackTrace();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    };
                }).start();
            }
        }

        public void onTouchDown(float x, float y) {
            sendTouchWithCoordinates(TouchType.DOWN, x, y);
        }

        public void onTouchMove(float x, float y) {
            sendTouchWithCoordinates(TouchType.MOVE, x, y);
        }

        public void onTouchUp() {
            sendTouch(TouchType.UP);
        }

        public void onTouchCancel() {
            sendTouch(TouchType.CANCEL);
        }

        interface RemoteTouchCallback {
            public void onTouchDown(float x, float y);

            public void onTouchMove(float x, float y);

            public void onTouchUp();

            public void onTouchCancel();
        }
    }

    private Handler mRemoteTouchHandler = new RemoteTouchHandler();

    private RectF mDst;

    private final class RemoteTouchHandler extends Handler {
        public static final int MESSAGE_TOUCH_UP = 0;
        public static final int MESSAGE_TOUCH_MOVE = 1;
        public static final int MESSAGE_TOUCH_DOWN = 2;
        public static final int MESSAGE_TOUCH_CANCEL = 3;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TOUCH_UP:
                    touchUpRemote();
                    break;

                case MESSAGE_TOUCH_DOWN:
                    PointF downPoint = (PointF) msg.obj;
                    touchDownRemote(downPoint.x, downPoint.y);
                    break;

                case MESSAGE_TOUCH_MOVE:
                    PointF movePoint = (PointF) msg.obj;
                    touchMoveRemote(movePoint.x, movePoint.y);
                    break;

                case MESSAGE_TOUCH_CANCEL:
                    mRemotePath.reset();
                    break;
            }
        }
    }

    private final class RemoteTouchCallbackImplementation implements RemoteTouchCallback {
        @Override
        public void onTouchUp() {
            Log.v(TAG, "onTouchUp");
            mRemoteTouchHandler.sendEmptyMessage(RemoteTouchHandler.MESSAGE_TOUCH_UP);
        }

        @Override
        public void onTouchMove(float x, float y) {
            Log.v(TAG, "onTouchMove");
            mRemoteTouchHandler.sendMessage(mRemoteTouchHandler.obtainMessage(RemoteTouchHandler.MESSAGE_TOUCH_MOVE, new PointF(x, y)));
        }

        @Override
        public void onTouchDown(float x, float y) {
            Log.v(TAG, "onTouchDown");
            mRemoteTouchHandler.sendMessage(mRemoteTouchHandler.obtainMessage(RemoteTouchHandler.MESSAGE_TOUCH_DOWN, new PointF(x, y)));
        }

        @Override
        public void onTouchCancel() {
            Log.v(TAG, "onTouchCancel");
            mRemoteTouchHandler.sendEmptyMessage(RemoteTouchHandler.MESSAGE_TOUCH_UP);

        }
    }
}
