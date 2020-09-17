package com.llvision.glass3.api.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.llvision.glxss.common.utils.ProcessMutexLock;
import com.llvision.glxss.common.utils.LogUtil;

/**
 * 同步锁控制
 *
 * Created by jerry on 2017/8/3.
 */
public class MutexLockService extends Service{

    private static final String TAG = MutexLockService.class.getSimpleName();
    private ProcessMutexLock mProcessMutexLock;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            mProcessMutexLock = new ProcessMutexLock(this, "test");
        } catch (IllegalArgumentException e) {
            LogUtil.w(e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "onStartCommand");
        lock();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void lock(){
        if (mProcessMutexLock != null) {
            boolean lock =  mProcessMutexLock.tryLock();
            LogUtil.i(TAG, "lock = "+lock);
        }
    }

    private void release(){
        if (mProcessMutexLock != null) {
            LogUtil.i(TAG, "lock = "+ mProcessMutexLock.isLock());
            mProcessMutexLock.unLock();
        }
    }

}
