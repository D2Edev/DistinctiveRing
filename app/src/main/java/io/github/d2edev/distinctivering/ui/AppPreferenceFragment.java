package io.github.d2edev.distinctivering.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;


import io.github.d2edev.distinctivering.R;
import io.github.d2edev.distinctivering.util.Utility;

/**
 * Created by d2e on 07.07.16.
 */
public class AppPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {
    public static final String TAG = "TAG_" + AppPreferenceFragment.class.getSimpleName();
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_settings);
        Preference preference = findPreference(getString(R.string.pref_key_show_on_startup));
        bindPreferenceSummaryToValue(preference);
        setPreferenceSummary(preference, Utility.getShowStartupMessage(getActivity()));
        preference = findPreference(getString(R.string.pref_key_time_window));
        bindPreferenceSummaryToValue(preference);
        setPreferenceSummary(preference, Utility.getTimeWindow(getActivity()));
        sp = PreferenceManager.getDefaultSharedPreferences(getContext());


    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
//        preference
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        sp.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_time_window))) {
            Preference pref=findPreference(key);
            setPreferenceSummary(
                    pref,
                    sharedPreferences.getInt(key,30));
        }

    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        DialogFragment fragment;
        if (preference instanceof SeekBarDialogPreference) {
            fragment = SeekBarDialogPreferenceFragmentCompat.newInstance(preference);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else super.onDisplayPreferenceDialog(preference);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        setPreferenceSummary(preference, o);
        return true;
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.pref_key_show_on_startup))) {
            boolean enabled = Boolean.valueOf(value.toString());
            preference.setSummary(enabled ?
                    getActivity().getString(R.string.enabled) :
                    getActivity().getString(R.string.disabled));

        } else if (key.equals(getString(R.string.pref_key_time_window))) {
            preference.setSummary("" + value.toString() + " " + getString(R.string.secs));
        } else {

        }
    }
}
