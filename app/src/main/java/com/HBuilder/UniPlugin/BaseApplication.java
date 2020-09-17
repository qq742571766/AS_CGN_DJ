package com.HBuilder.UniPlugin;

import com.llvision.glass3.api.test.utils.CrashHandler;
import com.llvision.glass3.platform.GlassException;
import com.llvision.glass3.platform.LLVisionGlass3SDK;

import io.dcloud.application.DCloudApplication;

public class BaseApplication extends DCloudApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        try {
            LLVisionGlass3SDK.getInstance().getGlass3DeviceList();
        } catch (GlassException e) {
            e.printStackTrace();
        }
    }
}