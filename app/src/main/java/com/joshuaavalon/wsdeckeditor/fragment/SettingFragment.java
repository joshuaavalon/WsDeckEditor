package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;

import com.joshuaavalon.wsdeckeditor.R;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class SettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting, rootKey);
    }
}
