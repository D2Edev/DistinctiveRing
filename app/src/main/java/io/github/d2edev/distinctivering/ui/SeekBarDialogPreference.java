package io.github.d2edev.distinctivering.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;

import android.util.AttributeSet;

import io.github.d2edev.distinctivering.R;

/**
 * Created by d2e on 09.07.16.
 */
public class SeekBarDialogPreference extends DialogPreference {

    private static final String TAG = "TAG_" + SeekBarDialogPreference.class.getSimpleName();
    public static final int MIN_VALUE = 0;
    public static final int DEF_VALUE = 50;
    public static final int MAX_VALUE = 100;
    public static final int MAX_STEP_QTY = 100;
    private int mDefault, mMax, mMin, mStepsQty;
    private String mUnit;


    public SeekBarDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SeekBarDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SeekBarDialogPreference);
        mMin = typedArray.getInt(R.styleable.SeekBarDialogPreference_minValue, MIN_VALUE);
        mDefault = typedArray.getInt(R.styleable.SeekBarDialogPreference_defValue, DEF_VALUE);
        mMax = typedArray.getInt(R.styleable.SeekBarDialogPreference_maxValue, MAX_VALUE);
        mUnit = typedArray.getString(R.styleable.SeekBarDialogPreference_unitName);
        mStepsQty = typedArray.getInt(R.styleable.SeekBarDialogPreference_stepsNumber, MAX_STEP_QTY);
        //attrs check
        if(mMax==mMin){
            throw new IllegalArgumentException("Provided minValue and maxValue are equal.");
        }
        //switch min|max if needed
        if(mMax<mMin){
            int tmp=mMax;
            mMax=mMin;
            mMin=tmp;
        }
        //normalize step qty if needed
        int possibleStepQTY= mMax-mMin;
        if(possibleStepQTY> MAX_STEP_QTY){
            possibleStepQTY= MAX_STEP_QTY;
        }
        if(mStepsQty<1||mStepsQty>possibleStepQTY){
            mStepsQty=possibleStepQTY;
        }
        //normalize default if needed
        if(mDefault<mMin||mDefault>mMax){
            mDefault=mMin+(mMax-mMin)/2;
        }
        setDialogLayoutResource(R.layout.dialog_seek_bar);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
        typedArray.recycle();
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

    public int getStepQty() {        return mStepsQty;    }

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
