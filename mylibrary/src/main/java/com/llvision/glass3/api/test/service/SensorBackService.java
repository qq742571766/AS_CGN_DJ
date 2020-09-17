package com.llvision.glass3.api.test.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.llvision.glass3.api.test.utils.CVSConstants;
import com.llvision.glass3.core.sensor.SensorEvent;
import com.llvision.glass3.core.sensor.SensorType;
import com.llvision.glass3.core.sensor.client.IGlassSensor;
import com.llvision.glass3.core.sensor.client.ISensorClient;
import com.llvision.glass3.core.sensor.client.SensorListener;
import com.llvision.glass3.platform.ConnectionStatusListener;
import com.llvision.glass3.platform.IGlass3Device;
import com.llvision.glass3.platform.LLVisionGlass3SDK;
import com.llvision.glass3.platform.utils.Constacts;
import com.llvision.glxss.common.exception.BaseException;
import com.llvision.glxss.common.utils.FileUtil;
import com.llvision.glxss.common.utils.LogUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SensorBackService extends Service implements ConnectionStatusListener {

    private static final String TAG = SensorBackService.class.getSimpleName();
    private static final String TIME_FORMAT = "yyyy-MM-dd-HH:mm:ss";

    private SensorBinder mSensorBinder;
    private IGlassSensor mSensor;
    private ISensorClient mISensorClient;
    private boolean mBackRunning;

    private StringBuilder mCVSBuild = new StringBuilder();
    private File mAccCVSFile;
    private File mTemeratureCVSFile;
    private File mOrientationCVSFile;
    private File mLightCVSFile;
    private File mGravityCVSFile;
    private File mMagnetometerCVSFile;
    private File mGyroscopeCVSFile;

    private BufferedWriter mAccBufferedWriter;
    private BufferedWriter mTemptureBufferedWriter;
    private BufferedWriter mOrientationBufferedWriter;
    private BufferedWriter mLightWriter;
    private BufferedWriter mGravityWriter;
    private BufferedWriter mMagnetometerWriter;
    private BufferedWriter mGyroscopeWriter;


    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(TAG, "onCreate");
        mBackRunning = false;
        LLVisionGlass3SDK.getInstance().registerConnectionListener(this);
        try {
            List<IGlass3Device> glass3Devices = LLVisionGlass3SDK.getInstance().getGlass3DeviceList();
            mISensorClient = (ISensorClient) LLVisionGlass3SDK.getInstance().getGlass3Client(IGlass3Device.Glass3DeviceClient.SENSOR);
            if (glass3Devices.size() > 0) {
                IGlass3Device glass3Device = glass3Devices.get(0);
                mSensor = mISensorClient.getGlassSensor(glass3Device);
                LogUtil.i(TAG, "sensor = " + mSensor);
                mSensor.registerSensorLister(SensorType.CMD_SYNC_SENSOR_MARK_ALL, mSyncSensorListener);
            }
        } catch (BaseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i(TAG, "onDestroy");
        mBackRunning = false;
        LLVisionGlass3SDK.getInstance().unRegisterConnectionListener(this);
        if (mSensor != null) {
            mSensor.unregisterSensorListener(mSyncSensorListener);
            mSensor = null;
        }
        closeAccIOBuffer();
        closeTemptureIOBuffer();
        closeOrientationIOBuffer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogUtil.i(TAG, "bind sensor service");
        if (mSensorBinder == null) {
            mSensorBinder = new SensorBinder();
        }
        return mSensorBinder;
    }

    public class SensorBinder extends Binder {

        /**
         * 创建统计数据
         *
         * @param sensorType
         */
        public void createResultCsv(int sensorType) {
            switch (sensorType) {
                case SensorType.CMD_SYNC_SENSOR_MARK_ACCELERATION:
                    mAccCVSFile = SensorBackService.this.createResultCsv(CVSConstants.SENSOR_TYPE_ACC);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_TEMPERATURE:
                    mTemeratureCVSFile = SensorBackService.this.createResultCsv(CVSConstants.SENSOR_TYPE_TEM);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_EULER:
                    mOrientationCVSFile = SensorBackService.this.createResultCsv(CVSConstants.SENSOR_TYPE_EULER);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_LIGHT:
                    mLightCVSFile = SensorBackService.this.createResultCsv(CVSConstants.SENSOR_TYPE_LIGHT);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_GRAVITY:
                    mGravityCVSFile = SensorBackService.this.createResultCsv(CVSConstants.SENSOR_TYPE_GRAVITY);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_MAGNETOMETER:
                    mMagnetometerCVSFile = SensorBackService.this.createResultCsv(CVSConstants.SENSOR_TYPE_MAGNETOMETER);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_GYROSCOPE:
                    mGyroscopeCVSFile = SensorBackService.this.createResultCsv(CVSConstants.SENSOR_TYPE_GYROSCOPE);
                    break;
                default:
                    break;
            }
        }

        public void closeBufferedWriter(int sensorType) {
            switch (sensorType) {
                case SensorType.CMD_SYNC_SENSOR_MARK_ACCELERATION:
                    closeAccIOBuffer();
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_TEMPERATURE:
                    closeTemptureIOBuffer();
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_EULER:
                    closeOrientationIOBuffer();
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_LIGHT:
                    SensorBackService.this.closeBuffer(mLightWriter);
                    mLightWriter = null;
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_GRAVITY:
                    SensorBackService.this.closeBuffer(mGravityWriter);
                    mGravityWriter = null;
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_MAGNETOMETER:
                    SensorBackService.this.closeBuffer(mMagnetometerWriter);
                    mMagnetometerWriter = null;
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_GYROSCOPE:
                    SensorBackService.this.closeBuffer(mGyroscopeWriter);
                    mGyroscopeWriter = null;
                    break;
                default:
                    break;
            }
        }

        public void closeAccIOBuffer() {
            SensorBackService.this.closeAccIOBuffer();
        }

        public void closeTemptureIOBuffer() {
            SensorBackService.this.closeTemptureIOBuffer();
        }

        public void closeOrientationIOBuffer() {
            SensorBackService.this.closeOrientationIOBuffer();
        }

        public void setBackRunning(boolean backRunning) {
            mBackRunning = backRunning;
        }

        public boolean isBackRunning() {
            return mBackRunning;
        }
    }

    private SensorListener mSyncSensorListener = new SensorListener() {
        @Override
        public void onSensorChanged(final SensorEvent sensorEvent) {
//            LogUtil.i(TAG, sensorEvent.toString());
            if (mCVSBuild != null) {
                mCVSBuild.setLength(0);
                mCVSBuild.append(getTimeString()).append(CVSConstants.COMMA);
                float[] values = sensorEvent.getValues();
                for (int i = 0; i < sensorEvent.getValueSize(); i++) {
                    mCVSBuild.append(values[i]).append(CVSConstants.COMMA);
                }
                mCVSBuild.append(CVSConstants.LINE_END);
            }
            final String content = mCVSBuild.toString();

            switch (sensorEvent.getSensorType()) {
                case SensorType.CMD_SYNC_SENSOR_MARK_ACCELERATION:
                    writeAccCVSFile(content);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_TEMPERATURE:
                    writeTemptureCVSFile(content);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_EULER:
                    writeOrientationCVSFile(content);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_LIGHT:
                    writeLightCVSFile(content);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_GRAVITY:
                    writeGravityCVSFile(content);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_MAGNETOMETER:
                    writeMagnetometerCVSFile(content);
                    break;
                case SensorType.CMD_SYNC_SENSOR_MARK_GYROSCOPE:
                    writeGyroscopeCVSFile(content);
                    break;
                default:
                    break;
            }
        }
    };

    private void writeGyroscopeCVSFile(String content) {
        if (mGyroscopeCVSFile != null) {
            try {
                if (mGyroscopeWriter == null) {
                    mGyroscopeWriter = new BufferedWriter(new FileWriter(mGyroscopeCVSFile, true));
                }
                mGyroscopeWriter.write(content);
                mGyroscopeWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeBuffer(mGyroscopeWriter);
            }
        }
    }

    private void writeMagnetometerCVSFile(String content) {
        if (mMagnetometerCVSFile != null) {
            try {
                if (mMagnetometerWriter == null) {
                    mMagnetometerWriter = new BufferedWriter(new FileWriter(mMagnetometerCVSFile, true));
                }
                mMagnetometerWriter.write(content);
                mMagnetometerWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeBuffer(mMagnetometerWriter);
            }
        }
    }

    private void writeGravityCVSFile(String content) {
        if (mGravityCVSFile != null) {
            try {
                if (mGravityWriter == null) {
                    mGravityWriter = new BufferedWriter(new FileWriter(mGravityCVSFile, true));
                }
                mGravityWriter.write(content);
                mGravityWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeBuffer(mGravityWriter);
            }
        }

    }

    private void writeLightCVSFile(String content) {
        if (mLightCVSFile != null) {
            try {
                if (mLightWriter == null) {
                    mLightWriter = new BufferedWriter(new FileWriter(mLightCVSFile, true));
                }
                mLightWriter.write(content);
                mLightWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeBuffer(mLightWriter);
            }
        }
    }

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
        }
    }

    @Override
    public void onError(int code, String msg) {
        LogUtil.i(TAG, "onError code = " + code + " msg = " + msg);
    }

    /**
     * 创建统计数据
     *
     * @param type
     */
    private File createResultCsv(String type) {
        try {
            File file = new File(Constacts.EXTERNAL_DIR_G26 + "/sensor/" + type + getTimeString() + ".csv");
            if (FileUtil.createFile(file)) {
                //写入项目信息
                mCVSBuild.setLength(0);
                mCVSBuild.append("测试项目").append(CVSConstants.COMMA)
                        .append(type).append(CVSConstants.LINE_END)
                        .append("测试开始时间").append(CVSConstants.COMMA)
                        .append(getTimeString()).append(CVSConstants.LINE_END);
                mCVSBuild.append("测试时间").append(CVSConstants.COMMA)
                        .append("数据1").append(CVSConstants.COMMA)
                        .append("数据2").append(CVSConstants.COMMA)
                        .append("数据3").append(CVSConstants.COMMA)
                        .append("数据4").append(CVSConstants.COMMA)
                        .append(CVSConstants.LINE_END);
                FileUtil.writeFile(mCVSBuild.toString(), file);
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void writeAccCVSFile(String content) {
        if (mAccCVSFile != null) {
            try {
                if (mAccBufferedWriter == null) {
                    mAccBufferedWriter = new BufferedWriter(new FileWriter(mAccCVSFile, true));
                }
                mAccBufferedWriter.write(content);
                mAccBufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeAccIOBuffer();
            }
        }
    }

    private void closeAccIOBuffer() {
        LogUtil.i("closeAccIOBuffer");
        if (mAccBufferedWriter != null) {
            try {
                mAccBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mAccBufferedWriter = null;
        }
        mAccCVSFile = null;
    }

    private void writeTemptureCVSFile(String content) {
        if (mTemeratureCVSFile != null) {
            try {
                if (mTemptureBufferedWriter == null) {
                    mTemptureBufferedWriter = new BufferedWriter(new FileWriter(mTemeratureCVSFile, true));
                }
                mTemptureBufferedWriter.write(content);
                mTemptureBufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeTemptureIOBuffer();
            }
        }
    }

    private void closeTemptureIOBuffer() {
        LogUtil.i("closeAccIOBuffer");
        if (mTemptureBufferedWriter != null) {
            try {
                mTemptureBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mTemptureBufferedWriter = null;
        }
        mTemeratureCVSFile = null;
    }

    private void writeOrientationCVSFile(String content) {
        if (mOrientationCVSFile != null) {
            try {
                if (mOrientationBufferedWriter == null) {
                    mOrientationBufferedWriter = new BufferedWriter(new FileWriter(mOrientationCVSFile, true));
                }
                mOrientationBufferedWriter.write(content);
                mOrientationBufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
                closeOrientationIOBuffer();
            }
        }
    }

    private void closeBuffer(BufferedWriter bufferedWriter) {
        if (bufferedWriter != null) {
            try {
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bufferedWriter = null;
        }
    }

    private void closeOrientationIOBuffer() {
        LogUtil.i("closeOrientationIOBuffer");
        if (mOrientationBufferedWriter != null) {
            try {
                mOrientationBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mOrientationBufferedWriter = null;
        }
        mOrientationCVSFile = null;
    }

    private static String getTimeString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
        Date curDate = new Date(System.currentTimeMillis());
        return dateFormat.format(curDate);
    }

}
