package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.joshuaavalon.wsdeckeditor.R;

public class SettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.nav_setting));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting, rootKey);
    }
}
