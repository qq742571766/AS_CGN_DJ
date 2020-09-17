package com.llvision.glass3.api.test.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.llvision.glass3.api.test.R;

/**
 *
 * 用户自定义Fragment类，在LCD Fragment扩展屏中显示，需继承Fragment
 *
 * @author xieth
 * @date 2018/9/9
 */

public class UserFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_test_fragment, container, false);
        return view;
    }
}
