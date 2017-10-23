package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;

import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;


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
    public String getNavigationString() {
        return getResources().getString(R.string.view_drivemode_navigation);
    }

    @Override
    public void updateImage() {
        if (CameraInstance.GET().getLongeExposureNR()) {
            setImageResource(R.drawable.lnr_on);
        }
        else
            setImageResource(R.drawable.lnr_off);
    }

    @Override
    public void toggle() {
        if (CameraInstance.GET().getLongeExposureNR()) {
            CameraInstance.GET().setLongExposureNoiseReduction(false);
        }
        else
            CameraInstance.GET().setLongExposureNoiseReduction(true);
        updateImage();
    }
}
