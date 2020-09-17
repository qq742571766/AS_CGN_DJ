package com.llvision.glass3.api.test.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import com.llvision.glass3.api.test.AiForegoundService;
import com.llvision.glass3.api.test.R;

/**
 * Created by elena on 2018/8/30.
 */

public class CameraActivity extends Activity {
    private Intent mForegroundServiceIntent;
    private Handler mForegroundHandler = new Handler();
    private Runnable mForgroundRunnable = new Runnable() {
        @Override
        public void run() {

            if (!isDestroyed()) {

                if (mForegroundServiceIntent != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (mStartForgroundServiceFlag) {
                            startForegroundService(mForegroundServiceIntent);
                        } else {
                            stopService(mForegroundServiceIntent);
                        }
                    }
                }
            }

        }
    };

    private boolean mStartForgroundServiceFlag = false;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        if (savedInstanceState == null) {
            final CameraFragment fragment = new CameraFragment();
            getFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }
        mForegroundServiceIntent = new Intent();
        Bundle bundle = new Bundle();
        mForegroundServiceIntent.putExtra("title","Glxss Suite Demo");
        mForegroundServiceIntent.putExtra("content","UVCCamera 正在运行");

        mForegroundServiceIntent.setClass(this.getApplicationContext(), AiForegoundService.class);
    }

    @Override
    protected void onResume() {
        mStartForgroundServiceFlag = false;
        mForegroundHandler.postDelayed(mForgroundRunnable,10);

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        mStartForgroundServiceFlag = true;
        mForegroundHandler.postDelayed(mForgroundRunnable,10);


        super.onStop();
    }






    @Override
    protected void onDestroy() {
        mStartForgroundServiceFlag = false;
        mForegroundHandler.removeCallbacks(mForgroundRunnable);
        super.onDestroy();
    }
}
