package com.llvision.glass3.api.test;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.llvision.glass3.api.test.ui.UserFragment;
import com.llvision.glass3.api.test.utils.ForgroundServiceUtils;
import com.llvision.glass3.core.lcd.client.DisplayStatusListener;
import com.llvision.glass3.core.lcd.client.IGlassDisplay;
import com.llvision.glass3.core.lcd.client.ILCDClient;
import com.llvision.glass3.library.ResultCode;
import com.llvision.glass3.library.VendorInfo;
import com.llvision.glass3.library.boot.FirmwareInfo;
import com.llvision.glass3.library.lcd.LCDInfo;
import com.llvision.glass3.library.lcd.OledInfo;
import com.llvision.glass3.platform.ConnectionStatusListener;
import com.llvision.glass3.platform.IGlass3Device;
import com.llvision.glass3.platform.LLVisionGlass3SDK;
import com.llvision.glxss.common.exception.BaseException;
import com.llvision.glxss.common.utils.LogUtil;
import com.llvision.glxss.common.utils.StringUtil;
import com.llvision.glxss.common.utils.ToastUtils;

import java.util.List;

public class DisplayActivity extends FragmentActivity implements View.OnClickListener,
        ConnectionStatusListener,
        DisplayStatusListener {

    private static final String TAG = "LCDActivity";
    private IGlassDisplay mGlassDisplay;
    private Switch lcdOpen;
    private Switch lcdAuto;
    private RadioGroup mRadioGroup;
    private RadioGroup rgTrasfer;
    private ILCDClient mIlcdClient;
    private TextView mTvDisplayModel;
    private Button mBtnStopDisplayBtn;
    private Button mRbSyncTrasfer;
    private Button mBtnSetDeviceParaOLEDBtn;
    private Button mBtnSetOLEDBtn;
    private Button mBtnGetDeviceParaOLEDBtn;
    private EditText mEdtLh;
    private EditText mEdtLv;
    private EditText mEdtRh;
    private EditText mEdtRv;
    private TextView mTvShowOledInfo;
    private View mGlassOverlayView;
    private UserFragment mGlassUserFragment;
    private ForgroundServiceUtils mForgroundServiceUtils;
    private Handler mUIHandler;
    private LinearLayout mOledEditLayout;
    private LinearLayout mOledBtnLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lcd_main);
        mBtnStopDisplayBtn = (Button) findViewById(R.id.btn_stoplcd);
        mRbSyncTrasfer = (AppCompatRadioButton) findViewById(R.id.rb_sync_trasfer);
        mBtnSetOLEDBtn = (Button) findViewById(R.id.btn_set_oled);
        mBtnSetDeviceParaOLEDBtn = (Button) findViewById(R.id.btn_set_device_para_oled);
        mBtnGetDeviceParaOLEDBtn = (Button) findViewById(R.id.btn_get_device_para_oled);
        mEdtLh = findViewById(R.id.edt_lh);
        mEdtLv = findViewById(R.id.edt_lv);
        mEdtRh = findViewById(R.id.edt_rh);
        mEdtRv = findViewById(R.id.edt_rv);
        mTvShowOledInfo = findViewById(R.id.tv_show_oled_info);
        rgTrasfer = findViewById(R.id.rg_trasfer);
        lcdOpen = findViewById(R.id.sw_lcd);
        lcdAuto = findViewById(R.id.sw_lcd_auto);
        mRadioGroup = findViewById(R.id.rg_lum);
        mTvDisplayModel = findViewById(R.id.tv_model);
        mOledEditLayout = findViewById(R.id.id_oled_edit_layout);
        mOledBtnLayout = findViewById(R.id.id_oled_btn_layout);
        LLVisionGlass3SDK.getInstance().registerConnectionListener(this);
        initDisplayInfo();
        mBtnStopDisplayBtn.setOnClickListener(this);
        mBtnSetOLEDBtn.setOnClickListener(this);
        mBtnSetDeviceParaOLEDBtn.setOnClickListener(this);
        mBtnGetDeviceParaOLEDBtn.setOnClickListener(this);
        mRadioGroup.setOnCheckedChangeListener(onRadioGroupChangeListener);
        lcdOpen.setOnCheckedChangeListener(onCheckedChangeListener);
        lcdAuto.setOnCheckedChangeListener(onCheckedChangeListener);
        rgTrasfer.setOnCheckedChangeListener(onRadioGroupChangeListener);
        mUIHandler = new Handler();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    private void initDisplayInfo() {
        try {
            List<IGlass3Device> glass3Devices = LLVisionGlass3SDK.getInstance()
                    .getGlass3DeviceList();
            mIlcdClient = (ILCDClient) LLVisionGlass3SDK.getInstance().getGlass3Client
                    (IGlass3Device.Glass3DeviceClient.LCD);
            if (glass3Devices.size() > 0) {
                IGlass3Device glass3Device = glass3Devices.get(0);
                FirmwareInfo firmwareInfo = glass3Device.getFirmwareInfo();
                if (StringUtil.isNotEmpty(firmwareInfo.projectName) &&
                        (firmwareInfo.projectName.contains(VendorInfo.PRODUCT_NAME_R10) ||
                                firmwareInfo.projectName.contains(VendorInfo.PRODUCT_NAME_R11))) {
                    mOledEditLayout.setVisibility(View.VISIBLE);
                    mOledBtnLayout.setVisibility(View.VISIBLE);
                } else {
                    mOledEditLayout.setVisibility(View.GONE);
                    mOledBtnLayout.setVisibility(View.GONE);
                }

                mGlassDisplay = mIlcdClient.getGlassDisplay(glass3Device);
                LCDInfo lcdInfo = mGlassDisplay.getLCDInfo();
                mRadioGroup.setOnCheckedChangeListener(null);
                mRadioGroup.setOnCheckedChangeListener(onRadioGroupChangeListener);
                if (lcdInfo != null) {
                    switch (lcdInfo.level) {
                        case IGlassDisplay.LUM_LEVEL_0:
                            mRadioGroup.check(R.id.lum_level_0);
                            break;
                        case IGlassDisplay.LUM_LEVEL_1:
                            mRadioGroup.check(R.id.lum_level_1);
                            break;
                        case IGlassDisplay.LUM_LEVEL_2:
                            mRadioGroup.check(R.id.lum_level_2);
                            break;
                        case IGlassDisplay.LUM_LEVEL_3:
                            mRadioGroup.check(R.id.lum_level_3);
                            break;
                        case IGlassDisplay.LUM_LEVEL_MAX:
                            mRadioGroup.check(R.id.lum_level_max);
                            break;
                        default:
                            break;
                    }
                    lcdOpen.setOnCheckedChangeListener(null);
                    lcdOpen.setChecked(lcdInfo.open);
                    lcdAuto.setOnCheckedChangeListener(null);
                    lcdAuto.setChecked(lcdInfo.backLightAuto);
                    lcdOpen.setOnCheckedChangeListener(onCheckedChangeListener);
                    lcdAuto.setOnCheckedChangeListener(onCheckedChangeListener);
                    setDisplayModelType(lcdInfo.displayMode);
                }
            }
        } catch (BaseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (mGlassDisplay != null) {
            mGlassDisplay.stopCaptureScreen();
        }
        LLVisionGlass3SDK.getInstance().unRegisterConnectionListener(this);

        super.onDestroy();

    }

    private void resetView() {
        lcdOpen.setOnCheckedChangeListener(null);
        lcdOpen.setChecked(false);
        lcdAuto.setOnCheckedChangeListener(null);
        lcdAuto.setChecked(false);
        lcdOpen.setOnCheckedChangeListener(onCheckedChangeListener);
        lcdAuto.setOnCheckedChangeListener(onCheckedChangeListener);
        mBtnStopDisplayBtn.setVisibility(View.GONE);
        mRadioGroup.setOnCheckedChangeListener(null);
        mRadioGroup.clearCheck();
        mRadioGroup.setOnCheckedChangeListener(onRadioGroupChangeListener);
        rgTrasfer.setOnCheckedChangeListener(null);
        rgTrasfer.clearCheck();
        rgTrasfer.setOnCheckedChangeListener(onRadioGroupChangeListener);
    }

    private RadioGroup.OnCheckedChangeListener onRadioGroupChangeListener = new RadioGroup
            .OnCheckedChangeListener() {


        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (mGlassDisplay == null) {
                ToastUtils.showLong(DisplayActivity.this, "设备未连接");
                return;
            }
            if (group.getId() == R.id.rg_lum) {
                if (lcdAuto.isChecked()) {
                    lcdAuto.setChecked(false);
                }
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                if (checkedRadioButtonId == R.id.lum_level_0) {
                    mGlassDisplay.setLuminance(IGlassDisplay.LUM_LEVEL_0);
                } else if (checkedRadioButtonId == R.id.lum_level_1) {
                    mGlassDisplay.setLuminance(IGlassDisplay.LUM_LEVEL_1);
                } else if (checkedRadioButtonId == R.id.lum_level_2) {
                    mGlassDisplay.setLuminance(IGlassDisplay.LUM_LEVEL_2);
                } else if (checkedRadioButtonId == R.id.lum_level_3) {
                    mGlassDisplay.setLuminance(IGlassDisplay.LUM_LEVEL_3);
                } else if (checkedRadioButtonId == R.id.lum_level_max) {
                    mGlassDisplay.setLuminance(IGlassDisplay.LUM_LEVEL_MAX);
                }
            } else {

                if (checkedId == R.id.rb_sync_trasfer) {
                    mGlassDisplay.createCaptureScreen(DisplayActivity.this);
                    mBtnStopDisplayBtn.setVisibility(View.VISIBLE);
                    mRbSyncTrasfer.setVisibility(View.GONE);
                    mBtnStopDisplayBtn.setText("停止同屏");
                    ToastUtils.showLong(DisplayActivity.this, "开启同屏");
                    setDisplayModelType(0);
                    lcdOpen.setChecked(true);
                } else if (checkedId == R.id.rb_overlay_trasfer) {
                    if (mGlassOverlayView == null) {
                        mGlassOverlayView = getLayoutInflater().inflate(R.layout
                                .layout_glass_screen, null);
                    }
                    View cv = getWindow().getDecorView();
                    mGlassDisplay.createCaptureScreen(DisplayActivity.this, cv);
                    ToastUtils.showLong(DisplayActivity.this, "开启扩展屏");
                    mBtnStopDisplayBtn.setText("STOP OVERLAY TRASFER");
                    mBtnStopDisplayBtn.setVisibility(View.VISIBLE);
                    setDisplayModelType(1);
                    lcdOpen.setChecked(true);
                } else if (checkedId == R.id.rb_fragment_trasfer) {//                        if (mGlassUserFragment == null) {
//                            mGlassUserFragment = new UserFragment();
//                        }
                    //创建LCDFragment并显示
                    mGlassDisplay.createCaptureScreen(DisplayActivity.this, new UserFragment(),
                            getSupportFragmentManager());
                    ToastUtils.showLong(DisplayActivity.this, "开启fragment扩展屏");
                    mBtnStopDisplayBtn.setText("STOP FRAGMENT TRASFER");
                    mBtnStopDisplayBtn.setVisibility(View.VISIBLE);
                    setDisplayModelType(1);
                    lcdOpen.setChecked(true);
                }
            }
        }
    };
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton
            .OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.sw_lcd) {
                if (mGlassDisplay == null) {
                    ToastUtils.showShort(DisplayActivity.this, "设备未连接");
                    lcdOpen.setOnCheckedChangeListener(null);
                    lcdOpen.setChecked(false);
                    lcdOpen.setOnCheckedChangeListener(onCheckedChangeListener);
                    return;
                }
                boolean ret = mGlassDisplay.setSwitch(isChecked);
                if (ret) {
                    String prompt = isChecked ? "已开启" : "已关闭";
                    ToastUtils.showShort(DisplayActivity.this, prompt);
                    if (!isChecked) {
                        rgTrasfer.setOnCheckedChangeListener(null);
                        rgTrasfer.clearCheck();
                        mGlassDisplay.stopCaptureScreen();
                        mBtnStopDisplayBtn.setVisibility(View.GONE);
                        rgTrasfer.setOnCheckedChangeListener(onRadioGroupChangeListener);
                    }
                } else {
                    lcdOpen.setOnCheckedChangeListener(null);
                    lcdOpen.setChecked(false);
                    lcdOpen.setOnCheckedChangeListener(onCheckedChangeListener);
                    ToastUtils.showShort(DisplayActivity.this, "调用接口失败");
                }
            } else if (id == R.id.sw_lcd_auto) {
                if (mGlassDisplay == null) {
                    ToastUtils.showShort(DisplayActivity.this, "设备未连接");
                    lcdAuto.setOnCheckedChangeListener(null);
                    lcdAuto.setChecked(false);
                    lcdAuto.setOnCheckedChangeListener(onCheckedChangeListener);
                    return;
                }
                boolean ret1 = mGlassDisplay.setBackLightAuto(isChecked);
                LogUtil.i(TAG, "LCD activity bOpen->" + isChecked + " ret = " + ret1);
                if (ret1) {
                    String prompt = isChecked ? "已开启" : "已关闭";
                    ToastUtils.showShort(DisplayActivity.this, prompt);
                } else {
                    lcdAuto.setOnCheckedChangeListener(null);
                    lcdAuto.setChecked(false);
                    lcdAuto.setOnCheckedChangeListener(onCheckedChangeListener);
                    ToastUtils.showShort(DisplayActivity.this, "调用接口失败");
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (mGlassDisplay == null) {
            ToastUtils.showLong(this, R.string.service_disconnect);
            return;
        }
        String lh, lv, rh, rv = null;
        int lhInt,lvInt, rhInt, rvInt, ret;
        byte[] data = null;
        int id = v.getId();
        if (id == R.id.btn_stoplcd) {//设置Overlay模式
            mGlassDisplay.stopCaptureScreen();
            rgTrasfer.setOnCheckedChangeListener(null);
            rgTrasfer.clearCheck();
            rgTrasfer.setOnCheckedChangeListener(onRadioGroupChangeListener);
            mBtnStopDisplayBtn.setVisibility(View.GONE);
            ToastUtils.showLong(this, "停止同屏");
            mRbSyncTrasfer.setVisibility(View.VISIBLE);
        } else if (id == R.id.btn_set_oled) {
            lh = mEdtLh.getText().toString();
            if (lh == null) {
                ToastUtils.showLong(this, "lh不能为空.");
                return;
            }
            if (lh.equals("")) {
                ToastUtils.showLong(this, "lh不能为空.");
                return;
            }
            lhInt = Integer.valueOf(lh);
            if (!(lhInt >= -4 && lhInt <= 4)) {
                ToastUtils.showLong(this, "lh范围为-4 ~ 4.");
                return;
            }
            lv = mEdtLv.getText().toString();
            if (lv == null) {
                ToastUtils.showLong(this, "lv不能为空.");
                return;
            }
            if (lv.equals("")) {
                ToastUtils.showLong(this, "lv不能为空.");
                return;
            }
            lvInt = Integer.valueOf(lv);
            if (!(lvInt >= -10 && lvInt <= 10)) {
                ToastUtils.showLong(this, "lv范围为-10 ~ 10.");
                return;
            }
            rv = mEdtRv.getText().toString();
            if (rv == null) {
                ToastUtils.showLong(this, "rv不能为空.");
                return;
            }
            if (rv.equals("")) {
                ToastUtils.showLong(this, "rv不能为空.");
                return;
            }
            rvInt = Integer.valueOf(rv);
            if (!(rvInt >= -10 && rvInt <= 10)) {
                ToastUtils.showLong(this, "rv范围为-10 ~ 10.");
                return;
            }
            rh = mEdtRh.getText().toString();
            if (rh == null) {
                ToastUtils.showLong(this, "rh不能为空.");
                return;
            }
            if (rh.equals("")) {
                ToastUtils.showLong(this, "rh不能为空.");
                return;
            }
            rhInt = Integer.valueOf(rh);
            if (!(rhInt >= -4 && rhInt <= 4)) {
                ToastUtils.showLong(this, "rh范围为-4 ~ 4.");
                return;
            }
            data = new byte[128];
            for (int i = 0; i < 128; i++) {
                data[i] = 0;
            }
            ret = mGlassDisplay.setOledCalibration(lhInt, lvInt, rhInt, rvInt, data);
            mTvShowOledInfo.setText("Set Ret:" + ret);
            if (ret == ResultCode.ERROR_NOT_SUPPORTED) {
                ToastUtils.showLong(this, "设备不支持.");
            }
        } else if (id == R.id.btn_set_device_para_oled) {
            lh = mEdtLh.getText().toString();
            if (lh == null) {
                ToastUtils.showLong(this, "lh不能为空.");
                return;
            }
            if (lh.equals("")) {
                ToastUtils.showLong(this, "lh不能为空.");
                return;
            }
            lhInt = Integer.valueOf(lh);
            if (!(lhInt >= -4 && lhInt <= 4)) {
                ToastUtils.showLong(this, "lh范围为-4 ~ 4.");
                return;
            }
            lv = mEdtLv.getText().toString();
            if (lv == null) {
                ToastUtils.showLong(this, "lv不能为空.");
                return;
            }
            if (lv.equals("")) {
                ToastUtils.showLong(this, "lv不能为空.");
                return;
            }
            lvInt = Integer.valueOf(lv);
            if (!(lvInt >= -10 && lvInt <= 10)) {
                ToastUtils.showLong(this, "lv范围为-10 ~ 10.");
                return;
            }
            rv = mEdtRv.getText().toString();
            if (rv == null) {
                ToastUtils.showLong(this, "rv不能为空.");
                return;
            }
            if (rv.equals("")) {
                ToastUtils.showLong(this, "rv不能为空.");
                return;
            }
            rvInt = Integer.valueOf(rv);
            if (!(rvInt >= -10 && rvInt <= 10)) {
                ToastUtils.showLong(this, "rv范围为-10 ~ 10.");
                return;
            }
            rh = mEdtRh.getText().toString();
            if (rh == null) {
                ToastUtils.showLong(this, "rh不能为空.");
                return;
            }
            if (rh.equals("")) {
                ToastUtils.showLong(this, "rh不能为空.");
                return;
            }
            rhInt = Integer.valueOf(rh);
            if (!(rhInt >= -4 && rhInt <= 4)) {
                ToastUtils.showLong(this, "rh范围为-4 ~ 4.");
                return;
            }
            data = new byte[128];
            for (int i = 0; i < 128; i++) {
                data[i] = 0;
            }
            ret = mGlassDisplay.setDeviceParaOledCalibration(lhInt, lvInt, rhInt, rvInt, data);
            mTvShowOledInfo.setText("Set Ret:" + ret);
            if (ret == ResultCode.ERROR_NOT_SUPPORTED) {
                ToastUtils.showLong(this, "设备不支持.");
            }
        } else if (id == R.id.btn_get_device_para_oled) {
            OledInfo oledInfo = mGlassDisplay.getDeviceParaOledCalibration();
            if (oledInfo != null) {
                mTvShowOledInfo.setText(oledInfo.toString());
            } else {
                LogUtil.e("Activity#getOledCalibration#oledInfo = null");
                mTvShowOledInfo.setText("获取信息失败.");
            }
        }
    }

    private void setDisplayModelType(int code) {
        String msg = "LCD model:null";
        switch (code) {
            case 0:
                msg = "LCD model: Sync";
                break;
            case 1:
                msg = "LCD model: Overlay";
                break;
            case 2:
                msg = "LCD model: Fragment";
                break;
            default:
                break;
        }
        mTvDisplayModel.setText(msg);
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
        initDisplayInfo();
    }

    @Override
    public void onDeviceDisconnect(IGlass3Device device) {
        LogUtil.i(TAG, "onDeviceDisconnect device = " + device);
        if (mGlassDisplay != null) {
            mGlassDisplay.stopCaptureScreen();
            mGlassDisplay.release();
            mGlassDisplay = null;
        }
        resetView();
    }

    @Override
    public void onError(int code, String msg) {
        LogUtil.i(TAG, "onError code = " + code + " msg = " + msg);
        resetView();
    }

    @Override
    public void onPrepare(int displayMode) {
        LogUtil.i(TAG, "onPrepare displayMode = " + displayMode);
    }

    @Override
    public void onStarted(int displayMode) {
        LogUtil.i(TAG, "onStarted displayMode = " + displayMode);
    }

    @Override
    public void onStopped(int displayMode) {
        LogUtil.i(TAG, "onStopped displayMode = " + displayMode);
    }

    @Override
    public void onError(int displayMode, int code) {
        LogUtil.i(TAG, "onError displayMode = " + displayMode + " code = " + code);
    }

}
