package io.github.d2edev.distinctivering.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import io.github.d2edev.distinctivering.R;

/**
 * Created by d2e on 10.07.16.
 */
public class SeekBarDialogPreferenceFragmentCompat extends PreferenceDialogFragmentCompat implements SeekBar.OnSeekBarChangeListener {
    private static final String SEEK_BAR_VALUE_KEY = "seek_bar_value_key";
    public static final String TAG = "TAG_" + SeekBarDialogPreferenceFragmentCompat.class.getSimpleName();
    private SeekBar mSeekBar;
    private TextView mValueView;
    private SeekBarDialogPreference mSeekBarDialogPreference;
    private float mMin = 0, mMax = 100, mDefault = 50;
    private String mUnit, mKey;
    private int mSavedValue, mCurrentValue;

    @Override
    public void onDialogClosed(boolean b) {
        if(!b)return;
        if(mSeekBarDialogPreference.shouldPersist()){
            mSeekBarDialogPreference.persistInt(mCurrentValue);
        }
        mSeekBarDialogPreference.notifyChanged();

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mSeekBar = (SeekBar) view.findViewById(R.id.dialog_seek_bar_sb);
        mValueView = (TextView) view.findViewById(R.id.dialog_seek_bar_value_text);

        if (mSeekBarDialogPreference != null) {
            verifySeekBarSettings(
                    mSeekBarDialogPreference.getMinValue(),
                    mSeekBarDialogPreference.getDefaultValue(),
                    mSeekBarDialogPreference.getMaxValue()
            );
            mUnit = mSeekBarDialogPreference.getUnit();
            mKey = mSeekBarDialogPreference.getKey();
        }


        mSavedValue = getSavedValue(mKey);
        mSeekBar.setProgress(convertValueToProgress(mSavedValue));
        mSeekBar.setOnSeekBarChangeListener(this);
        mValueView.setText(" " + mSavedValue + " " + mUnit);


    }

    private int getSavedValue(String mKey) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sp.getInt(mKey, (int) mDefault);
    }

    private void verifySeekBarSettings(int minValue, int defaultValue, int maxValue) {
        if (minValue <= defaultValue && minValue < maxValue && defaultValue <= maxValue) {
            mMin = minValue;
            mDefault = defaultValue;
            mMax = maxValue;
        }
    }


    public static SeekBarDialogPreferenceFragmentCompat newInstance(Preference preference) {
        SeekBarDialogPreferenceFragmentCompat fragment = new SeekBarDialogPreferenceFragmentCompat();
        fragment.setSeekBarDialogPreference(preference);
        Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, preference.getKey());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mCurrentValue = convertProgressToValue(progress);
        mValueView.setText(" " + mCurrentValue + " " + mUnit);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void setSeekBarDialogPreference(Preference preference) {
        if (preference instanceof SeekBarDialogPreference) {
            this.mSeekBarDialogPreference = (SeekBarDialogPreference) preference;

        }

    }

    private int convertValueToProgress(int value) {
        float step = (mMax - mMin) / 100;
        return (int) ((value - mMin) / step);
    }

    private int convertProgressToValue(int progress) {
        float step = (mMax - mMin) / 100;
        return (int) (mMin + progress * step);
    }




}
