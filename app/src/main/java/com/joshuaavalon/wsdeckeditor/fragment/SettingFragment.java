package com.joshuaavalon.wsdeckeditor.fragment;

import android.os.Bundle;
import android.support.v7.preference.Preference;

import com.joshuaavalon.wsdeckeditor.BuildConfig;
import com.joshuaavalon.wsdeckeditor.R;
import com.joshuaavalon.wsdeckeditor.activity.BaseActivity;
import com.joshuaavalon.wsdeckeditor.config.CardSuggestionProvider;
import com.joshuaavalon.wsdeckeditor.util.WebUtils;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

public class SettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        final BaseActivity activity = (BaseActivity) getActivity();
        setPreferencesFromResource(R.xml.setting, rootKey);
        findPreference(getString(R.string.pref_license)).setOnPreferenceClickListener(
                WebUtils.launchUrlFromPreference(getContext(), getString(R.string.license_url)));
        findPreference(getString(R.string.pref_source_code)).setOnPreferenceClickListener(
                WebUtils.launchUrlFromPreference(getContext(), getString(R.string.source_url)));
        final Preference version = findPreference(getString(R.string.pref_version));
        version.setOnPreferenceClickListener(WebUtils.launchUrlFromPreference(getContext(),
                getString(R.string.google_play_url)));
        version.setSummary(BuildConfig.VERSION_NAME);
        findPreference(getString(R.string.pref_clear_quick_history))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new CardSuggestionProvider(getContext(), activity.getCardRepository()).clearHistory();
                        activity.showMessage(R.string.msg_clear_history);
                        return true;
                    }
                });
        findPreference(getString(R.string.pref_database_version))
                .setSummary(String.valueOf(activity.getCardRepository().version()));
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getString(R.string.nav_setting));
    }
}
