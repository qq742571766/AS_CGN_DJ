package com.HBuilder.UniPlugin;

import android.app.Activity;
import android.content.Intent;

import com.llvision.glass3.api.test.DisplayActivity;

import org.json.JSONArray;

import io.dcloud.common.DHInterface.IWebview;
import io.dcloud.common.DHInterface.StandardFeature;

public class SkipUtil extends StandardFeature {
    public void skipUtil(final IWebview iwebview, JSONArray array) {
        final Activity activity = iwebview.getActivity();
        Intent intent = new Intent(activity, DisplayActivity.class);
        activity.startActivity(intent);
    }
}