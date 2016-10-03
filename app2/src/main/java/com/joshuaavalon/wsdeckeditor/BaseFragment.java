package com.joshuaavalon.wsdeckeditor;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {
    @NonNull
    public String getTitle() {
        return "";
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
    }
}
