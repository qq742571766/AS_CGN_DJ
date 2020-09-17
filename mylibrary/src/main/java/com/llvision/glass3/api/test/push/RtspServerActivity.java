package com.llvision.glass3.api.test.push;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.llvision.glass3.api.test.R;
import com.llvision.glass3.core.camera.client.CameraException;
import com.llvision.glass3.core.camera.client.CameraStatusListener;
import com.llvision.glass3.core.camera.client.ICameraClient;
import com.llvision.glass3.core.camera.client.ICameraDevice;
import com.llvision.glass3.core.camera.client.IFrameCallback;
import com.llvision.glass3.core.camera.client.PixelFormat;
import com.llvision.glass3.core.lcd.client.IGlassDisplay;
import com.llvision.glass3.core.lcd.client.ILCDClient;
import com.llvision.glass3.platform.GlassException;
import com.llvision.glass3.platform.IGlass3Device;
import com.llvision.glass3.platform.LLVisionGlass3SDK;
import com.llvision.glxss.common.exception.BaseException;
import com.llvision.glxss.common.push.StreamParam;
import com.llvision.glxss.common.push.rtsp.DefultRtspServer;
import com.llvision.glxss.common.push.rtsp.RtspServerCallBack;
import com.llvision.glxss.common.ui.CameraTextureView;
import com.llvision.glxss.common.ui.SurfaceCallback;
import com.llvision.glxss.common.utils.LogUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RtspServerActivity extends AppCompatActivity implements RtspServerCallBack,
        View.OnClickListener {
    private static final String TAG = RtspServerActivity.class.getName();
    private TextView tvConnect;
    private CameraTextureView mSurfaceView;
    private int mCameraWidth = 1280;
    private int mCameraHeight = 720;
    private int mFps = 30;
    private ICameraClient mCameraClient;
    private ILCDClient mLcdClient;
    private IGlass3Device mGlass3Device;
    private ICameraDevice mICameraDevice;
    private DefultRtspServer mRtspServere;
    private StringBuilder mLogBuilder = new StringBuilder();
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private IGlassDisplay mGlassDisplay;
    private ProgressDialog mConnectDialog;
    private ScreenStatusReceiver mScreenStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsp_server);
        init();
        if (checkPermission()) {
            initRtspServer();
            initCamera();
        }
        registSreenStatusReceiver();
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_WIFI_STATE)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            return false;
        }
        return true;
    }

    private void init() {
        tvConnect = findViewById(R.id.tv_connect);
        tvConnect.setMovementMethod(ScrollingMovementMethod.getInstance());
        mSurfaceView = (CameraTextureView) findViewById(R.id.sf_rtspserver);
        mSurfaceView.setSurfaceCallback(mSurfaceCallback);
        mSurfaceView.setAspectRatio(mCameraWidth / (float) mCameraHeight);

    }



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
                    openGlxss();
                }
            } else {
                LogUtil.e("服务尚未连接.");
            }
        } catch (GlassException e) {
            e.printStackTrace();
        } catch (BaseException e) {
            e.printStackTrace();
        }
    }

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

    /**
     * 打开Camera
     */
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
    private void registSreenStatusReceiver() {
        mScreenStatusReceiver = new ScreenStatusReceiver();
        IntentFilter screenStatusIF = new IntentFilter();
        screenStatusIF.addAction(Intent.ACTION_SCREEN_ON);
        screenStatusIF.addAction(Intent.ACTION_SCREEN_OFF);
        this.registerReceiver(mScreenStatusReceiver, screenStatusIF);
    }
    /**
     * 初始化Rtsp服务
     */
    private void initRtspServer() {
        try {
            connectDialogShow();
            StreamParam streamParam = new StreamParam();
            streamParam.videoBitRate = (int) (mCameraHeight*mCameraWidth*mFps*0.5);
            mRtspServere = new DefultRtspServer(streamParam, this, this);
            mRtspServere.configureServer();
            mRtspServere.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    CameraStatusListener mCameraStatusListener = new CameraStatusListener() {
        @Override
        public void onCameraOpened() {

        }

        @Override
        public void onCameraConnected() {
            try {
                mICameraDevice.setFrameCallback(frameCallback, PixelFormat.PIXEL_FORMAT_NV21);

            } catch (CameraException e) {
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
    private IFrameCallback frameCallback = new IFrameCallback() {
        @Override
        public void onFrameAvailable(byte[] frame) {
//            mRtspServere.addNV21Data(frame);
        }
    };
    private SurfaceCallback mSurfaceCallback = new SurfaceCallback() {
        @Override
        public void onSurfaceCreated(Surface surface) {
            LogUtil.i(TAG, "onSurfaceCreated");
            if (mCameraClient != null) {
                try {
                    if (mSurfaceView.getSurface() != null) {
                        mICameraDevice.addSurface(mSurfaceView.getSurface(), false);
                    }
                    if (mRtspServere != null && mRtspServere.getEncodeSurface() != null) {
                        mICameraDevice.addSurface(mRtspServere.getEncodeSurface(), false);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mICameraDevice != null) {
            try {
                mICameraDevice.disconnect();
            } catch (CameraException e) {
                e.printStackTrace();
            }
            mICameraDevice.release();
        }
        if (mRtspServere != null) {
            mRtspServere.stop();
        }

        if (mConnectDialog != null && mConnectDialog.isShowing()){
            mConnectDialog.cancel();
        }
        if (mScreenStatusReceiver != null){
            unregisterReceiver(mScreenStatusReceiver);
        }
        if (wakeTimer != null){
            wakeTimer.cancel();
        }
    }

    @Override
    public void onAccept() {
        mLogBuilder.append("开始监听:" + mRtspServere.getServerUrl() + "\n");
        tvConnect.setText(mLogBuilder.toString());
        if (mConnectDialog != null && mConnectDialog.isShowing()){
            mConnectDialog.cancel();
        }
    }

    @Override
    public void onClientConnected(String address) {
        mLogBuilder.append("连接用户：" + address + "\n");
        tvConnect.setText(mLogBuilder.toString());
    }

    @Override
    public void onClientDisconnect(String address) {
        mLogBuilder.append("断开连接：" + address + "\n");
        tvConnect.setText(mLogBuilder.toString());
    }

    @Override
    public void onError(int code) {
        if (mConnectDialog != null && mConnectDialog.isShowing()){
            mConnectDialog.cancel();
        }
    }

    @Override
    public void onVideoEncoderStarted() {
        mLogBuilder.append("视频编译器已启动\n");
        tvConnect.setText(mLogBuilder.toString());
    }

    @Override
    public void onAudioEncoderStarted() {
        mLogBuilder.append("音频编码器已启动\n");
        tvConnect.setText(mLogBuilder.toString());
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initRtspServer();
                initCamera();
            } else {
                Toast.makeText(this, getString(R.string.open_wifi_peremission),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 正在连接live直播中
     */
    private void connectDialogShow() {
        if (mConnectDialog != null && mConnectDialog.isShowing()) {
            return;
        }
        mConnectDialog = new ProgressDialog(this);
        mConnectDialog.setCancelable(false);
        mConnectDialog.setMessage(getString(R.string.start_rtsp_server));
        mConnectDialog.show();
    }
    private Timer wakeTimer;
    class ScreenStatusReceiver extends BroadcastReceiver {
        String SCREEN_ON = "android.intent.action.SCREEN_ON";
        String SCREEN_OFF = "android.intent.action.SCREEN_OFF";

        @SuppressLint("InvalidWakeLockTag")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SCREEN_ON.equals(intent.getAction())) {
                if (wakeTimer != null){
                    wakeTimer.cancel();
                    wakeTimer = null;
                }

            } else if (SCREEN_OFF.equals(intent.getAction())) {
                if (wakeTimer != null){
                    wakeTimer.cancel();
                }
                wakeTimer = new Timer();
                wakeTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        wakeUpApplicition(RtspServerActivity.this);
                    }
                },5*60*1000);
                LogUtil.i(TAG,"keep running");
            }
        }
    }

    /**
     * 点亮屏幕
     * @param context
     */
    private void wakeUpApplicition(Context context){
        PowerManager pm=(PowerManager) context.getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        wl.acquire();
        wl.release();
    }
}
