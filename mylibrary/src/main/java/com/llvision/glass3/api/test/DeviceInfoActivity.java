package com.llvision.glass3.api.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.llvision.glass3.library.proxy.SNInfo;
import com.llvision.glass3.platform.ConnectionStatusListener;
import com.llvision.glass3.platform.IGlass3Device;
import com.llvision.glass3.platform.LLVisionGlass3SDK;
import com.llvision.glxss.common.exception.BaseException;
import com.llvision.glxss.common.utils.LogUtil;
import com.llvision.glxss.common.utils.ToastUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * 设备信息
 *
 * @author elena
 * @date  2019/11/1.
 */

public class DeviceInfoActivity extends Activity implements View.OnClickListener, ConnectionStatusListener {

    private IGlass3Device mIGlass3Device;
    private EditText mEditText;
    private TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        findViewById(R.id.btn_get_deviceinfo).setOnClickListener(this);
        findViewById(R.id.btn_set_deviceinfo).setOnClickListener(this);
        mTextView = findViewById(R.id.tv_showinfo);
        mEditText = findViewById(R.id.edt_info);
        LLVisionGlass3SDK.getInstance().registerConnectionListener(this);
        try {
            List<IGlass3Device> glass3Devices = LLVisionGlass3SDK.getInstance().getGlass3DeviceList();
            if (glass3Devices.size() > 0) {
                mIGlass3Device = glass3Devices.get(0);
            }
        } catch (BaseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        if (mIGlass3Device == null) {
            LogUtil.e("Glass device object is null.");
            return;
        }
        int id = v.getId();
        if (id == R.id.btn_get_deviceinfo) {
            try {
                byte[] customFields = mIGlass3Device.getCustomFields();
                if (customFields != null) {
                    String s = new String(customFields);
                    mTextView.setText(s);
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.btn_set_deviceinfo) {
            String s = mEditText.getText().toString();
            if (s == null) {
                ToastUtils.showShort(DeviceInfoActivity.this, "内容不能为空.");
                return;
            }
            if (s.equals("")) {
                ToastUtils.showShort(DeviceInfoActivity.this, "内容不能为空.");
                return;
            }
            try {
                byte[] all = new byte[128];
                byte[] info = s.getBytes("UTF-8");
                int length = s.getBytes("UTF-8").length;
                if (length > 128) {
                    ToastUtils.showShort(DeviceInfoActivity.this, "内容不能超过128字节.");
                    return;
                }
                if (length < 128) {
                    System.arraycopy(info, 0, all, 0, info.length);
                }
                if (length == 128) {
                    all = info;
                }
                mIGlass3Device.writeCustomFields(all);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (BaseException e) {
                e.printStackTrace();
            }
        }
    }

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

    }

    @Override
    public void onError(int code, String msg) {

    }
}
