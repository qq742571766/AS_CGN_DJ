package com.llvision.glass3.api.test;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.llvision.glass3.api.test.service.SensorBackService;
import com.llvision.glass3.core.sensor.SensorEvent;
import com.llvision.glass3.core.sensor.SensorType;
import com.llvision.glass3.core.sensor.client.IGlassSensor;
import com.llvision.glass3.core.sensor.client.ISensorClient;
import com.llvision.glass3.core.sensor.client.SensorListener;
import com.llvision.glass3.platform.ConnectionStatusListener;
import com.llvision.glass3.platform.IGlass3Device;
import com.llvision.glass3.platform.LLVisionGlass3SDK;
import com.llvision.glxss.common.exception.BaseException;
import com.llvision.glxss.common.utils.LogUtil;
import com.llvision.glxss.common.utils.ToastUtils;

import java.text.DecimalFormat;
import java.util.List;

public class SensorActivity extends Activity implements ConnectionStatusListener,
        CompoundButton.OnCheckedChangeListener {

    private static final String TAG = SensorActivity.class.getSimpleName();

    private TextView mShowTextBtnState;
    private TextView mShowTemp;
    private TextView mShowLightTv;
    private TextView mShowAccX;
    private TextView mShowAccY;
    private TextView mShowAccZ;
    private TextView mShowOriX;
    private TextView mShowOriY;
    private TextView mShowOriZ;

    private SwitchCompat mAccBtn;
    private SwitchCompat mGravityBtn;
    private SwitchCompat mMagnetometerBtn;
    private SwitchCompat mGyrosopeBtn;
    private SwitchCompat mBackRunningBtn;
    private SwitchCompat mTemperatureBtn;
    private SwitchCompat mLightBtn;
    private SwitchCompat mEulerBtn;
    private Button mGetSwitchBtn;

    private TextView mGravityInfoTv;
    private TextView mMagnetometerTv;
    private TextView mGyrosopeTv;
    private IGlassSensor mSensor;
    private ISensorClient mISensorClient;

    private DecimalFormat dfs = new DecimalFormat("0.000000");
    private Intent mBackServiceIntent;
    private SensorBackService.SensorBinder mSensorBinder;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i(TAG, "onServiceConnected");
            mSensorBinder = (SensorBackService.SensorBinder) service;
            if (mSensorBinder != null) {
                LogUtil.i(TAG, "isBackRunning : " + mSensorBinder.isBackRunning());
                mBackRunningBtn.setChecked(mSensorBinder.isBackRunning());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.i(TAG, "onServiceDisconnected");
            mSensorBinder = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_main);
        mShowTextBtnState = findViewById(R.id.id_show_status);
        mShowAccX = findViewById(R.id.id_show_acc_x);
        mShowAccY = findViewById(R.id.id_show_acc_y);
        mShowAccZ = findViewById(R.id.id_show_acc_z);
        mShowTemp = findViewById(R.id.id_show_temperatrue);
        mShowLightTv = findViewById(R.id.id_show_light_tv);
        mShowOriX = findViewById(R.id.id_show_oritation_x);
        mShowOriY = findViewById(R.id.id_show_oritation_y);
        mShowOriZ = findViewById(R.id.id_show_oritation_z);
        mAccBtn = findViewById(R.id.id_sensor_acc_btn);
        mGravityBtn = findViewById(R.id.id_sensor_gravity_btn);
        mMagnetometerBtn = findViewById(R.id.id_sensor_magnetometer_btn);
        mGyrosopeBtn = findViewById(R.id.id_sensor_gyrosope_btn);
        mBackRunningBtn = findViewById(R.id.id_back_running_btn);
        mEulerBtn = findViewById(R.id.id_sensor_orientation_btn);
        mTemperatureBtn = findViewById(R.id.id_sensor_temperature_btn);
        mLightBtn = findViewById(R.id.id_sensor_light_btn);
        mGetSwitchBtn = findViewById(R.id.id_btn_get_switch);

        mGravityInfoTv = findViewById(R.id.id_show_gravity_tv);
        mMagnetometerTv = findViewById(R.id.id_show_magnetometer_tv);
        mGyrosopeTv = findViewById(R.id.id_show_gyroscope_tv);

        mBackRunningBtn.setOnCheckedChangeListener(this);
        mAccBtn.setOnCheckedChangeListener(this);
        mGravityBtn.setOnCheckedChangeListener(this);
        mMagnetometerBtn.setOnCheckedChangeListener(this);
        mGyrosopeBtn.setOnCheckedChangeListener(this);

        mTemperatureBtn.setOnCheckedChangeListener(this);
        mLightBtn.setOnCheckedChangeListener(this);
        mEulerBtn.setOnCheckedChangeListener(this);
        mGetSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSensor != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Acceleration :").append(mSensor.isAccelerationOpened()).append("\n")
                            .append("Gravity :").append(mSensor.isGravityOpened()).append("\n")
                            .append("Magnetometer :").append(mSensor.isMagnetometerOpened()).append("\n")
                            .append("Gyroscope :").append(mSensor.isGyroscopeOpened()).append("\n")
                            .append("Temperature :").append(mSensor.isTemperatureOpened()).append("\n")
                            .append("euler :").append(mSensor.isEulerOpened()).append("\n")
                            .append("light :").append(mSensor.isLightOpened()).append("\n");

                    mShowTextBtnState.setText(builder.toString());
                }
            }
        });
        mGetSwitchBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mShowTextBtnState.setText("");
                ToastUtils.showLong(SensorActivity.this, "clear text");
                return true;
            }
        });

        LLVisionGlass3SDK.getInstance().registerConnectionListener(this);
        try {
            List<IGlass3Device> glass3Devices = LLVisionGlass3SDK.getInstance().getGlass3DeviceList();
            mISensorClient = (ISensorClient) LLVisionGlass3SDK.getInstance().getGlass3Client(IGlass3Device.Glass3DeviceClient.SENSOR);
            if (glass3Devices.size() > 0) {
                IGlass3Device glass3Device = glass3Devices.get(0);
                mSensor = mISensorClient.getGlassSensor(glass3Device);
                mSensor.registerSensorLister(SensorType.CMD_SYNC_SENSOR_MARK_ALL, mSyncSensorListener);
                mAccBtn.setChecked(mSensor.isAccelerationOpened());
                mGravityBtn.setChecked(mSensor.isGravityOpened());
                mMagnetometerBtn.setChecked(mSensor.isMagnetometerOpened());
                mGyrosopeBtn.setChecked(mSensor.isGyroscopeOpened());
                //mQuatenionBtn.setChecked(mSensor.isQuaternionOpened());
                mTemperatureBtn.setChecked(mSensor.isTemperatureOpened());
                mLightBtn.setChecked(mSensor.isTemperatureOpened());

                mEulerBtn.setChecked(mSensor.isEulerOpened());
            }
        } catch (BaseException e) {
            e.printStackTrace();
        }

        mBackServiceIntent = new Intent(this, SensorBackService.class);
        startService(mBackServiceIntent);
        bindService(mBackServiceIntent, mServiceConnection, Service.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LLVisionGlass3SDK.getInstance().unRegisterConnectionListener(this);
        if (mSensorBinder != null && mSensorBinder.isBackRunning()) {
            unbindService(mServiceConnection);
        } else {
            unbindService(mServiceConnection);
            stopService(mBackServiceIntent);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mSensor == null) {
            ToastUtils.showLong(this, "sensor is null");
            return;
        }
        if (mSensorBinder == null || !mSensorBinder.pingBinder()) {
            ToastUtils.showLong(this, "sensor binder disconnected");
            return;
        }
        if (buttonView == mAccBtn) {
            mSensor.setAccelerationSwitch(isChecked);
            if (isChecked) {
                mSensorBinder.createResultCsv(SensorType.CMD_SYNC_SENSOR_MARK_ACCELERATION);
            } else {
                mSensorBinder.closeAccIOBuffer();
            }
        } else if (buttonView == mGravityBtn) {
            mSensor.setGravitySwitch(isChecked);
            if (isChecked) {
                mSensorBinder.createResultCsv(SensorType.CMD_SYNC_SENSOR_MARK_GRAVITY);
            } else {
                mSensorBinder.closeBufferedWriter(SensorType.CMD_SYNC_SENSOR_MARK_GRAVITY);
            }
        } else if (buttonView == mMagnetometerBtn) {
            mSensor.setMagnetometerSwitch(isChecked);
            if (isChecked) {
                mSensorBinder.createResultCsv(SensorType.CMD_SYNC_SENSOR_MARK_MAGNETOMETER);
            } else {
                mSensorBinder.closeBufferedWriter(SensorType.CMD_SYNC_SENSOR_MARK_MAGNETOMETER);
            }
        } else if (buttonView == mGyrosopeBtn) {
            mSensor.setGyroscopeSwitch(isChecked);
            if (isChecked) {
                mSensorBinder.createResultCsv(SensorType.CMD_SYNC_SENSOR_MARK_GYROSCOPE);
            } else {
                mSensorBinder.closeBufferedWriter(SensorType.CMD_SYNC_SENSOR_MARK_GYROSCOPE);
            }
        /*} else if (buttonView == mQuatenionBtn) {
            mSensor.setQuaternionSwitch(isChecked);*/
        } else if (buttonView == mTemperatureBtn) {
            mSensor.setTemperatureSwitch(isChecked);
            if (isChecked) {
                mSensorBinder.createResultCsv(SensorType.CMD_SYNC_SENSOR_MARK_TEMPERATURE);
            } else {
                mSensorBinder.closeTemptureIOBuffer();
            }
        } else if (buttonView == mLightBtn) {
            mSensor.setLightSwitch(isChecked);
            if (isChecked) {
                mSensorBinder.createResultCsv(SensorType.CMD_SYNC_SENSOR_MARK_LIGHT);
            } else {
                mSensorBinder.closeBufferedWriter(SensorType.CMD_SYNC_SENSOR_MARK_LIGHT);
            }
        } else if (buttonView == mEulerBtn) {
            mSensor.setEulerSwitch(isChecked);
            if (isChecked) {
                mSensorBinder.createResultCsv(SensorType.CMD_SYNC_SENSOR_MARK_EULER);
            } else {
                mSensorBinder.closeOrientationIOBuffer();
            }
        } else if (mBackRunningBtn == buttonView) {
            mSensorBinder.setBackRunning(isChecked);
        }
    }

    private SensorListener mSyncSensorListener = new SensorListener() {
        @Override
        public void onSensorChanged(final SensorEvent sensorEvent) {
            final float[] values = sensorEvent.getValues();

            switch (sensorEvent.getSensorType()) {
                case SensorType.CMD_SYNC_SENSOR_MARK_ACCELERATION:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mShowAccX.setText(dfs.format(values[0]));
                            mShowAccY.setText(dfs.format(values[1]));
                            mShowAccZ.setText(dfs.format(values[2]));
                        }
                    });
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_TEMPERATURE:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mShowTemp.setText(dfs.format(values[0]));
                        }
                    });
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_EULER:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mShowOriX.setText(dfs.format(values[0]));
                            mShowOriY.setText(dfs.format(values[1]));
                            mShowOriZ.setText(dfs.format(values[2]));
                        }
                    });
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_GRAVITY:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder builder = new StringBuilder();
                            builder.append(dfs.format(values[0])).append("\n");
                            builder.append(dfs.format(values[1])).append("\n");
                            builder.append(dfs.format(values[2]));
                            mGravityInfoTv.setText(builder.toString());
                        }
                    });
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_MAGNETOMETER:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder builder = new StringBuilder();
                            builder.append(dfs.format(values[0])).append("\n");
                            builder.append(dfs.format(values[1])).append("\n");
                            builder.append(dfs.format(values[2]));
                            mMagnetometerTv.setText(builder.toString());
                        }
                    });
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_GYROSCOPE:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder builder = new StringBuilder();
                            builder.append(dfs.format(values[0])).append("\n");
                            builder.append(dfs.format(values[1])).append("\n");
                            builder.append(dfs.format(values[2]));
                            mGyrosopeTv.setText(builder.toString());
                        }
                    });
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_LIGHT:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder builder = new StringBuilder();
                            builder.append(dfs.format(values[0])).append("\n");
                            mShowLightTv.setText(builder.toString());
                        }
                    });
                    break;

                /*case SensorType.CMD_SYNC_SENSOR_MARK_QUATERNION:
                    mShowText4.setText(sensorEvent.toString());
                    break;*/
                default:
                    break;
            }
        }
    };

    @Override
    public void onServiceConnected(List<IGlass3Device> glass3Devices) {
        LogUtil.i(TAG, "onServiceConnected glass3Devices = " + glass3Devices.size());
    }

    @Override
    public void onServiceDisconnected() {
        LogUtil.i(TAG, "onServiceDisconnected");
    }

    @Override
    public void onDeviceConnect(IGlass3Device device) {
        LogUtil.i(TAG, "onDeviceConnect device = " + device);
        //close old device
        if (mSensor != null) {
            mSensor.unregisterSensorListener(mSyncSensorListener);
        }
        //open new device
        try {
            mSensor = mISensorClient.getGlassSensor(device);
            mSensor.registerSensorLister(SensorType.CMD_SYNC_SENSOR_MARK_ALL, mSyncSensorListener);
            mAccBtn.setChecked(mSensor.isAccelerationOpened());
            mGravityBtn.setChecked(mSensor.isGravityOpened());
            mMagnetometerBtn.setChecked(mSensor.isMagnetometerOpened());
            mGyrosopeBtn.setChecked(mSensor.isGyroscopeOpened());
            /*mQuatenionBtn.setChecked(mSensor.isQuaternionOpened());*/
            mTemperatureBtn.setChecked(mSensor.isTemperatureOpened());
            mEulerBtn.setChecked(mSensor.isEulerOpened());
            mLightBtn.setChecked(mSensor.isLightOpened());
        } catch (BaseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceDisconnect(IGlass3Device device) {
        LogUtil.i(TAG, "onDeviceDisconnect device = " + device);
        if (mSensor != null) {
            mSensor.unregisterSensorListener(mSyncSensorListener);
            mSensor = null;
            //恢复按钮状态
            mAccBtn.setChecked(false);
            mGravityBtn.setChecked(false);
            mMagnetometerBtn.setChecked(false);
            mGyrosopeBtn.setChecked(false);
            /* mQuatenionBtn.setChecked(false);*/
            mTemperatureBtn.setChecked(false);
            mEulerBtn.setChecked(false);
            mLightBtn.setChecked(false);
        }
    }

    @Override
    public void onError(int code, String msg) {
        LogUtil.i(TAG, "onError code = " + code + " msg = " + msg);
    }

}
