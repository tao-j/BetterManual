package com.obsidium.bettermanual.capture;

import com.obsidium.bettermanual.CameraUiInterface;
import com.obsidium.bettermanual.R;


public abstract class CaptureMode implements CaptureModeInterface
{

    public static final int COUNTDOWN_SECONDS = 5;

    protected CameraUiInterface cameraUiInterface;
    protected boolean isActive = false;
    protected int             m_countdown;

    public CaptureMode(CameraUiInterface cameraUiInterface)
    {
        this.cameraUiInterface = cameraUiInterface;
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
                cameraUiInterface.showMessage(String.format("Starting in %d...", m_countdown));
                cameraUiInterface.getActivityInterface().getMainHandler().postDelayed(this, 1000);
            }
            else
            {
                cameraUiInterface.hideMessage();
                startShooting();
            }
        }
    };

    @Override
    public void startCountDown() {
        isActive = true;
        //cameraUiInterface.getActivityInterface().getCamera().disableHwShutterButton();
        cameraUiInterface.showHintMessage(cameraUiInterface.getActivityInterface().getResString(R.string.icon_enterButton) + " to abort");
        // Stop preview (doesn't seem to preserve battery life?)
        cameraUiInterface.getActivityInterface().getCamera().stopDisplay();

        // Hide some bottom views
        cameraUiInterface.getActivityInterface().getPreferences().setViewFlags(cameraUiInterface.getActiveViewsFlag());
        cameraUiInterface.setActiveViewFlag(0);
        cameraUiInterface.updateViewVisibility();

        // Start countdown
        m_countdown = COUNTDOWN_SECONDS;
        cameraUiInterface.showMessage(String.format("Starting in %d...", m_countdown));
        cameraUiInterface.getActivityInterface().getMainHandler().postDelayed(m_countDownRunnable, 1000);
    }
}
