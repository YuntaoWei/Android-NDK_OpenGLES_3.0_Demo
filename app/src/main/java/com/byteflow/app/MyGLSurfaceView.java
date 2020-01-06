package com.byteflow.app;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_3D_MODEL;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_BASIC_LIGHTING;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_COORD_SYSTEM;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_DEPTH_TESTING;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_FBO_LEG;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_INSTANCING;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_MULTI_LIGHTS;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_PARTICLES;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_PBO;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_SKYBOX;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_STENCIL_TESTING;
import static com.byteflow.app.MyNativeRender.SAMPLE_TYPE_TRANS_FEEDBACK;

public class MyGLSurfaceView extends GLSurfaceView implements ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = "MyGLSurfaceView";

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;

    public static final int IMAGE_FORMAT_RGBA = 0x01;
    public static final int IMAGE_FORMAT_NV21 = 0x02;
    public static final int IMAGE_FORMAT_NV12 = 0x03;
    public static final int IMAGE_FORMAT_I420 = 0x04;

    private float mPreviousY;
    private float mPreviousX;
    private int mXAngle;
    private int mYAngle;

    private MyGLRender mGLRender;

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mPreScale = 1.0f;
    private float mCurScale = 1.0f;
    private long mLastMultiTouchTime;

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setEGLContextClientVersion(2);
        mGLRender = new MyGLRender();
        /*If no setEGLConfigChooser method is called,
        then by default the view will choose an RGB_888 surface with a depth buffer depth of at least 16 bits.*/
        setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        setRenderer(mGLRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (e.getPointerCount() == 1) {
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - mLastMultiTouchTime > 200)
            {
                float y = e.getY();
                float x = e.getX();
                switch (e.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        float dy = y - mPreviousY;
                        float dx = x - mPreviousX;
                        mYAngle += dx * TOUCH_SCALE_FACTOR;
                        mXAngle += dy * TOUCH_SCALE_FACTOR;
                }
                mPreviousY = y;
                mPreviousX = x;

                switch (mGLRender.getSampleType()) {
                    case SAMPLE_TYPE_FBO_LEG:
                    case SAMPLE_TYPE_COORD_SYSTEM:
                    case SAMPLE_TYPE_BASIC_LIGHTING:
                    case SAMPLE_TYPE_TRANS_FEEDBACK:
                    case SAMPLE_TYPE_MULTI_LIGHTS:
                    case SAMPLE_TYPE_DEPTH_TESTING:
                    case SAMPLE_TYPE_INSTANCING:
                    case SAMPLE_TYPE_STENCIL_TESTING:
                    case SAMPLE_TYPE_PARTICLES:
                    case SAMPLE_TYPE_SKYBOX:
                    case SAMPLE_TYPE_3D_MODEL:
                    case SAMPLE_TYPE_PBO:
                        mGLRender.UpdateTransformMatrix(mXAngle, mYAngle, mCurScale, mCurScale);
                        requestRender();
                        break;
                    default:
                        break;
                }
            }
        } else {
            mScaleGestureDetector.onTouchEvent(e);
        }

        return true;
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//
//        if (0 == mRatioWidth || 0 == mRatioHeight) {
//            setMeasuredDimension(width, height);
//        } else {
//            if (width < height * mRatioWidth / mRatioHeight) {
//                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
//            } else {
//                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
//            }
//        }
//    }
//
//    public void setAspectRatio(int width, int height) {
//        if (width < 0 || height < 0) {
//            throw new IllegalArgumentException("Size cannot be negative.");
//        }
//
//        mRatioWidth = width;
//        mRatioHeight = height;
//        requestLayout();
//    }

    public MyGLRender getGLRender() {
        return mGLRender;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        switch (mGLRender.getSampleType()) {
            case SAMPLE_TYPE_COORD_SYSTEM:
            case SAMPLE_TYPE_BASIC_LIGHTING:
            case SAMPLE_TYPE_INSTANCING:
            case SAMPLE_TYPE_3D_MODEL:
            {
                float preSpan = detector.getPreviousSpan();
                float curSpan = detector.getCurrentSpan();
                if (curSpan < preSpan) {
                    mCurScale = mPreScale - (preSpan - curSpan) / 200;
                } else {
                    mCurScale = mPreScale + (curSpan - preSpan) / 200;
                }
                mCurScale = Math.max(0.05f, Math.min(mCurScale, 80.0f));
                mGLRender.UpdateTransformMatrix(mXAngle, mYAngle, mCurScale, mCurScale);
                requestRender();
            }
                break;
            default:
                break;
        }

        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mPreScale = mCurScale;
        mLastMultiTouchTime = System.currentTimeMillis();

    }

    public static class MyGLRender implements GLSurfaceView.Renderer {
        private MyNativeRender mNativeRender;
        private int mSampleType;

        MyGLRender() {
            mNativeRender = new MyNativeRender();
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mNativeRender.native_OnSurfaceCreated();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            mNativeRender.native_OnSurfaceChanged(width, height);

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            mNativeRender.native_OnDrawFrame();

        }

        public void Init() {
            mNativeRender.native_Init();
        }

        public void UnInit() {
            mNativeRender.native_UnInit();
        }

        public void SetParamsInt(int paramType, int value0, int value1) {
            if (paramType == SAMPLE_TYPE) {
                mSampleType = value0;
            }
            mNativeRender.native_SetParamsInt(paramType, value0, value1);
        }

        public void SetImageData(int format, int width, int height, byte[] bytes) {
            mNativeRender.native_SetImageData(format, width, height, bytes);
        }

        public void SetImageDataWithIndex(int index, int format, int width, int height, byte[] bytes) {
            mNativeRender.native_SetImageDataWithIndex(index, format, width, height, bytes);
        }

        public int getSampleType() {
            return mSampleType;
        }

        public void UpdateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY)
        {
            mNativeRender.native_UpdateTransformMatrix(rotateX, rotateY, scaleX, scaleY);
        }

    }
}
