package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;

import com.obsidium.bettermanual.R;
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
        final String driveMode = activity.getCamera().getDriveMode();
        if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_SINGLE))
        {
            //noinspection ResourceType
            setImageResource(getResources().getInteger(R.integer.p_drivemode_n_001));
        }
        else if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_BURST))
        {
            final String burstDriveSpeed = activity.getCamera().getBurstDriveSpeed();
            if (burstDriveSpeed.equals(CameraEx.ParametersModifier.BURST_DRIVE_SPEED_LOW))
            {
                //noinspection ResourceType
                setImageResource(getResources().getInteger(R.integer.p_drivemode_n_003));
            }
            else if (burstDriveSpeed.equals(CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH))
            {
                //noinspection ResourceType
                setImageResource(getResources().getInteger(R.integer.p_drivemode_n_002));
            }
        }
        else //if (driveMode.equals("bracket"))
        {
            // Don't really care about this here
            //noinspection ResourceType
            setImageResource(getResources().getInteger(R.integer.p_dialogwarning));
        }
    }

    @Override
    public void toggle() {
        final String driveMode = activity.getCamera().getDriveMode();
        final String newMode;
        final String newBurstSpeed;
        if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_SINGLE))
        {
            newMode = CameraEx.ParametersModifier.DRIVE_MODE_BURST;
            newBurstSpeed = CameraEx.ParametersModifier.BURST_DRIVE_SPEED_HIGH;
        }
        else if (driveMode.equals(CameraEx.ParametersModifier.DRIVE_MODE_BURST))
        {
            final String burstDriveSpeed = activity.getCamera().getBurstDriveSpeed();
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

        activity.getCamera().setDriveMode(newMode);
        activity.getCamera().setBurstDriveSpeed(newBurstSpeed);

        updateImage();
    }

    @Override
    public void setIn_DecrementValue(int value) {

    }
}
