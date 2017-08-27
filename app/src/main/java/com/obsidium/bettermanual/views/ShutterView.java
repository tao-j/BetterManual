package com.obsidium.bettermanual.views;

import android.content.Context;
import android.util.AttributeSet;

import com.github.killerink.MainActivity;
import com.obsidium.bettermanual.CameraUtil;

public class ShutterView extends BaseTextView {

    @Override
    public void setIn_DecrementValue(int value) {
        onScrolled(value);
    }

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
                cameraUiInterface.getActivityInterface().getCamera().decrementShutterSpeed();
            else
                cameraUiInterface.getActivityInterface().getCamera().incrementShutterSpeed();
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
        cameraUiInterface.getActivityInterface().getBackHandler().post(new ShutterSetRunner(distance));
    }

    @Override
    public boolean onClick() {
        if (cameraUiInterface.getExposureMode().get() == ExposureModeView.ExposureModes.aperture && !getText().equals("BULB"))
        {
            // Set minimum shutter speed
            cameraUiInterface.getActivityInterface().loadFragment(MainActivity.FRAGMENT_MIN_SHUTTER);
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
            cameraUiInterface.showMessageDelayed(text);
            m_notifyOnNextShutterSpeedChange = false;
        }
    }

}
