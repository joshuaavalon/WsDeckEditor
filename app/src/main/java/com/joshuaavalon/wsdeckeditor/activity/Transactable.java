package com.joshuaavalon.wsdeckeditor.activity;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.joshuaavalon.wsdeckeditor.fragment.BaseFragment;

public interface Transactable {
    void transactTo(@NonNull Fragment fragment);
}
