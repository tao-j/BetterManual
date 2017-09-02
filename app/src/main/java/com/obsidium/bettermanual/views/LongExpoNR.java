package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;

import com.obsidium.bettermanual.R;


public class LongExpoNR extends BaseImageView {
    public LongExpoNR(Context context) {
        super(context);
    }

    public LongExpoNR(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongExpoNR(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setIn_DecrementValue(int value) {

    }

    @Override
    public void updateImage() {
        if (activity.getCamera().getLongeExposureNR()) {
            setImageResource(R.drawable.lnr_on);
        }
        else
            setImageResource(R.drawable.lnr_off);
    }

    @Override
    public void toggle() {
        if (activity.getCamera().getLongeExposureNR()) {
            activity.getCamera().setLongExposureNoiseReduction(false);
        }
        else
            activity.getCamera().setLongExposureNoiseReduction(true);
        updateImage();
    }
}
