package com.joshuaavalon.wsdeckeditor.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.joshuaavalon.wsdeckeditor.R;

public class AboutFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.nav_about);
    }
}
