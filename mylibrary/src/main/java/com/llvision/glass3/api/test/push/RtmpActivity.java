package com.llvision.glass3.api.test.push;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;


import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.llvision.glass3.api.test.R;
import com.llvision.glass3.core.camera.client.CameraException;
import com.llvision.glass3.core.camera.client.CameraStatusListener;
import com.llvision.glass3.core.camera.client.ICameraClient;
import com.llvision.glass3.core.camera.client.ICameraDevice;
import com.llvision.glass3.core.lcd.client.IGlassDisplay;
import com.llvision.glass3.core.lcd.client.ILCDClient;
import com.llvision.glass3.platform.ConnectionStatusListener;
import com.llvision.glass3.platform.GlassException;
import com.llvision.glass3.platform.IGlass3Device;
import com.llvision.glass3.platform.LLVisionGlass3SDK;
import com.llvision.glxss.common.exception.BaseException;
import com.llvision.glxss.common.push.RtmpPublisher;
import com.llvision.glxss.common.push.StreamParam;
import com.llvision.glxss.common.push.rtmp.net.ConnectCheckerRtmp;
import com.llvision.glxss.common.ui.CameraTextureView;
import com.llvision.glxss.common.ui.SurfaceCallback;
import com.llvision.glxss.common.utils.LogUtil;
import java.util.List;

public class RtmpActivity extends AppCompatActivity implements ConnectCheckerRtmp,
        View.OnClickListener {
    private static final String TAG = RtmpActivity.class.getName();
    private EditText mURLTv;
    private CameraTextureView mSurfaceView;
    private int mCameraWidth = 1280;
    private int mCameraHeight = 720;
    private int mFps = 15;
    private IGlass3Device mGlass3Device;
    private ICameraClient mCameraClient;
    private ILCDClient mLcdClient;
    private ICameraDevice mICameraDevice;
    private RtmpPublisher mRtmpPublisher;
    private Button mStartBtn;
    private IGlassDisplay mGlassDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rtmp);
        init();
        initCamera();
    }

    private void init() {
        mURLTv = findViewById(R.id.et_rtp_url);
        mSurfaceView = (CameraTextureView) findViewById(R.id.surfaceView);
        mSurfaceView.setSurfaceCallback(mSurfaceCallback);
        mStartBtn = findViewById(R.id.b_start_stop);
        mStartBtn.setOnClickListener(this);
        mSurfaceView.setAspectRatio(mCameraWidth / (float) mCameraHeight);
        LLVisionGlass3SDK.getInstance().registerConnectionListener(statusListener);
    }

    /**
     * 监听眼镜连接状态
     */
    private ConnectionStatusListener statusListener = new ConnectionStatusListener() {
        @Override
        public void onServiceConnected(List<IGlass3Device> glass3Devices) {

        }

        @Override
        public void onServiceDisconnected() {

        }

        @Override
        public void onDeviceConnect(IGlass3Device device) {

        }

        @Override
        public void onDeviceDisconnect(IGlass3Device device) {
            Toast.makeText(RtmpActivity.this,"眼镜已断开连接",0).show();
            finish();
        }

        @Override
        public void onError(int code, String msg) {

        }
    };

    /**
     * 初始化Camera
     */
    private void initCamera() {
        try {
            if (LLVisionGlass3SDK.getInstance().isServiceConnected()) {
                List<IGlass3Device> glass3DeviceList = LLVisionGlass3SDK.getInstance()
                        .getGlass3DeviceList();
                if (glass3DeviceList != null && glass3DeviceList.size() > 0) {
                    mGlass3Device = glass3DeviceList.get(0);
                    mCameraClient = (ICameraClient) LLVisionGlass3SDK.getInstance().getGlass3Client(
                            IGlass3Device.Glass3DeviceClient.CAMERA);
                    mLcdClient = (ILCDClient) LLVisionGlass3SDK.getInstance().getGlass3Client(
                            IGlass3Device.Glass3DeviceClient.LCD);
                    openCamera();
                }
            } else {
                LogUtil.e("服务尚未连接.");
            }
        } catch (GlassException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        if (mGlass3Device != null) {
            try {

                mICameraDevice = mCameraClient.openCamera(mGlass3Device, mCameraStatusListener);
                if (mICameraDevice != null) {
                    mICameraDevice.setPreviewSize(mCameraWidth, mCameraHeight, mFps);
                    mICameraDevice.connect();
                }
            } catch (CameraException e) {
                e.printStackTrace();
            } catch (BaseException e) {
                e.printStackTrace();
            }
        }
    }

    private SurfaceCallback mSurfaceCallback = new SurfaceCallback() {
        @Override
        public void onSurfaceCreated(Surface surface) {
            LogUtil.i(TAG, "onSurfaceCreated");
            if (mCameraClient != null) {
                try {
                    if (mSurfaceView.getSurface() != null) {
                        mICameraDevice.addSurface(mSurfaceView.getSurface(), false);
                    }

                } catch (CameraException e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        public void onSurfaceChanged(Surface surface, int width, int height) {

        }

        @Override
        public void onSurfaceDestroy(Surface surface) {
            LogUtil.i(TAG, "onSurfaceDestroy");
            if (mICameraDevice != null) {
                try {
                    if (mSurfaceView.getSurface() != null) {
                        mICameraDevice.removeSurface(mSurfaceView.getSurface());
                    }
                } catch (CameraException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onSurfaceUpdate(Surface surface) {

        }
    };
    CameraStatusListener mCameraStatusListener = new CameraStatusListener() {
        @Override
        public void onCameraOpened() {

        }

        @Override
        public void onCameraConnected() {
            try {
                openGlxss();
//                mICameraDevice.setFrameCallback(new IFrameCallback() {
//                    @Override
//                    public void onFrameAvailable(byte[] frame) {
//
//                    }
//                }, PixelFormat.PIXEL_FORMAT_NV21);

            } catch (CameraException e) {
                e.printStackTrace();
            } catch (BaseException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCameraDisconnected() {
            if (mICameraDevice != null) {
                try {
                    if (mSurfaceView.getSurface() != null) {
                        mICameraDevice.removeSurface(mSurfaceView.getSurface());
                    }
                } catch (CameraException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onCameraClosed() {

        }

        @Override
        public void onError(int code) {

        }
    };

    /**
     * lcd显示
     *
     * @throws BaseException
     */
    private void openGlxss() throws BaseException {
        if (mLcdClient == null) {
            mLcdClient = (ILCDClient) LLVisionGlass3SDK.getInstance().getGlass3Client
                    (IGlass3Device.Glass3DeviceClient.LCD);
        }
        if (mGlassDisplay == null) {
            mGlassDisplay = mLcdClient.getGlassDisplay(mGlass3Device);
        }
        View glassView = LayoutInflater.from(this).inflate(R.layout.glass_rtsp_screen, null);
        mGlassDisplay.createCaptureScreen(this, glassView);
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RtmpActivity.this, "onConnectionSuccessRtmp", Toast.LENGTH_SHORT).show();
                mStartBtn.setText("Stop Stream");
                if (mICameraDevice != null) {
                    try {
                        mICameraDevice.addSurface(mRtmpPublisher.getEncodeSurface(), false);
                    } catch (CameraException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    public void onConnectionFailedRtmp(String reason) {
        Toast.makeText(this, "onConnectionFailedRtmp:" + reason, Toast.LENGTH_SHORT).show();
        mStartBtn.setText("Start");
        stopRtmp();
    }

    private void stopRtmp() {
        if (mRtmpPublisher != null){
            mRtmpPublisher.stop();
            mRtmpPublisher = null;
        }
    }

    @Override
    public void onDisconnectRtmp() {
        Toast.makeText(this, "onDisconnectRtmp", Toast.LENGTH_SHORT).show();
        mStartBtn.setText("Start");
    }

    @Override
    public void onAuthErrorRtmp() {
        Toast.makeText(this, "onAuthErrorRtmp", Toast.LENGTH_SHORT).show();
        mStartBtn.setText("Start");
    }

    @Override
    public void onAuthSuccessRtmp() {
        Toast.makeText(this, "onAuthSuccessRtmp", Toast.LENGTH_SHORT).show();
        mStartBtn.setText("Start");
    }

    /**
     * 关闭眼镜显示及Camera
     */
    private void closeGlass() {
        if (mGlassDisplay != null) {
            mGlassDisplay.stopCaptureScreen();
        }
        if (mCameraClient != null) {
            mCameraClient.releaseAll();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            List<IGlass3Device> list = LLVisionGlass3SDK.getInstance().getGlass3DeviceList();
            if (list == null || list.size() == 0){
                Toast.makeText(this, "请插入眼镜", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (GlassException e) {
            e.printStackTrace();
        }

        if (mRtmpPublisher != null ) {
            stopRtmp();
            mStartBtn.setText("Start Stream");
        } else {
            if (!TextUtils.isEmpty(mURLTv.getText().toString())) {
                try {
                    //TODO 直播相关的参数
                    StreamParam param = new StreamParam();
                    mRtmpPublisher = new RtmpPublisher(this);
                    param.frameRate = mFps;
                    boolean prepare = mRtmpPublisher.prepareRtmp(param);
                    if (!prepare) {
                        Toast.makeText(this, "参数错误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //如果服务端有用户民和密码设置
//                    mRtmpPublisher.setAuthorization("","");
                    mRtmpPublisher.start(mURLTv.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "URL is null", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onDestroy() {
        LLVisionGlass3SDK.getInstance().unRegisterConnectionListener(statusListener);

        super.onDestroy();
        closeGlass();
        if (mRtmpPublisher != null){
            mRtmpPublisher.stop();
        }
    }
}
