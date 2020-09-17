package com.llvision.glass3.api.test.common.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Project: G25_platform_sdk
 * @Description:
 * @Author: haijianming
 * @Time: 2018/3/26 上午11:25
 */

public class SurfaceViewOverlay extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = SurfaceViewOverlay.class.getSimpleName();
    private volatile List<Rect> rects;
    private Paint mFaceCoordinatePaint;
    private Paint mCommonNetCoordinatePaint;
    private volatile Rect rect;
    private Thread mThread;
    private volatile boolean mIsDrawing = false;
    private Object mDrawingObj = new Object();
    private int mWidth;
    private int mHeight;
    private int fWidth = 1280;
    private int fHeight = 720;
    private double mRequestedAspect = -1.0;
    private String name;
    private Paint mTxtPaint;
    private String cosd;
    private List<Bitmap> bitmaps = new CopyOnWriteArrayList<>();

    public SurfaceViewOverlay(Context context) {
        this(context, null);
    }

    public SurfaceViewOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setZOrderOnTop(true);
        setZOrderMediaOverlay(true);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        initPaint();
    }

    public void setScreenView() {
        setZOrderOnTop(true);
        setBackgroundColor(Color.rgb(1, 1, 1));
    }

    public void setFaceValue(Bitmap mBitmap, String name, String cosd) {
        bitmaps.add(mBitmap);
        this.name = name;
        this.cosd = cosd;
        synchronized (mDrawingObj) {
            mDrawingObj.notify();
        }
    }

    public enum Status {
        DETECT, TRACK, RECOGNIZE, UNRECOGNIZE;
    }

    private Status mCurrentStatus;

    public void setStatus(Status status) {
//        LogUtil.w(TAG, "setStatus = " + status);
        switch (status) {
            case DETECT:
                mFaceCoordinatePaint.setColor(Color.WHITE);
                mCurrentStatus = Status.DETECT;
                break;
            case TRACK:
                if (mCurrentStatus == Status.DETECT) {
                    mFaceCoordinatePaint.setColor(Color.WHITE);
                }
                break;
            case RECOGNIZE:
                mFaceCoordinatePaint.setColor(Color.RED);
                mCurrentStatus = Status.RECOGNIZE;
                break;
            case UNRECOGNIZE:
                mFaceCoordinatePaint.setColor(Color.GREEN);
                if (bitmaps != null) {
                    bitmaps.clear();
                    name = null;
                }
                mCurrentStatus = Status.UNRECOGNIZE;
                break;
        }
    }

    /**
     * @param rects
     * @param identificationStatus 识别状态
     */
    public void setFaceRect(List<Rect> rects, boolean identificationStatus) {
        if (rects == null) {
            clearFaceRect();
            return;
        }
        mPrivateRwLock.lock();
        this.rects = rects;
        mPrivateRwLock.unlock();

        synchronized (mDrawingObj) {
            mDrawingObj.notify();
        }
    }

    private volatile List<Rect> mCommonNetRects = new ArrayList<>();

    public void setCommonNetRect(List<Rect> rects) {
        if (rects == null || rects.size() == 0) {
            clearCommonNetRect();
            return;
        }
        mCommonRwLock.lock();
        this.mCommonNetRects = rects;
        mCommonRwLock.unlock();
        synchronized (mDrawingObj) {
            mDrawingObj.notify();
        }
    }

    public void setFrameFormt(int widht, int heigt) {
        this.fWidth = widht;
        this.fHeight = heigt;
        mRequestedAspect = widht * 1.0f / heigt * 1.0f;
    }

    public void setTracking(Rect rect) {
        if (rect == null) {
            this.rect = null;
        } else {
            rect.left = (rect.left * mWidth) / fWidth;
            rect.right = (rect.right * mWidth) / fWidth;
            rect.top = (rect.top * mHeight) / fHeight;
            rect.bottom = (rect.bottom * mHeight) / fHeight;
            this.rect = rect;
        }

        synchronized (mDrawingObj) {
            mDrawingObj.notify();
        }

    }

    private void initPaint() {
        mFaceCoordinatePaint = new Paint();
        mFaceCoordinatePaint.setStyle(Paint.Style.STROKE);
        mFaceCoordinatePaint.setAntiAlias(true);
        mFaceCoordinatePaint.setColor(Color.WHITE);
        mFaceCoordinatePaint.setStrokeWidth(5);
        mTxtPaint = new Paint();
        mTxtPaint.setAntiAlias(true);
        mTxtPaint.setTextSize(30);
        mTxtPaint.setAntiAlias(true);
        mTxtPaint.setColor(Color.GREEN);
        mTxtPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mCommonNetCoordinatePaint = new Paint(mFaceCoordinatePaint);
        mCommonNetCoordinatePaint.setColor(Color.YELLOW);

    }

    public void clearFaceRect() {
        mPrivateRwLock.lock();
        if (rects != null) {
            rects.clear();
            rects = null;
        }
        mPrivateRwLock.unlock();
        if (bitmaps != null) {
            bitmaps.clear();
            name = null;
        }
        synchronized (mDrawingObj) {
            mDrawingObj.notify();
        }

    }


    private ReentrantLock mCommonRwLock = new ReentrantLock();
    private ReentrantLock mPrivateRwLock = new ReentrantLock();

    public void clearCommonNetRect() {
        mCommonRwLock.lock();
        if (mCommonNetRects != null) {

            mCommonNetRects.clear();
            mCommonNetRects = null;
        }
        mCommonRwLock.unlock();
        synchronized (mDrawingObj) {
            mDrawingObj.notify();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRequestedAspect > 0) {
            int initialWidth = MeasureSpec.getSize(widthMeasureSpec);
            int initialHeight = MeasureSpec.getSize(heightMeasureSpec);

            final int horizPadding = getPaddingLeft() + getPaddingRight();
            final int vertPadding = getPaddingTop() + getPaddingBottom();
            initialWidth -= horizPadding;
            initialHeight -= vertPadding;

            final double viewAspectRatio = (double) initialWidth / initialHeight;
            final double aspectDiff = mRequestedAspect / viewAspectRatio - 1;

            if (Math.abs(aspectDiff) > 0.01) {
                if (aspectDiff > 0) {
                    // width priority decision
                    initialHeight = (int) (initialWidth / mRequestedAspect);
                } else {
                    // height priority decison
                    initialWidth = (int) (initialHeight * mRequestedAspect);
                }
                initialWidth += horizPadding;
                initialHeight += vertPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mIsDrawing = true;
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        mWidth = width;
        mHeight = height;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (mDrawingObj) {
            mDrawingObj.notifyAll();
        }
        mIsDrawing = false;
        try {
            mThread.join(10);
            mThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        while (mIsDrawing) {
            synchronized (mDrawingObj) {
                try {
                    mDrawingObj.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                    return;
                }
            }
            Canvas canvas = null;
            try {
                canvas = getHolder().lockCanvas(null);
                if (canvas != null) {
//                    Log.i(TAG, "mFaceCoordinatePaint = " + mFaceCoordinatePaint.getColor() + ",status = " + mCurrentStatus);
                    synchronized (getHolder()) {
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        int viewHeight = mHeight;
                        int viewWidth = mWidth;

                        mPrivateRwLock.lock();
                        if (rects != null && rects.size() > 0) {
                            for (int i = 0; i < rects.size(); i++) {
                                Rect rect = rects.get(i);
                                if (rect != null && mFaceCoordinatePaint != null) {
                                    rect.left = (rect.left * viewWidth) / fWidth;
                                    rect.right = (rect.right * viewWidth) / fWidth;
                                    rect.top = (rect.top * viewHeight) / fHeight;
                                    rect.bottom = (rect.bottom * viewHeight) / fHeight;
                                    canvas.drawRect(rect, mFaceCoordinatePaint);
                                }
                            }
                            rects.clear();
                            rects = null;
                            mPrivateRwLock.unlock();
                        } else {
                            mPrivateRwLock.unlock();
                        }

                        mCommonRwLock.lock();
                        if (mCommonNetRects != null && mCommonNetRects.size() > 0) {
                            for (int i = 0; i < mCommonNetRects.size(); i++) {
                                Rect rect = mCommonNetRects.get(i);
                                if (rect != null && mCommonNetCoordinatePaint != null) {
                                    rect.left = (rect.left * viewWidth) / fWidth;
                                    rect.right = (rect.right * viewWidth) / fWidth;
                                    rect.top = (rect.top * viewHeight) / fHeight;
                                    rect.bottom = (rect.bottom * viewHeight) / fHeight;
                                    canvas.drawRect(rect, mCommonNetCoordinatePaint);
                                }
                            }
                            mCommonNetRects.clear();
                            mCommonNetRects = null;
                            mCommonRwLock.unlock();
                        } else {
                            mCommonRwLock.unlock();
                        }

                    }
                }


            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                if (getHolder() != null && canvas != null) {
                    getHolder().unlockCanvasAndPost(canvas);

                }

            }

        }
    }

}
