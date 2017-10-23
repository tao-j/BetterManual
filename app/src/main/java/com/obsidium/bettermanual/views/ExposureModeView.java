package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.obsidium.bettermanual.R;
import com.obsidium.bettermanual.camera.CameraInstance;
import com.sony.scalar.hardware.CameraEx;



public class ExposureModeView extends BaseImageView {
    private final String TAG = ExposureModeView.class.getSimpleName();

    @Override
    public void setIn_DecrementValue(int value) {

    }

    @Override
    public String getNavigationString() {
        return getResources().getString(R.string.view_drivemode_navigation);
    }

    public enum ExposureModes { manual, aperture, shutter, other }
    private ExposureModes m_exposureModes;


    public ExposureModeView(Context context) {
        super(context);
    }

    public ExposureModeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExposureModeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ExposureModes get()
    {
        return m_exposureModes;
    }



    public void updateImage()
    {
        String mode = CameraInstance.GET().getSceneMode();
        Log.d(TAG, "updateImage:" + mode);
        //log(String.format("updateImage %s\n", mode));
        if (mode.equals(CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE))
        {
            //noinspection ResourceType
            setImageResource(getResources().getInteger(R.integer.s_16_dd_parts_osd_icon_mode_m));
            m_exposureModes = ExposureModes.manual;
        }
        else if (mode.equals(CameraEx.ParametersModifier.SCENE_MODE_APERTURE_PRIORITY))
        {
            //noinspection ResourceType
            setImageResource(getResources().getInteger(R.integer.s_16_dd_parts_osd_icon_mode_a));
            m_exposureModes = ExposureModes.aperture;
        }
        else if (mode.equals(CameraEx.ParametersModifier.SCENE_MODE_SHUTTER_PRIORITY))
        {
            //noinspection ResourceType
            setImageResource(getResources().getInteger(R.integer.s_16_dd_parts_osd_icon_mode_s));
            m_exposureModes = ExposureModes.shutter;
        }
        else
        {
            //noinspection ResourceType
            setImageResource(getResources().getInteger(R.integer.p_dialogwarning));
            m_exposureModes = ExposureModes.other;
        }
    }

    public void toggle()
    {
        Log.d(TAG,"toggle()");
        final String newMode;
        switch (m_exposureModes)
        {
            case manual:
                newMode = CameraEx.ParametersModifier.SCENE_MODE_APERTURE_PRIORITY;
                /*if (activity.getDialMode() != CameraUiFragment.DialMode.mode)
                    activity.setDialMode(activity.getAperture().haveApertureControl() ? CameraUiFragment.DialMode.aperture : CameraUiFragment.DialMode.iso);*/
                CameraInstance.GET().setAutoShutterSpeedLowLimit(activity.getPreferences().getMinShutterSpeed());
                break;
            default:
                newMode = CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE;
                /*if (activity.getDialMode() != CameraUiFragment.DialMode.mode)
                    activity.setDialMode(CameraUiFragment.DialMode.shutter);*/
                CameraInstance.GET().setAutoShutterSpeedLowLimit(-1);
                break;
        }
        Log.d(TAG,"newmode:" + newMode);
        CameraInstance.GET().setSceneMode(newMode);
        updateImage();
    }

}
