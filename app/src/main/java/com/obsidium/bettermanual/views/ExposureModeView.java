package com.obsidium.bettermanual.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import com.obsidium.bettermanual.ManualActivity;
import com.obsidium.bettermanual.SonyDrawables;
import com.sony.scalar.hardware.CameraEx;



public class ExposureModeView extends BaseImageView {
    private final String TAG = ExposureModeView.class.getSimpleName();

    @Override
    public void setIn_DecrementValue(int value) {

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
        String mode = activity.getCamera().getNormalCamera().getParameters().getSceneMode();
        Log.d(TAG, "updateImage:" + mode);
        //log(String.format("updateImage %s\n", mode));
        if (mode.equals(CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE))
        {
            //noinspection ResourceType
            setImageResource(SonyDrawables.s_16_dd_parts_osd_icon_mode_m);
            m_exposureModes = ExposureModes.manual;
        }
        else if (mode.equals(CameraEx.ParametersModifier.SCENE_MODE_APERTURE_PRIORITY))
        {
            //noinspection ResourceType
            setImageResource(SonyDrawables.s_16_dd_parts_osd_icon_mode_a);
            m_exposureModes = ExposureModes.aperture;
        }
        else if (mode.equals(CameraEx.ParametersModifier.SCENE_MODE_SHUTTER_PRIORITY))
        {
            //noinspection ResourceType
            setImageResource(SonyDrawables.s_16_dd_parts_osd_icon_mode_s);
            m_exposureModes = ExposureModes.shutter;
        }
        else
        {
            //noinspection ResourceType
            setImageResource(SonyDrawables.p_dialogwarning);
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
                /*if (activity.getDialMode() != ManualActivity.DialMode.mode)
                    activity.setDialMode(activity.getAperture().haveApertureControl() ? ManualActivity.DialMode.aperture : ManualActivity.DialMode.iso);*/
                setMinShutterSpeed(activity.getPreferences().getMinShutterSpeed());
                break;
            default:
                newMode = CameraEx.ParametersModifier.SCENE_MODE_MANUAL_EXPOSURE;
                /*if (activity.getDialMode() != ManualActivity.DialMode.mode)
                    activity.setDialMode(ManualActivity.DialMode.shutter);*/
                setMinShutterSpeed(-1);
                break;
        }
        Log.d(TAG,"newmode:" + newMode);
        setSceneMode(newMode);
    }

    private void setSceneMode(String mode)
    {
        Camera.Parameters params = activity.getCamera().createEmptyParameters();
        params.setSceneMode(mode);
        activity.getCamera().getNormalCamera().setParameters(params);
        updateImage();
    }

    private void setMinShutterSpeed(int speed)
    {
        final Camera.Parameters params = activity.getCamera().createEmptyParameters();
        final CameraEx.ParametersModifier modifier = activity.getCamera().createParametersModifier(params);
        modifier.setAutoShutterSpeedLowLimit(speed);
        activity.getCamera().getNormalCamera().setParameters(params);
    }
}
