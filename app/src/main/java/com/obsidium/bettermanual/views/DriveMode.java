package com.obsidium.bettermanual.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import com.obsidium.bettermanual.SonyDrawables;
import com.sony.scalar.hardware.CameraEx;

public class DriveMode extends BaseImageView
{
    public DriveMode(Context context) {
        super(context);
    }

    public DriveMode(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DriveMode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void updateImage() {
        final CameraEx.ParametersModifier paramsModifier = activity.getCamera().createParametersModifier(activity.getCamera().getNormalCamera().getParameters());
        final String driveMode = paramsModifier.getDriveMode();
        if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_SINGLE))
        {
            //noinspection ResourceType
            setImageResource(SonyDrawables.p_drivemode_n_001);
        }
        else if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_BURST))
        {
            final String burstDriveSpeed = paramsModifier.getBurstDriveSpeed();
            if (burstDriveSpeed.equals(CameraEx.ParametersModifier.BURST_DRIVE_SPEED_LOW))
            {
                //noinspection ResourceType
                setImageResource(SonyDrawables.p_drivemode_n_003);
            }
            else if (burstDriveSpeed.equals(CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH))
            {
                //noinspection ResourceType
                setImageResource(SonyDrawables.p_drivemode_n_002);
            }
        }
        else //if (driveMode.equals("bracket"))
        {
            // Don't really care about this here
            //noinspection ResourceType
            setImageResource(SonyDrawables.p_dialogwarning);
        }
    }

    @Override
    public void toggle() {
        final Camera normalCamera = activity.getCamera().getNormalCamera();
        final CameraEx.ParametersModifier paramsModifier = activity.getCamera().createParametersModifier(normalCamera.getParameters());
        final String driveMode = paramsModifier.getDriveMode();
        final String newMode;
        final String newBurstSpeed;
        if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_SINGLE))
        {
            newMode = CameraEx.ParametersModifier.DRIVE_MODE_BURST;
            newBurstSpeed = CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH;
        }
        else if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_BURST))
        {
            final String burstDriveSpeed = paramsModifier.getBurstDriveSpeed();
            if (burstDriveSpeed.equals(CameraEx.ParametersModifier.BURST_DRIVE_SPEED_LOW))
            {
                newMode = CameraEx.ParametersModifier.DRIVE_MODE_SINGLE;
                newBurstSpeed = burstDriveSpeed;
            }
            else
            {
                newMode = driveMode;
                newBurstSpeed = CameraEx.ParametersModifier.BURST_DRIVE_SPEED_LOW;
            }
        }
        else
        {
            // Anything else...
            newMode = CameraEx.ParametersModifier.DRIVE_MODE_SINGLE;
            newBurstSpeed = CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH;
        }

        final Camera.Parameters params = activity.getCamera().createEmptyParameters();
        final CameraEx.ParametersModifier newParamsModifier = activity.getCamera().createParametersModifier(params);
        newParamsModifier.setDriveMode(newMode);
        newParamsModifier.setBurstDriveSpeed(newBurstSpeed);
        activity.getCamera().getNormalCamera().setParameters(params);

        updateImage();
    }
}
