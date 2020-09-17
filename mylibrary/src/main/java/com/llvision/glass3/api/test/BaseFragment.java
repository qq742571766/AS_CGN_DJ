package com.llvision.glass3.api.test;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.llvision.glxss.common.utils.LogUtil;
import com.llvision.support.uvc.utils.HandlerThreadHandler;

/**
 * Created by elena on 2018/8/30.
 */

public class BaseFragment extends Fragment {

    private static final String TAG = BaseFragment.class.getSimpleName();

    /**
     * 处理程序UI操作
     */
    private final Handler mUIHandler = new Handler(Looper.getMainLooper());
    private final Thread mUiThread = mUIHandler.getLooper().getThread();
    /**
     * 处理程序进行处理的工作线程
     */
    private Handler mWorkerHandler;
    private long mWorkerThreadID = -1;

    public BaseFragment() {
        super();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mWorkerHandler == null) {
            mWorkerHandler = HandlerThreadHandler.createHandler(TAG);
            mWorkerThreadID = mWorkerHandler.getLooper().getThread().getId();
        }
    }

    @Override
    public void onPause() {
        clearToast();
        super.onPause();
    }

    @Override
    public synchronized void onDestroy() {
        if (mWorkerHandler != null) {
            try {
                mWorkerHandler.getLooper().quit();
            } catch (final Exception e) {
                //
            }
            mWorkerHandler = null;
        }
        super.onDestroy();
    }

//================================================================================

    /**
     * 用于执行在UI线程上了Runnable方法
     *
     * @param task
     * @param duration
     */
    public final void runOnUiThread(final Runnable task, final long duration) {
        if (task == null) return;
        mUIHandler.removeCallbacks(task);
        if ((duration > 0) || Thread.currentThread() != mUiThread) {
            mUIHandler.postDelayed(task, duration);
        } else {
            try {
                task.run();
            } catch (final Exception e) {
                LogUtil.w(TAG, e);
            }
        }
    }

    /**
     * 取消UI线程任务
     *
     * @param task
     */
    public final void removeFromUiThread(final Runnable task) {
        if (task == null) return;
        mUIHandler.removeCallbacks(task);
    }

    /**
     * 要执行的工作线程上指定了Runnable
     * 执行不相同的Runnable ， 执行正在运行的Runnable之前的task将被取消
     *
     * @param task
     * @param delayMillis
     */
    protected final synchronized void queueEvent(final Runnable task, final long delayMillis) {
        if ((task == null) || (mWorkerHandler == null)) return;
        try {
            mWorkerHandler.removeCallbacks(task);
            if (delayMillis > 0) {
                mWorkerHandler.postDelayed(task, delayMillis);
            } else if (mWorkerThreadID == Thread.currentThread().getId()) {
                task.run();
            } else {
                mWorkerHandler.post(task);
            }
        } catch (final Exception e) {
            // ignore
        }
    }

    /**
     * 取消执行任务
     *
     * @param task
     */
    protected final synchronized void removeEvent(final Runnable task) {
        if (task == null) return;
        try {
            mWorkerHandler.removeCallbacks(task);
        } catch (final Exception e) {
            // ignore
        }
    }

    //================================================================================
    private Toast mToast;

    /**
     * Toast 显示
     *
     * @param msg
     */
    protected void showToast(final int msg, final Object... args) {
        removeFromUiThread(mShowToastTask);
        mShowToastTask = new ShowToastTask(msg, args);
        runOnUiThread(mShowToastTask, 0);
    }

    /**
     * Toast 消除
     */
    protected void clearToast() {
        removeFromUiThread(mShowToastTask);
        mShowToastTask = null;
        try {
            if (mToast != null) {
                mToast.cancel();
                mToast = null;
            }
        } catch (final Exception e) {
            // ignore
        }
    }

    private ShowToastTask mShowToastTask;

    private final class ShowToastTask implements Runnable {
        final int msg;
        final Object args;

        private ShowToastTask(final int msg, final Object... args) {
            this.msg = msg;
            this.args = args;
        }

        @Override
        public void run() {
            try {
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                if (args != null) {
                    final String _msg = getString(msg, args);
                    mToast = Toast.makeText(getActivity(), _msg, Toast.LENGTH_SHORT);
                } else {
                    mToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
                }
                mToast.show();
            } catch (final Exception e) {
                // ignore
            }
        }
    }

}
