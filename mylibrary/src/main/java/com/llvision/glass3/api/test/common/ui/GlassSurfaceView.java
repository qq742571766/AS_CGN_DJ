package com.llvision.glass3.api.test.common.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * @Project: glxss_android_movidius_api_test
 * @Description:
 * @Author: haijianming
 * @Time: 2018/12/19 下午2:18
 */
public class GlassSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable{
    private static final String TAG = SurfaceViewOverlay.class.getSimpleName();
    private volatile List<Rect> rects;
    private Paint paint;
    private volatile Rect rect;
    private Thread mThread;
    private volatile boolean mIsDrawing = false;
    private Object mDrawingObj = new Object();
    private int mWidth;
    private int mHeight;
    private double mRequestedAspect = -1.0;
    private int fWidth = 1280;
    private int fHeight = 720;
    private String name;
    private volatile Bitmap bitmap;
    private Paint txtPaint;
    private String cosd;
    public GlassSurfaceView(Context context) {
        this(context, null);
    }

    public GlassSurfaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
//        setZOrderMediaOverlay(true);
        getHolder().addCallback(this);
//        setZOrderOnTop(true);
        setBackgroundColor(Color.rgb(1,1,1));
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        initPaint();
    }
    public void setFaceValue(Bitmap mBitmap, String name,String cosd) {
        if (bitmap!=null) {
           bitmap=null;
        }
        mBitmap.recycle();
//        this.bitmap=mBitmap;
        this.name = name;
        this.cosd=cosd;
        synchronized (mDrawingObj) {
            mDrawingObj.notify();
        }
    }

    public void setRect(List<Rect> rects, boolean hasFace) {
        this.rects = rects;
        if (rects == null) {
            clearPaint();
            return;
        }
        if (!hasFace||rects.size()==0){
            clearPaint();
        }
//        LogUtil.i(TAG,"The rectSize:"+rects.size());
//        synchronized (mDrawingObj) {
//            mDrawingObj.notify();
//        }
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
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(5);
         txtPaint = new Paint();
        txtPaint.setTextAlign(Paint.Align.CENTER);
        txtPaint.setColor(Color.WHITE);
        txtPaint.setStrokeWidth(2);
        txtPaint.setTextSize(25);
        txtPaint.setStyle(Paint.Style.FILL);
    }

    public void clearPaint() {
        if (bitmap!=null){
            bitmap=null;
            name=null;
        }
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
                }
            }
            Canvas canvas = null;
            try {
                canvas = getHolder().lockCanvas(null);
                if (canvas!=null){
                    synchronized (getHolder()){
                        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                        drawBitmap(canvas);
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

    private void drawBitmap(Canvas canvas) {
        if (bitmap == null || TextUtils.isEmpty(name)) {
            return;
        }
        float scale = 150 * 1f / bitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        canvas.drawBitmap(bitmap, matrix, paint);
        canvas.drawText("姓名:"+name, 100, 175, txtPaint);
        canvas.drawText("相似度:"+cosd+"%",100,200,txtPaint);

    }
}
