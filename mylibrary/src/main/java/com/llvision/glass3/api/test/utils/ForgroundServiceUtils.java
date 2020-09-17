package com.llvision.glass3.api.test.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.llvision.glass3.api.test.AiForegoundService;

/**
 * todo
 *
 * @author liuhui
 * @date 2019/4/11
 */
public class ForgroundServiceUtils {
    private Intent mForegroundIntent;
    private Context mContext;

    public ForgroundServiceUtils(Context context) {
        mContext = context.getApplicationContext();
        mForegroundIntent = new Intent();
        mForegroundIntent.setClass(context.getApplicationContext(), AiForegoundService.class);
    }

    public void startForgroundService() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(mForegroundIntent);
        }
    }

    public void stopForgroundService() {
        if (mForegroundIntent != null) {
            mContext.stopService(mForegroundIntent);
        }
    }
}
