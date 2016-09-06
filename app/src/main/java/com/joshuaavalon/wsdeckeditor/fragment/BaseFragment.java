package com.joshuaavalon.wsdeckeditor.fragment;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.joshuaavalon.wsdeckeditor.activity.BaseActivity;

public abstract class BaseFragment extends Fragment {

    public void showMessage(@StringRes final int resId) {
        final Activity parentActivity = getActivity();
        if (parentActivity instanceof BaseActivity)
            ((BaseActivity) parentActivity).showMessage(resId);
        else
            Toast.makeText(getContext(), resId, Toast.LENGTH_LONG).show();
    }
}
