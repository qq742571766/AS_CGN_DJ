package com.llvision.glass3.api.test.camera.ui;


import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.llvision.glass3.api.test.R;
import com.llvision.glass3.core.camera.client.RecordStatusListener;
import com.llvision.glass3.library.GodApplicationHolder;

import java.util.concurrent.locks.ReentrantLock;

/**
 * todo
 *
 * @author liuhui
 * @date 2019/4/10
 */
public class CameraLcdView implements RecordStatusListener {
    public static final int MSG_RECORDING_START = 1;
    public static final int MSG_RECORDING_RUNNING = 2;
    public static final int MSG_RECORDING_RESUME = 3;
    public static final int MSG_RECORDING_PAUSE = 4;
    public static final int MSG_RECORDING_STOP = 5;
    public static final int MSG_RECORDING_RESTART = 6;


    private RelativeLayout mOverlayView;
    private TextView mGlassResolutionTv;
    private ImageView mGlassFovIv;
    private TextView mGlassRecordTimeTv;
    private TextView mActualFpsTextTv;

    private Handler mHandler;
    //recording
    private ReentrantLock w = new ReentrantLock();
    private boolean isRecordingRunning;
    private boolean isRecordingPause;
    private int mTimeMiss = 0;

    /**
     * 默认可以录屏的时间
     */
    private int mDefaultRecordingMaximumTime = Integer.MAX_VALUE;
    /**
     * 监听录屏时间
     */
    private IReachMaximumTimeForRecordingListener mRecordingListener;

    /**
     * 录屏时间
     */
    private int mRecordingTime;


    private CameraLcdView() {
        mOverlayView = getLcdView(R.layout.layout_glass_screen);

        mHandler = new Handler(mOverlayView.getContext().getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_RECORDING_START:
                        setRecordTextWithVisibility("00:00:00", true);
                        sendEmptyMessageDelayed(MSG_RECORDING_RUNNING, 1000);
                        break;
                    case MSG_RECORDING_STOP:
                        setRecordTextWithVisibility("00:00:00", false);
                        mTimeMiss = 0;
                        break;
                    case MSG_RECORDING_RESUME:
                        removeMessages(MSG_RECORDING_RUNNING);
                        w.lock();
                        if (isRecordingRunning && !isRecordingPause) {
                            sendEmptyMessageDelayed(MSG_RECORDING_RUNNING, 1000);
                        }
                        w.unlock();
                        break;
                    case MSG_RECORDING_PAUSE:
                        removeMessages(MSG_RECORDING_RUNNING);
                        break;
                    case MSG_RECORDING_RUNNING:
                        mTimeMiss++;
                        setRecordText(FormatMiss(mTimeMiss));
                        w.lock();
                        if (mRecordingListener != null && mRecordingTime >= mDefaultRecordingMaximumTime) {
                            mRecordingListener.onReachMaximumTime();
                        } else if (isRecordingRunning && !isRecordingPause) {
                            sendEmptyMessageDelayed(MSG_RECORDING_RUNNING, 1000);
                        }
                        w.unlock();
                        break;
                    case MSG_RECORDING_RESTART:
                        w.lock();
                        if (isRecordingRunning && !isRecordingPause) {
                            sendEmptyMessageDelayed(MSG_RECORDING_RUNNING, 1000);
                        }
                        w.unlock();
                        break;
                }
            }
        };
        mGlassResolutionTv = findViewById(R.id.tv_glass_resolution);
        mActualFpsTextTv = findViewById(R.id.tv_glass_fps);
        mGlassFovIv = findViewById(R.id.img_glass_fov);
        mGlassRecordTimeTv = findViewById(R.id.tv_glass_recordTime);
    }

    public static CameraLcdView createCameralcdView() {
        return new CameraLcdView();
    }

    public void setResolutionText(String resolutionText) {
        mGlassResolutionTv.setText(resolutionText);
    }

    public void setFpsText(String fpsText) {
        mActualFpsTextTv.setText(fpsText);
    }

    public void setRecordText(String recordText) {
        mGlassRecordTimeTv.setText(recordText);
    }

    public void setRecordVisibility(int visible) {
        mGlassRecordTimeTv.setVisibility(visible);
    }

    public void setRecordTextWithVisibility(String txt, boolean isShow) {
        mGlassRecordTimeTv.setText(txt);
        mGlassRecordTimeTv.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }


    public void setGlassFovImageResource(@DrawableRes int resId) {
        mGlassFovIv.setImageResource(resId);
    }

    public View getView() {
        return mOverlayView;
    }


    private <T extends View> T getLcdView(@LayoutRes int layoutId) {
        return (T) View.inflate(GodApplicationHolder.sContext, layoutId, null);
    }

    private final <T extends View> T findViewById(@IdRes int id) {
        if (id == View.NO_ID) {
            return null;
        }
        return mOverlayView.findViewById(id);
    }


    @Override
    public void onCameraRecordPrepared() {
        w.lock();
        isRecordingRunning = true;
        isRecordingPause = false;
        mRecordingTime = 0;
        w.unlock();
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_RECORDING_START);
        }
    }

    @Override
    public void onCameraRecordStoped() {

        w.lock();
        isRecordingRunning = false;
        isRecordingPause = false;
        mRecordingTime = 0;
        w.unlock();
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_RECORDING_STOP);
        }

    }

    @Override
    public void onCameraRecordError(int code) {
        w.lock();
        isRecordingRunning = false;
        isRecordingPause = false;
        mRecordingTime = 0;
        w.unlock();
        if (mHandler != null) {
            mHandler.sendEmptyMessage(MSG_RECORDING_STOP);
        }
    }

    @Override
    public void onCameraRecordPaused() {
        if (isRecordingRunning) {
            w.lock();
            isRecordingPause = true;
            w.unlock();
            if (mHandler != null){
                mHandler.sendEmptyMessage(MSG_RECORDING_PAUSE);
            }
        }

    }

    @Override
    public void onCameraRecordResume() {
        if (isRecordingRunning) {
            w.lock();
            isRecordingPause = false;
            w.unlock();
            if (mHandler != null) {
                mHandler.sendEmptyMessage(MSG_RECORDING_RESUME);
            }
        }

    }

    public void destroy() {
        mRecordingListener = null;

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public String FormatMiss(int miss) {
        String hh = miss / 3600 > 9 ? miss / 3600 + "" : "0" + miss / 3600;
        String mm = (miss % 3600) / 60 > 9 ? (miss % 3600) / 60 + "" : "0" + (miss % 3600) / 60;
        String ss = (miss % 3600) % 60 > 9 ? (miss % 3600) % 60 + "" : "0" + (miss % 3600) % 60;
        mRecordingTime = Integer.valueOf(mm);
        return hh + ":" + mm + ":" + ss;
    }

    public void setRecordingMaximumTime(int time) {
        mDefaultRecordingMaximumTime = time;
    }

    public void setOnRecordingRearchMaximumListener(IReachMaximumTimeForRecordingListener listener) {
        mRecordingListener = listener;
    }

    public interface IReachMaximumTimeForRecordingListener {
        void onReachMaximumTime();
    }
}
