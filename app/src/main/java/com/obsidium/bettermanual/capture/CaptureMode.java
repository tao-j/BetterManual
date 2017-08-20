package com.obsidium.bettermanual.capture;

import com.obsidium.bettermanual.ActivityInterface;
import com.sony.scalar.hardware.CameraEx;


public abstract class CaptureMode implements CaptureModeInterface
{

    public static final int COUNTDOWN_SECONDS = 5;

    protected ActivityInterface activityInterface;
    protected boolean isActive = false;
    protected int             m_countdown;

    public CaptureMode(ActivityInterface activityInterface)
    {
        this.activityInterface = activityInterface;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    protected final Runnable  m_countDownRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (--m_countdown > 0)
            {
                activityInterface.showMessage(String.format("Starting in %d...", m_countdown));
                activityInterface.getMainHandler().postDelayed(this, 1000);
            }
            else
            {
                activityInterface.hideMessage();
                startShooting();
            }
        }
    };

    @Override
    public void startCountDown() {
        isActive = true;
        activityInterface.getCamera().stopDirectShutter(new CameraEx.DirectShutterStoppedCallback()
        {
            @Override
            public void onShutterStopped(CameraEx cameraEx)
            {
            }
        });
        activityInterface.showHintMessage("\uE04C to abort");
        // Stop preview (doesn't seem to preserve battery life?)
        activityInterface.getCamera().getNormalCamera().stopPreview();

        // Hide some bottom views
        activityInterface.getPreferences().setViewFlags(activityInterface.getActiveViewsFlag());
        activityInterface.setActiveViewFlag(0);
        activityInterface.updateViewVisibility();

        // Start countdown
        m_countdown = COUNTDOWN_SECONDS;
        activityInterface.showMessage(String.format("Starting in %d...", m_countdown));
        activityInterface.getMainHandler().postDelayed(m_countDownRunnable, 1000);
    }
}
