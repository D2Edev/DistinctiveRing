package io.github.d2edev.distinctivering.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;

import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import io.github.d2edev.distinctivering.R;

/**
 * Created by d2e on 09.07.16.
 */
public class SeekBarDialogPreference extends DialogPreference {

    private static final String TAG = "TAG_" + SeekBarDialogPreference.class.getSimpleName();
    private Context mContext;
    private SeekBar mSeekBar;
    private TextView mValueText;
    private int mDefault, mMax, mMin;
    private String mUnit, mKey;
    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
    private static final String APPNS = "http://schemas.android.com/apk/res-auto";


    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Get seekbear settinns from xml :
        int[] attrsArray = new int[]{
                R.attr.min,
                android.R.attr.defaultValue,
                android.R.attr.max,
                R.attr.step,
                R.attr.unit,
        };
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, attrsArray);
        mMin = typedArray.getInt(0, 20);
        mDefault = typedArray.getInt(1, 30);
        mMax = typedArray.getInt(2, 120);
        mUnit = typedArray.getString(4);
        //TODO step implementation
        setDialogLayoutResource(R.layout.dialog_seek_bar);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);

    }


    public int getMaxValue() {
        return mMax;
    }

    public int getMinValue() {
        return mMin;
    }

    public int getDefaultValue() {
        return mDefault;
    }

    public String getUnit() {
        return mUnit;
    }


    @Override
    public void notifyChanged() {
        super.notifyChanged();
    }

    @Override
    public boolean persistInt(int value) {
        return super.persistInt(value);
    }

    @Override
    public boolean shouldPersist() {
        return super.shouldPersist();
    }
}
