package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;

import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.util.WebUtils;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class SettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.setting, rootKey);
        findPreference(getString(R.string.pref_license)).setOnPreferenceClickListener(
                WebUtils.launchUrlFromPreference(getContext(), getString(R.string.license_url)));
        findPreference(getString(R.string.pref_source_code)).setOnPreferenceClickListener(
                WebUtils.launchUrlFromPreference(getContext(), getString(R.string.source_url)));
        findPreference(getString(R.string.pref_version)).setOnPreferenceClickListener(
                WebUtils.launchUrlFromPreference(getContext(), getString(R.string.google_play_url)));
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.nav_setting));
    }
}
