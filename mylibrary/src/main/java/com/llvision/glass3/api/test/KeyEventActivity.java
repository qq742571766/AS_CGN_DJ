package com.llvision.glass3.api.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.llvision.glass3.core.key.client.IGlassKeyEvent;
import com.llvision.glass3.core.key.client.IKeyEventClient;
import com.llvision.glass3.core.key.client.KeyEventListener;
import com.llvision.glass3.platform.ConnectionStatusListener;
import com.llvision.glass3.platform.IGlass3Device;
import com.llvision.glass3.platform.LLVisionGlass3SDK;
import com.llvision.glxss.common.exception.BaseException;
import com.llvision.glxss.common.utils.LogUtil;

import java.util.List;

/**
 * 按键测试
 *
 * @author elena
 * @date 2018/9/25.
 */

public class KeyEventActivity extends AppCompatActivity implements ConnectionStatusListener, IGlassKeyEvent.OnGlxssClickListener, IGlassKeyEvent.OnGlxssLongClickListener, IGlassKeyEvent.OnGlxssDoubleClickListener, View.OnClickListener {

    private static final String TAG = KeyEventActivity.class.getSimpleName();

    private TextView mTextView;
    private TextView mClickInfo;

    private IGlassKeyEvent mKey;
    private IKeyEventClient mIKeyEventClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keyevent);
        mTextView = findViewById(R.id.info);
        mClickInfo = findViewById(R.id.click_info);
        LLVisionGlass3SDK.getInstance().registerConnectionListener(this);
        try {
            List<IGlass3Device> glass3Devices = LLVisionGlass3SDK.getInstance().getGlass3DeviceList();
            mIKeyEventClient = (IKeyEventClient) LLVisionGlass3SDK.getInstance().getGlass3Client(IGlass3Device.Glass3DeviceClient.KEY);
            if (glass3Devices.size() > 0) {
                IGlass3Device glass3Device = glass3Devices.get(0);
                mKey = mIKeyEventClient.getGlassKeyEvent(glass3Device);
                mKey.registerKeyEventLister(mSyncKeyListener);
            }
        } catch (BaseException e) {
            e.printStackTrace();
        }
        findViewById(R.id.short_click).setOnClickListener(this);
        findViewById(R.id.double_click).setOnClickListener(this);
        findViewById(R.id.long_click).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LLVisionGlass3SDK.getInstance().unRegisterConnectionListener(this);
        if (mKey != null) {
            mKey.unregisterKeyEventListener(mSyncKeyListener);
            mKey = null;
        }
    }

    private KeyEventListener mSyncKeyListener = new KeyEventListener() {

        @Override
        public void onKeyChanged(final int keyStatus, final int keyCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText("键号：" + keyCode + ",按键状态：" + keyStatus);
                }
            });

        }
    };

    @Override
    public void onServiceConnected(List<IGlass3Device> glass3Devices) {
        LogUtil.e("KeyEventActivity#onServiceConnected");
    }

    @Override
    public void onServiceDisconnected() {
        LogUtil.e("KeyEventActivity#onServiceDisconnected");
    }

    @Override
    public void onDeviceConnect(IGlass3Device device) {
        LogUtil.e("KeyEventActivity#onDeviceConnect");
        try {
            mKey = mIKeyEventClient.getGlassKeyEvent(device);
        } catch (BaseException e) {
            e.printStackTrace();
        }
        mKey.registerKeyEventLister(mSyncKeyListener);
        LogUtil.e("KeyEventActivity#onDeviceConnect#mKey:" + mKey);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(KeyEventActivity.this, "Device Connect", Toast.LENGTH_SHORT).show();
                mClickInfo.setText("");
                mTextView.setText("");
            }
        });
    }

    @Override
    public void onDeviceDisconnect(IGlass3Device device) {
        LogUtil.d("KeyEventActivity#onDeviceDisconnect");
        if (mKey != null) {
            mKey.unregisterKeyEventListener(mSyncKeyListener);
            mKey = null;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(KeyEventActivity.this, "Device Disconnect", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onError(int code, String msg) {
        LogUtil.d("KeyEventActivity#onError");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mClickInfo.setText("Error");
            }
        });
    }

    @Override
    public void onClick(int keyCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mClickInfo.setText("单击");
            }
        });
    }

    @Override
    public void onLongClick(int keyCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mClickInfo.setText("长按");
            }
        });
    }

    @Override
    public void onDoubleClick(int keyCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mClickInfo.setText("双击");
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.short_click) {
            mKey.setOnGlxssFnClickListener(this);
        } else if (id == R.id.double_click) {
            mKey.setOnGlxssFnDoubleClickListener(this);
        } else if (id == R.id.long_click) {
            mKey.setOnGlxssFnLongClickListener(this);
        }
    }
}
