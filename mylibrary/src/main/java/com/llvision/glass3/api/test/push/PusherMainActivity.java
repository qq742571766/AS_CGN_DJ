package com.llvision.glass3.api.test.push;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.llvision.glass3.api.test.R;
import com.llvision.glass3.platform.GlassException;
import com.llvision.glass3.platform.IGlass3Device;
import com.llvision.glass3.platform.LLVisionGlass3SDK;

import java.util.List;

public class PusherMainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pusher_main);
        findViewById(R.id.btn_rtmp).setOnClickListener(this);
        findViewById(R.id.btn_rtsp).setOnClickListener(this);
        findViewById(R.id.btn_rtsp_server).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        try {
            List<IGlass3Device> glass3DeviceList = LLVisionGlass3SDK.getInstance()
                    .getGlass3DeviceList();
            if (glass3DeviceList == null || glass3DeviceList.size() == 0) {
                Toast.makeText(PusherMainActivity.this, "设备未连接", Toast.LENGTH_LONG).show();
                return;
            }
            if (glass3DeviceList.get(0) == null) {
                Toast.makeText(PusherMainActivity.this, "设备未连接", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (GlassException e) {
            e.printStackTrace();
        }
        int id = v.getId();
        if (id == R.id.btn_rtmp) {
            startActivity(new Intent(this, RtmpActivity.class));
        } else if (id == R.id.btn_rtsp) {
            startActivity(new Intent(this, RtspActivity.class));
        } else if (id == R.id.btn_rtsp_server) {
            startActivity(new Intent(this, RtspServerActivity.class));
        }
    }
}
