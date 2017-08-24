package com.obsidium.bettermanual.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Pair;

import com.obsidium.bettermanual.CameraUtil;
import com.obsidium.bettermanual.MinShutterActivity;
import com.sony.scalar.hardware.CameraEx;

public class ShutterView extends BaseTextView {

    private class ShutterSetRunner implements Runnable
    {
        private int direction;
        public ShutterSetRunner(int direction)
        {
            this.direction = direction;
        }
        @Override
        public void run() {
            if (direction > 0)
                activityInterface.getCamera().decrementShutterSpeed();
            else
                activityInterface.getCamera().incrementShutterSpeed();
        }
    }

    // Shutter speed
    private boolean         m_notifyOnNextShutterSpeedChange;
    public ShutterView(Context context) {
        super(context);
    }

    public ShutterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShutterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onScrolled(int distance) {
        if (activityInterface.getIso().getCurrentIso() != 0)
        {
            activityInterface.getBackHandler().post(new ShutterSetRunner(distance));
        }

    }

    @Override
    public boolean onClick() {
        if (activityInterface.getExposureMode().get() == ExposureModeView.ExposureModes.aperture)
        {
            // Set minimum shutter speed
            activityInterface.startActivity(MinShutterActivity.class);
            return true;
        }
        return false;
    }


    public void updateShutterSpeed(int n, int d)
    {
        final String text = CameraUtil.formatShutterSpeed(n, d);
        setText(text);
        if (m_notifyOnNextShutterSpeedChange)
        {
            activityInterface.showMessageDelayed(text);
            m_notifyOnNextShutterSpeedChange = false;
        }
    }

    public Pair<Integer, Integer> getCurrentShutterSpeed()
    {
        final Camera.Parameters params = activityInterface.getCamera().getNormalCamera().getParameters();
        final CameraEx.ParametersModifier paramsModifier = activityInterface.getCamera().createParametersModifier(params);
        return paramsModifier.getShutterSpeed();
    }
}
